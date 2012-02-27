package com.tamaproject;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * This is a generic GameObject. It is an object that you can interact with in the game.
 * 
 * @author Jonathan
 * 
 */
public class GameObject
{

    private Bitmap bitmap; // the actual bitmap
    private String bitmapFileLocation;
    private int x; // the X coordinate
    private int y; // the Y coordinate
    private boolean touched;
    protected boolean moved;
    private String group = "";
    protected boolean locked = false;

    /**
     * Constructor for GameObject
     * 
     * @param bitmap
     *            The bitmap object.
     * @param x
     *            The x-position.
     * @param y
     *            The y-position.
     */
    public GameObject(Bitmap bitmap, int x, int y)
    {
	this.bitmap = bitmap;
	this.x = x;
	this.y = y;
    }

    /**
     * Constructor for GameObject
     * 
     * @param bitmapFileLocation
     *            The location of the image file.
     * @param assetManager
     *            The asset manager used to retrieve the image file.
     * @param x
     *            The x-position for the GameObject.
     * @param y
     *            The y-position for the GameObject.
     */
    public GameObject(String bitmapFileLocation, AssetManager assetManager, int x, int y)
    {
	this.bitmapFileLocation = bitmapFileLocation;
	this.bitmap = loadBitmap(bitmapFileLocation, assetManager);
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

    /**
     * Handles when the GameObject is touched
     * 
     * @param eventX
     *            The x-position of the input event.
     * @param eventY
     *            The y-position of the input event.
     * @return true if the GameObject has been touched, false if not.
     */
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

    /**
     * Handles when the GameObject is moved.
     * 
     * @param x
     *            The x-position of input event.
     * @param y
     *            The y-position of input event.
     * @return true if the GameObject was moved, false if not
     */
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

    /**
     * Handles when the user lets go of GameObject
     * 
     * @return true if the GameObject was being touched and has been let go, false if GameObject wasn't being touched to begin with
     */
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

    /**
     * Loads the bitmap from the file location into the GameObject
     * 
     * @param bitmapFileLocation
     *            Location of image file.
     * @param assetManager
     *            AssetManager that is used to get the image file from the assets.
     * @return The Bitmap that was loaded, null if it could not be loaded.
     */
    public Bitmap loadBitmap(String bitmapFileLocation, AssetManager assetManager)
    {
	try
	{
	    return BitmapFactory.decodeStream(assetManager.open(bitmapFileLocation));
	} catch (Exception e)
	{
	    return null;
	}
    }

}
