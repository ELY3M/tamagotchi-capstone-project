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
	initialValues.put("money", t.getMoney());
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
	args.put("money", t.getMoney());
	return db.update("Tamagotchi", args, "_id = " + t.getID(), null);
    }

    public int saveMoney(int money, int id)
    {
	System.out.println("Save Money");
	ContentValues args = new ContentValues();
	args.put("money", money);
	return db.update("Tamagotchi", args, "_id =" + id, null);
    }

    public int loadMoney(int id)
    {
	try
	{
	    Cursor c = db.rawQuery("Select money from Tamagotchi where _id = " + id, null);

	    if (c != null)
	    {
		c.moveToFirst();
	    }

	    return c.getInt(c.getColumnIndex("money"));
	} catch (Exception e)
	{
	    e.printStackTrace();
	    return -1;
	}
    }

    /**
     * loads the tama with its last saved attributes
     * 
     * @param id
     * @return a Tamagotchi object
     */
    public Tamagotchi loadTama(int id, Hashtable<String, TextureRegion> table)
    {
	try
	{

	    Cursor c = db.rawQuery("Select * from Tamagotchi where _id = " + id, null);

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
	    int money = c.getInt(c.getColumnIndex("money"));
	    Item equippedItem = null;
	    if ("None".equals(c.getString(c.getColumnIndex("equippedItem"))))
	    {
		return new Tamagotchi(curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness, maxSickness, battleLevel, status, birthday, null, age, id, money);
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
		    TextureRegion textureRegion = table.get(c3.getString(c3.getColumnIndex("filename")));

		    equippedItem = new Item(0, 0, textureRegion, equippedItemName, description, health, hunger, sickness, xp, type, protection);
		}

		return new Tamagotchi(curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness, maxSickness, battleLevel, status, birthday, equippedItem, age, id, money);
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
	db.delete("Backpack", null, null);

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
	    Cursor c = db.rawQuery("select * from Backpack", null);
	    c.moveToFirst();

	    ArrayList<Item> resultSet = new ArrayList<Item>();

	    if (!c.isAfterLast())
	    {
		do
		{
		    Cursor c2 = db.rawQuery("select * from Items where itemName = '" + c.getString(c.getColumnIndex("itemName")) + "'", null);
		    c2.moveToFirst();

		    Cursor c3 = db.rawQuery("select * from Filenames where itemName = '" + c2.getString(c2.getColumnIndex("itemName")) + "'", null);
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

    public ArrayList<Item> getAllItems(Hashtable<String, TextureRegion> table)
    {
	System.out.println("Get All Items");
	try
	{
	    Cursor c = db.rawQuery("select * from Items", null);
	    c.moveToFirst();

	    ArrayList<Item> resultSet = new ArrayList<Item>();

	    if (!c.isAfterLast())
	    {
		do
		{
		    try
		    {
			Cursor c3 = db.rawQuery("select filename from Filenames where itemName = '" + c.getString(c.getColumnIndex("itemName")) + "'", null);
			c3.moveToFirst();

			String itemName = c.getString(c.getColumnIndex("itemName"));
			int health = c.getInt(c.getColumnIndex("health"));
			int hunger = c.getInt(c.getColumnIndex("hunger"));
			int sickness = c.getInt(c.getColumnIndex("sickness"));
			int xp = c.getInt(c.getColumnIndex("xp"));
			int protection = c.getInt(c.getColumnIndex("protection"));
			int type = c.getInt(c.getColumnIndex("type"));
			String description = c.getString(c.getColumnIndex("description"));
			int price = c.getInt(c.getColumnIndex("price"));

			TextureRegion textureRegion = table.get(c3.getString(c3.getColumnIndex("filename")));

			Item i = new Item(0, 0, textureRegion, itemName, description, health, hunger, sickness, xp, type, protection, price);
			resultSet.add(i);
		    } catch (Exception e)
		    {
			e.printStackTrace();
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