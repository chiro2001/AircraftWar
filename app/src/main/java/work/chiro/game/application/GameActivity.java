package work.chiro.game.application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import work.chiro.game.aircraft.BossEnemy;
import work.chiro.game.aircraft.BossEnemyFactory;
import work.chiro.game.aircraft.HeroAircraftFactory;
import work.chiro.game.basic.AbstractFlyingObject;
import work.chiro.game.basic.BasicCallback;
import work.chiro.game.compatible.HeroControllerAndroidImpl;
import work.chiro.game.compatible.HistoryImplAndroid;
import work.chiro.game.compatible.ResourceProvider;
import work.chiro.game.compatible.XGraphics;
import work.chiro.game.compatible.XImage;
import work.chiro.game.compatible.XImageFactory;
import work.chiro.game.config.Constants;
import work.chiro.game.config.RunningConfig;
import work.chiro.game.history.HistoryObjectFactory;
import work.chiro.game.resource.ImageManager;
import work.chiro.game.resource.MusicType;
import work.chiro.game.thread.MyThreadFactory;
import work.chiro.game.utils.Utils;

public class GameActivity extends AppCompatActivity {
    private SurfaceHolder surfaceHolder;
    private Game game = null;
    SurfaceView surfaceView = null;
    private final HeroControllerAndroidImpl heroControllerAndroid = new HeroControllerAndroidImpl();
    private Typeface font;
    static private float canvasScale = 1.0f;
    static private final int fontSize = 20;

    private abstract static class XGraphicsPart implements XGraphics {
        private double alpha = 1.0;
        private double rotation = 0.0;
        private int color = 0x0;

        @Override
        public XImage<?> drawImage(XImage<?> image, double x, double y) {
            Paint paint = new Paint();
            paint.setAlpha((int) (alpha * 255));
            if (alpha == 0) {
                getCanvas().drawBitmap((Bitmap) image.getImage(), (float) x, (float) y, paint);
            } else {
                Matrix matrix = new Matrix();
                matrix.postTranslate(-(float) image.getWidth() / 2, -(float) image.getHeight() / 2);
                matrix.postRotate((float) (rotation * 180 / Math.PI));
                matrix.postTranslate((float) (image.getWidth() / 2 + x), (float) (image.getHeight() / 2 + y));
                getCanvas().drawBitmap((Bitmap) image.getImage(), matrix, paint);
            }
            return image;
        }

        @Override
        public XImage<?> drawImage(XImage<?> image, double x, double y, double w, double h) {
            if (image.getWidth() != (int) w || image.getHeight() != (int) h) {
                System.out.println("WARN: update cache image");
                Matrix matrix = new Matrix();
                matrix.postScale((float) (w / image.getWidth()), (float) (h / image.getHeight()));
                Bitmap scaledBitmap = Bitmap.createBitmap((Bitmap) image.getImage(), 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                return drawImage(new XImageFactory().create(scaledBitmap), x, y);
            } else {
                return drawImage(image, x, y);
            }
        }

        @Override
        public XGraphics setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public XGraphics setRotation(double rotation) {
            this.rotation = rotation;
            return this;
        }

        @Override
        public XGraphics setColor(int color) {
            this.color = color;
            return this;
        }

        @Override
        public XGraphics fillRect(double x, double y, double width, double height) {
            Paint paint = new Paint();
            paint.setColor(color);
            getCanvas().drawRect((int) x, (int) y, (int) (x + width), (int) (y + height), paint);
            return this;
        }

        @Override
        public XGraphics drawString(String text, double x, double y) {
            Paint paint = getPaint();
            paint.setColor(color);
            paint.setTextSize(fontSize * 3.0f / canvasScale);
            getCanvas().drawText(text, (int) x, (int) y, paint);
            return this;
        }

        abstract protected Canvas getCanvas();

        abstract protected Paint getPaint();
    }

    private void draw() {
        List<List<? extends AbstractFlyingObject>> allObjects = game.getAllObjects();
        // Canvas canvas = surfaceHolder.lockCanvas();
        // 使用硬件加速
        Canvas canvas = surfaceHolder.lockHardwareCanvas();
        // 储存当前 canvas 设置
        canvas.save();
        // 设置缩放和偏移
        if (RunningConfig.allowResize) {
            canvas.scale(1.0f, 1.0f);
        } else {
            int windowWidth = surfaceView.getMeasuredWidth();
            int windowHeight = surfaceView.getMeasuredHeight();
            canvas.translate((windowWidth * 1.0f - RunningConfig.windowWidth * canvasScale) / 2, (windowHeight * 1.0f - RunningConfig.windowHeight * canvasScale) / 2);
            canvas.scale(canvasScale, canvasScale);
        }
        Paint paint = new Paint();
        XGraphicsPart xGraphics = new XGraphicsPart() {
            @Override
            protected Canvas getCanvas() {
                return canvas;
            }

            @Override
            protected Paint getPaint() {
                return paint;
            }
        };
        canvas.drawColor(Color.BLACK);

        // 绘制所有物体
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (allObjects) {
            allObjects.forEach(objList -> {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (objList) {
                    objList.forEach(obj -> obj.draw(xGraphics));
                }
            });
        }

        paintInfo(xGraphics);
        // 恢复 canvas 设置之后绘制遮罩
        canvas.restore();
        paintMask(xGraphics);

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void paintMask(XGraphicsPart g) {
        Paint paint = new Paint();
        paint.setColor(0x0);
        // g.getCanvas().drawRect(0, 0, );
    }

    private void paintInfo(XGraphicsPart g) {
        int d = (int) (fontSize * 3.0f / canvasScale);
        int x = 10;
        int y = d;
        g.setColor(0xcfcfcfcf);
        g.getPaint().setTypeface(font);
        g.drawString("SCORE:" + (int) (RunningConfig.score), x, y);
        y = y + d;
        g.drawString("LIFE:" + (int) (HeroAircraftFactory.getInstance().getHp()), x, y);
        y = y + d;
        BossEnemy boss = BossEnemyFactory.getInstance();
        if (boss == null) {
            g.drawString("Before Boss:" + (int) (game.getNextBossScore() - RunningConfig.score), x, y);
        } else {
            g.drawString("BOSS LIFE:" + (int) (boss.getHp()), x, y);
        }
        y = y + d;
        g.drawString("FPS:" + game.getTimerController().getFps(), x, y);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResourceProvider.setInstance(new ResourceProvider() {
            @Override
            public XImage<?> getImageFromResource(String path) throws IOException {
                Bitmap bitmap = BitmapFactory.decodeStream(Utils.class.getResourceAsStream("/images/" + path));
                if (bitmap == null) {
                    throw new IOException("file: " + path + " not found!");
                }
                return new XImageFactory().create(bitmap);
            }

            private final Map<MusicType, Integer> musicResource = Map.of(
                    MusicType.BGM, R.raw.bgm,
                    MusicType.BGM_BOSS, R.raw.bgm_boss,
                    MusicType.GAME_OVER, R.raw.game_over,
                    MusicType.BOMB_EXPLOSION, R.raw.bomb_explosion,
                    MusicType.HERO_HIT, R.raw.bullet_hit,
                    MusicType.HERO_SHOOT, R.raw.bullet,
                    MusicType.PROPS, R.raw.get_supply
            );
            private final Map<MusicType, Integer> musicIDMap = new HashMap<>();
            private final Map<MusicType, Boolean> musicNoStop = new HashMap<>();
            private SoundPool soundPool = null;

            @Override
            public void musicLoadAll() {
                if (soundPool != null) {
                    soundPool.release();
                }
                musicIDMap.clear();
                SoundPool.Builder spb = new SoundPool.Builder();
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                spb.setMaxStreams(Constants.ANDROID_SOUND_STREAM_MAX);
                // 转换音频格式
                spb.setAudioAttributes(audioAttributes);
                // 创建SoundPool对象
                soundPool = spb.build();
                musicResource.forEach((type, resourceID) -> musicIDMap.put(type, soundPool.load(GameActivity.this, resourceID, 1)));
            }

            public void startMusic(MusicType type, Boolean noStop, Boolean loop) {
                if (!RunningConfig.musicEnable) {
                    return;
                }
                musicNoStop.replace(type, noStop);
                try {
                    // noinspection ConstantConditions
                    soundPool.play(musicIDMap.get(type), 1, 1, 0, loop ? -1 : 0, 1);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void startMusic(MusicType type, Boolean noStop) {
                startMusic(type, noStop, false);
            }

            @Override
            public void stopMusic(MusicType type) {
                try {
                    // noinspection ConstantConditions
                    soundPool.stop(musicIDMap.get(type));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void stopAllMusic() {
                musicIDMap.forEach((type, id) -> {
                    if (!musicNoStop.containsKey(type)) {
                        stopMusic(type);
                    }
                });
            }

            @Override
            public void startLoopMusic(MusicType type) {
                startMusic(type, false, true);
            }
        });

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);
        font = ResourcesCompat.getFont(this, R.font.genshin);

        surfaceView = findViewById(R.id.gameSurfaceView);
        surfaceHolder = surfaceView.getHolder();

        surfaceView.post(this::resetGame);

        game = new Game(heroControllerAndroid);

        game.setOnPaint(() -> {
            if (surfaceHolder == null) {
                return;
            }
            // noinspection SynchronizeOnNonFinalField
            synchronized (surfaceHolder) {
                draw();
            }
        });
        game.setOnFinish(() -> {
            System.out.println("FINISH!!!");
            surfaceView.post(() -> {
                EditText editName = new EditText(this);
                EditText editMessage = new EditText(this);
                editName.setText(R.string.dialog_input_name_default);
                editMessage.setText(R.string.dialog_input_message_default);
                BasicCallback goHistory = () -> {
                    startActivity(new Intent(GameActivity.this, HistoryActivity.class));
                    finish();
                };
                if (RunningConfig.score > 0) {
                    new AlertDialog.Builder(GameActivity.this, R.style.AlertDialogCustom).setTitle(R.string.dialog_input_name_title)
                            .setView(editName)
                            .setCancelable(false)
                            .setNegativeButton(R.string.button_cancel, (d, w) -> goHistory.run())
                            .setPositiveButton(R.string.button_ok, (dialogName, witch) -> surfaceView.post(() -> {
                                String name = editName.getText().toString();
                                new AlertDialog.Builder(GameActivity.this, R.style.AlertDialogCustom).setTitle(R.string.dialog_input_message_title)
                                        .setView(editMessage)
                                        .setCancelable(false)
                                        .setNegativeButton(R.string.button_cancel, (d, w) -> goHistory.run())
                                        .setPositiveButton(R.string.button_ok, (dialogMessage, witch2) -> {
                                            String message = editMessage.getText().toString();
                                            HistoryImplAndroid.getInstance(GameActivity.this).addOne(
                                                    new HistoryObjectFactory(
                                                            name,
                                                            RunningConfig.score,
                                                            message,
                                                            RunningConfig.difficulty)
                                                            .create()
                                            );
                                            HistoryImplAndroid.getInstance(GameActivity.this).display();
                                            goHistory.run();
                                        }).show();
                            }))
                            .show();
                    System.out.println("build done");
                }
            });
        });
        surfaceView.setOnTouchListener((v, event) -> {
            heroControllerAndroid.onTouchEvent(event);
            v.performClick();
            return true;
        });
    }

    private void resetGame() {
        if (RunningConfig.windowWidth > RunningConfig.windowHeight) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        heroControllerAndroid.reset();

        int windowWidth = surfaceView.getMeasuredWidth();
        int windowHeight = surfaceView.getMeasuredHeight();

        if (RunningConfig.allowResize) {
            RunningConfig.windowWidth = windowWidth;
            RunningConfig.windowHeight = windowHeight;
            HeroAircraftFactory.getInstance().setPosition(RunningConfig.windowWidth / 2.0, RunningConfig.windowHeight - ImageManager.getInstance().HERO_IMAGE.getHeight());
            System.out.println("set window(" + RunningConfig.windowWidth + ", " + RunningConfig.windowHeight + "), place hero: " + HeroAircraftFactory.getInstance().getPosition());
            heroControllerAndroid.setScale(1.0);
        } else {
            float scaleWidth = windowWidth * 1.0f / RunningConfig.windowWidth;
            float scaleHeight = windowHeight * 1.0f / RunningConfig.windowHeight;
            canvasScale = Math.min(scaleWidth, scaleHeight);
            heroControllerAndroid.setScale(canvasScale);
        }

        game.resetStates();
    }

    @Override
    protected void onStart() {
        MyThreadFactory.getInstance().newThread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resetGame();
            game.action();
            HistoryImplAndroid.getInstance(this);
        }).start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        resetGame();
        super.onStop();
    }
}