package gg.destiny.app.widget;

import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;
import gg.destiny.app.widget.FullMediaController.OnSettingsListener;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.*;
import android.media.MediaPlayer.*;
import android.net.Uri;
import android.os.Handler;
import android.util.*;
import android.view.*;

@TargetApi(14)
public class PlayerView extends SurfaceView implements FullMediaPlayerControl
{
    enum State {
        ERROR, RESETTING, IDLE, PREPARING, PREPARED, PLAYING, PAUSED, COMPLETE
    }

    private static final String TAG = "PlayerView";

    private long prePrepare;

    private int mVideoWidth;
    private int mVideoHeight;
    private State state, targetState;
    private MediaPlayer player = null;
    private FullMediaController controls = null;
    private SurfaceHolder holder = null;
    private Uri playUri = null;
    private boolean fullScreen, userFullScreen;
    private OnCompletionListener onCompletionListener;
    private OnFullScreenListener onFullScreenListener;
    private OnSettingsListener onSettingsListener;

    public PlayerView(Context context)
    {
        super(context);
        initVideoView();
    }

    public PlayerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initVideoView();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initVideoView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (controls != null) {
            controls.toggleVisibility();
        }
        return false;
    }

    public void setVideoURI(String uri) throws IOException {
        play(uri);
        requestLayout();
        invalidate();
    }

    public void setOnCompletionListener(OnCompletionListener l)
    {
        onCompletionListener = l;
    }

    public void setOnFullScreenListener(OnFullScreenListener l)
    {
        onFullScreenListener = l;
    }

    public void setOnSettingsListener(OnSettingsListener l)
    {
        onSettingsListener = l;
    }

    public void resume() throws IOException {
        play();
    }

    public void suspend() {
        releasePlayer(false);
    }

    public void stop() {
        if (player != null) {
            player.stop();
        }

        releasePlayer(true);
    }

    @Override
    public boolean isInPlaybackState()
    {
        return player != null &&
            state != State.ERROR &&
            state != State.RESETTING &&
            state != State.IDLE &&
            state != State.PREPARING;
    }

    @Override
    public boolean isBuffering()
    {
        if (state == State.PREPARING)
            return true;

        if (state == State.IDLE || state == State.RESETTING) {
            return targetState == State.PLAYING ||
                   targetState == State.PAUSED;
        }

        return false;
    }

    @Override
    public void start()
    {
        if (isInPlaybackState()) {
            player.start();
            state = State.PLAYING;
        }

        targetState = State.PLAYING;
    }

    @Override
    public void pause()
    {
        if (isPlaying()) {
            player.pause();
            state = State.PAUSED;
        }

        targetState = State.PAUSED;
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
        if (onSettingsListener != null) {
            onSettingsListener.onSettings(player);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);

        int visibility = getSystemUiVisibility();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && isInPlaybackState()) {
            doFullScreen(true, false);
            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        } else {
            doFullScreen(false, false);
            visibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (!ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            setSystemUiVisibility(visibility);
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        initControls();
    }

    protected void doFullScreen(boolean full, boolean userInitiated)
    {
        if (!userInitiated && userFullScreen) {
            return;
        }

        if (full)
        {
            fullScreen = true;
            if (userInitiated)
            {
                userFullScreen = true;
            }
        }
        else
        {
            fullScreen = false;
            if (userInitiated)
            {
                userFullScreen = false;
            }
        }

        if (onFullScreenListener != null) {
            onFullScreenListener.onFullScreen(player, full, userInitiated);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");

        if (mVideoWidth <= 0 || mVideoHeight <= 0) {
            mVideoWidth = 16;
            mVideoHeight = 9;
        }

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (isFullScreen()) {
            // stretch in full screen mode
            height = heightSpecSize;
            width = widthSpecSize;
        } else if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
            // the size is fixed
            width = widthSpecSize;
            height = heightSpecSize;

            // for compatibility, we adjust size based on aspect ratio
            if ( mVideoWidth * height  < width * mVideoHeight ) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            // only the width is fixed, adjust the height to match aspect ratio if possible
            width = widthSpecSize;
            height = width * mVideoHeight / mVideoWidth;
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                // couldn't match aspect ratio within the constraints
                height = heightSpecSize;
            }
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            // only the height is fixed, adjust the width to match aspect ratio if possible
            height = heightSpecSize;
            width = height * mVideoWidth / mVideoHeight;
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                // couldn't match aspect ratio within the constraints
                width = widthSpecSize;
            }
        } else {
            // neither the width nor the height are fixed, try to use actual video size
            width = mVideoWidth;
            height = mVideoHeight;
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                // too tall, decrease both width and height
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
            }
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                // too wide, decrease both width and height
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
            }
        }

        setMeasuredDimension(width, height);
    }

    private void initVideoView() {
        mVideoWidth = 16;
        mVideoHeight = 9;
        getHolder().addCallback(callback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        state = State.IDLE;
        targetState = State.IDLE;
    }

    private void initControls()
    {
        if (controls == null) {
            controls = new FullMediaController(getContext());
            controls.setMediaPlayer(this);
            if (getParent() instanceof ViewGroup) {
                controls.setAnchorView((ViewGroup) getParent());
            } else {
                Log.w(TAG, "Can't anchor controls without a parent ViewGroup.");
            }
        }
    }

    private void initPlayer()
    {
        if (player == null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(preparedListener);
            player.setOnErrorListener(errorListener);
            player.setOnVideoSizeChangedListener(videoSizeChangedListener);
            player.setOnCompletionListener(completionListener);
            player.setOnBufferingUpdateListener(bufferingUpdateListener);
            player.setOnInfoListener(infoListener);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        initControls();
    }

    private void releasePlayer(boolean clearTarget)
    {
        if (clearTarget) {
            targetState = State.IDLE;
        }

        if (player != null) {
            state = State.RESETTING;
            player.reset();
            player.release();
            player = null;
        }

        state = State.IDLE;
    }

    private void releasePlayerAsync(final boolean clearTarget, final Uri playUri)
    {
        state = State.RESETTING;

        if (clearTarget) {
            targetState = State.IDLE;
        }

        controls.show();

        final Handler h = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                if (player != null) {
                    player.reset();
                }
                h.post(new Runnable() {
                   @Override
                    public void run()
                    {
                        playerReset();
                        initPlayer();
                        player.setDisplay(holder);
                        try {
                            player.setDataSource(getContext(), playUri);
                        } catch (IOException e) {
                            Log.e(TAG, "Error during async play", e);
                        }
                        player.setScreenOnWhilePlaying(true);

                        prePrepare = System.currentTimeMillis();
                        player.prepareAsync();

                        state = State.PREPARING;
                    }
                });
            }
        }).start();
    }

    private void playerReset()
    {
        if (player != null) {
            player.release();
            player = null;
        }
        state = State.IDLE;
    }

    private void play(String url) throws IOException
    {
        playUri = Uri.parse(url);
        play();
    }

    private void play() throws IOException
    {
        if (holder == null || playUri == null)
            return;

        Log.d(TAG, "play: " + playUri);

        releasePlayerAsync(false, playUri);
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceDestroyed(SurfaceHolder sHolder)
        {
            holder = null;
            releasePlayer(false);
        }

        @Override
        public void surfaceCreated(SurfaceHolder sHolder)
        {
            holder = sHolder;
            try {
                play();
            } catch (IOException e) {
                Log.e(TAG, "Playback error", e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder sHolder, int format, int width, int height)
        {
            if (targetState == State.PLAYING && width > 0 && height > 0) {
                start();
            }
        }
    };

    private MediaPlayer.OnPreparedListener preparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            long prepareTime = System.currentTimeMillis() - prePrepare;
            Log.d(TAG, "Prepare time elapsed: " + prepareTime + "ms");

            state = State.PREPARED;
            if (targetState == State.PLAYING) {
                player.start();
                state = State.PLAYING;
                if (controls != null)
                    controls.show();
            } else if (!isPlaying()) {
                if (controls != null)
                    controls.show(0);
            }
        }
    };

    private MediaPlayer.OnErrorListener errorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            Log.e(TAG, "what: " + what + ", extra: " + extra);
            return false;
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener =
        new OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int mWidth, int mHeight)
        {
            Log.d(TAG, "onVideoSizeChanged: " + mWidth + "x" + mHeight);

            if (mWidth <= 0 || mHeight <= 0)
                return;

            mVideoWidth = mWidth;
            mVideoHeight = mHeight;

            //getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    };

    private MediaPlayer.OnCompletionListener completionListener =
        new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            state = State.COMPLETE;
            targetState = State.COMPLETE;
            if (controls != null) {
                controls.hide();
            }

            stop();

            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(player);
            }
        }
    };

    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener =
        new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {
            Log.d(TAG, "Buffering: " + percent + "%");
        }
    };

    private MediaPlayer.OnInfoListener infoListener =
        new MediaPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra)
            {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        Log.d(TAG, "Info Metadata Update: " + extra);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(TAG, "Info Buffering Start: " + extra);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(TAG, "Info Buffering End: " + extra);
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.d(TAG, "Info Video Rendering Start: " + extra);
                        break;
                    default:
                        return false;
                }

                return true;
            }
        };
}
