package com.tamaproject.andengine.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.anddev.andengine.entity.sprite.Sprite;

public class Tamagotchi
{
    public static final int MAX_BATTLE_LEVEL = 100;
    private float currentHealth, maxHealth;
    private float currentHunger, maxHunger;
    private float currentXP, maxXP;
    private float currentSickness, maxSickness;
    private int battleLevel;
    private int status;
    private long birthday;
    private long age;
    private Item equippedItem;

    private Sprite sprite;
    private DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private Calendar calendar = Calendar.getInstance();

    public final static int ALIVE = 1, DEAD = 0, LEVEL_UP = 2;

    public Tamagotchi()
    {
	setDefault();
	this.calendar.setTimeInMillis(birthday);
    }

    public Tamagotchi(float currentHealth, float maxHealth, float currentHunger, float maxHunger,
	    float currentXP, float maxXP, float currentSickness, float maxSickness,
	    int battleLevel, int status, long birthday, Item equippedItem, long age)
    {
	this.currentHealth = currentHealth;
	this.maxHealth = maxHealth;
	this.currentHunger = currentHunger;
	this.maxHunger = maxHunger;
	this.currentXP = currentXP;
	this.maxXP = maxXP;
	this.currentSickness = currentSickness;
	this.maxSickness = maxSickness;
	this.battleLevel = battleLevel;
	this.status = status;
	this.birthday = birthday;
	this.equippedItem = equippedItem;
	this.calendar.setTimeInMillis(birthday);
	this.age = age;
    }

    /**
     * For testing purposes. Sets the Tamagotchi to a default state if none has been specified.
     */
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
	this.battleLevel = 5;
	this.status = Tamagotchi.ALIVE;
	this.birthday = 1325376000000l;
	this.age = 0;
    }

    public Tamagotchi(int currentHealth, int maxHealth, int currentHunger, int maxHunger,
	    int currentXP, int maxXP, int currentSickness, int maxSickness, int battleLevel,
	    int status)
    {
	this.currentHealth = currentHealth;
	this.maxHealth = maxHealth;
	this.currentHunger = currentHunger;
	this.maxHunger = maxHunger;
	this.currentXP = currentXP;
	this.maxXP = maxXP;
	this.currentSickness = currentSickness;
	this.maxSickness = maxSickness;
	this.battleLevel = battleLevel;
	this.status = status;
    }

    /**
     * Applies item to Tamagotchi
     * 
     * @param item
     *            Item to be applied
     * @return DEAD, ALIVE, or LEVEL_UP
     */
    public int applyItem(Item item)
    {
	this.currentHealth += item.getHealth();
	this.currentHunger += item.getHunger();
	this.currentSickness += item.getSickness();
	this.currentXP += item.getXp();

	return checkStats();
    }

    /**
     * Equips an item to Tamagotchi and returns the previously equipped item.
     * 
     * @param item
     *            Item to be equipped.
     * @return Previously equipped item, null if nothing was equipped.
     */
    public Item equipItem(Item item)
    {
	if (equippedItem != null)
	{
	    Item oldItem = equippedItem;
	    equippedItem = item;
	    return oldItem;
	}
	else
	{
	    equippedItem = item;
	    return null;
	}
    }

    /**
     * Checks the stats of Tamagotchi
     * 
     * @return Tamagotchi.DEAD, LEVEL_UP, or ALIVE
     */
    public int checkStats()
    {
	if (this.currentHealth > this.maxHealth)
	{
	    this.currentHealth = this.maxHealth;
	}
	else if (this.currentHealth < 0)
	{
	    this.currentHealth = 0;
	}

	if (this.currentHunger < 0)
	{
	    this.currentHunger = 0;
	}
	else if (this.currentHunger > this.maxHunger)
	{
	    this.currentHunger = this.maxHunger;
	}

	if (this.currentSickness < 0)
	{
	    this.currentSickness = 0;
	}
	else if (this.currentSickness > this.maxSickness)
	{
	    this.currentSickness = this.maxSickness;
	}

	// check if tama is dead
	if (isDead())
	    return Tamagotchi.DEAD;

	// check if tama leveled up

	if (levelUp())
	    return Tamagotchi.LEVEL_UP;

	return Tamagotchi.ALIVE;

    }

    /**
     * Checks to see if Tamagotchi is dead
     * 
     * @return true if dead, false if alive
     */
    public boolean isDead()
    {
	if (status == Tamagotchi.DEAD)
	{
	    return true;
	}
	else if (currentHealth <= 0)
	{
	    status = Tamagotchi.DEAD;
	    return true;
	}

	return false;
    }

    /**
     * Checks to see if Tamagotchi has gained a level
     * 
     * @return true if gained a level, false if not
     */
    private boolean levelUp()
    {
	boolean leveled = false;
	while (this.currentXP > this.maxXP)
	{
	    this.battleLevel++;

	    this.currentXP = this.currentXP - this.maxXP;
	    this.maxXP *= 2;

	    this.maxHealth += this.maxHealth / 2;
	    this.currentHealth = this.maxHealth;

	    this.maxHunger += this.maxHunger / 4;

	    this.maxSickness += this.maxSickness / 4;
	    leveled = true;
	}
	return leveled;
    }

    /**
     * Gets the Tamagotchi's age by subtracting its birthday from the current time.
     * 
     * @return The tama's age in days.
     */
    public long getAge()
    {
	return age / (1000 * 60 * 60 * 24);
    }

    public void addToAge(long time)
    {
	age += time;
    }

    public int getBattleLevel()
    {
	return battleLevel;
    }

    public int getStatus()
    {
	return status;
    }

    public long getBirthday()
    {
	return birthday;
    }

    public String getFormattedBirthday()
    {
	return formatter.format(calendar.getTime());
    }

    public void setBirthday(long birthday)
    {
	this.birthday = birthday;
    }

    public Item getEquippedItem()
    {
	return equippedItem;
    }

    public String getEquippedItemName()
    {
	if (equippedItem == null)
	    return "None";
	else
	    return equippedItem.getName();
    }

    public Sprite getSprite()
    {
	return sprite;
    }

    public void setEquippedItem(Item equippedItem)
    {
	this.equippedItem = equippedItem;
    }

    public void setSprite(Sprite sprite)
    {
	this.sprite = sprite;
    }

    public float getCurrentHealth()
    {
	return currentHealth;
    }

    public float getMaxHealth()
    {
	return maxHealth;
    }

    public float getCurrentHunger()
    {
	return currentHunger;
    }

    public float getMaxHunger()
    {
	return maxHunger;
    }

    public float getCurrentXP()
    {
	return currentXP;
    }

    public float getMaxXP()
    {
	return maxXP;
    }

    public float getCurrentSickness()
    {
	return currentSickness;
    }

    public float getMaxSickness()
    {
	return maxSickness;
    }

    public void setCurrentHealth(float currentHealth)
    {
	this.currentHealth = currentHealth;
    }

    public void setMaxHealth(float maxHealth)
    {
	this.maxHealth = maxHealth;
    }

    public void setCurrentHunger(float currentHunger)
    {
	this.currentHunger = currentHunger;
    }

    public void setMaxHunger(float maxHunger)
    {
	this.maxHunger = maxHunger;
    }

    public void setCurrentXP(float currentXP)
    {
	this.currentXP = currentXP;
    }

    public void setMaxXP(float maxXP)
    {
	this.maxXP = maxXP;
    }

    public void setCurrentSickness(float currentSickness)
    {
	this.currentSickness = currentSickness;
    }

    public void setMaxSickness(float maxSickness)
    {
	this.maxSickness = maxSickness;
    }

    public String getStats()
    {
	String s = "Age: " + getAge() + "\nHealth: " + currentHealth + "/" + maxHealth + "\nSickness: " + currentSickness + "/" + maxSickness + "\nHunger: " + currentHunger + "/" + maxHunger + "\nExperience: " + currentXP + "/" + maxXP + "\nBattle Level: " + battleLevel + "\nBirthday: " + getFormattedBirthday() + "\n\nEquipped Item: \n" + equippedItem.getInfo();
	return s;
    }

    public void setBattleLevel(int battleLevel)
    {
	this.battleLevel = battleLevel;
    }

    public void setStatus(int status)
    {
	this.status = status;
    }
}
