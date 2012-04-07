package com.tamaproject.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import com.tamaproject.util.TamaBattleConstants;

public interface BattleMessages extends TamaBattleConstants
{
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

    public static class ReceivedDamageServerMessage extends ServerMessage
    {
	int playerNumber;

	public ReceivedDamageServerMessage()
	{

	}
	
	public ReceivedDamageServerMessage(final int id)
	{
	    this.playerNumber = id;
	}

	public void set(final int id)
	{
	    this.playerNumber = id;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_RECEIVED_DAMAGE;
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

    public static class StartGameServerMessage extends ServerMessage
    {
	public StartGameServerMessage()
	{

	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_START_GAME;
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
	int playerNumber;

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

    public static class ModifyPlayerStatsServerMessage extends ServerMessage
    {
	int health, playerID;

	public ModifyPlayerStatsServerMessage()
	{

	}

	public ModifyPlayerStatsServerMessage(int health, int playerID)
	{
	    this.health = health;
	    this.playerID = playerID;
	}

	public void set(final int health, final int playerID)
	{
	    this.health = health;
	    this.playerID = playerID;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_MODIFY_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream d) throws IOException
	{
	    this.health = d.readInt();
	    this.playerID = d.readInt();
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream d) throws IOException
	{
	    d.writeInt(this.health);
	    d.writeInt(this.playerID);
	}

    }

    public static class SendPlayerStatsClientMessage extends ClientMessage
    {
	int health, maxHealth, battleLevel, playerID;

	public SendPlayerStatsClientMessage()
	{

	}

	public SendPlayerStatsClientMessage(int health, int maxHealth, int battleLevel, int playerID)
	{
	    this.health = health;
	    this.maxHealth = maxHealth;
	    this.battleLevel = battleLevel;
	    this.playerID = playerID;
	}

	public void set(int health, int maxHealth, int battleLevel, int playerID)
	{
	    this.health = health;
	    this.maxHealth = maxHealth;
	    this.battleLevel = battleLevel;
	    this.playerID = playerID;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_CLIENT_SEND_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream d) throws IOException
	{
	    this.health = d.readInt();
	    this.maxHealth = d.readInt();
	    this.battleLevel = d.readInt();
	    this.playerID = d.readInt();
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream d) throws IOException
	{
	    d.writeInt(this.health);
	    d.writeInt(this.maxHealth);
	    d.writeInt(this.battleLevel);
	    d.writeInt(this.playerID);
	}

    }

    public static class SendPlayerStatsServerMessage extends ServerMessage
    {
	int health, maxHealth, battleLevel, playerID;

	public SendPlayerStatsServerMessage()
	{

	}

	public SendPlayerStatsServerMessage(int health, int maxHealth, int battleLevel, int playerID)
	{
	    this.health = health;
	    this.maxHealth = maxHealth;
	    this.battleLevel = battleLevel;
	    this.playerID = playerID;
	}

	public void set(int health, int maxHealth, int battleLevel, int playerID)
	{
	    this.health = health;
	    this.maxHealth = maxHealth;
	    this.battleLevel = battleLevel;
	    this.playerID = playerID;
	}

	@Override
	public short getFlag()
	{
	    return FLAG_MESSAGE_SERVER_SEND_PLAYER;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream d) throws IOException
	{
	    this.health = d.readInt();
	    this.maxHealth = d.readInt();
	    this.battleLevel = d.readInt();
	    this.playerID = d.readInt();
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream d) throws IOException
	{
	    d.writeInt(this.health);
	    d.writeInt(this.maxHealth);
	    d.writeInt(this.battleLevel);
	    d.writeInt(this.playerID);
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

}
