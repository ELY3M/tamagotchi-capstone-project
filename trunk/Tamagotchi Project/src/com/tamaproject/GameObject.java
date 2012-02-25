package com.tamaproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class GameObject
{

    private Bitmap bitmap; // the actual bitmap
    private String bitmapFileLocation;
    private int x; // the X coordinate
    private int y; // the Y coordinate
    private boolean touched;
    private boolean moved;
    private String group = null;
    private boolean locked = false;

    public GameObject(Bitmap bitmap, int x, int y)
    {
	this.bitmap = bitmap;
	this.x = x;
	this.y = y;
    }

    public Bitmap getBitmap()
    {
	return bitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
	this.bitmap = bitmap;
    }

    public int getX()
    {
	return x;
    }

    public void setX(int x)
    {
	this.x = x;
    }

    public int getY()
    {
	return y;
    }

    public void setY(int y)
    {
	this.y = y;
    }

    public boolean isMoved()
    {
	return moved;
    }

    public void setMoved(boolean moved)
    {
	this.moved = moved;
    }

    public void setXY(int x, int y)
    {
	setX(x);
	setY(y);
    }

    public boolean isTouched()
    {
	return touched;
    }

    public void setTouched(boolean touched)
    {
	this.touched = touched;
    }

    public void draw(Canvas canvas)
    {
	canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
    }

    public boolean handleActionDown(int eventX, int eventY)
    {
	this.moved = false;

	if (eventX >= (x - bitmap.getWidth() / 2) && (eventX <= (x + bitmap.getWidth() / 2)))
	{
	    if (eventY >= (y - bitmap.getHeight() / 2) && (eventY <= (y + bitmap.getHeight() / 2)))
	    {
		// object touched
		setTouched(true);
		return true;
	    }
	    else
	    {
		setTouched(false);
		return false;
	    }
	}
	else
	{
	    setTouched(false);
	    return false;
	}

    }

    public boolean handleActionMove(int x, int y)
    {
	if (locked)
	    return false;
	if (isTouched())
	{
	    this.moved = true;
	    this.setXY(x, y);
	    return true;
	}
	return false;
    }

    public boolean handleActionUp()
    {
	if (isTouched())
	{
	    this.setTouched(false);
	    return true;
	}
	return false;
    }

    @Override
    public String toString()
    {
	return "GameObject [bitmap=" + bitmap + ", x=" + x + ", y=" + y + ", touched=" + touched + "]";
    }

    public String getGroup()
    {
	return group;
    }

    public void setGroup(String group)
    {
	this.group = group;
    }

    public boolean isLocked()
    {
	return locked;
    }

    public void setLocked(boolean locked)
    {
	this.locked = locked;
    }

}
