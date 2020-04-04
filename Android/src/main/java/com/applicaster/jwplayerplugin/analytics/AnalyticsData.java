package com.applicaster.jwplayerplugin.analytics;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applicaster.atom.model.APAtomEntry;
import com.applicaster.plugin_manager.Plugin;
import com.applicaster.plugin_manager.PluginManager;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayableType;
import com.longtailvideo.jwplayer.JWPlayerView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AnalyticsData {

    public static String PROP_DATA_NONE_PROVIDED = "None Provided";
    private String KEY_MOBILE_DEVICE = "Mobile Device: ";
    private String KEY_CAST_DEVICE = " Cast Device: ";

    //Common fields
    private String itemId = PROP_DATA_NONE_PROVIDED;
    private String itemName = PROP_DATA_NONE_PROVIDED;
    private String videoType = PROP_DATA_NONE_PROVIDED;
    private String view = PROP_DATA_NONE_PROVIDED;
    private String itemDuration = PROP_DATA_NONE_PROVIDED;
    private String vodType = PROP_DATA_NONE_PROVIDED;
    private String freeOrPaid = PROP_DATA_NONE_PROVIDED;
    private String timeCode = PROP_DATA_NONE_PROVIDED;
    private String castingDevice = KEY_MOBILE_DEVICE + getOsVersion() + KEY_CAST_DEVICE + PROP_DATA_NONE_PROVIDED;
    private String previousState = PROP_DATA_NONE_PROVIDED;
    private String completedVideoByUser = PROP_DATA_NONE_PROVIDED;
    private String programName = PROP_DATA_NONE_PROVIDED;
    private String originalView = PROP_DATA_NONE_PROVIDED;
    private String newView = PROP_DATA_NONE_PROVIDED;
    private String switchInstance = PROP_DATA_NONE_PROVIDED;
    private String durationInVideo = PROP_DATA_NONE_PROVIDED;
    private String seekDirection = PROP_DATA_NONE_PROVIDED;
    private String timeCodeFrom = PROP_DATA_NONE_PROVIDED;
    private String timeCodeTo = PROP_DATA_NONE_PROVIDED;
    private String itemLink = PROP_DATA_NONE_PROVIDED;
    private String videoPlayerPlugin = PROP_DATA_NONE_PROVIDED;
    private String videoPlayErrorMessage = PROP_DATA_NONE_PROVIDED;
    private String videoPlayExceptionErrorProps = PROP_DATA_NONE_PROVIDED;
    //AD fields
    private String videoAdErrorCode = PROP_DATA_NONE_PROVIDED;
    private String adProvider = PROP_DATA_NONE_PROVIDED;
    private String videoAdType = PROP_DATA_NONE_PROVIDED;
    private String adUnit = PROP_DATA_NONE_PROVIDED;
    private String adSkipped = AnalyticsTypes.Skipped.N_A;
    private String adBreakTime = PROP_DATA_NONE_PROVIDED;
    private String adBreakDuration = PROP_DATA_NONE_PROVIDED;
    private String adExitMethod = PROP_DATA_NONE_PROVIDED;
    private String timeWhenAdExited = PROP_DATA_NONE_PROVIDED;
    private String adClicked = AnalyticsTypes.AdClicked.NO;


    public AnalyticsData(Playable playable, JWPlayerView playerView) {
        setItemId(playable);
        setItemName(playable);
        setItemDuration(playerView);
        setVodType(playable);
        setVideoType(playable);
        setFreeOrPaid(playable);
        setItemLink(playable);
        setVideoPlayerPlugin();
        setAdProvider();
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    private void setItemId(@NonNull Playable playable) {
        if (playable.getPlayableId() != null)
            this.itemId = playable.getPlayableId();
    }

    @NonNull
    public String getItemName() {
        return itemName;
    }

    private void setItemName(@NonNull Playable playable) {
        if (playable.getPlayableName() != null)
            this.itemName = playable.getPlayableName();
    }

    @NonNull
    public String getVideoType() {
        return videoType;
    }

    private void setVideoType(@NonNull Playable playable) {
        if (playable.isLive()) {
            this.videoType = AnalyticsTypes.VideoType.LIVE;
        } else {
            this.videoType = AnalyticsTypes.VideoType.VOD;
        }
    }

    @NonNull
    public String getView() {
        return view;
    }

    public void setView(@NonNull String playerView) {
        this.view = playerView;
    }

    @NonNull
    public String getItemDuration() {
        return itemDuration;
    }

    public void setItemDuration(JWPlayerView playerView) {
        this.itemDuration = parseDuration((long)playerView.getDuration(), false);
    }

    @NonNull
    public String getVodType() {
        return vodType;
    }

    private void setVodType(@NonNull Playable playable) {
        if (!playable.isLive()) {
            if (playable instanceof APAtomEntry.APAtomEntryPlayable) {
                this.vodType = AnalyticsTypes.VodType.ATOM;
            } else if (playable.getPlayableType() == PlayableType.Youtube) {
                this.vodType = AnalyticsTypes.VodType.YOUTUBE;
            } else {
                this.vodType = AnalyticsTypes.VodType.APPLICASTER_MODEL;
            }
        }
    }

    @NonNull
    public String getFreeOrPaid() {
        return freeOrPaid;
    }

    private void setFreeOrPaid(Playable playable) {
        if (playable.isFree())
            this.freeOrPaid = AnalyticsTypes.FreeOrPaid.FREE;
        else
            this.freeOrPaid = AnalyticsTypes.FreeOrPaid.PAID;
    }

    @NonNull
    public String getTimeCode() {
        return timeCode;
    }

    public void setTimeCode(double timeCode) {
        this.timeCode = parseDuration((long) timeCode, false);
    }

    @NonNull
    public String getCastingDevice() {
        return castingDevice;
    }

    public void setCastingDevice(@Nullable String castingDevice) {
        if (castingDevice != null)
            this.castingDevice = KEY_MOBILE_DEVICE + getOsVersion() + KEY_CAST_DEVICE + castingDevice;
    }

    @NonNull
    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(@Nullable String previousState) {
        if (previousState != null)
            this.previousState = previousState;
    }

    @NonNull
    public String getCompletedVideoByUser() {
        return completedVideoByUser;
    }

    public void setCompletedVideoByUser(@NonNull String completedVideoByUser) {
        this.completedVideoByUser = completedVideoByUser;
    }

    @NonNull
    public String getProgramName() {
        return programName;
    }

    public void setProgramName(@Nullable String programName) {
        if (programName != null)
            this.programName = programName;
    }

    @NonNull
    public String getOriginalView() {
        return originalView;
    }

    public void setOriginalView(@Nullable String originalView) {
        if (originalView != null)
            this.originalView = originalView;
    }

    @NonNull
    public String getNewView() {
        return newView;
    }

    public void setNewView(@Nullable String newView) {
        if (newView != null)
            this.newView = newView;
    }

    @NonNull
    public String getSwitchInstance() {
        return switchInstance;
    }

    public void setSwitchInstance(int switchInstance) {
        this.switchInstance = String.valueOf(switchInstance);
    }

    @NonNull
    public String getDurationInVideo() {
        return durationInVideo;
    }

    public void setDurationInVideo(long durationInVideo) {
        this.durationInVideo = parseDuration(durationInVideo, true);
    }

    @NonNull
    public String getSeekDirection() {
        return seekDirection;
    }

    public void setSeekDirection(@Nullable String seekDirection) {
        if (seekDirection != null)
            this.seekDirection = seekDirection;
    }

    @NonNull
    public String getTimeCodeFrom() {
        return timeCodeFrom;
    }

    public void setTimeCodeFrom(double timeCodeFrom) {
        this.timeCodeFrom = parseDuration((long)timeCodeFrom, false);
    }

    @NonNull
    public String getTimeCodeTo() {
        return timeCodeTo;
    }

    public void setTimeCodeTo(double timeCodeTo) {
        this.timeCodeTo = parseDuration((long)timeCodeTo, false);
    }

    @NonNull
    public String getItemLink() {
        return itemLink;
    }

    private void setItemLink(Playable playable) {
        if (playable.getContentVideoURL() != null)
            this.itemLink = playable.getContentVideoURL();
    }

    @NonNull
    public String getVideoPlayerPlugin() {
        return videoPlayerPlugin;
    }

    private void setVideoPlayerPlugin() {
        String pluginName = null;
        try {
            pluginName = PluginManager.getInstance().getInitiatedPlugin(Plugin.Type.PLAYER).plugin.name;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
        }
        if (pluginName != null) this.videoPlayerPlugin = pluginName;
    }

    @NonNull
    public String getVideoPlayErrorMessage() {
        return videoPlayErrorMessage;
    }

    public void setVideoPlayErrorMessage(@Nullable String videoPlayErrorMessage) {
        if (videoPlayErrorMessage != null)
            this.videoPlayErrorMessage = videoPlayErrorMessage;
    }

    @NonNull
    public String getVideoPlayExceptionErrorProps() {
        return videoPlayExceptionErrorProps;
    }

    public void setVideoPlayExceptionErrorProps(@Nullable String videoPlayExceptionErrorProps) {
        if (videoPlayExceptionErrorProps != null)
            this.videoPlayExceptionErrorProps = videoPlayExceptionErrorProps;
    }

    @NonNull
    public String getVideoAdErrorCode() {
        return videoAdErrorCode;
    }

    public void setVideoAdErrorCode(@Nullable String videoAdErrorCode) {
        if (videoAdErrorCode != null)
            this.videoAdErrorCode = videoAdErrorCode;
    }

    @NonNull
    public String getAdProvider() {
        return adProvider;
    }

    private void setAdProvider() {
            this.adProvider = "DFP";
    }

    @NonNull
    public String getVideoAdType() {
        return videoAdType;
    }

    public void setVideoAdType(@Nullable String videoAdType) {
        if (videoAdType != null)
            this.videoAdType = videoAdType;
    }

    @NonNull
    public String getAdUnit() {
        return adUnit;
    }

    public void setAdUnit(@Nullable String adUnit) {
        if (adUnit != null)
            this.adUnit = adUnit;
    }

    @NonNull
    public String getAdSkipped() {
        return adSkipped;
    }

    public void setAdSkipped(@Nullable String adSkipped) {
        if (adSkipped != null)
            this.adSkipped = adSkipped;
    }

    @NonNull
    public String getAdBreakTime() {
        return adBreakTime;
    }

    public void setAdBreakTime(JWPlayerView playerView) {
        this.adBreakTime = parseDuration((long)playerView.getPosition(), false);
    }

    @NonNull
    public String getAdBreakDuration() {
        return adBreakDuration;
    }

    public void setAdBreakDuration(double adBreakDuration) {
        this.adBreakDuration = parseDuration((long)adBreakDuration, false);
    }

    @NonNull
    public String getAdExitMethod() {
        return adExitMethod;
    }

    public void setAdExitMethod(@Nullable String adExitMethod) {
        if (adExitMethod != null)
            this.adExitMethod = adExitMethod;
    }

    @NonNull
    public String getTimeWhenAdExited() {
        return timeWhenAdExited;
    }

    public void setTimeWhenAdExited(double timeWhenAdExited) {
        this.timeWhenAdExited = parseDuration((long)timeWhenAdExited, false);
    }

    @NonNull
    public String getAdClicked() {
        return adClicked;
    }

    public void setAdClicked(@Nullable String adClicked) {
        if (adClicked != null)
            this.adClicked = adClicked;
    }

    /**
     * Returns a formatted duration string.
     *
     * @param duration The given duration, for example "12345".
     * @return the formatted duration string, for example "01:05:20". If something went wrong returns an None Provided string.
     */
    private String parseDuration(long duration, boolean isInMilliseconds) {
        if (duration > 0) {
            long durationMillis;
            if (isInMilliseconds) {
                durationMillis = duration;
            } else {
                durationMillis = duration * 1000;
            }

            long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % TimeUnit.HOURS.toMinutes(1);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % TimeUnit.MINUTES.toSeconds(1);

            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return PROP_DATA_NONE_PROVIDED;
    }

    public String getOsVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }
}
