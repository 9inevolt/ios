package gg.destiny.app.widget;

import android.widget.MediaController.MediaPlayerControl;

public interface FullMediaPlayerControl extends MediaPlayerControl
{
    public boolean isFullScreen();
    public void fullScreen(boolean full);
    public void doSettings();
}
