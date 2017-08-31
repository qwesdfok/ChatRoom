package com.qwesdfok.ChatRoom.ServerDeploy;

import com.qwesdfok.ChatRoom.CommonModule.MessageWrapper;
import com.qwesdfok.ChatRoom.CommonModule.SocketWrapper;

import java.io.IOException;

public class ServerListener extends Thread
{
	private int userID;
	private ServerControl control;
	private SocketWrapper wrapper;
	private int status=0;

	public ServerListener(int userID, ServerControl control, SocketWrapper wrapper)
	{
		this.userID = userID;
		this.control = control;
		this.wrapper = wrapper;
	}

	@Override
	public void run()
	{
		try
		{
			status=1;
			while (true)
			{
				String msg = wrapper.readNextItem();
				control.addEvent(new ServerEvent(userID, ServerEvent.EventIndex.ReceiveMSG, new MessageWrapper(msg)));
			}
		} catch (IOException e)
		{
			ServerControl.infoLog("Stop Listener:" + userID + " for: " + e.getMessage() + "\n");
		}finally
		{
			status=-1;
		}
	}
	public void stopReceive()throws IOException
	{
		wrapper.close();
	}

	public SocketWrapper getWrapper()
	{
		return wrapper;
	}
	public boolean isValid()
	{
		return status!=-1;
	}
}
