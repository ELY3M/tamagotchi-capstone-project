package com.tamaproject.andengine.entity;

import com.tamaproject.util.Weather;

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
