package com.tamaproject.util;

import com.tamaproject.GameObject;

public class GameObjectUtil
{
    /**
     * Returns true if two GameObjects are touching, false if they are not
     * @param a
     * @param b
     * @return
     */
    public static boolean isTouching(GameObject a, GameObject b)
    {
	if (a != null & b != null)
	{
	    if (a.getX() >= (b.getX() - b.getBitmap().getWidth() / 2) && (a.getX() <= (b.getX() + b.getBitmap().getWidth() / 2)))
	    {
		if (a.getY() >= (b.getY() - b.getBitmap().getWidth() / 2) && (a.getY() <= (b.getY() + b.getBitmap().getWidth() / 2)))
		{
		    return true;
		}
	    }
	}
	return false;
    }
}
