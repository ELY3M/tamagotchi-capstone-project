package com.tamaproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import com.tamaproject.gameobjects.*;
import com.tamaproject.util.GameObjectUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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

    private Context context = null;
    public final String PREFS_NAME = "GRAPHICS";
    private SharedPreferences settings;

    private Hashtable<String, Bitmap> bitmapTable = new Hashtable<String, Bitmap>();

    private Display display = null;
    private int height = -1, width = -1;

    private int playTopBound, playBottomBound, playLeftBound, playRightBound, cushion;

    private Backpack bp; // backpack with items
    protected Tamagotchi tama; // our tamagotchi
    protected InPlayObjects ipo;

    private Random r = new Random();

    protected Handler handler;

    private Bitmap background;
    private PopupWindow popUp;
    private LinearLayout layout;

    private boolean pooping = true;

    public GameView(Context context)
    {
	super(context);
	// adding the callback (this) to the surface holder to intercept events
	getHolder().addCallback(this);
	handler = new Handler();

	this.context = context;

	settings = context.getSharedPreferences(PREFS_NAME, 0);

	// initialize the height, width, display variables
	initDisplay();

	// initialize bitmaps
	initBitmaps();

	// create dummy items

	ArrayList<Item> items = new ArrayList<Item>();
	items.add(new Item(bitmapTable.get("ic_launcher"), "Health item", 7, 0, 0, 0));
	items.add(new Item(bitmapTable.get("ic_launcher"), "Food item", 7, -20, 0, 0));
	items.add(new Item(bitmapTable.get("ic_launcher"), "XP item", 0, 0, 0, 1000));
	for (int i = 1; i <= 15; i++)
	{
	    items.add(new Item(bitmapTable.get("treasure")));
	}

	bp = new Backpack(items, display, playBottomBound + 45);

	tama = new Tamagotchi(bitmapTable.get("tama"), width / 2, (playTopBound + playBottomBound) / 2);
	tama.setLocked(true);

	ipo = new InPlayObjects();

	initPoop(tama.getPoop());

	initEnvironment();

	initInterface();

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
	this.playBottomBound = height / 3 * 2 - cushion;
	this.playLeftBound = 25;
	this.playRightBound = width - 25;
	this.cushion = 25;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
	Log.d(TAG, "Surface changed...");
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
	Log.d(TAG, "Surface created...");
	// at this point the surface is created and
	// we can safely start the game loop
	// create the game loop thread

	// check if the game was minimized or we're starting for first time
	if (pooping)
	{
	    poopThread = new PoopThread();
	    poopThread.start();
	}

	tamaThread = new TamaThread();
	tamaThread.start();

	thread = new GameLoopThread(getHolder(), this);
	thread.setRunning(true);
	thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
	// Toast.makeText(this.context, tama.toString(), Toast.LENGTH_SHORT).show();
	Log.d(TAG, "Surface is being destroyed...");
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
		    if (!ipo.handleActionDown(ex, ey))
		    {
			tama.handleActionDown(ex, ey);
		    }
		}
	    }

	    // close any open pop ups
	    if (popUp != null)
		popUp.dismiss();

	    // region to open backpack
	    if (ey > height - bp.getOpenSquareSize() && ex > width - bp.getOpenSquareSize())
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
	    else
	    {
		if (bp.isBackpackOpen())
		{
		    bp.setBackpackOpen(false);
		    // this.invalidate();
		}
		bp.refreshItems();
	    }

	    /*
	     * if (GameObjectUtil.isTouching(temp, tama)) { tama.setBitmap(bitmapTable.get(.kuro)); } else { tama.setBitmap(bitmapTable.get(.tama)); }
	     */

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
		if (tama.handleActionUp())
		{
		    if (!tama.isMoved())
		    {
			Log.d(TAG, "Starting voice recognition activity...");
			GameActivity ga = (GameActivity) context;
			ga.startVoiceRecognitionActivity();
		    }

		}
	    }

	    bp.refreshItems();
	}
	return true;
    }

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
	int ty = tama.getY() + cushion;
	int x = r.nextInt(width);
	int y = r.nextInt(playBottomBound - ty) + ty;
	GameObject go = new GameObject(bitmapTable.get("poop"), x, y);
	go.setGroup("poop");
	return go;
    }

    // this method is to demonstrate collisions
    protected boolean giveItem(Tamagotchi tama, Item item)
    {
	if (tama != null && item != null)
	{
	    // tama.setBitmap(bitmapTable.get(.tama));
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
	    if (bp.isBackpackOpen())
	    {
		bp.drawAllItems(canvas);
	    }
	    else
	    {
		canvas.drawBitmap(background, 0, 0, null);
		tama.draw(canvas);
		ipo.draw(canvas);
		bp.draw(canvas);
		drawInterface(canvas);
	    }
	}
    }

    private Paint paint = new Paint();
    private Rect topRectangle;
    private Rect cHealthBar, mHealthBar;
    private int textSize = 20;

    protected void drawInterface(Canvas canvas)
    {
	// draw the rectangle around backpack
	paint.setColor(Color.BLACK);
	paint.setStyle(Style.FILL_AND_STROKE);
	paint.setStrokeWidth(3);
	canvas.drawRect(topRectangle, paint);

	// draw the backpack label and number of items in backpack
	paint.setColor(Color.WHITE);
	paint.setStyle(Style.FILL_AND_STROKE);
	paint.setStrokeWidth(1);
	paint.setTextSize(textSize);
	paint.setAntiAlias(true);

	// draw the health, hunger, sickness
	canvas.drawText("Health: " + tama.getCurrentHealth() + "/" + tama.getMaxHealth(), textSize, (playTopBound - cushion) / 4, paint);
	canvas.drawText("Hunger: " + tama.getCurrentHunger() + "/" + tama.getMaxHunger(), textSize, (playTopBound - cushion) / 4 * 2, paint);
	canvas.drawText("Sick: " + tama.getCurrentSickness() + "/" + tama.getMaxSickness(), width / 2, (playTopBound - cushion) / 4, paint);
	canvas.drawText("XP: " + tama.getCurrentXP() + "/" + tama.getMaxXP(), width / 2, (playTopBound - cushion) / 4 * 2, paint);
	canvas.drawText("Battle Level: " + tama.getBattleLevel(), width / 2, (playTopBound - cushion) / 4 * 3, paint);
	canvas.drawText("Age: " + tama.getAge(), textSize, (playTopBound - cushion) / 4 * 3, paint);

    }

    protected void initInterface()
    {
	// this.bpRectangle = new Rect(1, playBottomBound + cushion, width - 1, height - 1);
	this.topRectangle = new Rect(1, 1, width - 1, playTopBound - cushion);
    }

    private void initEnvironment()
    {
	GameObject trash = new GameObject(bitmapTable.get("trash"), playRightBound, playBottomBound);
	trash.setGroup("trashcan");
	trash.setLocked(true);
	ipo.add(trash);

	this.background = Bitmap.createScaledBitmap(bitmapTable.get("background"), width, height, false);
    }

    // loads up the sprites and bitmaps
    private void initBitmaps()
    {
	AssetManager assetManager = context.getAssets();
	try
	{
	    bitmapTable.put("kuro", BitmapFactory.decodeStream(assetManager.open("game/kuro.png")));
	    bitmapTable.put("ic_launcher", BitmapFactory.decodeStream(assetManager.open("game/ic_launcher.png")));
	    bitmapTable.put("poop", BitmapFactory.decodeStream(assetManager.open("game/poop.png")));
	    bitmapTable.put("tama", BitmapFactory.decodeStream(assetManager.open("game/tama.png")));
	    bitmapTable.put("treasure", BitmapFactory.decodeStream(assetManager.open("game/treasure.png")));
	    bitmapTable.put("trash", BitmapFactory.decodeStream(assetManager.open("game/trash.png")));
	    bitmapTable.put("background", BitmapFactory.decodeStream(assetManager.open("weather/background.png")));
	} catch (IOException e)
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
		    if (!active)
			break;
		    ipo.add(makePoop());

		} catch (Exception e)
		{
		    e.printStackTrace();
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
			handler.post(toastRunnable);
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

    public Backpack getBp()
    {
	return bp;
    }

    public Tamagotchi getTama()
    {
	return tama;
    }

    public InPlayObjects getIpo()
    {
	return ipo;
    }

    public void setBp(Backpack bp)
    {
	this.bp = bp;
    }

    public void setTama(Tamagotchi tama)
    {
	this.tama = tama;
    }

    public void setIpo(InPlayObjects ipo)
    {
	this.ipo = ipo;
    }

    public void onVoiceCommand(ArrayList<String> matches)
    {
	Log.d(TAG, matches.toString());
	if (matches.contains("transform"))
	{
	    if (tama != null)
	    {
		tama.setBitmap(bitmapTable.get("kuro"));
	    }
	}
	else if (matches.contains("go back"))
	{
	    if (tama != null)
	    {
		tama.setBitmap(bitmapTable.get("tama"));
	    }
	}
	else if (matches.contains("stop pooping"))
	{
	    Toast.makeText(this.context, "OK, fine.", Toast.LENGTH_SHORT).show();
	    poopThread.setRunning(false);
	    pooping = false;
	}
    }

}
