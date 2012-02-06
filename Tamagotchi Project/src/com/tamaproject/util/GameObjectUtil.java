package com.tamaproject.util;

import com.tamaproject.GameObject;

public class GameObjectUtil
{
    public static boolean isTouching(GameObject a, GameObject b)
    {
	if (a.getX() >= (b.getX() - b.getBitmap().getWidth() / 2) && (a.getX() <= (b.getX() + b.getBitmap().getWidth() / 2)))
	{
	    if (a.getY() >= (b.getY() - b.getBitmap().getWidth() / 2) && (a.getY() <= (b.getY() + b.getBitmap().getWidth() / 2)))
	    {
		return true;
	    }
	}
	return false;
    }
}
