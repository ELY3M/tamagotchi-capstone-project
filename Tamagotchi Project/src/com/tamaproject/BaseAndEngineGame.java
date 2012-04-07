package com.tamaproject;

import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

public abstract class BaseAndEngineGame extends BaseGameActivity
{
    // ===========================================================
    // Constants
    // ===========================================================
    private static final int VIBRATE = 0, SOUND = 1;
    // ===========================================================
    // Fields
    // ===========================================================

    protected boolean vibrateOn = false, soundOn = false;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu)
    {
	pMenu.add(Menu.NONE, VIBRATE, Menu.NONE, "Enable Vibration");
	pMenu.add(Menu.NONE, SOUND, Menu.NONE, "Enable Sound");
	return super.onCreateOptionsMenu(pMenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu)
    {
	pMenu.findItem(VIBRATE).setTitle(vibrateOn ? "Disable Vibration" : "Enable Vibration");
	pMenu.findItem(SOUND).setTitle(soundOn ? "Disable Sound" : "Enable Sound");
	return super.onPrepareOptionsMenu(pMenu);
    }

    @Override
    public boolean onMenuItemSelected(final int pFeatureId, final MenuItem pItem)
    {
	switch (pItem.getItemId())
	{
	case VIBRATE:
	    vibrateOn = vibrateOn ? false : true;
	    savePreferences("VIBRATE", vibrateOn ? "true" : "false");
	    return true;
	case SOUND:
	    soundOn = soundOn ? false : true;
	    savePreferences("SOUND", soundOn ? "true" : "false");
	    if (soundOn)
		resumeSound();
	    else
		pauseSound();
	    return true;
	default:
	    return super.onMenuItemSelected(pFeatureId, pItem);
	}
    }

    private void savePreferences(String key, String value)
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	SharedPreferences.Editor editor = sharedPreferences.edit();
	editor.putString(key, value);
	editor.commit();
    }

    protected void loadOptions()
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	String savedVibrate = sharedPreferences.getString("VIBRATE", "");
	String savedSound = sharedPreferences.getString("SOUND", "");
	if ("true".equals(savedSound))
	    soundOn = true;
	if ("true".equals(savedVibrate))
	    vibrateOn = true;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public abstract void pauseSound();

    public abstract void resumeSound();

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
