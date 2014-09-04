package gg.destiny.app.fragments;

import gg.destiny.app.chat.App;
import gg.destiny.app.chat.R;
import gg.destiny.app.parsers.extm3u.StreamInfo;
import gg.destiny.app.preference.*;
import gg.destiny.app.util.StreamEventListener;
import gg.destiny.app.util.StreamWatcher;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;
import gg.destiny.app.widget.FullMediaController.OnSettingsListener;
import gg.destiny.app.widget.*;

import java.io.IOException;

import android.annotation.TargetApi;
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

@TargetApi(14)
public class PlayerFragment extends Fragment implements OnTouchListener,
        QualityPreferenceChangeListener, OnSettingsListener, Callback, OnCompletionListener,
        StreamEventListener
{
    public static final String TAG = "PlayerFragment";

    private String preferredChannel;
    private String preferredQuality;
    private PlayerView playerView = null;
    private ImageView offlineImageView = null;
    private OnFullScreenListener onFullScreenListener;
    private StreamWatcher watcher;

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

        watcher = new StreamWatcher(preferredChannel);
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
        watcher.start(this);

        if (watcher.isOnline()) {
            try {
                playerView.resume();
            } catch (IOException e) {
                Log.e(TAG, "error on resume", e);
                watcher.forceOffline();
            }
        }
    }

    @Override
    public void onStop()
    {
        playerView.suspend();
        watcher.stop();
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        App.getQualityPreferenceHelper().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSettings(MediaPlayer mp)
    {
        App.getQualityPreferenceHelper()
            .showDialog(getActivity(), watcher.getQualities());
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        watcher.forceOffline();
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

    private boolean playQuality(String quality) {
        StreamInfo info = watcher.getQuality(quality);
        if (info == null)
            return false;

        try {
            play(info.url);
        } catch (IOException e) {
            Log.e(TAG, "Playback error", e);
        }
        return true;
    }

    private void play(String url) throws IOException
    {
        playerView.start();
        playerView.setVideoURI(url);
    }

    @Override
    public void online()
    {
        if (!playQuality(preferredQuality)) {
            Toast.makeText(getActivity(),
                "Preferred quality \"" + preferredQuality + "\" is not available. " +
                "Please select another quality.", Toast.LENGTH_SHORT).show();
        }

        playerView.setVisibility(View.VISIBLE);
        offlineImageView.setVisibility(View.GONE);
    }

    @Override
    public void offline()
    {
        if (watcher.getOfflineImage() == null) {
            offlineImageView.setImageResource(R.drawable.offline_default);
            offlineImageView.setScaleType(ScaleType.CENTER_CROP);
        } else {
            offlineImage(watcher.getOfflineImage());
        }

        playerView.setVisibility(View.GONE);
        offlineImageView.setVisibility(View.VISIBLE);
        Log.d(TAG, preferredChannel + " went offline");
    }

    @Override
    public void offlineImage(Bitmap bm)
    {
        Drawable d = new BitmapDrawable(getResources(), bm);
        offlineImageView.setImageDrawable(d);
        offlineImageView.setScaleType(ScaleType.CENTER_INSIDE);
    }
}
