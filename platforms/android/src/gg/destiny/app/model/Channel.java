package gg.destiny.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Channel
{
    public static final String TAG = "Channel";

    private final String name;
    private final String displayName;
    private final String status;
    private final String logo;
    private final String videoBanner;
    private JSONObject jsonObject;

    public Channel(String name, String displayName)
    {
        this.name = name;
        this.displayName = displayName;
        status = null;
        logo = null;
        videoBanner = null;
    }

    public Channel(JSONObject obj)
    {
        name = optString(obj, "name");
        displayName = optString(obj, "display_name");
        status = optString(obj, "status");
        logo = optString(obj, "logo");
        videoBanner = optString(obj, "video_banner");
        jsonObject = obj;
    }

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getStatus()
    {
        return status;
    }

    public String getLogo()
    {
        return logo;
    }

    public String getVideoBanner()
    {
        return videoBanner;
    }

    @Override
    public String toString()
    {
        return getJSONObject().toString();
    }

    public JSONObject getJSONObject()
    {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("display_name", displayName);
                jsonObject.put("status", status);
                jsonObject.put("logo", logo);
                jsonObject.put("video_banner", videoBanner);
            } catch (JSONException e) {
                Log.e(TAG, "Error serializing to JSON", e);
            }
        }

        return jsonObject;
    }

    private static final String optString(JSONObject obj, String name) {
        return obj == null || obj.isNull(name) ? null
                : obj.optString(name, "");
    }
}
