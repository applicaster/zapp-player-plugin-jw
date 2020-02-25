package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;

import com.applicaster.analytics.AnalyticsAgentUtil;
import com.applicaster.jwplayerplugin.cast.CastListenerOperator;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayersManager;
import com.applicaster.zapp_automation.AutomationManager;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.AdCompleteEvent;
import com.longtailvideo.jwplayer.events.AdPauseEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.ControlBarVisibilityEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.PauseEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.SeekEvent;
import com.longtailvideo.jwplayer.events.TimeEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

import java.util.HashMap;
import java.util.Map;

public class JWPlayerActivity
        extends    AppCompatActivity
        implements VideoPlayerEvents.OnFullscreenListener,
                   VideoPlayerEvents.OnTimeListener,
                   VideoPlayerEvents.OnSeekListener,
                   AdvertisingEvents.OnAdPlayListener,
                   AdvertisingEvents.OnAdPauseListener,
                   AdvertisingEvents.OnAdCompleteListener,
                   VideoPlayerEvents.OnPlayListener,
                   VideoPlayerEvents.OnPauseListener,
                   VideoPlayerEvents.OnControlBarVisibilityListener {

    private static final String PLAYABLE_KEY = "playable";
    private static final String PERCENTAGE_KEY = "percentage";
    private static final String ADVERTISEMENT_POSITION_KEY = "advertisement_position";

    //play services availability
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;
    protected JWPlayerContainer jwPlayerContainer;
    private double trackedPercentage;
    private Map<String, String> analyticsParams;

    private CastContext castContext;
    private MediaRouteButton mediaRouteButton;
    private CastListenerOperator castListenerOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayer);

        //play services availability check
        if (isGoogleApiAvailable(this)) {
            castContext = CastContext.getSharedInstance(this);
            mediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        jwPlayerContainer = findViewById(R.id.playerView);
        mPlayerView = jwPlayerContainer.getJWPlayerView();
        mPlayerView.addOnFullscreenListener(this);
        mPlayerView.addOnTimeListener(this);
        mPlayerView.addOnSeekListener(this);
        mPlayerView.addOnAdPlayListener(this);
        mPlayerView.addOnAdPauseListener(this);
        mPlayerView.addOnAdCompleteListener(this);
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnPauseListener(this);
        mPlayerView.addOnControlBarVisibilityListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Playable playable = (Playable) getIntent().getSerializableExtra(PLAYABLE_KEY);
        Map configuration =  null;
        if (PlayersManager.getCurrentPlayer() != null ){
            configuration =  PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
        }
        analyticsParams = new HashMap<>(playable.getAnalyticsParams());
        AnalyticsAgentUtil.logEvent("Play Video Item", analyticsParams);

        AutomationManager.getInstance().setAccessibilityIdentifier(mPlayerView, "jw_player_screen");

        // Load a media source
        mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
        mPlayerView.play();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume();

        //If the video was paused, resume & play.
        if (mPlayerView.getState().name().equals("PAUSED")) {
            mPlayerView.onResume();
            mPlayerView.play();
        }

        //cast
        castListenerOperator = new CastListenerOperator(mPlayerView);
        castContext.getSessionManager().addSessionManagerListener(castListenerOperator, CastSession.class);

    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        mPlayerView.pause();
        castContext.getSessionManager().removeSessionManagerListener(castListenerOperator, CastSession.class);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.player_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        return true;
    }


    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreenEvent true if the player is fullscreen
     */
    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        if (fullscreenEvent.getFullscreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        jwPlayerContainer.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String, String> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYABLE_KEY, playable);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    @Override
    public void onTime(TimeEvent timeEvent) {
        double position = timeEvent.getPosition();
        double duration = timeEvent.getDuration();
        double percent = (position / duration) * 100;

        if (percent >= 25f && percent < 26f && trackedPercentage < 25) {
            trackedPercentage = 25;
        } else if (percent >= 50f && percent < 51f && trackedPercentage < 50) {
            trackedPercentage = 50;
        } else if (percent >= 75f && percent < 76f && trackedPercentage < 75) {
            trackedPercentage = 75;
        } else if (percent >= 95f && percent < 96 && trackedPercentage < 95) {
            trackedPercentage = 95;
        } else return;

        Map<String, String> params = new HashMap<>(analyticsParams);
        params.put(PERCENTAGE_KEY, String.valueOf(trackedPercentage));
        AnalyticsAgentUtil.logEvent("Watch VOD Percentage", params);
    }

    @Override
    public void onSeek(SeekEvent seekEvent) {
        trackedPercentage = 0;
        if (castListenerOperator.getCastSession() != null) {
            MediaSeekOptions seekOptions = new MediaSeekOptions.Builder()
                    .setPosition((long) seekEvent.getPosition())
                    .build();
            castListenerOperator.getCastSession().getRemoteMediaClient().seek(seekOptions);
        }
    }

    @Override
    public void onControlBarVisibilityChanged(ControlBarVisibilityEvent controlBarVisibilityEvent) {
        if (castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            if (controlBarVisibilityEvent.isVisible()) {
                mediaRouteButton.setVisibility(View.VISIBLE);
            } else {
                mediaRouteButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAdPlay(AdPlayEvent adPlayEvent) {
        Map<String, String> params = new HashMap<>(analyticsParams);
        params.put(ADVERTISEMENT_POSITION_KEY, "Start");
        AnalyticsAgentUtil.logEvent("Watch Video Advertisement", params);
    }

    @Override
    public void onAdPause(AdPauseEvent adPauseEvent) {
        Map<String, String> params = new HashMap<>(analyticsParams);
        params.put(ADVERTISEMENT_POSITION_KEY, "Pause");
        AnalyticsAgentUtil.logEvent("Watch Video Advertisement", params);
    }

    @Override
    public void onAdComplete(AdCompleteEvent adCompleteEvent) {
        Map<String, String> params = new HashMap<>(analyticsParams);
        params.put(ADVERTISEMENT_POSITION_KEY, "End");
        AnalyticsAgentUtil.logEvent("Watch Video Advertisement", params);
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        AnalyticsAgentUtil.logEvent("Start Video", analyticsParams);
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        AnalyticsAgentUtil.logEvent("Pause Video", analyticsParams);
    }

    private boolean doesPackageExist(String targetPackage) {
        try {
            getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    // Without the Google API's Chromecast won't work
    private boolean isGoogleApiAvailable(Context context) {
        boolean isOldPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD);
        boolean isNewPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW);

        boolean isPlaystoreInstalled = isNewPlayStoreInstalled || isOldPlayStoreInstalled;

        boolean isGoogleApiAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        return isPlaystoreInstalled && isGoogleApiAvailable;
    }
}