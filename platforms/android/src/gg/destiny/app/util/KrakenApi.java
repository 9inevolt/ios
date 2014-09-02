package gg.destiny.app.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public final class KrakenApi
{
    public static final JSONObject getChannel(String channel) throws IOException, JSONException
    {
        String url = String.format("https://api.twitch.tv/kraken/channels/%s", channel);

        String obj = getString(url);

        if (obj == null)
            return null;

        return new JSONObject(obj);
    }

    public static final JSONObject getStream(String channel) throws IOException, JSONException
    {
        String url = String.format("https://api.twitch.tv/kraken/streams/%s", channel);

        String obj = getString(url);

        if (obj == null)
            return null;

        return new JSONObject(obj);
    }

    public static final ChannelAccessToken getChannelAccessToken(String channel) throws IOException, JSONException
    {
        String url = String.format("http://api.twitch.tv/api/channels/%s/access_token", channel);

        String token = getString(url);

        if (token == null)
            return null;

        JSONObject jObj = new JSONObject(token);
        return new ChannelAccessToken(jObj.getString("sig"), jObj.getString("token"));
    }

    public static final String getPlaylist(String channel) throws IOException, JSONException
    {
        ChannelAccessToken token = getChannelAccessToken(channel);

        if (token ==  null)
            return null;

        String url = String.format(
                "http://usher.twitch.tv/select/%s.json?nauthsig=%s&nauth=%s&allow_source=true&allow_audio_only=true",
                channel, Uri.encode(token.sig), Uri.encode(token.token));

        return getString(url);
    }

    public static final String getPlaylist2(String channel) throws IOException, JSONException
    {
        ChannelAccessToken token = getChannelAccessToken(channel);

        if (token ==  null)
            return null;

        String url = String.format(
                "http://usher.twitch.tv/api/channel/hls/%s.m3u8?sig=%s&token=%s&allow_source=true&allow_audio_only=true",
                channel, Uri.encode(token.sig), Uri.encode(token.token));

        return getString(url);
    }

    private static final String getString(String url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            if (conn.getResponseCode() == 200) {
                return readFullyString(conn.getInputStream());
            }

            if (conn.getResponseCode() < 500) {
                return null;
            }

            throw new IOException(conn.getResponseMessage());
        } finally {
            conn.disconnect();
        }
    }

    public static final class ChannelAccessToken {
        public String sig;
        public String token;

        ChannelAccessToken(String sig, String token) {
            this.sig = sig;
            this.token = token;
        }
    }

    private static final String readFullyString(InputStream inputStream) throws IOException {
        return new String(readFully(inputStream), "UTF-8");
    }

    private static final byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }
}
