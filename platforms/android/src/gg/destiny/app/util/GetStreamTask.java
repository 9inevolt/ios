package gg.destiny.app.util;

import gg.destiny.app.model.Stream;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class GetStreamTask extends AsyncTask<String, Void, Stream>
{
    public static final String TAG = "GetStreamTask";

    @Override
    protected Stream doInBackground(String... params)
    {
        try {
            JSONObject obj = KrakenApi.getStream(params[0]);
            if (obj != null && obj.optJSONObject("stream") != null) {
                return new Stream(obj);
            }
        } catch (Exception e) {
            Log.e(TAG, "Kraken error", e);
        }

        return null;
    }
}