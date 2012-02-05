package com.tamaproject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = GameView.class.getSimpleName();

    private GameLoopThread thread;
    private GameObject tama;
    private int startX = 50, startY = 50;
    private Context context = null;
    public final String PREFS_NAME = "GRAPHICS";
    private SharedPreferences settings;
    private ArrayList<GameObject> items = new ArrayList<GameObject>();

    public GameView(Context context)
    {
	super(context);
	// adding the callback (this) to the surface holder to intercept events
	getHolder().addCallback(this);

	this.context = context;

	settings = context.getSharedPreferences(PREFS_NAME, 0);

	// load last location of tama
	LoadPreferences();
	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	Display display = wm.getDefaultDisplay();

	// create tama and load bitmap
	for (int i = 0; i < 6; i++)
	{
	    GameObject item = new GameObject(BitmapFactory.decodeResource(getResources(), R.drawable.treasure), 50 * (i + 1), 50);
	    items.add(item);
	}

	tama = new GameObject(BitmapFactory.decodeResource(getResources(), R.drawable.tama), display.getWidth() / 2, display.getHeight() / 2);

	// create the game loop thread
	thread = new GameLoopThread(getHolder(), this);

	// make the GamePanel focusable so it can handle events
	setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
	// at this point the surface is created and
	// we can safely start the game loop
	thread.setRunning(true);
	thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
	Toast.makeText(this.context, tama.toString(), Toast.LENGTH_SHORT).show();
	SavePreferences("x", tama.getX() + "");
	SavePreferences("y", tama.getY() + "");
	Log.d(TAG, "Surface is being destroyed");
	try
	{
	    thread.setRunning(false);
	} catch (Exception e)
	{

	}
	Log.d(TAG, "Thread was shut down cleanly");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
	if (event.getAction() == MotionEvent.ACTION_DOWN)
	{
	    // delegating event handling to the droid
	    tama.handleActionDown((int) event.getX(), (int) event.getY());
	    for (GameObject item : items)
	    {
		item.handleActionDown((int) event.getX(), (int) event.getY());
	    }

	    // check if in the lower part of the screen we exit
	    if (event.getY() > getHeight() - 50)
	    {
		thread.setRunning(false);
		((Activity) getContext()).finish();
	    }
	    else
	    {
		Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
	    }
	}
	if (event.getAction() == MotionEvent.ACTION_MOVE)
	{
	    // the gestures
	    if (tama.isTouched())
	    {
		// the tama was picked up and is being dragged
		tama.setX((int) event.getX());
		tama.setY((int) event.getY());
	    }
	    else
	    {
		for (GameObject item : items)
		{
		    if (item.isTouched())
		    {
			item.setX((int) event.getX());
			item.setY((int) event.getY());
			break;
		    }
		}
	    }
	}
	if (event.getAction() == MotionEvent.ACTION_UP)
	{
	    // touch was released
	    if (tama.isTouched())
	    {
		tama.setTouched(false);
	    }
	    else
	    {
		for (GameObject item : items)
		{
		    if (item.isTouched())
		    {
			item.setTouched(false);
			if(giveItem(tama, item))
			    break;
		    }
		}
	    }

	    refreshItems();

	}
	return true;
    }

    // this method is to demonstrate collisions
    protected boolean giveItem(GameObject tama, GameObject item)
    {
	if (tama.getX() >= (item.getX() - item.getBitmap().getWidth() / 2) && (tama.getX() <= (item.getX() + item.getBitmap().getWidth() / 2)))
	{
	    if (tama.getY() >= (item.getY() - item.getBitmap().getWidth() / 2) && (tama.getY() <= (item.getY() + item.getBitmap().getWidth() / 2)))
	    {
		tama.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.kuro));
		items.remove(item);
		return true;
	    }
	}
	return false;
    }

    protected void refreshItems()
    {
	int i = 1;
	synchronized (items)
	{
	    for (GameObject item : items)
	    {
		item.setXY(50 * i, 50);
		i++;
	    }
	}
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
	// fills the canvas with black
	if (canvas != null)
	{
	    canvas.drawColor(Color.BLACK);
	    tama.draw(canvas);
	    for (GameObject item : items)
	    {
		item.draw(canvas);
	    }

	}
    }

    private void SavePreferences(String key, String value)
    {
	SharedPreferences.Editor editor = settings.edit();
	editor.putString(key, value);
	editor.commit();
    }

    private void LoadPreferences()
    {
	try
	{
	    this.startX = Integer.parseInt(settings.getString("x", ""));
	    this.startY = Integer.parseInt(settings.getString("y", ""));
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

}
