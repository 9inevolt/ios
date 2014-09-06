package gg.destiny.app.chat;

import gg.destiny.app.content.AppSearchRecentSuggestionProvider;
import gg.destiny.app.fragments.PlayerFragment;
import gg.destiny.app.preference.ChannelPreferenceChangeListener;
import gg.destiny.app.util.NetworkListener;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.SearchView;

@TargetApi(14)
public class PlayerActivity extends Activity implements CordovaInterface, OnFullScreenListener,
        ChannelPreferenceChangeListener, NetworkListener
{
    public static final String TAG = "PlayerActivity";
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private View webContainer;
    private CordovaWebView webView;
    private View webOffline, webLoading;
    private PlayerFragment player;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        setContentView(R.layout.player);

        player = (PlayerFragment) getFragmentManager().findFragmentByTag(getString(R.string.player_fragment));
        if (player == null)
        {
            player = new PlayerFragment();
            getFragmentManager().beginTransaction()
                .add(R.id.player_container, player, getString(R.string.player_fragment))
                .commit();
        }
        player.setOnFullScreenListener(this);

        Config.init(this);
        webContainer = findViewById(R.id.web_container);
        webView = (CordovaWebView) findViewById(R.id.web_view);
        webOffline = findViewById(R.id.web_offline);
        webLoading = findViewById(R.id.web_loading);
        //webView.loadUrl(Config.getStartUrl());
        webView.loadUrl("file:///android_asset/www/chat-lite.html");

        App.getChannelPreferenceHelper().addListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        App.getNetworkHelper().removeListener(this);
        webView.handlePause(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        App.getNetworkHelper().addListener(this);
        webView.handleResume(true, true);
        onConnectivityChanged(App.getNetworkHelper().isConnected());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.menu_clear_history) {
            AppSearchRecentSuggestionProvider.getSuggestions(this).clearHistory();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectivityChanged(boolean connected)
    {
        if (connected) {
            if (webOffline.getVisibility() == View.VISIBLE) {
                webView.reload();
            }
            webOffline.setVisibility(View.GONE);
        } else {
            webOffline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFullScreen(MediaPlayer mp, boolean full, boolean userInitiated)
    {
        if (full) {
            webContainer.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActionBar().hide();
            if (userInitiated) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            webContainer.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActionBar().show();
            if (userInitiated) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public void onChannelPreferenceChanged(String channel)
    {
        player = new PlayerFragment();
        player.setOnFullScreenListener(this);
        getFragmentManager().beginTransaction()
            .replace(R.id.player_container, player, getString(R.string.player_fragment))
            .commit();
    }

    @Override
    protected void onDestroy()
    {
        if (webView != null) {
            webView.removeAllViews();
            webView.handleDestroy();
        }

        App.getChannelPreferenceHelper().removeListener(this);

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null && !"".equals(query.trim())) {
                App.getChannelPreferenceHelper().setPreferenceValue(query);
                searchMenuItem.collapseActionView();
                AppSearchRecentSuggestionProvider.getSuggestions(this).saveRecentQuery(query, null);
            }
        }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public Activity getActivity()
    {
        return this;
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     * @return              Object or null
     */
    public Object onMessage(String id, Object data) {
        LOG.d(TAG, "onMessage(" + id + "," + data + ")");
        if ("onPageStarted".equals(id)) {
            webLoading.setVisibility(View.VISIBLE);
        } else if ("spinner".equals(id) && "stop".equals(data.toString())) {
            webLoading.setVisibility(View.GONE);
        } else if ("onReceivedError".equals(id)) {
            //TODO
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool()
    {
        return threadPool;
    }

    /**
     * Get string property for activity.
     *
     * @param name
     * @param defaultValue
     * @return the String value for the named property
     */
    protected String getStringProperty(String name, String defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        name = name.toLowerCase(Locale.getDefault());
        String p = bundle.getString(name);
        if (p == null) {
            return defaultValue;
        }
        return p;
    }
}
