package com.tamaproject.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.MathUtils;

import android.content.Context;
import android.graphics.BitmapFactory;

/**
 * Used for loading textures
 * 
 * @author Jonathan
 * 
 */
public class TextureUtil
{
    /**
     * Loads all bitmaps into TextureRegions from the gfx folder into hashtable with file name as the key
     * 
     * @param context
     * @param pEngine
     */
    public static Hashtable<String, TextureRegion> loadTextures(Context context, Engine pEngine,
	    String[] folderNameArray)
    {
	Hashtable<String, TextureRegion> listTR = new Hashtable<String, TextureRegion>();
	String[] fileNames;
	List<BitmapTextureAtlas> texturelist = new ArrayList<BitmapTextureAtlas>();
	BitmapFactory.Options opt = new BitmapFactory.Options();
	opt.inJustDecodeBounds = true;

	for (int i = 0; i < folderNameArray.length; i++)
	{
	    BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(folderNameArray[i]);
	    try
	    {
		fileNames = context.getResources().getAssets().list(folderNameArray[i].substring(0, folderNameArray[i].lastIndexOf("/")));
		Arrays.sort(fileNames);
		for (int j = 0; j < fileNames.length; j++)
		{

		    String rscPath = folderNameArray[i].concat(fileNames[j]);
		    InputStream in = context.getResources().getAssets().open(rscPath);
		    BitmapFactory.decodeStream(in, null, opt);

		    int width = opt.outWidth;
		    int height = opt.outHeight;

		    boolean flag = MathUtils.isPowerOfTwo(width);

		    if (!flag)
		    {
			width = MathUtils.nextPowerOfTwo(opt.outWidth);
		    }
		    flag = MathUtils.isPowerOfTwo(height);
		    if (!flag)
		    {
			height = MathUtils.nextPowerOfTwo(opt.outHeight);
		    }
		    texturelist.add(new BitmapTextureAtlas(width, height, TextureOptions.BILINEAR_PREMULTIPLYALPHA));

		    listTR.put(fileNames[j], BitmapTextureAtlasTextureRegionFactory.createFromAsset(texturelist.get(j), context, fileNames[j], 0, 0));
		    pEngine.getTextureManager().loadTexture(texturelist.get(j));
		}
	    } catch (IOException e)
	    {
		e.printStackTrace();
		return null;
	    }
	}
	context = null;
	return listTR;
    }
}
