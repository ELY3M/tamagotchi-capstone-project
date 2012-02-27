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
    private boolean backpackOpen = false;
    private Rect bpRectangle;
    private Paint paint = new Paint();
    private int top, itemTopBound;
    private final int textSize = 20;
    private final int OFFSET = 15;
    private Rect fullBPRectangle;
    private final int openSquareSize;
    private final int width, height;

    /**
     * Constructor for Backpack
     * 
     * @param items
     *            ArrayList of items to be put in backpack.
     * @param width
     *            Width of the screen.
     * @param height
     *            Height of the screen.
     * @param top
     *            The top border of the backpack for when it is drawn.
     */
    public Backpack(ArrayList<Item> items, int width, int height, int top)
    {
	this.width = width;
	this.height = height;
	this.items = items;
	this.top = top;
	this.itemTopBound = top;
	this.bpRectangle = new Rect(1, top, width - 1, height - 1);
	this.openSquareSize = width / 15;
	this.fullBPRectangle = new Rect(width - openSquareSize, height - openSquareSize, width, height);
	refreshItems();
    }

    public int getOpenSquareSize()
    {
	return openSquareSize;
    }

    /**
     * Adds an item to the backpack.
     * 
     * @param item
     * @return true if item was added, false if backpack is full and item not added
     */
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

    /**
     * Draws the first 10 items of backpack at the bottom of the main screen.
     * 
     * @param canvas
     */
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
	    refreshItems();
	    for (Item item : items)
	    {
		if (!item.isLocked())
		    item.draw(canvas);
	    }
	}
    }

    /**
     * Draws all the items when backpack is opened
     * 
     * @param canvas
     */
    public void drawAllItems(Canvas canvas)
    {
	canvas.drawColor(Color.BLACK);
	paint.setColor(Color.WHITE);
	paint.setStrokeWidth(1);
	canvas.drawRect(fullBPRectangle, paint);
	paint.setTextSize(textSize);
	paint.setStyle(Style.FILL_AND_STROKE);
	canvas.drawText("Backpack (" + this.numItems() + "/" + this.maxSize() + ")", textSize + 1, textSize * 2, paint);
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

    /**
     * Sets the items positions for when backpack is minimized
     */
    public void refreshItems()
    {
	int SPACING = width / 6; // this centers the items
	int c = 0;

	int i = 1, j = 1;

	for (Item item : items)
	{
	    if (!item.isTouched())
	    {
		if (c > DISP_ITEMS - 1)
		{
		    item.setLocked(true);
		}
		else
		{
		    item.setLocked(false);
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

    /**
     * Set the items positions for when backpack is maximized
     */
    public void refreshItemsAll()
    {
	int SPACING = width / 6; // this centers the items
	int d = MAX_SIZE / 5 + 1;

	synchronized (items)
	{
	    int i = 1, j = 1;

	    for (Item item : items)
	    {
		item.setLocked(false);
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
		    // item.setLocked(false);
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

    /**
     * Removes an item from the backpack.
     * 
     * @param item
     * @return true if successfully removed, false if not
     */
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
