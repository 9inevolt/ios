package gg.destiny.app.model;

import org.json.JSONObject;

public class Stream
{
    private final JSONObject obj;

    public Stream(JSONObject jObj)
    {
        obj = jObj;
    }

    public Channel getChannel()
    {
        JSONObject jObj = obj.optJSONObject("channel");

        if (jObj == null) {
            return null;
        }

        return new Channel(jObj);
    }

    public int getViewers()
    {
        return obj.optInt("viewers", -1);
    }
}
