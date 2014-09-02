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
    private List<PreferenceChangeListener<T>> listeners;

    public PreferenceHelper(Context context, String preferenceKey)
    {
        sharedContext = context;
        preferenceName = preferenceKey;
        listeners = new ArrayList<PreferenceChangeListener<T>>();

        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (preferenceName.equals(key)) {
            PreferenceChangeListener<T> listener;
            Iterator<PreferenceChangeListener<T>> i;
            for (i = listeners.iterator(); i.hasNext(); ) {
                listener = i.next();
                if (!listener.isValid()) {
                    i.remove();
                } else {
                    listener.onPreferenceChanged(getPreferenceValue());
                }
            }
        }
    }

    protected void addListener(PreferenceChangeListener<T> listener)
    {
        listeners.add(listener);
    }

    protected boolean removeListener(PreferenceChangeListener<T> listener)
    {
        return listeners.remove(listener);
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
