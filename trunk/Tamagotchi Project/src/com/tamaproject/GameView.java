package com.tamaproject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import com.tamaproject.gameobjects.*;
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
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = GameView.class.getSimpleName();

    private GameLoopThread thread;
    private PoopThread poopThread;
    private TamaThread tamaThread;

    private int startX = 50, startY = 50;
    private Context context = null;
    public final String PREFS_NAME = "GRAPHICS";
    private SharedPreferences settings;

    private Hashtable<Integer, Bitmap> bitmapTable = new Hashtable<Integer, Bitmap>();

    private Display display = null;
    private int height = -1, width = -1;
    private int playTopBound, playBottomBound, playRightBound, playLeftBound;

    private final String BACKPACK_LABEL = "Backpack";

    private Backpack bp; // backpack with items
    protected Tamagotchi tama; // our tamagotchi
    protected InPlayObjects ipo;

    private Random r = new Random();

    protected Handler toastHandler;

    public GameView(Context context)
    {
	super(context);
	// adding the callback (this) to the surface holder to intercept events
	getHolder().addCallback(this);
	toastHandler = new Handler();

	this.context = context;

	settings = context.getSharedPreferences(PREFS_NAME, 0);

	// load last location of tama
	LoadPreferences();

	// initialize the height, width, display variables
	initDisplay();

	// initialize bitmaps
	initBitmaps();

	// create dummy items

	ArrayList<Item> items = new ArrayList<Item>();
	items.add(new Item(bitmapTable.get(R.drawable.ic_launcher), "Health item", 7, 0, 0, 0));
	items.add(new Item(bitmapTable.get(R.drawable.ic_launcher), "Food item", 7, -20, 0, 0));
	items.add(new Item(bitmapTable.get(R.drawable.ic_launcher), "XP item", 0, 0, 0, 1000));
	for (int i = 1; i <= 15; i++)
	{
	    items.add(new Item(bitmapTable.get(R.drawable.treasure)));
	}

	bp = new Backpack(items, display);

	tama = new Tamagotchi(bitmapTable.get(R.drawable.tama), width / 2, (playTopBound + playBottomBound) / 2);
	tama.setLocked(true);

	ipo = new InPlayObjects();

	initPoop(tama.getPoop());

	initEnvironment();

	initInterface();

	// create the game loop thread
	thread = new GameLoopThread(getHolder(), this);
	poopThread = new PoopThread();
	tamaThread = new TamaThread();

	// make the GamePanel focusable so it can handle events
	setFocusable(true);
    }

    // gets the width and height of the screen
    public void initDisplay()
    {
	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	display = wm.getDefaultDisplay();
	this.height = display.getHeight();
	this.width = display.getWidth();
	this.playTopBound = height / 5;
	this.playBottomBound = height / 3 * 2 - 50;
	this.playLeftBound = 25;
	this.playRightBound = width - 25;
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
	poopThread.start();
	tamaThread.start();
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
	    poopThread.setRunning(false);
	    tamaThread.setRunning(false);
	} catch (Exception e)
	{

	}
	Log.d(TAG, "Thread was shut down cleanly");
    }

    // loads up the sprites and bitmaps
    private void initBitmaps()
    {
	bitmapTable.put(R.drawable.kuro, BitmapFactory.decodeResource(getResources(), R.drawable.kuro));
	bitmapTable.put(R.drawable.tama, BitmapFactory.decodeResource(getResources(), R.drawable.tama));
	bitmapTable.put(R.drawable.treasure, BitmapFactory.decodeResource(getResources(), R.drawable.treasure));
	bitmapTable.put(R.drawable.poop, BitmapFactory.decodeResource(getResources(), R.drawable.poop));
	bitmapTable.put(R.drawable.trash, BitmapFactory.decodeResource(getResources(), R.drawable.trash));
	bitmapTable.put(R.drawable.ic_launcher, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
	int ex = (int) event.getX();
	int ey = (int) event.getY();

	if (event.getAction() == MotionEvent.ACTION_DOWN)
	{
	    Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());

	    if (!bp.handleActionDown(ex, ey))
	    {
		if (!bp.isBackpackOpen())
		{
		    // tama.handleActionDown(ex, ey);
		    ipo.handleActionDown(ex, ey);
		}
	    }

	    // close any open pop ups
	    if (popUp != null)
		popUp.dismiss();

	    // region to open backpack
	    if (ey > height - 50 && ex > width - 50)
	    {
		bp.setBackpackOpen(!bp.isBackpackOpen());
		if (!bp.isBackpackOpen())
		{
		    bp.refreshItems();
		}
	    }
	}
	if (event.getAction() == MotionEvent.ACTION_MOVE)
	{
	    // the tama was picked up and is being dragged
	    // tama.handleActionMove(ex, ey);
	    Item temp = bp.handleActionMove(ex, ey);
	    if (temp == null)
	    {
		ipo.handleActionMove(ex, ey, playTopBound, playBottomBound);
	    }

	    if (temp != null && bp.isBackpackOpen())
	    {
		bp.setBackpackOpen(false);
		bp.refreshItems();
	    }

	    if (GameObjectUtil.isTouching(temp, tama))
	    {
		tama.setBitmap(bitmapTable.get(R.drawable.kuro));
	    }
	    else
	    {
		tama.setBitmap(bitmapTable.get(R.drawable.tama));
	    }

	}
	if (event.getAction() == MotionEvent.ACTION_UP)
	{
	    // tama.handleActionUp();
	    Item temp = bp.handleActionUp();

	    if (temp != null)
	    {
		if (temp.isMoved())
		{
		    temp.setTouched(false);
		    giveItem(tama, temp);
		}
		else
		// if user tapped item
		{
		    showItemDescription(temp);
		}
	    }
	    else
	    {
		ipo.handleActionUp();
	    }

	    bp.refreshItems();
	}
	return true;
    }

    private PopupWindow popUp;
    private LinearLayout layout;

    private void showItemDescription(Item i)
    {
	popUp = new PopupWindow(context);
	layout = new LinearLayout(context);
	TextView tv = new TextView(context);
	Button but = new Button(context);
	ImageView iv = new ImageView(context);
	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	layout.setOrientation(LinearLayout.HORIZONTAL);
	params.setMargins(10, 10, 10, 10);
	tv.setText(i.getDescription() + "\n");
	iv.setImageBitmap(i.getBitmap());
	but.setText("Close");
	but.setOnClickListener(new OnClickListener()
	{
	    public void onClick(View v)
	    {
		popUp.dismiss();
	    }

	});

	layout.addView(iv, params);
	layout.addView(tv, params);
	layout.addView(but, params);
	popUp.setContentView(layout);

	popUp.showAtLocation(layout, Gravity.BOTTOM, 10, 10);
	popUp.update(width, height / 4);
	popUp.setFocusable(true);
    }

    private void initPoop(int numPoop)
    {
	int count = 1;
	while (count < numPoop)
	{
	    GameObject go = makePoop();
	    if (!GameObjectUtil.isTouching(go, tama))
	    {
		ipo.add(go);
		count++;
	    }
	}
    }

    protected GameObject makePoop()
    {
	int tx = tama.getX();
	int ty = tama.getY();
	int x = r.nextInt(width);
	int y = r.nextInt(playBottomBound - ty) + ty;
	GameObject go = new GameObject(bitmapTable.get(R.drawable.poop), x, y);
	go.setGroup("poop");
	return go;
    }

    private void initEnvironment()
    {
	GameObject trash = new GameObject(bitmapTable.get(R.drawable.trash), playRightBound, playBottomBound);
	trash.setGroup("trashcan");
	trash.setLocked(true);
	ipo.add(trash);
    }

    // this method is to demonstrate collisions
    protected boolean giveItem(Tamagotchi tama, Item item)
    {
	if (tama != null && item != null)
	{
	    tama.setBitmap(bitmapTable.get(R.drawable.tama));
	    if (GameObjectUtil.isTouching(tama, item))
	    {
		tama.applyItem(item);
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
		ipo.draw(canvas);
		bp.draw(canvas);
	    }
	}
    }

    private Paint paint = new Paint();
    private Rect bpRectangle;
    private Rect topRectangle;

    protected void drawInterface(Canvas canvas)
    {
	// draw the rectangle around backpack
	paint.setColor(Color.WHITE);
	paint.setStyle(Style.STROKE);
	paint.setStrokeWidth(1);
	canvas.drawRect(bpRectangle, paint);
	canvas.drawRect(topRectangle, paint);

	// draw the backpack label and number of items in backpack
	paint.setStyle(Style.FILL_AND_STROKE);
	paint.setTextSize(20);
	paint.setAntiAlias(true);
	canvas.drawText(BACKPACK_LABEL + " (" + bp.numItems() + "/" + bp.maxSize() + ")", 5, height / 3 * 2 + 25, paint);

	// draw the health, hunger, sickness
	paint.setTextSize(21);
	canvas.drawText("Health: " + tama.getCurrentHealth() + "/" + tama.getMaxHealth(), 25, (playTopBound - 50) / 3, paint);
	canvas.drawText("Hunger: " + tama.getCurrentHunger() + "/" + tama.getMaxHunger(), 25, (playTopBound - 50) / 3 * 2, paint);
	canvas.drawText("Sick: " + tama.getCurrentSickness() + "/" + tama.getMaxSickness(), width / 2, (playTopBound - 50) / 3, paint);
	canvas.drawText("XP: " + tama.getCurrentXP() + "/" + tama.getMaxXP(), width / 2, (playTopBound - 50) / 3 * 2, paint);

    }

    protected void initInterface()
    {
	this.bpRectangle = new Rect(1, playBottomBound + 50, width - 1, height - 1);
	this.topRectangle = new Rect(1, 1, width - 1, playTopBound - 50);
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

    public class PoopThread extends Thread
    {
	private boolean active = true;

	public void run()
	{
	    Log.d(TAG, "Poop thread started.");
	    while (active)
	    {
		try
		{
		    Thread.sleep(5000l);
		    ipo.add(makePoop());
		} catch (Exception e)
		{

		}
	    }
	    Log.d(TAG, "Poop thread ended.");
	}

	public void setRunning(boolean b)
	{
	    active = b;
	}
    }

    public class TamaThread extends Thread
    {
	private boolean active = true;

	public void setRunning(boolean b)
	{
	    active = b;
	}

	public void run()
	{
	    Log.d(TAG, "Tama thread started.");
	    while (active)
	    {
		try
		{
		    Thread.sleep(500l);
		    // check if tamagotchi has died
		    if (tama.isDead())
		    {
			Runnable toastRunnable = new Runnable()
			{
			    public void run()
			    {
				Toast.makeText(context, "Tama is dead", Toast.LENGTH_SHORT).show();
			    }
			};
			toastHandler.post(toastRunnable);
			active = false;
		    }
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }

	    Log.d(TAG, "Tama thread ended.");
	}
    }
}