package com.tamaproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();
    private GameView gv;

    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	gv = new GameView(this);
	setContentView(gv);
    }

    @Override
    protected void onDestroy()
    {
	Log.d(TAG, "Destroying...");
	super.onDestroy();
    }

    @Override
    protected void onStop()
    {
	Log.d(TAG, "Stopping...");
	super.onStop();
	finish();
    }

}
