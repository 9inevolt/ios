package gg.destiny.app.content;

import gg.destiny.app.chat.R;
import gg.destiny.app.model.Channel;
import gg.destiny.app.util.KrakenApi;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TwitchSearchSuggestionProvider extends ContentProvider
{
    public static final String TAG = "TwitchSearchSuggestionProvider";

    // Minimum query length to actually call twitch
    private static final int MINIMUM_QUERY_LENGTH = 3;

    private static final String AUTHORITY = "gg.destiny.app.content.twitchsearchprovider";
    private static final int URI_MATCH_SUGGEST = 1;

    private static final String[] COLUMNS = new String [] {
        SearchManager.SUGGEST_COLUMN_FORMAT,
        SearchManager.SUGGEST_COLUMN_ICON_1,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_QUERY,
        "_id"
    };

    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public TwitchSearchSuggestionProvider()
    {
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, URI_MATCH_SUGGEST);
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder)
    {
        if (uriMatcher.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        Cursor recents = getContext().getContentResolver().query(AppSearchRecentSuggestionProvider.CONTENT_URI,
                projection, selection, selectionArgs, sortOrder);

        if (!shouldCallTwitch(selectionArgs)) {
            return recents;
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        performQuery(selectionArgs[0], cursor);
        return new MergeCursor(new Cursor[] { recents, cursor });
    }

    @Override
    public String getType(Uri uri)
    {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    private boolean shouldCallTwitch(String[] selectionArgs)
    {
        return selectionArgs.length > 0 &&
                !TextUtils.isEmpty(selectionArgs[0]) &&
                selectionArgs[0].length() >= MINIMUM_QUERY_LENGTH;
    }

    private void performQuery(String query, MatrixCursor cursor)
    {
        JSONArray channels = null;
        try {
            JSONObject obj = KrakenApi.searchChannels(query, 10);
            if (obj != null) {
                channels = obj.optJSONArray("channels");
            }
        } catch (Exception e) {
            Log.e(TAG, "Kraken error", e);
        }

        if (channels != null) {
            for (int i = 0; i < channels.length(); i++) {
                Channel c = new Channel(channels.optJSONObject(i));
                if (!TextUtils.isEmpty(c.getDisplayName()) && !TextUtils.isEmpty(c.getName())) {
                    cursor.newRow().add(null)
                        .add(R.drawable.suggest_twitch)
                        .add(c.getDisplayName())
                        .add(c.getName());
                }
            }
        }
    }

}
