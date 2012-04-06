package com.tamaproject.multiplayer;

import java.io.IOException;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;

import android.util.SparseArray;

import com.tamaproject.util.TamaBattleConstants;

/**
 * This class is for the server to handle incoming messages from the clients.
 * 
 * @author Jonathan
 * 
 */
public class BattleServer extends SocketServer<SocketConnectionClientConnector> implements
	IUpdateHandler, TamaBattleConstants, BattleMessages
{
    private int numPlayers = 0;
    final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
    private IBattleServerListener battleServerListener;
    private int mSpriteIDCounter = 0;
    final SparseArray<String> playerIps = new SparseArray<String>();

    public BattleServer(
	    final ISocketConnectionClientConnectorListener pSocketConnectionClientConnectorListener,
	    final ISocketServerListener<SocketConnectionClientConnector> pSocketServerListener,
	    final IBattleServerListener pBattleServerListener)
    {
	super(SERVER_PORT, pSocketConnectionClientConnectorListener, pSocketServerListener);
	this.initMessagePool();
	this.battleServerListener = pBattleServerListener;
    }

    private void initMessagePool()
    {
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE, AddSpriteServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ID_PLAYER, GetPlayerIdServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET, FireBulletServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER, SendPlayerStatsServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MODIFY_PLAYER, ModifyPlayerStatsServerMessage.class);
    }

    @Override
    public void onUpdate(float arg0)
    {

    }

    @Override
    public void reset()
    {

    }

    @Override
    protected SocketConnectionClientConnector newClientConnector(SocketConnection pSocketConnection)
	    throws IOException
    {
	final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);

	/**
	 * Receives player number request message from Client and sends Client their player number.
	 */
	clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_REQUEST_ID, RequestPlayerIdClientMessage.class, new IClientMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(ClientConnector<SocketConnection> pClientConnector,
		    IClientMessage pClientMessage) throws IOException
	    {
		numPlayers++;
		String IP = pClientConnector.getConnection().getSocket().getInetAddress().getHostAddress();
		playerIps.put(numPlayers, IP);
		System.out.println("New player IP added: " + numPlayers + ", " + IP);
		
		final GetPlayerIdServerMessage sMessage = (GetPlayerIdServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ID_PLAYER);
		sMessage.playerNumber = numPlayers;
		pClientConnector.sendServerMessage(sMessage);
		BattleServer.this.mMessagePool.recycleMessage(sMessage);

		final AddSpriteServerMessage addPlayerMessage = (AddSpriteServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
		addPlayerMessage.set(0, numPlayers, 0, 0, true);
		try
		{
		    Thread.sleep(500l);
		    BattleServer.this.sendBroadcastServerMessage(addPlayerMessage);
		    BattleServer.this.mMessagePool.recycleMessage(addPlayerMessage);
		} catch (IOException e)
		{
		    e.printStackTrace();
		} catch (InterruptedException e)
		{
		    e.printStackTrace();
		}

		battleServerListener.updateAllPlayerSprites();
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
		    final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE);
		    moveSpriteServerMessage.set(message.playerID, message.mID, message.mX, message.mY, message.mIsPlayer);

		    BattleServer.this.sendBroadcastServerMessage(moveSpriteServerMessage);
		    BattleServer.this.mMessagePool.recycleMessage(moveSpriteServerMessage);
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
		    final AddSpriteServerMessage addSpriteServerMessage = (AddSpriteServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
		    addSpriteServerMessage.set(message.playerID, message.mID, message.mX, message.mY, message.mIsPlayer);

		    BattleServer.this.sendBroadcastServerMessage(addSpriteServerMessage);
		    BattleServer.this.mMessagePool.recycleMessage(addSpriteServerMessage);
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
		    final FireBulletServerMessage bMessage = (FireBulletServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET);
		    bMessage.set(message.playerID, BattleServer.this.mSpriteIDCounter++, message.mX, message.mY, message.mIsPlayer);

		    BattleServer.this.sendBroadcastServerMessage(bMessage);
		    BattleServer.this.mMessagePool.recycleMessage(bMessage);
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }

	});

	clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER, SendPlayerStatsClientMessage.class, new IClientMessageHandler<SocketConnection>()
	{

	    @Override
	    public void onHandleMessage(ClientConnector<SocketConnection> arg0,
		    IClientMessage clientMessage) throws IOException
	    {
		SendPlayerStatsClientMessage message = (SendPlayerStatsClientMessage) clientMessage;
		battleServerListener.setPlayerData(message.playerID, message.health, message.maxHealth, message.battleLevel);
		battleServerListener.updateAllPlayerInfo();
	    }

	});

	return clientConnector;
    }

    public interface IBattleServerListener
    {
	public void updateAllPlayerSprites();

	public void updateAllPlayerInfo();

	public void setPlayerData(final int playerID, final int health, final int maxHealth,
		final int battleLevel);
    }

}
