package d.hitthatcolorproversion;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.Display;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import org.andengine.AndEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.IGameInterface;
import org.andengine.ui.activity.LayoutGameActivity;
import org.andengine.util.color.Color;

import java.util.Random;

public class EndlessActivity extends LayoutGameActivity {
    protected static final int CAMERA_WIDTH = 480;
    protected static int CAMERA_HEIGHT = 800;
    protected static float SPEED = 2f;
    protected static float SPEEDDISTANCE = 45f / 4;
    Font scoreFnt;
    Font topFnt;
    Scene scene;
    Random randomGenerater = new Random();
    int score = 0;
    int bestScore = 0;
    boolean died;
    int difficulty;
    boolean reverseWheel;
    String difficultySave;
    String leaderboardID;
    InterstitialAd mInterstitialAd;
    AdRequest adRequest;
    GoogleApiClient mGoogleApiClient;

    private void requestNewInterstitial() {
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        //3:4, 2:3, 10:16, 3:5, and 9:16
        //0.75, 0.66, 0.625, 0.6, 0.5625
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            CAMERA_HEIGHT = (int) (CAMERA_WIDTH / ((float) size.x / (float) size.y));
        } else {
            CAMERA_HEIGHT = (int) (CAMERA_WIDTH / ((float) display.getWidth() / (float) display.getHeight()));
        }
        difficulty = getIntent().getIntExtra("difficulty", 1);
        Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        //prefs.edit().clear().apply();
        switch (difficulty) {
            case 0:
                SPEED = 1.8f;
                difficultySave = "EasyScore";
                leaderboardID = getResources().getString(R.string.leaderboard_easy__endless_mode);
                break;
            case 1:
                SPEED = 2.8f;
                difficultySave = "MediumScore";
                leaderboardID = getResources().getString(R.string.leaderboard_medium__endless_mode);
                break;
            case 2:
                SPEED = 4f;
                difficultySave = "HardScore";
                leaderboardID = getResources().getString(R.string.leaderboard_hard__endless_mode);
                break;
        }
        bestScore = prefs.getInt(difficultySave, 0);
        adRequest = new AdRequest.Builder().addTestDevice("DDB504E461FF179D726AD9B5F625CBE1").build();
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6184270616715379/2643233040");
        requestNewInterstitial();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        reverseWheel = getIntent().getBooleanExtra("reverseWheel", false);
        GlobalSlate state = ((GlobalSlate) getApplicationContext());
        mGoogleApiClient = state.getGoogleApiClient();
        if (checkPlayServices()) {
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                    leaderboardID,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                    new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                        @Override
                        public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                            LeaderboardScore c = arg0.getScore();
                            if (c != null) {
                                if (bestScore > c.getRawScore()) {
                                    Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardID, bestScore);
                                }
                            }
                        }
                    });
        }

        return new

                EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), mCamera

        );
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws
            Exception {
        loadGFX();
        scoreFnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(), "fnt/Linotte Bold.ttf", 120, true, new Color(0, 0, 37f / 255, 40f / 255).getABGRPackedInt());
        scoreFnt.load();
        topFnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(), "fnt/Linotte Bold.ttf", 44, true, Color.WHITE.getABGRPackedInt());
        topFnt.load();
        //highscrFnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(), "fnt/Linotte Bold.ttf", 44, true, Color.WHITE.getABGRPackedInt());
        //highscrFnt.load();
        //TODO: only numbers
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    TextureRegion wheel;
    TextureRegion dot;
    TextureRegion arrow;
    TextureRegion best;
    TextureRegion instruct;
    TextureRegion difficultyIndicator;
    TextureRegion leaderboard;

    private void loadGFX() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        BitmapTextureAtlas b = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        int x = 0;
        wheel = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "wheel.png", x, 0); //346
        x += wheel.getWidth() + 1;
        dot = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "dotblank.png", x, 0); //47
        x += dot.getWidth() + 1;
        arrow = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "arrow.png", x, 0); //23
        x += arrow.getWidth() + 1;
        best = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "best.png", x, 0);
        x += best.getWidth() + 1;
        if (reverseWheel) {
            instruct = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "instruct.png", x, 0);
        } else {
            instruct = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "reverseinstruct.png", x, 0);
        }
        x += instruct.getWidth() + 1;
        x = 0;
        switch (difficulty) {
            case 0:
                difficultyIndicator = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "easy.png", x, 347);
                break;
            case 1:
                difficultyIndicator = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "medium.png", x, 347);
                break;
            case 2:
                difficultyIndicator = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "hard.png", x, 347);
                break;
        }
        x += difficultyIndicator.getWidth() + 1;
        leaderboard = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "leaderboard.png", x, 347);
        x += leaderboard.getWidth() + 1;
        b.load();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        scene = new Scene();
        scene.setBackground(new Background(new Color(74f / 255, 182f / 255, 230f / 255)));
        scene.setBackgroundEnabled(true);
        scene.registerUpdateHandler(new IUpdateHandler() {
            public void reset() {
            }

            public void onUpdate(float pSecondsElapsed) {
                if (gameOn && !died) {
                    update();
                }
            }
        });
        pOnCreateSceneCallback.onCreateSceneFinished(scene);
    }

    boolean gameOn;

    /*public void updateTimer() {
        mEngine.registerUpdateHandler(new TimerHandler(0.01f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                update();
                if (gameOn) {
                    pTimerHandler.reset();
                }
            }
        }
        ));
    }*/

    public void update() {
        switch (direction) {
            case 0:
                dotSprite.setY(dotSprite.getY() - SPEED);
                arrowSprite.setY(arrowSprite.getY() - SPEED);
                //dotSprite.setY(dotSprite.getY() + arrowSprite.getHeight() / 2 + dotSprite.getHeight() /2);
                if (arrowDirection != -1) {
                    if (dotSprite.getY() <= 176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2) {
                        dotSprite.setY(176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2);
                        direction = arrowDirection;
                        arrowDirection = -1;
                        arrowSprite.setVisible(false);
                    }
                } else if (dotSprite.getY() <= 176f + 18f) {
                    d();
                }
                break;
            case 1:
                dotSprite.setX(dotSprite.getX() + SPEED);
                arrowSprite.setX(arrowSprite.getX() + SPEED);
                //dotSprite.setX(arrowSprite.getX() + arrowSprite.getWidth() / 2 + dotSprite.getWidth() / 2);
                if (arrowDirection != -1) {
                    if (dotSprite.getX() >= 67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2) {
                        dotSprite.setX(67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2);
                        direction = arrowDirection;
                        arrowDirection = -1;
                        arrowSprite.setVisible(false);
                    }
                } else if (dotSprite.getX() + dotSprite.getWidth() >= 67f + 328f) {
                    d();
                }
                break;
            case 2:
                dotSprite.setY(dotSprite.getY() + SPEED);
                arrowSprite.setY(arrowSprite.getY() + SPEED);
                //dotSprite.setY(arrowSprite.getY() + arrowSprite.getHeight() / 2 + dotSprite.getHeight() /2);
                if (arrowDirection != -1) {
                    if (dotSprite.getY() >= 176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2) {
                        dotSprite.setY(176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2);
                        direction = arrowDirection;
                        arrowDirection = -1;
                        arrowSprite.setVisible(false);
                    }
                } else if (dotSprite.getY() + dotSprite.getHeight() >= 176f + 328f) {
                    d();
                }
                break;
            case 3:
                dotSprite.setX(dotSprite.getX() - SPEED);
                arrowSprite.setX(arrowSprite.getX() - SPEED);
                //dotSprite.setX(arrowSprite.getX() + arrowSprite.getWidth() / 2 + dotSprite.getWidth() /2);
                if (arrowDirection != -1) {
                    if (dotSprite.getX() <= 67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2) {
                        dotSprite.setX(67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2);
                        direction = arrowDirection;
                        arrowDirection = -1;
                        arrowSprite.setVisible(false);
                    }
                } else if (dotSprite.getX() <= 67f + 18f) {
                    d();
                }
        }
    }

    public void d() {
        if (rotation >= 360) rotation -= 360;
        else if (rotation < 0) rotation = 360 + rotation;
        int x = Math.round(rotation / 45);
        if (x == 8) {
            x = 0;
        }
        Color testColor;
        if (direction == 1) {
            for (int i = 0; i <= 1; i++) {
                x -= 1;
                if (x == -1) {
                    x = 7;
                }
            }
        } else if (direction == 2) {
            for (int i = 0; i <= 3; i++) {
                x += 1;
                if (x == 8) {
                    x = 0;
                }
            }
        } else if (direction == 3) {
            for (int i = 0; i <= 1; i++) {
                x += 1;
                if (x == 8) {
                    x = 0;
                }
            }
        }
        testColor = colors[x];

        if (testColor == currentColor) {
            //TODO: crush/expand animation
            if (direction < 2)
                direction += 2;
            else
                direction -= 2;
            randomArrow();
            score += 1;
            scoreText.setText(String.valueOf(score));
            scoreText.setPosition(67f + wheelSprite.getWidth() / 2 - scoreText.getWidth() / 2 - 6, 176f + wheelSprite.getHeight() / 2 - scoreText.getHeight() / 2 + 10);
            //scene.sortChildren();
        } else {
            final int mSpeed = 4;
            mEngine.registerUpdateHandler(new TimerHandler(0.01f, new ITimerCallback() {
                @Override
                public void onTimePassed(TimerHandler pTimerHandler) {
                    if (direction == 0 || direction == 2) {
                        if (direction == 2) {
                            dotSprite.setY(dotSprite.getY() + mSpeed);
                        }
                        dotSprite.setHeight(dotSprite.getHeight() - mSpeed);
                        if (dotSprite.getHeight() > 15) {
                            pTimerHandler.reset();
                        }
                    } else {
                        if (direction == 1) {
                            dotSprite.setX(dotSprite.getX() + mSpeed);
                        }
                        dotSprite.setWidth(dotSprite.getWidth() - mSpeed);
                        if (dotSprite.getWidth() > 15) {
                            pTimerHandler.reset();
                        }
                    }
                }
            }
            ));
            died = true;
            gameOn = false;
            scene.setBackground(new Background(new Color(new Color(230f / 255, 76f / 255, 61f / 255))));
            mEngine.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
                @Override
                public void onTimePassed(TimerHandler pTimerHandler) {
                    timeup = true;
                }
            }
            ));
            //SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            //prefs.edit().putInt("Tries", prefs.getInt("Tries", 0) + 1).apply();
            //if (prefs.getInt("Tries", 0) > 2) {
            //prefs.edit().putInt("Tries", 0).apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialAd.isLoaded()) {
                        //mInterstitialAd.show();
                    }
                }
            });
            //}
        }
    }

    boolean timeup;

    private void gameOver() {
        died = false;
        rotation = Math.round(rotation / 45) * 45;
        wheelSprite.setRotation(rotation);
        scene.setBackground(new Background(new Color(74f / 255, 182f / 255, 230f / 255)));
        if (score > bestScore) {
            if (checkPlayServices())
                Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardID, score);
            highScoreAnimation();
            bestScore = score;
            bestText.setText(String.valueOf(bestScore));
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            prefs.edit().putInt(difficultySave, bestScore).apply();
        }
        score = 0;
        scoreText.setText(String.valueOf(score));
        scoreText.setPosition(67f + wheelSprite.getWidth() / 2 - scoreText.getWidth() / 2 - 6, 176f + wheelSprite.getHeight() / 2 - scoreText.getHeight() / 2 + 10);
        //scene.sortChildren();
        startingColor();
        direction = 2;
        dotSprite.setHeight(46);
        dotSprite.setWidth(47);
        dotSprite.setPosition(67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2, 176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2);
        timeup = false;
    }

    private void highScoreAnimation() {
        /*highscoreSprite.setVisible(true);
        mEngine.registerUpdateHandler(new TimerHandler(0.01f, new ITimerCallback() {
            float c = highscoreSprite.getScaleX();

            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                c += 0.03;
                highscoreSprite.setScale(c);
                //highscrreSprite.setAlpha(highscoreSprite.getAlpha() - 0.3f);
                //highscoreSprite.setAlpha(0.5f);
                if (c > 1.3) {
                    highscoreSprite.setScale(0.1f);
                    //highscoreSprite.setSize(highscore.getWidth(), highscore.getHeight());
                    highscoreSprite.setVisible(false);
                    //highscoreSprite.setAlpha(1);
                    //highscoreSprite.setPosition(CAMERA_WIDTH - highscore.getWidth() / 2, 176 + wheel.getHeight() / 2 - highscore.getHeight() / 2);
                } else {
                    pTimerHandler.reset();
                }
            }
        }));*/
        highscrText.setVisible(true);
        mEngine.registerUpdateHandler(new TimerHandler(0.01f, new ITimerCallback() {
            float c = highscrText.getScaleX();

            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                c += 0.03;
                highscrText.setScale(c);
                highscrText.setAlpha(highscrText.getAlpha() - 0.01f);
                //highscoreSprite.setAlpha(highscoreSprite.getAlpha() - 0.3f);
                //highscoreSprite.setAlpha(0.5f);
                if (c > 2) {
                    highscrText.setScale(0.5f);
                    //highscoreSprite.setSize(highscore.getWidth(), highscore.getHeight());
                    highscrText.setVisible(false);
                    highscrText.setAlpha(1);
                    //highscoreSprite.setPosition(CAMERA_WIDTH - highscore.getWidth() / 2, 176 + wheel.getHeight() / 2 - highscore.getHeight() / 2);
                } else {
                    pTimerHandler.reset();
                }
            }
        }));
    }

    private boolean checkPlayServices() {
        return mGoogleApiClient.isConnected();
    }

    Sprite wheelSprite;
    Sprite dotSprite;
    Sprite arrowSprite;
    Sprite bestSprite;
    Sprite instructSprite;
    Sprite leaderboardSprite;
    Sprite difficultySprite;
    Text scoreText;
    Text bestText;
    Text highscrText;
    Color[] colors = new Color[]{
            new Color(0, 252f / 255, 255f / 255), //aqua
            new Color(0f, 102f / 255, 255f / 255), //blue
            new Color(68f / 255, 0f, 183f / 255), //purple
            new Color(255f / 255, 0f, 234f / 255), //violet
            new Color(234f / 255, 0f, 0f), //red
            new Color(255f / 255, 168f / 255, 0f), //orange
            new Color(255f / 255, 240f / 255, 0f), //yellow
            new Color(138f / 255, 234f / 255, 0f), //green
    };
    //will never be aqua to begin
    Color currentColor = colors[0];
    int direction = 2;

    @Override
    public void onPopulateScene(final Scene pScene, IGameInterface.OnPopulateSceneCallback
            pOnPopulateSceneCallback) throws Exception {
        wheelSprite = new Sprite(67f, 176f, wheel, this.getVertexBufferObjectManager());
        wheelSprite.setZIndex(1);
        scene.attachChild(wheelSprite);
        difficultySprite = new Sprite(CAMERA_WIDTH - difficultyIndicator.getWidth() - 27, 43, difficultyIndicator, this.getVertexBufferObjectManager());
        scene.attachChild(difficultySprite);
        dotSprite = new Sprite(67f + wheelSprite.getWidth() / 2 - dot.getWidth() / 2, 176f + wheelSprite.getHeight() / 2 - dot.getHeight() / 2, dot, this.getVertexBufferObjectManager());
        startingColor();
        dotSprite.setZIndex(2);
        scene.attachChild(dotSprite);
        arrowSprite = new Sprite(dotSprite.getX() + dotSprite.getWidth() / 2 - arrow.getWidth() / 2, dotSprite.getY() + dotSprite.getHeight() / 2 - arrow.getHeight() / 2, arrow, this.getVertexBufferObjectManager());
        arrowSprite.setVisible(false);
        arrowSprite.setZIndex(3);
        scene.attachChild(arrowSprite);
        bestSprite = new Sprite(27, 43, best, this.getVertexBufferObjectManager());
        scene.attachChild(bestSprite);
        instructSprite = new Sprite((CAMERA_WIDTH - instruct.getWidth()) / 2, wheelSprite.getY() + wheelSprite.getHeight() + 20, instruct, this.getVertexBufferObjectManager());
        scene.attachChild(instructSprite);
        leaderboardSprite = new Sprite(bestSprite.getX() + bestSprite.getWidth() - 47, bestSprite.getY() + 23, leaderboard, this.getVertexBufferObjectManager());
        scene.attachChild(leaderboardSprite);
        /*highscoreSprite = new Sprite((CAMERA_WIDTH - highscore.getWidth()) / 2, 176 + wheel.getHeight() / 2 - highscore.getHeight() / 2, highscore, this.getVertexBufferObjectManager());
        highscoreSprite.setScale(0.1f);
        highscoreSprite.setVisible(false);
        highscoreSprite.setZIndex(4);
        scene.attachChild(highscoreSprite);*/
        bestText = new Text(bestSprite.getX() + bestSprite.getWidth() + 20, bestSprite.getY(), topFnt, "Score: ", this.getVertexBufferObjectManager());
        //SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        //bestText.setText(String.valueOf(prefs.getInt("Score", 0)));
        bestText.setText(String.valueOf(bestScore));
        scene.attachChild(bestText);
        highscrText = new Text(0, 0, topFnt, "New Highscore!", this.getVertexBufferObjectManager());
        highscrText.setText("New Highscore!");
        highscrText.setPosition((CAMERA_WIDTH - highscrText.getWidthScaled()) / 2, 176 + (wheel.getHeight() / 2) / 2 - highscrText.getHeightScaled() / 2);
        highscrText.setScale(0.5f);
        //highscrText.setScaleCenter(highscrText.getWidth() / 2, highscrText.getHeight() / 2);
        highscrText.setZIndex(4);
        highscrText.setVisible(false);
        scene.attachChild(highscrText);
        scoreText = new Text(67f + wheelSprite.getWidth() / 2, 176f + wheelSprite.getHeight() / 2, scoreFnt, "Score: ", this.getVertexBufferObjectManager());
        scoreText.setText(String.valueOf(score));
        scoreText.setPosition(67f + wheelSprite.getWidth() / 2 - scoreText.getWidthScaled() / 2 - 6, 176f + wheelSprite.getHeight() / 2 - scoreText.getHeightScaled() / 2 + 10);
        scoreText.setZIndex(0);
        scene.attachChild(scoreText);
        scene.sortChildren();
        createTouchArea();
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    float rotation;

    private void createTouchArea() {
        Rectangle r = new Rectangle(0, 155f, CAMERA_WIDTH / 2, CAMERA_HEIGHT, this.getVertexBufferObjectManager()) {

            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        if (instructSprite.isVisible()) {
                            instructSprite.setVisible(false);
                            leaderboardSprite.setVisible(false);
                        }
                        if (!died) {
                            wheelAnimation(reverseWheel);
                            gameOn = true;
                        } else if (timeup) {
                            gameOver();
                        }
                        //updateTimer();
                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        r.setColor(0, 0, 0, 0);
        scene.attachChild(r);
        scene.registerTouchArea(r);
        Rectangle r2 = new Rectangle(CAMERA_WIDTH / 2, 155f, CAMERA_WIDTH / 2, CAMERA_HEIGHT, this.getVertexBufferObjectManager()) {

            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        if (instructSprite.isVisible()) {
                            instructSprite.setVisible(false);
                            leaderboardSprite.setVisible(false);
                        }
                        if (!died) {
                            wheelAnimation(!reverseWheel);
                            gameOn = true;
                        } else if (timeup) {
                            gameOver();
                        }
                        //updateTimer();
                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        r2.setColor(0, 0, 0, 0);
        scene.attachChild(r2);
        scene.registerTouchArea(r2);
        Rectangle r3 = new Rectangle(CAMERA_WIDTH - difficultyIndicator.getWidth() - 27 * 2, 43 - 27, difficultyIndicator.getWidth() + 27 * 2, difficultyIndicator.getHeight() + 27 * 2, this.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_UP:
                        if (checkPlayServices()) {
                            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderboardID), 100);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EndlessActivity.this, "Google Play Games not connected!", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }

                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        r3.setColor(0, 0, 0, 0);
        scene.attachChild(r3);
        scene.registerTouchArea(r3);
    }

    private void wheelAnimation(final boolean left) {
        mEngine.registerUpdateHandler(new TimerHandler(0.001f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                if (!died) {
                    if (left) {
                        rotation -= SPEEDDISTANCE;
                    } else {
                        rotation += SPEEDDISTANCE;
                    }
                    wheelSprite.setRotation(rotation);
                    if (rotation % 45 != 0) {
                        pTimerHandler.reset();
                    } else if (rotation == 360) {
                        rotation = 0;
                    } else if (rotation == -45) {
                        rotation = 315;
                    }
                }
            }
        }
        ));
    }

    private void randomColor() {
        Color c = colors[randomGenerater.nextInt(8)];
        if (arrowDirection == -1) {
            while (c == currentColor) {
                c = colors[randomGenerater.nextInt(8)];
            }
        } else {
            int d;
            if (arrowDirection < 2)
                d = arrowDirection + 2;
            else
                d = arrowDirection - 2;
            int x = Math.round(rotation / 45);
            if (x == 8) {
                x = 0;
            }
            Color testColor;
            if (d == 1) {
                for (int i = 0; i <= 1; i++) {
                    x -= 1;
                    if (x == -1) {
                        x = 7;
                    }
                }
            } else if (d == 2) {
                for (int i = 0; i <= 3; i++) {
                    x += 1;
                    if (x == 8) {
                        x = 0;
                    }
                }
            } else if (d == 3) {
                for (int i = 0; i <= 1; i++) {
                    x += 1;
                    if (x == 8) {
                        x = 0;
                    }
                }
            }
            while (c == currentColor || c == colors[x]) {
                c = colors[randomGenerater.nextInt(8)];
            }
        }
        currentColor = c;
        dotSprite.setColor(c);
        //TODO: transition color animation
    }

    private void startingColor() {
        int p;
        if (randomGenerater.nextInt(101) <= 50) p = 4;
        else p = 2;
        int x = (int) (rotation / 45);
        if (x == 8) {
            x = 0;
        }
        for (int i = 0; i <= p; i++) {
            x += 1;
            if (x == 8) {
                x = 0;
            }
        }
        currentColor = colors[x];
        dotSprite.setColor(colors[x]);
    }

    int arrowDirection = -1;

    private void randomArrow() {
        if (randomGenerater.nextInt(101) <= 50) {
            if (direction == 0 || direction == 2) {
                if (randomGenerater.nextInt(101) < 50) {
                    arrowDirection = 1;
                } else {
                    arrowDirection = 3;
                }
            } else {
                if (randomGenerater.nextInt(101) < 50) {
                    arrowDirection = 0;
                } else {
                    arrowDirection = 2;
                }
            }
            arrowSprite.setRotation(90 * arrowDirection);
            arrowSprite.setPosition(dotSprite.getX() + dotSprite.getWidth() / 2 - arrow.getWidth() / 2, dotSprite.getY() + dotSprite.getHeight() / 2 - arrow.getHeight() / 2);
            //scene.sortChildren();
            arrowSprite.setVisible(true);
        }
        randomColor();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.SurfaceViewId;
    }

    AdView adView;

    @Override
    protected void onSetContentView() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        final RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);

        if (!AndEngine.isDeviceSupported()) {
            //this device is not supported, create a toast to tell the user
            //then kill the activity
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3500);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    } catch (InterruptedException e) {
                    }
                }
            };
            this.toastOnUIThread("This device does not support AndEngine GLES2, so this game will not work. Sorry.");
            finish();
            thread.start();

            this.setContentView(relativeLayout, relativeLayoutParams);
        } else {
            this.mRenderSurfaceView = new RenderSurfaceView(this);
            mRenderSurfaceView.setRenderer(mEngine, this);

            relativeLayout.addView(mRenderSurfaceView, EndlessActivity.createSurfaceViewLayoutParams());

            try {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setAdUnitId("ca-app-pub-4130770981589196/8076623864");
                adView.setTag("adView");
                adView.refreshDrawableState();
                adView.setVisibility(AdView.GONE);

                // Initiate a generic request to load it with an ad
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adView.loadAd(adRequest);
                    }
                });

                adView.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        adView.setVisibility(AdView.VISIBLE);
                    }
                });

                RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                //the next line is the key to putting it on the bottom
                adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                relativeLayout.addView(adView, adViewParams);
            } catch (Exception e) {
                //ads aren't working. oh well
            }
            this.setContentView(relativeLayout, relativeLayoutParams);
        }
    }
}