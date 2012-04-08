package com.tamaproject.multiplayer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
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
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;

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
import com.tamaproject.util.TextUtil;
import com.tamaproject.util.TextureUtil;

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

    private Music backgroundMusic, lobbyMusic;
    private Sound pewSound, fightSound;
    private BitmapTextureAtlas mFontTexture;
    public Hashtable<String, TextureRegion> listTR;
    private RepeatingSpriteBackground mBackground;
    private SpriteBackground orangeBackground;
    private Font mFont;
    private final SparseArray<Sprite> mSprites = new SparseArray<Sprite>();
    private final SparseArray<AnimatedSprite> mPlayerSprites = new SparseArray<AnimatedSprite>();
    private final HashMap<String, Integer> ipArray = new HashMap<String, Integer>();
    private final HashMap<String, Text> textIpArray = new HashMap<String, Text>();

    private Text ipText, winText, loseText;

    private Entity topLayer, bottomLayer;

    private String mServerIP = LOCALHOST_IP;
    private BattleServer mBattleServer;
    private BattleServerConnector mServerConnector;

    private int health = 0, maxHealth = 0, battleLevel = 0;

    int playerNumber = -1;

    private BitmapTextureAtlas mTamaBitmapTextureAtlas, mOnScreenControlTexture;
    private TiledTextureRegion mTamaTextureRegion;
    private TextureRegion mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion;

    private boolean loadComplete = false;

    private Sprite crosshairSprite;

    private Scene scene;
    private Scene lobbyScene, endScene, deathMatchWarningScene;

    private boolean isServer = false;

    private final List<Sprite> bulletsToRemove = new ArrayList<Sprite>();

    private int team1 = 0, team2 = 0;

    private String IP;

    private boolean isDeathMatch = false;
    private boolean voteDeathMatch = false;

    private int lowestBattleLevel = 1;
    private int numPlayers = 0;

    private AnimatedSprite me;

    // ===========================================================
    // Constructors
    // ===========================================================

    public TamaBattle()
    {

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
	final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera).setNeedsMusic(true).setNeedsSound(true));

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

	loadOptions();

	return engine;

    }

    @Override
    public void onLoadResources()
    {
	// Load textures
	this.listTR = TextureUtil.loadTextures(this, this.mEngine, new String[] { new String("gfx/") });
	BULLET_POOL = new BulletPool(listTR.get("particle_point.png"));

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

	this.mBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_grass_inverted.png"));
	this.orangeBackground = new SpriteBackground(new Sprite(0, 0, listTR.get("orange.png")));

	// Load sounds
	SoundFactory.setAssetBasePath("mfx/");
	try
	{
	    this.pewSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "pew.mp3");
	    this.fightSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fight.mp3");
	} catch (final IOException e)
	{
	    Debug.e(e);
	}

	MusicFactory.setAssetBasePath("mfx/");
	try
	{
	    this.backgroundMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "bionic_belly_button.mp3");
	    this.backgroundMusic.setLooping(true);
	} catch (final IOException e)
	{
	    Debug.e(e);
	}

	try
	{
	    this.lobbyMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "playing_with_power.mp3");
	    this.lobbyMusic.setLooping(true);
	} catch (final IOException e)
	{
	    Debug.e(e);
	}
    }

    @Override
    public void pauseSound()
    {
	if (this.lobbyMusic.isPlaying())
	    this.lobbyMusic.pause();
	if (this.backgroundMusic.isPlaying())
	    this.backgroundMusic.pause();
    }

    @Override
    public void resumeSound()
    {
	if (!mEngine.getScene().equals(lobbyScene))
	    this.backgroundMusic.resume();
	else
	    this.lobbyMusic.resume();
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

    private void loadDeathMatchWarningScene()
    {
	deathMatchWarningScene = new Scene();
	deathMatchWarningScene.setBackground(orangeBackground);
	final Text warningText = new Text(0, 0, mFont, "Warning, death match is enabled!\nIf your Tamagotchi dies the matrix, it dies in real life.", HorizontalAlign.CENTER);
	warningText.setPosition(CAMERA_WIDTH / 2 - warningText.getWidth() / 2, CAMERA_HEIGHT / 2 - warningText.getHeight() / 2);
	final Sprite dmIcon = new Sprite(0, 0, listTR.get("skull.png"));
	dmIcon.setPosition(CAMERA_WIDTH / 2 - dmIcon.getWidth() / 2, warningText.getY() + warningText.getHeight() + 30);
	deathMatchWarningScene.attachChild(dmIcon);
	deathMatchWarningScene.attachChild(warningText);
    }

    private void loadLobbyScene()
    {
	Debug.d("Creating lobby...");
	final int padding = 40;
	lobbyScene = new Scene();
	// lobbyScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
	lobbyScene.setBackground(orangeBackground);
	lobbyScene.setBackgroundEnabled(true);
	ipText = new Text(15, 15, mFont, "Multiplayer Battle Mode - Server IP: " + IP);
	lobbyScene.attachChild(ipText);
	final Sprite noSprite = new Sprite(0, 0, listTR.get("not_allowed.png"));
	if (voteDeathMatch)
	    noSprite.setVisible(false);
	else
	    noSprite.setVisible(true);
	final Sprite voteDeathMatchButton = new Sprite(0, 0, listTR.get("skull.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    if (voteDeathMatch)
		    {
			voteDeathMatch = false;
			noSprite.setVisible(true);
		    }
		    else
		    {
			voteDeathMatch = true;
			noSprite.setVisible(false);
		    }
		    mServerConnector.sendVoteDeathMatch(voteDeathMatch);
		}
		else if (pSceneTouchEvent.isActionUp())
		{

		}
		return true;
	    }
	};

	voteDeathMatchButton.attachChild(noSprite);
	voteDeathMatchButton.setPosition(padding, CAMERA_HEIGHT - voteDeathMatchButton.getHeight() - padding);
	lobbyScene.attachChild(voteDeathMatchButton);
	lobbyScene.registerTouchArea(voteDeathMatchButton);

	if (isServer)
	{
	    final Text waitingText = new Text(0, 0, mFont, "Waiting for players to join...");
	    waitingText.setPosition(CAMERA_WIDTH - waitingText.getWidth() - 20, CAMERA_HEIGHT - waitingText.getHeight() - 20);

	    final Text startText = new Text(padding / 2, padding / 2, mFont, "Start Game");
	    final Rectangle startButton = new Rectangle(0, 0, startText.getWidth() + padding, startText.getHeight() + padding)
	    {
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
		    if (this.isVisible())
		    {
			if (pSceneTouchEvent.isActionDown())
			{
			    Debug.d("Touched start button...");
			    this.setColor(0, 1, 0);
			}
			else if (pSceneTouchEvent.isActionUp())
			{
			    if (mBattleServer.getNumPlayers() == mBattleServer.getDeathMatchVotes())
				mBattleServer.sendDeathMatchMessage(true);
			    mBattleServer.sendStartMessage();

			}
			return true;
		    }
		    else
			return false;
		}
	    };
	    startButton.setVisible(false);
	    startButton.registerUpdateHandler(new IUpdateHandler()
	    {

		@Override
		public void reset()
		{

		}

		@Override
		public void onUpdate(float arg0)
		{
		    if (!startButton.isVisible() && mBattleServer.getNumPlayers() > 1)
		    {
			startButton.setVisible(true);
			waitingText.setVisible(false);
			startButton.unregisterUpdateHandler(this);
		    }
		}
	    });

	    startButton.setColor(1, 0, 0);
	    startButton.setPosition(CAMERA_WIDTH - startButton.getWidth() - 20, CAMERA_HEIGHT - startButton.getHeight() - 20);
	    startButton.attachChild(startText);

	    lobbyScene.attachChild(waitingText);
	    lobbyScene.registerTouchArea(startButton);
	    lobbyScene.attachChild(startButton);

	    final Sprite tamaSprite = new Sprite(0, 0, listTR.get("tama.png"));
	    tamaSprite.setPosition(CAMERA_WIDTH - tamaSprite.getWidth() - 60, 60);
	    tamaSprite.registerEntityModifier(new LoopEntityModifier(new SequenceEntityModifier(new RotationModifier(4, 0, -360))));

	    lobbyScene.attachChild(tamaSprite);
	}
	else
	{
	    final Text waitingText = new Text(0, 0, mFont, "Waiting for host to start game...");
	    waitingText.setPosition(CAMERA_WIDTH * 0.5f - waitingText.getWidth() * 0.5f, CAMERA_HEIGHT / 2 - 10);

	    final Sprite tamaSprite = new Sprite(0, 0, listTR.get("tama.png"));
	    tamaSprite.setPosition(CAMERA_WIDTH / 2 - tamaSprite.getWidth() / 2, waitingText.getY() + waitingText.getHeight() + 50);
	    tamaSprite.registerEntityModifier(new LoopEntityModifier(new SequenceEntityModifier(new RotationModifier(4, 0, -360))));
	    lobbyScene.attachChild(tamaSprite);
	    lobbyScene.attachChild(waitingText); // must be attached last
	}
	lobbyScene.setTouchAreaBindingEnabled(true);
    }

    @Override
    public Scene onLoadScene()
    {
	// this.mEngine.registerUpdateHandler(new FPSLogger());

	this.enableVibrator();
	this.loadLobbyScene();
	this.loadDeathMatchWarningScene();

	endScene = new Scene();
	endScene.setBackground(orangeBackground);
	winText = new Text(0, 0, mFont, "You win!");
	winText.setScale(1.5f);
	winText.setVisible(false);
	winText.setPosition(CAMERA_WIDTH / 2 - winText.getWidth() / 2, CAMERA_HEIGHT / 2 - winText.getHeight());

	loseText = new Text(0, 0, mFont, "You lose!");
	loseText.setScale(1.5f);
	loseText.setVisible(false);
	loseText.setPosition(CAMERA_WIDTH / 2 - winText.getWidth() / 2, CAMERA_HEIGHT / 2 - loseText.getHeight());

	final Text continueText = new Text(15, 10, mFont, "Continue");
	final Rectangle continueButton = new Rectangle(0, 0, continueText.getWidth() + 30, continueText.getHeight() + 20)
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (pSceneTouchEvent.isActionDown())
		{
		    Debug.d("Touched continue button...");
		    this.setColor(0, 1, 0);
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    TamaBattle.this.finish();
		}
		return true;
	    }
	};
	continueButton.setColor(1, 0, 0);
	continueButton.setPosition(CAMERA_WIDTH / 2 - continueButton.getWidth() / 2, winText.getY() + winText.getHeight() + 30);
	continueButton.attachChild(continueText);

	if (isServer)
	    endScene.registerUpdateHandler(new IUpdateHandler()
	    {
		@Override
		public void reset()
		{

		}

		@Override
		public void onUpdate(float arg0)
		{
		    if (team1 <= 0 || team2 <= 0)
		    {
			endScene.attachChild(continueButton);
			endScene.unregisterUpdateHandler(this);
		    }
		}
	    });

	endScene.attachChild(winText);
	endScene.attachChild(loseText);
	if (!isServer)
	    endScene.attachChild(continueButton);
	endScene.registerTouchArea(continueButton);
	endScene.setTouchAreaBindingEnabled(true);

	scene = new Scene();
	// scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
	scene.setBackground(mBackground);

	topLayer = new Entity();
	bottomLayer = new Entity();
	scene.attachChild(bottomLayer);
	scene.attachChild(topLayer);

	ipText = new Text(15, 15, mFont, "Server IP: " + IP);
	topLayer.attachChild(ipText);

	crosshairSprite = new Sprite(0, 0, listTR.get("crosshair.png"));
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

		if (team1 <= 0)
		{
		    if (playerNumber % 2 == 0)
			showWinScreen(true);
		    else
			showWinScreen(false);
		}
		else if (team2 <= 0)
		{
		    if (playerNumber % 2 != 0)
			showWinScreen(true);
		    else
			showWinScreen(false);
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
		    try
		    {
			AnimatedSprite p = mPlayerSprites.get(playerNumber);
			if ((pSceneTouchEvent.getX() > p.getX() + p.getWidth() + 20 && playerNumber % 2 != 0) || (pSceneTouchEvent.getX() < mPlayerSprites.get(playerNumber).getX() - 20 && playerNumber % 2 == 0))
			{
			    // Fire a bullet
			    Debug.d("Fire bullet!");
			    crosshairSprite.setPosition(pSceneTouchEvent.getX() - crosshairSprite.getWidth() * 0.5f, pSceneTouchEvent.getY() - crosshairSprite.getHeight() * 0.5f);
			    crosshairSprite.setVisible(true);
			    mServerConnector.sendFireBulletMessage(playerNumber, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
			}
		    } catch (Exception e)
		    {
			e.printStackTrace();
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
	if (soundOn)
	    lobbyMusic.play();
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
		    // TamaBattle.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
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
	if (winText.isVisible() && numPlayers > 1)
	{
	    Debug.d("Winner rewarded xp");
	    returnIntent.putExtra(MultiplayerConstants.XP_GAIN, 5 * lowestBattleLevel);
	}

	if (isDeathMatch)
	{
	    Debug.d("Deathmatch enabled, setting health to " + health + "...");
	    returnIntent.putExtra(MultiplayerConstants.HEALTH, this.health);
	    returnIntent.putExtra(MultiplayerConstants.DEATHMATCH, true);
	}
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

	try
	{
	    IP = WifiUtils.getWifiIPv4Address(this);
	} catch (UnknownHostException e)
	{
	    e.printStackTrace();
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
     * Shows the win screen
     * 
     * @param win
     *            True if win, false if lose
     */
    public void showWinScreen(final boolean win)
    {
	if (this.mEngine.getScene().equals(endScene))
	    return;

	if (win)
	    winText.setVisible(true);
	else
	    loseText.setVisible(true);
	this.mEngine.setScene(endScene);
    }

    private void log(final String pMessage)
    {
	Debug.d(pMessage);
    }

    /**
     * Simple function to display toast message
     * 
     * @param pMessage
     */
    public void toast(final String pMessage)
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

    private void savePreferences(String key, String value)
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	SharedPreferences.Editor editor = sharedPreferences.edit();
	editor.putString(key, value);
	editor.commit();
    }

    /**
     * Sets the appropriate sprite's user data to reflect that player's stats.
     */
    @Override
    public void setPlayerData(int playerID, int health, int maxHealth, int battleLevel)
    {
	try
	{
	    if (this.playerNumber == playerID)
		this.health = health;
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
		    final Sprite arrowSprite = new Sprite(0, 0, listTR.get("down_arrow.png"));
		    arrowSprite.setPosition(sprite.getWidth() * 0.5f - arrowSprite.getWidth() * 0.5f, -arrowSprite.getHeight() - 5);
		    arrowSprite.registerEntityModifier(new LoopEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.2f, 1, 1.2f), new ScaleModifier(0.2f, 1.2f, 1))));
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

    // ===========================================================
    // Client methods
    // ===========================================================

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
    public void client_fireBullet(final int playerID, final int pID, final float pX, final float pY)
    {
	// final Sprite bullet = new Sprite(0, 0, this.listTR.get("particle_point.png"));
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
	    int c = 200;
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

			    Debug.d("Player " + key + "'s health: " + info.getHealth());

			    mServerConnector.sendMoveSpriteMessage(((BulletInfo) bullet.getUserData()).getPlayerID(), ((BulletInfo) bullet.getUserData()).getID(), -1, -1, false);

			    // Remove bullet
			    bulletsToRemove.add(mSprites.get(pID));
			    mBattleServer.sendPlayerStatsMessage(info.getHealth(), info.getMaxHealth(), info.getBattleLevel(), info.getPlayerID());
			    if (info.getHealth() <= 0)
			    {
				Debug.d("Player " + key + " has lost!");
				mBattleServer.sendAddPlayerSpriteMessage(key, -1, -1);
			    }

			    mBattleServer.sendDamageMessage(key);

			    return;
			}
		    }

		    mServerConnector.sendMoveSpriteMessage(((BulletInfo) bullet.getUserData()).getPlayerID(), ((BulletInfo) bullet.getUserData()).getID(), bullet.getX(), bullet.getY(), false);
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
	if (soundOn)
	    pewSound.play();
	mSprites.put(pID, bullet);
	if (!bullet.hasParent())
	    bottomLayer.attachChild(bullet);
    }

    @Override
    /**
     * Sets the playerNumber, which identifies the player
     */
    public void client_setPlayerNumber(final int playerNumber)
    {
	this.playerNumber = playerNumber;
	Debug.d("I am player " + playerNumber);

	runOnUpdateThread(new Runnable()
	{

	    @Override
	    public void run()
	    {
		final Text playerNumText = new Text(0, 0, mFont, "Player " + playerNumber);
		playerNumText.setPosition(CAMERA_WIDTH - playerNumText.getWidth() - 15, 15);
		topLayer.attachChild(playerNumText);
	    }
	});
    }

    public void client_removePlayer(final int pID)
    {
	if (mPlayerSprites.get(pID) == null)
	    return;

	Debug.d("Removing player " + pID + "...");
	if (pID % 2 == 0)
	    team2--;
	else
	    team1--;
	if (pID == playerNumber && !isServer)
	{
	    loseText.setVisible(true);
	    this.mEngine.setScene(endScene);
	}
	else if (pID == playerNumber && isServer)
	{
	    Debug.d("Disabled touch listener...");
	    scene.setOnSceneTouchListenerBindingEnabled(false);
	    scene.clearChildScene();
	}

	runOnUpdateThread(new Runnable()
	{
	    @Override
	    public void run()
	    {
		try
		{
		    mPlayerSprites.get(pID).detachSelf();
		    mPlayerSprites.remove(pID);
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	});

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
    public void client_addPlayerSprite(final int pID, final float pX, final float pY)
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

	numPlayers++;
	Debug.d("[PLAYER " + playerNumber + "] Adding player " + pID + "... ");

	if (pID % 2 == 0)
	    team2++;
	else
	    team1++;

	final AnimatedSprite player = new AnimatedSprite(0, 0, this.mTamaTextureRegion.deepCopy());
	if (pX != 0 && pY != 0)
	    player.setPosition(pX, pY);
	else
	{
	    float x = 0, y = 0;
	    int offset = pID * 10;
	    if (pID % 2 == 0) // spawn on right side
	    {
		offset = (pID - 1) * 10;
		x = CAMERA_WIDTH - player.getWidth() - 25 - offset;
		y = CAMERA_HEIGHT / 2 + offset;
	    }
	    else
	    // spawn on left side
	    {
		x = player.getWidth() + 25 + offset;
		y = CAMERA_HEIGHT / 2 + offset;
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
	    me = player;
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

		    mServerConnector.sendMoveSpriteMessage(playerNumber, playerNumber, player.getX(), player.getY(), true);
		}
	    });
	}
	bottomLayer.attachChild(player);
	if (pID != playerNumber)
	    bottomLayer.swapChildren(player, me);
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
    public void client_moveSprite(final int pID, final float pX, final float pY,
	    final boolean isPlayer)
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

    /**
     * Sends your own player information to the server, so that it can let the other clients know.
     */
    @Override
    public void client_sendPlayerInfoToServer()
    {
	Debug.d("Sending player info to server...");
	mServerConnector.sendPlayerStatsMessage(this.health, this.maxHealth, this.battleLevel, playerNumber);
    }

    /**
     * Ends the game activity
     */
    public void client_endGame()
    {
	Debug.d("Ending game...");
	this.finish();
    }

    /**
     * Handles when a player is damaged
     */
    @Override
    public void client_handleReceivedDamage(final int id)
    {
	if (id == playerNumber && vibrateOn)
	    this.mEngine.vibrate(100l);
    }

    /**
     * Sets the mode to deathmatch (Tama dies for real)
     */
    @Override
    public void client_setDeathMatch(final boolean isDeathMatch)
    {
	this.isDeathMatch = isDeathMatch;
	final Sprite dmIcon = new Sprite(0, 0, listTR.get("skull.png"));
	dmIcon.setPosition(ipText.getWidth(), ipText.getHeight() / 2 - dmIcon.getWidth() / 2);
	dmIcon.setAlpha(0.7f);
	ipText.attachChild(dmIcon);
    }

    @Override
    public void client_startGame()
    {
	if (isDeathMatch)
	{
	    this.mEngine.setScene(deathMatchWarningScene);
	    waitTime(2000l);
	}
	this.mEngine.setScene(scene);
	if (soundOn)
	{
	    if (this.lobbyMusic.isPlaying())
		this.lobbyMusic.pause();
	    this.fightSound.play();
	    this.backgroundMusic.play();
	}

    }

    // ===========================================================
    // Server methods
    // ===========================================================

    @Override
    /**
     * Sends updated player user data to all the clients.
     */
    public void server_updateAllPlayerInfo()
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
		    Debug.d("Waiting for info to be ready for player " + key + "...");
		    while (aSprite.getUserData() == null)
		    {
			waitTime(500);
		    }
		    Debug.d("Info Ready!");
		    PlayerInfo info = (PlayerInfo) aSprite.getUserData();
		    mBattleServer.sendPlayerStatsMessage(info.getHealth(), info.getMaxHealth(), info.getBattleLevel(), info.getPlayerID());

		    if (info.getBattleLevel() < lowestBattleLevel)
			lowestBattleLevel = info.getBattleLevel();
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}
    }

    @Override
    public void server_updateSkull(final String ip, final boolean vote)
    {
	Text text = textIpArray.get(ip);
	if (text == null)
	{
	    Debug.d("IP " + ip + " not found!");
	    return;
	}
	if (text.getChildCount() == 0)
	{
	    if (vote)
	    {
		final Sprite skull = new Sprite(0, 0, listTR.get("skull.png"));
		skull.setPosition(text.getWidth() + 20, text.getHeight() / 2 - skull.getHeight() / 2);
		text.attachChild(skull);
	    }
	}
	else
	{
	    text.getLastChild().setVisible(vote);
	}
    }

    @Override
    public void server_addPlayerToLobby(final String ip, final int playerId, final int battleLevel)
    {
	this.runOnUpdateThread(new Runnable()
	{
	    @Override
	    public void run()
	    {
		final Text pText = new Text(50, 50 + playerId * 50, mFont, "Player " + playerId + ", Battle Level: " + battleLevel + ", " + ip);
		ipArray.put(ip, playerId);
		textIpArray.put(ip, pText);
		if (lobbyScene != null)
		    lobbyScene.attachChild(pText);

	    }
	});
    }

    @Override
    public void server_addPlayerSpriteToServer(final int playerID)
    {
	client_addPlayerSprite(playerID, 0, 0);
    }

    @Override
    /**
     * Sends a message to all clients to add new player sprites onto the screen.
     */
    public void server_updateAllPlayerSprites()
    {
	synchronized (mPlayerSprites)
	{
	    int key = 0;
	    for (int i = 0; i < mPlayerSprites.size(); i++)
	    {
		key = mPlayerSprites.keyAt(i);
		AnimatedSprite aSprite = mPlayerSprites.get(key);
		mBattleServer.sendAddPlayerSpriteMessage(key, aSprite.getX(), aSprite.getY());
	    }
	}

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
	    // TamaBattle.this.toast("CLIENT: Connected to server.");
	}

	@Override
	public void onTerminated(final ServerConnector<SocketConnection> pConnector)
	{
	    // TamaBattle.this.toast("CLIENT: Disconnected from Server...");
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
	    // TamaBattle.this.toast("SERVER: Started.");
	}

	@Override
	public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer)
	{
	    // TamaBattle.this.toast("SERVER: Terminated.");
	}

	@Override
	public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer,
		final Throwable pThrowable)
	{
	    Debug.e(pThrowable);
	    // TamaBattle.this.toast("SERVER: Exception: " + pThrowable);
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
	    String ip = TextUtil.getIpAndPort(pConnector);
	    Debug.d("SERVER: Client connected: " + ip);
	}

	@Override
	public void onTerminated(final ClientConnector<SocketConnection> pConnector)
	{
	    final String ip = TextUtil.getIpAndPort(pConnector);
	    Debug.d("SERVER: Client disconnected: " + ip);
	    mBattleServer.sendRemovePlayerMessage(ipArray.get(ip));
	    runOnUpdateThread(new Runnable()
	    {

		@Override
		public void run()
		{
		    try
		    {
			textIpArray.get(ip).detachSelf();
		    } catch (Exception e)
		    {
			e.printStackTrace();
		    }
		}
	    });
	}
    }

}
