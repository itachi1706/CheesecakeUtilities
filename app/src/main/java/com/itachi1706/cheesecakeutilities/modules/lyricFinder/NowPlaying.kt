package com.itachi1706.cheesecakeutilities.modules.lyricFinder

import android.content.Intent
import android.graphics.Bitmap

/**
 * Created by Kenneth on 25/12/2017.
 * for com.itachi1706.cheesecakeutilities.modules.LyricFinder in CheesecakeUtilities
 */

class NowPlaying {

    private var album: String? = null
    private var title: String? = null
    private var artist: String? = null
    var albumart: Bitmap? = null
    var state: Int = 0

    val stateString: String
        get() {
            return when (state) {
                PLAY -> "Playing"
                PAUSE -> "Paused"
                STOP -> "Stopped/Unknown"
                else -> "Stopped/Unknown"
            }
        }

    constructor() {
        album = "Unknown Album"
        title = "Unknown Title"
        artist = "Unknown Artist"
        state = STOP
    }

    constructor(intent: Intent) {
        album = intent.getStringExtra(LYRIC_ALBUM)
        artist = intent.getStringExtra(LYRIC_ARTIST)
        title = intent.getStringExtra(LYRIC_TITLE)
        state = intent.getIntExtra(LYRIC_STATE, STOP)
    }

    fun generateIntent(): Intent {
        val intent = Intent(LYRIC_DATA)
        intent.putExtra(LYRIC_ALBUM, album)
        intent.putExtra(LYRIC_ARTIST, artist)
        intent.putExtra(LYRIC_TITLE, title)
        intent.putExtra(LYRIC_STATE, state)
        return intent
    }

    fun getAlbum(): String? {
        return album
    }

    fun setAlbum(album: String?) {
        if (album == null)
            this.album = "Unknown Album"
        else
            this.album = album
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        if (title == null)
            this.title = "Unknown Title"
        else
            this.title = title
    }

    fun getArtist(): String? {
        return artist
    }

    fun setArtist(artist: String?) {
        if (artist == null)
            this.artist = "Unknown Artist"
        else
            this.artist = artist
    }

    companion object {

        val PLAY = 0
        val PAUSE = 1
        val STOP = 2

        val LYRIC_TITLE = "title"
        val LYRIC_ARTIST = "artist"
        val LYRIC_ALBUM = "album"
        val LYRIC_STATE = "state"
        val LYRIC_ALBUMART = "album_art"

        val LYRIC_UPDATE = "com.itachi1706.cheesecakeutilities.LYRIC_UPDATE_BROADCAST"
        val LYRIC_DATA = "com.itachi1706.cheesecakeutilities.LYRIC_DATA_BROADCAST"
    }
}
