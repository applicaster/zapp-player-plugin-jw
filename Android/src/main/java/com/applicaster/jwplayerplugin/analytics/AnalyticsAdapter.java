package com.applicaster.jwplayerplugin.analytics;

import com.applicaster.analytics.AnalyticsAgentUtil;

import java.util.HashMap;
import java.util.Map;

import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes.*;

public class AnalyticsAdapter {

    //region Public analytics methods
    public static void logPlayVodItem(AnalyticsData data, TimedEvent timedEvent) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getPlayVodItemProps(data));
        if (timedEvent == TimedEvent.START) {
            AnalyticsAgentUtil.logTimedEvent(Event.PLAY_VOD_ITEM, analyticsProps);
        } else {
            AnalyticsAgentUtil.endTimedEvent(Event.PLAY_VOD_ITEM, analyticsProps);
        }
    }

    public static void logPlayLiveStream(AnalyticsData data, TimedEvent timedEvent) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getPlayLiveStreamProps(data));
        if (timedEvent == TimedEvent.START) {
            AnalyticsAgentUtil.logTimedEvent(Event.PLAY_LIVE_STREAM, analyticsProps);
        } else {
            AnalyticsAgentUtil.endTimedEvent(Event.PLAY_LIVE_STREAM, analyticsProps);
        }
    }

    public static void logSwitchPlayerView(AnalyticsData data) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getSwitchPlayerView(data));
        AnalyticsAgentUtil.logEvent(Event.SWITCH_PLAYER_VIEW, analyticsProps);
    }

    public static void logPause(AnalyticsData data) {
        HashMap<String, String> analyticsData = getCommonProps(data);
        analyticsData.putAll(getPauseProps(data));
        AnalyticsAgentUtil.logEvent(Event.PAUSE, analyticsData);
    }

    public static void logSeek(AnalyticsData data) {
        HashMap<String, String> analyticsData = getCommonProps(data);
        analyticsData.putAll(getSeekProps(data));
        AnalyticsAgentUtil.logEvent(Event.SEEK, analyticsData);
    }

    public static void logVideoPlayError(AnalyticsData data) {
        HashMap<String, String> analyticsData = getCommonProps(data);
        analyticsData.putAll(getVideoPlayErrorProps(data));
        AnalyticsAgentUtil.logEvent(Event.VIDEO_PLAY_ERROR, analyticsData);
    }

    public static void logVideoAdError(AnalyticsData data) {
        HashMap<String, String> analyticsData = getCommonProps(data);
        analyticsData.putAll(getVideoAdErrorProps(data));
        AnalyticsAgentUtil.logEvent(Event.VIDEO_AD_ERROR, analyticsData);
    }

    public static void logWatchVideoAdvertising(AnalyticsData data) {
        HashMap<String, String> analyticsData = getCommonProps(data);
        analyticsData.putAll(getVideoAdvertisingProps(data));
        AnalyticsAgentUtil.logEvent(Event.WATCH_VIDEO_AD);
    }

    public static void logTapCast(AnalyticsData data) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getCommonCastProps(data));
        analyticsProps.putAll(getCastAnalyticsProps(data, true));
        AnalyticsAgentUtil.logEvent(Event.TAP_CAST, analyticsProps);
    }

    public static void logCastStart(AnalyticsData data) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getCommonCastProps(data));
        analyticsProps.putAll(getCastAnalyticsProps(data, false));
        AnalyticsAgentUtil.logEvent(Event.CAST_START, analyticsProps);
    }

    public static void logCastStop(AnalyticsData data) {
        HashMap<String, String> analyticsProps = getCommonProps(data);
        analyticsProps.putAll(getCommonCastProps(data));
        analyticsProps.putAll(getCastAnalyticsProps(data, false));
        AnalyticsAgentUtil.logEvent(Event.CAST_STOP, analyticsProps);
    }
    //endregion

    //region Private helper methods
    private static HashMap<String, String> getCommonProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.FREE_OR_PAID, data.getFreeOrPaid());
        propsMap.put(CommonProps.ITEM_ID, data.getItemId());
        propsMap.put(CommonProps.ITEM_NAME, data.getItemName());
        return propsMap;
    }

    private static HashMap<String, String> getCommonCastProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.TIME_CODE, data.getTimeCode());
        propsMap.put(CommonProps.VIDEO_TYPE, data.getVideoType());
        propsMap.put(CommonProps.VIEW, data.getView());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        return propsMap;
    }

    private static HashMap<String, String> getCastAnalyticsProps(AnalyticsData data, boolean addPreviousState) {
        HashMap<String, String> propsMap = new HashMap<>();
        if (addPreviousState) {
            propsMap.put(CastProps.PREVIOUS_STATE, data.getPreviousState());
        }
        propsMap.put(CastProps.CASTING_DEVICE, data.getCastingDevice());
        return propsMap;
    }

    private static HashMap<String, String> getPlayVodItemProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.USER_COMPLETED_VIDEO, data.getCompletedVideoByUser());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.VIEW, data.getView());
        return propsMap;
    }

    private static HashMap<String, String> getPlayLiveStreamProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.VIEW, data.getView());
        propsMap.put(CommonProps.PROGRAM_NAME, data.getProgramName());
        return propsMap;
    }

    private static HashMap<String, String> getSwitchPlayerView(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.ORIGINAL_VIEW, data.getOriginalView());
        propsMap.put(CommonProps.NEW_VIEW, data.getNewView());
        propsMap.put(CommonProps.VIDEO_TYPE, data.getVideoType());
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.TIME_CODE, data.getTimeCode());
        propsMap.put(CommonProps.SWITCH_INSTANCE, data.getSwitchInstance());
        propsMap.put(CommonProps.DURATION_IN_VIDEO, data.getDurationInVideo());
        return propsMap;
    }

    private static HashMap<String, String> getPauseProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.VIDEO_TYPE, data.getVideoType());
        propsMap.put(CommonProps.VIEW, data.getView());
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.TIME_CODE, data.getTimeCode());
        propsMap.put(CommonProps.DURATION_IN_VIDEO, data.getDurationInVideo());
        return propsMap;
    }

    private static HashMap<String, String> getSeekProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.SEEK_DIRECTION, data.getSeekDirection());
        propsMap.put(CommonProps.VIEW, data.getView());
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.TIMECODE_FROM, data.getTimeCodeFrom());
        propsMap.put(CommonProps.TIMECODE_TO, data.getTimeCodeTo());
        return propsMap;
    }

    private static HashMap<String, String> getVideoPlayErrorProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        //Playable props
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.ITEM_LINK, data.getItemLink());
        propsMap.put(CommonProps.USER_COMPLETED_VIDEO, data.getCompletedVideoByUser());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.VIEW, data.getView());
        //Video play error props
        propsMap.put(VideoPlayErrorProps.VIDEO_PLAYER_PLUGIN, data.getVideoPlayerPlugin());
        propsMap.put(VideoPlayErrorProps.ERROR_MESSAGE, data.getVideoPlayErrorMessage());
        propsMap.put(VideoPlayErrorProps.EXCEPTION_EVENT_PROPERTIES, data.getVideoPlayExceptionErrorProps());
        return propsMap;
    }

    private static Map<String, String> getVideoAdErrorProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        //Playable props
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.ITEM_LINK, data.getItemLink());
        propsMap.put(CommonProps.USER_COMPLETED_VIDEO, data.getCompletedVideoByUser());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(CommonProps.VIEW, data.getView());
        //Video ad error props
        propsMap.put(VideoAdErrorProps.VIDEO_PLAYER_PLUGIN, data.getVideoPlayerPlugin());
        propsMap.put(VideoAdErrorProps.ERROR_CODE, data.getVideoAdErrorCode());
        propsMap.put(AdProps.ADVERTISING_PROVIDER, data.getAdProvider());
        return propsMap;
    }

    private static Map<String, String> getVideoAdvertisingProps(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        propsMap.put(AdProps.VIDEO_AD_TYPE, data.getVideoAdType());
        propsMap.put(AdProps.AD_PROVIDER, data.getAdProvider());
        propsMap.put(AdProps.AD_UNIT, data.getAdUnit());
        propsMap.put(AdProps.SKIPPED, data.getAdSkipped());
        propsMap.put(AdProps.CONTENT_VIDEO_DURATION, data.getItemDuration());
        propsMap.put(AdProps.AD_BREAK_TIME, data.getAdBreakTime());
        propsMap.put(AdProps.AD_BREAK_DURATION, data.getAdBreakDuration());
        propsMap.put(AdProps.AD_EXIT_METHOD, data.getAdExitMethod());
        propsMap.put(AdProps.TIME_WHEN_EXITED, data.getTimeWhenAdExited());
        propsMap.put(AdProps.CLICKED, data.getAdClicked());
        return propsMap;
    }
    //endregion

}
