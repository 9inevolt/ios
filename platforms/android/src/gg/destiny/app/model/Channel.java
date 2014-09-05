package gg.destiny.app.model;

import org.json.JSONObject;

public class Channel
{
    private final JSONObject obj;

    public Channel(JSONObject jObj)
    {
        obj = jObj;
    }

    public String getDisplayName()
    {
        return optString("display_name");
    }

    public String getName()
    {
        return optString("name");
    }

    public String getStatus()
    {
        return optString("status");
    }

    public String getLogo()
    {
        return optString("logo");
    }

    public String getVideoBanner()
    {
        return optString("video_banner");
    }

    private String optString(String name) {
        return obj.isNull(name) ? null
                : obj.optString(name, "");
    }
}
