package com.applicaster.jwplayerplugin.analytics;

import com.applicaster.analytics.AnalyticsAgentUtil;

import java.util.HashMap;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes.*;

public class AnalyticsAdapter {

    public static void logPlayVodItem(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.PLAY_VOD_ITEM, resultProps);
    }

    public static void logPlayLiveStream(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.PLAY_LIVE_STREAM, resultProps);
    }

    public static void logSwitchPlayerView(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.SWITCH_PLAYER_VIEW, resultProps);
    }

    public static void logPause(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.PAUSE, resultProps);
    }

    public static void logSeek(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.SEEK, resultProps);
    }

    public static void logTapRewind(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.TAP_REWIND, resultProps);
    }

    public static void logTapClosedCaptions(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> resultProps = new HashMap<>();
        AnalyticsAgentUtil.logTimedEvent(Event.TAP_CLOSED_CAPTIONS, resultProps);
    }

    public static void logTapCast(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> castProps = buildCastAnalyticsPropsMap(data, true);
        HashMap<String, String> resultProps = new HashMap<>();
        resultProps.putAll(commonProps);
        resultProps.putAll(castProps);
        AnalyticsAgentUtil.logEvent(Event.TAP_CAST, resultProps);
    }

    public static void logCastStart(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> castProps = buildCastAnalyticsPropsMap(data, false);
        HashMap<String, String> resultProps = new HashMap<>();
        resultProps.putAll(commonProps);
        resultProps.putAll(castProps);
        AnalyticsAgentUtil.logEvent(Event.CAST_START, resultProps);
    }

    public static void logCastStop(AnalyticsData data) {
        HashMap<String, String> commonProps = buildCommonPropsMap(data);
        HashMap<String, String> castProps = buildCastAnalyticsPropsMap(data, false);
        HashMap<String, String> resultProps = new HashMap<>();
        resultProps.putAll(commonProps);
        resultProps.putAll(castProps);
        AnalyticsAgentUtil.logEvent(Event.CAST_STOP, resultProps);
    }

    private static HashMap<String, String> buildCastAnalyticsPropsMap(AnalyticsData data, boolean addPreviousState) {
        HashMap<String, String> propsMap = new HashMap<>();
        if (addPreviousState) {
            propsMap.put(CastProps.PREVIOUS_STATE, data.getPreviousState());
        }
        propsMap.put(CastProps.CASTING_DEVICE, data.getCastingDevice());
        return propsMap;
    }

    private static HashMap<String, String> buildCommonPropsMap(AnalyticsData data) {
        HashMap<String, String> propsMap = new HashMap<>();
        propsMap.put(CommonProps.FREE_OR_PAID, data.getFreeOrPaid());
        propsMap.put(CommonProps.ITEM_DURATION, data.getItemDuration());
        propsMap.put(CommonProps.ITEM_ID, data.getItemId());
        propsMap.put(CommonProps.ITEM_NAME, data.getItemName());
        propsMap.put(CommonProps.TIME_CODE, data.getTimeCode());
        propsMap.put(CommonProps.VIDEO_TYPE, data.getVideoType());
        propsMap.put(CommonProps.VIEW, data.getPlayerView());
        propsMap.put(CommonProps.VOD_TYPE, data.getVodType());
        return propsMap;
    }

}
