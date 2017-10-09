package com.retro.musicplayer.backend;

/**
 * @author Hemanth S (h4h13).
 */

public class RetroConstants {
    public static final String RETRO_MUSIC_PACKAGE_NAME = "code.name.monkey.retromusic";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String ACTION_TOGGLE_PAUSE = RETRO_MUSIC_PACKAGE_NAME + ".togglepause";
    public static final String ACTION_PLAY = RETRO_MUSIC_PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = RETRO_MUSIC_PACKAGE_NAME + ".play.playlist";
    public static final String ACTION_PAUSE = RETRO_MUSIC_PACKAGE_NAME + ".pause";
    public static final String ACTION_STOP = RETRO_MUSIC_PACKAGE_NAME + ".stop";
    public static final String ACTION_SKIP = RETRO_MUSIC_PACKAGE_NAME + ".skip";
    public static final String ACTION_REWIND = RETRO_MUSIC_PACKAGE_NAME + ".rewind";
    public static final String ACTION_QUIT = RETRO_MUSIC_PACKAGE_NAME + ".quitservice";
    public static final String INTENT_EXTRA_PLAYLIST = RETRO_MUSIC_PACKAGE_NAME + "intentextra.playlist";
    public static final String INTENT_EXTRA_SHUFFLE_MODE = RETRO_MUSIC_PACKAGE_NAME + ".intentextra.shufflemode";

    public static final String APP_WIDGET_UPDATE = RETRO_MUSIC_PACKAGE_NAME + ".appwidgetupdate";
    public static final String EXTRA_APP_WIDGET_NAME = RETRO_MUSIC_PACKAGE_NAME + "app_widget_name";

    // do not change these three strings as it will break support with other apps (e.g. last.fm scrobbling)
    public static final String META_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".metachanged";
    public static final String QUEUE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".queuechanged";
    public static final String PLAY_STATE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".playstatechanged";

    public static final String REPEAT_MODE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".mediastorechanged";
}
