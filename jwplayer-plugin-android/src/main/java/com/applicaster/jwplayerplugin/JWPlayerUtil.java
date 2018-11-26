package com.applicaster.jwplayerplugin;

import com.applicaster.plugin_manager.playersmanager.Playable;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public class JWPlayerUtil {

    public static PlaylistItem getPlaylistItem(Playable playable){
        PlaylistItem result=null;

        if (playable !=null) {
            // Load a media source
            result = new PlaylistItem.Builder()
                    .file(playable.getContentVideoURL())
                    .title(playable.getPlayableName())
                    .description(playable.getPlayableDescription())
                    .build();
        }

        return result;

    }
}
