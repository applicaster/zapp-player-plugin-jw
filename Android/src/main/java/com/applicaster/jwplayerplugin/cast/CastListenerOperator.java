package com.applicaster.jwplayerplugin.cast;

import android.util.Log;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.longtailvideo.jwplayer.JWPlayerView;

public class CastListenerOperator implements SessionManagerListener<CastSession> {

    private final String TAG = this.getClass().getSimpleName();

    private CastSession castSession;
    private JWPlayerView playerView;

    public CastListenerOperator(JWPlayerView playerView) {
        this.playerView = playerView;
    }

    public CastSession getCastSession() {
        return this.castSession;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session STARTING for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session STARTED for: " + castSession.getCastDevice().getFriendlyName());
        this.castSession = castSession;
        playerView.play();
    }

    @Override
    public void onSessionStartFailed(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session START FAILED for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionEnding(CastSession castSession) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session ENDING for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionEnded(CastSession castSession, int i) {
        this.castSession = null;
    }

    @Override
    public void onSessionResuming(CastSession castSession, String s) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMING for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionResumed(CastSession castSession, boolean b) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMED for: " + castSession.getCastDevice().getFriendlyName());
        this.castSession = castSession;
        playerView.play();
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUME FAILED for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session SUSPENDED for: " + castSession.getCastDevice().getFriendlyName());
    }

    /**
     * indicates whether we are doing a local or a remote playback
     */
    public static enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    /**
     * List of various states that we can be in
     */
    public static enum PlaybackState {
        PLAYING,
        PAUSED,
        BUFFERING,
        IDLE
    }
}
