package com.tamaproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;

public class LoginActivity extends Activity
{
    private EditText usernameEditText = null;
    private EditText passwordEditText = null;
    private final String USERNAME = "username";
    private final String PASSWORD = "password";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login);

	Button launch = (Button) findViewById(R.id.login_button);

	usernameEditText = (EditText) findViewById(R.id.txt_username);
	passwordEditText = (EditText) findViewById(R.id.txt_password);
	LoadPreferences();

	// this is the action listener
	launch.setOnClickListener(new OnClickListener()
	{

	    public void onClick(View viewParam)
	    {

		// the getText() gets the current value of the text box
		// the toString() converts the value to String data type
		// then assigns it to a variable of type String
		String sUserName = usernameEditText.getText().toString();
		String sPassword = passwordEditText.getText().toString();

		// this just catches the error if the program cant locate the GUI stuff
		if (usernameEditText == null || passwordEditText == null)
		{
		    Toast.makeText(LoginActivity.this, "Couldn't find the 'txt_username' or 'txt_password'", Toast.LENGTH_SHORT).show();
		}
		else
		{
		    // display the username and the password in string format
		    SavePreferences(USERNAME, sUserName);
		    SavePreferences(PASSWORD, sPassword);
		    if (checkLogin(sUserName, sPassword))
			startGame();
		}
	    }
	}

	); // end of launch.setOnclickListener
    }

    private boolean checkLogin(String username, String password)
    {
	Toast.makeText(LoginActivity.this, "Username: " + username + ", Password: " + password, Toast.LENGTH_SHORT).show();
	return true;
    }

    private void startGame()
    {
	Intent goToGame = new Intent(LoginActivity.this, GameActivity.class);
	startActivity(goToGame);
	finish();
    }

    private void SavePreferences(String key, String value)
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	SharedPreferences.Editor editor = sharedPreferences.edit();
	editor.putString(key, value);
	editor.commit();
    }

    private void LoadPreferences()
    {
	SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
	String user = sharedPreferences.getString(USERNAME, null);
	String pass = sharedPreferences.getString(PASSWORD, null);
	if (user != null && pass != null)
	{
	    if (checkLogin(user, pass))
		startGame();
	}
    }
}