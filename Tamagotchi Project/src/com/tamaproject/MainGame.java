package com.tamaproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
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
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tamaproject.andengine.entity.Backpack;
import com.tamaproject.andengine.entity.Item;
import com.tamaproject.andengine.entity.Protection;
import com.tamaproject.andengine.entity.Tamagotchi;
import com.tamaproject.util.Weather;
import com.tamaproject.weather.CurrentConditions;
import com.tamaproject.weather.WeatherRetriever;

public class MainGame extends BaseAndEngineGame implements IOnSceneTouchListener,
	IOnAreaTouchListener
{
    // ===========================================================
    // Constants
    // ===========================================================

    private final int cameraWidth = 480, cameraHeight = 800;
    private static final int CONFIRM_APPLYITEM = 0;
    private static final int CONFIRM_QUITGAME = 1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final boolean FULLSCREEN = true;

    // Length of health bars, etc.
    private final float barLength = 150;
    private final float barHeight = 25;
    private final float leftSpacing = 50;
    private final float vSpacing = 15;
    private float iconSpacing = 30;

    private final int numIcons = 6; // number of icons in the bottom bar
    private final int iconSpacer = cameraWidth / (numIcons + 1);

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
    private List<Entity> subLayers = new ArrayList<Entity>(); // layers that are opened by icons in the bottom bar
    private List<Entity> mainLayers = new ArrayList<Entity>(); // layers that belong to the main gameplay
    private HashMap<Entity, Rectangle> selectBoxes = new HashMap<Entity, Rectangle>(); // mapping of layers to icon select boxes

    private Item takeOut; // item to take out of backpack
    private Item putBack; // item to put back into packpack
    private Item itemToApply; // item to apply to Tama

    private List<BaseSprite> inPlayObjects = new ArrayList<BaseSprite>(); // list of objects that are in the environment
    private Tamagotchi tama; // Tamagotchi
    private float pTopBound, pBottomBound; // top and bottom bounds of play area
    private Sprite trashCan;
    private PopupWindow popUp;
    private LinearLayout layout;
    private PhysicsWorld mPhysicsWorld;
    private ParticleSystem particleSystem;
    private int weather = Weather.NONE;
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;

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

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine()
    {
	this.pTopBound = 100;
	this.pBottomBound = cameraHeight - 60;
	this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
	return new Engine(new EngineOptions(FULLSCREEN, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), this.mCamera));
    }

    @Override
    public void onLoadResources()
    {
	this.mGrassBackground = new RepeatingSpriteBackground(cameraWidth, cameraHeight, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass.png"));
	loadTextures(this, this.mEngine);
	this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, true, Color.WHITE);
	this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
	this.mEngine.getFontManager().loadFont(this.mFont);

	Debug.d(listTR.toString());
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

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

	// Load interface
	this.loadInterface();

	this.mainLayers.add(mainLayer);
	this.mainLayers.add(weatherLayer);
	this.subLayers.add(backpackLayer);
	this.subLayers.add(statsLayer);

	this.mScene.attachChild(mainLayer);
	this.mScene.attachChild(weatherLayer);
	this.mScene.attachChild(backpackLayer);
	this.mScene.attachChild(statsLayer);

	this.mainLayer.setVisible(true);
	this.backpackLayer.setVisible(false);
	this.statsLayer.setVisible(false);

	this.bp = new Backpack();
	this.loadItems();
	for (Item item : bp.getItems())
	{
	    this.backpackLayer.attachChild(item);
	    this.mScene.registerTouchArea(item);
	}

	this.mScene.registerTouchArea(tama.getSprite());

	// Add trash can
	this.trashCan = new Sprite(cameraWidth - listTR.get("trash.png").getWidth(), pBottomBound - listTR.get("trash.png").getHeight(), listTR.get("trash.png"));
	this.mainLayer.attachChild(trashCan);

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
			stats.setText(tama.getStats());

		    else if (mainLayer.isVisible())
			updateStatusBars();

		    tama.checkStats();

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
	 * Timer to generate poop
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(10, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		final float xPos = MathUtils.random(30.0f, (cameraWidth - 30.0f));
		final float yPos = MathUtils.random(pTopBound, (pBottomBound - pTopBound));
		addPoop(xPos, yPos);
	    }
	}));

	/**
	 * Timer to run GPS to check weather every hour
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(0, true, new ITimerCallback()
	{	    
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
	    {
		runOnUiThread(new Runnable(){
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
	 * Checks to see if Tamagotchi is prepared for weather every 5 minutes, if it isn't, increase sickness
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(5 * 60, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
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
	}));

	/**
	 * Handling hunger
	 */
	this.mScene.registerUpdateHandler(new TimerHandler(5 * 60, true, new ITimerCallback()
	{
	    @Override
	    public void onTimePassed(final TimerHandler pTimerHandler)
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
	}));

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
	    builder2.setTitle("Apply Item");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to apply this item?");
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
			    mainLayer.detachChild(itemToApply);
			    backpackLayer.attachChild(itemToApply);
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
    }

    @Override
    public void onResume()
    {
	super.onResume();
	this.mEngine.start();
    }

    @Override
    public void onDestroy()
    {
	super.onDestroy();
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

	float mid = (cameraHeight - pBottomBound) / 2;

	/**
	 * Load backpack background
	 */
	final Rectangle backpackBackground = new Rectangle(0, 0, cameraWidth, pBottomBound);
	backpackBackground.setColor(87 / 255f, 57 / 255f, 20 / 255f);
	backpackLayer.attachChild(backpackBackground);

	final Text backpackLabel = new Text(15, 15, mFont, "Backpack", HorizontalAlign.LEFT);
	backpackBackground.attachChild(backpackLabel);

	/**
	 * Load stats background
	 */
	final Rectangle statsBackground = new Rectangle(0, 0, cameraWidth, pBottomBound);
	statsBackground.setColor(0, 0, 0);
	statsLayer.attachChild(statsBackground);

	final Text statsLabel = new Text(15, 15, mFont, "Tamagotchi Stats", HorizontalAlign.LEFT);
	statsBackground.attachChild(statsLabel);

	this.stats = new ChangeableText(25, statsLabel.getY() + 100, mFont, tama.getStats(), HorizontalAlign.LEFT, 512);
	stats.setScale(0.95f);
	statsBackground.attachChild(stats);

	/**
	 * Draw bottom rectangle bar
	 */
	bottomRect = new Rectangle(0, pBottomBound, cameraWidth, cameraHeight);
	bottomRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.mScene.attachChild(bottomRect);

	/**
	 * Draw top rectangle bar
	 */
	topRect = new Rectangle(0, 0, cameraWidth, pTopBound);
	topRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.mScene.attachChild(topRect);

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
		    {
			stats.setText(tama.getStats());
			openLayer(statsLayer);
		    }
		    else
			closeSubLayers();

		    return true;
		}
		return false;
	    }
	};
	bottomRect.attachChild(statsIcon);
	this.mScene.registerTouchArea(statsIcon);

	TextureRegion temp;
	/**
	 * Load status bars
	 */
	final Rectangle healthBar = new Rectangle(leftSpacing, vSpacing, barLength, barHeight);
	healthBar.setColor(1, 1, 1);
	topRect.attachChild(healthBar);

	final Rectangle sicknessBar = new Rectangle(leftSpacing, healthBar.getY() + barHeight + vSpacing, barLength, barHeight);
	sicknessBar.setColor(1, 1, 1);
	topRect.attachChild(sicknessBar);

	final Rectangle hungerBar = new Rectangle(healthBar.getX() + barLength + leftSpacing, vSpacing, barLength, barHeight);
	hungerBar.setColor(1, 1, 1);
	topRect.attachChild(hungerBar);

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
	float xSpacing = this.cameraWidth / 6;
	float ySpacing = this.cameraHeight / 7;
	for (int i = 1; i <= 3; i++)
	{
	    for (int j = 1; j <= 5; j++)
	    {
		Item item = new GameItem((xSpacing * j) - this.listTR.get("treasure.png").getWidth() / 2, (ySpacing * i) - this.listTR.get("treasure.png").getHeight() / 2, this.listTR.get("treasure.png"), "Health item", 7, 0, 0, 0);
		this.bp.addItem(item);
	    }
	}

	final Item umbrella = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Umbrella", 0, 0, 0, 0);
	umbrella.setType(Item.EQUIP);
	umbrella.setProtection(Protection.RAIN);
	this.bp.addItem(umbrella);

	final Item newItem = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Level item", 7, 0, 0, 10000);
	this.bp.addItem(newItem);

	final Item cureAll = new GameItem(0, 0, this.listTR.get("ic_launcher.png"), "Cure All", 0, -10000, -10000, 0);
	this.bp.addItem(cureAll);
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
	popUp = new PopupWindow(this);
	layout = new LinearLayout(this);
	TextView tv = new TextView(this);
	Button but = new Button(this);
	ImageView iv = new ImageView(this);
	LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

	layout.setOrientation(LinearLayout.HORIZONTAL);
	params.setMargins(10, 10, 10, 10);
	tv.setText(i.getInfo() + "\n");
	// iv.setImageBitmap(i.getTextureRegion().getTexture().g);
	but.setText("Close");
	but.setOnClickListener(new OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		popUp.dismiss();
	    }

	});

	layout.addView(iv, params);
	layout.addView(tv, params);
	layout.addView(but, params);
	popUp.setContentView(layout);

	popUp.showAtLocation(layout, Gravity.NO_GRAVITY, 10, cameraHeight / 2);
	popUp.update(cameraWidth - 20, Math.round(cameraHeight / 2 - (cameraHeight - pBottomBound) - 10));
	popUp.setFocusable(true);
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

	    particleSystem.addParticleInitializer(new VelocityInitializer(-10, 10, 60, 90));
	    particleSystem.addParticleInitializer(new AccelerationInitializer(5, 15));

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
	    selectBoxes.get(layer).setVisible(true);
	    for (Entity e : subLayers)
	    {
		if (!e.equals(layer))
		{
		    e.setVisible(false);
		    selectBoxes.get(e).setVisible(false);
		}
	    }

	    for (Entity e : mainLayers)
	    {
		e.setVisible(false);
	    }

	    if (popUp != null)
		popUp.dismiss();

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
	}

	if (popUp != null)
	    popUp.dismiss();
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
	if (!unequipItem())
	{
	    Toast.makeText(getApplicationContext(), "Could not unequip item!", Toast.LENGTH_SHORT).show();
	    return false;
	}

	try
	{
	    item.detachSelf(); // detach item from any other entities
	    mScene.unregisterTouchArea(item); // try to unregister to touch area
	} catch (Exception e)
	{
	    // item was not previously registered with touch
	}

	try
	{
	    bp.removeItem(item); // try to remove from backpack
	} catch (Exception e)
	{
	    // item was not taken from backpack
	}

	tama.setEquippedItem(item);
	item.setPosition(tama.getSprite().getBaseWidth() - 25, tama.getSprite().getBaseHeight() - 25);
	tama.getSprite().attachChild(item);
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
		Toast.makeText(getApplicationContext(), "Backpack is full!", Toast.LENGTH_SHORT).show();
		return false;
	    }
	    tama.getSprite().detachChild(previousItem);
	    backpackLayer.attachChild(previousItem);
	    mScene.registerTouchArea(previousItem);
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
	    final CircleOutlineParticleEmitter particleEmitter = new CircleOutlineParticleEmitter(tama.getSprite().getX() + tama.getSprite().getWidth() / 2, tama.getSprite().getY() + tama.getSprite().getHeight() / 2, 60);
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
	    mScene.attachChild(particleSystem);
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

	}
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
	    if (this.getParent().isVisible())
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
			if (this.getParent().equals(backpackLayer))
			{
			    takeOut = this;
			    runOnUpdateThread(new Runnable()
			    {
				@Override
				public void run()
				{
				    Debug.d("Taking out item");
				    backpackLayer.detachChild(takeOut);
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
			if (this.getParent().equals(backpackLayer))
			{
			    this.setInitialPosition();
			}
			else if (this.getParent().equals(mainLayer))
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
			    else
			    {
				putBack = this;
				runOnUpdateThread(new Runnable()
				{
				    @Override
				    public void run()
				    {
					Debug.d("Putting back item");
					mainLayer.detachChild(putBack);
					backpackLayer.attachChild(putBack);
					putBack = null;
				    }
				}); // End runOnUpdateThread
			    }
			}
		    }
		    else
		    {
			// show item description
			if (popUp != null)
			    popUp.dismiss();
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
		final Text weatherText = new Text(0, pBottomBound - 60, mFont, cc.getCondition(), HorizontalAlign.LEFT);
		mainLayer.attachChild(weatherText);

		// Parse the result and see if there is rain or snow
		String[] temp = cc.getCondition().split(" ");
		Debug.d(temp.toString());
		int weatherType = Weather.NONE;
		for (String s : temp)
		{
		    if (s.equalsIgnoreCase("rain"))
		    {
			weatherType = Weather.RAIN;
			break;
		    }
		    else if (s.equalsIgnoreCase("snow"))
		    {
			weatherType = Weather.SNOW;
			break;
		    }
		}
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
