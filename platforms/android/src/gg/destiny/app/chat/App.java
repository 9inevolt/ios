package gg.destiny.app.chat;

import gg.destiny.app.preference.*;
import android.app.Application;

public final class App extends Application {
    private static final String CHANNEL_PREFERENCE = "preferred_channel";
    private static final String CHANNEL_DEFAULT = "destiny";

    private static App instance;

    private QualityPreferenceHelper qualityPreferenceHelper;
    private ChannelPreferenceHelper channelPreferenceHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        qualityPreferenceHelper = new QualityPreferenceHelper(this);
        channelPreferenceHelper = new ChannelPreferenceHelper(this, CHANNEL_PREFERENCE,
                CHANNEL_DEFAULT);
    }

    public static QualityPreferenceHelper getQualityPreferenceHelper()
    {
        return instance.qualityPreferenceHelper;
    }

    public static ChannelPreferenceHelper getChannelPreferenceHelper()
    {
        return instance.channelPreferenceHelper;
    }
}
