package com.tamaproject.multiplayer;

import java.io.IOException;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener.DefaultSocketServerListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.SocketConnection;

import com.tamaproject.util.TamaBattleConstants;

public class BattleServer extends SocketServer<SocketConnectionClientConnector> implements IUpdateHandler, TamaBattleConstants
{
    public BattleServer(final ISocketConnectionClientConnectorListener pSocketConnectionClientConnectorListener) {
        super(SERVER_PORT, pSocketConnectionClientConnectorListener, new DefaultSocketServerListener<SocketConnectionClientConnector>());
    }

    @Override
    public void onUpdate(float arg0)
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void reset()
    {
	// TODO Auto-generated method stub
	
    }

    @Override
    protected SocketConnectionClientConnector newClientConnector(SocketConnection pSocketConnection)
	    throws IOException
    {
	final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);
	
	return clientConnector;
    }

}
