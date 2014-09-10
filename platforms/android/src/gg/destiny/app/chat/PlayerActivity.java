package gg.destiny.app.chat;

import gg.destiny.app.content.AppSearchRecentSuggestionProvider;
import gg.destiny.app.fragments.PlayerFragment;
import gg.destiny.app.model.Channel;
import gg.destiny.app.preference.*;
import gg.destiny.app.service.StreamWatcherService;
import gg.destiny.app.util.NetworkListener;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.SearchView;

@TargetApi(14)
public class PlayerActivity extends Activity implements CordovaInterface, OnFullScreenListener,
        ChannelPreferenceChangeListener, NotificationPreferenceChangeListener,
        NetworkListener
{
    public static final String TAG = "PlayerActivity";
    private boolean wideLayout;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
//    private LinearLayout playerLayoutContainer;
    private View webContainer;
    private CordovaWebView webView;
    private View webOffline, webLoading;
    private PlayerFragment player;
    private MenuItem searchMenuItem, collapsePlayerMenuItem,
        enableNotificationsMenuItem;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        wideLayout = getResources().getBoolean(R.bool.wide_layout);
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (wideLayout) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

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
//        playerLayoutContainer = (LinearLayout) findViewById(R.id.player_layout_container);
        webContainer = findViewById(R.id.web_container);
        webView = (CordovaWebView) findViewById(R.id.web_view);
        webOffline = findViewById(R.id.web_offline);
        webLoading = findViewById(R.id.web_loading);
        //webView.loadUrl(Config.getStartUrl());
        // This will cause reload on rotation if we don't handle the configChanges
        // But webview can't save display state properly
        webView.loadUrl("file:///android_asset/www/chat-lite.html");

        App.getChannelPreferenceHelper().addListener(this);
        App.getNotificationPreferenceHelper().addListener(this);

        if (App.getNotificationPreferenceHelper().getPreferenceValue()) {
            startStreamWatcherService();
        }

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
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

        collapsePlayerMenuItem = menu.findItem(R.id.menu_collapse_player);
        if (player.isHidden()) {
            collapsePlayerMenuItem.setIcon(R.drawable.player_expand);
            collapsePlayerMenuItem.setTitle(R.string.expand_player_title);
        } else {
            collapsePlayerMenuItem.setIcon(R.drawable.player_collapse);
            collapsePlayerMenuItem.setTitle(R.string.collapse_player_title);
        }

        enableNotificationsMenuItem = menu.findItem(R.id.menu_enable_notifications);
        enableNotificationsMenuItem.setChecked(
                App.getNotificationPreferenceHelper().getPreferenceValue());

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
        } else if (item.getItemId() == R.id.menu_collapse_player) {
            collapsePlayer(!player.isHidden());
            return true;
        } else if (item.getItemId() == R.id.menu_enable_notifications) {
            item.setChecked(!item.isChecked());
            App.getNotificationPreferenceHelper().setPreferenceValue(
                    item.isChecked());
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
        if (wideLayout) {
            onFullScreenWide(mp, full, userInitiated);
        } else {
            onFullScreenDefault(mp, full, userInitiated);
        }
    }

    @Override
    public void onChannelPreferenceChanged(Channel channel)
    {
        player = new PlayerFragment();
        player.setOnFullScreenListener(this);
        getFragmentManager().beginTransaction()
            .replace(R.id.player_container, player, getString(R.string.player_fragment))
            .commit();
    }

    @Override
    public void onNotificationPreferenceChanged(boolean enabled)
    {
        if (enabled) {
            startStreamWatcherService();
        } else {
            stopStreamWatcherService();
        }
    }

    @Override
    protected void onDestroy()
    {
        if (webView != null) {
            webView.removeAllViews();
            webView.handleDestroy();
        }

        App.getChannelPreferenceHelper().removeListener(this);
        App.getNotificationPreferenceHelper().removeListener(this);

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    protected void onFullScreenWide(MediaPlayer mp, boolean full, boolean userInitiated)
    {
        if (full) {
            collapsePlayer(false);
            if (userInitiated) {
                webContainer.setVisibility(View.GONE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActionBar().hide();
            }
        } else {
            webContainer.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActionBar().show();
        }
    }

    protected void onFullScreenDefault(MediaPlayer mp, boolean full, boolean userInitiated)
    {
        if (full) {
            webContainer.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActionBar().hide();
            collapsePlayer(false);
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
//        LOG.d(TAG, "onMessage(" + id + "," + data + ")");
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

    private void collapsePlayer(boolean collapse)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (collapse) {
            ft.hide(player);
            collapsePlayerMenuItem.setIcon(R.drawable.player_expand);
            collapsePlayerMenuItem.setTitle(R.string.expand_player_title);
        } else {
            ft.show(player);
            collapsePlayerMenuItem.setIcon(R.drawable.player_collapse);
            collapsePlayerMenuItem.setTitle(R.string.collapse_player_title);
        }
        ft.commit();
    }

    private void startStreamWatcherService()
    {
        Intent intent = new Intent(this, StreamWatcherService.class);
        intent.putExtra(StreamWatcherService.EXTRA_CHANNEL_NAME,
                App.CHANNEL_DEFAULT.getName());
        intent.putExtra(StreamWatcherService.EXTRA_CHANNEL_DISPLAY_NAME,
                App.CHANNEL_DEFAULT.getDisplayName());
        startService(intent);
    }

    private void stopStreamWatcherService()
    {
        Intent intent = new Intent(this, StreamWatcherService.class);
        stopService(intent);
    }

    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null && !"".equals(query.trim())) {
                App.getChannelPreferenceHelper().setPreferenceValue(new Channel(query, query));
                if (searchMenuItem != null) {
                    searchMenuItem.collapseActionView();
                }
                AppSearchRecentSuggestionProvider.getSuggestions(this).saveRecentQuery(query, null);
            }

            // Clear the search flag
            setIntent(intent.setAction(null));
        }
    }
}
