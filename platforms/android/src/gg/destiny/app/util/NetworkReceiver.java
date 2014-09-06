package gg.destiny.app.util;

import gg.destiny.app.chat.App;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (ConnectivityManager.CONNECTIVITY_ACTION != intent.getAction())
        {
            return;
        }

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo network = cm.getActiveNetworkInfo();
        boolean connected = network != null && network.isConnected();

        if (!connected) {
            // Don't notify if still connecting
            if (network != null && network.isConnectedOrConnecting()) {
                return;
            }

            // Don't notify if fallback is available
            NetworkInfo other = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            if (other != null && other.isAvailable()) {
                return;
            }
        }

        App.getNetworkHelper().onConnectivityChanged(connected);
    }

}
