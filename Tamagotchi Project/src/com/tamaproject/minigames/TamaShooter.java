package com.tamaproject.minigames;

import java.util.ArrayList;
import java.util.Iterator;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.util.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.Display;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.tamaproject.BaseAndEngineGame;

public class TamaShooter extends BaseAndEngineGame implements IAccelerometerListener,
	IOnSceneTouchListener, IOnAreaTouchListener
{
    private static final String TAG = TamaShooter.class.getSimpleName();
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    // ===========================================================
    // Fields
    // ===========================================================

    private BitmapTextureAtlas mBitmapTextureAtlas, mBitTextureAtlas;

    private TiledTextureRegion mBoxFaceTextureRegion;
    private TiledTextureRegion mCircleFaceTextureRegion;
    private TextureRegion mBulletTextureRegion;

    private int mFaceCount = 0;

    private PhysicsWorld mPhysicsWorld;

    private float mGravityX;
    private float mGravityY;
    private Scene mScene;

    private Camera mCamera;

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
	// getting the device's screen size
	final Display display = getWindowManager().getDefaultDisplay();
	int cameraWidth = display.getWidth();
	int cameraHeight = display.getHeight();

	mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
	final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	engineOptions.getTouchOptions().setRunOnUpdateThread(true);
	return new Engine(engineOptions);
    }

    @Override
    public void onLoadResources()
    {
	this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mBitTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_box_tiled.png", 0, 0, 2, 1); // 64x32
	this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
	this.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0); // 64x32
	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene()
    {
	this.mEngine.registerUpdateHandler(new FPSLogger());

	this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

	this.mScene = new Scene();
	this.mScene.setBackground(new ColorBackground(0, 0, 0));

	this.addTarget(200, 200);

	this.mScene.setOnSceneTouchListener(this);

	this.mScene.registerUpdateHandler(this.mPhysicsWorld);

	this.mScene.setOnAreaTouchListener(this);

	this.mPhysicsWorld.setContactListener(this.contactListener);

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
		    probeMyBody();
		    for(Sprite p : projectileList)
		    {
			
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

	return this.mScene;
    }
    
    ArrayList<Sprite> projectileList = new ArrayList<Sprite>();

    public void addTarget(float pX, float pY)
    {
	final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	final Sprite target = new Sprite(pX, pY, this.mBulletTextureRegion);
	final Body body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, target, BodyType.DynamicBody, objectFixtureDef);
	body.setUserData(storeMyBodyInfo("target", target, false));
	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(target, body, true, true));
	target.setUserData(body);
	this.mScene.attachChild(target);
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
	    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
    {
	if (pSceneTouchEvent.isActionDown())
	{
	    // final AnimatedSprite face = (AnimatedSprite) pTouchArea;
	    // this.jumpFace(face);
	    return true;
	}

	return false;
    }

    @Override
    public void onLoadComplete()
    {

    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent)
    {
	if (this.mPhysicsWorld != null)
	{
	    if (pSceneTouchEvent.isActionDown())
	    {
		this.shootProjectile(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
		return true;
	    }
	}
	return false;
    }

    @Override
    public void onAccelerometerChanged(final AccelerometerData pAccelerometerData)
    {

    }

    @Override
    public void onResumeGame()
    {
	super.onResumeGame();

	this.enableAccelerometerSensor(this);
    }

    @Override
    public void onPauseGame()
    {
	super.onPauseGame();

	this.disableAccelerometerSensor();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void shootProjectile(final float pX, final float pY)
    {
	final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	final Sprite projectile = new Sprite(0, 0, this.mBulletTextureRegion);
	final Body body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, projectile, BodyType.DynamicBody, objectFixtureDef);
	body.setUserData(storeMyBodyInfo("projectile", projectile, false));

	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(projectile, body, true, true));
	projectile.setUserData(body);
	this.mScene.attachChild(projectile);
	
	this.projectileList.add(projectile);

	final Vector2 velocity = Vector2Pool.obtain(pX, pY);
	body.setLinearVelocity(velocity);
	body.setBullet(true);
	Vector2Pool.recycle(velocity);

    }

    protected JSONObject myGlobalJSON;
    protected ArrayList<JSONObject> myJSONarray = new ArrayList<JSONObject>();

    private JSONObject storeMyBodyInfo(final String myDescription, final Sprite mySprite,
	    final Boolean myKill)
    {
	// STORE INFORMATION ABOUT THE CURRENT BODY SO THAT WE HAVE ENOUGH INFO TO DELETE IT LATER
	JSONObject myObject = new JSONObject();

	try
	{
	    myObject.put("myDescription", myDescription);
	    myObject.put("killMe", myKill);
	    myObject.put("mySprite", mySprite);
	} catch (JSONException e)
	{
	    Debug.d("storeMyBodyInfo FAILED: " + e);
	}
	return myObject;
    }

    private void updateMyBody(JSONObject myJSON)
    {
	// ANY BODIES PASSED INTO THIS METHOD ARE SCHEDULED TO BE KILLED
	try
	{
	    this.myGlobalJSON = myJSON;
	    this.myJSONarray.add(myJSON);
	    Debug.d("updateMyBody ADDED: " + myJSON);
	} catch (Exception e)
	{
	    Debug.d("updateMyBody FAILED: " + e);
	}
    }

    private void probeMyBody()
    {
	if (this.mPhysicsWorld != null)
	{
	    for (JSONObject j : this.myJSONarray)
	    {
		try
		{
		    if (j != null)
		    {
			try
			{
			    Debug.d("probeMyBody THE BODY WANTS TO DIE");
			    final Sprite mySprite = (Sprite) j.get("mySprite");
			    myBodyKiller(mySprite);
			} catch (JSONException e)
			{
			    Debug.d("probeMyBody JSON FAILED: " + e);
			}
		    }
		} catch (Exception e)
		{
		    Debug.d("probeMyBody THE BODY DOES NOT WANT TO DIE: " + e);
		}
	    }
	}
    }

    private void myBodyKiller(final Sprite sprite)
    {
	final PhysicsConnector facePhysicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(sprite);
	this.mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
	this.mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());
	this.mScene.detachChild(sprite);
	Debug.d("myBodyKiller THE BODY IS NOW DEAD");
	System.gc();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private ContactListener contactListener = new ContactListener()
    {

	@Override
	public void preSolve(Contact contact, Manifold oldManifold)
	{

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse)
	{

	}

	@Override
	public void endContact(Contact contact)
	{

	}

	@Override
	public void beginContact(Contact contact)
	{
	    try
	    {
		Body x1 = contact.getFixtureA().getBody();
		Body x2 = contact.getFixtureB().getBody();
		if (x1.getUserData().equals("projectile"))
		{
		    JSONObject myJSONdata = (JSONObject) x2.getUserData();
		    updateMyBody(myJSONdata);
		    Log.d(TAG, "CONTACT BETWEEN PROJECTILE AND TARGET");
		}
		else if (x2.getUserData().equals("projectile"))
		{
		    JSONObject myJSONdata = (JSONObject) x1.getUserData();
		    updateMyBody(myJSONdata);
		    Log.d(TAG, "CONTACT BETWEEN PROJECTILE AND TARGET");
		}
	    } catch (Exception e)
	    {
		e.printStackTrace();
	    }

	}
    };
}
