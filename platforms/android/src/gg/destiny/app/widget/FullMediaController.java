package gg.destiny.app.widget;

import gg.destiny.app.chat.R;
import gg.destiny.app.widget.FullMediaPlayerControl;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.*;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class FullMediaController extends FrameLayout implements View.OnClickListener, Callback
{
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int MESSAGE_HIDE = 1;

    private Context mContext;
    private View mRoot;
    private ViewGroup mAnchor;
    private boolean mShowing;
    private FullMediaPlayerControl mPlayer;
    private ImageButton mPauseButton;
    private ImageButton mFullScreenButton;
    private ImageButton mSettingsButton;
    private Handler handler;

    public FullMediaController(Context context) {
        super(context);
        mContext = context;
        handler = new Handler(this);
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        hide();
        return true;
    }

    public void setMediaPlayer(FullMediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }

    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null)
            return;

        if (mShowing) {
            try {
                mAnchor.removeView(this);
            } catch (IllegalArgumentException ex) {
                Log.w("FullMediaController", "already removed");
            }
            mShowing = false;
        }
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        if (mAnchor == null)
            return;

        if (!mShowing) {
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            mShowing = true;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);

            mAnchor.addView(this, params);
        }

        handler.removeMessages(MESSAGE_HIDE);
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_HIDE), timeout);

        updatePausePlay();
        updateFullScreen();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.play_pause_button:
                doPauseResume();
                break;
            case R.id.full_screen_button:
                doToggleFullScreen();
                break;
            case R.id.settings_button:
                doSettings();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        show();
        return true;
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_controller, mAnchor, false);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.play_pause_button);
        if (mPauseButton != null) {
            mPauseButton.setOnClickListener(this);
        }

        mFullScreenButton = (ImageButton) v.findViewById(R.id.full_screen_button);
        if (mFullScreenButton != null) {
            mFullScreenButton.setOnClickListener(this);
        }

        mSettingsButton = (ImageButton) v.findViewById(R.id.settings_button);
        if (mSettingsButton != null) {
            mSettingsButton.setOnClickListener(this);
        }
    }

    private void updatePausePlay() {
        if (mPlayer == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.player_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.player_play);
        }
    }

    private void updateFullScreen() {
        if (mPlayer == null || mFullScreenButton == null)
            return;

        if (mPlayer.isFullScreen()) {
            mFullScreenButton.setImageResource(R.drawable.player_full_off);
        } else {
            mFullScreenButton.setImageResource(R.drawable.player_full_on);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null)
            return;

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullScreen() {
        if (mPlayer == null)
            return;

        mPlayer.fullScreen(!mPlayer.isFullScreen());
        updateFullScreen();
    }

    private void doSettings() {
        if (mPlayer == null)
            return;

        mPlayer.doSettings();
    }

    public interface OnFullScreenListener
    {
        /**
         * @param mp        the MediaPlayer associated with this callback
         * @param full      true if full screen requested
         */
        public void onFullScreen(MediaPlayer mp, boolean full);
    }
}
