package gg.destiny.app.preference;

import java.lang.ref.WeakReference;
import java.util.*;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;


public final class QualityPreferenceHelper implements OnSharedPreferenceChangeListener
{
    private static final String QUALITY_PREFERENCE = "preferred_quality";

    private Context sharedContext;
    private List<WeakReference<QualityPreferenceChangeListener>> listeners;

    public QualityPreferenceHelper(Context context)
    {
        sharedContext = context;
        listeners = new ArrayList<WeakReference<QualityPreferenceChangeListener>>();

        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (QUALITY_PREFERENCE.equals(key)) {
            QualityPreferenceChangeListener listener;
            Iterator<WeakReference<QualityPreferenceChangeListener>> i;
            for (i = listeners.iterator(); i.hasNext(); ) {
                listener = i.next().get();
                if (listener == null) {
                    i.remove();
                } else {
                    listener.onQualityPreferenceChanged(getPreferenceValue());
                }
            }
        }
    }

    public void showDialog(Context context, List<String> qualities)
    {
        int selected = qualities.indexOf(getPreferenceValue());
        final String[] qArray = qualities.toArray(new String[0]);
        new AlertDialog.Builder(context)
            .setTitle("Quality")
            .setSingleChoiceItems(qArray, selected, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    setPreferenceValue(qArray[which]);
                    dialog.dismiss();
                }
            })
            .show();
    }

    public void addListener(QualityPreferenceChangeListener listener)
    {
        listeners.add(new WeakReference<QualityPreferenceChangeListener>(listener));
    }

    public String getPreferenceValue()
    {
        return PreferenceManager.getDefaultSharedPreferences(sharedContext)
            .getString(QUALITY_PREFERENCE, "mobile");
    }

    @TargetApi(9)
    public void setPreferenceValue(String value)
    {
        SharedPreferences.Editor editor = getSharedPreferences().edit()
                .putString(QUALITY_PREFERENCE, value);
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
