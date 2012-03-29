package com.tamaproject.entity;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

/**
 * The item class represents an item that can be given to a Tamagotchi
 * 
 * @author Jonathan
 * 
 */
public class Item extends Sprite
{
    public static final int EQUIP = 1, NORMAL = 0;
    private String name;
    private String description;
    private int health, hunger, sickness, xp;
    private int type = NORMAL;
    private int protection = Protection.NONE;

    public Item(float x, float y, TextureRegion textureRegion)
    {
	super(x, y, textureRegion);
	setDefault();
    }

    public Item(float x, float y, TextureRegion textureRegion, String name, String description,
	    int health, int hunger, int sickness, int xp)
    {
	super(x, y, textureRegion);
	this.name = name;
	this.description = description;
	this.health = health;
	this.hunger = hunger;
	this.sickness = sickness;
	this.xp = xp;
    }

    public Item(float x, float y, TextureRegion textureRegion, String name, int health, int hunger,
	    int sickness, int xp)
    {
	super(x, y, textureRegion);
	this.name = name;
	this.description = "No description.";
	this.health = health;
	this.hunger = hunger;
	this.sickness = sickness;
	this.xp = xp;
    }

    public Item(float pX, float pY, TextureRegion pTextureRegion, String name, String description,
	    int health, int hunger, int sickness, int xp, int type, int protection)
    {
	super(pX, pY, pTextureRegion);
	this.name = name;
	this.description = description;
	this.health = health;
	this.hunger = hunger;
	this.sickness = sickness;
	this.xp = xp;
	this.type = type;
	this.protection = protection;
    }

    private void setDefault()
    {
	this.name = "Dummy";
	this.description = "This is a dummy item.";
	this.health = 0;
	this.hunger = 0;
	this.sickness = 0;
	this.xp = 0;
    }

    public String getName()
    {
	return name;
    }

    public int getHealth()
    {
	return health;
    }

    public int getHunger()
    {
	return hunger;
    }

    public int getSickness()
    {
	return sickness;
    }

    public int getXp()
    {
	return xp;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public void setHealth(int health)
    {
	this.health = health;
    }

    public void setHunger(int hunger)
    {
	this.hunger = hunger;
    }

    public void setSickness(int sickness)
    {
	this.sickness = sickness;
    }

    public void setXp(int xp)
    {
	this.xp = xp;
    }

    public String getDescription()
    {
	return description;
    }

    public void setDescription(String description)
    {
	this.description = description;
    }

    public int getType()
    {
	return type;
    }

    public void setType(int type)
    {
	this.type = type;
    }

    public String getStringType()
    {
	switch (type)
	{
	case Item.EQUIP:
	    return "Equipment";
	case Item.NORMAL:
	    return "Normal";
	default:
	    return "Normal";
	}
    }

    public String getInfo()
    {
	return "Item name: " + name + "\nType: " + getStringType() + "\nDescription: " + description + "\nHealth: " + health + "\nHunger: " + hunger + "\nSickness: " + sickness + "\nExperience: " + xp + "\nProtection: " + Protection.getString(protection);
    }

    public int getProtection()
    {
	return protection;
    }

    public void setProtection(int protection)
    {
	this.protection = protection;
    }
}
