package gg.destiny.app.preference;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.*;


public final class ChannelPreferenceHelper extends StringPreferenceHelper
{
    public ChannelPreferenceHelper(Context context, String preferenceKey,
            String preferenceDefault)
    {
        super(context, preferenceKey, preferenceDefault);
    }

    public void addListener(ChannelPreferenceChangeListener l)
    {
        super.addListener(new ListenerWrapper(l));
    }

    private static final class ListenerWrapper extends PreferenceChangeListener<String>
    {
        WeakReference<ChannelPreferenceChangeListener> listener;

        ListenerWrapper(ChannelPreferenceChangeListener l)
        {
            listener = new WeakReference<ChannelPreferenceChangeListener>(l);
        }

        @Override
        boolean isValid()
        {
            ChannelPreferenceChangeListener l = listener.get();

            if (l == null)
                return false;

            if (l instanceof Activity) {
                Activity a = (Activity) l;
                return !a.isFinishing() && !a.isDestroyed();
            }

            return true;
        }

        @Override
        public void onPreferenceChanged(String value)
        {
            ChannelPreferenceChangeListener l = listener.get();
            if (l != null) {
                l.onChannelPreferenceChanged(value);
            }
        }
    }
}
