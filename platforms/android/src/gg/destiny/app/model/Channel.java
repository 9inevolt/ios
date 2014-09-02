package gg.destiny.app.model;

import org.json.JSONObject;

public class Channel
{
    private final JSONObject obj;

    public Channel(JSONObject jObj)
    {
        obj = jObj;
    }

    public String getStatus()
    {
        return obj.isNull("status") ? null
                : obj.optString("status", "");
    }

    public String getLogo()
    {
        return obj.isNull("logo") ? null
                : obj.optString("logo", "");
    }

    public String getVideoBanner()
    {
        return obj.isNull("video_banner") ? null
                : obj.optString("video_banner", "");
    }
}
