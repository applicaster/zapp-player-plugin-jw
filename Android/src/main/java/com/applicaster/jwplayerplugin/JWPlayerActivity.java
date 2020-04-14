package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.events.AdvertisingEventsAnalytics;
import com.applicaster.jwplayerplugin.analytics.events.PlayerEventsAnalytics;
import com.applicaster.jwplayerplugin.cast.CastProvider;
import com.applicaster.jwplayerplugin.networkutil.NetworkChangeReceiver;
import com.applicaster.jwplayerplugin.networkutil.NetworkUtil;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayersManager;
import com.applicaster.zapp_automation.AutomationManager;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastState;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.AdCompleteEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.ControlBarVisibilityEvent;
import com.longtailvideo.jwplayer.events.ErrorEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.SeekEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

import java.util.Map;

public class JWPlayerActivity
        extends AppCompatActivity
        implements VideoPlayerEvents.OnFullscreenListener,
        VideoPlayerEvents.OnControlBarVisibilityListener,
        VideoPlayerEvents.OnSeekListener,
        AdvertisingEvents.OnAdCompleteListener,
        AdvertisingEvents.OnAdPlayListener,
        VideoPlayerEvents.OnErrorListener,
        NetworkUtil.ConnectionAvailabilityCallback {

    private static final String PLAYABLE_KEY = "playable";

    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;
    protected JWPlayerContainer jwPlayerContainer;
    private PlayerEventsAnalytics playerEventsAnalytics;
    private AdvertisingEventsAnalytics advertisingEventsAnalytics;
    private Playable playable;

    private CastProvider castProvider;
    private NetworkChangeReceiver networkChangeReceiver;

    private double playerPosition;

    private AdState adState = AdState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayer);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        jwPlayerContainer = findViewById(R.id.playerView);
        mPlayerView = jwPlayerContainer.getJWPlayerView();
        mPlayerView.addOnFullscreenListener(this);
        mPlayerView.addOnSeekListener(this);
        mPlayerView.addOnAdPlayListener(this);
        mPlayerView.addOnAdCompleteListener(this);
        mPlayerView.addOnControlBarVisibilityListener(this);
        mPlayerView.addOnErrorListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playable = (Playable) getIntent().getSerializableExtra(PLAYABLE_KEY);
        boolean enableChromecast = getIntent().getBooleanExtra(JWPlayerAdapter.CAST_ENABLED_KEY, false);
        String receiverAppId = getIntent().getStringExtra(JWPlayerAdapter.CAST_RECEIVER_APP_ID);

        //analytics data and events
        AnalyticsData analyticsData = new AnalyticsData(playable, mPlayerView);
        playerEventsAnalytics = new PlayerEventsAnalytics(analyticsData, playable, mPlayerView);
        advertisingEventsAnalytics = new AdvertisingEventsAnalytics(analyticsData, playable, mPlayerView);

        //Initialize cast provider
        if (enableChromecast) {
            castProvider = new CastProvider(this, jwPlayerContainer);
            castProvider.init(playable,
                    analyticsData,
                    playerEventsAnalytics.getScreenAnalyticsState(),
                    receiverAppId);
        }


        Map configuration = null;
        if (PlayersManager.getCurrentPlayer() != null) {
            configuration = PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
        }

        AutomationManager.getInstance().setAccessibilityIdentifier(mPlayerView, "jw_player_screen");

        // Load a media source
        mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
        analyticsData.setItemDuration(mPlayerView);
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
        setConnectionAvailabilityListener();

        //If the video was paused, resume & play.
        if (mPlayerView.getState().name().equals("PAUSED")) {
            mPlayerView.onResume();
            mPlayerView.play();
        }
        if (castProvider != null) castProvider.addSessionManagerListener();
    }

    @Override
    protected void onPause() {
        mPlayerView.onPause();
        // Let JW Player know that the app is going to the background
        if (castProvider != null && castProvider.getCastContext().getCastState() != CastState.CONNECTED) {
            mPlayerView.pause();
        }
        if (castProvider != null) castProvider.removeSessionManagerListener();
        unregisterConnectionReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (castProvider != null) castProvider.release();
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
        castProvider.setScreenAnalyticsState(playerEventsAnalytics.getScreenAnalyticsState());

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        jwPlayerContainer.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String, String> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYABLE_KEY, playable);
        bundle.putBoolean(JWPlayerAdapter.CAST_ENABLED_KEY, JWPlayerUtil.parseBoolean(params.get(JWPlayerAdapter.CAST_ENABLED_KEY)));
        bundle.putString(JWPlayerAdapter.CAST_RECEIVER_APP_ID, params.get(JWPlayerAdapter.CAST_RECEIVER_APP_ID));
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    @Override
    public void onSeek(SeekEvent seekEvent) {
        if (castProvider != null && castProvider.getCastListenerOperator().getCastSession() != null) {
            MediaSeekOptions seekOptions = new MediaSeekOptions.Builder()
                    .setPosition((long) seekEvent.getPosition())
                    .build();
            castProvider.getCastListenerOperator().getCastSession().getRemoteMediaClient().seek(seekOptions);
        }
    }

    @Override
    public void onControlBarVisibilityChanged(ControlBarVisibilityEvent controlBarVisibilityEvent) {
        if (castProvider != null
                && castProvider.getCastContext().getCastState() != CastState.NO_DEVICES_AVAILABLE
                && adState == AdState.IDLE) {
            if (controlBarVisibilityEvent.isVisible()) {
                castProvider.getMediaRouteButton().setVisibility(View.VISIBLE);
            } else {
                castProvider.getMediaRouteButton().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (castProvider != null) {
            return castProvider.getCastContext()
                    .onDispatchVolumeKeyEventBeforeJellyBean(event)
                    || super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onError(ErrorEvent errorEvent) {
        if (errorEvent.getException() != null) {
            playerPosition = mPlayerView.getPosition();
        }
    }

    private enum AdState {
        PLAYING,
        IDLE
    }

    @Override
    public void onAdPlay(AdPlayEvent adPlayEvent) {
        adState = AdState.PLAYING;
        if (castProvider != null
                && castProvider.getCastContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            castProvider.getMediaRouteButton().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAdComplete(AdCompleteEvent adCompleteEvent) {
        adState = AdState.IDLE;
        if (castProvider != null
                && castProvider.getCastContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            castProvider.getMediaRouteButton().setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        playerEventsAnalytics.backPressed();
        advertisingEventsAnalytics.backPressed();
    }

    @Override
    public void onNetworkAvailable() {
        if (mPlayerView.getState() == PlayerState.IDLE) {
            Map configuration = null;
            if (PlayersManager.getCurrentPlayer() != null) {
                configuration = PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
            }
            mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
            mPlayerView.seek(playerPosition);
            mPlayerView.play();
        }
    }

    @Override
    public void onNetworkLost() {
        Log.e(this.getClass().getSimpleName(), "Network connection was lost!");
    }

    private void setConnectionAvailabilityListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkUtil.checkConnectionAvailability(this, this);
        } else if (networkChangeReceiver == null) {
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            networkChangeReceiver = new NetworkChangeReceiver(this);
            this.registerReceiver(networkChangeReceiver, intentFilter);
        }
    }

    private void unregisterConnectionReceiver() {
        if (networkChangeReceiver != null) {
            this.unregisterReceiver(networkChangeReceiver);
            networkChangeReceiver = null;
        }
    }
}