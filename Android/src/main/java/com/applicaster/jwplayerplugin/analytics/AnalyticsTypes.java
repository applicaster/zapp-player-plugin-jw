package com.applicaster.jwplayerplugin.analytics;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnalyticsTypes {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CastProperties.ITEM_ID,
                CastProperties.ITEM_NAME,
                CastProperties.VIDEO_TYPE,
                CastProperties.VIEW,
                CastProperties.ITEM_DURATION,
                CastProperties.VOD_TYPE,
                CastProperties.FREE_OR_PAID,
                CastProperties.TIME_CODE,
                CastProperties.CASTING_DEVICE,
                CastProperties.PREVIOUS_STATE})
    public @interface CastProperties {
        String ITEM_ID = "Item ID";
        String ITEM_NAME = "Item Name";
        String VIDEO_TYPE = "Video Type";
        String VIEW = "View";
        String ITEM_DURATION = "Item Duration";
        String VOD_TYPE = "VOD Type";
        String FREE_OR_PAID = "Free or Paid";
        String TIME_CODE = "Time Code";
        String CASTING_DEVICE = "Casting Device";
        String PREVIOUS_STATE = "Previous State";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FreeOrPaid.FREE, FreeOrPaid.PAID})
    public @interface FreeOrPaid {
        String FREE = "Free";
        String PAID = "Paid";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VideoType.VOD, VideoType.LIVE})
    public @interface VideoType {
        String VOD = "VOD";
        String LIVE = "Live";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PlayerView.FULLSCREEN, PlayerView.INLINE, PlayerView.CAST})
    public @interface PlayerView {
        String FULLSCREEN = "Fullscreen";
        String INLINE = "Inline";
        String CAST = "Cast";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VodType.APPLICASTER_MODEL, VodType.YOUTUBE, VodType.ATOM})
    public @interface VodType {
        String APPLICASTER_MODEL = "Applicaster Model";
        String YOUTUBE = "YouTube";
        String ATOM = "ATOM";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CastBtnPreviousState.ON, CastBtnPreviousState.OFF})
    public @interface CastBtnPreviousState {
        String ON = "On";
        String OFF = "Off";
    }
}
