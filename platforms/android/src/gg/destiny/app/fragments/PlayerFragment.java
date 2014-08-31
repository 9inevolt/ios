package gg.destiny.app.fragments;

import gg.destiny.app.chat.R;
import gg.destiny.app.chat.App;
import gg.destiny.app.parsers.extm3uParser;
import gg.destiny.app.parsers.extm3u.*;
import gg.destiny.app.preference.QualityPreferenceChangeListener;
import gg.destiny.app.preference.QualityPreferenceHelper;
import gg.destiny.app.util.KrakenApi;
import gg.destiny.app.util.KrakenApi.ChannelAccessToken;
import gg.destiny.app.widget.FullMediaController;
import gg.destiny.app.widget.FullMediaPlayerControl;

import java.io.IOException;
import java.util.*;

import android.content.pm.ActivityInfo;
import android.media.*;
import android.media.MediaPlayer.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PlayerFragment extends Fragment implements Callback, OnErrorListener, OnPreparedListener,
        OnVideoSizeChangedListener, OnCompletionListener, FullMediaPlayerControl, OnTouchListener,
        QualityPreferenceChangeListener
{
    enum State {
        ERROR, IDLE, PREPARING, PREPARED, PLAYING, PAUSED, COMPLETE
    }
    public static final String TAG = "PlayerFragment";
    public static final String CHANNEL = "destiny";

    private MediaPlayer player = null;
    private ViewGroup container = null;
    private SurfaceView surface = null;
    private SurfaceHolder holder = null;
    private FullMediaController controls = null;
    private String preferredQuality;
    private Uri playUri = null;
    private boolean fullScreen = false;
    private boolean orientationLocked = false;
    private State state = State.IDLE;
    private extm3u masterPlaylist;
    private Map<String, StreamInfo> qualityMap = new LinkedHashMap<String, StreamInfo>();

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
        container = (ViewGroup) view.findViewById(R.id.player_container);
        surface = (SurfaceView) view.findViewById(R.id.player_surface);
        surface.getHolder().addCallback(this);
        surface.setOnTouchListener(this);
        container.post(new Runnable() {
            @Override
            public void run()
            {
                int height = (int) (container.getWidth() * 9f / 16);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
                container.setLayoutParams(params);
            }
        });
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
        releasePlayer();
        super.onStop();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        this.holder = null;
        releasePlayer();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        this.holder = holder;
        try {
            play();
        } catch (IOException e) {
            Log.e(TAG, "Playback error", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.e(TAG, "what: " + what + ", extra: " + extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        state = State.PREPARED;
        if (controls != null)
            controls.setEnabled(true);
        player.start();
        state = State.PLAYING;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int mWidth, int mHeight)
    {
//        int mHeight = mp.getVideoHeight();
//        int mWidth = mp.getVideoWidth();
//        if (mHeight > 0 && mWidth > 0) {
//            surface.getHolder().setFixedSize(mWidth, mHeight);
//            surface.requestLayout();
//            surface.invalidate();
//        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        state = State.COMPLETE;
    }

    @Override
    public void start()
    {
        if (isInPlaybackState()) {
            player.start();
            state = State.PLAYING;
        }
//        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause()
    {
        if (isPlaying()) {
            player.pause();
            state = State.PAUSED;
        }
//        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getDuration()
    {
        if (isInPlaybackState()) {
            return player.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition()
    {
        if (isInPlaybackState()) {
            return player.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public void seekTo(int pos)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPlaying()
    {
        return isInPlaybackState() && player.isPlaying();
    }

    @Override
    public int getBufferPercentage()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward()
    {
        return false;
    }

    @Override
    public boolean canSeekForward()
    {
        return false;
    }

    @Override
    public int getAudioSessionId()
    {
        return 0;
    }

    @Override
    public boolean isFullScreen()
    {
        return fullScreen;
    }

    @Override
    public void fullScreen(boolean full)
    {
        doFullScreen(full, true);
    }

    @Override
    public void doSettings()
    {
        App.getQualityPreferenceHelper().showDialog(getActivity(),
            new ArrayList<String>(qualityMap.keySet()));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (isInPlaybackState() && controls != null) {
            toggleControlsVisibility();
        }
        return false;
    }

    public boolean isInPlaybackState()
    {
        return player != null &&
            state != State.ERROR &&
            state != State.IDLE &&
            state != State.PREPARING;
    }

    // TODO: Some way to keep this private
    public void doFullScreen(boolean full, boolean userInitiated)
    {
        if (!userInitiated) {
            if (orientationLocked)
                return;
            else
              getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if (full) {
            fullScreen = true;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            container.setLayoutParams(params);
            if (userInitiated) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                orientationLocked = true;
            }
        } else {
            fullScreen = false;
            // TODO: Refactor with onViewCreated
            if (userInitiated) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                orientationLocked = false;
            }
            container.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    int height = (int) (container.getWidth() * 9f / 16);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
                    container.setLayoutParams(params);
                }
            }, 500);
        }
    }

    @Override
    public void onQualityPreferenceChanged(String quality)
    {
        preferredQuality = quality;
        if (isInPlaybackState())
            playQuality(quality);
    }

    private void toggleControlsVisibility() {
        if (controls.isShowing()) {
            controls.hide();
        } else {
            controls.show();
        }
    }

    private void initPlayer()
    {
        if (player == null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnErrorListener(this);
            player.setOnVideoSizeChangedListener(this);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        if (controls == null) {
            controls = new FullMediaController(getActivity());
            controls.setMediaPlayer(this);
        }
    }

    private void releasePlayer()
    {
        if (player != null) {
            player.reset();
            player.release();
            player = null;
            state = State.IDLE;
        }
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
        playUri = Uri.parse(url);
        play();
    }

    private void play() throws IOException
    {
        if (playUri == null || holder == null)
            return;

        Log.d(TAG, "play: " + playUri);
        releasePlayer();
        initPlayer();
        player.setDataSource(getActivity(), playUri);
        player.setDisplay(holder);
        player.setScreenOnWhilePlaying(true);
        player.prepareAsync();
        state = State.PREPARING;
        controls.setAnchorView(container);
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
