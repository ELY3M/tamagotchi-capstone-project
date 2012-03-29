package com.tamaproject.database;

import com.tamaproject.entity.Backpack;
import com.tamaproject.entity.Item;
import com.tamaproject.entity.Tamagotchi;

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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * 
 * Save and loads the information about the Tamagotchi
 * @author Vamshi
 *
 */

public class DBAdapter {
	
	public static final String colID = "_id";
	public static final String colCurHealth = "curHealth";
	public static final String colMaxHealth = "maxHealth";
	public static final String colCurHunger = "curHunger";
	public static final String colMaxHunger = "maxHunger";
	public static final String colCurXP = "curXP";
	public static final String colMaxXP = "maxXp";
	public static final String colCurSickness = "curSickness";
	public static final String colMaxSickness = "maxSickness";
	public static final String colPoop = "poop";
	public static final String colBattleLevel = "battleLevel";
	public static final String colStatus = "status";
	public static final String colBirthday = "birthday";
	public static final String colEquippedItem = "equippedItem";
	public static final String colAge = "age";
	private static final String TamaTable = "Tamagotchi";
	
	public static final String colIPOID = "_id";
	public static final String colXCoord = "xCoord";
	public static final String colYCoord = "yCoord";
	public static final String colItem = "itemName";
	public static final String colScale = "scale";
	private static final String IPOTable = "InPlayObjects";
	
	public static final String colItemID = "_id";
	public static final String colItemName = "itemName";
	public static final String colHealth = "health";
	public static final String colHunger = "hunger";
	public static final String colSickness = "sickness";
	public static final String colXP = "xp";
	public static final String colDescription = "description";
	private static final String ItemTable = "Items";
	
	public static final String colItemName2 = "itemName";
	public static final String colQuantity = "quantity";
	private static final String BackpackTable = "Backpack";
	
	public static final String colItemName3 = "itemName";
	public static final String colFileName = "filename";
	private static final String FilenameTable = "Filename";
	
	private static final String dbName = "TamagotchiProject";
	private static final int dbVersion = 1;
	private static final String tag = "DBAdapter";
	
	private static final String createTamaTable = "create table "+TamaTable+" (_id integer primary key autoincrement, curHealth integer" +
			"not null, maxHealth integer not null, curHunger integer not null, maxHunger integer not null, curXP integer not null, " +
			"maxXP integer not null, curSickness integer not null, maxSickness integer not null, poop integer not null, battleLevel" +
			"integer not null, status integer not null, birthday integer not null, equippedItem text not null, age integer not null, filePath text);";
	
	private static final String createIPOTable = "create table "+IPOTable+" (_id integer primary key autoincrement, xCoord float" +
			"not null, yCoord float not null, itemName text not null);";
	
	private static final String createItemTable = "create table "+ItemTable+" (_id integer primary key autoincrement " +
			"itemName text unique not null, health integer not null, " +
			"hunger integer not null, sickness integer not null, xp integer not null, description text not null);";
	
	private static final String createBackpackTable = "create table "+BackpackTable+" (itemName text unique not null, quantity integer" +
			"not null);";
	
	private static final String createFilenameTable = "create table "+FilenameTable+" (itemName text unique not null, filename text not null);";
	
	
	private final Context context;
	
	private static DatabaseHelper DBHelper;
	private static SQLiteDatabase db;
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		/**
		 * Constructor
		 * 
		 * @param context
		 */
		DatabaseHelper(Context context) {
			super(context, dbName, null, dbVersion);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(createTamaTable);
			db.execSQL(createIPOTable);
			db.execSQL(createItemTable);
			db.execSQL(createBackpackTable);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(tag, "Upgrading database from version " +
					oldVersion + "to " + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists Tamagotchi");
			db.execSQL("drop table if exists InPlayObjects");
			db.execSQL("drop table if exists Items");
			db.execSQL("drop table if exists Backpack");
			onCreate(db);
		}
		
		/**
		 * Opens the database
		 * 
		 * @return
		 * @throws SQLException
		 */
		public DatabaseHelper open() throws SQLException {
			db = DBHelper.getWritableDatabase();
			return this;
		}
		
		/**
		 * Closes the database
		 */
		public void close() {
			DBHelper.close();
		}
		
		/**
		 * Inserts the Tamagotchi attributes into the database for the first time
		 * 
		 * @param t
		 * @return
		 */
		public long insertTama(Tamagotchi t) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(colID, t.getID());
			initialValues.put(colCurHealth, t.getCurrentHealth());
			initialValues.put(colMaxHealth, t.getMaxHealth());
			initialValues.put(colCurHunger, t.getCurrentHunger());
			initialValues.put(colMaxHunger, t.getMaxHunger());
			initialValues.put(colCurXP, t.getCurrentXP());
			initialValues.put(colMaxXP, t.getMaxXP());
			initialValues.put(colCurSickness, t.getCurrentSickness());
			initialValues.put(colMaxSickness, t.getMaxSickness());
			/*initialValues.put(colPoop, );*/
			initialValues.put(colBattleLevel, t.getBattleLevel());
			initialValues.put(colStatus, t.getStatus());
			initialValues.put(colBirthday, t.getBirthday());
			initialValues.put(colEquippedItem, t.getEquippedItemName());
			initialValues.put(colAge, t.getAge());
			long success = db.insert(TamaTable, null, initialValues);
			
			if(success < 0) {
				return saveTama(t);
			}
			else {
				return success;
			}
		
		}
		
		/**
		 * Saves the Tamagotchi attributes into the database after the initial save
		 * 
		 * @param t
		 * @returns true if tamagotchi is saved successfully
		 */
		public int saveTama(Tamagotchi t) {
			
			ContentValues args = new ContentValues();
			args.put(colID, t.getID());
			args.put(colCurHealth, t.getCurrentHealth());
			args.put(colMaxHealth, t.getMaxHealth());
			args.put(colCurHunger, t.getCurrentHunger());
			args.put(colMaxHunger, t.getMaxHunger());
			args.put(colCurXP, t.getCurrentXP());
			args.put(colMaxXP, t.getMaxXP());
			args.put(colCurSickness, t.getCurrentSickness());
			args.put(colMaxSickness, t.getMaxSickness());
			args.put(colBattleLevel, t.getBattleLevel());
			args.put(colStatus, t.getStatus());
			args.put(colBirthday, t.getBirthday());
			args.put(colEquippedItem, t.getEquippedItemName());
			args.put(colAge, t.getAge());
			return db.update(TamaTable, args, colID+"="+t.getID(), null);
		}
		
		/**
		 * Inserts the In Play Objects into the database table for the first time
		 * 
		 * @param id
		 * @param xCoord
		 * @param yCoord
		 * @param itemName
		 * @return
		 */
		public long insertIPOs(int id, float xCoord, float yCoord, String itemName) {
			
			ContentValues initialValues = new ContentValues();
			initialValues.put(colIPOID, id);
			initialValues.put(colXCoord, xCoord);
			initialValues.put(colYCoord, yCoord);
			initialValues.put(colItem, itemName);
			return db.insert(IPOTable, null, initialValues);
		}
		
		/**
		 * Updates the in play objects in the database after the initial save
		 * 
		 * @param id
		 * @param xCoord
		 * @param yCoord
		 * @param itemName
		 * @return
		 */
		public boolean saveIPOs(int id, float xCoord, float yCoord, String itemName) {
			
			ContentValues args = new ContentValues();
			args.put(colIPOID, id);
			args.put(colXCoord, xCoord);
			args.put(colYCoord, yCoord);
			args.put(colItem, itemName);
			return db.update(IPOTable, args, colIPOID+"="+id, null) > 0;
		}
		
		/**
		 * Inserts the items data into the database for the first time
		 * @param i
		 * @return
		 */
		public long insertItems(Item i) {
			
			ContentValues initialValues = new ContentValues();
			/*initialValues.put(colCategory, i.getCategory);*/  
			initialValues.put(colItemName, i.getName());
			initialValues.put(colHealth, i.getHealth());
			initialValues.put(colHunger, i.getHunger());
			initialValues.put(colSickness, i.getSickness());
			initialValues.put(colXP, i.getXp());
			initialValues.put(colDescription, i.getDescription());
			return db.insert(ItemTable, null, initialValues);
		}
		
		/**
		 * Inserts the items that the user currently has in the game for the first time
		 * 
		 * @param id
		 * @param quantity
		 * @return
		 */
		 
		public long insertBackpack(List<Item> item) {
			Map<String, Integer> table = new HashMap<String, Integer>();
			
			for(int i = 0; i < item.size(); i++) {
				String name = item.get(i).getName();
				
				if(table.get(name) == null) {
					table.put(name, 1);
				}
				else {
					Integer counter = (Integer) table.get(name);
					table.put(name, counter + 1);
				}
			}
			return insertParseTable(table);
		}
		
		/**
		 * Takes the hash table created by the above function and takes each key and value and stores it into the database
		 * @param table
		 * @return
		 */
		public long insertParseTable(Map<String, Integer> table) {
			Iterator it = table.entrySet().iterator();
			long success = (Long) null;
			ContentValues initialValues = new ContentValues();
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				initialValues.put(colItemName2, (String) pair.getKey());
				initialValues.put(colQuantity, (Integer) pair.getValue());
				success = db.insert(BackpackTable, null, initialValues);
				if(success < 0) {
					return success;
				}
			}
			return success;
		}
		
		public boolean saveParseTable(Map<String, Integer> table) {
			Iterator it = table.entrySet().iterator();
			ContentValues args = new ContentValues();
			int success = (Integer) null;
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				args.put(colItemName2, (String) pair.getKey());
				args.put(colQuantity, (Integer) pair.getValue());
				success = db.update(BackpackTable, args, colItemName2+"="+pair.getKey(), null);
				if(success < 0) {
					return false;
				}
			}
			return success > 0;
		}
		
		/**
		 * Retrieves the last saved attributes of the Tamagotchi
		 * 		
		 * @param id
		 * @return
		 */
		public Tamagotchi retrieveTama(int id, Hashtable<String, TextureRegion> table) {
			Cursor cursor1 =  db.query(true, TamaTable, new String[] {colID,colCurHealth, colMaxHealth, 
					colCurHunger, colMaxHunger, colCurXP, colMaxXP, colCurSickness, 
					colMaxSickness, colBattleLevel, colStatus, colBirthday, colEquippedItem, colAge}, colID+"="+id, null, null, null, null, null);
			
			Cursor cursor2 = db.query(ItemTable, new String[] {colItemName, colHealth, colHunger, colSickness, colXP, colDescription},
					colItemName+"="+cursor1.getString(cursor1.getColumnIndexOrThrow(colEquippedItem)), null, null, null, null);
			
			Cursor cursor3 = db.query(FilenameTable, new String[] {colFileName}, colItemName3+"="+cursor1.getString(cursor1.getColumnIndexOrThrow(colEquippedItem)), 
					null, null, null, null);
			
			if(cursor1 != null && cursor2 != null && cursor3 != null) {
				cursor1.moveToFirst();
				cursor2.moveToFirst();
				cursor3.moveToFirst();
			}
			
			return this.loadTama(cursor1, cursor2, cursor3, table);
			
		}
		
		
		/**
		 * Loads the tamagotchi attributes with the last saved data once the game starts
		 * 
		 * @param cursor
		 * @returns the tamagotchi object
		 */
		Tamagotchi loadTama(Cursor cursor1, Cursor cursor2, Cursor cursor3, Hashtable<String, TextureRegion> table) {
				
			int curHealth = cursor1.getInt(cursor1.getColumnIndexOrThrow(colCurHealth));
			int maxHealth = cursor1.getInt(cursor1.getColumnIndexOrThrow(colMaxHealth));
			int curHunger = cursor1.getInt(cursor1.getColumnIndexOrThrow(colCurHunger));
			int maxHunger = cursor1.getInt(cursor1.getColumnIndexOrThrow(colMaxHunger));
			int curXP = cursor1.getInt(cursor1.getColumnIndexOrThrow(colCurXP));
			int maxXP = cursor1.getInt(cursor1.getColumnIndexOrThrow(colMaxXP));
			int curSickness = cursor1.getInt(cursor1.getColumnIndexOrThrow(colCurSickness));
			int maxSickness = cursor1.getInt(cursor1.getColumnIndexOrThrow(colMaxSickness));
			int battleLevel = cursor1.getInt(cursor1.getColumnIndexOrThrow(colBattleLevel));
			int status = cursor1.getInt(cursor1.getColumnIndexOrThrow(colStatus));
			long birthday = cursor1.getLong(cursor1.getColumnIndexOrThrow(colBirthday));
			long age = cursor1.getLong(cursor1.getColumnIndexOrThrow(colAge));
			int id = cursor1.getInt(cursor1.getColumnIndexOrThrow(colID));
			
			String equippedItemName = cursor2.getString(cursor2.getColumnIndexOrThrow(colItemName));
			int health = cursor2.getInt(cursor2.getColumnIndexOrThrow(colHealth));
			int hunger = cursor2.getInt(cursor2.getColumnIndexOrThrow(colHunger));
			int sickness = cursor2.getInt(cursor2.getColumnIndexOrThrow(colSickness));
			int xp = cursor2.getInt(cursor2.getColumnIndexOrThrow(colXP));
			String description = cursor2.getString(cursor2.getColumnIndexOrThrow(colDescription));
			TextureRegion textureRegion = table.get(cursor3.getString(cursor3.getColumnIndexOrThrow(colFileName)));
			
			Item equippedItem = new Item(0, 0, textureRegion, equippedItemName, description, health, hunger, sickness, xp);
			
			
			
			return new Tamagotchi(curHealth, maxHealth, curHunger, maxHunger, curXP, maxXP, curSickness, maxSickness, 
					battleLevel, status, birthday, equippedItem, age, id);
		}
		
		public Cursor retreatIPO() {
			return db.query(IPOTable, new String[] {colIPOID, colXCoord, colYCoord, colItem}, null, null, null, null, null);
		}
		
		public Cursor retreatItems() {
			return db.query(ItemTable, new String[] {colItemID, colItemName, colHealth, colHunger, 
					colSickness, colXP, colDescription}, null, null, null, null,  null);
		}
		
		/**
		 * retrieves the backpack items from last saved and places them into an arraylist
		 * @param table
		 * @return an arraylist with item object and the quantity for each object
		 */
		public ArrayList<Item> getBackpack(Hashtable<String, TextureRegion> table) {
			Cursor c = db.query(BackpackTable, new String[] {colItemName2, colQuantity}, null, null, null, null, null);
			Cursor c2 = db.query(ItemTable, new String[] {colItemName, colHealth, colHunger, colSickness, colXP, colDescription}, 
					colItemName+"="+c.getString(c.getColumnIndexOrThrow(colItemName2)), null, null, null, null);
			Cursor c3 = db.query(FilenameTable, new String[] {colFileName}, colItemName3+"="+c2.getString(c2.getColumnIndexOrThrow(colEquippedItem)), 
					null, null, null, null);
			
			ArrayList<Item> resultSet = new ArrayList<Item>();
			c.moveToFirst();
			c2.moveToFirst();
			c3.moveToFirst();
			
			if(!c.isAfterLast() && !c2.isAfterLast() && !c3.isAfterLast()) {
				do {
					String itemName = c2.getString(c2.getColumnIndexOrThrow(colItemName));
					int health = c2.getInt(c2.getColumnIndexOrThrow(colHealth));
					int hunger = c2.getInt(c2.getColumnIndexOrThrow(colHunger));
					int sickness = c2.getInt(c2.getColumnIndexOrThrow(colSickness));
					int xp = c2.getInt(c2.getColumnIndexOrThrow(colXP));
					String description = c2.getString(c2.getColumnIndexOrThrow(colDescription));
					TextureRegion textureRegion = table.get(c3.getString(c3.getColumnIndexOrThrow(colFileName)));
					int quantity = c.getInt(c.getColumnIndexOrThrow(colQuantity));
					Item i = new Item(0, 0, textureRegion, itemName, description, health, hunger, sickness, xp);
					resultSet.add(i);
				} while(c.moveToNext() && c2.moveToNext() && c3.moveToNext());
			}
			return resultSet;
		}
	}
}