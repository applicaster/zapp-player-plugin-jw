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

import com.applicaster.analytics.AnalyticsAgentUtil;
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
import com.longtailvideo.jwplayer.events.AdPauseEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.ControlBarVisibilityEvent;
import com.longtailvideo.jwplayer.events.ErrorEvent;
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
        extends AppCompatActivity
        implements VideoPlayerEvents.OnFullscreenListener,
        VideoPlayerEvents.OnTimeListener,
        VideoPlayerEvents.OnSeekListener,
        AdvertisingEvents.OnAdPlayListener,
        AdvertisingEvents.OnAdPauseListener,
        AdvertisingEvents.OnAdCompleteListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnPauseListener,
        VideoPlayerEvents.OnControlBarVisibilityListener,
        VideoPlayerEvents.OnErrorListener,
        NetworkUtil.ConnectionAvailabilityCallback {

    private static final String PLAYABLE_KEY = "playable";
    private static final String PERCENTAGE_KEY = "percentage";
    private static final String ADVERTISEMENT_POSITION_KEY = "advertisement_position";
    private static String ENABLE_CHROMECAST_KEY = "enable_chromecast";

    /**
     * Reference to the {@link JWPlayerView}
     */
    private JWPlayerView mPlayerView;
    protected JWPlayerContainer jwPlayerContainer;
    private double trackedPercentage;
    private Map<String, String> analyticsParams;
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
        mPlayerView.addOnTimeListener(this);
        mPlayerView.addOnSeekListener(this);
        mPlayerView.addOnAdPlayListener(this);
        mPlayerView.addOnAdPauseListener(this);
        mPlayerView.addOnAdCompleteListener(this);
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnPauseListener(this);
        mPlayerView.addOnControlBarVisibilityListener(this);
        mPlayerView.addOnErrorListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playable = (Playable) getIntent().getSerializableExtra(PLAYABLE_KEY);
        boolean enableChromecast = getIntent().getBooleanExtra(ENABLE_CHROMECAST_KEY, false);

        //Initialize cast provider
        if (enableChromecast) {
            castProvider = new CastProvider(this, jwPlayerContainer);
            castProvider.init(playable, true);
        }


        Map configuration = null;
        if (PlayersManager.getCurrentPlayer() != null) {
            configuration = PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
        }
        analyticsParams = new HashMap<>(playable.getAnalyticsParams());
        AnalyticsAgentUtil.logEvent("Play Video Item", analyticsParams);

        AutomationManager.getInstance().setAccessibilityIdentifier(mPlayerView, "jw_player_screen");

        // Load a media source
        mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
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

        // When going to Fullscreen we want to set fitsSystemWindows="false"
        jwPlayerContainer.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String, String> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYABLE_KEY, playable);
        bundle.putBoolean(ENABLE_CHROMECAST_KEY, parseBoolean(params.get("Chromecast")));
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    private static boolean parseBoolean(String s) {
        if (s != null) {
            return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1");
        }
        return false;
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
        adState = AdState.IDLE;
        if (castProvider != null
                && castProvider.getCastContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            castProvider.getMediaRouteButton().setVisibility(View.GONE);
        }
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