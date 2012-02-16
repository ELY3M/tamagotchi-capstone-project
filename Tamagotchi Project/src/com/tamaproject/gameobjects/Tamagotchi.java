package com.tamaproject.gameobjects;

import android.graphics.Bitmap;

import com.tamaproject.GameObject;

public class Tamagotchi extends GameObject
{
    int currentHealth, maxHealth;
    int hunger;
    int currentXP, maxXP;
    int poop;
    int battleLevel;

    public Tamagotchi(Bitmap bitmap, int x, int y)
    {
	super(bitmap, x, y);
    }
}
