package com.tamaproject.util;

import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.anddev.andengine.opengl.font.Font;

public class TextUtil
{
    public static String getNormalizedText(Font font, String ptext, float textWidth)
    {
	// no need to normalize, its just one word, so return
	if (!ptext.contains(" "))
	    return ptext;
	String[] lines = ptext.split("\n");
	StringBuilder normalizedText = new StringBuilder();
	StringBuilder line = new StringBuilder();

	for (int j = 0; j < lines.length; j++)
	{
	    if (lines[j].contains(" "))
	    {
		String[] words = lines[j].split(" ");
		line = new StringBuilder();
		for (int i = 0; i < words.length; i++)
		{
		    if (font.getStringWidth((line + words[i])) > (textWidth))
		    {
			normalizedText.append(line).append('\n');
			line = new StringBuilder();
		    }

		    if (line.length() == 0)
			line.append(words[i]);
		    else
			line.append(' ').append(words[i]);

		    if (i == words.length - 1)
			normalizedText.append(line);
		}
	    }
	    else
	    {
		normalizedText.append(lines[j]);
	    }

	    normalizedText.append('\n');
	}

	return normalizedText.toString();
    }

    public static String getIpAndPort(final ClientConnector<SocketConnection> pConnector)
    {
	return pConnector.getConnection().getSocket().getInetAddress().getHostAddress() + ":" + pConnector.getConnection().getSocket().getPort();
    }
}
