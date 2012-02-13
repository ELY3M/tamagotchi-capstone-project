package com.tamaproject.gameobjects;

import android.graphics.Bitmap;

import com.tamaproject.GameObject;

public class Tamagotchi extends GameObject
{
    int currentHealth, maxHealth;
    int currentHunger, maxHunger;
    

    public Tamagotchi(Bitmap bitmap, int x, int y)
    {
	super(bitmap, x, y);
	// TODO Auto-generated constructor stub
    }

}
