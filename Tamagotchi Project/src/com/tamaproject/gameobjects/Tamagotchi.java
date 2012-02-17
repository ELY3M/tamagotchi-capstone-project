package com.tamaproject.gameobjects;

import android.graphics.Bitmap;

import com.tamaproject.GameObject;

public class Tamagotchi extends GameObject
{
    private int currentHealth, maxHealth;
    private int currentHunger, maxHunger;
    private int currentXP, maxXP;
    private int currentSickness, maxSickness;
    private int poop;
    private int battleLevel;
    private int status;

    public static int ALIVE = 1, DEAD = 0;

    public Tamagotchi(Bitmap bitmap, int x, int y)
    {
	super(bitmap, x, y);
	setDefault();
    }

    private void setDefault()
    {
	this.currentHealth = 100;
	this.maxHealth = 150;
	this.currentHunger = 10;
	this.maxHunger = 50;
	this.currentXP = 10;
	this.maxXP = 50;
	this.currentSickness = 10;
	this.maxSickness = 50;
	this.poop = 6;
	this.battleLevel = 5;
	this.status = Tamagotchi.ALIVE;
    }

    public Tamagotchi(Bitmap bitmap, int x, int y, int currentHealth, int maxHealth,
	    int currentHunger, int maxHunger, int currentXP, int maxXP, int currentSickness,
	    int maxSickness, int poop, int battleLevel, int status)
    {
	super(bitmap, x, y);
	this.currentHealth = currentHealth;
	this.maxHealth = maxHealth;
	this.currentHunger = currentHunger;
	this.maxHunger = maxHunger;
	this.currentXP = currentXP;
	this.maxXP = maxXP;
	this.currentSickness = currentSickness;
	this.maxSickness = maxSickness;
	this.poop = poop;
	this.battleLevel = battleLevel;
	this.status = status;
    }

    public boolean applyItem(Item item)
    {
	this.currentHealth += item.getHealth();
	this.currentHunger += item.getHunger();
	this.currentSickness += item.getSickness();
	this.currentXP += item.getXp();

	checkStats();
	return true;
    }

    // makes sure that the values are legit
    private void checkStats()
    {
	if (this.currentHealth > this.maxHealth)
	{
	    this.currentHealth = this.maxHealth;
	}

	if (this.currentHunger < 0)
	{
	    this.currentHunger = 0;
	}

	if (this.currentSickness < 0)
	{
	    this.currentSickness = 0;
	}

	// check if tama leveled up
	levelUp();

	// check if tama is dead
	isDead();
    }

    public boolean isDead()
    {
	if (status == Tamagotchi.DEAD)
	{
	    return true;
	}
	else if (currentHunger > maxHunger || currentSickness > maxSickness || currentHealth < 0)
	{
	    status = Tamagotchi.DEAD;
	    return true;
	}

	return false;
    }

    private boolean levelUp()
    {
	if (this.currentXP > this.maxXP)
	{
	    this.battleLevel++;

	    this.currentXP = this.maxXP - this.currentXP;
	    this.maxXP *= 2;

	    this.maxHealth += this.maxHealth / 2;
	    this.currentHealth = this.maxHealth;

	    this.maxHunger += this.maxHunger / 4;

	    this.maxSickness += this.maxSickness / 4;

	    return true;
	}
	return false;
    }

    public int getCurrentHealth()
    {
        return currentHealth;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }

    public int getCurrentHunger()
    {
        return currentHunger;
    }

    public int getMaxHunger()
    {
        return maxHunger;
    }

    public int getCurrentXP()
    {
        return currentXP;
    }

    public int getMaxXP()
    {
        return maxXP;
    }

    public int getCurrentSickness()
    {
        return currentSickness;
    }

    public int getMaxSickness()
    {
        return maxSickness;
    }

    public int getPoop()
    {
        return poop;
    }

    public int getBattleLevel()
    {
        return battleLevel;
    }

    public int getStatus()
    {
        return status;
    }
}
