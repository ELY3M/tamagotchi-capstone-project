package com.tamaproject.multiplayer;

import java.io.IOException;
import java.util.LinkedList;

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

import com.tamaproject.adt.messages.client.ClientMessageFlags;
import com.tamaproject.adt.messages.server.ConnectionCloseServerMessage;
import com.tamaproject.adt.messages.server.ServerMessageFlags;
import com.tamaproject.util.TamaBattleConstants;
import com.tamaproject.util.TextUtil;

/**
 * This class is for the server to handle incoming messages from the clients.
 * 
 * @author Jonathan
 * 
 */
public class BattleServer extends SocketServer<SocketConnectionClientConnector> implements
	IUpdateHandler, TamaBattleConstants, BattleMessages, ServerMessageFlags, ClientMessageFlags
{
    private int numPlayers = 0;
    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
    private IBattleServerListener battleServerListener;
    private int mSpriteIDCounter = 0;
    private boolean gameStarted = false;
    private final SparseArray<String> playerIps = new SparseArray<String>();
    private int deathMatchVotes = 0;
    private LinkedList<Integer> playerNumbers = new LinkedList<Integer>();

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
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_START_GAME, StartGameServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_RECEIVED_DAMAGE, ReceivedDamageServerMessage.class);
	this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_DEATHMATCH, DeathMatchServerMessage.class);
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
		System.out.println("Incoming client request...");
		if (!gameStarted)
		{
		    // Increment number of players
		    numPlayers++;
		    String IP = TextUtil.getIpAndPort(pClientConnector);

		    // Find an open player number between 1 and the number of players
		    int newPlayerNumber = 0;
		    for (int i = 1; i <= numPlayers; i++)
		    {
			if (!playerNumbers.contains(i))
			{
			    newPlayerNumber = i;
			    break;
			}
		    }

		    // Add new player number
		    playerIps.put(newPlayerNumber, IP);
		    playerNumbers.add(newPlayerNumber);
		    System.out.println("New player IP added: " + newPlayerNumber + ", " + IP);

		    // Send player number to player
		    final GetPlayerIdServerMessage sMessage = (GetPlayerIdServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ID_PLAYER);
		    sMessage.playerNumber = newPlayerNumber;
		    pClientConnector.sendServerMessage(sMessage);
		    BattleServer.this.mMessagePool.recycleMessage(sMessage);

		    // Notify everyone else that there is a new player
		    battleServerListener.server_addPlayerSpriteToServer(newPlayerNumber);
		    battleServerListener.server_updateAllPlayerSprites();
		}
		else
		{
		    try
		    {
			final ConnectionCloseServerMessage closeMessage = (ConnectionCloseServerMessage) BattleServer.this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE);
			pClientConnector.sendServerMessage(closeMessage);
			BattleServer.this.mMessagePool.recycleMessage(closeMessage);
		    } catch (Exception e)
		    {
			e.printStackTrace();
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

	/**
	 * Receives message from client containing player info about the client. Then it forwards the information to the other players.
	 */
	clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_SEND_PLAYER, SendPlayerStatsClientMessage.class, new IClientMessageHandler<SocketConnection>()
	{

	    @Override
	    public void onHandleMessage(ClientConnector<SocketConnection> arg0,
		    IClientMessage clientMessage) throws IOException
	    {
		SendPlayerStatsClientMessage message = (SendPlayerStatsClientMessage) clientMessage;
		battleServerListener.server_addPlayerToLobby(playerIps.get(message.playerID), message.playerID, message.battleLevel);
		battleServerListener.setPlayerData(message.playerID, message.health, message.maxHealth, message.battleLevel);
		battleServerListener.server_updateAllPlayerInfo();
	    }

	});

	clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_VOTE_DEATHMATCH, VoteDeathMatchClientMessage.class, new IClientMessageHandler<SocketConnection>()
	{

	    @Override
	    public void onHandleMessage(ClientConnector<SocketConnection> pConnector,
		    IClientMessage clientMessage) throws IOException
	    {
		VoteDeathMatchClientMessage msg = (VoteDeathMatchClientMessage) clientMessage;
		if (msg.voteDeathMatch)
		    deathMatchVotes++;
		else
		    deathMatchVotes--;
		String ip = TextUtil.getIpAndPort(pConnector);
		System.out.println("IP: " + ip);
		battleServerListener.server_updateSkull(ip, msg.voteDeathMatch);
	    }

	});

	return clientConnector;
    }

    public void sendRemovePlayerMessage(final int playerID)
    {
	try
	{
	    AddSpriteServerMessage message = (AddSpriteServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
	    message.set(0, playerID, -1, -1, true);
	    this.sendBroadcastServerMessage(message);
	    mMessagePool.recycleMessage(message);
	    playerIps.remove(playerID);
	    playerNumbers.remove((Integer) playerID);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendStartMessage()
    {
	this.gameStarted = true;
	try
	{
	    StartGameServerMessage startMessage = (StartGameServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_START_GAME);
	    this.sendBroadcastServerMessage(startMessage);
	    this.mMessagePool.recycleMessage(startMessage);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendAddPlayerSpriteMessage(final int playerID, final float x, final float y)
    {
	try
	{
	    final AddSpriteServerMessage apMessage = (AddSpriteServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_SPRITE);
	    apMessage.set(0, playerID, x, y, true);
	    this.sendBroadcastServerMessage(apMessage);
	    this.mMessagePool.recycleMessage(apMessage);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendPlayerStatsMessage(int health, int maxHealth, int battleLevel, int playerID)
    {
	try
	{
	    final SendPlayerStatsServerMessage spssMessage = (SendPlayerStatsServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_SEND_PLAYER);
	    spssMessage.set(health, maxHealth, battleLevel, playerID);
	    this.sendBroadcastServerMessage(spssMessage);
	    this.mMessagePool.recycleMessage(spssMessage);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendDamageMessage(int playerID)
    {
	try
	{
	    final ReceivedDamageServerMessage msg = (ReceivedDamageServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_RECEIVED_DAMAGE);
	    msg.set(playerID);
	    this.sendBroadcastServerMessage(msg);
	    this.mMessagePool.recycleMessage(msg);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public void sendDeathMatchMessage(final boolean isDeathMatch)
    {
	try
	{
	    final DeathMatchServerMessage msg = (DeathMatchServerMessage) this.mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_DEATHMATCH);
	    msg.set(isDeathMatch);
	    this.sendBroadcastServerMessage(msg);
	    this.mMessagePool.recycleMessage(msg);
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    public int getDeathMatchVotes()
    {
	return deathMatchVotes;
    }

    public int getLowestTeamCount()
    {
	int team1 = 0, team2 = 0;
	for (int i : playerNumbers)
	{
	    if (i % 2 == 0)
		team2++;
	    else
		team1++;
	}
	if (team1 < team2)
	    return team1;
	else
	    return team2;
    }

    public interface IBattleServerListener
    {
	public void server_updateAllPlayerSprites();

	public void server_updateAllPlayerInfo();

	public void setPlayerData(final int playerID, final int health, final int maxHealth,
		final int battleLevel);

	public void server_updateSkull(final String ip, final boolean vote);

	public void server_addPlayerToLobby(String ip, int playerId, int battleLevel);

	public void server_addPlayerSpriteToServer(final int playerID);
    }

    public int getNumPlayers()
    {
	return playerNumbers.size();
    }

    public boolean isGameStarted()
    {
	return gameStarted;
    }

}
