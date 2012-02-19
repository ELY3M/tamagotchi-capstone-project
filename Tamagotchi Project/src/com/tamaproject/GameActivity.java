package com.tamaproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class GameActivity extends Activity
{
    private static final String TAG = GameActivity.class.getSimpleName();
    private GameView gv;
    private static final int CONFIRM_ENDGAME = 0;

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

    @Override
    protected Dialog onCreateDialog(int id)
    {
	switch (id)
	{
	case GameActivity.CONFIRM_ENDGAME:
	    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	    builder2.setTitle("End Game");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to quit?");
	    builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	    {
		public void onClick(DialogInterface dialog, int which)
		{
		    finish();
		    return;
		}
	    });

	    builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
	    {
		public void onClick(DialogInterface dialog, int which)
		{
		    //Toast.makeText(getApplicationContext(), "Clicked Cancel!", Toast.LENGTH_SHORT).show();
		    return;
		}
	    });

	    return builder2.create();
	}

	return null;
    }

}
