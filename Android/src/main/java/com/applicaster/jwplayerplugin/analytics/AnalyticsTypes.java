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
                Event.WATCH_VIDEO_AD,
                Event.VIDEO_PLAY_ERROR,
                Event.VIDEO_AD_ERROR,
                Event.TAP_CAST,
                Event.CAST_START,
                Event.CAST_STOP})
    public @interface Event {
        String PLAY_VOD_ITEM = "Play VOD Item";
        String PLAY_LIVE_STREAM = "Play Live Stream";
        String SWITCH_PLAYER_VIEW = "Switch Player View";
        String PAUSE = "Pause";
        String SEEK = "Seek";
        String WATCH_VIDEO_AD = "Watch Video Advertisement";
        String VIDEO_PLAY_ERROR = "Video Play Error";
        String VIDEO_AD_ERROR = "Video Ad Error";
        String TAP_CAST = "Tap Cast";
        String CAST_START = "Cast Start";
        String CAST_STOP = "Cast Stop";
    }
    //endregion

    //region Common Types
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CommonProps.ITEM_ID,
                CommonProps.ITEM_NAME,
                CommonProps.ITEM_LINK,
                CommonProps.ITEM_DURATION,
                CommonProps.VOD_TYPE,
                CommonProps.VIEW,
                CommonProps.VIDEO_TYPE,
                CommonProps.FREE_OR_PAID,
                CommonProps.TIME_CODE,
                CommonProps.USER_COMPLETED_VIDEO,
                CommonProps.ORIGINAL_VIEW,
                CommonProps.NEW_VIEW,
                CommonProps.SWITCH_INSTANCE,
                CommonProps.DURATION_IN_VIDEO,
                CommonProps.PROGRAM_NAME,
                CommonProps.SEEK_DIRECTION,
                CommonProps.TIMECODE_FROM,
                CommonProps.TIMECODE_TO})
    public @interface CommonProps {
        String ITEM_ID = "Item ID";
        String ITEM_NAME = "Item Name";
        String ITEM_LINK = "Item Link";
        String ITEM_DURATION = "Item Duration";
        String VOD_TYPE = "VOD Type";
        String VIEW = "View";
        String VIDEO_TYPE = "Video Type";
        String FREE_OR_PAID = "Free or Paid";
        String TIME_CODE = "Timecode";
        String USER_COMPLETED_VIDEO = "Completed";
        String ORIGINAL_VIEW = "Original View";
        String NEW_VIEW = "New View";
        String SWITCH_INSTANCE = "Switch Instance";
        String DURATION_IN_VIDEO = "Duration In Video";
        String PROGRAM_NAME = "Program Name";
        String SEEK_DIRECTION = "Seek Direction";
        String TIMECODE_FROM = "Timecode From";
        String TIMECODE_TO = "Timecode To";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VideoPlayErrorProps.VIDEO_PLAYER_PLUGIN,
                VideoPlayErrorProps.ERROR_MESSAGE,
                VideoPlayErrorProps.EXCEPTION_EVENT_PROPERTIES})
    public @interface VideoPlayErrorProps {
        String VIDEO_PLAYER_PLUGIN = "Video Player Plugin";
        String ERROR_MESSAGE = "Error Message";
        String EXCEPTION_EVENT_PROPERTIES = "Exception Event Properties";
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
    public enum TimedEvent {
        START, END
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Completed.YES, Completed.NO})
    public @interface Completed {
        String YES = "Yes";
        String NO = "No";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({OriginalView.FULLSCREEN, OriginalView.INLINE, OriginalView.CAST})
    public @interface OriginalView {
        String FULLSCREEN = "Fullscreen";
        String INLINE = "Inline";
        String CAST = "Cast";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({NewView.FULLSCREEN, NewView.INLINE, NewView.CAST})
    public @interface NewView {
        String FULLSCREEN = "Fullscreen";
        String INLINE = "Inline";
        String CAST = "Cast";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SeekDirection.FAST_FORWARD, SeekDirection.REWIND})
    public @interface SeekDirection {
        String FAST_FORWARD = "Fast Forward";
        String REWIND = "Rewind";
    }
    //endregion

    //region AdAnalytics
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({AdProps.ADVERTISING_PROVIDER,
                AdProps.AD_UNIT,
                AdProps.AD_BREAK_TIME,
                AdProps.VIDEO_AD_TYPE,
                AdProps.AD_PROVIDER,
                AdProps.SKIPPED,
                AdProps.CONTENT_VIDEO_DURATION,
                AdProps.AD_BREAK_DURATION,
                AdProps.AD_EXIT_METHOD,
                AdProps.TIME_WHEN_EXITED,
                AdProps.CLICKED})
    public @interface AdProps {
        String ADVERTISING_PROVIDER = "Advertising Provider";
        String AD_UNIT = "Ad Unit";
        String AD_BREAK_TIME = "Ad Break Time";
        String VIDEO_AD_TYPE = "Video Ad Type";
        String AD_PROVIDER = "Ad Provider";
        String SKIPPED = "Skipped";
        String CONTENT_VIDEO_DURATION = "Content Video Duration";
        String AD_BREAK_DURATION = "Ad Break Duration";
        String AD_EXIT_METHOD = "Ad Exit Method";
        String TIME_WHEN_EXITED = "Time When Exited";
        String CLICKED = "Clicked";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VideoAdType.PREROLL,
                VideoAdType.MIDROLL,
                VideoAdType.POSTROLL})
    public @interface VideoAdType {
        String PREROLL = "Preroll";
        String MIDROLL = "Midroll";
        String POSTROLL = "Postroll";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Skipped.YES,
                Skipped.NO,
                Skipped.N_A})
    public @interface Skipped {
        String YES = "Yes";
        String NO = "No";
        String N_A = "N/A";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Skipped.YES,
                Skipped.NO})
    public @interface AdClicked {
        String YES = "Yes";
        String NO = "No";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({AdExitMethod.COMPLETED,
                AdExitMethod.SKIPPED,
                AdExitMethod.AD_SERVER_ERROR,
                AdExitMethod.CLOSED_APP,
                AdExitMethod.CLICKED,
                AdExitMethod.UNSPECIFIED,
                AdExitMethod.ANDROID_BACK_BUTTON})
    public @interface AdExitMethod {
        String COMPLETED = "Completed";
        String SKIPPED = "Skipped";
        String AD_SERVER_ERROR = "Ad Server Error";
        String CLOSED_APP = "Closed App";
        String CLICKED = "Clicked";
        String UNSPECIFIED = "Unspecified";
        String ANDROID_BACK_BUTTON = "android_back_button";
    }


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({VideoAdErrorProps.PLAYABLE_PROPERTIES,
                VideoAdErrorProps.VIDEO_PLAYER_PLUGIN,
                VideoAdErrorProps.ERROR_CODE})
    public @interface VideoAdErrorProps {
        String PLAYABLE_PROPERTIES = "Playable Properties";
        String VIDEO_PLAYER_PLUGIN = "Video Player Plugin";
        String ERROR_CODE = "Error Code";
    }

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
