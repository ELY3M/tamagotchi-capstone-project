package com.tamaproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Game extends Activity
{
    private static final String TAG = Game.class.getSimpleName();
    
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(new GameView(this));
    }

}
