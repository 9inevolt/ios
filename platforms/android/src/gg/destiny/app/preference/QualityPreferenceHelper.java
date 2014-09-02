package gg.destiny.app.preference;

import java.lang.ref.WeakReference;
import java.util.*;

import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;


public final class QualityPreferenceHelper extends StringPreferenceHelper
{
    private static final String QUALITY_PREFERENCE = "preferred_quality";
    private static final String QUALITY_DEFAULT = "mobile";

    public QualityPreferenceHelper(Context context)
    {
        super(context, QUALITY_PREFERENCE, QUALITY_DEFAULT);
    }

    public void addListener(QualityPreferenceChangeListener l)
    {
        super.addListener(l, new ListenerWrapper(l));
    }

    public void removeListener(QualityPreferenceChangeListener l)
    {
        super.removeListener(l);
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

    private static final class ListenerWrapper extends PreferenceChangeListener<String>
    {
        WeakReference<QualityPreferenceChangeListener> listener;

        ListenerWrapper(QualityPreferenceChangeListener l)
        {
            listener = new WeakReference<QualityPreferenceChangeListener>(l);
        }

        @Override
        boolean isValid()
        {
            return listener.get() != null;
        }

        @Override
        public void onPreferenceChanged(String value)
        {
            QualityPreferenceChangeListener l = listener.get();
            if (l != null) {
                l.onQualityPreferenceChanged(value);
            }
        }
    }
}
