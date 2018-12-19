package com.applicaster.jwplayerplugin;

import com.applicaster.player.VideoAdsUtil;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.applicaster.util.AppData;
import com.longtailvideo.jwplayer.media.ads.AdBreak;
import com.longtailvideo.jwplayer.media.ads.AdSource;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;

import static com.applicaster.player.VideoAdsUtil.MIDROLL_INTERVAL;

@CheckReturnValue
public class JWPlayerUtil {
    final static String EXTERNAL_PLAYER_MIDROLL_INTERVAL="midroll_interval_external_player";

    public static PlaylistItem getPlaylistItem(Playable playable){
        PlaylistItem result=null;


        if (playable !=null) {
            // Load a media source
            result = new PlaylistItem.Builder()
                    .file(playable.getContentVideoURL())
                    .title(playable.getPlayableName())
                    .description(playable.getPlayableDescription())
                    .adSchedule(getAdSchedule(playable))
                    .build();
        }

        return result;

    }

    private static List<AdBreak> getAdSchedule(Playable playable){
        // Create your ad schedule
        List<AdBreak> adSchedule = new ArrayList<>();

        String imaPrerollAdUnit = VideoAdsUtil.getAccountPreroll(playable.isLive(), false);
        AdBreak adBreak = new AdBreak("pre", AdSource.IMA, imaPrerollAdUnit); // "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
        adSchedule.add(adBreak);

        String imaPostrollAdUnit = VideoAdsUtil.getAccountPostroll();
        AdBreak postrollAdBreak = new AdBreak("post", AdSource.IMA, imaPostrollAdUnit );
        adSchedule.add(postrollAdBreak);

        String imaMidrollAdUnit = VideoAdsUtil.getAccountMidroll();
        int breakInterval = getMidrollInterval(); //percentage
        if (breakInterval> 0) {
            for (int i=1; breakInterval*i<100; i++ ){
                AdBreak midrollAdBreak = new AdBreak(breakInterval*i + "%", AdSource.IMA, imaMidrollAdUnit);
                adSchedule.add(midrollAdBreak);
            }
        }

        return adSchedule;
    }

    private static int getMidrollInterval(){
        int interval  = 0;
        String extensionValue = (String) AppData.getAPExtension(EXTERNAL_PLAYER_MIDROLL_INTERVAL);
        if(extensionValue != null) {
            try {
                interval = (Integer.parseInt((String) AppData.getAPExtension(MIDROLL_INTERVAL)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return interval;
    }
}
