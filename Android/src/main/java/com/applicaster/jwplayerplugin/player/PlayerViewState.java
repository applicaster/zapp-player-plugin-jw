package com.applicaster.jwplayerplugin.player;

public enum PlayerViewState {
    INLINE,
    FULLSCREEN;

    public static PlayerViewState fromOrdinal(int index){
        return PlayerViewState.values()[index];
    }
}