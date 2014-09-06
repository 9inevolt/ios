package gg.destiny.app.preference;

import gg.destiny.app.model.Channel;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.*;
import android.util.Log;


public final class ChannelPreferenceHelper extends PreferenceHelper<Channel>
{
    public static final String TAG = "ChannelPreferenceHelper";

    private final Channel defaultChannel;

    public ChannelPreferenceHelper(Context context, String preferenceKey,
            Channel preferenceDefault)
    {
        super(context, preferenceKey);
        defaultChannel = preferenceDefault;
    }

    @Override
    public Channel getPreferenceValue()
    {
        Channel channel = defaultChannel;
        try {
            String value = getPreferenceString(null);
            if (value != null) {
                channel = new Channel(new JSONObject(value));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error deserializing from JSON", e);
        }

        return channel;
    }

    @Override
    public void setPreferenceValue(Channel value)
    {
        setPreferenceString(value.toString());
    }

    public void addListener(ChannelPreferenceChangeListener l)
    {
        super.addListener(l, new ListenerWrapper(l));
    }

    public void removeListener(ChannelPreferenceChangeListener l)
    {
        super.removeListener(l);
    }

    private static final class ListenerWrapper extends PreferenceChangeListener<Channel>
    {
        WeakReference<ChannelPreferenceChangeListener> listener;

        ListenerWrapper(ChannelPreferenceChangeListener l)
        {
            listener = new WeakReference<ChannelPreferenceChangeListener>(l);
        }

        @Override
        boolean isValid()
        {
            return listener.get() != null;
        }

        @Override
        public void onPreferenceChanged(Channel value)
        {
            ChannelPreferenceChangeListener l = listener.get();
            if (l != null) {
                l.onChannelPreferenceChanged(value);
            }
        }
    }
}
