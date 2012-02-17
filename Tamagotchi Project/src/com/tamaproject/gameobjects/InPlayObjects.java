package com.tamaproject.gameobjects;

import java.util.ArrayList;

import android.graphics.Canvas;

import com.tamaproject.*;

public class InPlayObjects
{
    private ArrayList<GameObject> ipo = new ArrayList<GameObject>();
    
    public boolean handleActionDown(int x, int y)
    {
	for (GameObject g : ipo)
	{
	    if (g.handleActionDown(x, y))
		return true;
	}
	return false;
    }

    public GameObject handleActionMove(int x, int y, int top, int bottom)
    {
	if (y < top)
	    y = top;
	else if (y > bottom)
	    y = bottom;
	
	for (GameObject g : ipo)
	{
	    if (g.handleActionMove(x, y))
		return g;
	}

	return null;
    }

    public GameObject handleActionUp()
    {
	for (GameObject g : ipo)
	{
	    if (g.handleActionUp())
		return g;
	}
	return null;
    }

    public boolean add(GameObject g)
    {
	return ipo.add(g);
    }
    
    public void draw(Canvas canvas)
    {
	for (GameObject g : ipo)
	{
	    g.draw(canvas);
	}	
    }

}
