package com.tamaproject.multiplayer;

import java.io.IOException;
import java.net.Socket;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.anddev.andengine.util.Debug;

import com.tamaproject.adt.messages.client.ClientMessageFlags;
import com.tamaproject.adt.messages.server.ConnectionCloseServerMessage;
import com.tamaproject.adt.messages.server.ServerMessageFlags;
import com.tamaproject.util.TamaBattleConstants;

/**
 * This class is for clients to handle messages coming from the server.
 * 
 * @author Jonathan
 * 
 */
public class BattleServerConnector extends ServerConnector<SocketConnection> implements
	TamaBattleConstants, ClientMessageFlags, ServerMessageFlags, BattleMessages
{
    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    public BattleServerConnector(
	    final String pServerIP,
	    final ISocketConnectionServerConnectorListener pSocketConnectionServerConnectorListener,
	    final IBattleServerConnectorListener pBattleServerConnectorListener) throws IOException
    {
	super(new SocketConnection(new Socket(pServerIP, SERVER_PORT)), pSocketConnectionServerConnectorListener);
	this.initMessagePool();
	this.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		pBattleServerConnectorListener.client_endGame();
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE, AddSpriteServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final AddSpriteServerMessage addSpriteServerMessage = (AddSpriteServerMessage) pServerMessage;
		if (addSpriteServerMessage.mIsPlayer)
		{
		    if (addSpriteServerMessage.mX >= 0 && addSpriteServerMessage.mY >= 0)
		    {
			pBattleServerConnectorListener.client_addPlayerSprite(addSpriteServerMessage.mID, addSpriteServerMessage.mX, addSpriteServerMessage.mY);
			Debug.d("[SERVER] Adding player " + addSpriteServerMessage.mID);
		    }
		    else
		    {
			pBattleServerConnectorListener.client_removePlayer(addSpriteServerMessage.mID);
			Debug.d("[SERVER] Removing player " + addSpriteServerMessage.mID);
		    }
		}
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage) pServerMessage;
		pBattleServerConnectorListener.client_moveSprite(moveSpriteServerMessage.mID, moveSpriteServerMessage.mX, moveSpriteServerMessage.mY, moveSpriteServerMessage.mIsPlayer);
	    }
	});

	/**
	 * Receives the player number from the server.
	 */
	this.registerServerMessage(FLAG_MESSAGE_SERVER_ID_PLAYER, GetPlayerIdServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final GetPlayerIdServerMessage serverMessage = (GetPlayerIdServerMessage) pServerMessage;
		pBattleServerConnectorListener.client_setPlayerNumber(serverMessage.playerNumber);
		pBattleServerConnectorListener.client_sendPlayerInfoToServer();
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET, FireBulletServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final FireBulletServerMessage message = (FireBulletServerMessage) pServerMessage;
		pBattleServerConnectorListener.client_fireBullet(message.playerID, message.mID, message.mX, message.mY);
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER, SendPlayerStatsServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final SendPlayerStatsServerMessage message = (SendPlayerStatsServerMessage) pServerMessage;
		pBattleServerConnectorListener.setPlayerData(message.playerID, message.health, message.maxHealth, message.battleLevel);
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_START_GAME, StartGameServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		pBattleServerConnectorListener.client_startGame();
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_RECEIVED_DAMAGE, ReceivedDamageServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		int playerID = ((ReceivedDamageServerMessage) pServerMessage).playerNumber;
		pBattleServerConnectorListener.client_handleReceivedDamage(playerID);
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_DEATHMATCH, DeathMatchServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		DeathMatchServerMessage msg = (DeathMatchServerMessage) pServerMessage;
		pBattleServerConnectorListener.client_setDeathMatch(msg.isDeathMatch);
	    }
	});
    }

    private void initMessagePool()
    {
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_ADD_SPRITE, AddSpriteClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE, MoveSpriteClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_FIRE_BULLET, FireBulletClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER, SendPlayerStatsClientMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_VOTE_DEATHMATCH, VoteDeathMatchClientMessage.class);
    }
    

    public void sendMoveSpriteMessage(int playerID, int id, float x, float y, boolean isPlayer)
    {
	try
	{
	    final MoveSpriteClientMessage message = (MoveSpriteClientMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
	    message.set(playerID, id, x, y, isPlayer);
	    this.sendClientMessage(message);
	    this.mMessagePool.recycleMessage(message);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendFireBulletMessage(int playerID, float x, float y)
    {
	try
	{
	    final FireBulletClientMessage message = (FireBulletClientMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_FIRE_BULLET);
	    message.set(playerID, 0, x, y, false);

	    this.sendClientMessage(message);

	    this.mMessagePool.recycleMessage(message);
	} catch (final IOException e)
	{
	    e.printStackTrace();
	}
    }

    public void sendPlayerStatsMessage(int health, int maxHealth, int battleLevel, int playerID)
    {
	try
	{
	    final SendPlayerStatsClientMessage message = (SendPlayerStatsClientMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER);
	    message.set(health, maxHealth, battleLevel, playerID);
	    this.sendClientMessage(message);
	    this.mMessagePool.recycleMessage(message);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
    
    public void sendVoteDeathMatch(boolean vote)
    {
	try
	{
	    final VoteDeathMatchClientMessage message = (VoteDeathMatchClientMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_VOTE_DEATHMATCH);
	    message.set(vote);
	    this.sendClientMessage(message);
	    this.mMessagePool.recycleMessage(message);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public static interface IBattleServerConnectorListener
    {
	public void client_fireBullet(final int playerID, final int pID, final float pX, final float pY);

	public void client_addPlayerSprite(final int pID, final float pX, final float pY);

	public void client_moveSprite(final int pID, final float pX, final float pY, final boolean isPlayer);

	public void client_setPlayerNumber(final int playerNumber);

	public void setPlayerData(final int playerID, final int health, final int maxHealth,
		final int battleLevel);

	public void client_sendPlayerInfoToServer();

	public void client_startGame();

	public void client_removePlayer(final int pID);

	public void client_endGame();

	public void client_handleReceivedDamage(final int playerID);
	
	public void client_setDeathMatch(final boolean isDeathMatch);
    }
}
