package com.qwesdfok.ChatRoom.ClientDeploy;

import com.qwesdfok.ChatRoom.ClientDeploy.GUI.GUIControl;
import com.qwesdfok.ChatRoom.CommonModule.MessageWrapper;
import com.qwesdfok.ChatRoom.CommonModule.SocketWrapper;

import java.io.IOException;

public class ClientListener extends Thread
{
	private SocketWrapper wrapper;
	private GUIControl control;
	private boolean flag;

	public ClientListener(SocketWrapper serverHandel, GUIControl control)
	{
		this.wrapper = serverHandel;
		this.control = control;
	}

	@Override
	public void run()
	{
		flag = true;
		ClientControl.infoLog("Listener started.\n");
		while (flag)
		{
			try
			{
				String msg = wrapper.readNextItem();
				control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.ReceiveMSG, new MessageWrapper(msg)));
			} catch (IOException e)
			{
				control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.ListenerClose, 0));
				flag = false;
			}
		}
	}

	public void stopReceive()throws IOException
	{
		wrapper.close();
	}
}
