package com.tamaproject.entity;

import com.tamaproject.util.Weather;

/**
 * This class contains static values that pertain to the different types of protection against weather.
 * 
 * @author Jonathan
 * 
 */
public class Protection
{
    // All weather-related things are between 1-10
    public static final int NONE = Weather.NONE, SNOW = Weather.SNOW, RAIN = Weather.RAIN;

    public static String getString(int code)
    {
	switch (code)
	{
	case NONE:
	    return "None";
	case SNOW:
	    return "Snow";
	case RAIN:
	    return "Rain";
	default:
	    return "None";
	}
    }

    // Other protection will be 11-100
}
