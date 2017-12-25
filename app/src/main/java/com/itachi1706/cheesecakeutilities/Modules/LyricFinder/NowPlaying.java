package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.graphics.Bitmap;

import javax.annotation.Nullable;

/**
 * Created by Kenneth on 25/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.LyricFinder in CheesecakeUtilities
 */

public class NowPlaying {

    static final int PLAY = 0;
    static final int PAUSE = 1;
    static final int STOP = 2;

    private String album, title, artist;
    private Bitmap albumart;
    private int state;

    public NowPlaying(){
        album = "Unknown Album";
        title = "Unknown Title";
        artist = "Unknown Artist";
        state = STOP;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        if (album == null) this.album = "Unknown Album";
        else this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) this.title = "Unknown Title";
        else this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        if (artist== null) this.artist = "Unknown Artist";
        else this.artist = artist;
    }

    public int getState() {
        return state;
    }

    public String getStateString() {
        switch (state) {
            case PLAY: return "Playing";
            case PAUSE: return "Paused";
            case STOP:
            default: return "Stopped/Unknown";
        }
    }

    public void setState(int state) {
        this.state = state;
    }

    @Nullable
    public Bitmap getAlbumart() {
        return albumart;
    }

    public void setAlbumart(Bitmap albumart) {
        this.albumart = albumart;
    }
}
