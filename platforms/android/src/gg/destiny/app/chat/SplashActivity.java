package gg.destiny.app.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.*;

public class SplashActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    intent = new Intent(SplashActivity.this, PlayerActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, ChatActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
