package com.tamaproject;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.tamaproject.gameobjects.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
    private SharedPreferences settings;
    private Gson gson = new Gson();
    private boolean firstStart = true;

    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	firstStart = false;
	gv = new GameView(this);
	//setContentView(gv);
    }

    @Override
    protected void onDestroy() // called when back button pressed
    {
	Log.d(TAG, "Destroying...");
	Toast.makeText(this, "Closing game...", Toast.LENGTH_SHORT).show();
	super.onDestroy();
    }

    @Override
    protected void onStop() // called when home button pressed, comes after onPause()
    {
	Log.d(TAG, "Stopping...");
	super.onStop();
	// finish();
    }

    protected void onPause() // called when the app is minimized because another activity comes into the foreground
    {
	super.onPause();
	Toast.makeText(this, "Pausing game...", Toast.LENGTH_SHORT).show();
	synchronized (gv)
	{
	    String tamaJson = gson.toJson(gv.getTama());
	    String ipoJson = gson.toJson(gv.getIpo());
	    String bpJson = gson.toJson(gv.getBp());
	    writeSettings(this, tamaJson, "tama.dat");
	    writeSettings(this, ipoJson, "ipo.dat");
	    // writeSettings(this, bpJson, "bp.dat");

	    Log.d(TAG, tamaJson);
	    Log.d(TAG, ipoJson);
	    Log.d(TAG, bpJson);
	}
    }

    protected void onResume() // called when user returns to activity from onPause()
    {
	super.onResume();
	Log.d(TAG, "Restarting...");
	Toast.makeText(this, "Resuming game...", Toast.LENGTH_SHORT).show();
	synchronized (gv)
	{
	    try
	    {
		String tamaJson = readSettings(this, "tama.dat");
		Tamagotchi tama = gson.fromJson(tamaJson, Tamagotchi.class);
		if (tama != null)
		    gv.setTama(tama);
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    
	    try
	    {
		String ipoJson = readSettings(this, "ipo.dat");
		InPlayObjects ipo = gson.fromJson(ipoJson, InPlayObjects.class);
		if (ipo != null)
		    gv.setIpo(ipo);
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    
	    try
	    {
		String bpJson = readSettings(this, "bp.dat");
		Backpack bp = gson.fromJson(bpJson, Backpack.class);
		if (bp != null)
		    gv.setBp(bp);
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
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
		    // Toast.makeText(getApplicationContext(), "Clicked Cancel!", Toast.LENGTH_SHORT).show();
		    return;
		}
	    });

	    return builder2.create();
	}

	return null;
    }

    // Save settings

    public void writeSettings(Context context, String data, String filename)
    {
	FileOutputStream fOut = null;
	OutputStreamWriter osw = null;
	try
	{
	    fOut = openFileOutput(filename, MODE_PRIVATE);
	    osw = new OutputStreamWriter(fOut);
	    osw.write(data);
	    osw.flush();
	    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();
	}

	catch (Exception e)
	{
	    e.printStackTrace();
	    Toast.makeText(context, "Settings not saved", Toast.LENGTH_SHORT).show();
	}

	finally
	{
	    try
	    {
		osw.close();
		fOut.close();
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    // Read settings

    public String readSettings(Context context, String filename)
    {
	FileInputStream fIn = null;
	InputStreamReader isr = null;
	BufferedReader in = null;
	String data = "";
	try
	{
	    fIn = openFileInput(filename);
	    isr = new InputStreamReader(fIn);
	    in = new BufferedReader(isr);
	    String inputLine;

	    while ((inputLine = in.readLine()) != null)
	    {
		data += inputLine;
	    }
	    Toast.makeText(context, "Settings read", Toast.LENGTH_SHORT).show();
	} catch (Exception e)
	{
	    e.printStackTrace();
	    Toast.makeText(context, "Settings not read", Toast.LENGTH_SHORT).show();
	} finally
	{
	    try
	    {
		isr.close();
		fIn.close();
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
	return data;
    }
}