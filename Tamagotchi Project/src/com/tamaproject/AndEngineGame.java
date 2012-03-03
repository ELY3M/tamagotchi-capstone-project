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

import android.view.Display;
import android.widget.Toast;

public class AndEngineGame extends BaseAndEngineGame implements IOnSceneTouchListener,
	IOnAreaTouchListener
{
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mTamaTextureRegion, mPoopTextureRegion, mTreasureTextureRegion,
	    mPlaceHolderTextureRegion;
    private RepeatingSpriteBackground mGrassBackground;
    private int cameraWidth, cameraHeight;
    private Scene mScene;
    private Backpack bp;
    private Entity mainLayer = new Entity();
    private Entity backpackLayer = new Entity();
    private ArrayList<Entity> takeOut = new ArrayList<Entity>();
    private ArrayList<Entity> putBack = new ArrayList<Entity>();
    private ArrayList<BaseSprite> inPlayObjects = new ArrayList<BaseSprite>();
    private Tamagotchi tama;
    private float pTopBound, pBottomBound;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine()
    {
	final Display display = getWindowManager().getDefaultDisplay();
	this.cameraWidth = display.getWidth();
	this.cameraHeight = display.getHeight();
	this.pTopBound = 50;
	this.pBottomBound = cameraHeight - 100;
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
	this.mGrassBackground = new RepeatingSpriteBackground(cameraWidth, cameraHeight, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass.png"));
	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

	this.mScene = new Scene();
	this.mScene.setBackground(this.mGrassBackground);
	this.mScene.attachChild(mainLayer);
	this.mScene.attachChild(backpackLayer);

	this.bp = new Backpack();
	this.loadItems();
	for (Item item : bp.getItems())
	{
	    this.backpackLayer.attachChild(item.getSprite());
	    this.mScene.registerTouchArea(item.getSprite());
	}

	final int centerX = (cameraWidth - this.mTamaTextureRegion.getWidth()) / 2;
	final int centerY = (cameraHeight - this.mTamaTextureRegion.getHeight()) / 2;

	this.tama = new Tamagotchi();
	this.tama.setSprite(new Sprite(centerX, centerY, this.mTamaTextureRegion));
	this.mainLayer.attachChild(tama.getSprite());
	this.mScene.registerTouchArea(tama.getSprite());

	this.loadInterface();

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
		final float yPos = MathUtils.random(30.0f, (cameraHeight - 30.0f));
		addPoop(xPos, yPos);
	    }
	}));

	this.mainLayer.setVisible(true);
	this.backpackLayer.setVisible(false);
	this.mScene.setOnAreaTouchTraversalFrontToBack();

	return this.mScene;
    }

    @Override
    public void onLoadComplete()
    {

    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void loadInterface()
    {
	/**
	 * Draw top rectangle bar
	 */
	final Rectangle topRect = new Rectangle(0, 0, cameraWidth, 50);
	topRect.setColor(60 / 255, 1.0f, 81 / 255);
	this.mScene.attachChild(topRect);

	/**
	 * Load the open backpack icon
	 */
	// final Sprite openBackpackIcon = new Sprite(cameraWidth - this.mPlaceHolderTextureRegion.getWidth(), cameraHeight - this.mPlaceHolderTextureRegion.getHeight(), mPlaceHolderTextureRegion)
	final Sprite openBackpackIcon = new Sprite(0, 0, mPlaceHolderTextureRegion)
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionUp())
		{
		    if (!bp.isBackpackOpen())
			openBackpack();
		    else
			closeBackpack();
		    return true;
		}
		return false;
	    }
	};
	this.mScene.attachChild(openBackpackIcon);
	this.mScene.registerTouchArea(openBackpackIcon);
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
		Item item = new Item();

		item.setSprite(new Sprite((xSpacing * j) - mTreasureTextureRegion.getWidth() / 2, (ySpacing * i) - mTreasureTextureRegion.getHeight() / 2, mTreasureTextureRegion)
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
				    takeOut.add(this);
				    runOnUpdateThread(new Runnable()
				    {
					@Override
					public void run()
					{
					    Debug.d("Taking out item");
					    synchronized (takeOut)
					    {
						for (Entity e : takeOut)
						{
						    backpackLayer.detachChild(e);
						    mainLayer.attachChild(e);
						    takeOut.remove(e);
						}
					    }
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
				    putBack.add(this);
					runOnUpdateThread(new Runnable()
					{
					    @Override
					    public void run()
					    {
						Debug.d("Putting back item");
						synchronized (putBack)
						{
						    for (Entity e : putBack)
						    {
							mainLayer.detachChild(e);
							backpackLayer.attachChild(e);
							e.setInitialPosition();
							putBack.remove(e);
						    }
						}
					    }
					}); // End runOnUpdateThread
				}
			    }
			}
			
			return true;
		    }
		}); // End new Sprite
		this.bp.addItem(item);
	    }
	}
    }

    private void openBackpack()
    {
	this.backpackLayer.setVisible(true);
	this.mainLayer.setVisible(false);
	this.bp.setBackpackOpen(true);
    }

    private void closeBackpack()
    {
	this.backpackLayer.setVisible(false);
	this.mainLayer.setVisible(true);
	this.bp.setBackpackOpen(false);
	this.bp.resetPositions(cameraWidth, cameraHeight);
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
