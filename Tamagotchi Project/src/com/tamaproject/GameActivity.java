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
    protected void onDestroy()  // called when back button pressed
    {
	Log.d(TAG, "Destroying...");
	Toast.makeText(this, "Closing game...", Toast.LENGTH_SHORT).show();
	super.onDestroy();
    }

    @Override
    protected void onStop()  // called when home button pressed, comes after onPause()
    {
	Log.d(TAG, "Stopping...");	
	super.onStop();
	//finish();
    }
    
    protected void onRestart()
    {	
	super.onRestart();
    }
    
    protected void onPause()  // called when the app is minimized because another activity comes into the foreground
    {
	super.onPause();
	Toast.makeText(this, "Pausing game...", Toast.LENGTH_SHORT).show();
    }
    
    protected void onResume()  // called when user returns to activity from onPause()
    {
	super.onResume();
	Log.d(TAG, "Restarting...");
	Toast.makeText(this, "Resuming game...", Toast.LENGTH_SHORT).show();
	setContentView(gv);
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
