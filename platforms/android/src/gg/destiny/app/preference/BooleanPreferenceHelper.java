package gg.destiny.app.preference;

import android.content.Context;

public class BooleanPreferenceHelper extends PreferenceHelper<Boolean>
{
    private final boolean defaultValue;

    public BooleanPreferenceHelper(Context context, String preferenceKey,
            boolean preferenceDefault)
    {
        super(context, preferenceKey);
        defaultValue = preferenceDefault;
    }

    @Override
    public Boolean getPreferenceValue()
    {
        return getPreferenceBoolean(defaultValue);
    }

    @Override
    public void setPreferenceValue(Boolean value)
    {
        setPreferenceBoolean(value);
    }

}