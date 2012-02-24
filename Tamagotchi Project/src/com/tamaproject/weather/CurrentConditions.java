package com.tamaproject.weather;

public class CurrentConditions
{
    private String condition;
    private int tempF;
    private int tempC;
    private String humidity;
    private String icon;
    private String windCondition;
    
    public CurrentConditions()
    {
	
    }

    public CurrentConditions(String condition, int tempF, int tempC)
    {
	super();
	this.condition = condition;
	this.tempF = tempF;
	this.tempC = tempC;
    }

    public String getCondition()
    {
	return condition;
    }

    public int getTempF()
    {
	return tempF;
    }

    public int getTempC()
    {
	return tempC;
    }

    public String getHumidity()
    {
	return humidity;
    }

    public String getIcon()
    {
	return icon;
    }

    public String getWindCondition()
    {
	return windCondition;
    }

    public void setCondition(String condition)
    {
	this.condition = condition;
    }

    public void setTempF(int tempF)
    {
	this.tempF = tempF;
    }

    public void setTempC(int tempC)
    {
	this.tempC = tempC;
    }

    public void setHumidity(String humidity)
    {
	this.humidity = humidity;
    }

    public void setIcon(String icon)
    {
	this.icon = icon;
    }

    public void setWindCondition(String windCondition)
    {
	this.windCondition = windCondition;
    }

    @Override
    public String toString()
    {
	return "CurrentConditions [condition=" + condition + ", tempF=" + tempF + ", tempC=" + tempC + ", humidity=" + humidity + ", icon=" + icon + ", windCondition=" + windCondition + "]";
    }

}
