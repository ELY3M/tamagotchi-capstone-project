package com.tamaproject.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anddev.andengine.opengl.texture.region.TextureRegion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.tamaproject.entity.Item;
import com.tamaproject.entity.Tamagotchi;

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
    public DatabaseHelper(Context ctx) throws IOException
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
	    System.out.println("Database Exists");
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

    public static void createDatabaseIfNotExists(Context context) throws IOException
    {
	boolean createDb = false;

	File dbDir = new File(DB_PATH);
	File dbFile = new File(DB_PATH + DB_NAME);
	if (!dbDir.exists())
	{
	    dbDir.mkdir();
	    createDb = true;
	}
	else if (!dbFile.exists())
	{
	    createDb = true;
	}
	else
	{
	    // Check that we have the latest version of the db
	    boolean doUpgrade = false;

	    // Insert your own logic here on whether to upgrade the db; I personally
	    // just store the db version # in a text file, but you can do whatever
	    // you want. I've tried MD5 hashing the db before, but that takes a while.

	    // If we are doing an upgrade, basically we just delete the db then
	    // flip the switch to create a new one
	    if (doUpgrade)
	    {
		dbFile.delete();
		createDb = true;
	    }
	}

	if (createDb)
	{
	    System.out.println("Database not found, copying from assets...");
	    // Open your local db as the input stream
	    InputStream myInput = context.getAssets().open(DB_NAME);

	    // Open the empty db as the output stream
	    OutputStream myOutput = new FileOutputStream(dbFile);

	    // transfer bytes from the inputfile to the outputfile
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = myInput.read(buffer)) > 0)
	    {
		myOutput.write(buffer, 0, length);
	    }

	    // Close the streams
	    myOutput.flush();
	    myOutput.close();
	    myInput.close();
	}
    }

    public static SQLiteDatabase getStaticDb()
    {
	return SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
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
	    checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
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

    /**
     * Opens the database
     * 
     * @throws SQLiteException
     */
    public void openDatabase() throws SQLiteException
    {
	String path = DB_PATH + DB_NAME;
	db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
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

    /**
     * Inserts the tama attributes into the database for the first time
     * 
     * @param t
     * @return
     */
    public long insertTama(Tamagotchi t)
    {
	System.out.println("Insert Tama");
	ContentValues initialValues = new ContentValues();
	initialValues.put("_id", t.getID());
	initialValues.put("curHealth", t.getCurrentHealth());
	initialValues.put("maxHealth", t.getMaxHealth());
	initialValues.put("curHunger", t.getCurrentHunger());
	initialValues.put("maxHunger", t.getMaxHunger());
	initialValues.put("curXP", t.getCurrentXP());
	initialValues.put("maxXP", t.getMaxXP());
	initialValues.put("curSickness", t.getCurrentSickness());
	initialValues.put("maxSickness", t.getMaxSickness());
	/* initialValues.put(colPoop, ); */
	initialValues.put("battleLevel", t.getBattleLevel());
	initialValues.put("status", t.getStatus());
	initialValues.put("birthday", t.getBirthday());
	initialValues.put("equippedItem", t.getEquippedItemName());
	initialValues.put("age", t.getAge());
	long success = db.insert("Tamagotchi", null, initialValues);
	if (success < 0)
	    return saveTama(t);
	else
	    return success;
    }

    /**
     * saves the tama into the database after the initial save
     * 
     * @param t
     * @return
     */
    public int saveTama(Tamagotchi t)
    {
	System.out.println("Save Tama");
	ContentValues args = new ContentValues();
	args.put("_id", t.getID());
	args.put("curHealth", t.getCurrentHealth());
	args.put("maxHealth", t.getMaxHealth());
	args.put("curHunger", t.getCurrentHunger());
	args.put("maxHunger", t.getMaxHunger());
	args.put("curXP", t.getCurrentXP());
	args.put("maxXP", t.getMaxXP());
	args.put("curSickness", t.getCurrentSickness());
	args.put("maxSickness", t.getMaxSickness());
	/* initialValues.put(colPoop, ); */
	args.put("battleLevel", t.getBattleLevel());
	args.put("status", t.getStatus());
	args.put("birthday", t.getBirthday());
	args.put("equippedItem", t.getEquippedItemName());
	args.put("age", t.getAge());
	return db.update("Tamagotchi", args, "_id = " + t.getID(), null);
    }

    /**
     * loads the tama with its last saved attributes
     * 
     * @param id
     * @return a Tamagotchi object
     */
    public Tamagotchi loadTama(int id)
    {
	try
	{

	    Cursor c = db.rawQuery("Select _id, curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness," + "maxSickness, battleLevel, status, birthday, equippedItem, age from Tamagotchi where _id = " + id, null);

	    if (c != null)
	    {
		c.moveToFirst();
	    }

	    int curHealth = c.getInt(c.getColumnIndex("curHealth"));
	    int maxHealth = c.getInt(c.getColumnIndex("maxHealth"));
	    int curHunger = c.getInt(c.getColumnIndex("curHunger"));
	    int maxHunger = c.getInt(c.getColumnIndex("maxHunger"));
	    int curXP = c.getInt(c.getColumnIndex("curXP"));
	    int maxXP = c.getInt(c.getColumnIndex("maxXP"));
	    int curSickness = c.getInt(c.getColumnIndex("curSickness"));
	    int maxSickness = c.getInt(c.getColumnIndex("maxSickness"));
	    int battleLevel = c.getInt(c.getColumnIndex("battleLevel"));
	    int status = c.getInt(c.getColumnIndex("status"));
	    long birthday = c.getLong(c.getColumnIndex("birthday"));
	    long age = c.getLong(c.getColumnIndex("age"));
	    Item equippedItem = null;
	    if ("None".equals(c.getString(c.getColumnIndex("equippedItem"))))
	    {
		return new Tamagotchi(curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness, maxSickness, battleLevel, status, birthday, null, age, id);
	    }
	    else
	    {
		Cursor c2 = db.rawQuery("Select _id, itemName, health, hunger, sickness, xp, protection, type, description FROM Items where itemName = '" + c.getString(c.getColumnIndex("equippedItem")) + "'", null);
		Cursor c3 = db.rawQuery("Select itemName, filename from Filenames where itemName = '" + c.getString(c.getColumnIndex("equippedItem")) + "'", null);
		if (c2 != null && c3 != null)
		{
		    c2.moveToFirst();
		    c3.moveToFirst();

		    String equippedItemName = c2.getString(c2.getColumnIndex("itemName"));
		    int health = c2.getInt(c2.getColumnIndex("health"));
		    int hunger = c2.getInt(c2.getColumnIndex("hunger"));
		    int sickness = c2.getInt(c2.getColumnIndex("sickness"));
		    int xp = c2.getInt(c2.getColumnIndex("xp"));
		    int protection = c2.getInt(c2.getColumnIndex("protection"));
		    int type = c2.getInt(c2.getColumnIndex("type"));
		    String description = c2.getString(c2.getColumnIndex("description"));
		    // TextureRegion textureRegion = table.get(cursor3.getString(cursor3.getColumnIndex(colFileName)));
		    TextureRegion textureRegion = null;

		    equippedItem = new Item(0, 0, textureRegion, equippedItemName, description, health, hunger, sickness, xp, type, protection);
		}

		return new Tamagotchi(curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness, maxSickness, battleLevel, status, birthday, equippedItem, age, id);
	    }

	} catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * parses through the arraylist of items in the backpack and places them into a hashmap with the quantity of each item
     * 
     * @param item
     * @return
     */
    public long insertBackpack(List<Item> item)
    {
	Map<String, Integer> table = new HashMap<String, Integer>();

	for (int i = 0; i < item.size(); i++)
	{
	    String name = item.get(i).getName();

	    if (table.get(name) == null)
	    {
		table.put(name, 1);
	    }
	    else
	    {
		Integer counter = (Integer) table.get(name);
		table.put(name, counter + 1);
	    }
	}

	return insertParseTable(table);
    }

    /**
     * takes the hashmap created above and stores the contents into the database for the first time
     * 
     * @param table
     * @return
     */
    public long insertParseTable(Map<String, Integer> table)
    {
	Iterator it = table.entrySet().iterator();
	ContentValues bp = new ContentValues();
	long success = 0;
	while (it.hasNext())
	{
	    Map.Entry pair = (Map.Entry) it.next();
	    bp.put("itemName", (String) pair.getKey());
	    bp.put("quantity", (Integer) pair.getValue());
	    success = db.insert("Backpack", null, bp);
	    if (success < 0)
	    {
		ContentValues args = new ContentValues();
		args.put("quantity", (Integer) pair.getValue());
		success = db.update("Backpack", args, "itemName = '" + pair.getKey() + "'", null);
		if (success < 0)
		    return success;
		else
		    System.out.println(pair.getKey() + " saved");
	    }
	    else
	    {
		System.out.println(pair.getKey() + " saved");
	    }
	}
	return success;
    }

    /**
     * retrieves the backpack from the database
     * 
     * @param table
     * @return an arraylist of items
     */
    public ArrayList<Item> getBackpack(Hashtable<String, TextureRegion> table)
    {
	System.out.println("Get Backpack");
	try
	{
	    Cursor c = db.rawQuery("select itemName, quantity from Backpack", null);
	    c.moveToFirst();

	    ArrayList<Item> resultSet = new ArrayList<Item>();

	    if (!c.isAfterLast())
	    {
		do
		{
		    Cursor c2 = db.rawQuery("select itemName, health, hunger, sickness, xp, protection, description, type from Items where itemName = '" + c.getString(c.getColumnIndex("itemName")) + "'", null);
		    c2.moveToFirst();

		    Cursor c3 = db.rawQuery("select filename from Filenames where itemName = '" + c2.getString(c2.getColumnIndex("itemName")) + "'", null);
		    c3.moveToFirst();

		    String itemName = c2.getString(c2.getColumnIndex("itemName"));
		    int health = c2.getInt(c2.getColumnIndex("health"));
		    int hunger = c2.getInt(c2.getColumnIndex("hunger"));
		    int sickness = c2.getInt(c2.getColumnIndex("sickness"));
		    int xp = c2.getInt(c2.getColumnIndex("xp"));
		    int protection = c2.getInt(c2.getColumnIndex("protection"));
		    int type = c2.getInt(c2.getColumnIndex("type"));
		    String description = c2.getString(c2.getColumnIndex("description"));

		    TextureRegion textureRegion = table.get(c3.getString(c3.getColumnIndex("filename")));
		    int quantity = c.getInt(c.getColumnIndex("quantity"));
		    for (int j = 0; j < quantity; j++)
		    {
			System.out.println("Adding " + itemName + " to backpack");
			Item i = new Item(0, 0, textureRegion, itemName, description, health, hunger, sickness, xp, type, protection);
			resultSet.add(i);
		    }
		} while (c.moveToNext());
	    }
	    return resultSet;
	} catch (Exception e)
	{
	    e.printStackTrace();
	    return new ArrayList<Item>();
	}
    }
}