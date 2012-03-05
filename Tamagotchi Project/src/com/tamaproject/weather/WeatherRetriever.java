package com.tamaproject.weather;

import com.tamaproject.util.GoogleWeatherParser;

/**
 * Gets the current conditions from Google given a latitude and longitude
 * @author Jonathan
 *
 */
public class WeatherRetriever
{
    private static final String XML_SOURCE = "http://www.google.com/ig/api?weather=,,,";

    public static CurrentConditions getCurrentConditions(double latitude, double longitude)
    {
	latitude *= 1000000;
	longitude *= 1000000;
	int lat = (int) latitude;
	int lon = (int) longitude;
	
	String s = XML_SOURCE + lat + "," + lon;
	System.out.println(s);
	
	CurrentConditions cc = GoogleWeatherParser.getCurrentConditions(s);
	return cc;
    }
}
