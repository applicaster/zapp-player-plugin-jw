package com.applicaster.jwplayerplugin.analytics;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnalyticsTypes {

    //region Analytics Events
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Event.PLAY_VOD_ITEM,
                Event.PLAY_LIVE_STREAM,
                Event.SWITCH_PLAYER_VIEW,
                Event.PAUSE,
                Event.SEEK,
                Event.TAP_REWIND,
                Event.TAP_CLOSED_CAPTIONS,
                Event.TAP_CAST,
                Event.CAST_START,
                Event.CAST_STOP})
    public @interface Event {
        String PLAY_VOD_ITEM = "Play VOD Item";
        String PLAY_LIVE_STREAM = "Play Live Stream";
        String SWITCH_PLAYER_VIEW = "Switch Player View";
        String PAUSE = "Pause";
        String SEEK = "Seek";
        String TAP_REWIND = "Tap Rewind";
        String TAP_CLOSED_CAPTIONS = "Tap Closed Captions";
        String TAP_CAST = "Tap Cast";
        String CAST_START = "Cast Start";
        String CAST_STOP = "Cast Stop";
    }
    //endregion

    //region Common Types
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CommonProps.ITEM_ID,
                CommonProps.ITEM_NAME,
                CommonProps.ITEM_DURATION,
                CommonProps.VOD_TYPE,
                CommonProps.VIEW,
                CommonProps.VIDEO_TYPE,
                CommonProps.FREE_OR_PAID,
                CommonProps.TIME_CODE})
    public @interface CommonProps {
        String ITEM_ID = "Item ID";
        String ITEM_NAME = "Item Name";
        String ITEM_DURATION = "Item Duration";
        String VOD_TYPE = "VOD Type";
        String VIEW = "View";
        String VIDEO_TYPE = "Video Type";
        String FREE_OR_PAID = "Free or Paid";
        String TIME_CODE = "Time Code";
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
    //endregion

    //region Player Analytics

    //endregion

    //region Cast Analytics
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CastProps.CASTING_DEVICE, CastProps.PREVIOUS_STATE})
    public @interface CastProps {
        String CASTING_DEVICE = "Casting Device";
        String PREVIOUS_STATE = "Previous State";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CastBtnPreviousState.ON, CastBtnPreviousState.OFF})
    public @interface CastBtnPreviousState {
        String ON = "On";
        String OFF = "Off";
    }
    //endregion
}
