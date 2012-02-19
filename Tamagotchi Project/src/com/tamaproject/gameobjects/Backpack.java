package com.tamaproject.gameobjects;

import java.util.ArrayList;

import com.tamaproject.GameObject;
import com.tamaproject.R;
import com.tamaproject.util.GameObjectUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.widget.Toast;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class Backpack
{
    private ArrayList<Item> items;
    private final int MAX_SIZE = 30; // max number of items backpack can hold
    private final int DISP_ITEMS = 10; // number of items to display on main screen
    private Display display;
    private boolean backpackOpen = false;
    private Rect bpRectangle;
    private Paint paint = new Paint();
    private int top, itemTopBound;
    private final int textSize = 20;
    private final int OFFSET = 15;
    private Rect fullBPRectangle;
    private final int openSquareSize;

    public Backpack(ArrayList<Item> items, Display display, int top)
    {
	this.items = items;
	this.display = display;
	this.top = top;
	this.itemTopBound = top;
	int width = display.getWidth();
	int height = display.getHeight();
	this.bpRectangle = new Rect(1, top, width - 1, height - 1);
	this.openSquareSize = width/20;
	this.fullBPRectangle = new Rect(width - openSquareSize, height - openSquareSize, width, height);
	refreshItems();
    }

    public int getOpenSquareSize()
    {
        return openSquareSize;
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
	paint.setColor(Color.BLACK);
	paint.setStyle(Style.FILL_AND_STROKE);
	paint.setStrokeWidth(3);
	canvas.drawRect(bpRectangle, paint);

	paint.setColor(Color.WHITE);
	paint.setStrokeWidth(1);
	canvas.drawRect(fullBPRectangle, paint);
	paint.setTextSize(textSize);
	paint.setAntiAlias(true);
	canvas.drawText("Backpack (" + this.numItems() + "/" + this.maxSize() + ")", textSize, top + textSize + OFFSET, paint);

	synchronized (items)
	{
	    for (Item item : items)
	    {
		if (item.getX() > -1 || item.getY() > -1)
		    item.draw(canvas);
	    }
	}
    }

    public void drawAllItems(Canvas canvas)
    {
	canvas.drawColor(Color.BLACK);
	paint.setColor(Color.WHITE);
	paint.setStrokeWidth(1);
	canvas.drawRect(fullBPRectangle, paint);
	paint.setTextSize(textSize);
	paint.setStyle(Style.FILL_AND_STROKE);
	canvas.drawText("Backpack", textSize+1, textSize*2, paint);
	synchronized (items)
	{
	    refreshItemsAll();
	    for (Item item : items)
	    {
		item.draw(canvas);
	    }
	}
    }

    public int numItems()
    {
	return items.size();
    }

    public int maxSize()
    {
	return MAX_SIZE;
    }

    public void refreshItems()
    {
	int height = display.getHeight();
	int width = display.getWidth();
	int SPACING = width / 6; // this centers the items
	int c = 0;

	synchronized (items)
	{
	    int i = 1, j = 1;

	    for (Item item : items)
	    {
		if (c > DISP_ITEMS - 1)
		{
		    item.setXY(-100, -100);
		}
		else
		{
		    item.setXY(SPACING * i, (height - itemTopBound) / 3 * j + itemTopBound + OFFSET);
		    i++;
		    if ((SPACING * i) > width - SPACING / 2)
		    {
			i = 1;
			j++;
		    }
		}
		c++;
	    }
	}
    }

    public void refreshItemsAll()
    {
	int height = display.getHeight();
	int width = display.getWidth();
	int SPACING = width / 6; // this centers the items
	int d = MAX_SIZE / 5 + 1;

	synchronized (items)
	{
	    int i = 1, j = 1;

	    for (Item item : items)
	    {
		item.setXY(SPACING * i, SPACING * j + d);
		i++;
		if ((SPACING * i) > width - SPACING / 2)
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
		    if(isBackpackOpen())
		    {
			setBackpackOpen(false);
			refreshItems();
		    }
		    item.handleActionMove(eventX, eventY);
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

    public boolean isBackpackOpen()
    {
	return backpackOpen;
    }

    public void setBackpackOpen(boolean backpackOpen)
    {
	this.backpackOpen = backpackOpen;
    }

}
