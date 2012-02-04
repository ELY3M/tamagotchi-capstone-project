package com.tamaproject;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoopThread extends Thread
{
    private static final String TAG = GameLoopThread.class.getSimpleName();
    private boolean running;
    private SurfaceHolder surfaceHolder;
    private GameView gameView;

    public GameLoopThread(SurfaceHolder surfaceHolder, GameView gameView)
    {
	super();
	this.surfaceHolder = surfaceHolder;
	this.gameView = gameView;
    }

    public void setRunning(boolean running)
    {
	this.running = running;
    }

    public void run()
    {
	Canvas canvas;
	Log.d(TAG, "Starting game loop");
	try
	{
	    while (running)
	    {
		canvas = null;
		// try locking the canvas for exclusive pixel editing on the surface
		try
		{
		    canvas = this.surfaceHolder.lockCanvas();
		    synchronized (surfaceHolder)
		    {
			// update game state
			// draws the canvas on the panel
			this.gameView.onDraw(canvas);
		    }
		} finally
		{
		    // in case of an exception the surface is not left in
		    // an inconsistent state
		    if (canvas != null)
		    {
			surfaceHolder.unlockCanvasAndPost(canvas);
		    }
		} // end finally
	    }
	} catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}