package gg.destiny.app.chat;

import gg.destiny.app.model.Channel;
import gg.destiny.app.preference.*;
import gg.destiny.app.util.NetworkHelper;
import android.app.Application;

public final class App extends Application {
    private static final String CHANNEL_PREFERENCE = "preferred_channel";

    public static final Channel CHANNEL_DEFAULT = new Channel("destiny", "Destiny");

    private static App instance;

    private QualityPreferenceHelper qualityPreferenceHelper;
    private ChannelPreferenceHelper channelPreferenceHelper;
    private NotificationPreferenceHelper notificationPreferenceHelper;
    private NetworkHelper networkHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        qualityPreferenceHelper = new QualityPreferenceHelper(this);
        channelPreferenceHelper = new ChannelPreferenceHelper(this,
                CHANNEL_PREFERENCE, CHANNEL_DEFAULT);
        notificationPreferenceHelper = new NotificationPreferenceHelper(this);
        networkHelper = new NetworkHelper(this);
    }

    public static QualityPreferenceHelper getQualityPreferenceHelper()
    {
        return instance.qualityPreferenceHelper;
    }

    public static ChannelPreferenceHelper getChannelPreferenceHelper()
    {
        return instance.channelPreferenceHelper;
    }

    public static NotificationPreferenceHelper getNotificationPreferenceHelper()
    {
        return instance.notificationPreferenceHelper;
    }

    public static NetworkHelper getNetworkHelper()
    {
        return instance.networkHelper;
    }
}
