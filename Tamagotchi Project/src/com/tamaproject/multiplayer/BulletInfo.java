package com.tamaproject.multiplayer;

public class BulletInfo
{
    private int playerID;
    private int ID;

    public BulletInfo(final int playerID, final int ID)
    {
	this.playerID = playerID;
	this.ID = ID;
    }

    public int getPlayerID()
    {
	return playerID;
    }

    public int getID()
    {
	return ID;
    }

    public void setPlayerID(int playerID)
    {
	this.playerID = playerID;
    }

    public void setID(int iD)
    {
	ID = iD;
    }

}
