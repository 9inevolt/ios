package gg.destiny.app.content;

import android.app.SearchManager;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;

public class AppSearchRecentSuggestionProvider extends SearchRecentSuggestionsProvider
{
    private static final String AUTHORITY = "gg.destiny.app.content.recentsearchprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
        + SearchManager.SUGGEST_URI_PATH_QUERY);
    private static final int MODE = DATABASE_MODE_QUERIES;

    public AppSearchRecentSuggestionProvider()
    {
        setupSuggestions(AUTHORITY, MODE);
    }

    public static final SearchRecentSuggestions getSuggestions(Context context)
    {
        return new SearchRecentSuggestions(context, AUTHORITY, MODE);
    }
}
