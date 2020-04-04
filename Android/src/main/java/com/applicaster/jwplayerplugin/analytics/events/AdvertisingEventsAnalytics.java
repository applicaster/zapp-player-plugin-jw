package com.applicaster.jwplayerplugin.analytics.events;

import androidx.annotation.Nullable;

import com.applicaster.jwplayerplugin.analytics.AnalyticsAdapter;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.events.AdClickEvent;
import com.longtailvideo.jwplayer.events.AdCompleteEvent;
import com.longtailvideo.jwplayer.events.AdErrorEvent;
import com.longtailvideo.jwplayer.events.AdMetaEvent;
import com.longtailvideo.jwplayer.events.AdPlayEvent;
import com.longtailvideo.jwplayer.events.AdSkippedEvent;
import com.longtailvideo.jwplayer.events.AdTimeEvent;
import com.longtailvideo.jwplayer.events.listeners.AdvertisingEvents;
import com.longtailvideo.jwplayer.media.ads.AdPosition;

public class AdvertisingEventsAnalytics
        implements AdvertisingEvents.OnAdPlayListener,
        AdvertisingEvents.OnAdCompleteListener,
        AdvertisingEvents.OnAdMetaListener,
        AdvertisingEvents.OnAdErrorListener,
        AdvertisingEvents.OnAdClickListener,
        AdvertisingEvents.OnAdSkippedListener,
        AdvertisingEvents.OnAdTimeListener {

    private JWPlayerView playerView;
    private AnalyticsData analyticsData;
    private AdState adState = AdState.UNDEFINED;

    private enum AdState {
        UNDEFINED,
        PLAY
    }

    public AdvertisingEventsAnalytics(AnalyticsData analyticsData, Playable playable, JWPlayerView playerView) {
        this.playerView = playerView;
        this.analyticsData = analyticsData;
        playerView.addOnAdPlayListener(this);
        playerView.addOnAdCompleteListener(this);
        playerView.addOnAdMetaListener(this);
        playerView.addOnAdErrorListener(this);
        playerView.addOnAdClickListener(this);
        playerView.addOnAdSkippedListener(this);
        playerView.addOnAdTimeListener(this);
    }

    @Override
    public void onAdPlay(AdPlayEvent adPlayEvent) {
        adState = AdState.PLAY;
        analyticsData.setAdBreakTime(playerView);
    }

    @Override
    public void onAdComplete(AdCompleteEvent adCompleteEvent) {
        adState = AdState.UNDEFINED;
        analyticsData.setAdExitMethod(AnalyticsTypes.AdExitMethod.COMPLETED);
        analyticsData.setAdSkipped(AnalyticsTypes.Skipped.NO);
        AnalyticsAdapter.logWatchVideoAdvertising(analyticsData);
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        adState = AdState.UNDEFINED;
        String adError = adErrorEvent.getMessage();
        analyticsData.setAdExitMethod(AnalyticsTypes.AdExitMethod.AD_SERVER_ERROR);
        if (adError != null)
            this.analyticsData.setVideoAdErrorCode(adError);
        AnalyticsAdapter.logVideoAdError(analyticsData);
        AnalyticsAdapter.logWatchVideoAdvertising(analyticsData);
    }

    @Override
    public void onAdMeta(AdMetaEvent adMetaEvent) {
        analyticsData.setVideoAdType(getVideoAdType(adMetaEvent.getAdPosition()));
        analyticsData.setAdUnit(adMetaEvent.getTag());
    }

    @Override
    public void onAdClick(AdClickEvent adClickEvent) {
        analyticsData.setAdClicked(AnalyticsTypes.AdClicked.YES);
        analyticsData.setAdExitMethod(AnalyticsTypes.AdExitMethod.CLICKED);
        AnalyticsAdapter.logWatchVideoAdvertising(analyticsData);
        analyticsData.setAdClicked(AnalyticsTypes.AdClicked.NO);
    }

    @Override
    public void onAdSkipped(AdSkippedEvent adSkippedEvent) {
        adState = AdState.UNDEFINED;
        analyticsData.setAdSkipped(AnalyticsTypes.Skipped.YES);
        analyticsData.setAdExitMethod(AnalyticsTypes.AdExitMethod.SKIPPED);
        AnalyticsAdapter.logWatchVideoAdvertising(analyticsData);
    }

    @Override
    public void onAdTime(AdTimeEvent adTimeEvent) {
        if ((long)adTimeEvent.getPosition() == 0) {
            analyticsData.setAdBreakDuration(adTimeEvent.getDuration());
        }
        analyticsData.setTimeWhenAdExited(adTimeEvent.getPosition());
    }

    public void backPressed() {
        if (adState == AdState.PLAY) {
            analyticsData.setAdExitMethod(AnalyticsTypes.AdExitMethod.ANDROID_BACK_BUTTON);
            AnalyticsAdapter.logWatchVideoAdvertising(analyticsData);
        }
    }

    @Nullable
    private String getVideoAdType(AdPosition position) {
        String adType;
        switch (position) {
            case PRE:
                adType = AnalyticsTypes.VideoAdType.PREROLL;
                break;
            case MID:
                adType = AnalyticsTypes.VideoAdType.MIDROLL;
                break;
            case POST:
                adType = AnalyticsTypes.VideoAdType.POSTROLL;
                break;
            default:
                adType = null;
        }
        return adType;
    }
}
