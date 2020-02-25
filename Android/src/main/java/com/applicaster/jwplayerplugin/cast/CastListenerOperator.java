package com.applicaster.jwplayerplugin.cast;

import android.util.Log;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class CastListenerOperator implements SessionManagerListener<CastSession> {

    private final String TAG = this.getClass().getSimpleName();

    private CastSession castSession;

    public CastSession getCastSession() {
        return this.castSession;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session STARTING for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session STARTED for: " + castSession.getCastDevice().getModelName());
        this.castSession = castSession;
    }

    @Override
    public void onSessionStartFailed(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session START FAILED for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionEnding(CastSession castSession) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session ENDING for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionEnded(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session ENDED for: " + castSession.getCastDevice().getModelName());
        this.castSession = null;
    }

    @Override
    public void onSessionResuming(CastSession castSession, String s) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMING for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionResumed(CastSession castSession, boolean b) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMED for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUME FAILED for: " + castSession.getCastDevice().getModelName());
    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session SUSPENDED for: " + castSession.getCastDevice().getModelName());
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
