package gg.destiny.app.util;

import gg.destiny.app.model.Channel;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

class StreamEventHandler extends Handler
{
    private static final int MSG_OFFLINE = 1;
    private static final int MSG_ONLINE = 2;
    private static final int MSG_OFFLINE_IMAGE = 3;
    private static final int MSG_CHANNEL = 4;

    private StreamEventListener listener;

    StreamEventHandler(StreamEventListener l)
    {
        listener = l;
    }

    public void clear()
    {
        removeMessages(MSG_OFFLINE);
        removeMessages(MSG_ONLINE);
        removeMessages(MSG_OFFLINE_IMAGE);
        removeMessages(MSG_CHANNEL);
    }

    public void offline()
    {
        obtainMessage(MSG_OFFLINE).sendToTarget();
    }

    public void online()
    {
        obtainMessage(MSG_ONLINE).sendToTarget();
    }

    public void offlineImage(Bitmap bm)
    {
        obtainMessage(MSG_OFFLINE_IMAGE, bm).sendToTarget();
    }

    public void channel(Channel channel)
    {
        obtainMessage(MSG_CHANNEL, channel).sendToTarget();
    }

    @Override
    public void handleMessage(Message msg)
    {
        switch (msg.what) {
            case MSG_OFFLINE:
                listener.offline();
                break;
            case MSG_ONLINE:
                listener.online();
                break;
            case MSG_OFFLINE_IMAGE:
                listener.offlineImage((Bitmap) msg.obj);
                break;
            case MSG_CHANNEL:
                listener.channel((Channel) msg.obj);
                break;
        }
    }
}
