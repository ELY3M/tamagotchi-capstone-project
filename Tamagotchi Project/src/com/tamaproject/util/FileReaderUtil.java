package com.tamaproject.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * Utility for reading file
 * 
 * @author Jonathan
 * 
 */
public class FileReaderUtil
{

    public static String[] readFile(Context context, String filename)
    {
	try
	{

	    AssetManager am = context.getAssets();
	    InputStream is = am.open(filename);
	    String text = readFileToString(is);
	    String[] lines = text.split("\n");
	    return lines;

	} catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    private static String readFileToString(InputStream is)
    {
	ByteArrayOutputStream bo = new ByteArrayOutputStream();

	byte[] buffer = new byte[1024];

	try
	{
	    is.read(buffer);

	    bo.write(buffer);

	    bo.close();

	    is.close();
	}

	catch (IOException e)
	{

	}

	return bo.toString();
    }

}
