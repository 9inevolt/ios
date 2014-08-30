package gg.destiny.app.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public final class KrakenApi
{
    public static final ChannelAccessToken getChannelAccessToken(String channel) throws IOException, JSONException
    {
        String url = String.format("http://api.twitch.tv/api/channels/%s/access_token", channel);

        JSONObject jObj = new JSONObject(getPlaylist(url));
        return new ChannelAccessToken(jObj.getString("sig"), jObj.getString("token"));
    }

    public static final String getStreams(ChannelAccessToken token, String channel) throws IOException
    {
        String url = String.format(
                "http://usher.twitch.tv/select/%s.json?nauthsig=%s&nauth=%s&allow_source=true&allow_audio_only=true",
                channel, Uri.encode(token.sig), Uri.encode(token.token));

        return getPlaylist(url);
    }

    public static final String getStreams2(ChannelAccessToken token, String channel) throws IOException
    {
        String url = String.format(
                "http://usher.twitch.tv/api/channel/hls/%s.m3u8?sig=%s&token=%s&allow_source=true&allow_audio_only=true",
                channel, Uri.encode(token.sig), Uri.encode(token.token));

        return getPlaylist(url);
    }

    public static final String getPlaylist(String url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException(conn.getResponseMessage());
            }

            return readFullyString(conn.getInputStream());
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
