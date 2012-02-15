package com.tamaproject.gameobjects;

import java.util.ArrayList;

import com.tamaproject.GameObject;
import com.tamaproject.R;
import com.tamaproject.util.GameObjectUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Display;

public class Backpack
{
    private ArrayList<Item> items;
    private final int MAX_SIZE = 50;
    private Display display;

    public Backpack(ArrayList<Item> items, Display display)
    {
	this.items = items;
	this.display = display;
    }

    public boolean addItem(Item item)
    {
	synchronized (items)
	{
	    if (items.size() == MAX_SIZE)
	    {
		return false;
	    }
	    else
	    {
		items.add(item);
		refreshItems();
		return true;
	    }
	}
    }

    public void draw(Canvas canvas)
    {
	synchronized (items)
	{
	    for (Item item : items)
	    {
		item.draw(canvas);
	    }
	}
    }

    public void refreshItems()
    {
	synchronized (items)
	{
	    int i = 1, j = 1;
	    for (Item item : items)
	    {
		item.setXY(50 * i, 50 * j);
		i++;
		if ((50 * i) > display.getWidth() - 50)
		{
		    i = 1;
		    j++;
		}
	    }
	}
    }

    public boolean handleActionDown(int eventX, int eventY)
    {
	synchronized (items)
	{
	    for (Item item : items)
	    {
		// assume that only one item is touched at a time
		if (item.handleActionDown(eventX, eventY))
		{
		    return true;
		}
	    }
	}
	return false;
    }

    public Item handleActionMove(int eventX, int eventY)
    {
	synchronized (items)
	{
	    for (Item item : items)
	    {
		if (item.isTouched())
		{
		    item.setXY(eventX, eventY);
		    return item;
		}
	    }
	}
	return null;
    }

    public Item handleActionUp()
    {
	synchronized (items)
	{
	    for (Item item : items)
	    {
		if (item.isTouched())
		{
		    item.setTouched(false);
		    return item;
		}
	    }
	    refreshItems();
	}

	return null;
    }

    public boolean removeItem(Item item)
    {
	synchronized (items)
	{
	    if (items.remove(item))
	    {
		refreshItems();
		return true;
	    }
	    else
	    {
		return false;
	    }
	}
    }

}
