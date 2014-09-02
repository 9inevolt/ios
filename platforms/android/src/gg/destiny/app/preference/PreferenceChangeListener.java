package gg.destiny.app.preference;

abstract class PreferenceChangeListener<T>
{
    abstract boolean isValid();
    public abstract void onPreferenceChanged(T value);
}
