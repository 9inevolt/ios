package gg.destiny.app.fragments;

import gg.destiny.app.chat.R;
import gg.destiny.app.chat.App;
import gg.destiny.app.parsers.extm3uParser;
import gg.destiny.app.parsers.extm3u.*;
import gg.destiny.app.preference.QualityPreferenceChangeListener;
import gg.destiny.app.preference.QualityPreferenceHelper;
import gg.destiny.app.util.KrakenApi;
import gg.destiny.app.util.KrakenApi.ChannelAccessToken;
import gg.destiny.app.widget.*;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;
import gg.destiny.app.widget.FullMediaController.OnSettingsListener;

import java.io.IOException;
import java.util.*;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class PlayerFragment extends Fragment implements OnTouchListener,
        QualityPreferenceChangeListener, OnSettingsListener
{
    enum State {
        ERROR, IDLE, PREPARING, PREPARED, PLAYING, PAUSED, COMPLETE
    }
    public static final String TAG = "PlayerFragment";
    public static final String CHANNEL = "iateyourpie";

    private String preferredQuality;
//    private Uri playUri = null;
    private extm3u masterPlaylist;
    private Map<String, StreamInfo> qualityMap = new LinkedHashMap<String, StreamInfo>();
    private PlayerView playerView = null;
    private OnFullScreenListener onFullScreenListener;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        QualityPreferenceHelper helper = App.getQualityPreferenceHelper();
        preferredQuality = helper.getPreferenceValue();
        helper.addListener(this);
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
        playerView.setOnFullScreenListener(onFullScreenListener);
        playerView.setOnSettingsListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        new GetMasterPlaylistTask().execute(CHANNEL);
    }

    @Override
    public void onStop()
    {
        playerView.stop();
        super.onStop();
    }

    @Override
    public void onSettings(MediaPlayer mp)
    {
        App.getQualityPreferenceHelper().showDialog(getActivity(),
            new ArrayList<String>(qualityMap.keySet()));
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
        if (playerView.isInPlaybackState())
            playQuality(quality);
    }

    public void setOnFullScreenListener(OnFullScreenListener l)
    {
        onFullScreenListener = l;
        if (playerView != null)
            playerView.setOnFullScreenListener(l);
    }

    private void setMasterPlaylist(extm3u playlist)
    {
        if (playlist == null)
            return;

        masterPlaylist = playlist;
        qualityMap.clear();

        for (Media m : playlist.media) {
            qualityMap.put(m.name.toLowerCase(), m.streams.get(0));
            //new GetPlaylistTask().execute(m.name, m.streams.get(0).url);
        }

        for (StreamInfo s : playlist.streams) {
            Log.d(TAG, "other stream: " + s.video);
        }

        if (!playQuality(preferredQuality)) {
            Toast.makeText(getActivity(),
                "Preferred quality \"" + preferredQuality + "\" is not available. " +
                "Please select another quality.", Toast.LENGTH_SHORT).show();
        }
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

    private class GetMasterPlaylistTask extends AsyncTask<String, Void, extm3u>
    {
        @Override
        protected extm3u doInBackground(String... params)
        {
            try {
                ChannelAccessToken t = KrakenApi.getChannelAccessToken(params[0]);
//                Log.d(TAG, "sig: " + t.sig + ", token: " + t.token);
                String streams = KrakenApi.getStreams(t, params[0]);
                Log.d(TAG, "streams: " + streams);
//                String streams2 = KrakenApi.getStreams2(t, params[0]);
//                Log.d(TAG, "streams2: " + streams2);
                return extm3uParser.a(streams);

            } catch (Exception e) {
                Log.e(TAG, "Kraken error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(extm3u playlist)
        {
            if (playlist != null)
            {
                setMasterPlaylist(playlist);
            }
        }
    }

    private class GetPlaylistTask extends AsyncTask<String, Void, extm3u>
    {
        @Override
        protected extm3u doInBackground(String... params)
        {
            try {
                Log.d(TAG, "Load playlist: " + params[0] + "[" + params[1] + "]");
                String playlist = KrakenApi.getPlaylist(params[1]);
                Log.d(TAG, playlist);
                return extm3uParser.a(playlist);
            } catch (Exception e) {
                Log.e(TAG, "Kraken error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(extm3u playlist)
        {
        }
    }
}
