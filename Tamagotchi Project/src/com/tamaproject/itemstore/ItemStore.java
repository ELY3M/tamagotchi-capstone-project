package com.tamaproject.itemstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
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

import android.graphics.Color;

import com.tamaproject.BaseAndEngineGame;
import com.tamaproject.database.DatabaseHelper;
import com.tamaproject.entity.Item;
import com.tamaproject.util.TextUtil;
import com.tamaproject.util.TextureUtil;

public class ItemStore extends BaseAndEngineGame
{

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

    @Override
    public void onLoadComplete()
    {
	// TODO Auto-generated method stub

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

	this.mScene = new Scene();
	this.topLayer = new Entity();
	this.bottomLayer = new Entity();
	this.mScene.attachChild(bottomLayer);
	this.mScene.attachChild(topLayer);
	
	this.loadItemDescriptionBox();


	ArrayList<Item> allItems = dbHelper.getAllItems(listTR);
	LinkedList<Rectangle> itemBoxes = new LinkedList<Rectangle>();
	for (final Item item : allItems)
	{
	    final Rectangle itemBox = new Rectangle(5, 0, cameraWidth - 10, item.getHeight() + 4)
	    {
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
		    if (pSceneTouchEvent.isActionDown())
		    {
			showItemDescription(item);
			return true;
		    }
		    return false;
		}
	    };
	    mScene.registerTouchArea(itemBox);
	    item.setPosition(2, 2);
	    itemBox.attachChild(item);
	    itemBox.setColor(1, 0, 0);

	    final Text itemText = new Text(0, 0, mSmallFont, item.getName() + ": $" + item.getPrice());
	    itemText.setPosition(item.getWidth() + 5, item.getHeight() / 2 - itemText.getHeight() / 2);
	    item.attachChild(itemText);

	    final Text buyText = new Text(0, 0, mSmallFont, "Buy");
	    final Rectangle buyButton = new Rectangle(0, 0, buyText.getWidth() + 20, itemBox.getHeight() - 4)
	    {
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
		    if (pSceneTouchEvent.isActionDown())
		    {
			return true;
		    }
		    return false;
		}
	    };
	    mScene.registerTouchArea(buyButton);
	    buyButton.setColor(0, 1, 0);
	    buyText.setPosition(10, buyButton.getHeight() / 2 - buyText.getHeight() / 2);
	    buyButton.attachChild(buyText);
	    buyButton.setPosition(itemBox.getWidth() - buyButton.getWidth() - 5, 2);
	    itemBox.attachChild(buyButton);
	    if (!itemBoxes.isEmpty())
		itemBox.setPosition(5, itemBoxes.getLast().getY() + itemBoxes.getLast().getHeight() + 5);
	    itemBoxes.add(itemBox);
	    bottomLayer.attachChild(itemBox);
	}
	mScene.setTouchAreaBindingEnabled(true);
	return mScene;
    }

    @Override
    public void pauseSound()
    {
	// TODO Auto-generated method stub

    }

    @Override
    public void resumeSound()
    {
	// TODO Auto-generated method stub

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
		    return true;
		else
		    return false;
	    }
	};
	this.itemDescriptionRect.setColor(0, 0, 0);
	this.itemDescriptionRect.setAlpha(.8f);
	this.itemDescriptionRect.setVisible(false);
	this.topLayer.attachChild(itemDescriptionRect);
	
	/**
	 * Add close button
	 */
	final Sprite closeButton = new Sprite(this.itemDescriptionRect.getWidth() - listTR.get("close.png").getWidth(), 0, listTR.get("close.png"))
	{
	    @Override
	    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
		    final float pTouchAreaLocalX, final float pTouchAreaLocalY)
	    {
		if (this.getParent().isVisible())
		{
		    hideItemDescription();
		    return true;
		}
		else
		    return false;
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

}
