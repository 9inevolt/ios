package gg.destiny.app.chat;

import gg.destiny.app.fragments.PlayerFragment;
import gg.destiny.app.preference.ChannelPreferenceChangeListener;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;

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

public class PlayerActivity extends Activity implements CordovaInterface, OnFullScreenListener,
        ChannelPreferenceChangeListener
{
    public static final String TAG = "PlayerActivity";
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    CordovaWebView webView;
    private PlayerFragment player;

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
        webView = (CordovaWebView) findViewById(R.id.web_view);
        //webView.loadUrl(Config.getStartUrl());
        webView.loadUrl("file:///android_asset/www/chat-lite.html");

        App.getChannelPreferenceHelper().addListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        return true;
    }

    @Override
    public void onFullScreen(MediaPlayer mp, boolean full)
    {
        if (full) {
            webView.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            webView.setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null && !"".equals(query.trim())) {
                App.getChannelPreferenceHelper().setPreferenceValue(query);
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

    @Override
    public Object onMessage(String id, Object data)
    {
        //Log.d(TAG, "onMessage(" + id + "," + data + ")");
        if ("onReceivedError".equals(id)) {
            Log.e(TAG, "onReceivedError");
        }
        else if ("onPageFinished".equals(id)) {
            //webView.loadUrl("javascript:alert(window.cordova);");
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool()
    {
        return threadPool;
    }
}
