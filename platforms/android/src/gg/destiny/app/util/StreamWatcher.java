package gg.destiny.app.util;

import gg.destiny.app.model.Channel;
import gg.destiny.app.model.Stream;
import gg.destiny.app.parsers.extm3uParser;
import gg.destiny.app.parsers.extm3u.*;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class StreamWatcher
{
    enum Status {
        UNKNOWN, OFFLINE, ONLINE
    }
    public static final String TAG = "StreamWatcher";
    private static final long STATUS_DELAY = 15000;

    private final String channel;
    private ScheduledExecutorService executor;
    private Status status = Status.UNKNOWN;
    private StreamEventHandler handler;
    private extm3u masterPlaylist;
    private Map<String, StreamInfo> qualityMap = new LinkedHashMap<String, StreamInfo>();
    private boolean offlineImageLoaded;
    private Bitmap offlineImage;
    private Runnable getStreamRunnable;
    private Future getStreamFuture;
    private Runnable getMasterPlaylistRunnable;
    private Future getMasterPlaylistFuture;
    private Runnable getChannelRunnable;
    private Future getChannelFuture;

    public StreamWatcher(String watchChannel)
    {
        channel = watchChannel;
        initStreamRunnable();
        initGetMasterPlaylistRunnable();
        initGetChannelRunnable();
    }

    public void start(StreamEventListener l)
    {
        start(new StreamEventHandler(l));

        if (!isOnline()) {
            getStreamFuture = executor.submit(getStreamRunnable);
        }
    }

    private void start(StreamEventHandler h)
    {
        if (executor != null && !executor.isShutdown()) {
            stop();
        }

        handler = h;

        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void stop()
    {
        if (executor != null) {
            executor.shutdownNow();
        }

        handler.clear();
    }

    public List<String> getQualities()
    {
        return new ArrayList<String>(qualityMap.keySet());
    }

    public StreamInfo getQuality(String quality)
    {
        return qualityMap.get(quality);
    }

    public Bitmap getOfflineImage()
    {
        return offlineImage;
    }

    public void forceOffline()
    {
        stop();
        start(handler);

        status = Status.UNKNOWN;
        offline();
    }

    public boolean isOnline()
    {
        return status == Status.ONLINE;
    }

    private void offline()
    {
        executor.schedule(getStreamRunnable, STATUS_DELAY, TimeUnit.MILLISECONDS);

        if (status == Status.OFFLINE) {
            Log.d(TAG, channel + " still offline");
            return;
        }

        status = Status.OFFLINE;
        masterPlaylist = null;

        if (!offlineImageLoaded) {
            getChannelFuture = executor.submit(getChannelRunnable);
        }

        handler.offline();
    }

    private void online()
    {
        if (status == Status.ONLINE && masterPlaylist != null) {
            Log.d(TAG, channel + " still online");
        } else {
            status = Status.ONLINE;
            handler.online();
        }
    }

    private void tryOnline()
    {
        if (status == Status.ONLINE)
            return;

        executor.submit(getMasterPlaylistRunnable);
    }

    private void setMasterPlaylist(extm3u playlist)
    {
        if (playlist == null || playlist.media.isEmpty()) {
            offline();
            return;
        }

        masterPlaylist = playlist;
        qualityMap.clear();

        for (Media m : playlist.media) {
            qualityMap.put(m.name.toLowerCase(), m.streams.get(0));
        }

        for (StreamInfo s : playlist.streams) {
            Log.d(TAG, "other stream: " + s.video);
        }

        online();
    }

    private void initStreamRunnable() {
        if (getStreamRunnable != null) {
            return;
        }

        getStreamRunnable = new Runnable() {
            @Override
            public void run()
            {
                Stream stream = null;
                try {
                    JSONObject obj = KrakenApi.getStream(channel);
                    if (obj != null && obj.optJSONObject("stream") != null) {
                        stream = new Stream(obj);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Kraken error", e);
                }

                if (stream != null) {
                    tryOnline();
                } else {
                    offline();
                }
            }
        };
    }

    private void initGetMasterPlaylistRunnable() {
        if (getMasterPlaylistRunnable != null) {
            return;
        }

        getMasterPlaylistRunnable = new Runnable() {
            @Override
            public void run()
            {
                extm3u playlist = null;
                try {
                    String sPlaylist = KrakenApi.getPlaylist(channel);
                    Log.d(TAG, "playlist: " + sPlaylist);
                    playlist = extm3uParser.a(sPlaylist);

                } catch (Exception e) {
                    Log.e(TAG, "Kraken error", e);
                }

                setMasterPlaylist(playlist);
            }
        };
    }

    private void initGetChannelRunnable() {
        if (getChannelRunnable != null) {
            return;
        }

        getChannelRunnable = new Runnable() {
            @Override
            public void run()
            {
                Channel videoChannel = null;
                try {
                    JSONObject obj = KrakenApi.getChannel(channel);
                    if (obj != null) {
                        videoChannel = new Channel(obj);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Kraken error", e);
                }

                // TODO ?
    //            if (playerView.isInPlaybackState()) {
    //                playerView.stop();
    //            }

                if (videoChannel == null) {
                    return;
                }

                if (videoChannel.getVideoBanner() == null) {
                    offlineImageLoaded = true;
                }

                if (videoChannel.getVideoBanner() != null) {
                    Bitmap bm = null;
                    try
                    {
                        URLConnection conn = new URL(videoChannel.getVideoBanner()).openConnection();
                        conn.setConnectTimeout(KrakenApi.CONNECT_TIMEOUT);
                        conn.setReadTimeout(KrakenApi.READ_TIMEOUT);
                        bm = BitmapFactory.decodeStream(conn.getInputStream());
                        handler.offlineImage(bm);
                        offlineImageLoaded = true;
                    } catch (Exception e)
                    {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        };
    }
}
