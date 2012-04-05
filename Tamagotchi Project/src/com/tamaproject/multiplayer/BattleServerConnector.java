package com.tamaproject.multiplayer;

import java.io.IOException;
import java.net.Socket;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.util.Debug;

import com.tamaproject.adt.messages.client.ClientMessageFlags;
import com.tamaproject.adt.messages.server.ConnectionCloseServerMessage;
import com.tamaproject.adt.messages.server.ServerMessageFlags;
import com.tamaproject.multiplayer.TamaBattle.AddSpriteServerMessage;
import com.tamaproject.multiplayer.TamaBattle.FireBulletServerMessage;
import com.tamaproject.multiplayer.TamaBattle.GetPlayerIdServerMessage;
import com.tamaproject.multiplayer.TamaBattle.MoveSpriteServerMessage;
import com.tamaproject.util.TamaBattleConstants;

public class BattleServerConnector extends ServerConnector<SocketConnection> implements
	TamaBattleConstants, ClientMessageFlags, ServerMessageFlags
{
    public BattleServerConnector(
	    final String pServerIP,
	    final ISocketConnectionServerConnectorListener pSocketConnectionServerConnectorListener,
	    final IBattleServerConnectorListener pBattleServerConnectorListener) throws IOException
    {
	super(new SocketConnection(new Socket(pServerIP, SERVER_PORT)), pSocketConnectionServerConnectorListener);
	
	this.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		((TamaBattle) pBattleServerConnectorListener).finish();
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
		    pBattleServerConnectorListener.addPlayerSprite(addSpriteServerMessage.mID, addSpriteServerMessage.mX, addSpriteServerMessage.mY);
		    Debug.d("Adding player " + addSpriteServerMessage.mID);
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
		pBattleServerConnectorListener.moveSprite(moveSpriteServerMessage.mID, moveSpriteServerMessage.mX, moveSpriteServerMessage.mY, moveSpriteServerMessage.mIsPlayer);
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
		pBattleServerConnectorListener.setPlayerNumber(serverMessage.playerNumber);
	    }
	});

	this.registerServerMessage(FLAG_MESSAGE_SERVER_FIRE_BULLET, FireBulletServerMessage.class, new IServerMessageHandler<SocketConnection>()
	{
	    @Override
	    public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector,
		    final IServerMessage pServerMessage) throws IOException
	    {
		final FireBulletServerMessage message = (FireBulletServerMessage) pServerMessage;
		pBattleServerConnectorListener.fireBullet(message.playerID, message.mID, message.mX, message.mY);
	    }
	});
    }

    public static interface IBattleServerConnectorListener
    {
	public void fireBullet(final int playerID, final int pID, final float pX, final float pY);

	public void addPlayerSprite(final int pID, final float pX, final float pY);

	public void moveSprite(final int pID, final float pX, final float pY, final boolean isPlayer);
	
	public void setPlayerNumber(final int playerNumber);
    }
}
