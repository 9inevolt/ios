package gg.destiny.app.chat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ChatActivity extends Activity implements CordovaInterface
{
    public static final String TAG = "ChatActivity";
    private static final String SHOW_WARNING_PREFERENCE = "show_version_warning";

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    CordovaWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        Config.init(this);
        webView = (CordovaWebView) findViewById(R.id.web_view);
        //webView.loadUrl(Config.getStartUrl());
        //webView.loadUrl("file:///android_asset/www/chat2.html");
        webView.loadUrl("file:///android_asset/www/chat-lite.html");

        if (savedInstanceState == null) {
            warningCheck();
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

    private void warningCheck()
    {
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean show = prefs.getBoolean(SHOW_WARNING_PREFERENCE, true);

        if (!show)
            return;

        new AlertDialog.Builder(this)
            .setMessage("Android 4.0 or higher required for video support.")
            .setPositiveButton("Dismiss", null)
            .setNegativeButton("Don't show again", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    prefs.edit().putBoolean(SHOW_WARNING_PREFERENCE, false).commit();
                }
            }).show();
    }
}
