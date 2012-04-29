package com.tamaproject.adt.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class ConnectionCloseServerMessage extends ServerMessage implements ServerMessageFlags
{
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    public ConnectionCloseServerMessage()
    {

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public short getFlag()
    {
	return FLAG_MESSAGE_SERVER_CONNECTION_CLOSE;
    }

    @Override
    protected void onReadTransmissionData(final DataInputStream pDataInputStream)
	    throws IOException
    {
	/* Nothing to read. */
    }

    @Override
    protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream)
	    throws IOException
    {
	/* Nothing to write. */
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
