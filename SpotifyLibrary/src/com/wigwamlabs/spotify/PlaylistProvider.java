package com.wigwamlabs.spotify;

import java.util.LinkedHashMap;

public class PlaylistProvider {
    private static final int MAX_SIZE = 25;
    private final Session mSession;
    private final LinkedHashMap<String, Playlist> mPlaylists;

    public PlaylistProvider(Session session) {
        mSession = session;

        mPlaylists = new LinkedHashMap<String, Playlist>(MAX_SIZE + 1, 0.75f, true) {
            @Override
            public Playlist remove(Object key) {
                final Playlist value = super.remove(key);
                if (value != null) {
                    value.destroy();
                }
                return value;
            }

            @Override
            public void clear() {
                for (Playlist value : values()) {
                    if (value != null) {
                        value.destroy();
                    }
                }
                super.clear();
            }

            @Override
            protected boolean removeEldestEntry(Entry<String, Playlist> eldest) {
                return size() > MAX_SIZE;
            }
        };
    }

    public void destroy() {
        mPlaylists.clear();
    }

    public Playlist getPlaylist(String link) {
        if (link == null || link.length() == 0) {
            return null;
        }
        Playlist playlist = mPlaylists.get(link);
        if (playlist == null) {
            playlist = Playlist.create(mSession, link);
            if (playlist != null) {
                mPlaylists.put(link, playlist);
            }
        }
        return playlist;
    }

    public Session getSession() {
        return mSession;
    }
}
