package com.tamaproject.entity;

import java.util.ArrayList;

/**
 * The backpack stores the user's items and manages the positions at which the items are displayed in the game
 * 
 * @author Jonathan
 * 
 */
public class Backpack
{
    private ArrayList<Item> items;
    private final int MAX_SIZE = 30; // max number of items backpack can hold
    private boolean backpackOpen = false;

    public Backpack(ArrayList<Item> items)
    {
	this.items = items;
    }

    public Backpack()
    {
	this.items = new ArrayList<Item>();
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
		return true;
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

    public ArrayList<Item> getItems()
    {
	return items;
    }

    public void setItems(ArrayList<Item> items)
    {
	this.items = items;
    }

    /**
     * Puts all the items in a grid formation for when the user opens the backpack to view the items
     * 
     * @param width
     *            Width of the screen.
     * @param height
     *            Height of the screen.
     */
    public void resetPositions(float width, float height)
    {
	float xSpacing = width / 6;
	float ySpacing = height / 7;
	int count = 0;
	int size = items.size();
	try
	{
	    for (int i = 1; i <= 6; i++)
	    {
		for (int j = 1; j <= 5; j++)
		{
		    items.get(count).setPosition((xSpacing * j) - items.get(count).getTextureRegion().getWidth() / 2, (ySpacing * i) - items.get(i).getTextureRegion().getHeight() / 2);
		    if (++count > size - 1)
			return;
		}
	    }
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
