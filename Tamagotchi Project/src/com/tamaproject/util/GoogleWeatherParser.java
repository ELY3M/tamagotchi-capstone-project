package com.tamaproject.util;

import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tamaproject.weather.*;

/**
 * This class parses the XML that is retrieved from the Google Weather API into a CurrentConditions object
 * @author Jonathan
 *
 */
public class GoogleWeatherParser
{
    private static Document dom;

    public static CurrentConditions getCurrentConditions(String file)
    {
	parseXMLFile(file);
	if (dom == null)
	    return null;
	else
	    return parseDocument();
    }

    private static void parseXMLFile(String file)
    {
	// System.out.println("Parsing XML to DOM");
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try
	{
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    dom = db.parse(file);

	} catch (ParserConfigurationException pce)
	{
	    pce.printStackTrace();
	} catch (SAXException se)
	{
	    se.printStackTrace();
	} catch (IOException ioe)
	{
	    ioe.printStackTrace();
	}
    }

    private static CurrentConditions parseDocument()
    {
	Element docEle = dom.getDocumentElement();
	CurrentConditions cc = new CurrentConditions();
	NodeList nl = docEle.getElementsByTagName("current_conditions");
	if (nl != null && nl.getLength() > 0)
	{
	    for (int i = 0; i < nl.getLength(); i++)
	    {
		Element el = (Element) nl.item(i);
		cc = getCC(el);
	    }
	}
	return cc;
    }

    private static CurrentConditions getCC(Element e)
    {
	CurrentConditions cc = new CurrentConditions();
	NodeList nl = e.getChildNodes();
	if (nl != null && nl.getLength() > 0)
	{
	    for (int i = 0; i < nl.getLength(); i++)
	    {
		Element el = (Element) nl.item(i);
		if (el.getTagName().equalsIgnoreCase("condition"))
		{
		    cc.setCondition(el.getAttribute("data"));
		}
		else if (el.getTagName().equalsIgnoreCase("temp_f"))
		{
		    cc.setTempF(Integer.parseInt(el.getAttribute("data")));
		}
		else if (el.getTagName().equalsIgnoreCase("temp_c"))
		{
		    cc.setTempC(Integer.parseInt(el.getAttribute("data")));
		}
		else if (el.getTagName().equalsIgnoreCase("humidity"))
		{
		    cc.setHumidity(el.getAttribute("data"));
		}
		else if (el.getTagName().equalsIgnoreCase("icon"))
		{
		    cc.setIcon(el.getAttribute("data"));
		}
		else if (el.getTagName().equalsIgnoreCase("wind_condition"))
		{
		    cc.setWindCondition(el.getAttribute("data"));
		}
	    }
	}
	return cc;
    }

}
