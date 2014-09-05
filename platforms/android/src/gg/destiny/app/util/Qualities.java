package gg.destiny.app.util;

import java.util.Collection;

public final class Qualities
{
    public static final String SOURCE =     "source";
    public static final String HIGH =       "high";
    public static final String MEDIUM =     "medium";
    public static final String LOW =        "low";
    public static final String MOBILE =     "mobile";
    public static final String AUDIO_ONLY = "audio only";

    public static final boolean isVideoQuality(String quality)
    {
        return SOURCE.equals(quality) || HIGH.equals(quality) || MEDIUM.equals(quality)
                || LOW.equals(quality) || MOBILE.equals(quality);
    }

    public static final boolean isAudioQuality(String quality)
    {
        return AUDIO_ONLY.equals(quality);
    }

    public static final int numVideoQualities(Collection<String> qualities)
    {
        return qualities.size() - numAudioQualities(qualities);
    }

    public static final int numAudioQualities(Collection<String> qualities)
    {
        return qualities.contains(AUDIO_ONLY) ? 1 : 0;
    }
}
