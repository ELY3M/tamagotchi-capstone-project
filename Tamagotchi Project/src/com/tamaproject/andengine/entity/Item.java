package com.tamaproject.andengine.entity;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import android.graphics.Bitmap;

import com.tamaproject.GameObject;

public class Item 
{
    private String name;
    private String description;
    private int health, hunger, sickness, xp;
    private Sprite sprite;

    public Item()
    {
	setDefault();
    }

    public Item(String name, String description,
	    int health, int hunger, int sickness, int xp)
    {
	this.name = name;
	this.description = description;
	this.health = health;
	this.hunger = hunger;
	this.sickness = sickness;
	this.xp = xp;
    }

    public Item(String name, int health, int hunger,
	    int sickness, int xp)
    {
	this.name = name;
	this.description = "Item name: " + name + "\nHealth: " + health + "\nHunger: " + hunger + "\nSickness: " + sickness + "\nExperience: " + xp;
	this.health = health;
	this.hunger = hunger;
	this.sickness = sickness;
	this.xp = xp;
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

    public Sprite getSprite()
    {
        return sprite;
    }

    public void setSprite(Sprite sprite)
    {
        this.sprite = sprite;
    }

}
