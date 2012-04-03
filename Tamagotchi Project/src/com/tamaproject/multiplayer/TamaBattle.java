package com.tamaproject.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
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

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    private static final int SERVER_PORT = 4444;

    private static final short FLAG_MESSAGE_SERVER_ADD_SPRITE = 1;
    private static final short FLAG_MESSAGE_SERVER_MOVE_SPRITE = 2;
    private static final short FLAG_MESSAGE_SERVER_ID_PLAYER = 3;
    private static final short FLAG_MESSAGE_CLIENT_REQUEST_ID = 4;

    private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
    private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
    private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;

    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mSpriteTextureRegion;

    private int mSpriteIDCounter;
    private final SparseArray<Sprite> mSprites = new SparseArray<Sprite>();

    private String mServerIP = LOCALHOST_IP;
    private SocketServer<SocketConnectionClientConnector> mSocketServer;
    private ServerConnector<SocketConnection> mServerConnector;

    private int health = 0, maxHealth = 0, battleLevel = 0;

    private int numPlayers = 0;

    private int playerNumber = 0;

    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

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
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ID_PLAYER, GetPlayerIdServerMessage.class);
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
	return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
    }

    @Override
    public void onLoadResources()
    {
	this.mBitmapTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	this.mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);

	this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene()
    {
	//this.mEngine.registerUpdateHandler(new FPSLogger());

	final Scene scene = new Scene();
	scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

	/* We allow only the server to actively send around messages. */
	if (TamaBattle.this.mSocketServer != null)
	{
	    scene.setOnSceneTouchListener(new IOnSceneTouchListener()
	    {
		@Override
		public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent)
		{
		    if (pSceneTouchEvent.isActionDown())
		    {
			try
			{
			    final AddSpriteServerMessage addSpriteServerMessage = (AddSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
			    addSpriteServerMessage.set(TamaBattle.this.mSpriteIDCounter++, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

			    TamaBattle.this.mSocketServer.sendBroadcastServerMessage(addSpriteServerMessage);

			    TamaBattle.this.mMessagePool.recycleMessage(addSpriteServerMessage);
			} catch (final IOException e)
			{
			    Debug.e(e);
			}
			return true;
		    }
		    else
		    {
			return true;
		    }
		}
	    });

	    scene.setOnAreaTouchListener(new IOnAreaTouchListener()
	    {
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final ITouchArea pTouchArea, final float pTouchAreaLocalX,
			final float pTouchAreaLocalY)
		{
		    try
		    {
			final Sprite face = (Sprite) pTouchArea;
			final Integer faceID = (Integer) face.getUserData();

			final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE);
			moveSpriteServerMessage.set(faceID, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());

			TamaBattle.this.mSocketServer.sendBroadcastServerMessage(moveSpriteServerMessage);

			TamaBattle.this.mMessagePool.recycleMessage(moveSpriteServerMessage);
		    } catch (final IOException e)
		    {
			Debug.e(e);
			return false;
		    }
		    return true;
		}
	    });

	    scene.setTouchAreaBindingEnabled(true);
	}

	return scene;
    }

    @Override
    public void onLoadComplete()
    {

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
	    return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Be Server or Client ...").setCancelable(false).setPositiveButton("Client", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.showDialog(DIALOG_ENTER_SERVER_IP_ID);
		}
	    }).setNeutralButton("Server", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.toast("You can add and move sprites, which are only shown on the clients.");
		    TamaBattle.this.initServer();
		    TamaBattle.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
		}
	    }).setNegativeButton("Both", new OnClickListener()
	    {
		@Override
		public void onClick(final DialogInterface pDialog, final int pWhich)
		{
		    TamaBattle.this.toast("You can add sprites and move them, by dragging them.");
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

    public void addSprite(final int pID, final float pX, final float pY)
    {
	final Scene scene = this.mEngine.getScene();
	/* Create the face and add it to the scene. */
	final Sprite face = new Sprite(0, 0, this.mSpriteTextureRegion);
	face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
	face.setUserData(pID);
	this.mSprites.put(pID, face);
	scene.registerTouchArea(face);
	scene.attachChild(face);
    }

    public void moveSprite(final int pID, final float pX, final float pY)
    {
	/* Find and move the face. */
	final Sprite face = this.mSprites.get(pID);
	face.setPosition(pX - face.getWidth() * 0.5f, pY - face.getHeight() * 0.5f);
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

		clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_REQUEST_ID, RequestPlayerIdClientMessage.class, new IClientMessageHandler<SocketConnection>()
		{

		    @Override
		    public void onHandleMessage(ClientConnector<SocketConnection> pClientConnector,
			    IClientMessage arg1) throws IOException
		    {
			final GetPlayerIdServerMessage sMessage = (GetPlayerIdServerMessage) TamaBattle.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ID_PLAYER);
			sMessage.playerNumber = numPlayers;
			pClientConnector.sendServerMessage(sMessage);
			TamaBattle.this.mMessagePool.recycleMessage(sMessage);
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
		    TamaBattle.this.addSprite(addSpriteServerMessage.mID, addSpriteServerMessage.mX, addSpriteServerMessage.mY);
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
		    TamaBattle.this.moveSprite(moveSpriteServerMessage.mID, moveSpriteServerMessage.mX, moveSpriteServerMessage.mY);
		}
	    });

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

    public static class AddSpriteServerMessage extends ServerMessage
    {
	private int mID;
	private float mX;
	private float mY;

	public AddSpriteServerMessage()
	{

	}

	public AddSpriteServerMessage(final int pID, final float pX, final float pY)
	{
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	}

	public void set(final int pID, final float pX, final float pY)
	{
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_ADD_SPRITE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
		throws IOException
	{
	    this.mID = pDataInputStream.readInt();
	    this.mX = pDataInputStream.readFloat();
	    this.mY = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
		throws IOException
	{
	    pDataOutputStream.writeInt(this.mID);
	    pDataOutputStream.writeFloat(this.mX);
	    pDataOutputStream.writeFloat(this.mY);
	}
    }

    public static class MoveSpriteServerMessage extends ServerMessage
    {
	private int mID;
	private float mX;
	private float mY;

	public MoveSpriteServerMessage()
	{

	}

	public MoveSpriteServerMessage(final int pID, final float pX, final float pY)
	{
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	}

	public void set(final int pID, final float pX, final float pY)
	{
	    this.mID = pID;
	    this.mX = pX;
	    this.mY = pY;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_MOVE_SPRITE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream)
		throws IOException
	{
	    this.mID = pDataInputStream.readInt();
	    this.mX = pDataInputStream.readFloat();
	    this.mY = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
		throws IOException
	{
	    pDataOutputStream.writeInt(this.mID);
	    pDataOutputStream.writeFloat(this.mX);
	    pDataOutputStream.writeFloat(this.mY);
	}
    }

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
