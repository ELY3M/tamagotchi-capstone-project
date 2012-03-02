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
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
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
    private int cameraWidth, cameraHeight;
    private Scene mScene;
    private Backpack bp;
    private Entity mainLayer = new Entity();
    private Entity backpackLayer = new Entity();
    private ArrayList<Entity> takeOut = new ArrayList<Entity>();
    private ArrayList<Entity> putBack = new ArrayList<Entity>();

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
	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

	this.mScene = new Scene();
	this.mScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
	this.mScene.attachChild(mainLayer);
	this.mScene.attachChild(backpackLayer);

	this.bp = new Backpack();
	this.loadItems();
	for (Item item : bp.getItems())
	{
	    this.backpackLayer.attachChild(item);
	    this.mScene.registerTouchArea(item);
	}

	final int centerX = (cameraWidth - this.mTamaTextureRegion.getWidth()) / 2;
	final int centerY = (cameraHeight - this.mTamaTextureRegion.getHeight()) / 2;

	final Tamagotchi tama = new Tamagotchi(centerX, centerY, this.mTamaTextureRegion);
	this.mainLayer.attachChild(tama);
	this.mScene.registerTouchArea(tama);

	final Sprite openBackpackIcon = new Sprite(cameraWidth - this.mPlaceHolderTextureRegion.getWidth(), cameraHeight - this.mPlaceHolderTextureRegion.getHeight(), mPlaceHolderTextureRegion)
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

    public void addPoop(final float pX, final float pY)
    {
	final Sprite poop = new Sprite(pX, pY, this.mPoopTextureRegion)
	{
	    private boolean touched = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    touched = true;
		}
		else if (pSceneTouchEvent.isActionMove())
		{
		    if (touched)
			this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
		}
		else if(pSceneTouchEvent.isActionUp())
		{
		    touched = false;
		}
		return true;
	    }
	};
	poop.setUserData("poop");

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

			if (backpackLayer.isVisible() && this.getParent().equals(backpackLayer))
			{
			    if (pSceneTouchEvent.isActionDown())
			    {
				Debug.d("Item action down");
				touched = true;

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
				this.setInitialPosition();
			    }
			}
			else if (mainLayer.isVisible() && this.getParent().equals(mainLayer))
			{
			    if (pSceneTouchEvent.isActionDown())
			    {
				Debug.d("Item action down");
				touched = true;
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
				});
			    }
			}
			return true;
		    }
		};
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
