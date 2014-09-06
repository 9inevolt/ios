package gg.destiny.app.preference;

import java.util.*;

import android.annotation.TargetApi;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;

public abstract class PreferenceHelper<T> implements OnSharedPreferenceChangeListener
{
    private Context sharedContext;
    private final String preferenceName;
    private Map<Object, PreferenceChangeListener<T>> listeners;

    public PreferenceHelper(Context context, String preferenceKey)
    {
        sharedContext = context;
        preferenceName = preferenceKey;
        listeners = new HashMap<Object, PreferenceChangeListener<T>>();

        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (preferenceName.equals(key)) {
            PreferenceChangeListener<T> listener;
            Iterator<PreferenceChangeListener<T>> i;
            synchronized (this) {
                for (i = listeners.values().iterator(); i.hasNext(); ) {
                    listener = i.next();
                    if (!listener.isValid()) {
                        i.remove();
                    } else {
                        listener.onPreferenceChanged(getPreferenceValue());
                    }
                }
            }
        }
    }

    protected synchronized void addListener(Object key, PreferenceChangeListener<T> listener)
    {
        listeners.put(key, listener);
    }

    protected synchronized void removeListener(Object key)
    {
        listeners.remove(key);
    }

    public abstract T getPreferenceValue();

    public abstract void setPreferenceValue(T value);

    protected String getPreferenceString(String defaultValue)
    {
        return PreferenceManager.getDefaultSharedPreferences(sharedContext)
            .getString(preferenceName, defaultValue);
    }

    @TargetApi(9)
    protected void setPreferenceString(String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences().edit()
                .putString(preferenceName, value);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private SharedPreferences getSharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(sharedContext);
    }
}
