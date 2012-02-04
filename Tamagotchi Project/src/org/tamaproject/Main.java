package org.tamaproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	Button launch = (Button) findViewById(R.id.login_button);

	// this is the action listener
	launch.setOnClickListener(new OnClickListener()
	{

	    public void onClick(View viewParam)
	    {
		// this gets the resources in the xml file and assigns it to a local variable of type EditText
		EditText usernameEditText = (EditText) findViewById(R.id.txt_username);
		EditText passwordEditText = (EditText) findViewById(R.id.txt_password);

		// the getText() gets the current value of the text box
		// the toString() converts the value to String data type
		// then assigns it to a variable of type String
		String sUserName = usernameEditText.getText().toString();
		String sPassword = passwordEditText.getText().toString();

		// this just catches the error if the program cant locate the GUI stuff
		if (usernameEditText == null || passwordEditText == null)
		{
		    Toast.makeText(Main.this, "Couldn't find the 'txt_username' or 'txt_password'", Toast.LENGTH_SHORT).show();
		}
		else
		{
		    // display the username and the password in string format
		    Toast.makeText(Main.this, "Username: " + sUserName + ", Password: " + sPassword, Toast.LENGTH_SHORT).show();
		}
	    }
	}

	); // end of launch.setOnclickListener
    }
}