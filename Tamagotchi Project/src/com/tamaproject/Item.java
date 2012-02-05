package com.tamaproject;

import android.graphics.Bitmap;

public class Item extends GameObject
{
    private String name = null;
    public Item(Bitmap bitmap, int x, int y, String name)
    {
	super(bitmap, x, y);
	this.name = name;
    }

}
