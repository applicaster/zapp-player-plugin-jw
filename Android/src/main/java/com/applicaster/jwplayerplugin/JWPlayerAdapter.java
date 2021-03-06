package com.applicaster.jwplayerplugin;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;

import com.applicaster.controller.PlayerLoader;
import com.applicaster.jwplayerplugin.ad.MidrollIntervalHandler;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes;
import com.applicaster.jwplayerplugin.analytics.events.AdvertisingEventsAnalytics;
import com.applicaster.jwplayerplugin.analytics.events.PlayerEventsAnalytics;
import com.applicaster.jwplayerplugin.cast.CastProvider;
import com.applicaster.jwplayerplugin.player.Constants;
import com.applicaster.jwplayerplugin.player.JWPlayerActivity;
import com.applicaster.jwplayerplugin.player.JWPlayerContainer;
import com.applicaster.jwplayerplugin.player.JWPlayerUtil;
import com.applicaster.jwplayerplugin.player.PlayerViewState;
import com.applicaster.player.PlayerLoaderI;
import com.applicaster.player.defaultplayer.BasePlayer;
import com.applicaster.plugin_manager.login.LoginContract;
import com.applicaster.plugin_manager.login.LoginManager;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.PlayableConfiguration;
import com.applicaster.plugin_manager.screen.PluginScreen;
import com.google.android.gms.cast.framework.CastState;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.CompleteEvent;
import com.longtailvideo.jwplayer.events.ControlBarVisibilityEvent;
import com.longtailvideo.jwplayer.events.FirstFrameEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.fullscreen.FullscreenHandler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWPlayerAdapter
        extends    BasePlayer
        implements FullscreenHandler,
                   VideoPlayerEvents.OnFullscreenListener,
                   VideoPlayerEvents.OnControlBarVisibilityListener,
                   VideoPlayerEvents.OnPlayListener,
                   VideoPlayerEvents.OnFirstFrameListener,
                   VideoPlayerEvents.OnCompleteListener,
                   PluginScreen, LifecycleObserver {


    public static final String TAG = "JWPLAYER_DEBUG_KEY";
    // Properties
    private JWPlayerContainer jwPlayerContainer;
    private JWPlayerView jwPlayerView;
    private String licenseKey = "";
    private boolean enableChromecast = false;
    private String receiverApplicationID = "";

    private Context context;
    private CastProvider castProvider;

    private PlayerEventsAnalytics playerEventsAnalytics;
    private AdvertisingEventsAnalytics advertisingEventsAnalytics;
    private AnalyticsData analyticsData;

    public static PlayerViewState previousViewState = PlayerViewState.INLINE;
    public static double currentPlayerPosition = -1;
    private boolean isAlreadySeekRequested = false;
    private MidrollIntervalHandler midrollIntervalHandler;

    /**
     * Optional initialization for the PlayerContract - will be called in the App's onCreate
     */
    @Override
    public void init(@NonNull Context appContext) {
        super.init(appContext);
    }

    /**
     * initialization of the player instance with a playable item
     *
     * @param playable
     */
    @Override
    public void init(@NonNull Playable playable, @NonNull Context context) {
        super.init(playable, context);
        this.context = context;
        JWPlayerView.setLicenseKey(context, licenseKey);
    }

    /**
     * initialization of the player instance with  multiple playable items
     *
     * @param playableList
     */
    @Override
    public void init(@NonNull List<Playable> playableList, @NonNull Context context) {
        super.init(playableList, context);

    }

    /**
     * initialization of the player plugin configuration with a Map params.
     * the params taken from res/raw/plugin_configurations.json
     *
     * @param params
     */
    @Override
    public void setPluginConfigurationParams(Map params) {
        super.setPluginConfigurationParams(params);

        Object licenseKeyObj = params.get(Constants.LICENSE_KEY);
        Object enableChromecastObj = params.get(Constants.CAST_ENABLED_KEY);
        Object customMediaReceiverIdObj = params.get(Constants.CAST_RECEIVER_APP_ID);

        if (licenseKeyObj != null)
            licenseKey = licenseKeyObj.toString();

        if (enableChromecastObj != null)
            enableChromecast = JWPlayerUtil.parseBoolean(enableChromecastObj.toString());

        if (customMediaReceiverIdObj != null)
            receiverApplicationID = customMediaReceiverIdObj.toString();
    }

    /**
     * return the player type
     *
     * @return PlayerItem.Type type of the player
     */
    @Override
    public PlayerType getPlayerType() {
        return PlayerType.Default;
    }

    /**
     * Optional method - best to implement but in case you can't it will still just return false.
     *
     * @return the playing state of the player
     */
    @Override
    public boolean isPlayerPlaying() {
        return jwPlayerView.getState() == PlayerState.PLAYING;
    }


    /**
     * start the player in fullscreen with configuration.
     *
     * @param configuration player configuration.
     * @param requestCode   request code if needed - if not send NO_REQUEST_CODE instead.
     * @param context
     */
    @Override
    public void playInFullscreen(PlayableConfiguration configuration, int requestCode, @NonNull Context context) {
        previousViewState = PlayerViewState.FULLSCREEN;
        getPluginConfigurationParams().put(Constants.PREVIOUS_VIEW_STATE, previousViewState);
        openLoginPluginIfNeeded(false);
    }


    //---------- Inline ----------//
    /**
     * Add the player into the given container.
     *
     * @param videoContainerView The container to add the player to.
     */
    @Override
    public void attachInline(@NonNull ViewGroup videoContainerView) {
        initPlayer(videoContainerView);
        initAnalyticsAdapters();
        openLoginPluginIfNeeded(true);
        initChromecast();
    }

    private void initPlayer(@NonNull ViewGroup videoContainerView) {
        jwPlayerContainer = new JWPlayerContainer(videoContainerView.getContext());
        jwPlayerView = jwPlayerContainer.getJWPlayerView();
        previousViewState = PlayerViewState.INLINE;
        setPlayerListeners();
        ViewGroup.LayoutParams playerContainerLayoutParams
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        videoContainerView.addView(jwPlayerContainer, playerContainerLayoutParams);
    }

    private void setPlayerListeners() {
        jwPlayerView.setFullscreenHandler(this);
        jwPlayerView.addOnFullscreenListener(this);
        jwPlayerView.addOnControlBarVisibilityListener(this);
        jwPlayerView.addOnPlayListener(this);
        jwPlayerView.addOnFirstFrameListener(this);
        jwPlayerView.addOnCompleteListener(this);
    }

    private void initChromecast() {
        if (this.context instanceof Activity && enableChromecast) {
            castProvider = new CastProvider((Activity) this.context, jwPlayerContainer);
            castProvider.init(
                    getFirstPlayable(),
                    analyticsData,
                    AnalyticsTypes.PlayerView.INLINE,
                    receiverApplicationID
            );
        }
    }

    private void initAnalyticsAdapters() {
        analyticsData = new AnalyticsData(getFirstPlayable(), jwPlayerView);
        playerEventsAnalytics = new PlayerEventsAnalytics(analyticsData, getFirstPlayable(), jwPlayerView);
        advertisingEventsAnalytics = new AdvertisingEventsAnalytics(analyticsData, getFirstPlayable(), jwPlayerView);
    }

    /**
     * Remove the player from it's container.
     *
     * @param videoContainerView The container that contains the player.
     */
    @Override
    public void removeInline(@NonNull ViewGroup videoContainerView) {
        playerEventsAnalytics.backPressed();
        advertisingEventsAnalytics.backPressed();
        if(videoContainerView.indexOfChild(jwPlayerContainer) >= 0) {
            jwPlayerContainer.getJWPlayerView().onDestroy();
            videoContainerView.removeView(jwPlayerContainer);
        }
        if (castProvider != null) {
            castProvider.removeSessionManagerListener();
            castProvider = null;
        }
    }

    /**
     * Start the player in inline with configuration.
     *
     * @param configuration player configuration.
     */
    @Override
    public void playInline(PlayableConfiguration configuration) {
        jwPlayerView.play();
        Log.d(JWPlayerAdapter.TAG, "playInline");
    }

    /**
     * Stops playing the inline player.
     */
    @Override
    public void stopInline() {
        jwPlayerView.stop();
        Log.d(JWPlayerAdapter.TAG, "stopInline");
    }

    /**
     * Pauses playing the inline player
     */
    @Override
    public void pauseInline() {
        jwPlayerView.pause();
        Log.d(JWPlayerAdapter.TAG, "pauseInline");
    }
    /**
     * Resumes playing the inline player.
     */
    @Override
    public void resumeInline() {
        jwPlayerView.play();
    }

    protected void openLoginPluginIfNeeded(final boolean isInline){
        /**
         * if item is not locked continue to play, otherwise call login with playable item.
         */
        final LoginContract loginPlugin = LoginManager.getLoginPlugin();
        if (loginPlugin != null ){

            loginPlugin.isItemLocked(getContext(), getFirstPlayable(), new LoginContract.Callback() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        loginPlugin.login(getContext(), getFirstPlayable(), null, new LoginContract.Callback() {
                            @Override
                            public void onResult(boolean result) {
                                if (result) {
                                    PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
                                    applicasterPlayerLoader.loadItem();
                                }
                            }
                        });
                    } else {
                        PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
                        applicasterPlayerLoader.loadItem();
                    }
                }
            });
        } else {
            PlayerLoader applicasterPlayerLoader = new PlayerLoader(new ApplicaterPlayerLoaderListener(isInline));
            applicasterPlayerLoader.loadItem();
        }

    }

    protected void displayVideo(boolean isInline){
        if (isInline){
            jwPlayerView.load(JWPlayerUtil.getPlaylistItem(getFirstPlayable(), getPluginConfigurationParams()));
            midrollIntervalHandler = new MidrollIntervalHandler(jwPlayerView, JWPlayerUtil.getConfigMidrollInterval());
            jwPlayerView.seek(currentPlayerPosition);
            jwPlayerView.play();
        }else {
            JWPlayerActivity.startPlayerActivity(getContext(), getFirstPlayable(), getPluginConfigurationParams());
        }
    }

    @Override
    public void onControlBarVisibilityChanged(ControlBarVisibilityEvent controlBarVisibilityEvent) {
        if (castProvider != null
                && castProvider.getCastContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
            if (controlBarVisibilityEvent.isVisible()) {
                castProvider.getMediaRouteButton().setVisibility(View.VISIBLE);
            } else {
                castProvider.getMediaRouteButton().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
        if (castProvider != null)
            castProvider.addSessionManagerListener();
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        if (midrollIntervalHandler != null) {
            midrollIntervalHandler.release();
            midrollIntervalHandler = new MidrollIntervalHandler(jwPlayerView, JWPlayerUtil.getConfigMidrollInterval());
        }
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        seekAfterScreenSwitch();
    }

    private void seekAfterScreenSwitch() {
        if (currentPlayerPosition > 0 && !isAlreadySeekRequested) {
            jwPlayerView.seek(currentPlayerPosition);
            isAlreadySeekRequested = true;
        }
    }

    /************************** FullscreenHandler implementation ********************/

    @Override
    public void onFullscreenRequested() {
        currentPlayerPosition = jwPlayerView.getPosition();
        getPluginConfigurationParams().put(Constants.PLAYER_CURRENT_POSITION, currentPlayerPosition);
        getPluginConfigurationParams().put(Constants.PREVIOUS_VIEW_STATE, previousViewState);
        jwPlayerView.stop();
        displayVideo(false);
        Log.d(JWPlayerAdapter.TAG, "onFullscreenRequested");
    }

    @Override
    public void onFullscreenExitRequested() {
        handleExitFullscreen();
        Log.d(JWPlayerAdapter.TAG, "onFullscreenExitRequested");
    }

    @Override
    public void onResume() {
        handleExitFullscreen();
        if (castProvider != null) castProvider.removeSessionManagerListener();
    }

    private void handleExitFullscreen() {
        if (previousViewState == PlayerViewState.FULLSCREEN) {
            displayVideo(true);
            previousViewState = PlayerViewState.INLINE;
            isAlreadySeekRequested = false;
        }
    }

    @Override
    public void onPause() {
        jwPlayerView.pause();
        if (castProvider != null) castProvider.removeSessionManagerListener();
    }

    @Override
    public void onDestroy() {
//        jwPlayerView.stop();
    }

    @Override
    public void onAllowRotationChanged(boolean b) { }

    @Override
    public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        Log.d(JWPlayerAdapter.TAG, "updateLayoutParams");
    }

    @Override
    public void setUseFullscreenLayoutFlags(boolean b) {
        Log.d(JWPlayerAdapter.TAG, "setUseFullscreenLayoutFlags");
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        Log.d(JWPlayerAdapter.TAG, "onFullScreen");
    }

    //region PluginScreen
    //Necessary to implement this method to allow prehooks, however this constructor is never called for a player
    @Override
    public void present(Context context, HashMap<String, Object> screenMap, Serializable dataSource, boolean isActivity) { }

    @Override
    public Fragment generateFragment(HashMap<String, Object> screenMap, Serializable dataSource) {
        return null;
    }

    //endregion


    /************************** PlayerLoaderI ********************/
    private class ApplicaterPlayerLoaderListener implements PlayerLoaderI {
        private boolean isInline;

        public ApplicaterPlayerLoaderListener(boolean isInline) {
            this.isInline=isInline;
        }

        @Override
        public String getItemId() {
            return getPlayable().getPlayableId();
        }


        @Override
        public Playable getPlayable() {
            return getFirstPlayable();
        }

        @Override
        public void onItemLoaded(Playable playable) {
            init(playable, getContext());
            displayVideo(isInline);
        }

        @Override
        public boolean isFinishing() {
            return ((Activity) getContext()).isFinishing();
        }

        @Override
        public void showMediaErroDialog() {

        }
    }
}
