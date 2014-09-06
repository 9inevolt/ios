package gg.destiny.app.fragments;

import gg.destiny.app.chat.App;
import gg.destiny.app.chat.R;
import gg.destiny.app.model.Channel;
import gg.destiny.app.parsers.extm3u.StreamInfo;
import gg.destiny.app.preference.*;
import gg.destiny.app.util.*;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;
import gg.destiny.app.widget.FullMediaController.OnSettingsListener;
import gg.destiny.app.widget.*;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.ImageView.ScaleType;

@TargetApi(14)
public class PlayerFragment extends Fragment implements OnTouchListener,
        QualityPreferenceChangeListener, OnSettingsListener, OnCompletionListener,
        StreamEventListener, OnErrorListener, NetworkListener
{
    public static final String TAG = "PlayerFragment";
    private static final long DIALOG_DELAY = 1000;

    private Channel preferredChannel;
    private String preferredQuality;
    private PlayerView playerView = null;
    private ImageView offlineImageView = null;
    private TextView offlineTextView = null;
    private OnFullScreenListener onFullScreenListener;
    private StreamWatcher watcher;
    private boolean isConnected;
    private Handler handler;

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
        handler = new Handler();
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
        offlineTextView = (TextView) view.findViewById(R.id.player_offline_text);
        playerView.setOnFullScreenListener(onFullScreenListener);
        playerView.setOnSettingsListener(this);
        playerView.setOnCompletionListener(this);
        playerView.setOnErrorListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        App.getNetworkHelper().addListener(this);
        onConnectivityChanged(App.getNetworkHelper().isConnected());

        if (isConnected && watcher.isOnline()) {
            try {
                playerView.resume();
            } catch (IOException e) {
                Log.e(TAG, "error on resume", e);
                watcher.forceOffline(true);
            }
        }

    }

    @Override
    public void onStop()
    {
        playerView.suspend();
        watcher.stop();
        App.getNetworkHelper().removeListener(this);
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
    public void onConnectivityChanged(boolean connected)
    {
        isConnected = connected;

        if (isConnected) {
            Log.d(TAG, "Connected");
            watcher.start(this);
        } else {
            Log.d(TAG, "Disconnected");
            watcher.forceOffline(false);
            watcher.stop();
            offline();
            cancelQualityErrorDialog();
            showDisconnectedToast();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        watcher.forceOffline(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // Force completion to allow stream restart
            return false;
        }

        showQualityErrorDialog();

        return true;
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
            showQualityUnavailableDialog();
        }

        playerView.setVisibility(View.VISIBLE);
        offlineImageView.setVisibility(View.GONE);
        offlineTextView.setVisibility(View.GONE);
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
        offlineTextView.setVisibility(View.VISIBLE);
        Log.d(TAG, preferredChannel + " went offline");
    }

    @Override
    public void offlineImage(Bitmap bm)
    {
        Drawable d = new BitmapDrawable(getResources(), bm);
        offlineImageView.setImageDrawable(d);
        offlineImageView.setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    public void channel(Channel channel)
    {
        getActivity().setTitle(channel.getDisplayName());
    }

    private void showDisconnectedToast()
    {
        Toast.makeText(getActivity(), "No connection to server.", Toast.LENGTH_SHORT)
            .show();
    }

    private void showQualityUnavailableDialog()
    {
        StringBuilder sb = new StringBuilder("Preferred quality \"")
            .append(preferredQuality).append("\" is not available.");

        if (Qualities.isVideoQuality(preferredQuality) &&
                Qualities.numVideoQualities(watcher.getQualities()) > 0)
        {
            sb.append(" Please select another quality.");
        }

        new AlertDialog.Builder(getActivity()).setMessage(sb.toString())
            .setCancelable(false)
            .setNeutralButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            })
            .show();
    }

    private void showQualityErrorDialog()
    {
        handler.postDelayed(showQualityErrorRunnable, DIALOG_DELAY);
    }

    private void cancelQualityErrorDialog()
    {
        handler.removeCallbacks(showQualityErrorRunnable);
    }

    private Runnable showQualityErrorRunnable = new Runnable() {
        @Override
        public void run()
        {
            StringBuilder sb = new StringBuilder("An error occurred playing quality \"")
                .append(preferredQuality).append("\".");

            if (Qualities.isVideoQuality(preferredQuality) &&
                    Qualities.numVideoQualities(watcher.getQualities()) > 1)
            {
                sb.append(" Please select another quality.");
            }

            new AlertDialog.Builder(getActivity()).setMessage(sb.toString())
                .setTitle("Error")
                .setIcon(R.drawable.dialog_alert_dark)
                .setCancelable(false)
                .setNeutralButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();
        }
    };
}
