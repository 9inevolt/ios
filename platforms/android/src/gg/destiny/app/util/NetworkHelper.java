package gg.destiny.app.util;

import java.util.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkHelper
{
    private Collection<NetworkListener> listeners = new LinkedHashSet<NetworkListener>();
    private Context sharedContext;

    public NetworkHelper(Context context)
    {
        sharedContext = context;
    }

    public synchronized void addListener(NetworkListener l)
    {
        listeners.add(l);
    }

    public synchronized void removeListener(NetworkListener l)
    {
        listeners.remove(l);
    }

    public boolean isConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager) sharedContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    synchronized void onConnectivityChanged(boolean connected)
    {
        for (NetworkListener l : listeners) {
            l.onConnectivityChanged(connected);
        }
    }
}
