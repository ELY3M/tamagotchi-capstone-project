package com.tamaproject;

import java.util.ArrayList;

import com.tamaproject.gameobjects.Backpack;
import com.tamaproject.gameobjects.Item;
import com.tamaproject.gameobjects.Tamagotchi;
import com.tamaproject.util.GameObjectUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
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

    private int startX = 50, startY = 50;
    private Context context = null;
    public final String PREFS_NAME = "GRAPHICS";
    private SharedPreferences settings;
    private ArrayList<Item> items = new ArrayList<Item>();
    private Display display = null;
    private int height = -1, width = -1;

    private final String BACKPACK_LABEL = "Backpack";

    private Backpack bp;
    private Tamagotchi tama;

    public GameView(Context context)
    {
	super(context);
	// adding the callback (this) to the surface holder to intercept events
	getHolder().addCallback(this);

	this.context = context;

	settings = context.getSharedPreferences(PREFS_NAME, 0);

	// load last location of tama
	LoadPreferences();

	// initialize the height, width, display variables
	initDisplay();

	// create dummy items
	for (int i = 1; i <= 15; i++)
	{
	    items.add(new Item(BitmapFactory.decodeResource(getResources(), R.drawable.treasure), 0, 0));
	}

	bp = new Backpack(items, display);
	tama = new Tamagotchi(BitmapFactory.decodeResource(getResources(), R.drawable.tama), display.getWidth() / 2, display.getHeight() / 2);

	initInterface();

	// create the game loop thread
	thread = new GameLoopThread(getHolder(), this);

	// make the GamePanel focusable so it can handle events
	setFocusable(true);
    }

    public void initDisplay()
    {
	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	display = wm.getDefaultDisplay();
	this.height = display.getHeight();
	this.width = display.getWidth();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
	// at this point the surface is created and
	// we can safely start the game loop
	thread.setRunning(true);
	thread.start();
    }

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
	    if (bp.handleActionDown((int) event.getX(), (int) event.getY()))
	    {
		bp.setBackpackOpen(false);
		bp.refreshItems();
	    }
	    if (!bp.isBackpackOpen())
	    {
		tama.handleActionDown((int) event.getX(), (int) event.getY());
	    }

	    if (event.getY() > getHeight() - 50 && event.getX() > getWidth() - 50)
	    {
		bp.setBackpackOpen(!bp.isBackpackOpen());
	    }
	}
	if (event.getAction() == MotionEvent.ACTION_MOVE)
	{
	    // the gestures

	    // the tama was picked up and is being dragged
	    tama.handleActionMove((int) event.getX(), (int) event.getY());
	    Item temp = bp.handleActionMove((int) event.getX(), (int) event.getY());

	    if (GameObjectUtil.isTouching(temp, tama))
	    {
		tama.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.kuro));
	    }
	    else
	    {
		tama.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tama));
	    }

	}
	if (event.getAction() == MotionEvent.ACTION_UP)
	{
	    // touch was released
	    tama.handleActionUp();
	    giveItem(tama, bp.handleActionUp());
	}
	return true;
    }

    // this method is to demonstrate collisions
    protected boolean giveItem(GameObject tama, Item item)
    {
	if (tama != null && item != null)
	{
	    tama.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tama));
	    if (GameObjectUtil.isTouching(tama, item))
	    {
		bp.removeItem(item);
		bp.refreshItems();
		return true;
	    }
	}

	return false;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
	// fills the canvas with black
	if (canvas != null)
	{
	    canvas.drawColor(Color.BLACK);
	    if (bp.isBackpackOpen())
	    {
		bp.drawAllItems(canvas);
	    }
	    else
	    {
		drawInterface(canvas);
		tama.draw(canvas);
		bp.draw(canvas);
	    }
	}
    }

    private Paint paint = new Paint();
    private Rect bpRectangle;

    protected void drawInterface(Canvas canvas)
    {
	paint.setColor(Color.WHITE);
	paint.setStyle(Style.STROKE);
	paint.setStrokeWidth(1);
	canvas.drawRect(bpRectangle, paint);

	paint.setStyle(Style.FILL_AND_STROKE);
	paint.setTextSize(20);
	paint.setAntiAlias(true);
	canvas.drawText(BACKPACK_LABEL + " (" + bp.numItems() + "/" + bp.maxSize() + ")", 5, height / 3 * 2 + 25, paint);
    }    

    protected void initInterface()
    {
	this.bpRectangle = new Rect(0, height / 3 * 2, width - 1, height - 1);
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
