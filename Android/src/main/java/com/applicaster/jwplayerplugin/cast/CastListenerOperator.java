package com.applicaster.jwplayerplugin.cast;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.applicaster.jwplayerplugin.R;
import com.applicaster.jwplayerplugin.analytics.AnalyticsAdapter;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.longtailvideo.jwplayer.JWPlayerView;

import java.lang.ref.WeakReference;

public class CastListenerOperator implements SessionManagerListener<CastSession> {

    private final String TAG = this.getClass().getSimpleName();

    private CastSession castSession;
    private WeakReference<JWPlayerView> playerView;
    private AnalyticsData analyticsData;

    public CastListenerOperator(JWPlayerView playerView, AnalyticsData analyticsData) {
        this.playerView = new WeakReference<>(playerView);
        this.analyticsData = analyticsData;
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
    public void onSessionStarted(CastSession castSession, String sessionId) {
        if (castSession.getCastDevice() != null) {
            Log.i(TAG, "Cast session STARTED for: " + castSession.getCastDevice().getFriendlyName());
            analyticsData.setCastingDevice(castSession.getCastDevice().getFriendlyName());
        }
        this.castSession = castSession;
        playerView.get().play();
        playerView.get().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerView.get().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                processViewChildren(playerView.get());
            }
        });
        analyticsData.setTimeCode(playerView.get().getPosition());
        analyticsData.setPlayerView(AnalyticsTypes.PlayerView.CAST);
        AnalyticsAdapter.logCastStart(analyticsData);
    }

    @Override
    public void onSessionStartFailed(CastSession castSession, int error) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session START FAILED for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionEnding(CastSession castSession) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session ENDING for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionEnded(CastSession castSession, int error) {
        this.castSession = null;
        analyticsData.setTimeCode(playerView.get().getPosition());
        analyticsData.setPlayerView(AnalyticsTypes.PlayerView.FULLSCREEN);
        AnalyticsAdapter.logCastStop(analyticsData);
    }

    @Override
    public void onSessionResuming(CastSession castSession, String sessionId) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMING for: " + castSession.getCastDevice().getFriendlyName());
    }

    @Override
    public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUMED for: " + castSession.getCastDevice().getFriendlyName());
        this.castSession = castSession;
        playerView.get().play();
        analyticsData.setPlayerView(AnalyticsTypes.PlayerView.CAST);
        processViewChildren(playerView.get());
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int error) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session RESUME FAILED for: " + castSession.getCastDevice().getFriendlyName());
        analyticsData.setPlayerView(AnalyticsTypes.PlayerView.FULLSCREEN);
    }

    @Override
    public void onSessionSuspended(CastSession castSession, int reason) {
        if (castSession.getCastDevice() != null)
            Log.i(TAG, "Cast session SUSPENDED for: " + castSession.getCastDevice().getFriendlyName());
    }

    private void processViewChildren(@Nullable ViewGroup view) {
        if (view != null) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View child = view.getChildAt(i);
                processChild(child);
            }
        }
    }

    private void processChild(@Nullable View view) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                processViewChildren((ViewGroup) view);
            } else if (view instanceof TextView) {
                try {
                    String btnContentText = ((TextView) view).getText().toString();
                    String castingToResourceText = playerView.get().getContext().getString(R.string.casting_to);
                    String textToMatch = castingToResourceText.split(":")[0];
                    if (btnContentText.contains(textToMatch)) {
                        String castingDeviceViewText = playerView.get().getContext().getString(
                                R.string.casting_to,
                                castSession.getCastDevice().getFriendlyName()
                        );
                        ((TextView) view).setText(castingDeviceViewText);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }
}
