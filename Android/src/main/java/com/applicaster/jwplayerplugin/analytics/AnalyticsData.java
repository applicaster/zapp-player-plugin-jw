package com.applicaster.jwplayerplugin.analytics;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applicaster.atom.model.APAtomEntry;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.plugin_manager.playersmanager.internal.PlayableType;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AnalyticsData {

    private String PROP_DATA_NONE_PROVIDED = "None Provided";
    private String KEY_MOBILE_DEVICE = "Mobile Device: ";
    private String KEY_CAST_DEVICE = " Cast Device: ";

    private String itemId = PROP_DATA_NONE_PROVIDED;
    private String itemName = PROP_DATA_NONE_PROVIDED;
    private String videoType = PROP_DATA_NONE_PROVIDED;
    private String playerView = PROP_DATA_NONE_PROVIDED;
    private String itemDuration = PROP_DATA_NONE_PROVIDED;
    private String vodType = PROP_DATA_NONE_PROVIDED;
    private String freeOrPaid = PROP_DATA_NONE_PROVIDED;
    private String timeCode = PROP_DATA_NONE_PROVIDED;
    private String castingDevice = KEY_MOBILE_DEVICE + getOsVersion() + KEY_CAST_DEVICE + PROP_DATA_NONE_PROVIDED;
    private String previousState = PROP_DATA_NONE_PROVIDED;

    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@Nullable String itemId) {
        if (itemId != null)
            this.itemId = itemId;
    }

    @NonNull
    public String getItemName() {
        return itemName;
    }

    public void setItemName(@Nullable String itemName) {
        if (itemName != null)
            this.itemName = itemName;
    }

    @NonNull
    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(@NonNull Playable playable) {
        if (playable.isLive()) {
            this.videoType = AnalyticsTypes.VideoType.LIVE;
        } else {
            this.videoType = AnalyticsTypes.VideoType.VOD;
        }
    }

    @NonNull
    public String getPlayerView() {
        return playerView;
    }

    public void setPlayerView(@NonNull String playerView) {
        this.playerView = playerView;
    }

    @NonNull
    public String getItemDuration() {
        return itemDuration;
    }

    public void setItemDuration(double itemDuration) {
        this.itemDuration = parseDuration((long)itemDuration);
    }

    @NonNull
    public String getVodType() {
        return vodType;
    }

    public void setVodType(@NonNull Playable playable) {
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

    public void setFreeOrPaid(boolean isFree) {
        if (isFree)
            this.freeOrPaid = AnalyticsTypes.FreeOrPaid.FREE;
        else
            this.freeOrPaid = AnalyticsTypes.FreeOrPaid.PAID;
    }

    @NonNull
    public String getTimeCode() {
        return timeCode;
    }

    public void setTimeCode(double timeCode) {
        this.timeCode = parseDuration((long) timeCode);
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

    /**
     * Returns a formatted duration string.
     *
     * @param duration The given duration, for example "12345".
     * @return the formatted duration string, for example "01:05:20". If something went wrong returns an None Provided string.
     */
    private String parseDuration(long duration) {
        if (duration > 0) {
            long durationMillis = duration * 1000;

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
