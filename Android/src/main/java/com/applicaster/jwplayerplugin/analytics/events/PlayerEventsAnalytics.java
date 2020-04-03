package com.applicaster.jwplayerplugin.analytics.events;

import android.util.Log;

import com.applicaster.analytics.AnalyticsAgentUtil;
import com.applicaster.jwplayerplugin.analytics.AnalyticsAdapter;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.CompleteEvent;
import com.longtailvideo.jwplayer.events.ErrorEvent;
import com.longtailvideo.jwplayer.events.FullscreenEvent;
import com.longtailvideo.jwplayer.events.PauseEvent;
import com.longtailvideo.jwplayer.events.PlayEvent;
import com.longtailvideo.jwplayer.events.ReadyEvent;
import com.longtailvideo.jwplayer.events.SeekEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class PlayerEventsAnalytics
        implements VideoPlayerEvents.OnFullscreenListener,
        VideoPlayerEvents.OnSeekListener,
        VideoPlayerEvents.OnReadyListener,
        VideoPlayerEvents.OnPlayListener,
        VideoPlayerEvents.OnPauseListener,
        VideoPlayerEvents.OnCompleteListener,
        VideoPlayerEvents.OnErrorListener {

    private Playable playable;
    private JWPlayerView playerView;
    private AnalyticsData analyticsData;

    private String screenAnalyticsState = AnalyticsTypes.PlayerView.INLINE;
    private int playerViewSwitchCount = 0;
    private long durationInVideoStartTime;
    private ReadyState videoReadyState = ReadyState.UNDEFINED;

    private enum ReadyState {
        UNDEFINED, READY, ACTIVE
    }

    public PlayerEventsAnalytics(AnalyticsData analyticsData, Playable playable, JWPlayerView playerView) {
        this.durationInVideoStartTime = System.currentTimeMillis();
        this.playable = playable;
        this.playerView = playerView;
        this.analyticsData = analyticsData;
        playerView.addOnFullscreenListener(this);
        playerView.addOnSeekListener(this);
        playerView.addOnReadyListener(this);
        playerView.addOnPlayListener(this);
        playerView.addOnPauseListener(this);
        playerView.addOnErrorListener(this);
    }

    public AnalyticsData getAnalyticsData() {
        return analyticsData;
    }

    public String getScreenAnalyticsState() {
        return screenAnalyticsState;
    }

    public void backPressed() {
        playerViewSwitchCount = 0;
        if (playable.isLive()) {
            analyticsData.setView(screenAnalyticsState);
            AnalyticsAdapter.logPlayLiveStream(analyticsData, AnalyticsTypes.TimedEvent.END);
        } else {
            analyticsData.setCompletedVideoByUser(AnalyticsTypes.Completed.YES);
            analyticsData.setView(screenAnalyticsState);
            AnalyticsAdapter.logPlayVodItem(analyticsData, AnalyticsTypes.TimedEvent.END);
        }
        videoReadyState = ReadyState.READY;
    }

    @Override
    public void onComplete(CompleteEvent completeEvent) {
        playerViewSwitchCount = 0;
        if (playable.isLive()) {
            analyticsData.setView(AnalyticsTypes.PlayerView.FULLSCREEN);
            AnalyticsAdapter.logPlayLiveStream(analyticsData, AnalyticsTypes.TimedEvent.END);
        } else {
            analyticsData.setCompletedVideoByUser(AnalyticsTypes.Completed.NO);
            analyticsData.setView(AnalyticsTypes.PlayerView.FULLSCREEN);
            AnalyticsAdapter.logPlayVodItem(analyticsData, AnalyticsTypes.TimedEvent.END);
        }
        videoReadyState = ReadyState.READY;
    }

    @Override
    public void onError(ErrorEvent errorEvent) {
        analyticsData.setVideoPlayErrorMessage(errorEvent.getMessage());
        try {
            analyticsData.setVideoPlayExceptionErrorProps(errorEvent.getException().getMessage());
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Failed to obtain exception properties.");
        }
        AnalyticsAdapter.logVideoPlayError(analyticsData);
    }

    @Override
    public void onFullscreen(FullscreenEvent fullscreenEvent) {
        analyticsData.setDurationInVideo(System.currentTimeMillis() - durationInVideoStartTime);
        playerViewSwitchCount++;
        if (fullscreenEvent.getFullscreen()) {
            screenAnalyticsState = AnalyticsTypes.PlayerView.FULLSCREEN;
            analyticsData.setOriginalView(AnalyticsTypes.PlayerView.INLINE);
            analyticsData.setView(AnalyticsTypes.PlayerView.INLINE);
        } else {
            screenAnalyticsState = AnalyticsTypes.PlayerView.INLINE;
            analyticsData.setOriginalView(AnalyticsTypes.PlayerView.FULLSCREEN);
            analyticsData.setView(AnalyticsTypes.PlayerView.FULLSCREEN);
        }
        analyticsData.setNewView(screenAnalyticsState);
        analyticsData.setTimeCode(playerView.getPosition());
        analyticsData.setSwitchInstance(playerViewSwitchCount);
        AnalyticsAdapter.logSwitchPlayerView(analyticsData);
    }

    @Override
    public void onPause(PauseEvent pauseEvent) {
        analyticsData.setDurationInVideo(System.currentTimeMillis() - durationInVideoStartTime);
        analyticsData.setTimeCode(playerView.getPosition());
        AnalyticsAdapter.logPause(analyticsData);
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        videoReadyState = ReadyState.READY;
        analyticsData.setView(screenAnalyticsState);
    }

    @Override
    public void onPlay(PlayEvent playEvent) {
        if (videoReadyState != ReadyState.ACTIVE) {
            analyticsData.setItemDuration(playerView);
            if (playable.isLive()) {
                AnalyticsAdapter.logPlayLiveStream(analyticsData, AnalyticsTypes.TimedEvent.START);
            } else {
                analyticsData.setCompletedVideoByUser(AnalyticsTypes.Completed.NO);
                AnalyticsAdapter.logPlayVodItem(analyticsData, AnalyticsTypes.TimedEvent.START);
            }
        }
        videoReadyState = ReadyState.ACTIVE;
    }

    @Override
    public void onSeek(SeekEvent seekEvent) {
        double position = seekEvent.getPosition();
        double offset = seekEvent.getOffset();
        if (position != 0 && offset != 0) {
            analyticsData.setSeekDirection(calculateSeekDirection(position, offset));
            analyticsData.setTimeCodeFrom(position);
            analyticsData.setTimeCodeTo(offset);
            AnalyticsAdapter.logSeek(analyticsData);
        }
    }

    private String calculateSeekDirection(double position, double offset) {
        if (offset > position)
            return AnalyticsTypes.SeekDirection.FAST_FORWARD;
        return AnalyticsTypes.SeekDirection.REWIND;
    }
}
