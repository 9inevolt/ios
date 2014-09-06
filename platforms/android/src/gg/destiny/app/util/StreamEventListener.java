package gg.destiny.app.util;

import gg.destiny.app.model.Channel;
import android.graphics.Bitmap;

public interface StreamEventListener
{
    void online();
    void offline();
    void offlineImage(Bitmap bm);
    void channel(Channel channel);
}
