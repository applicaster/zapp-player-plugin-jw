package com.applicaster.jwplayerplugin.analytics;

import com.applicaster.analytics.AnalyticsAgentUtil;

import java.util.HashMap;

public class AnalyticsAdapter {

    public static void logTapCast(AnalyticsData data) {
        AnalyticsAgentUtil.logEvent("Tap Cast", buildPropsMap(data, true));
    }

    public static void logCastStart(AnalyticsData data) {
        AnalyticsAgentUtil.logEvent("Cast Start", buildPropsMap(data, false));
    }

    public static void logCastStop(AnalyticsData data) {
        AnalyticsAgentUtil.logEvent("Cast Stop", buildPropsMap(data, false));
    }

    private static HashMap<String, String> buildPropsMap(AnalyticsData data, boolean addPreviousState) {
        HashMap<String, String> propsMap = new HashMap<>();
        if (addPreviousState) {
            propsMap.put(AnalyticsTypes.CastProperties.PREVIOUS_STATE, data.getPreviousState());
        }
        propsMap.put(AnalyticsTypes.CastProperties.CASTING_DEVICE, data.getCastingDevice());
        propsMap.put(AnalyticsTypes.CastProperties.FREE_OR_PAID, data.getFreeOrPaid());
        propsMap.put(AnalyticsTypes.CastProperties.ITEM_DURATION, data.getItemDuration());
        propsMap.put(AnalyticsTypes.CastProperties.ITEM_ID, data.getItemId());
        propsMap.put(AnalyticsTypes.CastProperties.ITEM_NAME, data.getItemName());
        propsMap.put(AnalyticsTypes.CastProperties.TIME_CODE, data.getTimeCode());
        propsMap.put(AnalyticsTypes.CastProperties.VIDEO_TYPE, data.getVideoType());
        propsMap.put(AnalyticsTypes.CastProperties.VIEW, data.getPlayerView());
        propsMap.put(AnalyticsTypes.CastProperties.VOD_TYPE, data.getVodType());
        return propsMap;
    }

}
