package com.tamaproject.multiplayer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.anddev.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.tamaproject.BaseAndEngineGame;
import com.tamaproject.adt.messages.client.ClientMessageFlags;
import com.tamaproject.adt.messages.server.ConnectionCloseServerMessage;
import com.tamaproject.adt.messages.server.ServerMessageFlags;
import com.tamaproject.multiplayer.BattleServer.IBattleServerListener;
import com.tamaproject.multiplayer.BattleServerConnector.IBattleServerConnectorListener;
import com.tamaproject.util.MultiplayerConstants;
import com.tamaproject.util.TamaBattleConstants;

/**
 * Multiplayer battle mode between multiple Tamagotchis.
 * 
 * @author Jonathan
 * 
 */
public class TamaBattle extends BaseAndEngineGame implements ClientMessageFlags,
	ServerMessageFlags, TamaBattleConstants, IBattleServerConnectorListener,
	IBattleServerListener, BattleMessages
{
    // ===========================================================
    // Constants
    // ===========================================================

    private static final String LOCALHOST_IP = "127.0.0.1";

    private static BulletPool BULLET_POOL;

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;

    private BitmapTextureAtlas mBitmapTextureAtlas, mFontTexture;
    private TextureRegion mSpriteTextureRegion, mCrossHairTextureRegion, mArrowTextureRegion;
    private Font mFont;
    private final SparseArray<Sprite> mSprites = new SparseArray<Sprite>();
    private final SparseArray<AnimatedSprite> mPlayerSprites = new SparseArray<AnimatedSprite>();

    private Text ipText;

    private Entity topLayer, bottomLayer;

    private String mServerIP = LOCALHOST_IP;
    private BattleServer mBattleServer;
    private BattleServerConnector mServerConnector;

    private int health = 0, maxHealth = 0, battleLevel = 0;

    int playerNumber = -1;
    int numPlayers = 1;
    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    private BitmapTextureAtlas mTamaBitmapTextureAtlas, mOnScreenControlTexture;
    private TiledTextureRegion mTamaTextureRegion;
    private TextureRegion mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion;

    private boolean loadComplete = false;

    private Sprite crosshairSprite;

    private Scene scene;
    private Scene lobbyScene;

    private boolean isServer = false;

    private final List<Sprite> bulletsToRemove = new ArrayList<Sprite>();

    private String IP;

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
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER, SendPlayerStatsClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER, SendPlayerStatsServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MODIFY_PLAYER, ModifyPlayerStatsServerMessage.class);
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
		Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
	    }
	} catch (final MultiTouchException e)
	{
	    Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
	}
	return engine;

    }

    @Override
    public void onLoadResources()
    {
	this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "particle_point.png", 0, 0);
	this.mCrossHairTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "crosshair.png", 0, 33);
	this.mArrowTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "down_arrow.png", 0, 74);

	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);

	BULLET_POOL = new BulletPool(mSpriteTextureRegion);

	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("animated_gfx/");
	this.mTamaBitmapTextureAtlas = new BitmapTextureAtlas(256, 512, TextureOptions.BILINEAR);
	this.mTamaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mTamaBitmapTextureAtlas, this, "animate_test.png", 0, 0, 3, 4);

	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
	this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

	this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mFont = FontFactory.createFromAsset(mFontTexture, this, "ITCKRIST.TTF", 24, true, Color.WHITE);
	this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
	this.mEngine.getFontManager().loadFont(this.mFont);

	this.mEngine.getTextureManager().loadTextures(this.mTamaBitmapTextureAtlas, this.mOnScreenControlTexture);
    }

    private void waitTime(long time)
    {
	try
	{
	    Thread.sleep(time);
	} catch (InterruptedException e)
	{
	    e.printStackTrace();
	}
    }

    private void createLobbyScene()
    {
	Debug.d("Creating lobby...");
	lobbyScene = new Scene();
	lobbyScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
	lobbyScene.setBackgroundEnabled(true);
	ipText = new Text(15, 15, mFont, "Server IP: " + IP);
	lobbyScene.attachChild(ipText);
	final Text startText = new Text(15, 15, mFont, "Start Game");
	final Rectangle startButton = new Rectangle(0, 0, startText.getWidth() + 30, startText.getHeight() + 30)
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		Debug.d("Touched start button...");
		if (pSceneTouchEvent.isActionDown())
		{
		    this.setColor(1, 0, 0);
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    mEngine.setScene(scene);
		    return true;
		}
		return true;
	    }
	};

	startButton.setColor(0, 1, 0);
	startButton.setPosition(CAMERA_WIDTH - startButton.getWidth() - 20, CAMERA_HEIGHT - startButton.getHeight() - 20);
	startButton.attachChild(startText);
	lobbyScene.registerTouchArea(startButton);
	lobbyScene.attachChild(startButton);
	if (isServer)
	{
	    final Text playerText = new Text(100, 100, mFont, "Player " + playerNumber + ", Server");
	    lobbyScene.attachChild(playerText);
	}
	else
	{
	    final Text waitingText = new Text(0, 0, mFont, "Waiting for players...");
	    waitingText.setPosition(CAMERA_WIDTH * 0.5f - waitingText.getWidth() * 0.5f, CAMERA_HEIGHT / 2);
	    lobbyScene.attachChild(waitingText);
	}
	lobbyScene.setTouchAreaBindingEnabled(true);
    }

    @Override
    public Scene onLoadScene()
    {
	// this.mEngine.registerUpdateHandler(new FPSLogger());

	this.createLobbyScene();
	scene = new Scene();
	scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

	topLayer = new Entity();
	bottomLayer = new Entity();
	scene.attachChild(bottomLayer);
	scene.attachChild(topLayer);

	ipText = new Text(15, 15, mFont, "Server IP: " + IP);
	topLayer.attachChild(ipText);
	while (playerNumber == -1)
	{
	    waitTime(500);
	}
	final Text playerNumText = new Text(0, 0, mFont, "Player " + playerNumber);
	playerNumText.setPosition(CAMERA_WIDTH - playerNumText.getWidth() - 15, 15);
	topLayer.attachChild(playerNumText);

	crosshairSprite = new Sprite(0, 0, mCrossHairTextureRegion);
	crosshairSprite.setVisible(false);
	topLayer.attachChild(crosshairSprite);

	final Line verticalLine = new Line(CAMERA_WIDTH / 2, 0, CAMERA_WIDTH / 2, CAMERA_HEIGHT);
	bottomLayer.attachChild(verticalLine);

	scene.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		synchronized (bulletsToRemove)
		{
		    for (Sprite s : bulletsToRemove)
		    {
			int id = ((BulletInfo) s.getUserData()).getID();
			mSprites.remove(id);
			sendBulletToBulletPool(s);
		    }
		    bulletsToRemove.clear();
		}
	    }
	});

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
	this.mEngine.setScene(lobbyScene);
    }

    @Override
    protected Dialog onCreateDialog(final int pID)
    {
	switch (pID)
	{
	case DIALOG_SHOW_SERVER_IP_ID:
	    try
	    {
		IP = WifiUtils.getWifiIPv4Address(this);
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
	    SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	    String savedIP = sharedPreferences.getString("BATTLEIP", "");
	    final EditText ipEditText = new EditText(this);
	    ipEditText.setText(savedIP);
	    return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Enter Server-IP ...").setCancelable(false).setView(ipEditText).setPositiveButton("Connect", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.mServerIP = ipEditText.getText().toString();
		    TamaBattle.this.savePreferences("BATTLEIP", ipEditText.getText().toString());
		    IP = ipEditText.getText().toString();
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
	if (this.mBattleServer != null)
	{
	    try
	    {
		this.mBattleServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
	    } catch (final IOException e)
	    {
		Debug.e(e);
	    }
	    this.mBattleServer.terminate();
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

    @Override
    /**
     * Sends a message to all clients to add new player sprites onto the screen.
     */
    public void updateAllPlayerSprites()
    {
	synchronized (mPlayerSprites)
	{
	    int key = 0;
	    for (int i = 0; i < mPlayerSprites.size(); i++)
	    {
		key = mPlayerSprites.keyAt(i);
		AnimatedSprite aSprite = mPlayerSprites.get(key);
		try
		{
		    final AddSpriteServerMessage apMessage = (AddSpriteServerMessage) mBattleServer.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
		    apMessage.set(0, key, aSprite.getX(), aSprite.getY(), true);
		    mBattleServer.sendBroadcastServerMessage(apMessage);
		    mBattleServer.mMessagePool.recycleMessage(apMessage);
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}

    }

    // ===========================================================
    // Methods
    // ===========================================================

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
	this.mBattleServer = new BattleServer(new ClientConnectorListener(), new ServerStateListener(), this);
	this.mBattleServer.start();
	this.isServer = true;
    }

    private void initClient()
    {
	try
	{
	    this.mServerConnector = new BattleServerConnector(mServerIP, new ServerConnectorListener(), this);
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

    /**
     * Called because the bullet can be recycled
     */
    private void sendBulletToBulletPool(final Sprite pBulletSprite)
    {
	synchronized (BULLET_POOL)
	{
	    pBulletSprite.clearUpdateHandlers();
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
	{
	    bullet.setPosition(player.getX() - bullet.getWidth(), player.getY() + player.getHeight() / 2);
	    bullet.setColor(1.0f, 1.0f, 0.0f);
	}
	else
	{
	    bullet.setPosition(player.getX() + player.getWidth() + bullet.getWidth(), player.getY() + player.getHeight() / 2);
	    bullet.setColor(0.0f, 1.0f, 1.0f);
	}
	bullet.setUserData(new BulletInfo(playerID, pID));

	if (isServer)
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
		    int bulletOwner = ((BulletInfo) bullet.getUserData()).getPlayerID();
		    for (int i = 0; i < mPlayerSprites.size(); i++)
		    {
			final int key = mPlayerSprites.keyAt(i);
			if (bullet.collidesWith(mPlayerSprites.get(key)) && bulletOwner != key)
			{
			    if ((bulletOwner % 2 == 0 && key % 2 == 0) || (bulletOwner % 2 != 0 && key % 2 != 0))
				return;

			    Debug.d("Collision!");
			    PlayerInfo info = (PlayerInfo) mPlayerSprites.get(key).getUserData();
			    info.setHealth(info.getHealth() - 10);
			    mPlayerSprites.get(key).setUserData(info);
			    if (info.getHealth() <= 0)
				Debug.d("Player " + key + " has lost!");

			    Debug.d("Player " + key + "'s health: " + info.getHealth());

			    try
			    {
				final MoveSpriteClientMessage message = (MoveSpriteClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
				message.set(((BulletInfo) bullet.getUserData()).getPlayerID(), ((BulletInfo) bullet.getUserData()).getID(), -1, -1, false);
				mServerConnector.sendClientMessage(message);
				TamaBattle.this.mMessagePool.recycleMessage(message);
			    } catch (Exception e)
			    {
				e.printStackTrace();
			    }

			    // Remove bullet
			    bulletsToRemove.add(mSprites.get(pID));

			    try
			    {
				final SendPlayerStatsServerMessage spssMessage = (SendPlayerStatsServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER);
				spssMessage.set(info.getHealth(), info.getMaxHealth(), info.getBattleLevel(), info.getPlayerID());
				mBattleServer.sendBroadcastServerMessage(spssMessage);
				TamaBattle.this.mMessagePool.recycleMessage(spssMessage);
			    } catch (Exception e)
			    {
				e.printStackTrace();
			    }
			    return;
			}
		    }

		    try
		    {
			final MoveSpriteClientMessage message = (MoveSpriteClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
			message.set(((BulletInfo) bullet.getUserData()).getPlayerID(), ((BulletInfo) bullet.getUserData()).getID(), bullet.getX(), bullet.getY(), false);
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
		    bulletsToRemove.add(bullet);
		}
	    }
	});
	mSprites.put(pID, bullet);
	if (!bullet.hasParent())
	    bottomLayer.attachChild(bullet);
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
	    waitTime(500);
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
		    float x = player.getX();
		    float y = player.getY();

		    if (x < 0 || x + player.getWidth() > CAMERA_WIDTH)
		    {
			if (x < 0)
			    player.setPosition(0, y);
			else
			    player.setPosition(CAMERA_WIDTH - player.getWidth(), y);
		    }

		    x = player.getX();
		    if (y < 0 || y + player.getHeight() > CAMERA_HEIGHT)
		    {
			if (y < 0)
			    player.setPosition(x, 0);
			else
			    player.setPosition(x, CAMERA_HEIGHT - player.getHeight());
		    }

		    if (playerNumber % 2 == 0)
		    {
			if (player.getX() < CAMERA_WIDTH / 2)
			    player.setPosition(CAMERA_WIDTH / 2, player.getY());
		    }
		    else
		    {
			if (player.getX() + player.getWidth() > CAMERA_WIDTH / 2)
			    player.setPosition(CAMERA_WIDTH / 2 - player.getWidth(), player.getY());
		    }

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
	bottomLayer.attachChild(player);
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
	if (isServer)
	{
	    if (!isPlayer)
		return;
	    else if (isPlayer && pID == playerNumber)
		return;
	}
	else
	{
	    if (isPlayer && pID == playerNumber)
		return;
	    if (pX == -1 && pY == -1)
	    {
		bulletsToRemove.add(mSprites.get(pID));
		return;
	    }
	}
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

    @Override
    /**
     * Sets the playerNumber, which identifies the player
     */
    public void setPlayerNumber(final int playerNumber)
    {
	this.playerNumber = playerNumber;
	Debug.d("I am player " + playerNumber);
    }

    @Override
    /**
     * Sets the appropriate sprite's user data to reflect that player's stats.
     */
    public void setPlayerData(int playerID, int health, int maxHealth, int battleLevel)
    {
	try
	{
	    Debug.d("Waiting for sprite to be ready...");
	    while (this.mPlayerSprites.get(playerID) == null)
	    {
		waitTime(500);
	    }
	    Debug.d("Sprite ready!");
	    AnimatedSprite sprite = this.mPlayerSprites.get(playerID);

	    float ratio = (float) health / maxHealth;
	    if (sprite.getChildCount() == 0)
	    {
		final Rectangle healthBar = new Rectangle(sprite.getWidth() * 0.5f - BAR_LENGTH * 0.5f, sprite.getHeight() + 5, BAR_LENGTH, BAR_HEIGHT);
		healthBar.setColor(1, 1, 1);

		final Rectangle currHealthBar = new Rectangle(2, 2, ratio * (BAR_LENGTH - 4), BAR_HEIGHT - 4);
		currHealthBar.setColor(1, 0, 0);
		healthBar.attachChild(currHealthBar);

		sprite.attachChild(healthBar);

		if (playerID == playerNumber)
		{
		    final Sprite arrowSprite = new Sprite(0, 0, mArrowTextureRegion);
		    arrowSprite.setPosition(sprite.getWidth() * 0.5f - arrowSprite.getWidth() * 0.5f, -arrowSprite.getHeight() - 5);
		    sprite.attachChild(arrowSprite);
		}
	    }
	    else
	    {
		final Rectangle currHealthBar = (Rectangle) ((Rectangle) sprite.getFirstChild()).getFirstChild();
		currHealthBar.setSize(ratio * (BAR_LENGTH - 4), BAR_HEIGHT - 4);
	    }
	    sprite.setUserData(new PlayerInfo(health, maxHealth, battleLevel, playerID));
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    @Override
    /**
     * Sends updated player user data to all the clients.
     */
    public void updateAllPlayerInfo()
    {
	Debug.d("Sending updated player info to clients...");
	synchronized (mPlayerSprites)
	{
	    int key = 0;
	    for (int i = 0; i < mPlayerSprites.size(); i++)
	    {
		key = mPlayerSprites.keyAt(i);
		Debug.d("Waiting for sprite to be ready for player " + key + "...");
		while (this.mPlayerSprites.get(key) == null)
		{
		    waitTime(500);
		}
		Debug.d("Sprite Ready!");
		AnimatedSprite aSprite = mPlayerSprites.get(key);
		try
		{
		    final SendPlayerStatsServerMessage message = (SendPlayerStatsServerMessage) mBattleServer.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER);
		    Debug.d("Waiting for info to be ready for player " + key + "...");
		    while (aSprite.getUserData() == null)
		    {
			waitTime(500);
		    }
		    Debug.d("Info Ready!");
		    PlayerInfo info = (PlayerInfo) aSprite.getUserData();
		    message.set(info.getHealth(), info.getMaxHealth(), info.getBattleLevel(), info.getPlayerID());
		    mBattleServer.sendBroadcastServerMessage(message);
		    mBattleServer.mMessagePool.recycleMessage(message);
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}
    }

    /**
     * Updates the userData for the sprite that belongs to the specified player.
     * 
     * @param playerID
     *            Player's number.
     */
    public void updatePlayerInfo(final int playerID)
    {
	Debug.d("Sending updated player info for player " + playerID + " to clients...");
	AnimatedSprite aSprite = mPlayerSprites.get(playerID);
	try
	{
	    final SendPlayerStatsServerMessage message = (SendPlayerStatsServerMessage) mBattleServer.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER);
	    PlayerInfo info = (PlayerInfo) aSprite.getUserData();
	    message.set(info.getHealth(), info.getMaxHealth(), info.getBattleLevel(), info.getPlayerID());
	    mBattleServer.sendBroadcastServerMessage(message);
	    mBattleServer.mMessagePool.recycleMessage(message);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    @Override
    /**
     * Sends your own player information to the server, so that it can let the other clients know.
     */
    public void sendPlayerInfoToServer()
    {
	Debug.d("Sending player info to server...");
	try
	{
	    final SendPlayerStatsClientMessage message = (SendPlayerStatsClientMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER);
	    message.set(this.health, this.maxHealth, this.battleLevel, playerNumber);
	    mServerConnector.sendClientMessage(message);
	    this.mMessagePool.recycleMessage(message);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    private void savePreferences(String key, String value)
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	SharedPreferences.Editor editor = sharedPreferences.edit();
	editor.putString(key, value);
	editor.commit();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

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
	    if (isServer && lobbyScene != null)
	    {
		final Text temp = (Text) lobbyScene.getLastChild();
		numPlayers++;
		final Text pText = new Text(temp.getX(), temp.getY() + 50, mFont, "Player " + numPlayers + ", " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
		lobbyScene.attachChild(pText);
	    }
	}

	@Override
	public void onTerminated(final ClientConnector<SocketConnection> pConnector)
	{
	    TamaBattle.this.toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
	}
    }

}
