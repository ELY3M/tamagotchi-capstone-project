package com.tamaproject.minigames;

import java.util.Hashtable;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.MathUtils;

import sun.applet.Main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.tamaproject.BaseAndEngineGame;
import com.tamaproject.util.TextureUtil;

public class MiniGame extends BaseAndEngineGame
{

    // ===========================================================
    // Constants
    // ===========================================================

    private static final int RACETRACK_WIDTH = 64;
    private static final int OBSTACLE_SIZE = 16;

    private static final int TAMA_SIZE = 16;

    private static final int cameraWidthMG = RACETRACK_WIDTH * 5,
	    cameraHeightMG = RACETRACK_WIDTH * 3;
    private static final boolean FULLSCREEN = true;

    private Hashtable<String, TextureRegion> listTR;
    // ===========================================================
    // Fields
    // ===========================================================

    private RepeatingSpriteBackground mGrassBackgroundMG;
    private Camera mCameraMG;
    private Scene mSceneMG;

    // Analog Stick
    private BitmapTextureAtlas mOnScreenControlTexture;
    private TextureRegion mOnScreenControlBaseTextureRegion;
    private TextureRegion mOnScreenControlKnobTextureRegion;

    private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;

    // physics
    private PhysicsWorld mPhysicsWorld;

    // Layers
    private BitmapTextureAtlas mTamaBitmapTextureAtlas;
    private TiledTextureRegion mTamaTextureRegion;

    private BitmapTextureAtlas mTamaTexture;
    private BitmapTextureAtlas mBallTexture;

    private BitmapTextureAtlas mRacetrackTexture;

    private Body mTamaBody;
    private TiledSprite mTama;
    
    private long startTime;
    private long lapTime;
    private long totalTime = 0;
    private int currentLap = 0;
    private int totalLap = 1;
    
    
    @Override
    public Engine onLoadEngine()
    { // Change camera dimensions to fit in custom controls?
	Toast.makeText(this, "Changed Shape to Rectangle", Toast.LENGTH_LONG).show();
    this.mCameraMG = new Camera(0, 0, cameraWidthMG, cameraHeightMG);
	// return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidthMG, cameraHeightMG), this.mCameraMG));
	final Engine engine = new Engine(new EngineOptions(FULLSCREEN, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidthMG, cameraHeightMG), this.mCameraMG));
/*
	try
	{
	    if (MultiTouch.isSupported(this))
	    {
		engine.setTouchController(new MultiTouchController());
		if (MultiTouch.isSupportedDistinct(this))
		{
		    Toast.makeText(this, "MultiTouch detected - Both controls will work", Toast.LENGTH_LONG).show();
		}
		else
		{
		    this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
		    Toast.makeText(this, "MultiTouch detected, but problems distinguishing between fingers", Toast.LENGTH_LONG).show();
		}
	    }
	    else
	    {
		Toast.makeText(this, "No MultiTouch detected", Toast.LENGTH_LONG).show();
	    }
	} catch (final MultiTouchException e)
	{
	    Toast.makeText(this, "Android Version does NOT support MultiTouch", Toast.LENGTH_LONG).show();
	}
		*/
	return engine;
    }

    @Override
    public void onLoadResources()
    { // new backgrounds?

	listTR = TextureUtil.loadTextures(this, mEngine, new String[] { new String("gfx/") });
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("");

	// Load background
	this.mGrassBackgroundMG = new RepeatingSpriteBackground(cameraWidthMG, cameraHeightMG, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass.png"));

	this.mTamaBitmapTextureAtlas = new BitmapTextureAtlas(256, 512, TextureOptions.BILINEAR);
	this.mTamaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mTamaBitmapTextureAtlas, this, "animated_gfx/animate_test.png", 0, 0, 3, 4);

	this.mRacetrackTexture = new BitmapTextureAtlas(128, 256, TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);

	// Load Analog Stick
	this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "gfx/onscreen_control_base.png", 0, 0);
	this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "gfx/onscreen_control_knob.png", 128, 0);

	this.mEngine.getTextureManager().loadTextures(this.mTamaBitmapTextureAtlas, this.mOnScreenControlTexture);
    }

    @Override
    public Scene onLoadScene()
    {
    	
		this.mEngine.registerUpdateHandler(new FPSLogger());
	
		this.mSceneMG = new Scene();
		this.mSceneMG.setBackground(this.mGrassBackgroundMG);
	
		this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 1);
		
		
		this.initRacetrack();
		this.initRacetrackBorders();
		this.initTama();
		this.initObstacles();
	//	this.initLap();
		this.initOnScreenControls();
		this.initClock();
	//	this.endCondition();
	
		this.mSceneMG.registerUpdateHandler(this.mPhysicsWorld);
	
		/* Initialize Laps */
    	
    	final Line startingLine = new Line(0, cameraHeightMG - 2*RACETRACK_WIDTH, RACETRACK_WIDTH, cameraHeightMG - 2*RACETRACK_WIDTH);
    	this.mSceneMG.attachChild(startingLine);
    	//this.mSceneMG.attachChild(checkpoint);
		
		
		/* Collision-checking for detecting laps */
		this.mSceneMG.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {}
			
			@Override
			public void onUpdate(final float pSecondsElapsed) {
				if(startingLine.collidesWith(mTama)) {
					startingLine.setColor(1,0,0);
					lapTime = System.currentTimeMillis() - startTime;
					currentLap += 1;
					totalTime += lapTime;
					// Toast.makeText(this, "Lap: " + currentLap + "Lap Time: " + lapTime, Toast.LENGTH_SHORT).show();
					//toaster.sendMessage("abc");
					if (currentLap >= totalLap)
					{
						startingLine.setColor(1,1,1);
					}
				}
				else {
					startingLine.setColor(0,1,0);
				}
			}
		});

		
		return this.mSceneMG;

    }

    @Override
    public void onLoadComplete()
    {

    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void initOnScreenControls()
    {
	final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, cameraHeightMG - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCameraMG, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener()
	{
	    @Override
	    public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl,
		    final float pValueX, final float pValueY)
	    {
		final Body TamaBody = MiniGame.this.mTamaBody;

		final Vector2 velocity = Vector2Pool.obtain(pValueX * 2, pValueY * 2);
		TamaBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);

		final float rotationInRad = (float) Math.atan2(-pValueX, pValueY);
		TamaBody.setTransform(TamaBody.getWorldCenter(), rotationInRad);

		MiniGame.this.mTama.setRotation(MathUtils.radToDeg(rotationInRad));
	    }

	    @Override
	    public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl)
	    {
		/* Nothing. */
	    }
	});

	analogOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	analogOnScreenControl.getControlBase().setAlpha(0.5f);

	analogOnScreenControl.refreshControlKnobPosition();

	this.mSceneMG.setChildScene(analogOnScreenControl);
    }

    // Place Tama on screen
    private void initTama()
    {
	this.mTama = new TiledSprite(20, RACETRACK_WIDTH-10, TAMA_SIZE, TAMA_SIZE, this.mTamaTextureRegion);
	this.mTama.setCurrentTileIndex(0);

	final FixtureDef TamaFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	this.mTamaBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, this.mTama, BodyType.DynamicBody, TamaFixtureDef);

	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this.mTama, this.mTamaBody, true, false));

	this.mSceneMG.attachChild(this.mTama);
    }

    // Add balls to the racetrack, serve as obstacles
    private void initObstacles()
    {
		this.addObstacle(cameraWidthMG / 2, RACETRACK_WIDTH / 2);
		this.addObstacle(cameraWidthMG / 2, RACETRACK_WIDTH / 2);
		this.addObstacle(cameraWidthMG / 2, cameraHeightMG - RACETRACK_WIDTH / 2);
		this.addObstacle(cameraWidthMG / 2, cameraHeightMG - RACETRACK_WIDTH / 2);
    }

    private void addObstacle(final float pX, final float pY)
    {
	final Sprite ball = new Sprite(pX, pY, OBSTACLE_SIZE, OBSTACLE_SIZE, this.listTR.get("red_ball.png"));

	final FixtureDef ballFixtureDef = PhysicsFactory.createFixtureDef(0.1f, 0.5f, 0.5f);
	final Body ballBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, ball, BodyType.DynamicBody, ballFixtureDef);
	ballBody.setLinearDamping(10);
	ballBody.setAngularDamping(10);

	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, ballBody, true, true));

	this.mSceneMG.attachChild(ball);
    }

    private void initRacetrack()
    {
	/* Straights. */
	{
	    final TextureRegion racetrackHorizontalStraightTextureRegion = this.listTR.get("racetrack_straight.png").deepCopy();
	    racetrackHorizontalStraightTextureRegion.setWidth(3 * this.listTR.get("racetrack_straight.png").getWidth());

	    final TextureRegion racetrackVerticalStraightTextureRegion = this.listTR.get("racetrack_straight.png");

	    /* Top Straight */
	    this.mSceneMG.attachChild(new Sprite(RACETRACK_WIDTH, 0, 3 * RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackHorizontalStraightTextureRegion));
	    /* Bottom Straight */
	    this.mSceneMG.attachChild(new Sprite(RACETRACK_WIDTH, cameraHeightMG - RACETRACK_WIDTH, 3 * RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackHorizontalStraightTextureRegion));

	    /* Left Straight */
	    final Sprite leftVerticalStraight = new Sprite(0, RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackVerticalStraightTextureRegion);
	    leftVerticalStraight.setRotation(90);
	    this.mSceneMG.attachChild(leftVerticalStraight);
	    /* Right Straight */
	    final Sprite rightVerticalStraight = new Sprite(cameraWidthMG - RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackVerticalStraightTextureRegion);
	    rightVerticalStraight.setRotation(90);
	    this.mSceneMG.attachChild(rightVerticalStraight);
	}

	/* Edges */
	{
	    final TextureRegion racetrackCurveTextureRegion = this.listTR.get("racetrack_curve.png");

	    /* Upper Left */
	    final Sprite upperLeftCurve = new Sprite(0, 0, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion);
	    upperLeftCurve.setRotation(90);
	    this.mSceneMG.attachChild(upperLeftCurve);

	    /* Upper Right */
	    final Sprite upperRightCurve = new Sprite(cameraWidthMG - RACETRACK_WIDTH, 0, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion);
	    upperRightCurve.setRotation(180);
	    this.mSceneMG.attachChild(upperRightCurve);

	    /* Lower Right */
	    final Sprite lowerRightCurve = new Sprite(cameraWidthMG - RACETRACK_WIDTH, cameraHeightMG - RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion);
	    lowerRightCurve.setRotation(270);
	    this.mSceneMG.attachChild(lowerRightCurve);

	    /* Lower Left */
	    final Sprite lowerLeftCurve = new Sprite(0, cameraHeightMG - RACETRACK_WIDTH, RACETRACK_WIDTH, RACETRACK_WIDTH, racetrackCurveTextureRegion);
	    this.mSceneMG.attachChild(lowerLeftCurve);
	}
    }

    /* Set up Race Track Borders */
    private void initRacetrackBorders()
    {
    	// RACETRACK_WIDTH = 64
    	// cameraWidthMG = 320
    	// cameraHeightMG = 192
    	// cameraHeightMG - 2 - RACETRACK_WIDTH = 126
	final Shape bottomOuter = new Rectangle(0, cameraHeightMG - 2, cameraWidthMG, 2);
	final Shape topOuter = new Rectangle(0, 0, cameraWidthMG, 2);
	final Shape leftOuter = new Rectangle(0, 0, 2, cameraHeightMG);
	final Shape rightOuter = new Rectangle(cameraWidthMG - 2, 0, 2, cameraHeightMG);

	final Shape bottomInner = new Rectangle(RACETRACK_WIDTH, cameraHeightMG - 2 - RACETRACK_WIDTH, cameraWidthMG - 2 * RACETRACK_WIDTH, 2);
	//final Shape bottomInner = new Rectangle((cameraHeightMG - RACETRACK_WIDTH)/2, (cameraHeightMG - 2 - RACETRACK_WIDTH) / 2, (cameraWidthMG - 2 * RACETRACK_WIDTH) / 2, 2/2);
	final Shape topInner = new Rectangle(RACETRACK_WIDTH, RACETRACK_WIDTH, cameraWidthMG - 2 * RACETRACK_WIDTH, 2);
	final Shape leftInner = new Rectangle(RACETRACK_WIDTH, RACETRACK_WIDTH, 2, cameraHeightMG - 2 * RACETRACK_WIDTH);
	final Shape rightInner = new Rectangle(cameraWidthMG - 2 - RACETRACK_WIDTH, RACETRACK_WIDTH, 2, cameraHeightMG - 2 * RACETRACK_WIDTH);

	
	final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, bottomOuter, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, topOuter, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, leftOuter, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, rightOuter, BodyType.StaticBody, wallFixtureDef);

	PhysicsFactory.createBoxBody(this.mPhysicsWorld, bottomInner, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, topInner, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, leftInner, BodyType.StaticBody, wallFixtureDef);
	PhysicsFactory.createBoxBody(this.mPhysicsWorld, rightInner, BodyType.StaticBody, wallFixtureDef);

	
	this.mSceneMG.attachChild(bottomOuter);
	this.mSceneMG.attachChild(topOuter);
	this.mSceneMG.attachChild(leftOuter);
	this.mSceneMG.attachChild(rightOuter);

	this.mSceneMG.attachChild(bottomInner);
	this.mSceneMG.attachChild(topInner);
	this.mSceneMG.attachChild(leftInner);
	this.mSceneMG.attachChild(rightInner);
	
	
    }
    
    /* Set up laps */
    private void initLap()
    {
    	final LoopEntityModifier entityModifier = new LoopEntityModifier(new ParallelEntityModifier(new RotationModifier(6, 0, 360), new SequenceEntityModifier(new ScaleModifier(3, 1, 1.5f), new ScaleModifier(3, 1.5f, 1))));
    	
    	final Line startingLine = new Line(0, cameraHeightMG - RACETRACK_WIDTH, RACETRACK_WIDTH, cameraHeightMG - RACETRACK_WIDTH);
    	startingLine.registerEntityModifier(entityModifier.deepCopy());
    	//final Line checkpoint = new Line( cameraWidthMG / 2 ,0, 2, RACETRACK_WIDTH);
    	
    	final FixtureDef lapFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
    	PhysicsFactory.createBoxBody(this.mPhysicsWorld, startingLine,BodyType.StaticBody, lapFixtureDef);
    	//PhysicsFactory.createBoxBody(this.mPhysicsWorld, checkpoint,BodyType.StaticBody, lapFixtureDef);
    	
    	this.mSceneMG.attachChild(startingLine);
    	//this.mSceneMG.attachChild(checkpoint);
    }
    
    private void initClock()
    {
    	startTime = System.currentTimeMillis();
    	
    }
    
    private void endCondition()
    {
    	if (currentLap == totalLap)
    	{
    		Toast.makeText(this, "GameOver", Toast.LENGTH_SHORT).show();
   
    	}
  
    }
    
    /* Enable displayed text in onUpdate */
    public Handler toaster = new Handler() {
    	@Override
    	public void handleMessage (Message msg) {
    		Toast.makeText(getBaseContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();	
    	}
    };

   
    public void makeToast(String str) {
    	Message status = this.toaster.obtainMessage();
    	Bundle datax = new Bundle();
    	datax.putString("msg", str);
    	status.setData(datax);
    	this.toaster.sendMessage(status);
  
    }
    
    /*
     * @Override public boolean onAreaTouched(TouchEvent arg0, ITouchArea arg1, float arg2, float arg3) { // TODO Auto-generated method stub return false; }
     * 
     * @Override public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) { // TODO Auto-generated method stub return false; }
     */
    @Override
    public void pauseSound()
    {
	// TODO Auto-generated method stub

    }

    @Override
    public void resumeSound()
    {
	// TODO Auto-generated method stub

    }

}
