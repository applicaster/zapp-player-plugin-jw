package com.applicaster.jwplayerplugin.ad;

import com.google.android.exoplayer2.util.Log;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.FirstFrameEvent;
import com.longtailvideo.jwplayer.events.TimeEvent;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.ads.AdBreak;

public class MidrollIntervalHandler
        implements VideoPlayerEvents.OnTimeListener,
        VideoPlayerEvents.OnFirstFrameListener {

    private final String TAG = this.getClass().getSimpleName();

    private JWPlayerView playerView;
    private AdBreak adBreak;
    private int midrollInterval;
    private int previousAbBreakPositionIndex;


    public MidrollIntervalHandler(JWPlayerView playerView, AdBreak adBreak) {
        if (adBreak != null) {
            this.playerView = playerView;
            this.adBreak = adBreak;
            this.previousAbBreakPositionIndex = -1;
            setListeners();
        }
    }

    private void setListeners() {
        playerView.addOnTimeListener(this);
        playerView.addOnFirstFrameListener(this);
    }

    private void playMidrollIfNeeded() {
        if (midrollInterval > 0) {
            int playerPosition = (int) playerView.getPosition();
            int adBreakPositionIndex = (playerPosition / midrollInterval) - 1;
            if (adBreakPositionIndex >= 0 && adBreakPositionIndex > previousAbBreakPositionIndex) {
                previousAbBreakPositionIndex = adBreakPositionIndex;
                playerView.playAd(adBreak.getTag().get(0));
                Log.i(TAG, "playAd => adBreakPositionIndex="
                        + adBreakPositionIndex
                        + " | adBreakTime="
                        + playerPosition);
            }
        }
    }

    @Override
    public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
        this.midrollInterval = Double.valueOf(adBreak.getOffset()).intValue();
    }

    @Override
    public void onTime(TimeEvent timeEvent) {
        playMidrollIfNeeded();
    }
}
