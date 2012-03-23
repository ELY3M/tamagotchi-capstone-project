package com.tamaproject.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{

    /* Android's default system path of the appalication database */
    private static String DB_PATH = "/data/data/com.tamaproject/databases/";
    private static String DB_NAME = "tamagotchi";
    private SQLiteDatabase db;
    private final Context context;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to access the application assets and resources.
     * 
     * @param context
     */
    public DatabaseHelper(Context ctx)
    {
	super(ctx, DB_NAME, null, 1);
	this.context = ctx;
    }

    /**
     * Creates an empty database on the system and rewrites it with the database.
     */
    public void createDatabase() throws IOException
    {
	boolean dbExist = checkDatabase();

	if (dbExist)
	{
	    /* Do nothing - database already exists */
	}
	else
	{
	    /*
	     * By calling this method and empty database will be created into the default system path of the application so we can overwrite the database with the application
	     */
	    this.getReadableDatabase();

	    try
	    {
		copyDatabase();
	    } catch (IOException e)
	    {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time the application is opened.
     * 
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDatabase()
    {
	SQLiteDatabase checkDB = null;

	try
	{
	    String path = DB_PATH + DB_NAME;
	    checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
	} catch (SQLiteException e)
	{
	    e.printStackTrace();
	}

	if (checkDB != null)
	{
	    checkDB.close();
	}

	return checkDB != null ? true : false;
    }

    /**
     * Copies your database from the local assets-folder to the just created empty database in teh system folder, from where it can be accessed and handled. This is done by transferring bytestream.
     */
    private void copyDatabase() throws IOException
    {
	/* Open your local db as the input stream */
	InputStream input = context.getAssets().open(DB_NAME);

	/* Path to the just create db */
	String outFileName = DB_PATH + DB_NAME;

	/* Open the empty db as the output stream */
	OutputStream output = new FileOutputStream(outFileName);

	/* Transfer bytes from the inputfile to outputfile */
	byte[] buffer = new byte[1024];
	int length;
	while ((length = input.read(buffer)) > 0)
	{
	    output.write(buffer, 0, length);
	}

	/* Close the Streams */
	output.flush();
	output.close();
	input.close();
    }

    public void openDatabase() throws SQLiteException
    {
	/* Open the database */
	String path = DB_PATH + DB_NAME;
	db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close()
    {
	if (db != null)
	{
	    db.close();
	}
	super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
	// TODO Auto-generated method stub

    }

    public void saveTama()
    {

    }

}