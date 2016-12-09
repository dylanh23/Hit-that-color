package d.hitthatcolorproversion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GMSBaseGameActivity;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.util.color.Color;

public class MenuActivity extends GMSBaseGameActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static final int CAMERA_WIDTH = 480;
    protected static int CAMERA_HEIGHT = 800;
    Scene scene;
    int difficulty = 1;
    boolean wheelControl;


    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            CAMERA_HEIGHT = (int) (CAMERA_WIDTH / ((float) size.x / (float) size.y));
        } else {
            CAMERA_HEIGHT = (int) (CAMERA_WIDTH / ((float) display.getWidth() / (float) display.getHeight()));
        }
        //3:4, 2:3, 10:16, 3:5, and 9:16
        //0.75, 0.66, 0.625, 0.6, 0.5625
        CAMERA_HEIGHT = (int) (CAMERA_WIDTH / ((float) size.x / (float) size.y));
        Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        difficulty = prefs.getInt("difficulty", 1);
        wheelControl = prefs.getBoolean("wheelcontrol", false);
        if (checkPlayServices()) {
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
            GlobalSlate state = ((GlobalSlate) getApplicationContext());
            state.setGoogleApiClient(mGoogleApiClient);
        }
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), mCamera);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws
            Exception {
        loadGFX();
        /*scoreFnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(), "fnt/Linotte Bold.ttf", 120, true, new Color(0, 0, 37f / 255, 90f / 255).getABGRPackedInt());
        scoreFnt.load();
        topFnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(), "fnt/Linotte Bold.ttf", 22, true, Color.WHITE.getABGRPackedInt());
        topFnt.load();*/
        //TODO: only numbers
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    TextureRegion endless;
    TextureRegion count;
    TextureRegion settings;

    private void loadGFX() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        BitmapTextureAtlas b = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        int x = 0;
        endless = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "endless.png", x, 0); //346
        x += endless.getWidth() + 1;
        count = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "count.png", x, 0); //346
        x += count.getWidth() + 1;
        settings = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "settingsLogo.png", x, 0);
        x += settings.getWidth() + 1;
        b.load();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        scene = new Scene();
        scene.setBackground(new Background(new Color(67f / 255, 211f / 255, 153f / 255)));
        scene.setBackgroundEnabled(true);
        scene.registerUpdateHandler(new IUpdateHandler() {
            public void reset() {
            }

            public void onUpdate(float pSecondsElapsed) {
            }
        });
        pOnCreateSceneCallback.onCreateSceneFinished(scene);
    }

    @Override
    public void onPopulateScene(final Scene pScene, OnPopulateSceneCallback
            pOnPopulateSceneCallback) throws Exception {

        final Sprite endlessSprite = new Sprite((CAMERA_WIDTH - 356) / 2, (CAMERA_HEIGHT - 298 * 2) / 3 + 30, endless, this.getVertexBufferObjectManager()) {

            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        this.setColor(220f / 255, 220f / 255, 220f / 255);
                        break;
                    case TouchEvent.ACTION_CANCEL:
                        this.setColor(1f, 1f, 1f);
                        break;
                    case TouchEvent.ACTION_UP:
                        this.setColor(1f,1f,1f);
                        Intent i = new Intent(MenuActivity.this, EndlessActivity.class);
                        i.putExtra("endlessMode", true);
                        i.putExtra("difficulty", difficulty);
                        i.putExtra("reverseWheel", wheelControl);
                        startActivity(i);
                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        endlessSprite.setColor(1f, 1f, 1f);
        scene.attachChild(endlessSprite);
        scene.registerTouchArea(endlessSprite);
        Sprite countSprite = new Sprite((CAMERA_WIDTH - 356) / 2, 298 + ((CAMERA_HEIGHT - 298 * 2) / 3) * 2, count, this.getVertexBufferObjectManager()) {

            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        this.setColor(220f / 255, 220f / 255, 220f / 255);
                        break;
                    case TouchEvent.ACTION_CANCEL:
                        this.setColor(1f, 1f, 1f);
                        break;
                    case TouchEvent.ACTION_UP:
                        Intent i = new Intent(MenuActivity.this, CountActivity.class);
                        i.putExtra("endlessMode", false);
                        i.putExtra("difficulty", difficulty);
                        i.putExtra("reverseWheel", wheelControl);
                        startActivity(i);
                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        countSprite.setColor(1f, 1f, 1f);
        scene.attachChild(countSprite);
        scene.registerTouchArea(countSprite);
        Sprite settingsSprite = new Sprite(CAMERA_WIDTH - settings.getWidth() - 10, 10, settings, this.getVertexBufferObjectManager());
        scene.attachChild(settingsSprite);
        Rectangle r = new Rectangle(CAMERA_WIDTH - settings.getWidth() - 10 * 6, 0, settings.getWidth() + 60, settingsSprite.getHeight() + 60, this.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                         final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_UP:
                        View view = new View(getApplicationContext());
                        showPopup(view);
                }
                return super.
                        onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        scene.registerTouchArea(r);
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    PopupMenu popup;

    public void showPopup(final View v) {
        popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.getMenu().getItem(difficulty).setChecked(true);
        if (wheelControl) {
            popup.getMenu().getItem(3).setChecked(true);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                switch (item.getTitle().toString()) {
                    case "Easy":
                        difficulty = 0;
                        prefs.edit().putInt("difficulty", difficulty).apply();
                        break;
                    case "Medium":
                        difficulty = 1;
                        prefs.edit().putInt("difficulty", difficulty).apply();
                        break;
                    case "Hard":
                        difficulty = 2;
                        prefs.edit().putInt("difficulty", difficulty).apply();
                        break;
                    case "Reverse Wheel Controls":
                        wheelControl = !wheelControl;
                        prefs.edit().putBoolean("wheelcontrol", wheelControl).apply();
                        break;
                    case "Exit":
                        return false;
                }
                showPopup(v);
                return false;
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popup.show();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}