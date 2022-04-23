package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.background.AbstractBackground;
import edu.hitsz.background.BasicBackgroundFactory;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.dao.HistoryImpl;
import edu.hitsz.dao.HistoryObjectFactory;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.timer.Timer;
import edu.hitsz.timer.TimerController;
import edu.hitsz.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    /**
     * 创建线程的工厂函数
     */
    static private final MyThreadFactory THREAD_FACTORY = new MyThreadFactory("AircraftWar");
    /**
     * 线程池，自动管理
     */
    @SuppressWarnings("AlibabaThreadPoolCreation")
    static private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100, THREAD_FACTORY);

    public static MyThreadFactory getThreadFactory() {
        return THREAD_FACTORY;
    }

    private HeroAircraft heroAircraft = new HeroAircraftFactory().create();
    private final List<AbstractAircraft> heroAircrafts = new LinkedList<>();
    private final List<AbstractBackground> backgrounds = new LinkedList<>();
    private final List<BossEnemy> bossAircrafts = new LinkedList<>();
    private final List<AbstractAircraft> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();
    private final List<AbstractProp> props = new LinkedList<>();
    private final List<List<? extends AbstractFlyingObject>> allObjects = Arrays.asList(
            backgrounds, heroBullets, enemyBullets, enemyAircrafts, bossAircrafts, heroAircrafts, props
    );
    private boolean gameOverFlag = false;
    private boolean startedFlag = false;
    private int score = 0;
    private final int bossScoreThreshold = 1000;
    private int nextBossScore = score + bossScoreThreshold;
    private final Object waitObject = new Object();
    @SuppressWarnings("rawtypes")
    private Future future = null;

    public void resetStates() {
        gameOverFlag = false;
        score = 0;
        enemyBullets.clear();
        enemyAircrafts.clear();
        props.clear();
        heroAircraft = new HeroAircraftFactory().clearInstance().create();
        heroAircrafts.clear();
        heroAircrafts.add(heroAircraft);
    }

    public boolean getGameOverFlag() {
        return gameOverFlag;
    }

    public boolean getStartedFlag() {
        return startedFlag;
    }

    public Object getWaitObject() {
        return waitObject;
    }

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    @SuppressWarnings("FieldCanBeLocal")
    public Game() {
        heroAircrafts.add(heroAircraft);
        backgrounds.add(new BasicBackgroundFactory().create());
        //启动英雄机鼠标监听
        new HeroController(this);
    }

    public void addEvents() {
        TimerController.init(0);
        // 英雄射击事件
        TimerController.add(new Timer(1, () -> {
            synchronized (heroBullets) {
                heroBullets.addAll(heroAircraft.shoot());
            }
        }));
        // 产生精英敌机事件
        TimerController.add(new Timer(1200, () -> {
            synchronized (enemyAircrafts) {
                enemyAircrafts.add(new EliteEnemyFactory().create());
            }
        }));
        // 产生普通敌机事件
        TimerController.add(new Timer(700, () -> {
            synchronized (enemyAircrafts) {
                enemyAircrafts.add(new MobEnemyFactory().create());
            }
        }));
        // 敌机射击事件
        TimerController.add(new Timer(200, () -> {
            synchronized (enemyBullets) {
                enemyAircrafts.forEach(enemyAircraft -> enemyBullets.addAll(enemyAircraft.shoot()));
                bossAircrafts.forEach(bossEnemy -> enemyBullets.addAll(bossEnemy.shoot()));
            }
        }));
        // fps 输出事件
        TimerController.add(new Timer(1000, () -> System.out.println("fps: " + TimerController.getFps())));
        // boss 生成事件
        TimerController.add(new Timer(100, () -> {
            if (score > nextBossScore && bossAircrafts.isEmpty()) {
                synchronized (bossAircrafts) {
                    bossAircrafts.add(new BossEnemyFactory(() -> {
                        nextBossScore = bossScoreThreshold + score;
                        BossEnemyFactory.clearInstance();
                    }).create());
                }
            }
        }));
    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {
        startedFlag = true;
        HistoryImpl.getInstance().display();
        addEvents();
        Utils.startMusic(MusicManager.MusicType.BGM);
        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {
            try {
                TimerController.update();
                // execute all
                TimerController.execute();
                // 所有物体移动
                synchronized (allObjects) {
                    allObjects.forEach(objList -> objList.forEach(AbstractFlyingObject::forward));
                }
                // 撞击检测
                crashCheckAction();
                // 后处理
                synchronized (allObjects) {
                    allObjects.forEach(objList -> objList.removeIf(AbstractFlyingObject::notValid));
                }
                // 每个时刻重绘界面
                repaint();

                // 游戏结束检查
                if (heroAircraft.getHp() <= 0) {
                    if (future != null) {
                        System.out.println("cancel future: " + future);
                        future.cancel(true);
                    }
                    // 游戏结束
                    gameOverFlag = true;
                    System.out.println("Game Over!");
                    String name = JOptionPane.showInputDialog("输入你的名字");
                    String message = JOptionPane.showInputDialog("输入额外的信息");
                    // 保存游戏结果
                    if (score > 0) {
                        HistoryImpl.getInstance().addOne(new HistoryObjectFactory(name.isEmpty() ? "NONAME" : name, score, message.isEmpty() ? "NO MESSAGE" : message).create());
                    }
                    HistoryImpl.getInstance().display();
                    synchronized (waitObject) {
                        waitObject.notify();
                    }
                }
                TimerController.done();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("this thread will exit: " + e);
            }
        };
        int timeInterval = 1;
        // 以固定延迟时间进行执行本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
        future = executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            BossEnemy boss = BossEnemyFactory.getInstance();
            if (boss != null) {
                if (bullet.crash(boss)) {
                    boss.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (boss.notValid()) {
                        score += boss.getScore();
                    }
                    continue;
                }
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 获得分数，添加掉落道具
                        score += enemyAircraft.getScore();
                        props.addAll(enemyAircraft.dropProps());
                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (bullet.crash(heroAircraft)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 我方获得道具，道具生效
        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (prop.crash(heroAircraft)) {
                prop.handleAircrafts(enemyAircrafts);
                prop.vanish();
            }
        }

    }

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param g 绘图
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制所有物体
        synchronized (allObjects) {
            allObjects.forEach(objList -> {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (objList) {
                    objList.forEach(obj -> obj.draw(g));
                }
            });
        }

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
        y = y + 20;
        BossEnemy boss = BossEnemyFactory.getInstance();
        if (boss == null) {
            g.drawString("Before Boss:" + (nextBossScore - score), x, y);
        } else {
            g.drawString("BOSS LIFE:" + boss.getHp(), x, y);
        }
    }
}
