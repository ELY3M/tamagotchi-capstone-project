package com.tamaproject.multiplayer;

public class PlayerInfo
{
    private int health, maxHealth, battleLevel, playerID;

    public PlayerInfo(int health, int maxHealth, int battleLevel, int playerID)
    {
	super();
	this.health = health;
	this.maxHealth = maxHealth;
	this.battleLevel = battleLevel;
	this.playerID = playerID;
    }

    public int getHealth()
    {
        return health;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }

    public int getBattleLevel()
    {
        return battleLevel;
    }

    public int getPlayerID()
    {
        return playerID;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth)
    {
        this.maxHealth = maxHealth;
    }

    public void setBattleLevel(int battleLevel)
    {
        this.battleLevel = battleLevel;
    }

    public void setPlayerID(int playerID)
    {
        this.playerID = playerID;
    }
}
