package com.applicaster.jwplayerplugin.player;

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

import com.applicaster.jwplayerplugin.JWPlayerAdapter;
import com.applicaster.jwplayerplugin.R;
import com.applicaster.jwplayerplugin.ad.MidrollIntervalHandler;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.events.AdvertisingEventsAnalytics;
import com.applicaster.jwplayerplugin.analytics.events.PlayerEventsAnalytics;
import com.applicaster.jwplayerplugin.cast.CastProvider;
import com.applicaster.jwplayerplugin.ad.AdState;
import com.applicaster.jwplayerplugin.networkutil.NetworkChangeReceiver;
import com.applicaster.jwplayerplugin.networkutil.NetworkState;
import com.applicaster.jwplayerplugin.networkutil.NetworkUtil;
import com.applicaster.player.defaultplayer.gmf.layeredvideo.VideoPlayer;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayersManager;
import com.applicaster.zapp_automation.AutomationManager;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastState;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.AdCompleteEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.CompleteEvent;
import com.longtailvideo.jwplayer.events.ControlBarVisibilityEvent;
import com.longtailvideo.jwplayer.events.ErrorEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.ReadyEvent;
import com.longtailvideo.jwplayer.events.SeekEvent;
import com.longtailvideo.jwplayer.events.TimeEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

import java.util.Map;

public class JWPlayerActivity
        extends AppCompatActivity
        implements VideoPlayerEvents.OnFullscreenListener,
        VideoPlayerEvents.OnControlBarVisibilityListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnSeekListener,
        VideoPlayerEvents.OnCompleteListener,
        AdvertisingEvents.OnAdCompleteListener,
        AdvertisingEvents.OnAdPlayListener,
        VideoPlayerEvents.OnErrorListener,
        NetworkUtil.ConnectionAvailabilityCallback {

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

    private double playerPosition = -1;
    private PlayerViewState previousViewState = PlayerViewState.FULLSCREEN;
    private AdState adState = AdState.IDLE;
    private NetworkState networkState = NetworkState.CONNECTED;
    private boolean isAlreadySeekRequested = false;
    private MidrollIntervalHandler midrollIntervalHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayer);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

       initPlayer();

        playable = (Playable) getIntent().getSerializableExtra(Constants.PLAYABLE_KEY);
        boolean enableChromecast = getIntent().getBooleanExtra(Constants.CAST_ENABLED_KEY, false);
        String receiverAppId = getIntent().getStringExtra(Constants.CAST_RECEIVER_APP_ID);
        previousViewState = PlayerViewState.fromOrdinal(
                getIntent().getIntExtra(Constants.PREVIOUS_VIEW_STATE,
                        PlayerViewState.FULLSCREEN.ordinal())
        );

        //analytics data and events
        AnalyticsData analyticsData = new AnalyticsData(playable, mPlayerView);
        playerEventsAnalytics = new PlayerEventsAnalytics(analyticsData, playable, mPlayerView);
        advertisingEventsAnalytics = new AdvertisingEventsAnalytics(analyticsData, playable, mPlayerView);

        //Initialize cast provider
        initChromecast(enableChromecast, receiverAppId, analyticsData);

        Map configuration = null;
        if (PlayersManager.getCurrentPlayer() != null) {
            configuration = PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
        }

        AutomationManager.getInstance().setAccessibilityIdentifier(mPlayerView, "jw_player_screen");

        // Load a media source
        mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
        midrollIntervalHandler = new MidrollIntervalHandler(mPlayerView, JWPlayerUtil.getConfigMidrollInterval());
        playerPosition =
                getIntent().getDoubleExtra(Constants.PLAYER_CURRENT_POSITION, -1);
        mPlayerView.play();
        analyticsData.setItemDuration(mPlayerView);
        if (previousViewState == PlayerViewState.INLINE)
            mPlayerView.setFullscreen(true, true);
    }

    private void initPlayer() {
        jwPlayerContainer = findViewById(R.id.playerView);
        mPlayerView = jwPlayerContainer.getJWPlayerView();
        setPlayerListeners();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setPlayerListeners() {
        mPlayerView.addOnFullscreenListener(this);
        mPlayerView.addOnSeekListener(this);
        mPlayerView.addOnAdPlayListener(this);
        mPlayerView.addOnAdCompleteListener(this);
        mPlayerView.addOnControlBarVisibilityListener(this);
        mPlayerView.addOnErrorListener(this);
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnCompleteListener(this);
    }

    private void initChromecast(boolean enableChromecast,
                                String receiverAppId,
                                AnalyticsData analyticsData) {
        if (enableChromecast) {
            castProvider = new CastProvider(this, jwPlayerContainer);
            castProvider.init(playable,
                    analyticsData,
                    playerEventsAnalytics.getScreenAnalyticsState(),
                    receiverAppId);
        }
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
            if (mPlayerView.getFullscreen() && previousViewState == PlayerViewState.FULLSCREEN) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
            if (mPlayerView.getFullscreen() && previousViewState == PlayerViewState.INLINE) {
                setExitFullscreenData();
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setExitFullscreenData() {
        if (mPlayerView.getFullscreen())
            mPlayerView.setFullscreen(false, true);
        JWPlayerAdapter.previousViewState = PlayerViewState.FULLSCREEN;
        JWPlayerAdapter.currentPlayerPosition = mPlayerView.getPosition();
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        if (midrollIntervalHandler != null) {
            midrollIntervalHandler.release();
            midrollIntervalHandler = new MidrollIntervalHandler(mPlayerView, JWPlayerUtil.getConfigMidrollInterval());
        }
    }

    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreenEvent true if the player is fullscreen
     */
    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        if (castProvider != null)
            castProvider.setScreenAnalyticsState(playerEventsAnalytics.getScreenAnalyticsState());
        // When going to Fullscreen we want to set fitsSystemWindows="false"
        jwPlayerContainer.setFitsSystemWindows(!fullscreenEvent.getFullscreen());
        if (fullscreenEvent.getFullscreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            if (previousViewState == PlayerViewState.INLINE) {
                setExitFullscreenData();
                finish();
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public static void startPlayerActivity(Context context, Playable playable, Map<String, Object> params) {
        Intent intent = new Intent(context, JWPlayerActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.PLAYABLE_KEY, playable);

        //Obtain configuration params
        Object isCastEnabled = params.get(Constants.CAST_ENABLED_KEY);
        if (isCastEnabled instanceof String)
            bundle.putBoolean(Constants.CAST_ENABLED_KEY, JWPlayerUtil.parseBoolean(isCastEnabled.toString()));
        Object castReceiverAppId = params.get(Constants.CAST_RECEIVER_APP_ID);
        if (castReceiverAppId instanceof String)
            bundle.putString(Constants.CAST_RECEIVER_APP_ID, (String)castReceiverAppId);
        Object playerCurrentPosition = params.get(Constants.PLAYER_CURRENT_POSITION);
        if (playerCurrentPosition != null)
            bundle.putDouble(Constants.PLAYER_CURRENT_POSITION, (double)playerCurrentPosition);
        Object playerViewState = params.get(Constants.PREVIOUS_VIEW_STATE);
        if (playerViewState instanceof PlayerViewState)
            bundle.putInt(Constants.PREVIOUS_VIEW_STATE, ((PlayerViewState)playerViewState).ordinal());

        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        seekAfterScreenSwitch();
    }

    private void seekAfterScreenSwitch() {
        if (playerPosition > 0 && !isAlreadySeekRequested) {
            mPlayerView.seek(playerPosition);
            isAlreadySeekRequested = true;
        }
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
        if (mPlayerView.getState() == PlayerState.IDLE
                && networkState == NetworkState.DISCONNECTED) {
            Map configuration = null;
            if (PlayersManager.getCurrentPlayer() != null) {
                configuration = PlayersManager.getCurrentPlayer().getPluginConfigurationParams();
            }
            mPlayerView.load(JWPlayerUtil.getPlaylistItem(playable, configuration));
            mPlayerView.seek(playerPosition);
            mPlayerView.play();
        }
        networkState = NetworkState.CONNECTED;
    }

    @Override
    public void onNetworkLost() {
        networkState = NetworkState.DISCONNECTED;
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