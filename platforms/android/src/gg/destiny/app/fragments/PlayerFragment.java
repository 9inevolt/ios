package gg.destiny.app.fragments;

import gg.destiny.app.chat.R;
import gg.destiny.app.chat.App;
import gg.destiny.app.model.Channel;
import gg.destiny.app.model.Stream;
import gg.destiny.app.parsers.extm3uParser;
import gg.destiny.app.parsers.extm3u.*;
import gg.destiny.app.preference.*;
import gg.destiny.app.util.*;
import gg.destiny.app.widget.*;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;
import gg.destiny.app.widget.FullMediaController.OnSettingsListener;

import java.io.IOException;
import java.util.*;

import org.json.JSONObject;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.*;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.ImageView.ScaleType;

public class PlayerFragment extends Fragment implements OnTouchListener,
        QualityPreferenceChangeListener, OnSettingsListener, Callback, OnCompletionListener
{
    enum Status {
        UNKNOWN, OFFLINE, ONLINE
    }
    public static final String TAG = "PlayerFragment";
    private static long STATUS_DELAY = 15000;

    private String preferredChannel;
    private String preferredQuality;
//    private Uri playUri = null;
    private extm3u masterPlaylist;
    private Map<String, StreamInfo> qualityMap = new LinkedHashMap<String, StreamInfo>();
    private PlayerView playerView = null;
    private ImageView offlineImageView = null;
    private OnFullScreenListener onFullScreenListener;
    private Handler statusHandler;
    private boolean offlineImageLoaded;
    private Status status = Status.UNKNOWN;
    private Runnable getStreamRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        QualityPreferenceHelper qualityHelper = App.getQualityPreferenceHelper();
        preferredQuality = qualityHelper.getPreferenceValue();
        qualityHelper.addListener(this);

        ChannelPreferenceHelper channelHelper = App.getChannelPreferenceHelper();
        preferredChannel = channelHelper.getPreferenceValue();

        statusHandler = new Handler(this);
        initStreamRunnable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        playerView = (PlayerView) view.findViewById(R.id.player_view);
        offlineImageView = (ImageView) view.findViewById(R.id.player_offline_image);
        playerView.setOnFullScreenListener(onFullScreenListener);
        playerView.setOnSettingsListener(this);
        playerView.setOnCompletionListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (status == Status.ONLINE) {
            try {
                playerView.resume();
            } catch (IOException e) {
                Log.e(TAG, "error on resume", e);
                offline();
            }
        } else {
            statusHandler.post(getStreamRunnable);
        }
    }

    @Override
    public void onStop()
    {
        playerView.suspend();
        statusHandler.removeCallbacks(getStreamRunnable);
        super.onStop();
    }

    @Override
    public void onSettings(MediaPlayer mp)
    {
        App.getQualityPreferenceHelper().showDialog(getActivity(),
            new ArrayList<String>(qualityMap.keySet()));
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        offline();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        return false;
    }

    @Override
    public void onQualityPreferenceChanged(String quality)
    {
        preferredQuality = quality;
        playQuality(quality);
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        return false;
    }

    public void setOnFullScreenListener(OnFullScreenListener l)
    {
        onFullScreenListener = l;
        if (playerView != null)
            playerView.setOnFullScreenListener(l);
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
            //new GetPlaylistTask().execute(m.name, m.streams.get(0).url);
        }

        for (StreamInfo s : playlist.streams) {
            Log.d(TAG, "other stream: " + s.video);
        }

        online();
    }

    private boolean playQuality(String quality) {
        if (!qualityMap.containsKey(quality))
            return false;

        try {
            play(qualityMap.get(quality).url);
        } catch (IOException e) {
            Log.e(TAG, "Playback error", e);
        }
        return true;
    }

    private void play(String url) throws IOException
    {
        playerView.setVideoURI(url);
        playerView.start();
    }

    private void tryOnline()
    {
        if (status == Status.ONLINE)
            return;

        new GetMasterPlaylistTask().execute(preferredChannel);
    }

    private void online()
    {
        if (status == Status.ONLINE && masterPlaylist != null) {
            Log.d(TAG, preferredChannel + " still online");
            return;
        }

        if (!playQuality(preferredQuality)) {
            Toast.makeText(getActivity(),
                "Preferred quality \"" + preferredQuality + "\" is not available. " +
                "Please select another quality.", Toast.LENGTH_SHORT).show();
        }

        status = Status.ONLINE;
        playerView.setVisibility(View.VISIBLE);
        offlineImageView.setVisibility(View.GONE);
        Log.d(TAG, preferredChannel + " went online");
    }

    private void offline()
    {
        statusHandler.postDelayed(getStreamRunnable, STATUS_DELAY);

        if (status == Status.OFFLINE) {
            Log.d(TAG, preferredChannel + " still offline");
            return;
        }

        status = Status.OFFLINE;
        masterPlaylist = null;
        if (!offlineImageLoaded) {
            offlineImageView.setImageResource(R.drawable.offline_default);
            offlineImageView.setScaleType(ScaleType.CENTER_CROP);
            new GetChannelTask().execute(preferredChannel);
        }
        playerView.setVisibility(View.GONE);
        offlineImageView.setVisibility(View.VISIBLE);
        Log.d(TAG, preferredChannel + " went offline");
    }

    private void setOfflineImage(Drawable d)
    {
        offlineImageView.setImageDrawable(d);
        offlineImageView.setScaleType(ScaleType.CENTER_INSIDE);
        offlineImageLoaded = true;
    }

    private class GetChannelTask extends AsyncTask<String, Void, Channel>
    {
        @Override
        protected Channel doInBackground(String... params)
        {
            Log.d(TAG, "GetChannelTask execute");
            try {
                JSONObject obj = KrakenApi.getChannel(params[0]);
                if (obj != null) {
                    return new Channel(obj);
                }
            } catch (Exception e) {
                Log.e(TAG, "Kraken error", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Channel channel)
        {
            if (playerView.isInPlaybackState()) {
                playerView.stop();
            }

            if (channel != null && channel.getVideoBanner() != null) {
                new DownloadImageTask() {
                    @Override
                    protected void onPostExecute(Bitmap bm) {
                        setOfflineImage(new BitmapDrawable(getResources(), bm));
                    }
                }.execute(channel.getVideoBanner());
            }
        }
    }

    private void initStreamRunnable() {
        if (getStreamRunnable != null) {
            return;
        }

        getStreamRunnable = new Runnable() {
            @Override
            public void run()
            {
                new GetStreamTask() {
                    @Override
                    protected void onPostExecute(Stream stream)
                    {
                        if (stream != null) {
                            tryOnline();
                        } else {
                            offline();
                        }
                    }
                }.execute(preferredChannel);
            }
        };
    }

    private void releaseStreamRunnable() {
        if (statusHandler != null && getStreamRunnable != null) {
            statusHandler.removeCallbacks(getStreamRunnable);
            getStreamRunnable = null;
        }
    }

    private class GetMasterPlaylistTask extends AsyncTask<String, Void, extm3u>
    {
        @Override
        protected extm3u doInBackground(String... params)
        {
            try {
                String playlist = KrakenApi.getPlaylist(params[0]);
                Log.d(TAG, "playlist: " + playlist);
//                String playlist2 = KrakenApi.getPlaylist2(params[0]);
//                Log.d(TAG, "playlist2: " + playlist2);
                return extm3uParser.a(playlist);

            } catch (Exception e) {
                Log.e(TAG, "Kraken error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(extm3u playlist)
        {
            setMasterPlaylist(playlist);
        }
    }
}
