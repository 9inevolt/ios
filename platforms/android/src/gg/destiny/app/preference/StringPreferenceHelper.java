package gg.destiny.app.preference;

import android.content.Context;

public class StringPreferenceHelper extends PreferenceHelper<String>
{
    private final String defaultValue;

    public StringPreferenceHelper(Context context, String preferenceKey,
            String preferenceDefault)
    {
        super(context, preferenceKey);
        defaultValue = preferenceDefault;
    }

    @Override
    public String getPreferenceValue()
    {
        return getPreferenceString(defaultValue);
    }

    @Override
    public void setPreferenceValue(String value)
    {
        setPreferenceString(value);
    }

}
