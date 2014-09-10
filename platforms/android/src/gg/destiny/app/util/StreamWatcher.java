package gg.destiny.app.util;

import gg.destiny.app.model.Channel;
import gg.destiny.app.model.Stream;
import gg.destiny.app.parsers.extm3uParser;
import gg.destiny.app.parsers.extm3u.*;

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
    private static final long LONG_STATUS_DELAY = 5 * 60 * 1000;

    private final Channel channel;
    private ScheduledExecutorService executor;
    private final boolean constantChecking;
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

    public StreamWatcher(Channel watchChannel)
    {
        this(watchChannel, false);
    }

    public StreamWatcher(Channel watchChannel, boolean onlyChecking)
    {
        channel = watchChannel;
        executor = Executors.newSingleThreadScheduledExecutor();
        handler = new StreamEventHandler(new DummyListener());
        constantChecking = onlyChecking;
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

    public void forceOffline(boolean connected)
    {
        stop();

        if (connected) {
            start(handler);
        }

        status = Status.UNKNOWN;
        offline(connected);
    }

    public boolean isOnline()
    {
        return status == Status.ONLINE;
    }

    private void offline()
    {
        offline(true);
    }

    private void offline(boolean connected)
    {
        if (connected) {
            executor.schedule(getStreamRunnable,
                    constantChecking ? LONG_STATUS_DELAY : STATUS_DELAY,
                    TimeUnit.MILLISECONDS);
        }

        if (status == Status.OFFLINE) {
            Log.d(TAG, channel + " still offline");
            return;
        }

        status = Status.OFFLINE;
        masterPlaylist = null;

        if (connected && !offlineImageLoaded) {
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
        if (status == Status.ONLINE && !constantChecking)
            return;

        if (constantChecking) {
            if (status != Status.ONLINE) {
                status = Status.ONLINE;
                handler.online();
            } else {
                Log.d(TAG, channel + " still online");
            }
            executor.schedule(getStreamRunnable, LONG_STATUS_DELAY, TimeUnit.MILLISECONDS);
        } else {
            executor.submit(getMasterPlaylistRunnable);
        }
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
            qualityMap.put(m.name.toLowerCase(Locale.ENGLISH), m.streams.get(0));
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
                    JSONObject obj = KrakenApi.getStream(channel.getName());
                    if (obj != null && obj.optJSONObject("stream") != null) {
                        stream = new Stream(obj.getJSONObject("stream"));
                        Channel c = stream.getChannel();
                        if (c != null) {
                            handler.channel(c);
                        }
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
                    String sPlaylist = KrakenApi.getPlaylist(channel.getName());
//                    Log.d(TAG, "playlist: " + sPlaylist);
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
                    JSONObject obj = KrakenApi.getChannel(channel.getName());
                    if (obj != null) {
                        videoChannel = new Channel(obj);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Kraken error", e);
                }

                if (videoChannel == null) {
                    return;
                }

                handler.channel(videoChannel);

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

    private static class DummyListener implements StreamEventListener {
        @Override
        public void online()
        {
            // no-op
        }

        @Override
        public void offline()
        {
            // no-op
        }

        @Override
        public void offlineImage(Bitmap bm)
        {
            // no-op
        }

        @Override
        public void channel(Channel channel)
        {
            // no-op
        }
    }
}
