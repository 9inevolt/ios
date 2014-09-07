package gg.destiny.app.preference;

import java.lang.ref.WeakReference;

import android.content.Context;

public class NotificationPreferenceHelper extends BooleanPreferenceHelper
{
    private static final String NOTIFICATION_PREFERENCE = "enable_notifications";
    private static final boolean NOTIFICATION_DEFAULT = true;

    public NotificationPreferenceHelper(Context context)
    {
        super(context, NOTIFICATION_PREFERENCE, NOTIFICATION_DEFAULT);
    }

    public void addListener(NotificationPreferenceChangeListener l)
    {
        super.addListener(l, new ListenerWrapper(l));
    }

    public void removeListener(NotificationPreferenceChangeListener l)
    {
        super.removeListener(l);
    }

    private static final class ListenerWrapper extends PreferenceChangeListener<Boolean>
    {
        WeakReference<NotificationPreferenceChangeListener> listener;

        ListenerWrapper(NotificationPreferenceChangeListener l)
        {
            listener = new WeakReference<NotificationPreferenceChangeListener>(l);
        }

        @Override
        boolean isValid()
        {
            return listener.get() != null;
        }

        @Override
        public void onPreferenceChanged(Boolean value)
        {
            NotificationPreferenceChangeListener l = listener.get();
            if (l != null) {
                l.onNotificationPreferenceChanged(value);
            }
        }
    }
}
