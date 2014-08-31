package gg.destiny.app.chat;

import gg.destiny.app.preference.QualityPreferenceHelper;
import android.app.Application;

public final class App extends Application {
    private static App instance;

    private QualityPreferenceHelper qualityPreferenceHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        qualityPreferenceHelper = new QualityPreferenceHelper(this);
    }

    public static QualityPreferenceHelper getQualityPreferenceHelper()
    {
        return instance.qualityPreferenceHelper;
    }
}
