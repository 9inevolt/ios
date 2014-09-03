package gg.destiny.app.util;

import android.graphics.Bitmap;

public interface StreamEventListener
{
    void online();
    void offline();
    void offlineImage(Bitmap bm);
}
