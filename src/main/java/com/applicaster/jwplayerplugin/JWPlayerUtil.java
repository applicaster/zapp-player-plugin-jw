package com.applicaster.jwplayerplugin;

import com.applicaster.player.VideoAdsUtil;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.longtailvideo.jwplayer.media.ads.AdBreak;
import com.longtailvideo.jwplayer.media.ads.AdSource;
import com.longtailvideo.jwplayer.media.ads.ImaAdvertising;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public class JWPlayerUtil {

    public static PlaylistItem getPlaylistItem(Playable playable){
        PlaylistItem result=null;

        // Create your ad schedule
        List<AdBreak> adSchedule = new ArrayList<>();

        String imaAdUnit = VideoAdsUtil.getAccountPreroll(playable.isLive(), false);
        AdBreak adBreak = new AdBreak("pre", AdSource.IMA, imaAdUnit); // "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");

        adSchedule.add(adBreak);

        if (playable !=null) {
            // Load a media source
            result = new PlaylistItem.Builder()
                    .file(playable.getContentVideoURL())
                    .title(playable.getPlayableName())
                    .description(playable.getPlayableDescription())
                    .adSchedule(adSchedule)
                    .build();
        }

        return result;

    }
}
