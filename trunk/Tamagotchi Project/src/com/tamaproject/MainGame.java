package com.tamaproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.RectangleParticleEmitter;
import org.anddev.andengine.entity.particle.initializer.AccelerationInitializer;
import org.anddev.andengine.entity.particle.initializer.AlphaInitializer;
import org.anddev.andengine.entity.particle.initializer.RotationInitializer;
import org.anddev.andengine.entity.particle.initializer.VelocityInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ColorModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.particle.modifier.ScaleModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.MathUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.widget.Toast;

import com.tamaproject.andengine.entity.Backpack;
import com.tamaproject.andengine.entity.Item;
import com.tamaproject.andengine.entity.Protection;
import com.tamaproject.andengine.entity.Tamagotchi;
import com.tamaproject.util.TextUtil;
import com.tamaproject.util.Weather;
import com.tamaproject.weather.CurrentConditions;
import com.tamaproject.weather.WeatherRetriever;

public class MainGame extends BaseAndEngineGame implements IOnSceneTouchListener,
	IOnAreaTouchListener
{
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int cameraWidth = 480, cameraHeight = 800;
    private static final int pTopBound = 115, pBottomBound = cameraHeight - 70; // top and bottom bounds of play area

    private static final int CONFIRM_APPLYITEM = 0;
    private static final int CONFIRM_QUITGAME = 1;
    private static final int CONFIRM_REMOVEITEM = 2;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final boolean FULLSCREEN = true;
    private static final int MAX_NOTIFICATIONS = 5; // max notifications to display

    // Length of health bars, etc.
    private static final int barLength = 150;
    private static final int barHeight = 25;
    private static final int leftSpacing = 50;
    private static final int vSpacing = 15;
    private static final int iconSpacing = 30;

    private static final int numIcons = 6; // number of icons in the bottom bar
    private static final int iconSpacer = cameraWidth / (numIcons + 1);

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private RepeatingSpriteBackground mGrassBackground;
    private Scene mScene;
    private Backpack bp;

    // Layers
    private Entity mainLayer = new Entity();
    private Entity backpackLayer = new Entity();
    private Entity weatherLayer = new Entity();
    private Entity statsLayer = new Entity();
    private Entity topLayer = new Entity();
    private Entity midLayer = new Entity();
    private Entity bottomLayer = new Entity();
    private List<Entity> subLayers = new ArrayList<Entity>(); // layers that are opened by icons in the bottom bar
    private List<Entity> mainLayers = new ArrayList<Entity>(); // layers that belong to the main gameplay
    private HashMap<Entity, Rectangle> selectBoxes = new HashMap<Entity, Rectangle>(); // mapping of layers to icon select boxes

    private Item takeOut; // item to take out of backpack
    private Item putBack; // item to put back into packpack
    private Item itemToApply; // item to apply to Tama
    private Item itemToRemove;

    private List<BaseSprite> inPlayObjects = new ArrayList<BaseSprite>(); // list of objects that are in the environment
    private Tamagotchi tama; // Tamagotchi
    private Sprite trashCan;
    private PhysicsWorld mPhysicsWorld;
    private ParticleSystem particleSystem;
    private int weather = Weather.NONE;
    private BitmapTextureAtlas mFontTexture, mSmallFontTexture;
    private Font mFont, mSmallFont;

    // Status bars that need to be updated
    private Rectangle currHealthBar, currSicknessBar, currHungerBar;

    // List of in play objects to be removed at next update thread
    private List<BaseSprite> ipoToRemove = new ArrayList<BaseSprite>();

    // Selection boxes for bottom bar

    // TextureRegions
    public Hashtable<String, TextureRegion> listTR = new Hashtable<String, TextureRegion>();
    public List<BitmapTextureAtlas> texturelist = new ArrayList<BitmapTextureAtlas>();
    private String[] fileNames;
    private String[] folderNameArray = new String[] { new String("gfx/") };

    // Weather and GPS fields
    private LocationManager mlocManager;
    private LocationListener mlocListener;
    private long lastWeatherRetrieve = 0;
    private CurrentConditions cc;

    private Rectangle topRect, bottomRect; // top and bottom bars

    private ChangeableText stats;

    private long startPlayTime;
    private long totalPlayTime = 0;

    private Rectangle itemDescriptionRect;
    private ChangeableText itemDesctiptionText;
    private Rectangle notificationRect;
    private ChangeableText notificationText;

    private boolean tamaDeadParticles = false;

    private Rectangle unequipItemButton;
    private ChangeableText backpackLabel;
    private Rectangle backpackBackground;

    private boolean vibrateOn = false;

    private List<String> notificationList = new LinkedList<String>();

    private Rectangle nightOverlayRect;

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine()
    {
	this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
	return new Engine(new EngineOptions(FULLSCREEN, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), this.mCamera));
    }

    @Override
    public void onLoadResources()
    {
	this.mGrassBackground = new RepeatingSpriteBackground(cameraWidth, cameraHeight, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass.png"));
	loadTextures(this, this.mEngine);
	this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mSmallFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mFont = FontFactory.createFromAsset(mFontTexture, this, "ITCKRIST.TTF", 24, true, Color.WHITE);
	this.mSmallFont = FontFactory.createFromAsset(mSmallFontTexture, this, "ITCKRIST.TTF", 18, true, Color.WHITE);
	this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
	this.mEngine.getTextureManager().loadTexture(this.mSmallFontTexture);
	this.mEngine.getFontManager().loadFont(this.mFont);
	this.mEngine.getFontManager().loadFont(this.mSmallFont);

	// Debug.d(listTR.toString());
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

	// Enable vibration
	this.enableVibrator();

	this.mScene = new Scene();
	this.mScene.setBackground(this.mGrassBackground);

	final int centerX = (cameraWidth - this.listTR.get("tama.png").getWidth()) / 2;
	final int centerY = (cameraHeight - this.listTR.get("tama.png").getHeight()) / 2;

	// Load tamagotchi first
	this.tama = new Tamagotchi();
	this.tama.setSprite(new Sprite(centerX, centerY, this.listTR.get("tama.png")));
	tama.getSprite().setScale(0.85f);
	this.mainLayer.attachChild(tama.getSprite());

	final Item eItem = new GameItem(0, 0, this.listTR.get("treasure.png"));
	eItem.setType(Item.EQUIP);
	equipItem(eItem);

	this.mainLayers.add(mainLayer);
	this.mainLayers.add(weatherLayer);
	this.subLayers.add(backpackLayer);
	this.subLayers.add(statsLayer);

	this.mScene.attachChild(bottomLayer);
	this.mScene.attachChild(mainLayer);
	this.mScene.attachChild(weatherLayer);
	this.mScene.attachChild(midLayer);
	this.mScene.attachChild(backpackLayer);
	this.mScene.attachChild(statsLayer);
	this.mScene.attachChild(topLayer);

	this.mainLayer.setVisible(true);
	this.backpackLayer.setVisible(false);
	this.statsLayer.setVisible(false);

	this.bp = new Backpack();
	this.loadItems();

	this.mScene.registerTouchArea(tama.getSprite());

	// Add trash can
	this.trashCan = new Sprite(cameraWidth - listTR.get("trash.png").getWidth(), pBottomBound - listTR.get("trash.png").getHeight(), listTR.get("trash.png"));
	this.mainLayer.attachChild(trashCan);

	// Load interface
	this.loadInterface();

	for (Item item : bp.getItems())
	{
	    this.backpackBackground.attachChild(item);
	    this.mScene.registerTouchArea(item);
	}

	this.mScene.setTouchAreaBindingEnabled(true);
	this.mScene.setOnSceneTouchListener(this);
	this.mScene.setOnAreaTouchListener(this);
	this.mScene.registerUpdateHandler(new IUpdateHandler()
	{
	    @Override
	    public void reset()
	    {
	    }

	    @Override
	    public void onUpdate(final float pSecondsElapsed)
	    {
		try
		{
		    if (ipoToRemove.size() > 0)
		    {
			for (BaseSprite s : ipoToRemove)
			{
			    mainLayer.detachChild(s);
			    mScene.unregisterTouchArea(s);
			    inPlayObjects.remove(s);
			}
		    }

		    if (statsLayer.isVisible())
		    {
			stats.setText(TextUtil.getNormalizedText(mSmallFont, tama.getStats(), stats.getWidth()));
			if (tama.getEquippedItem() != null)
			{
			    unequipItemButton.setPosition(stats.getX(), stats.getY() + stats.getHeight() + 25);
			    unequipItemButton.setVisible(true);
			}
			else
			{
			    unequipItemButton.setVisible(false);
			}
		    }
		    else if (mainLayer.isVisible())
			updateStatusBars();
		    else if (backpackLayer.isVisible())
			backpackLabel.setText("Backpack (" + bp.numItems() + "/" + bp.maxSize() + ")");

		    if (tama.checkStats() == Tamagotchi.DEAD)
		    {
			if (!tamaDeadParticles)
			    showEffect(Tamagotchi.DEAD);
			// Debug.d("Tamagotchi is dead!");
		    }

		} catch (Exception e)
		{
		    Debug.d("onUpdate EXCEPTION:" + e);
		} catch (Error e)
		{
		    Debug.d("onUpdate ERROR:" + e);
		}
	    }
	});

	/**
	 * Timer to run GPS to check weather every hour
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(0, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		runOnUiThread(new Runnable()
		{
		    @Override
		    public void run()
		    {
			startGPS();
		    }
		});
		pTimerHandler.setTimerSeconds(60 * 60);
	    }
	}));

	/**
	 * Timer to check if it is nighttime every hour
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(0, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		GregorianCalendar todaysDate = new GregorianCalendar();
		int hour = todaysDate.get(Calendar.HOUR_OF_DAY);
		if (hour < 17)
		{
		    if (nightOverlayRect != null)
			nightOverlayRect.detachSelf();
		}
		else
		{
		    if (nightOverlayRect == null)
		    {
			nightOverlayRect = new Rectangle(0, 0, cameraWidth, cameraHeight);
			nightOverlayRect.setColor(0, 0, 0, 0.35f);
		    }
		    if (!nightOverlayRect.hasParent())
			bottomLayer.attachChild(nightOverlayRect);
		}
		pTimerHandler.setTimerSeconds(60 * 60);
	    }
	}));

	this.loadTamaTimers();

	this.mScene.setOnAreaTouchTraversalFrontToBack();

	// this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
	// this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	return this.mScene;
    }

    @Override
    public void onLoadComplete()
    {

    }

    /**
     * Specify dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	switch (id)
	{
	case MainGame.CONFIRM_APPLYITEM:
	    builder2.setTitle("Give Item");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to give " + itemToApply.getName() + " to your Tamagotchi?");
	    builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    runOnUpdateThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    Debug.d("Applying item");
			    applyItem(itemToApply);
			    showNotification(itemToApply.getName() + " has been given to your Tamagotchi!");
			    itemToApply = null;
			}
		    }); // End runOnUpdateThread
		    return;
		}
	    });

	    builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    runOnUpdateThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    Debug.d("Putting back item");
			    itemToApply.detachSelf();
			    backpackBackground.attachChild(itemToApply);
			    itemToApply = null;
			}
		    }); // End runOnUpdateThread
		    return;
		}
	    });

	    return builder2.create();

	case MainGame.CONFIRM_QUITGAME:
	    builder2.setTitle("Quit Game");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to quit the game?");
	    builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    finish();
		    return;
		}
	    });

	    builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    return;
		}
	    });

	    return builder2.create();

	case MainGame.CONFIRM_REMOVEITEM:
	    builder2.setTitle("Throw Away Item");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to throw away " + itemToRemove.getName() + "?");
	    builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    runOnUpdateThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    if (itemToRemove != null)
			    {
				itemToRemove.detachSelf();
				bp.removeItem(itemToRemove);
				showNotification(itemToRemove.getName() + " has been removed.");
				itemToRemove = null;
				bp.resetPositions(cameraWidth, cameraHeight);
			    }
			}
		    });
		    return;
		}
	    });

	    builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    runOnUpdateThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    Debug.d("Putting back item");
			    mainLayer.detachChild(itemToRemove);
			    backpackBackground.attachChild(itemToRemove);
			    itemToRemove = null;
			}
		    }); // End runOnUpdateThread
		    return;
		}
	    });

	    return builder2.create();

	}

	return null;
    }

    /**
     * Captures the back key and menu key presses
     */
    @Override
    public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
    {
	// if menu button is pressed
	if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN)
	{
	    return true;
	}
	// if back key was pressed
	else if (pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN)
	{
	    showDialog(MainGame.CONFIRM_QUITGAME);
	    return true;
	}
	return super.onKeyDown(pKeyCode, pEvent);
    }

    @Override
    public void onPause()
    {
	super.onPause();
	this.mEngine.stop();
	totalPlayTime += System.currentTimeMillis() - startPlayTime;
    }

    @Override
    public void onResume()
    {
	super.onResume();
	this.mEngine.start();
	startPlayTime = System.currentTimeMillis();
    }

    @Override
    public void onDestroy()
    {
	super.onDestroy();

	int seconds = (int) (totalPlayTime / 1000) % 60;
	int minutes = (int) ((totalPlayTime / (1000 * 60)) % 60);
	int hours = (int) ((totalPlayTime / (1000 * 60 * 60)) % 24);
	int days = (int) (totalPlayTime / (1000 * 60 * 60 * 24));

	Toast.makeText(this, "Total Playtime: " + days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds", Toast.LENGTH_SHORT).show();

	tama.addToAge(totalPlayTime);

	stopGPS();
	finish();
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
	    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
    {
	if (pSceneTouchEvent.isActionDown())
	{

	}

	return false;
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent)
    {
	if (pSceneTouchEvent.isActionDown())
	{

	}

	return false;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Loads everything related to the interface (icons, bars, menus)
     */
    private void loadInterface()
    {

	final float mid = (cameraHeight - pBottomBound) / 2;

	/**
	 * Load backpack background
	 */
	backpackBackground = new Rectangle(0, 0, cameraWidth, pBottomBound);
	backpackBackground.setColor(87 / 255f, 57 / 255f, 20 / 255f);
	backpackLayer.attachChild(backpackBackground);

	backpackLabel = new ChangeableText(15, 15, mFont, "Backpack (" + bp.numItems() + "/" + bp.maxSize() + ")", HorizontalAlign.LEFT, 30);
	backpackBackground.attachChild(backpackLabel);

	/**
	 * Load stats background
	 */
	final Rectangle statsBackground = new Rectangle(0, 0, cameraWidth, pBottomBound);
	statsBackground.setColor(0, 0, 0);
	statsLayer.attachChild(statsBackground);

	final Text statsLabel = new Text(15, 15, mFont, "Tamagotchi Stats", HorizontalAlign.LEFT);
	statsBackground.attachChild(statsLabel);

	this.stats = new ChangeableText(25, statsLabel.getY() + 50, mSmallFont, tama.getStats(), HorizontalAlign.LEFT, 512);
	this.stats.setWidth(cameraWidth - 150);
	statsBackground.attachChild(stats);

	final Sprite statsSprite = new Sprite(0, 0, tama.getSprite().getTextureRegion());
	statsSprite.setPosition(cameraWidth - statsSprite.getWidth() - 50, statsSprite.getHeight() + 25);
	statsBackground.attachChild(statsSprite);

	final Text unequipItemText = new Text(10, 5, mSmallFont, "Unequip Item");
	unequipItemButton = new Rectangle(50, this.stats.getY() + this.stats.getHeight() + 50, unequipItemText.getWidth() + 20, unequipItemText.getHeight() + 10)
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (statsLayer.isVisible() && this.isVisible())
		{
		    if (pSceneTouchEvent.isActionDown())
			this.setColor(0, 0.659f, 0.698f);
		    else if (pSceneTouchEvent.isActionUp())
		    {
			this.setColor(1, 0.412f, 0.0196f);
			unequipItem();
		    }
		    return true;
		}
		else
		    return false;
	    }
	};
	unequipItemButton.setColor(1, 0.412f, 0.0196f);
	unequipItemButton.attachChild(unequipItemText);
	mScene.registerTouchArea(unequipItemButton);
	statsBackground.attachChild(unequipItemButton);

	/**
	 * Draw bottom rectangle bar
	 */
	bottomRect = new Rectangle(0, pBottomBound, cameraWidth, cameraHeight);
	bottomRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.midLayer.attachChild(bottomRect);

	/**
	 * Draw top rectangle bar
	 */
	topRect = new Rectangle(0, 0, cameraWidth, pTopBound);
	topRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.midLayer.attachChild(topRect);

	/**
	 * Load the open backpack icon
	 */
	createSelectBox(backpackLayer, 1);
	final Sprite openBackpackIcon = new Sprite(iconSpacer * 1 - this.listTR.get("backpack.png").getWidth() / 2, mid - this.listTR.get("backpack.png").getHeight() / 2, this.listTR.get("backpack.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    if (!backpackLayer.isVisible())
			openBackpack();
		    else
			closeBackpack();
		    return true;
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    this.setScale(1);
		}
		return false;
	    }
	};
	bottomRect.attachChild(openBackpackIcon);
	this.mScene.registerTouchArea(openBackpackIcon);

	/**
	 * Load microphone icon
	 */
	final Sprite micIcon = new Sprite(iconSpacer * 2 - this.listTR.get("mic.png").getWidth() / 2, mid - this.listTR.get("mic.png").getHeight() / 2, this.listTR.get("mic.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    closeSubLayers();
		    startVoiceRecognitionActivity();
		    return true;
		}
		return false;
	    }
	};
	bottomRect.attachChild(micIcon);
	this.mScene.registerTouchArea(micIcon);

	/**
	 * Load stats icon
	 */
	createSelectBox(statsLayer, 3);
	final Sprite statsIcon = new Sprite(iconSpacer * 3 - this.listTR.get("statsicon.png").getWidth() / 2, mid - this.listTR.get("statsicon.png").getHeight() / 2, this.listTR.get("statsicon.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    if (!statsLayer.isVisible())
			openLayer(statsLayer);
		    else
			closeSubLayers();

		    return true;
		}
		return false;
	    }
	};
	bottomRect.attachChild(statsIcon);
	this.mScene.registerTouchArea(statsIcon);

	/**
	 * Load minigame icon
	 */
	final Sprite minigameIcon = new Sprite(iconSpacer * 4 - this.listTR.get("statsicon.png").getWidth() / 2, mid - this.listTR.get("toad-icon.png").getHeight() / 2, this.listTR.get("toad-icon.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    showNotification("Minigames are still in development!");
		    return true;
		}
		return false;
	    }
	};
	bottomRect.attachChild(minigameIcon);
	this.mScene.registerTouchArea(minigameIcon);

	TextureRegion temp;
	/**
	 * Load status bars
	 */
	final Text titleText = new Text(leftSpacing - 15, vSpacing, mFont, "Tamagotchi");
	topRect.attachChild(titleText);

	final Rectangle healthBar = new Rectangle(leftSpacing, titleText.getY() + titleText.getHeight() + vSpacing, barLength, barHeight);
	healthBar.setColor(1, 1, 1);
	topRect.attachChild(healthBar);

	final Rectangle hungerBar = new Rectangle(cameraWidth - barLength - leftSpacing, vSpacing, barLength, barHeight);
	hungerBar.setColor(1, 1, 1);
	topRect.attachChild(hungerBar);

	final Rectangle sicknessBar = new Rectangle(hungerBar.getX(), healthBar.getY(), barLength, barHeight);
	sicknessBar.setColor(1, 1, 1);
	topRect.attachChild(sicknessBar);

	/**
	 * Load health bar
	 */
	float ratio = tama.getCurrentHealth() / tama.getMaxHealth();
	// Debug.d("Tama health ratio: " + ratio);
	this.currHealthBar = new Rectangle(2, 2, ratio * (barLength - 4), barHeight - 4);
	currHealthBar.setColor(1, 0, 0);
	healthBar.attachChild(currHealthBar);

	/**
	 * Load health icon
	 */
	temp = listTR.get("heart.png");
	final Sprite healthIcon = new Sprite(healthBar.getX() - iconSpacing, healthBar.getY(), temp);
	topRect.attachChild(healthIcon);

	/**
	 * Load sickness bar
	 */
	ratio = tama.getCurrentSickness() / tama.getMaxSickness();
	// Debug.d("Tama sick ratio: " + ratio);
	this.currSicknessBar = new Rectangle(2, 2, ratio * (barLength - 4), barHeight - 4);
	currSicknessBar.setColor(1, 0, 0);
	sicknessBar.attachChild(currSicknessBar);

	/**
	 * Load sickness icon
	 */
	temp = listTR.get("sick.png");
	final Sprite sickIcon = new Sprite(sicknessBar.getX() - iconSpacing, sicknessBar.getY(), temp);
	topRect.attachChild(sickIcon);

	/**
	 * Load hunger bar
	 */
	ratio = tama.getCurrentHunger() / tama.getMaxHunger();
	// Debug.d("Tama hunger ratio: " + ratio);
	this.currHungerBar = new Rectangle(2, 2, ratio * (barLength - 4), barHeight - 4);
	currHungerBar.setColor(1, 0, 0);
	hungerBar.attachChild(currHungerBar);

	/**
	 * Load hunger icon
	 */
	temp = listTR.get("food.png");
	final Sprite hungerIcon = new Sprite(hungerBar.getX() - iconSpacing, hungerBar.getY(), temp);
	topRect.attachChild(hungerIcon);

	/**
	 * Load item description box
	 */
	this.itemDescriptionRect = new Rectangle(10, cameraHeight / 2, cameraWidth - 20, Math.round(cameraHeight / 2 - (cameraHeight - pBottomBound) - 10));
	this.itemDescriptionRect.setColor(0, 0, 0);
	this.itemDescriptionRect.setVisible(false);
	this.topLayer.attachChild(itemDescriptionRect);

	/**
	 * Add text to item description box
	 */
	this.itemDesctiptionText = new ChangeableText(10, 10, mSmallFont, "", 512);
	this.itemDescriptionRect.attachChild(this.itemDesctiptionText);

	/**
	 * Load notification box
	 */
	this.notificationRect = new Rectangle(0, pTopBound, cameraWidth, 50);
	this.notificationRect.setColor(0, 0, 0);
	this.notificationRect.setVisible(false);
	this.midLayer.attachChild(notificationRect);

	/**
	 * Add text to notification box
	 */
	this.notificationText = new ChangeableText(10, 10, mSmallFont, "", 512);
	this.notificationRect.attachChild(notificationText);

	/**
	 * Add close button
	 */
	final Sprite closeButton = new Sprite(this.itemDescriptionRect.getWidth() - listTR.get("close.png").getWidth(), 0, listTR.get("close.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (this.getParent().isVisible())
		{
		    hideItemDescription();
		    return true;
		}
		else
		    return false;
	    }
	};
	this.itemDescriptionRect.attachChild(closeButton);
	this.mScene.registerTouchArea(closeButton);

	/**
	 * Add close button for notification
	 */
	final Sprite notifyCloseButton = new Sprite(this.notificationRect.getWidth() - listTR.get("close.png").getWidth(), 0, listTR.get("close.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (this.getParent().isVisible())
		{
		    notificationRect.setVisible(false);
		    notificationText.setText("");
		    notificationList = new LinkedList<String>();
		    return true;
		}
		else
		    return false;
	    }
	};
	this.notificationRect.attachChild(notifyCloseButton);
	this.mScene.registerTouchArea(notifyCloseButton);
    }

    /**
     * Updates the status bars with Tama info
     */
    private void updateStatusBars()
    {
	float ratio = tama.getCurrentHealth() / tama.getMaxHealth();
	// Debug.d("Tama health ratio: " + ratio);
	this.currHealthBar.setSize(ratio * (barLength - 4), barHeight - 4);

	ratio = tama.getCurrentSickness() / tama.getMaxSickness();
	// Debug.d("Tama sick ratio: " + ratio);
	this.currSicknessBar.setSize(ratio * (barLength - 4), barHeight - 4);

	ratio = tama.getCurrentHunger() / tama.getMaxHunger();
	// Debug.d("Tama hunger ratio: " + ratio);
	this.currHungerBar.setSize(ratio * (barLength - 4), barHeight - 4);
    }

    /**
     * Adds poop to scene at specified coordinates
     * 
     * @param pX
     * @param pY
     */
    public void addPoop(final float pX, final float pY)
    {
	final Sprite poop = new Sprite(pX, pY, this.listTR.get("poop.png"))
	{
	    private boolean touched = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();

		// don't respond to touch unless sprite's parent is visible
		if (this.getParent().isVisible())
		{
		    Debug.d("Touched poop");
		    if (pSceneTouchEvent.isActionDown())
		    {
			touched = true;
		    }
		    else if (pSceneTouchEvent.isActionMove())
		    {
			if (touched)
			{
			    if (y < pTopBound)
				this.setPosition(x - this.getWidth() / 2, pTopBound - this.getHeight() / 2);
			    else if (y > pBottomBound)
				this.setPosition(x - this.getWidth() / 2, pBottomBound - this.getHeight() / 2);
			    else
				this.setPosition(x - this.getWidth() / 2, y - this.getHeight() / 2);

			    if (this.collidesWith(trashCan))
				trashCan.setScale(1.5f);
			    else
				trashCan.setScale(1);
			}
			else
			{
			    return false;
			}
		    }
		    else if (pSceneTouchEvent.isActionUp())
		    {
			touched = false;
			if (this.collidesWith(trashCan))
			{
			    ipoToRemove.add(this);
			    trashCan.setScale(1);
			}
		    }
		    return true;
		}
		else
		{
		    return false;
		}
	    }
	};
	poop.setUserData("poop");
	float scale = MathUtils.random(0.75f, (2.0f - 0.75f));
	poop.setScale(scale);
	inPlayObjects.add(poop);

	this.mainLayer.attachChild(poop);
	this.mScene.registerTouchArea(poop);
    }

    /**
     * This method just adds a bunch of dummy items to the backpack
     */
    private void loadItems()
    {
	for (int i = 0; i < 26; i++)
	{
	    Item item = new GameItem(0, 0, this.listTR.get("treasure.png"), "Health item", 7, 0, 0, 0);
	    this.bp.addItem(item);
	}

	final Item umbrella = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Umbrella", "This item protects the Tamagotchi from the rain. blah blah blah more text lol", 0, 0, 0, 0);
	umbrella.setType(Item.EQUIP);
	umbrella.setProtection(Protection.RAIN);
	this.bp.addItem(umbrella);

	final Item newItem = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Level item", 7, 0, 0, 10000);
	this.bp.addItem(newItem);

	final Item cureAll = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Cure All", 0, -10000, -10000, 0);
	this.bp.addItem(cureAll);

	final Item killTama = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Kill Tama", "This item kills the Tamagotchi.", -10000, 0, 0, 0);
	this.bp.addItem(killTama);

	bp.resetPositions(cameraWidth, cameraHeight);
    }

    /**
     * Applies an item to the Tamagotchi and removes the item from the backpack. It also detaches the item from the scene and unregisters its touch area.
     * 
     * @param item
     *            Item to be applied
     */
    private void applyItem(Item item)
    {
	int tamaStatus = this.tama.applyItem(item);
	showEffect(tamaStatus);

	this.bp.removeItem(item);
	this.mainLayer.detachChild(item);
	this.mScene.unregisterTouchArea(item);
    }

    private void openBackpack()
    {
	this.openLayer(backpackLayer);
	this.bp.resetPositions(cameraWidth, cameraHeight);
    }

    private void closeBackpack()
    {
	this.closeSubLayers();
	this.bp.resetPositions(cameraWidth, cameraHeight);
    }

    /**
     * Generates the pop up that shows the item description
     * 
     * @param i
     *            - the item that was selected
     */
    private void showItemDescription(Item i)
    {
	String normalizedText = TextUtil.getNormalizedText(mSmallFont, i.getInfo(), this.itemDescriptionRect.getWidth() - 20);
	this.itemDesctiptionText.setText(normalizedText);
	this.itemDescriptionRect.setHeight(this.itemDesctiptionText.getHeight());
	this.itemDescriptionRect.setPosition(this.itemDescriptionRect.getX(), pBottomBound - this.itemDescriptionRect.getHeight() - 10);
	this.itemDescriptionRect.setVisible(true);
    }

    private void hideItemDescription()
    {
	this.itemDescriptionRect.setVisible(false);
    }

    /**
     * Starts gps listener if connected to internet
     */
    private void startGPS()
    {
	Debug.d("Starting GPS...");
	if (isNetworkAvailable())
	{
	    mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    mlocListener = new MyLocationListener();
	    mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
	}
	else
	{
	    Debug.d("Internet connection not available, not starting GPS.");
	}
    }

    /**
     * Stops gps listener if gps listener is active
     */
    private void stopGPS()
    {
	Debug.d("Stopping GPS...");
	if (mlocManager != null)
	    try
	    {
		mlocManager.removeUpdates(mlocListener);
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }
    }

    /**
     * Checks to see if Android is connected to the internet *
     * 
     * @return if connected
     */
    private boolean isNetworkAvailable()
    {
	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	return activeNetworkInfo != null;
    }

    /**
     * Loads all bitmaps into TextureRegions from the gfx folder into hashtable with file name as the key
     * 
     * @param context
     * @param pEngine
     */
    public void loadTextures(Context context, Engine pEngine)
    {
	BitmapFactory.Options opt = new BitmapFactory.Options();
	opt.inJustDecodeBounds = true;

	for (int i = 0; i < folderNameArray.length; i++)
	{
	    BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(folderNameArray[i]);
	    try
	    {
		fileNames = context.getResources().getAssets().list(folderNameArray[i].substring(0, folderNameArray[i].lastIndexOf("/")));
		Arrays.sort(fileNames);
		for (int j = 0; j < fileNames.length; j++)
		{

		    String rscPath = folderNameArray[i].concat(fileNames[j]);
		    InputStream in = context.getResources().getAssets().open(rscPath);
		    BitmapFactory.decodeStream(in, null, opt);

		    int width = opt.outWidth;
		    int height = opt.outHeight;

		    boolean flag = MathUtils.isPowerOfTwo(width);

		    if (!flag)
		    {
			width = MathUtils.nextPowerOfTwo(opt.outWidth);
		    }
		    flag = MathUtils.isPowerOfTwo(height);
		    if (!flag)
		    {
			height = MathUtils.nextPowerOfTwo(opt.outHeight);
		    }
		    texturelist.add(new BitmapTextureAtlas(width, height, TextureOptions.BILINEAR_PREMULTIPLYALPHA));

		    listTR.put(fileNames[j], BitmapTextureAtlasTextureRegionFactory.createFromAsset(texturelist.get(j), context, fileNames[j], 0, 0));
		    pEngine.getTextureManager().loadTexture(texturelist.get(j));
		}
	    } catch (IOException e)
	    {
		e.printStackTrace();
		return;
	    }
	}
	context = null;
    }

    /**
     * Loads the selected weather into the environment.
     * 
     * @param type
     *            Specified by Weather class. (e.g. Weather.SNOW, Weather.RAIN, Weather.NONE)
     */
    private void loadWeather(int type)
    {
	if (particleSystem != null)
	{
	    this.weatherLayer.detachChild(particleSystem);
	    this.particleSystem = null;
	}

	if (type == Weather.SNOW)
	{
	    weather = type;
	    final RectangleParticleEmitter particleEmitter = new RectangleParticleEmitter(cameraWidth / 2, pTopBound, cameraWidth, 1);
	    this.particleSystem = new ParticleSystem(particleEmitter, 6, 10, 200, listTR.get("snowflake.png"));
	    particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

	    particleSystem.addParticleInitializer(new VelocityInitializer(-10, 10, 60, 90));
	    particleSystem.addParticleInitializer(new AccelerationInitializer(5, 15));
	    particleSystem.addParticleInitializer(new RotationInitializer(0.0f, 360.0f));

	    particleSystem.addParticleModifier(new ExpireModifier(11.5f));
	    this.weatherLayer.attachChild(particleSystem);
	}
	else if (type == Weather.RAIN)
	{
	    weather = type;
	    final RectangleParticleEmitter particleEmitter = new RectangleParticleEmitter(cameraWidth / 2, pTopBound, cameraWidth, 1);
	    this.particleSystem = new ParticleSystem(particleEmitter, 6, 10, 200, listTR.get("raindrop.png"));
	    particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

	    particleSystem.addParticleInitializer(new VelocityInitializer(0, 0, 60, 90));
	    particleSystem.addParticleInitializer(new AccelerationInitializer(0, 15));

	    particleSystem.addParticleModifier(new ExpireModifier(11.5f));
	    this.weatherLayer.attachChild(particleSystem);
	}
	else
	{
	    weather = Weather.NONE;
	}
    }

    /**
     * Voice recognition system, starts the Activity that shows the voice prompt
     */
    public void startVoiceRecognitionActivity()
    {
	if (isNetworkAvailable())
	{
	    try
	    {
		this.mEngine.stop();
		Debug.d("Starting voice recognition");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// uses free form text input
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// Puts a customized message to the prompt
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	    } catch (Exception e)
	    {
		Toast.makeText(this, "Error! Cannot start voice command", Toast.LENGTH_SHORT).show();
	    }
	}
	else
	{
	    Toast.makeText(this, "Cannot start voice commands, there is no internet connection", Toast.LENGTH_SHORT).show();
	}
    }

    /**
     * Handles the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
	{
	    Debug.d("Interpret results");
	    // Fill the list view with the strings the recognizer thought it could have heard
	    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    if (matches.contains("toggle snow"))
	    {
		if (weather == Weather.SNOW)
		{
		    loadWeather(Weather.NONE);
		}
		else
		{
		    loadWeather(Weather.SNOW);
		}
	    }
	    else if (matches.contains("toggle rain"))
	    {
		if (weather == Weather.RAIN)
		{
		    loadWeather(Weather.NONE);
		}
		else
		{
		    loadWeather(Weather.RAIN);
		}
	    }
	    else if (matches.contains("remove poop"))
	    {
		for (Entity e : inPlayObjects)
		{
		    ipoToRemove.add((Sprite) e);
		}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	    this.mEngine.start();
	}
    }

    /**
     * Opens the specified sublayer and hides the other layers
     * 
     * @param layer
     *            subLayer to be opened
     * @return
     */
    private boolean openLayer(Entity layer)
    {
	try
	{
	    layer.setVisible(true);
	    layer.setChildrenVisible(true);
	    selectBoxes.get(layer).setVisible(true);
	    for (Entity e : subLayers)
	    {
		if (!e.equals(layer))
		{
		    e.setVisible(false);
		    e.setChildrenVisible(false);
		    selectBoxes.get(e).setVisible(false);
		}
	    }

	    for (Entity e : mainLayers)
	    {
		e.setVisible(false);
		e.setChildrenVisible(false);
	    }

	    hideItemDescription();

	    return true;
	} catch (Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    /**
     * Closes all subLayers and sets the main layers to visible
     */
    private void closeSubLayers()
    {
	for (Entity e : subLayers)
	{
	    e.setVisible(false);
	    e.setChildrenVisible(false);
	    try
	    {
		selectBoxes.get(e).setVisible(false);
	    } catch (Exception ex)
	    {

	    }
	}

	for (Entity e : mainLayers)
	{
	    e.setVisible(true);
	    e.setChildrenVisible(true);
	}

	hideItemDescription();
    }

    /**
     * Creates a select box for an icon, which is mapped to the sublayer that the icon opens. Should be created before the icon is created.
     * 
     * @param entity
     *            The subLayer that the icon is meant to open
     * @param index
     *            The icon's placement in the bottom bar. (starting at 1)
     */
    private void createSelectBox(Entity entity, int index)
    {
	final Rectangle selectBox = new Rectangle(iconSpacer * index - 25f, 0, 50, bottomRect.getHeight());
	selectBox.setColor(1, 1, 1);
	selectBox.setVisible(false);
	selectBoxes.put(entity, selectBox);
	bottomRect.attachChild(selectBox);
    }

    /**
     * Equips item to tamagotchi.
     * 
     * @param item
     *            Item to be equipped
     * @return true if successfully equipped, false otherwise
     */
    private boolean equipItem(Item item)
    {
	itemToApply = item;
	runOnUpdateThread(new Runnable()
	{
	    public void run()
	    {
		if (!unequipItem())
		{
		    Debug.d("Could not unequip item!");
		    mainLayer.detachChild(itemToApply);
		    backpackBackground.attachChild(itemToApply);
		    itemToApply = null;
		    return;
		}
		try
		{
		    itemToApply.detachSelf(); // detach item from any other entities
		    mScene.unregisterTouchArea(itemToApply); // try to unregister to touch area
		} catch (Exception e)
		{
		    // item was not previously registered with touch
		}

		try
		{
		    bp.removeItem(itemToApply); // try to remove from backpack
		} catch (Exception e)
		{
		    // item was not taken from backpack
		}

		tama.setEquippedItem(itemToApply);
		itemToApply.setPosition(tama.getSprite().getBaseWidth() - 25, tama.getSprite().getBaseHeight() - 25);
		tama.getSprite().attachChild(itemToApply);
		showNotification(itemToApply.getName() + " has been equipped!");
		itemToApply = null;
	    }

	});
	return true;
    }

    /**
     * Unequips item from tamagotchi.
     * 
     * @return true if unequipped, false otherwise
     */
    private boolean unequipItem()
    {
	try
	{
	    Item previousItem = tama.getEquippedItem();

	    if (previousItem == null)
		return true;

	    if (!bp.addItem(previousItem))
	    {
		showNotification("Backpack is full!");
		return false;
	    }
	    tama.getSprite().detachChild(previousItem);
	    backpackBackground.attachChild(previousItem);
	    mScene.registerTouchArea(previousItem);
	    showNotification(previousItem.getName() + " has been unequipped!");
	    tama.setEquippedItem(null);
	} catch (Exception e)
	{
	    return false;
	}
	return true;
    }

    private void showEffect(int status)
    {
	if (status == Tamagotchi.LEVEL_UP)
	{
	    final CircleOutlineParticleEmitter particleEmitter = new CircleOutlineParticleEmitter(tama.getSprite().getWidth() / 2, tama.getSprite().getHeight() / 2, 60);
	    final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 25, 25, 360, listTR.get("particle_point.png"));
	    particleSystem.addParticleInitializer(new AlphaInitializer(0));
	    particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
	    particleSystem.addParticleInitializer(new VelocityInitializer(-2, 2, -20, -10));
	    particleSystem.addParticleInitializer(new RotationInitializer(0.0f, 360.0f));

	    particleSystem.addParticleModifier(new ScaleModifier(1.0f, 2.0f, 0, 5));
	    particleSystem.addParticleModifier(new ColorModifier(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 2f));
	    particleSystem.addParticleModifier(new AlphaModifier(0, 1, 0, 1));
	    particleSystem.addParticleModifier(new AlphaModifier(1, 0, 5, 6));
	    particleSystem.addParticleModifier(new ExpireModifier(2, 2));
	    tama.getSprite().attachChild(particleSystem);
	    mScene.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback()
	    {
		@Override
		public void onTimePassed(final TimerHandler pTimerHandler)
		{
		    particleSystem.setParticlesSpawnEnabled(false);
		    mScene.unregisterUpdateHandler(pTimerHandler);
		}

	    }));
	    mScene.registerUpdateHandler(new TimerHandler(5f, new ITimerCallback()
	    {
		@Override
		public void onTimePassed(final TimerHandler pTimerHandler)
		{
		    mScene.detachChild(particleSystem);
		    mScene.unregisterUpdateHandler(pTimerHandler);
		}
	    }));
	    showNotification("Tamagotchi has leveled up!");
	}
	else if (status == Tamagotchi.DEAD)
	{
	    final RectangleParticleEmitter particleEmitter = new RectangleParticleEmitter(cameraWidth / 2, pBottomBound, cameraWidth, 1);
	    final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 1, 10, 100, listTR.get("particle_point.png"));
	    particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

	    particleSystem.addParticleInitializer(new VelocityInitializer(-10, 10, -60, -90));
	    particleSystem.addParticleInitializer(new AccelerationInitializer(5, -15));
	    particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
	    particleSystem.addParticleModifier(new ScaleModifier(1.0f, 2.0f, 0, 5));
	    particleSystem.addParticleModifier(new ColorModifier(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 2f));
	    particleSystem.addParticleModifier(new AlphaModifier(0, 1, 0, 1));
	    particleSystem.addParticleModifier(new AlphaModifier(1, 0, 5, 6));
	    particleSystem.addParticleModifier(new ExpireModifier(11.5f));
	    mainLayer.attachChild(particleSystem);

	    final CircleOutlineParticleEmitter tamaParticleEmitter = new CircleOutlineParticleEmitter(tama.getSprite().getWidth() / 2, tama.getSprite().getHeight() / 2, 60);
	    final ParticleSystem tamaParticleSystem = new ParticleSystem(tamaParticleEmitter, 5, 5, 100, listTR.get("particle_point.png"));
	    tamaParticleSystem.addParticleInitializer(new AlphaInitializer(0));
	    tamaParticleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
	    tamaParticleSystem.addParticleInitializer(new VelocityInitializer(-2, 2, -20, -10));
	    tamaParticleSystem.addParticleInitializer(new RotationInitializer(0.0f, 360.0f));

	    tamaParticleSystem.addParticleModifier(new ScaleModifier(1.0f, 2.0f, 0, 5));
	    tamaParticleSystem.addParticleModifier(new ColorModifier(1.0f, 0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 2f));
	    tamaParticleSystem.addParticleModifier(new AlphaModifier(0, 1, 0, 1));
	    tamaParticleSystem.addParticleModifier(new AlphaModifier(1, 0, 5, 6));
	    tamaParticleSystem.addParticleModifier(new ExpireModifier(5f));
	    tama.getSprite().attachChild(tamaParticleSystem);

	    tamaDeadParticles = true;

	    mScene.registerUpdateHandler(new TimerHandler(10f, new ITimerCallback()
	    {
		@Override
		public void onTimePassed(final TimerHandler pTimerHandler)
		{
		    tamaParticleSystem.setParticlesSpawnEnabled(false);
		    particleSystem.setParticlesSpawnEnabled(false);
		    tama.getSprite().setVisible(false);
		    mScene.unregisterUpdateHandler(pTimerHandler);
		}

	    }));
	    mScene.registerUpdateHandler(new TimerHandler(15f, new ITimerCallback()
	    {
		@Override
		public void onTimePassed(final TimerHandler pTimerHandler)
		{
		    tamaParticleSystem.detachSelf();
		    particleSystem.detachSelf();
		    tama.getSprite().detachSelf();
		    mScene.unregisterUpdateHandler(pTimerHandler);
		}
	    }));

	    showNotification("Tamagotchi has passed away!");
	}
    }

    private void showNotification(String text)
    {
	notificationList.add(TextUtil.getNormalizedText(mSmallFont, text, this.notificationRect.getWidth()));

	while (notificationList.size() > MAX_NOTIFICATIONS)
	    notificationList.remove(0);

	StringBuilder n = new StringBuilder();

	for (String s : notificationList)
	{
	    n.append(s);
	}

	if (vibrateOn)
	    this.mEngine.vibrate(500l);

	this.notificationText.setText(n.toString());
	this.notificationRect.setHeight(this.notificationText.getHeight());
	this.notificationRect.setVisible(true);
	closeSubLayers();
    }

    private void loadTamaTimers()
    {
	/**
	 * Timer to generate poop
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(5 * 60, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		if (tama.getStatus() != Tamagotchi.DEAD)
		{
		    final float xPos = MathUtils.random(30.0f, (cameraWidth - 30.0f));
		    final float yPos = MathUtils.random(pTopBound, (pBottomBound - pTopBound));
		    addPoop(xPos, yPos);
		}
	    }
	}));

	/**
	 * Checks to see if Tamagotchi is prepared for weather every 5 minutes, if it isn't, increase sickness
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(5 * 60, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		if (tama.getStatus() != Tamagotchi.DEAD)
		{
		    if (tama.getEquippedItem() != null)
		    {
			// if equipped item doesn't protect tama from current weather and current weather is not clear
			if (tama.getEquippedItem().getProtection() != weather && weather != Weather.NONE)
			{
			    tama.setCurrentSickness(tama.getCurrentSickness() + tama.getMaxSickness() * .05f);
			}
		    }
		    else
		    {
			if (weather != Weather.NONE)
			{
			    tama.setCurrentSickness(tama.getCurrentSickness() + tama.getMaxSickness() * .05f);
			}
		    }
		}
	    }
	}));

	/**
	 * Handling health regeneration
	 */
	final float battleLevelRatio = 1 - (float) tama.getBattleLevel() / Tamagotchi.MAX_BATTLE_LEVEL;
	final float initialTime = battleLevelRatio * 60;
	this.mScene.registerUpdateHandler(new TimerHandler(initialTime, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		if (tama.getStatus() != Tamagotchi.DEAD)
		{
		    if (tama.getCurrentSickness() < 1 && tama.getCurrentHunger() < tama.getMaxHunger())
		    {
			if (tama.getCurrentHealth() < tama.getMaxHealth())
			{
			    final float battleLevelRatio = 1 - (float) tama.getBattleLevel() / Tamagotchi.MAX_BATTLE_LEVEL;
			    final float hungerRatio = 1 - tama.getCurrentHunger() / tama.getMaxHunger();
			    tama.setCurrentHealth(tama.getCurrentHealth() + hungerRatio * 0.05f * tama.getMaxHealth());
			    pTimerHandler.setTimerSeconds(battleLevelRatio * 60);
			}

		    }
		}
	    }
	}));

	/**
	 * Handling hunger
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(5 * 60, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		if (tama.getStatus() != Tamagotchi.DEAD)
		{
		    if (tama.getCurrentHunger() >= tama.getMaxHunger() && tama.getCurrentHealth() > 0)
		    {
			tama.setCurrentHealth(tama.getCurrentHealth() - 0.05f * tama.getMaxHealth());
		    }
		    else
		    {
			tama.setCurrentHunger(tama.getCurrentHunger() + 0.05f * tama.getMaxHunger());
		    }
		}
	    }
	}));

	Debug.d("Tama timers loaded.");
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * Same as Item class but overrides the onAreaTouched() method to work with game
     * 
     * @author Jonathan
     * 
     */
    private class GameItem extends Item
    {
	public GameItem(float x, float y, TextureRegion textureRegion)
	{
	    super(x, y, textureRegion);
	}

	public GameItem(float x, float y, TextureRegion textureRegion, String name, int health,
		int hunger, int sickness, int xp)
	{
	    super(x, y, textureRegion, name, health, hunger, sickness, xp);
	}

	public GameItem(float x, float y, TextureRegion textureRegion, String name,
		String description, int health, int hunger, int sickness, int xp)
	{
	    super(x, y, textureRegion, name, description, health, hunger, sickness, xp);
	}

	private boolean touched = false;
	private boolean moved = false;

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	{
	    if (this.getParent().getParent().isVisible())
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    Debug.d("Item action down");
		    touched = true;
		    this.setScale(1.5f);
		}
		else if (pSceneTouchEvent.isActionMove())
		{
		    Debug.d("Item action move");
		    if (touched)
		    {
			if (this.getParent().equals(backpackBackground))
			{
			    takeOut = this;
			    runOnUpdateThread(new Runnable()
			    {
				@Override
				public void run()
				{
				    Debug.d("Taking out item");
				    takeOut.detachSelf();
				    mainLayer.attachChild(takeOut);
				    takeOut = null;
				}
			    });
			    closeBackpack();
			}
			this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);

			moved = true;
			return true;
		    }
		    else
		    {
			return false;
		    }
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    Debug.d("Item action up");
		    touched = false;
		    this.setScale(1);
		    if (moved)
		    {
			moved = false;
			if (this.getParent().equals(mainLayer))
			{
			    if (this.collidesWith(tama.getSprite()))
			    {
				if (this.getType() == Item.NORMAL)
				{
				    itemToApply = this;
				    showDialog(MainGame.CONFIRM_APPLYITEM);
				}
				else if (this.getType() == Item.EQUIP)
				{
				    equipItem(this);
				}
			    }
			    else if (this.collidesWith(trashCan))
			    {
				itemToRemove = this;
				showDialog(MainGame.CONFIRM_REMOVEITEM);
			    }
			    else
			    {
				putBack = this;
				runOnUpdateThread(new Runnable()
				{
				    @Override
				    public void run()
				    {
					Debug.d("Putting back item");
					putBack.detachSelf();
					backpackBackground.attachChild(putBack);
					putBack = null;
				    }
				}); // End runOnUpdateThread
			    }
			}
		    }
		    else
		    {
			// show item description
			showItemDescription(this);
		    }
		}
		return true;
	    }

	    return false;
	}
    }

    /**
     * GPS Location Listener
     */
    public class MyLocationListener implements LocationListener
    {
	@Override
	public void onLocationChanged(Location loc)
	{
	    double lat = loc.getLatitude();
	    double lon = loc.getLongitude();
	    String Text = "My current location is: " + "Latitude = " + loc.getLatitude() + ", Longitude = " + loc.getLongitude();
	    Debug.d(Text);
	    cc = WeatherRetriever.getCurrentConditions(lat, lon);
	    if (cc != null)
	    {
		Debug.d(cc.toString());
		/**
		 * For debugging, display current weather
		 */
		final Text weatherText = new Text(0, pBottomBound - 60, mFont, cc.getCondition() + ", " + cc.getTempF() + "F", HorizontalAlign.LEFT);
		mainLayer.attachChild(weatherText);

		// Parse the result and see if there is rain or snow
		int weatherType = Weather.NONE;
		String condition = cc.getCondition().toLowerCase();

		if (condition.contains("rain"))
		    weatherType = Weather.RAIN;
		else if (condition.contains("snow"))
		    weatherType = Weather.SNOW;

		loadWeather(weatherType);

		// Stop GPS listener to save battery
		lastWeatherRetrieve = System.currentTimeMillis();
		stopGPS();
	    }
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	    Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	    Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}
    }

}