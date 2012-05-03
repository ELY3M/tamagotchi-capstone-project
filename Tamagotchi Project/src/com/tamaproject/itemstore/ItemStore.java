package com.tamaproject.itemstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import com.tamaproject.BaseAndEngineGame;
import com.tamaproject.database.DatabaseHelper;
import com.tamaproject.entity.Backpack;
import com.tamaproject.entity.Item;
import com.tamaproject.util.TextUtil;
import com.tamaproject.util.TextureUtil;

/**
 * Item store. Loads all available items from database and allows user to purchase them.
 * 
 * @author Jonathan
 * 
 */
public class ItemStore extends BaseAndEngineGame
{
    private final static int CONFIRM_PURCHASE = 0;
    private DatabaseHelper dbHelper;
    private Scene mScene;
    private Hashtable<String, TextureRegion> listTR;
    private Camera mCamera;
    private BitmapTextureAtlas mFontTexture;
    private BitmapTextureAtlas mSmallFontTexture;
    private Font mFont;
    private Font mSmallFont;
    private Rectangle itemDescriptionRect;
    private ChangeableText itemDesctiptionText;
    private Entity topLayer;
    private Entity bottomLayer;
    private static final int cameraWidth = 480, cameraHeight = 800;
    private static final int pTopBound = 115, pBottomBound = cameraHeight - 70; // top and bottom bounds of play area
    private static final boolean FULLSCREEN = true;
    private Backpack backpack;
    private int money = 0;
    private int tamaId = 1;
    private Item itemToPurchase;
    private ArrayList<Entity> itemPages = new ArrayList<Entity>();
    private int currentPage = 0;
    private ChangeableText titleText;

    @Override
    public void onLoadComplete()
    {

    }

    @Override
    public void onPause()
    {
	super.onPause();
	this.mEngine.stop();

	if (dbHelper != null)
	{
	    long resultBackpackSave = dbHelper.insertBackpack(backpack.getItems());
	    if (resultBackpackSave < 0)
		Debug.d("Save backpack failed! " + resultBackpackSave);
	    else
		Debug.d("Save backpack success! " + resultBackpackSave);

	    long saveMoneyResult = dbHelper.saveMoney(money, tamaId);
	    if (saveMoneyResult < 0)
	    {
		Debug.d("Save money failed! " + saveMoneyResult);
	    }
	}
    }

    @Override
    public void onResume()
    {
	super.onResume();
	this.mEngine.start();
    }

    @Override
    public void finish()
    {
	Intent returnIntent = new Intent();
	returnIntent.putExtra("money", this.money);
	setResult(RESULT_OK, returnIntent);

	super.finish();
    }

    @Override
    public Engine onLoadEngine()
    {
	this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
	return new Engine(new EngineOptions(FULLSCREEN, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), this.mCamera));
    }

    @Override
    public void onLoadResources()
    {
	this.listTR = TextureUtil.loadTextures(this, this.mEngine, new String[] { new String("gfx/") });

	// Load fonts
	this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mSmallFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	this.mFont = FontFactory.createFromAsset(mFontTexture, this, "ITCKRIST.TTF", 24, true, Color.WHITE);
	this.mSmallFont = FontFactory.createFromAsset(mSmallFontTexture, this, "ITCKRIST.TTF", 18, true, Color.WHITE);
	this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
	this.mEngine.getTextureManager().loadTexture(this.mSmallFontTexture);
	this.mEngine.getFontManager().loadFont(this.mFont);
	this.mEngine.getFontManager().loadFont(this.mSmallFont);

    }

    @Override
    public Scene onLoadScene()
    {
	try
	{
	    dbHelper = new DatabaseHelper(this);
	} catch (IOException e)
	{
	    e.printStackTrace();
	}

	try
	{
	    DatabaseHelper.createDatabaseIfNotExists(this);
	    Debug.d("createDatabase()");
	} catch (IOException e)
	{
	    e.printStackTrace();
	}

	try
	{
	    dbHelper.openDatabase();
	    Debug.d("openDatabase()");
	} catch (Exception e)
	{
	    e.printStackTrace();
	}

	Intent intent = getIntent();
	this.money = intent.getIntExtra("money", 0);
	this.tamaId = intent.getIntExtra("tamaId", 1);

	this.mScene = new Scene();
	this.mScene.setBackground(new ColorBackground(247 / 255f, 233 / 255f, 103 / 255f));
	this.topLayer = new Entity();
	this.bottomLayer = new Entity();
	this.mScene.attachChild(bottomLayer);
	this.mScene.attachChild(topLayer);

	this.loadBackpack();
	this.loadStoreItems();
	this.loadItemDescriptionBox();
	this.loadTopBar();
	this.loadBottomBar();

	mScene.setTouchAreaBindingEnabled(true);
	mScene.setOnAreaTouchTraversalFrontToBack();
	return mScene;
    }

    @Override
    public void pauseSound()
    {

    }

    @Override
    public void resumeSound()
    {

    }

    private void loadTopBar()
    {
	final Rectangle topRect = new Rectangle(0, 0, cameraWidth, pTopBound);
	topRect.setColor(169 / 255f, 207 / 255f, 84 / 255f);
	topLayer.attachChild(topRect);

	titleText = new ChangeableText(0, 0, mFont, "Item Store", 25);
	titleText.setPosition(40, topRect.getHeight() / 2 - titleText.getHeight() / 2);
	topRect.attachChild(titleText);

	final Sprite shopIcon = new Sprite(0, 0, listTR.get("shop_big.png"));
	shopIcon.setPosition(cameraWidth - shopIcon.getWidth() - 40, pTopBound / 2 - shopIcon.getHeight() / 2);
	shopIcon.registerEntityModifier(new LoopEntityModifier(new SequenceEntityModifier(new ScaleModifier(1f, 1, 1.1f), new ScaleModifier(1f, 1.1f, 1))));
	topRect.attachChild(shopIcon);
    }

    private void loadBottomBar()
    {
	final Rectangle bottomRect = new Rectangle(0, pBottomBound, cameraWidth, cameraHeight - pBottomBound);
	bottomRect.setColor(169 / 255f, 207 / 255f, 84 / 255f);
	topLayer.attachChild(bottomRect);

	final ChangeableText moneyText = new ChangeableText(0, 0, mFont, "Money: $" + money, 40);
	moneyText.setPosition(40, bottomRect.getHeight() / 2 - moneyText.getHeight() / 2);
	bottomRect.attachChild(moneyText);
	moneyText.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		moneyText.setText("Money: $" + money);
	    }
	});

	final Sprite rightArrowSprite = new Sprite(0, 0, listTR.get("right_icon.png"))
	{
	    private boolean touched = false;
	    private boolean moved = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (!this.getParent().isVisible())
		    return false;

		if (pSceneTouchEvent.isActionDown())
		{
		    touched = true;
		}
		else if (pSceneTouchEvent.isActionMove())
		{
		    moved = true;
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    if (touched)
		    {
			if (currentPage < (itemPages.size() - 1))
			{
			    itemPages.get(currentPage).setVisible(false);
			    itemPages.get(++currentPage).setVisible(true);
			}
		    }

		    touched = false;
		}
		return true;
	    }
	};
	rightArrowSprite.setPosition(bottomRect.getWidth() - rightArrowSprite.getWidth() - 20, bottomRect.getHeight() / 2 - rightArrowSprite.getHeight() / 2);
	rightArrowSprite.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		if (currentPage < (itemPages.size() - 1))
		{
		    rightArrowSprite.setVisible(true);
		}
		else
		{
		    rightArrowSprite.setVisible(false);
		}
	    }
	});
	mScene.registerTouchArea(rightArrowSprite);
	rightArrowSprite.setVisible(false);
	bottomRect.attachChild(rightArrowSprite);

	final Sprite leftArrowSprite = new Sprite(0, 0, listTR.get("left_icon.png"))
	{
	    private boolean touched = false;
	    private boolean moved = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (!this.getParent().isVisible())
		    return false;

		if (pSceneTouchEvent.isActionDown())
		{
		    touched = true;
		}
		else if (pSceneTouchEvent.isActionMove())
		{
		    moved = true;
		}
		else if (pSceneTouchEvent.isActionUp())
		{
		    if (touched)
		    {
			if (currentPage > 0)
			{
			    itemPages.get(currentPage).setVisible(false);
			    itemPages.get(--currentPage).setVisible(true);
			}
		    }

		    touched = false;
		}
		return true;
	    }
	};
	leftArrowSprite.setPosition(rightArrowSprite.getX() - leftArrowSprite.getWidth() - 20, rightArrowSprite.getY());
	leftArrowSprite.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		if (currentPage > 0)
		{
		    leftArrowSprite.setVisible(true);
		}
		else
		{
		    leftArrowSprite.setVisible(false);
		}
	    }
	});
	mScene.registerTouchArea(leftArrowSprite);
	leftArrowSprite.setVisible(false);
	bottomRect.attachChild(leftArrowSprite);
    }

    private void loadStoreItems()
    {
	ArrayList<Item> allItems = dbHelper.getAllItems(listTR);
	float boxHeight = (pBottomBound - pTopBound) / 10;
	int spacing = 5;
	int counter = 0;
	Entity currentLayer = new Entity();
	for (final Item item : allItems)
	{
	    counter++;
	    final Rectangle itemBox = new Rectangle(spacing, pTopBound + spacing, cameraWidth - 2 * spacing, boxHeight - spacing)
	    {
		private boolean touched = false;
		private boolean moved = false;

		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
		    if (!this.getParent().isVisible())
			return false;

		    if (pSceneTouchEvent.isActionDown())
		    {
			touched = true;
		    }
		    else if (pSceneTouchEvent.isActionMove())
		    {
			moved = true;
		    }
		    else if (pSceneTouchEvent.isActionUp())
		    {
			if (touched)
			    showItemDescription(item);

			touched = false;
		    }
		    return true;
		}
	    };
	    mScene.registerTouchArea(itemBox);
	    item.setPosition(2, 2);
	    itemBox.attachChild(item);
	    itemBox.setColor(4 / 255f, 191 / 255f, 191 / 255f);

	    final Text itemText = new Text(0, 0, mSmallFont, item.getName() + ": $" + item.getPrice());
	    itemText.setPosition(item.getWidth() + 5, item.getHeight() / 2 - itemText.getHeight() / 2);
	    item.attachChild(itemText);

	    final Text buyText = new Text(0, 0, mSmallFont, "Buy");
	    final Rectangle buyButton = new Rectangle(0, 0, buyText.getWidth() + 50, itemBox.getHeight())
	    {

		private boolean touched = false;
		private boolean moved = false;

		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
		    if (!itemBox.getParent().isVisible())
			return false;

		    if (pSceneTouchEvent.isActionDown())
		    {
			touched = true;
		    }
		    else if (pSceneTouchEvent.isActionMove())
		    {
			moved = true;
		    }
		    else if (pSceneTouchEvent.isActionUp())
		    {
			if (touched)
			{
			    itemToPurchase = item;
			    showDialog(ItemStore.CONFIRM_PURCHASE);
			}
			touched = false;
		    }
		    return true;
		}
	    };
	    mScene.registerTouchArea(buyButton);
	    buyButton.setColor(88 / 255f, 143 / 255f, 39 / 255f);
	    buyText.setPosition(25, buyButton.getHeight() / 2 - buyText.getHeight() / 2);
	    buyButton.attachChild(buyText);
	    // buyButton.setPosition(itemBox.getWidth() - buyButton.getWidth() - 5, 2);
	    itemBox.setWidth(cameraWidth - buyButton.getWidth() - 10);
	    buyButton.setPosition(itemBox.getWidth(), 0);
	    itemBox.attachChild(buyButton);
	    if (currentLayer.getChildCount() > 0)
		itemBox.setPosition(5, currentLayer.getLastChild().getY() + ((Rectangle) currentLayer.getLastChild()).getHeight() + 5);

	    currentLayer.attachChild(itemBox);

	    if (counter >= 10)
	    {
		currentLayer.setVisible(false);
		itemPages.add(currentLayer);
		bottomLayer.attachChild(currentLayer);
		currentLayer = new Entity();
		counter = 0;
	    }
	}

	if (counter > 0 && counter < 10)
	{
	    currentLayer.setVisible(false);
	    itemPages.add(currentLayer);
	    bottomLayer.attachChild(currentLayer);
	}

	itemPages.get(0).setVisible(true);
	currentPage = 0;

	mScene.registerUpdateHandler(new IUpdateHandler()
	{

	    @Override
	    public void reset()
	    {

	    }

	    @Override
	    public void onUpdate(float arg0)
	    {
		titleText.setText("Item Store  (" + (currentPage + 1) + "/" + itemPages.size() + ")");
	    }
	});
    }

    private void loadItemDescriptionBox()
    {
	/**
	 * Load item description box
	 */
	this.itemDescriptionRect = new Rectangle(10, cameraHeight / 2, cameraWidth - 20, Math.round(cameraHeight / 2 - (cameraHeight - pBottomBound) - 10))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (this.isVisible())
		{
		    Debug.d("Touched item description box");
		    return true;
		}
		else
		    return false;
	    }
	};
	this.itemDescriptionRect.setColor(0, 0, 0);
	this.itemDescriptionRect.setAlpha(.8f);
	this.itemDescriptionRect.setVisible(false);
	mScene.registerTouchArea(itemDescriptionRect);
	this.topLayer.attachChild(itemDescriptionRect);

	/**
	 * Add close button
	 */
	final Sprite closeButton = new Sprite(this.itemDescriptionRect.getWidth() - listTR.get("close.png").getWidth(), 0, listTR.get("close.png"))
	{
	    private boolean touched = false;

	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (!this.getParent().isVisible())
		    return false;

		if (pSceneTouchEvent.isActionDown())
		{
		    Debug.d("Touched close button");
		    hideItemDescription();
		}
		else if (pSceneTouchEvent.isActionMove())
		{

		}
		else if (pSceneTouchEvent.isActionUp())
		{

		}
		return true;
	    }
	};
	this.itemDescriptionRect.attachChild(closeButton);
	this.mScene.registerTouchArea(closeButton);

	/**
	 * Add text to item description box
	 */
	this.itemDesctiptionText = new ChangeableText(10, 10, mSmallFont, "", 512);
	this.itemDescriptionRect.attachChild(this.itemDesctiptionText);
    }

    /**
     * Generates the pop up that shows the item description
     * 
     * @param i
     *            - the item that was selected
     */
    private void showItemDescription(Item i)
    {
	this.itemDescriptionRect.setVisible(false);
	String normalizedText = TextUtil.getNormalizedText(mSmallFont, i.getInfoWithPrice(), this.itemDescriptionRect.getWidth() - 20);
	this.itemDesctiptionText.setText(normalizedText);
	this.itemDescriptionRect.setHeight(this.itemDesctiptionText.getHeight());
	this.itemDescriptionRect.setPosition(this.itemDescriptionRect.getX(), pBottomBound - this.itemDescriptionRect.getHeight() - 10);
	this.itemDescriptionRect.setVisible(true);
    }

    private void hideItemDescription()
    {
	this.itemDescriptionRect.setVisible(false);
    }

    private void loadBackpack()
    {
	Debug.d("Loading backpack...");
	ArrayList<Item> backpackItems = dbHelper.getBackpack(listTR);
	if (backpackItems != null)
	    this.backpack = new Backpack(backpackItems);
	else
	    this.backpack = new Backpack();
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	try
	{
	    switch (id)
	    {
	    case ItemStore.CONFIRM_PURCHASE:
		builder2.setTitle("Confirm Purchase");
		builder2.setIcon(android.R.drawable.btn_star);
		builder2.setMessage("Are you sure you want to buy " + itemToPurchase.getName());
		builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {
			if (money < itemToPurchase.getPrice())
			    toast("Not enough money!");
			else
			{
			    Debug.d("Purchased " + itemToPurchase.getName());
			    if (!backpack.addItem(itemToPurchase))
				toast("Backpack is full!");
			    else
			    {
				money -= itemToPurchase.getPrice();
				toast(itemToPurchase.getName() + " has been added to your backpack!");
			    }
			}
			return;
		    }
		});

		builder2.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {
			return;
		    }
		});

		return builder2.create();
	    }

	} catch (Exception e)
	{
	    e.printStackTrace();
	}

	return null;
    }

    /**
     * Used to update the alert dialogs with the correct item names
     */
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog)
    {
	switch (id)
	{
	case ItemStore.CONFIRM_PURCHASE:
	    ((AlertDialog) dialog).setMessage("Are you sure you want to buy " + itemToPurchase.getName() + "?");
	    return;
	default:
	    return;
	}
    }

    public void toast(final String pMessage)
    {
	this.runOnUiThread(new Runnable()
	{
	    @Override
	    public void run()
	    {
		Toast.makeText(ItemStore.this, pMessage, Toast.LENGTH_SHORT).show();
	    }
	});
    }

}
