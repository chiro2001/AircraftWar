package work.chiro.game.application;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import work.chiro.game.compatible.XGraphicsPC;
import work.chiro.game.config.RunningConfig;
import work.chiro.game.config.RunningConfigPC;
import work.chiro.game.game.Game;
import work.chiro.game.scene.SceneRun;
import work.chiro.game.storage.history.HistoryImpl;
import work.chiro.game.storage.history.HistoryObjectFactory;
import work.chiro.game.utils.Utils;
import work.chiro.game.utils.UtilsPC;
import work.chiro.game.utils.timer.Timer;
import work.chiro.game.windows.HistoryWindow;
import work.chiro.game.x.compatible.XGraphics;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class GamePanel extends JPanel {
    private Font myFontBase = null;
    private final HeroControllerImpl heroControllerImpl = HeroControllerImpl.getInstance(this);
    private final Game game;
    private String lastProvidedName = null;
    private String lastProvidedMessage = null;
    private final Object waitObject = new Object();
    private static double scale = 1.0;
    private static boolean justResized = false;

    public Game getGame() {
        return game;
    }

    public static void setScale(double scale) {
        GamePanel.scale = scale;
    }

    public static double getScale() {
        return scale;
    }

    public static boolean getJustResized() {
        return GamePanel.justResized;
    }

    public void resetStates() {
        heroControllerImpl.clear();
        game.resetStates();
    }

    public void action() {
        game.action();
        addTimers();
    }

    public GamePanel() {
        game = new Game(heroControllerImpl);
        loadFont();
        Utils.getLogger().info("GamePanel instance created!");
        game.setOnFinish(() -> {
            Utils.getLogger().info("finish!");
            try {
                String name = JOptionPane.showInputDialog("输入你的名字", lastProvidedName == null ? "Nanshi" : lastProvidedName);
                if (name == null) {
                    String name2 = JOptionPane.showInputDialog("输入你的名字", lastProvidedName == null ? "Nanshi" : lastProvidedName);
                    if (name2 == null) {
                        int res = JOptionPane.showConfirmDialog(null, "不保存记录?", "Save Game", JOptionPane.OK_CANCEL_OPTION);
                        if (res == JOptionPane.YES_OPTION) {
                            throw new Exception();
                        }
                    } else {
                        lastProvidedName = name2;
                    }
                } else {
                    lastProvidedName = name;
                }
                String message = JOptionPane.showInputDialog("输入额外的信息", lastProvidedMessage == null ? "NO MESSAGE" : lastProvidedMessage);
                lastProvidedMessage = message;
                // 保存游戏结果
                if (RunningConfig.score > 0) {
                    HistoryImpl.getInstance().addOne(
                            new HistoryObjectFactory(
                                    name == null ? "Nanshi" : name.isEmpty() ? "Nanshi" : name,
                                    RunningConfig.score,
                                    message == null ? "NO MESSAGE" : message.isEmpty() ? "NO MESSAGE" : message,
                                    RunningConfig.difficulty)
                                    .create());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.getLogger().warn("Input exception: " + e);
            } finally {
                SceneRun.getInstance().setNextScene(HistoryWindow.class);
                synchronized (waitObject) {
                    waitObject.notify();
                }
            }
        });
        game.setOnPaint(this::repaint);
        game.setOnFrame(heroControllerImpl::onFrame);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                // if (RunningConfig.allowResize) {
                UtilsPC.refreshWindowSize(getWidth(), getHeight());
                justResized = true;
                // }
            }
        });
    }

    public void addTimers() {
        // 获取键盘焦点
        game.getTimerController().add(new Timer(100, this::requestFocus));
    }

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param g 绘图
     */
    @Override
    public void paint(Graphics g) {
        double timeStart = Utils.getTimeMills();
        VolatileImage thisFrame = getGraphicsConfiguration().createCompatibleVolatileImage(RunningConfigPC.displayWindowWidth, RunningConfigPC.displayWindowHeight);
        Graphics2D graphicsNew = thisFrame.createGraphics();

        XGraphics xGraphics = new XGraphicsPC() {
            @Override
            protected Graphics2D getGraphics() {
                return graphicsNew;
            }

            @Override
            protected GraphicsConfiguration getXGraphicsConfiguration() {
                return getGraphicsConfiguration();
            }
        };

        xGraphics.paintInOrdered(game);

        double timePaint = Utils.getTimeMills();

        //绘制得分和生命值
        // paintInfo(graphicsNew);
        xGraphics.paintInfo(game);

        double timePaintInfo = Utils.getTimeMills();

        // resize 到显示帧
        g.drawImage(thisFrame, 0, 0, null);
        double timeResize = Utils.getTimeMills();
        graphicsNew.dispose();
        Utils.getLogger().debug("paint -- object: {}, info: {}, resize: {}", timePaint - timeStart, timePaintInfo - timePaint, timeResize - timePaintInfo);

        justResized = false;
    }

    // private void paintInfo(Graphics g) {
    //     int x = 10;
    //     int y = 25;
    //     g.setColor(new Color(0xcfcfcfcf));
    //     g.setFont(myFontBase);
    //     g.drawString("SCORE:" + (int) (RunningConfig.score), x, y);
    //     y = y + 20;
    //     g.drawString("LIFE:" + (int) (HeroAircraftFactory.getInstance().getHp()), x, y);
    //     y = y + 20;
    //     BossEnemy boss = BossEnemyFactory.getInstance();
    //     if (boss == null) {
    //         g.drawString("Before Boss:" + (int) (game.getNextBossScore() - RunningConfig.score), x, y);
    //     } else {
    //         g.drawString("BOSS LIFE:" + (int) (boss.getHp()), x, y);
    //     }
    //     y = y + 20;
    //     g.drawString("FPS:" + game.getTimerController().getFps(), x, y);
    // }

    private void loadFont() {
        try {
            myFontBase = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("/fonts/genshin.ttf"))).deriveFont(22f);
        } catch (FontFormatException | IOException e) {
            myFontBase = new Font("SansSerif", Font.PLAIN, 22);
        }
    }

    public Object getWaitObject() {
        return waitObject;
    }
}
