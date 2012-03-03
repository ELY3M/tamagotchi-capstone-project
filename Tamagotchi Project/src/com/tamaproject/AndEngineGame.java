package com.tamaproject;

import java.util.ArrayList;

import com.tamaproject.andengine.entity.*;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.MathUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Display;
import android.widget.Toast;

public class AndEngineGame extends BaseAndEngineGame implements IOnSceneTouchListener,
	IOnAreaTouchListener
{
    // ===========================================================
    // Constants
    // ===========================================================

    private final int cameraWidth = 480, cameraHeight = 800;
    private static final int CONFIRM_APPLYITEM = 0;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mTamaTextureRegion, mPoopTextureRegion, mTreasureTextureRegion,
	    mPlaceHolderTextureRegion, mMicRegion, mBackpackIconRegion;
    private RepeatingSpriteBackground mGrassBackground;
    private Scene mScene;
    private Backpack bp;
    private Entity mainLayer = new Entity();
    private Entity backpackLayer = new Entity();
    private Item takeOut;
    private Item putBack;
    private Item itemToApply;
    private ArrayList<BaseSprite> inPlayObjects = new ArrayList<BaseSprite>();
    private Tamagotchi tama;
    private float pTopBound, pBottomBound;

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine()
    {
	this.pTopBound = 100;
	this.pBottomBound = cameraHeight - 60;
	this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
	return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), this.mCamera));
    }

    @Override
    public void onLoadResources()
    {
	this.mBitmapTextureAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mTamaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "tama.png", 0, 0);
	this.mPoopTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "poop.png", 0, 107);
	this.mTreasureTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "treasure.png", 0, 156);
	this.mPlaceHolderTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ic_launcher.png", 0, 205);
	this.mMicRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "mic.png", 50, 107);
	this.mBackpackIconRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "backpack.png", 100, 107);
	this.mGrassBackground = new RepeatingSpriteBackground(cameraWidth, cameraHeight, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass.png"));
	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

	this.mScene = new Scene();
	this.mScene.setBackground(this.mGrassBackground);

	final int centerX = (cameraWidth - this.mTamaTextureRegion.getWidth()) / 2;
	final int centerY = (cameraHeight - this.mTamaTextureRegion.getHeight()) / 2;

	// Load tamagotchi first
	this.tama = new Tamagotchi();
	this.tama.setSprite(new Sprite(centerX, centerY, this.mTamaTextureRegion));
	tama.getSprite().setScale(0.85f);
	this.mainLayer.attachChild(tama.getSprite());

	this.loadInterface();

	this.mScene.attachChild(mainLayer);
	this.mScene.attachChild(backpackLayer);

	this.mainLayer.setVisible(true);
	this.backpackLayer.setVisible(false);

	this.bp = new Backpack();
	this.loadItems();
	for (Item item : bp.getItems())
	{
	    this.backpackLayer.attachChild(item);
	    this.mScene.registerTouchArea(item);
	}

	this.mScene.registerTouchArea(tama.getSprite());

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

		} catch (Exception e)
		{
		    Debug.d("onUpdate EXCEPTION:" + e);
		} catch (Error e)
		{
		    Debug.d("onUpdate ERROR:" + e);
		}
	    }
	});

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

	this.mScene.setOnAreaTouchTraversalFrontToBack();

	return this.mScene;
    }

    @Override
    public void onLoadComplete()
    {

    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
	switch (id)
	{
	case AndEngineGame.CONFIRM_APPLYITEM:
	    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	    builder2.setTitle("Apply Item");
	    builder2.setIcon(android.R.drawable.btn_star);
	    builder2.setMessage("Are you sure you want to apply this item?");
	    builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	    {
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
	}

	return null;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private Rectangle selectionBox1;

    private void loadInterface()
    {
	int iconSpacer = cameraWidth / 7;
	float mid = (cameraHeight - pBottomBound) / 2;

	/**
	 * Load backpack background
	 */
	final Rectangle backpackBackground = new Rectangle(0, 0, cameraWidth, pBottomBound);
	backpackBackground.setColor(87 / 255f, 57 / 255f, 20 / 255f);
	backpackLayer.attachChild(backpackBackground);

	/**
	 * Draw bottom rectangle bar
	 */
	final Rectangle bottomRect = new Rectangle(0, pBottomBound, cameraWidth, cameraHeight);
	bottomRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.mScene.attachChild(bottomRect);

	/**
	 * Draw top rectangle bar
	 */
	final Rectangle topRect = new Rectangle(0, 0, cameraWidth, pTopBound);
	topRect.setColor(70 / 255f, 132 / 255f, 163 / 255f);
	this.mScene.attachChild(topRect);

	/**
	 * Make selection box
	 */
	this.selectionBox1 = new Rectangle(iconSpacer * 1 - 25f, 0, 50, bottomRect.getHeight());
	this.selectionBox1.setColor(1, 1, 1);
	this.selectionBox1.setVisible(false);
	bottomRect.attachChild(this.selectionBox1);

	/**
	 * Load the open backpack icon
	 */
	final Sprite openBackpackIcon = new Sprite(iconSpacer * 1 - mBackpackIconRegion.getWidth() / 2, mid - mBackpackIconRegion.getHeight() / 2, mBackpackIconRegion)
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionUp())
		{
		    if (!bp.isBackpackOpen())
		    {
			openBackpack();
		    }
		    else
		    {
			closeBackpack();
		    }
		    return true;
		}
		return false;
	    }
	};
	bottomRect.attachChild(openBackpackIcon);
	this.mScene.registerTouchArea(openBackpackIcon);

	/**
	 * Load microphone icon
	 */
	final Sprite micIcon = new Sprite(iconSpacer * 2 - mMicRegion.getWidth() / 2, mid - mMicRegion.getHeight() / 2, mMicRegion);
	bottomRect.attachChild(micIcon);
	this.mScene.registerTouchArea(micIcon);

	float barLength = 150;
	float barHeight = 15;

	/**
	 * Load health bar
	 */
	final Rectangle healthBar = new Rectangle(10, 10, barLength, barHeight);
	healthBar.setColor(1, 1, 1);
	topRect.attachChild(healthBar);

	float ratio = tama.getCurrentHealth() / tama.getMaxHealth();
	Debug.d("Tama health ratio: " + ratio);
	final Rectangle currHealthBar = new Rectangle(2, 2, ratio * (barLength - 4), barHeight - 4);
	currHealthBar.setColor(1, 0, 0);
	healthBar.attachChild(currHealthBar);
    }

    /**
     * Adds poop to scene at specified coordinates
     * 
     * @param pX
     * @param pY
     */
    public void addPoop(final float pX, final float pY)
    {
	final Sprite poop = new Sprite(pX, pY, this.mPoopTextureRegion)
	{
	    private boolean touched = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();

		// don't respond to touch unless sprite's parent is visible
		if (!this.getParent().isVisible())
		    return false;

		if (pSceneTouchEvent.isActionDown())
		{
		    touched = true;
		}
		else if (pSceneTouchEvent.isActionMove())
		{
		    if (touched)
		    {
			if (y < pTopBound)
			{
			    this.setPosition(x - this.getWidth() / 2, pTopBound - this.getHeight() / 2);
			}
			else if (y > pBottomBound)
			{
			    this.setPosition(x - this.getWidth() / 2, pBottomBound - this.getHeight() / 2);
			}
			else
			{
			    this.setPosition(x - this.getWidth() / 2, y - this.getHeight() / 2);
			}
		    }
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    touched = false;
		}
		return true;
	    }
	};
	poop.setUserData("poop");
	float scale = MathUtils.random(0.75f, (2.0f - 0.75f));
	poop.setScale(scale);
	inPlayObjects.add(poop);

	this.mainLayer.attachChild(poop);
	this.mScene.registerTouchArea(poop);
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
	    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
    {
	if (pSceneTouchEvent.isActionDown())
	{
	    return true;
	}

	return false;
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent)
    {
	if (pSceneTouchEvent.isActionDown())
	{
	    return true;
	}
	else if (pSceneTouchEvent.isActionUp())
	{
	    return true;
	}

	return false;
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
		Item item = new Item((xSpacing * j) - mTreasureTextureRegion.getWidth() / 2, (ySpacing * i) - mTreasureTextureRegion.getHeight() / 2, mTreasureTextureRegion)
		{
		    private boolean touched = false;

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
			    }
			    else if (pSceneTouchEvent.isActionMove())
			    {
				Debug.d("Item action move");
				if (touched)
				{
				    this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				}
			    }
			    else if (pSceneTouchEvent.isActionUp())
			    {
				Debug.d("Item action up");
				touched = false;
				if (this.getParent().equals(backpackLayer))
				{
				    this.setInitialPosition();
				}
				else if (this.getParent().equals(mainLayer))
				{
				    if (this.collidesWith(tama.getSprite()))
				    {
					itemToApply = this;
					showDialog(AndEngineGame.CONFIRM_APPLYITEM);
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
			}

			return true;
		    }
		}; // End new Sprite
		this.bp.addItem(item);
	    }
	}
    }

    private void applyItem(Item item)
    {
	this.tama.applyItem(item);
	this.bp.removeItem(item);
	this.mainLayer.detachChild(item);
	this.mScene.unregisterTouchArea(item);
    }

    private void openBackpack()
    {
	this.backpackLayer.setVisible(true);
	this.mainLayer.setVisible(false);
	this.bp.setBackpackOpen(true);
	this.selectionBox1.setVisible(true);
	this.bp.resetPositions(cameraWidth, cameraHeight);
    }

    private void closeBackpack()
    {
	this.backpackLayer.setVisible(false);
	this.mainLayer.setVisible(true);
	this.bp.setBackpackOpen(false);
	this.bp.resetPositions(cameraWidth, cameraHeight);
	this.selectionBox1.setVisible(false);
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
