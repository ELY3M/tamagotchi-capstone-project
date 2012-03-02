package com.tamaproject.andengine.entity;

import java.util.ArrayList;

import android.graphics.Canvas;

import com.tamaproject.*;
import com.tamaproject.util.GameObjectUtil;

public class InPlayObjects
{
    private ArrayList<GameObject> ipo = new ArrayList<GameObject>();
    private GameObject trashcan;
    private int poopCount;

    public InPlayObjects()
    {
	trashcan = new GameObject(null, -100, -100);
	poopCount = 0;
    }

    public boolean handleActionDown(int x, int y)
    {
	synchronized (ipo)
	{
	    for (GameObject g : ipo)
	    {
		if (g.handleActionDown(x, y))
		    return true;
	    }
	    return false;
	}
    }

    public GameObject handleActionMove(int x, int y, int top, int bottom)
    {
	if (y < top)
	    y = top;
	else if (y > bottom)
	    y = bottom;

	synchronized (ipo)
	{
	    for (GameObject g : ipo)
	    {
		if (g.handleActionMove(x, y))
		    return g;
	    }

	    return null;
	}
    }

    public GameObject handleActionUp()
    {
	synchronized (ipo)
	{
	    for (GameObject g : ipo)
	    {
		if (g.handleActionUp())
		{
		    if (g.getGroup().equals("poop"))
		    {
			if (GameObjectUtil.isTouching(g, trashcan))
			{
			    ipo.remove(g);
			    poopCount--;
			    return null;
			}
		    }
		    return g;
		}
	    }
	    return null;
	}
    }

    public boolean add(GameObject g)
    {
	synchronized (ipo)
	{
	    if (g.getGroup().equalsIgnoreCase("trashcan"))
	    {
		trashcan = g;
		return true;
	    }
	    else
	    {
		if(g.getGroup().equalsIgnoreCase("poop"))
		    poopCount++;
		return ipo.add(g);
	    }
	}
    }

    public void draw(Canvas canvas)
    {
	trashcan.draw(canvas);
	synchronized (ipo)
	{
	    /*
	    for (GameObject g : ipo)
	    {
		g.draw(canvas);
	    }
	    */
	    for(int i=ipo.size()-1;i>=0;i--)
	    {
		ipo.get(i).draw(canvas);
	    }
	}

    }

    public int getPoopCount()
    {
        return poopCount;
    }

    public ArrayList<GameObject> getIpo()
    {
        return ipo;
    }

    public void setIpo(ArrayList<GameObject> ipo)
    {
        this.ipo = ipo;
    }

}
