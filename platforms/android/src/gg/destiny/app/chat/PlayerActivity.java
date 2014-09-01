package gg.destiny.app.chat;

import gg.destiny.app.fragments.PlayerFragment;
import gg.destiny.app.widget.FullMediaController.OnFullScreenListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class PlayerActivity extends FragmentActivity implements CordovaInterface, OnFullScreenListener
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

        player = (PlayerFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.player_fragment));
        player.setOnFullScreenListener(this);

        Config.init(this);
        webView = (CordovaWebView) findViewById(R.id.web_view);
        //webView.loadUrl(Config.getStartUrl());
        webView.loadUrl("file:///android_asset/www/chat-lite.html");
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
    protected void onDestroy()
    {
        if (webView != null) {
            webView.removeAllViews();
            webView.handleDestroy();
        }
        super.onDestroy();
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
