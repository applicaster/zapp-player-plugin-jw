package com.applicaster.jwplayerplugin;

import android.util.Log;

import com.applicaster.atom.helpers.MediaItemIdentifier;
import com.applicaster.atom.model.APAtomEntry;
import com.applicaster.player.VideoAdsUtil;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.util.AppData;
import com.applicaster.util.StringUtil;
import com.longtailvideo.jwplayer.media.ads.AdBreak;
import com.longtailvideo.jwplayer.media.ads.AdSource;
import com.longtailvideo.jwplayer.media.captions.Caption;
import com.longtailvideo.jwplayer.media.captions.CaptionType;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public class JWPlayerUtil {
    final static String EXTERNAL_PLAYER_MIDROLL_INTERVAL = "midroll_interval_external_player";

    public static PlaylistItem getPlaylistItem(Playable playable) {
        return getPlaylistItem(playable, null);
    }

    public static PlaylistItem getPlaylistItem(Playable playable, Map pluginConfiguration) {
        PlaylistItem result = null;
        String streamImageUrl = null;

        if (playable != null) {
            if (playable instanceof APAtomEntry.APAtomEntryPlayable) {
                APAtomEntry entry = ((APAtomEntry.APAtomEntryPlayable) playable).getEntry();
                streamImageUrl = getStreamImageUrl(entry);
            }
            // Load a media source
            result = new PlaylistItem.Builder()
                    .file(playable.getContentVideoURL())
                    .image(streamImageUrl)
                    .title(playable.getPlayableName())
                    .description(playable.getPlayableDescription())
                    .adSchedule(getAdSchedule(playable, pluginConfiguration))
                    .build();
            try {
                List<Caption> captions = getCaptions(playable);
                if (captions.size() != 0) {
                    result.setCaptions(captions);
                }
            } catch (Exception e) {
                Log.e("JWPlayerUtil", e.toString());
            }

        }

        return result;

    }

    private static List<Caption> getCaptions(Playable playable) {
        List<Caption> captionList = new ArrayList<>();
        ArrayList tracks = null;

        if (playable instanceof APAtomEntry.APAtomEntryPlayable) {

            LinkedHashMap textTracks = (LinkedHashMap) ((APAtomEntry.APAtomEntryPlayable) playable).getEntry().getExtensions().get("text_tracks");

            if (textTracks != null) {
                tracks = (ArrayList) textTracks.get("tracks");
            }

            if (tracks != null) {
                for (int i = 0; i < tracks.size(); i++) {
                    LinkedHashMap<String, String> textTrack = (LinkedHashMap<String, String>) tracks.get(i);

                    String src = textTrack.get("source");
                    String label = textTrack.get("label");
                    String kind = textTrack.get("kind");

                    if (StringUtil.isNotEmpty(src)) {
                        Caption.Builder caption = new Caption.Builder();
                        caption.file(src);

                        if (StringUtil.isNotEmpty(label)) {
                            caption.label(label);
                        }

                        if (StringUtil.isNotEmpty(kind)) {
                            caption.kind(CaptionType.CAPTIONS);
                        }

                        captionList.add(caption.build());
                    }
                }
            }
        }
        return captionList;
    }


    private static List<AdBreak> getAdSchedule(Playable playable, Map pluginConfiguration) {
        List<AdBreak> adSchedule = new ArrayList<>();

        if (playable instanceof APAtomEntry.APAtomEntryPlayable) {
            List<LinkedHashMap<String, String>> advertisingList = ((APAtomEntry.APAtomEntryPlayable) playable).getEntry().getExtension("video_ads", ArrayList.class);
            adSchedule = getJWAdScheduler(advertisingList);
        }

        if (adSchedule.size() == 0 && pluginConfiguration != null) {
            adSchedule = getPluginConfigurationAdScheduler(playable, pluginConfiguration);
        }

        if (adSchedule.size() == 0) {
            adSchedule = getApplicasterAdScheduler(playable);
        }

        return adSchedule;
    }


    private static List<AdBreak> getJWAdScheduler(List<LinkedHashMap<String, String>> advertisingList) {
        List<AdBreak> result = new ArrayList<>();

        if (advertisingList != null) {
            for (int i = 0; i < advertisingList.size(); i++) {
                LinkedHashMap<String, String> advertisingModel = advertisingList.get(i);
                AdBreak adBreak = new AdBreak(String.valueOf(advertisingModel.get("offset")), AdSource.VAST, advertisingModel.get("ad_url"));
                result.add(adBreak);
            }
        }

        return result;

    }

    private static List<AdBreak> getPluginConfigurationAdScheduler(Playable playable, Map pluginConfiguration) {

        // Create your ad schedule
        List<AdBreak> adSchedule = new ArrayList<>();

        if (pluginConfiguration != null) {
            if (playable.isLive()) {
                String liveAdUrl = (String) pluginConfiguration.get("live_ad_url");
                String liveAdOffset = (String) pluginConfiguration.get("live_ad_offset");
                String liveAdType = (String) pluginConfiguration.get("live_ad_type");


                try {
                    AdSource liveAdSource = AdSource.valueByName(liveAdType);
                    AdBreak adBreak = new AdBreak(liveAdOffset, liveAdSource, liveAdUrl); // "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
                    adSchedule.add(adBreak);
                } catch (Exception e) {
                }

            } else {
                String vodPreAdUrl = (String) pluginConfiguration.get("vod_preroll_ad_url");
                String vodAdType = (String) pluginConfiguration.get("vod_ad_type");
                String vodMidAdUrl = (String) pluginConfiguration.get("vod_midroll_ad_url");
                String vodMidAdOffset = (String) pluginConfiguration.get("vod_midroll_offset");


                try {
                    AdSource vodAdSource = AdSource.valueByName(vodAdType);
                    if (StringUtil.isNotEmpty(vodPreAdUrl)) {
                        AdBreak adBreak = new AdBreak("pre", vodAdSource, vodPreAdUrl); // "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
                        adSchedule.add(adBreak);
                    }

                    if (StringUtil.isNotEmpty(vodMidAdUrl) && StringUtil.isNotEmpty(vodMidAdOffset)) {
                        AdBreak postrollAdBreak = new AdBreak(vodMidAdOffset, vodAdSource, vodMidAdUrl);
                        adSchedule.add(postrollAdBreak);
                    }
                } catch (Exception e) {
                }
            }
        }

        return adSchedule;
    }

    private static List<AdBreak> getApplicasterAdScheduler(Playable playable) {

        // Create your ad schedule
        List<AdBreak> adSchedule = new ArrayList<>();

        String imaPrerollAdUnit = VideoAdsUtil.getAccountPreroll(playable.isLive(), false);
        AdBreak adBreak = new AdBreak("pre", AdSource.IMA, imaPrerollAdUnit); // "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
        adSchedule.add(adBreak);

        String imaPostrollAdUnit = VideoAdsUtil.getAccountPostroll();
        AdBreak postrollAdBreak = new AdBreak("post", AdSource.IMA, imaPostrollAdUnit);
        adSchedule.add(postrollAdBreak);

        String imaMidrollAdUnit = VideoAdsUtil.getAccountMidroll();
        int breakInterval = getMidrollInterval(); //percentage
        if (breakInterval > 0) {
            for (int i = 1; breakInterval * i < 100; i++) {
                AdBreak midrollAdBreak = new AdBreak(breakInterval * i + "%", AdSource.IMA, imaMidrollAdUnit);
                adSchedule.add(midrollAdBreak);
            }
        }

        return adSchedule;
    }

    private static int getMidrollInterval() {
        int interval = 0;
        try {
            interval = (Integer.parseInt((String) AppData.getAPExtension(EXTERNAL_PLAYER_MIDROLL_INTERVAL)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return interval;
    }

    private static String getStreamImageUrl(APAtomEntry entry) {
        MediaItemIdentifier.Builder identifier = new MediaItemIdentifier.Builder();
        if (entry.getContent() != null && (entry.getContent().type).contains("video")) {
            identifier.setGroupType(APAtomEntry.MediaGroup.IMAGE_KEY)
                    .setKey("image_base")
                    .setForm(APAtomEntry.MediaItem.IMAGE_FORM_KEY)
                    .setScale(APAtomEntry.MediaItem.DEFAULT_LEGACY_SCALE);
        }
        return entry.getMediaUrl(identifier.build());
    }
}
