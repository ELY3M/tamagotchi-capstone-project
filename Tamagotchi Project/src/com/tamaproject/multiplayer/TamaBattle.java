package com.tamaproject.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.anddev.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.tamaproject.BaseAndEngineGame;
import com.tamaproject.adt.messages.client.ClientMessageFlags;
import com.tamaproject.adt.messages.server.ConnectionCloseServerMessage;
import com.tamaproject.adt.messages.server.ServerMessageFlags;

/**
 * Multiplayer battle mode between multiple Tamagotchis.
 * 
 * @author Jonathan
 * 
 */
public class TamaBattle extends BaseAndEngineGame implements ClientMessageFlags, ServerMessageFlags
{
    // ===========================================================
    // Constants
    // ===========================================================

    private static final String LOCALHOST_IP = "127.0.0.1";

    private static BulletPool BULLET_POOL;

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    private static final int SERVER_PORT = 4444;

    private static final short FLAG_MESSAGE_SERVER_ADD_SPRITE = 1;
    private static final short FLAG_MESSAGE_SERVER_MOVE_SPRITE = 2;
    private static final short FLAG_MESSAGE_SERVER_ID_PLAYER = 3;
    private static final short FLAG_MESSAGE_CLIENT_REQUEST_ID = 4,
	    FLAG_MESSAGE_CLIENT_MOVE_SPRITE = 6, FLAG_MESSAGE_CLIENT_ADD_SPRITE = 7,
	    FLAG_MESSAGE_SERVER_FIRE_BULLET = 8, FLAG_MESSAGE_CLIENT_FIRE_BULLET = 9,
	    FLAG_MESSAGE_SERVER_REMOVE_SPRITE = 10;

    private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
    private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
    private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;

    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mSpriteTextureRegion, mCrossHairTextureRegion;

    private int mSpriteIDCounter, bulletCounter = 0;
    private final SparseArray<Sprite> mSprites = new SparseArray<Sprite>();
    private final SparseArray<AnimatedSprite> mPlayerSprites = new SparseArray<AnimatedSprite>();

    private String mServerIP = LOCALHOST_IP;
    private SocketServer<SocketConnectionClientConnector> mSocketServer;
    private ServerConnector<SocketConnection> mServerConnector;

    private int health = 0, maxHealth = 0, battleLevel = 0;

    private int numPlayers = 0;

    private int playerNumber = -1;

    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    private BitmapTextureAtlas mTamaBitmapTextureAtlas, mOnScreenControlTexture;
    private TiledTextureRegion mTamaTextureRegion;
    private TextureRegion mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion;

    private boolean loadComplete = false;

    private Sprite crosshairSprite;

    private Scene scene;

    // ===========================================================
    // Constructors
    // ===========================================================

    public TamaBattle()
    {
	this.initMessagePool();
    }

    private void initMessagePool()
    {
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE, AddSpriteServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_ADD_SPRITE, AddSpriteClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ID_PLAYER, GetPlayerIdServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE, MoveSpriteClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_FIRE_BULLET, FireBulletClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET, FireBulletServerMessage.class);

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine()
    {
	this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);

	this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
	final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));

	/**
	 * Activate multitouch
	 */
	try
	{
	    if (MultiTouch.isSupported(this))
	    {
		engine.setTouchController(new MultiTouchController());
		if (MultiTouch.isSupportedDistinct(this))
		{
		    // Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_LONG).show();
		}
		else
		{
		    Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
		}
	    }
	    else
	    {
		Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
	    }
	} catch (final MultiTouchException e)
	{
	    Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
	}
	return engine;

    }

    @Override
    public void onLoadResources()
    {
	this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);
	this.mCrossHairTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "crosshair.png", 0, 33);

	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);

	BULLET_POOL = new BulletPool(mSpriteTextureRegion);

	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("animated_gfx/");
	this.mTamaBitmapTextureAtlas = new BitmapTextureAtlas(256, 512, TextureOptions.BILINEAR);
	this.mTamaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mTamaBitmapTextureAtlas, this, "animate_test.png", 0, 0, 3, 4);

	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
	this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

	this.mEngine.getTextureManager().loadTextures(this.mTamaBitmapTextureAtlas, this.mOnScreenControlTexture);
    }

    @Override
    public Scene onLoadScene()
    {
	// this.mEngine.registerUpdateHandler(new FPSLogger());

	scene = new Scene();
	scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

	Entity topLayer = new Entity();
	scene.attachChild(topLayer);

	crosshairSprite = new Sprite(0, 0, mCrossHairTextureRegion);
	crosshairSprite.setVisible(false);
	topLayer.attachChild(crosshairSprite);

	scene.setOnSceneTouchListener(new IOnSceneTouchListener()
	{
	    @Override
	    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    if ((pSceneTouchEvent.getX() > mPlayerSprites.get(playerNumber).getX() && playerNumber % 2 != 0) || (pSceneTouchEvent.getX() < mPlayerSprites.get(playerNumber).getX() && playerNumber % 2 == 0))
		    {
			// Fire a bullet
			Debug.d("Fire bullet!");
			try
			{
			    crosshairSprite.setPosition(pSceneTouchEvent.getX() - crosshairSprite.getWidth() * 0.5f, pSceneTouchEvent.getY() - crosshairSprite.getHeight() * 0.5f);
			    crosshairSprite.setVisible(true);
			    final FireBulletClientMessage message = (FireBulletClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_FIRE_BULLET);
			    message.set(playerNumber, 0, pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), false);

			    mServerConnector.sendClientMessage(message);

			    TamaBattle.this.mMessagePool.recycleMessage(message);
			} catch (final IOException e)
			{
			    Debug.e(e);
			}
		    }

		    return true;
		}
		else
		{
		    return true;
		}
	    }
	});

	scene.setTouchAreaBindingEnabled(true);
	return scene;
    }

    @Override
    public void onLoadComplete()
    {
	this.loadComplete = true;
    }

    @Override
    protected Dialog onCreateDialog(final int pID)
    {
	switch (pID)
	{
	case DIALOG_SHOW_SERVER_IP_ID:
	    try
	    {
		return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Your Server-IP ...").setCancelable(false).setMessage("The IP of your Server is:\n" + WifiUtils.getWifiIPv4Address(this)).setPositiveButton(android.R.string.ok, null).create();
	    } catch (final UnknownHostException e)
	    {
		return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Your Server-IP ...").setCancelable(false).setMessage("Error retrieving IP of your Server: " + e).setPositiveButton(android.R.string.ok, new OnClickListener()
		{
		    @Override
		    public void onClick(final DialogInterface pDialog, final int pWhich)
		    {
			TamaBattle.this.finish();
		    }
		}).create();
	    }
	case DIALOG_ENTER_SERVER_IP_ID:
	    final EditText ipEditText = new EditText(this);
	    return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Enter Server-IP ...").setCancelable(false).setView(ipEditText).setPositiveButton("Connect", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.mServerIP = ipEditText.getText().toString();
		    TamaBattle.this.initClient();
		}
	    }).setNegativeButton(android.R.string.cancel, new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.finish();
		}
	    }).create();
	case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
	    return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Start new multiplayer battle").setCancelable(false).setPositiveButton("Join", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.showDialog(DIALOG_ENTER_SERVER_IP_ID);
		}
	    }).setNegativeButton("Host", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.initServerAndClient();
		    TamaBattle.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
		}
	    }).create();
	default:
	    return super.onCreateDialog(pID);
	}
    }

    @Override
    protected void onDestroy()
    {
	Debug.d("Running onDestroy()...");
	if (this.mSocketServer != null)
	{
	    try
	    {
		this.mSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
	    } catch (final IOException e)
	    {
		Debug.e(e);
	    }
	    this.mSocketServer.terminate();
	}

	if (this.mServerConnector != null)
	{
	    this.mServerConnector.terminate();
	}

	super.onDestroy();
	Debug.d("Finished onDestroy()...");
    }

    @Override
    public void finish()
    {
	Debug.d("Running finish()...");
	Intent returnIntent = new Intent();
	returnIntent.putExtra(MultiplayerConstants.XP_GAIN, 5);
	setResult(RESULT_OK, returnIntent);
	super.finish();
    }

    @Override
    public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent)
    {
	switch (pKeyCode)
	{
	case KeyEvent.KEYCODE_BACK:
	    this.finish();
	    return true;
	}
	return super.onKeyUp(pKeyCode, pEvent);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Called because the bullet can be recycled
     */
    private void sendBulletToBulletPool(Sprite pBulletSprite)
    {
	synchronized (BULLET_POOL)
	{
	    BULLET_POOL.recyclePoolItem(pBulletSprite);
	}
    }

    /**
     * The player fired, we need a bullet to display
     */
    private Sprite getBulletFromBulletPool()
    {
	synchronized (BULLET_POOL)
	{
	    return BULLET_POOL.obtainPoolItem();
	}
    }

    public void addSprite(final int pID, final float pX, final float pY)
    {
	final Scene scene = this.mEngine.getScene();
	final Sprite face = new Sprite(0, 0, this.mSpriteTextureRegion);
	face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
	face.setUserData(pID);
	this.mSprites.put(pID, face);
	scene.registerTouchArea(face);
	scene.attachChild(face);
    }

    /**
     * 
     * @param pID
     *            ID of the new bullet sprite.
     * @param srcID
     *            ID of the originating source.
     * @param pX
     *            X-coordinate
     * @param pY
     *            Y-coordinate
     */
    public void fireBullet(final int playerID, final int pID, final float pX, final float pY)
    {
	// final Sprite bullet = new Sprite(0, 0, this.mSpriteTextureRegion);
	final Sprite bullet = getBulletFromBulletPool();
	AnimatedSprite player = mPlayerSprites.get(playerID);
	if (playerID % 2 == 0)
	    bullet.setPosition(player.getX(), player.getY() + player.getHeight() / 2);
	else
	    bullet.setPosition(player.getX() + player.getWidth(), player.getY() + player.getHeight() / 2);
	bullet.setUserData(pID);
	if (playerID == playerNumber)
	{
	    int c = 1000;
	    float xDim = pX - bullet.getX();
	    float yDim = pY - bullet.getY();
	    float nY = yDim / Math.abs(xDim);
	    float nX = xDim / Math.abs(xDim);
	    final PhysicsHandler physicsHandler = new PhysicsHandler(bullet);
	    physicsHandler.setVelocity(c * nX, c * nY);
	    bullet.registerUpdateHandler(physicsHandler);
	    bullet.registerUpdateHandler(new IUpdateHandler()
	    {

		@Override
		public void reset()
		{

		}

		@Override
		public void onUpdate(float arg0)
		{
		    try
		    {
			final MoveSpriteClientMessage message = (MoveSpriteClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
			message.set(playerNumber, (Integer) bullet.getUserData(), bullet.getX(), bullet.getY(), false);
			mServerConnector.sendClientMessage(message);
			TamaBattle.this.mMessagePool.recycleMessage(message);
		    } catch (Exception e)
		    {
			e.printStackTrace();
		    }

		}
	    });
	}
	bullet.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		if (bullet.getX() > CAMERA_WIDTH + 100 || bullet.getY() > CAMERA_HEIGHT + 100 || bullet.getX() < -100 || bullet.getY() < -100)
		{
		    Debug.d("Recycled bullet!");

		    try
		    {
			final MoveSpriteClientMessage message = (MoveSpriteClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
			message.set(playerNumber, (Integer) bullet.getUserData(), bullet.getX(), bullet.getY(), false);
			mServerConnector.sendClientMessage(message);
			TamaBattle.this.mMessagePool.recycleMessage(message);
		    } catch (Exception e)
		    {
			e.printStackTrace();
		    }
		    // bullet.clearUpdateHandlers();
		    mSprites.remove(pID);
		    sendBulletToBulletPool(bullet);
		}
	    }
	});
	mSprites.put(pID, bullet);
	scene.attachChild(bullet);
    }

    /**
     * Adds a player sprite to the screen.
     * 
     * @param pID
     *            Player's ID
     * @param pX
     *            X-coordinate
     * @param pY
     *            Y-coordinate
     */
    public void addPlayerSprite(final int pID, final float pX, final float pY)
    {
	/**
	 * Don't add until the engine is done loading
	 */
	while (!this.loadComplete)
	{
	    try
	    {
		Thread.sleep(500l);
	    } catch (InterruptedException e)
	    {
		e.printStackTrace();
	    }
	}

	/**
	 * Don't add sprite if it already exists
	 */
	if (this.mPlayerSprites.get(pID) != null)
	    return;

	Debug.d("[PLAYER " + playerNumber + "] Adding player " + pID + "... ");

	final AnimatedSprite player = new AnimatedSprite(0, 0, this.mTamaTextureRegion.deepCopy());
	if (pX != 0 && pY != 0)
	    player.setPosition(pX, pY);
	else
	{
	    float x = 0, y = 0;
	    if (pID % 2 == 0) // spawn on right side
	    {
		x = CAMERA_WIDTH - player.getWidth() - 25;
		y = CAMERA_HEIGHT / 2;
	    }
	    else
	    // spawn on left side
	    {
		x = player.getWidth() + 25;
		y = CAMERA_HEIGHT / 2;
	    }
	    player.setPosition(x - player.getWidth() * 0.5f, y - player.getHeight() * 0.5f);
	}

	if (pID % 2 == 0)
	    player.animate(new long[] { 300, 300, 300 }, 3, 5, true); // make player face left
	else
	    player.animate(new long[] { 300, 300, 300 }, 6, 8, true); // make player face right

	player.setUserData(pID);

	synchronized (mPlayerSprites)
	{
	    this.mPlayerSprites.put(pID, player);
	}

	// If you are adding your own sprite.
	if (pID == playerNumber)
	{
	    /**
	     * Add analog controls
	     */
	    final PhysicsHandler physicsHandler = new PhysicsHandler(player);
	    player.registerUpdateHandler(physicsHandler);

	    /* Velocity control (left). */
	    final int spacing = 40;
	    final int x1 = spacing;
	    final int y1 = CAMERA_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight() - spacing;
	    final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(x1, y1, mCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener()
	    {
		@Override
		public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl,
			final float pValueX, final float pValueY)
		{
		    physicsHandler.setVelocity(pValueX * 100, pValueY * 100);
		    // Debug.d("pValueX = " + pValueX + ", pValueY = " + pValueY);
		}

		@Override
		public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl)
		{
		    /* Nothing. */
		}
	    });
	    velocityOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	    velocityOnScreenControl.getControlBase().setAlpha(0.5f);

	    scene.setChildScene(velocityOnScreenControl);

	    /**
	     * Send position of sprite to server on each update
	     */
	    player.registerUpdateHandler(new IUpdateHandler()
	    {

		@Override
		public void reset()
		{

		}

		@Override
		public void onUpdate(float arg0)
		{
		    try
		    {
			final MoveSpriteClientMessage message = (MoveSpriteClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
			message.set(playerNumber, playerNumber, player.getX(), player.getY(), true);

			mServerConnector.sendClientMessage(message);
			TamaBattle.this.mMessagePool.recycleMessage(message);
		    } catch (Exception e)
		    {
			e.printStackTrace();
		    }

		}
	    });
	}

	scene.registerTouchArea(player);
	scene.attachChild(player);
    }

    /**
     * Moves the sprite on the screen to position.
     * 
     * @param pID
     *            Sprite's ID
     * @param pX
     *            X-coordinate
     * @param pY
     *            Y-coordinate
     * @param isPlayer
     *            True if sprite is a player, False otherwise
     */
    public void moveSprite(final int pID, final float pX, final float pY, final boolean isPlayer)
    {

	final BaseSprite sprite;
	if (isPlayer)
	{
	    sprite = this.mPlayerSprites.get(pID);
	}
	else
	{
	    sprite = this.mSprites.get(pID);
	}

	if (sprite != null)
	{
	    sprite.setPosition(pX, pY);

	    if (!isPlayer)
		if (sprite.getX() > CAMERA_WIDTH || sprite.getY() > CAMERA_HEIGHT || sprite.getX() < 0 || sprite.getY() < 0)
		{
		    Debug.d("Recycled bullet!");
		    mSprites.remove(pID);
		    sendBulletToBulletPool((Sprite) sprite);
		}
	}

    }

    private void initServerAndClient()
    {
	this.initServer();

	/* Wait some time after the server has been started, so it actually can start up. */
	try
	{
	    Thread.sleep(500);
	} catch (final Throwable t)
	{
	    Debug.e(t);
	}

	this.initClient();
    }

    private void initServer()
    {
	this.mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ClientConnectorListener(), new ServerStateListener())
	{
	    @Override
	    protected SocketConnectionClientConnector newClientConnector(
		    final SocketConnection pSocketConnection) throws IOException
	    {
		SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);

		/**
		 * Receives player number request message from Client and sends Client their player number.
		 */
		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_REQUEST_ID, RequestPlayerIdClientMessage.class, new IClientMessageHandler<SocketConnection>()
		{
		    @Override
		    public void onHandleMessage(ClientConnector<SocketConnection> pClientConnector,
			    IClientMessage pClientMessage) throws IOException
		    {
			final GetPlayerIdServerMessage sMessage = (GetPlayerIdServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ID_PLAYER);
			sMessage.playerNumber = numPlayers;
			pClientConnector.sendServerMessage(sMessage);
			TamaBattle.this.mMessagePool.recycleMessage(sMessage);

			final AddSpriteServerMessage addPlayerMessage = (AddSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
			addPlayerMessage.set(0, numPlayers, 0, 0, true);
			try
			{
			    Thread.sleep(500l);
			    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(addPlayerMessage);
			    TamaBattle.this.mMessagePool.recycleMessage(addPlayerMessage);
			} catch (IOException e)
			{
			    e.printStackTrace();
			} catch (InterruptedException e)
			{
			    e.printStackTrace();
			}

			synchronized (mPlayerSprites)
			{
			    int key = 0;
			    for (int i = 0; i < mPlayerSprites.size(); i++)
			    {
				key = mPlayerSprites.keyAt(i);
				AnimatedSprite aSprite = mPlayerSprites.get(key);
				try
				{
				    final AddSpriteServerMessage apMessage = (AddSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
				    apMessage.set(0, key, aSprite.getX(), aSprite.getY(), true);
				    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(apMessage);
				    TamaBattle.this.mMessagePool.recycleMessage(apMessage);
				} catch (Exception e)
				{
				    e.printStackTrace();
				}
			    }
			}
		    }
		});

		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE, MoveSpriteClientMessage.class, new IClientMessageHandler<SocketConnection>()
		{

		    @Override
		    public void onHandleMessage(ClientConnector<SocketConnection> arg0,
			    IClientMessage clientMessage) throws IOException
		    {
			MoveSpriteClientMessage message = (MoveSpriteClientMessage) clientMessage;
			try
			{
			    final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE);
			    moveSpriteServerMessage.set(message.playerID, message.mID, message.mX, message.mY, message.mIsPlayer);

			    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(moveSpriteServerMessage);
			    TamaBattle.this.mMessagePool.recycleMessage(moveSpriteServerMessage);
			} catch (Exception e)
			{
			    e.printStackTrace();
			}
		    }

		});

		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_ADD_SPRITE, AddSpriteClientMessage.class, new IClientMessageHandler<SocketConnection>()
		{

		    @Override
		    public void onHandleMessage(ClientConnector<SocketConnection> arg0,
			    IClientMessage clientMessage) throws IOException
		    {
			AddSpriteClientMessage message = (AddSpriteClientMessage) clientMessage;
			try
			{
			    final AddSpriteServerMessage addSpriteServerMessage = (AddSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
			    addSpriteServerMessage.set(message.playerID, message.mID, message.mX, message.mY, message.mIsPlayer);

			    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(addSpriteServerMessage);
			    TamaBattle.this.mMessagePool.recycleMessage(addSpriteServerMessage);
			} catch (Exception e)
			{
			    e.printStackTrace();
			}
		    }

		});

		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_FIRE_BULLET, FireBulletClientMessage.class, new IClientMessageHandler<SocketConnection>()
		{

		    @Override
		    public void onHandleMessage(ClientConnector<SocketConnection> arg0,
			    IClientMessage clientMessage) throws IOException
		    {
			FireBulletClientMessage message = (FireBulletClientMessage) clientMessage;
			try
			{
			    final FireBulletServerMessage bMessage = (FireBulletServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET);
			    bMessage.set(message.playerID, TamaBattle.this.mSpriteIDCounter++, message.mX, message.mY, message.mIsPlayer);

			    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(bMessage);
			    TamaBattle.this.mMessagePool.recycleMessage(bMessage);
			} catch (Exception e)
			{
			    e.printStackTrace();
			}
		    }

		});

		return clientConnector;
	    }
	};

	this.mSocketServer.start();
    }

    private void initClient()
    {
	try
	{
	    this.mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ServerConnectorListener());

	    this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>()
	    {
		@Override
		public void onHandleMessage(
			final ServerConnector<SocketConnection> pServerConnector,
			final IServerMessage pServerMessage) throws IOException
		{
		    TamaBattle.this.finish();
		}
	    });

	    this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE, AddSpriteServerMessage.class, new IServerMessageHandler<SocketConnection>()
	    {
		@Override
		public void onHandleMessage(
			final ServerConnector<SocketConnection> pServerConnector,
			final IServerMessage pServerMessage) throws IOException
		{
		    final AddSpriteServerMessage addSpriteServerMessage = (AddSpriteServerMessage) pServerMessage;
		    if (addSpriteServerMessage.mIsPlayer)
		    {
			TamaBattle.this.addPlayerSprite(addSpriteServerMessage.mID, addSpriteServerMessage.mX, addSpriteServerMessage.mY);
			Debug.d("Adding player " + addSpriteServerMessage.mID);
		    }
		    else
		    {
			TamaBattle.this.addSprite(addSpriteServerMessage.mID, addSpriteServerMessage.mX, addSpriteServerMessage.mY);
		    }
		}
	    });

	    this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class, new IServerMessageHandler<SocketConnection>()
	    {
		@Override
		public void onHandleMessage(
			final ServerConnector<SocketConnection> pServerConnector,
			final IServerMessage pServerMessage) throws IOException
		{
		    final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage) pServerMessage;
		    if (moveSpriteServerMessage.mIsPlayer)
		    {
			if (moveSpriteServerMessage.mID != playerNumber)
			    TamaBattle.this.moveSprite(moveSpriteServerMessage.mID, moveSpriteServerMessage.mX, moveSpriteServerMessage.mY, moveSpriteServerMessage.mIsPlayer);
		    }
		    else
		    {
			if (moveSpriteServerMessage.playerID != playerNumber)
			    TamaBattle.this.moveSprite(moveSpriteServerMessage.mID, moveSpriteServerMessage.mX, moveSpriteServerMessage.mY, moveSpriteServerMessage.mIsPlayer);
		    }
		}
	    });

	    /**
	     * Receives the player number from the server.
	     */
	    this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_ID_PLAYER, GetPlayerIdServerMessage.class, new IServerMessageHandler<SocketConnection>()
	    {
		@Override
		public void onHandleMessage(
			final ServerConnector<SocketConnection> pServerConnector,
			final IServerMessage pServerMessage) throws IOException
		{
		    final GetPlayerIdServerMessage serverMessage = (GetPlayerIdServerMessage) pServerMessage;
		    TamaBattle.this.playerNumber = serverMessage.playerNumber;
		    Debug.d("I am player " + playerNumber);
		}
	    });

	    this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET, FireBulletServerMessage.class, new IServerMessageHandler<SocketConnection>()
	    {
		@Override
		public void onHandleMessage(
			final ServerConnector<SocketConnection> pServerConnector,
			final IServerMessage pServerMessage) throws IOException
		{
		    final FireBulletServerMessage message = (FireBulletServerMessage) pServerMessage;
		    TamaBattle.this.fireBullet(message.playerID, message.mID, message.mX, message.mY);
		}
	    });

	    this.mServerConnector.getConnection().start();

	    Intent intent = getIntent();
	    this.health = intent.getIntExtra(MultiplayerConstants.HEALTH, 0);
	    this.maxHealth = intent.getIntExtra(MultiplayerConstants.MAX_HEALTH, 0);
	    this.battleLevel = intent.getIntExtra(MultiplayerConstants.BATTLE_LEVEL, 0);

	    this.mServerConnector.sendClientMessage(new RequestPlayerIdClientMessage());

	} catch (final Throwable t)
	{
	    Debug.e(t);
	}
    }

    private void log(final String pMessage)
    {
	Debug.d(pMessage);
    }

    private void toast(final String pMessage)
    {
	this.log(pMessage);
	this.runOnUiThread(new Runnable()
	{
	    @Override
	    public void run()
	    {
		Toast.makeText(TamaBattle.this, pMessage, Toast.LENGTH_SHORT).show();
	    }
	});
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * Client sends this message to the server to request it's player ID number
     * 
     * @author Jonathan
     * 
     */
    public static class RequestPlayerIdClientMessage extends ClientMessage
    {

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_CLIENT_REQUEST_ID;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream arg0) throws IOException
	{

	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream arg0) throws IOException
	{

	}

    }

    /**
     * Server sends this message to client telling it which player it is.
     * 
     * @author Jonathan
     * 
     */
    public static class GetPlayerIdServerMessage extends ServerMessage
    {
	private int playerNumber;

	public GetPlayerIdServerMessage()
	{

	}

	public GetPlayerIdServerMessage(final int playerNumber)
	{
	    this.playerNumber = playerNumber;
	}

	public void set(final int playerNumber)
	{
	    this.playerNumber = playerNumber;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_ID_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
		throws IOException
	{
	    this.playerNumber = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
		throws IOException
	{
	    pDataOutputStream.writeInt(this.playerNumber);
	}

    }

    public static class AddSpriteServerMessage extends SpriteServerMessage
    {
	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_ADD_SPRITE;
	}
    }

    public static class AddSpriteClientMessage extends SpriteClientMessage
    {
	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_CLIENT_ADD_SPRITE;
	}
    }

    /**
     * Server sends this message to all clients telling them to move some sprite to a specific location.
     * 
     * @author Jonathan
     * 
     */
    public static class MoveSpriteServerMessage extends SpriteServerMessage
    {
	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_MOVE_SPRITE;
	}
    }

    /**
     * Client sends this message to server to tell it to move a sprite to a specified location.
     * 
     * @author Jonathan
     * 
     */
    public static class MoveSpriteClientMessage extends SpriteClientMessage
    {
	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_CLIENT_MOVE_SPRITE;
	}

    }

    public static class FireBulletServerMessage extends SpriteServerMessage
    {
	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_FIRE_BULLET;
	}
    }

    public static class FireBulletClientMessage extends SpriteClientMessage
    {

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_CLIENT_FIRE_BULLET;
	}

    }

    public static class RemoveSpriteServerMessage extends SpriteServerMessage
    {

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_REMOVE_SPRITE;
	}

    }

    public static abstract class SpriteClientMessage extends ClientMessage
    {

	int playerID;
	int mID;
	float mX;
	float mY;
	boolean mIsPlayer;

	public SpriteClientMessage()
	{

	}

	public SpriteClientMessage(final int playerID, final int pID, final float pX,
		final float pY, final boolean pIsPlayer)
	{
	    this.playerID = playerID;
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	    this.mIsPlayer = pIsPlayer;
	}

	public void set(final int playerID, final int pID, final float pX, final float pY,
		final boolean pIsPlayer)
	{
	    this.playerID = playerID;
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	    this.mIsPlayer = pIsPlayer;
	}

	public abstract short getFlag();

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
		throws IOException
	{
	    this.playerID = pDataInputStream.readInt();
	    this.mID = pDataInputStream.readInt();
	    this.mX = pDataInputStream.readFloat();
	    this.mY = pDataInputStream.readFloat();
	    this.mIsPlayer = pDataInputStream.readBoolean();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
		throws IOException
	{
	    pDataOutputStream.writeInt(this.playerID);
	    pDataOutputStream.writeInt(this.mID);
	    pDataOutputStream.writeFloat(this.mX);
	    pDataOutputStream.writeFloat(this.mY);
	    pDataOutputStream.writeBoolean(this.mIsPlayer);
	}

    }

    public static abstract class SpriteServerMessage extends ServerMessage
    {

	int playerID;
	int mID;
	float mX;
	float mY;
	boolean mIsPlayer;

	public SpriteServerMessage()
	{

	}

	public SpriteServerMessage(final int playerID, final int pID, final float pX,
		final float pY, final boolean pIsPlayer)
	{
	    this.playerID = playerID;
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	    this.mIsPlayer = pIsPlayer;
	}

	public void set(final int playerID, final int pID, final float pX, final float pY,
		final boolean pIsPlayer)
	{
	    this.playerID = playerID;
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	    this.mIsPlayer = pIsPlayer;
	}

	public abstract short getFlag();

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
		throws IOException
	{
	    this.playerID = pDataInputStream.readInt();
	    this.mID = pDataInputStream.readInt();
	    this.mX = pDataInputStream.readFloat();
	    this.mY = pDataInputStream.readFloat();
	    this.mIsPlayer = pDataInputStream.readBoolean();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
		throws IOException
	{
	    pDataOutputStream.writeInt(this.playerID);
	    pDataOutputStream.writeInt(this.mID);
	    pDataOutputStream.writeFloat(this.mX);
	    pDataOutputStream.writeFloat(this.mY);
	    pDataOutputStream.writeBoolean(this.mIsPlayer);
	}

    }

    /**
     * Checks to see if you have connected to the server.
     * 
     * @author Jonathan
     * 
     */
    private class ServerConnectorListener implements ISocketConnectionServerConnectorListener
    {
	@Override
	public void onStarted(final ServerConnector<SocketConnection> pConnector)
	{
	    TamaBattle.this.toast("CLIENT: Connected to server.");
	}

	@Override
	public void onTerminated(final ServerConnector<SocketConnection> pConnector)
	{
	    TamaBattle.this.toast("CLIENT: Disconnected from Server...");
	    TamaBattle.this.finish();
	}
    }

    /**
     * Listens for changes to the server state (Started or Terminated)
     * 
     * @author Jonathan
     * 
     */
    private class ServerStateListener implements
	    ISocketServerListener<SocketConnectionClientConnector>
    {
	@Override
	public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer)
	{
	    TamaBattle.this.toast("SERVER: Started.");
	}

	@Override
	public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer)
	{
	    TamaBattle.this.toast("SERVER: Terminated.");
	}

	@Override
	public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer,
		final Throwable pThrowable)
	{
	    Debug.e(pThrowable);
	    TamaBattle.this.toast("SERVER: Exception: " + pThrowable);
	}
    }

    /**
     * Listens for clients that connect to server.
     * 
     * @author Jonathan
     * 
     */
    private class ClientConnectorListener implements ISocketConnectionClientConnectorListener
    {
	@Override
	public void onStarted(final ClientConnector<SocketConnection> pConnector)
	{
	    TamaBattle.this.toast("SERVER: Client connected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
	    numPlayers++;
	    Debug.d("Number of players: " + numPlayers);
	}

	@Override
	public void onTerminated(final ClientConnector<SocketConnection> pConnector)
	{
	    TamaBattle.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
	    numPlayers--;
	    Debug.d("Number of players: " + numPlayers);
	}
    }
}
