package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.applicaster.player.defaultplayer.BasePlayer;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.PlayableConfiguration;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.core.PlayerState;

import java.util.List;
import java.util.Map;

public class JWPlayerAdapter extends BasePlayer {

    private static final String LICENSE_KEY = "LICENSE_KEY";

    // Properties
//    private JWPlayerContainer jwPlayerContainer;
    private JWPlayerView jwPlayerView;
    private String licenseKey;

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

        JWPlayerView.setLicenseKey(context, licenseKey);
        jwPlayerView = new JWPlayerView(context, new PlayerConfig.Builder().build());

//        jwPlayerContainer = new JWPlayerContainer(context);
//        jwPlayerView = jwPlayerContainer.getJWPlayerView();
//        PlaylistItem playlistItem = new PlaylistItem(getFirstPlayable().getContentVideoURL());
//        jwPlayerView.load(playlistItem);
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
        licenseKey = params.get(LICENSE_KEY).toString();
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
        JWPlayerActivity.startPlayerActivity(context, getFirstPlayable(), configuration.getCustomConfiguration());
//        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        windowManager.addView(jwPlayerContainer, mParams);
//        jwPlayerView.play();
//        jwPlayerView.setFullscreen(true, false);
    }


    //---------- Inline ----------//
    /**
     * Add the player into the given container.
     *
     * @param videoContainerView The container to add the player to.
     */
    @Override
    public void attachInline(@NonNull ViewGroup videoContainerView) {
        ViewGroup.LayoutParams playerContainerLayoutParams
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        videoContainerView.addView(jwPlayerView, playerContainerLayoutParams);
    }

    /**
     * Remove the player from it's container.
     *
     * @param videoContainerView The container that contains the player.
     */
    @Override
    public void removeInline(@NonNull ViewGroup videoContainerView) {
        if(videoContainerView.indexOfChild(jwPlayerView) >= 0) {
            videoContainerView.removeView(jwPlayerView);
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
    }

    /**
     * Stops playing the inline player.
     */
    @Override
    public void stopInline() {
        jwPlayerView.stop();
    }

    /**
     * Pauses playing the inline player
     */
    @Override
    public void pauseInline() {
        jwPlayerView.pause();
    }
    /**
     * Resumes playing the inline player.
     */
    @Override
    public void resumeInline() {
        jwPlayerView.play();
    }
}
