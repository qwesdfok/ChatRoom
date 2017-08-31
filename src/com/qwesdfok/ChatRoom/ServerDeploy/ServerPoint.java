package com.qwesdfok.ChatRoom.ServerDeploy;

import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;
import com.qwesdfok.ChatRoom.CommonModule.MessageWrapper;
import com.qwesdfok.ChatRoom.CommonModule.SocketWrapper;
import com.qwesdfok.ChatRoom.CommonModule.User;
import com.qwesdfok.Libs.MySqlDB;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

class ServerPoint extends Thread
{
	private SocketWrapper wrapper;
	private MySqlDB database;
	private ServerControl control;
	private User currentUser = new User();
	private ServerListener serverListener;
	private ServerSocket serverSend;
	private ArrayList<MessageWrapper> sendQueue = new ArrayList<>();
	private ArrayList<MessageWrapper> sendBufferQueue = new ArrayList<>();
	private boolean isSending = false;

	public ServerPoint(SocketWrapper wrapper, ServerSocket serverSend, MySqlDB database, ServerControl control)
	{
		this.database = database;
		this.wrapper = wrapper;
		this.control = control;
		this.serverSend = serverSend;
	}

	private void writeFriendData() throws IOException, SQLException
	{
		ResultSet rs = database.executeQuery("select usertable.name,usertable.id,usertable.status from usertable,friendtable where (friendtable.id1=usertable.id and friendtable.id2=" + currentUser.id + ") or (friendtable.id2=usertable.id and friendtable.id1=" + currentUser.id + ")");
		rs.last();
		int size = rs.getRow();
		rs.first();
		wrapper.writeString(Integer.toString(size));
		if (size != 0)
		{
			do
			{
				wrapper.writeString(rs.getString(1));
				wrapper.writeString(Integer.toString(rs.getInt(2)));
				int us = rs.getInt(3);
				if (control.serverMap.containsKey(rs.getInt(2)))
					us |= ConstanceValue.STATUS_LOGIN;
				wrapper.writeString(Integer.toString(us));
			} while (rs.next());
		}
	}

	private void writeChatroomData() throws IOException, SQLException
	{
		ResultSet rs = database.executeQuery("select chatroomtable.name,chatroomtable.id from userinchatroomtable,chatroomtable where userinchatroomtable.userid=" + currentUser.id + " and chatroomtable.id=userinchatroomtable.chatroomid");
		rs.last();
		int size = rs.getRow();
		rs.first();
		wrapper.writeString(Integer.toString(size));
		if (size != 0)
		{
			do
			{
				wrapper.writeString(rs.getString(1));
				wrapper.writeString(Integer.toString(rs.getInt(2)));
			} while (rs.next());
		}
	}

	@Override
	public void run()
	{
		try
		{
			int length = 0;
			StringBuffer sb = new StringBuffer();
			currentUser.name = wrapper.readNextItem("UTF-8");
			currentUser.password = wrapper.readNextItem("UTF-8");
			ResultSet rs = database.executeQuery("select id,password,status from usertable where name='" + currentUser.name + "'");
			rs.last();
			int size = rs.getRow();
			rs.first();
			if (size == 0)
			{
				rs = database.executeQuery("select MAX(id) from usertable");
				rs.next();
				currentUser.id = rs.getInt(1) + 1;
				currentUser.status = 0;
				database.executeUpdate("insert into usertable values('" + currentUser.name + "'," + currentUser.id + ",'" + currentUser.password + "'," + currentUser.status + ")");
			} else
			{
				currentUser.id = rs.getInt(1);
				String collatePassword = rs.getString(2);
				currentUser.status = rs.getInt(3);
				if (collatePassword.compareTo(currentUser.password) != 0)
				{
					wrapper.writeString(ConstanceValue.WRONG_PASSWORD);
					return;
				}
			}
			wrapper.writeString(ConstanceValue.USER_LOGIN_OK);
			wrapper.writeString(Integer.toString(currentUser.id));
			wrapper.writeString(Integer.toString(currentUser.status));
			writeFriendData();
			writeChatroomData();

			wrapper.writeString(Integer.toString(ConstanceValue.DEFAULT_SECOND_CONNECT_PORT));
			//Create Server->Client socket
			//And prior socket is used to be a listener
			SocketWrapper send = new SocketWrapper(serverSend.accept());
			control.serverMap.put(currentUser.id, this);
			serverListener = new ServerListener(currentUser.id, control, wrapper);
			serverListener.start();
			control.addEvent(new ServerEvent(currentUser.id, ServerEvent.EventIndex.ThreadStarted, this));
			control.addEvent(new ServerEvent(currentUser.id, ServerEvent.EventIndex.BroadcastUserStatus, ConstanceValue.STATUS_LOGIN));
			while (true)
			{
				while (sendQueue.size() == 0)
					if (serverListener.isValid())
						yield();
					else
						return;
				isSending = true;
				for (MessageWrapper msg : sendQueue)
				{
					send.writeWrapper(msg);
				}
				sendQueue.clear();
				sendQueue.addAll(sendBufferQueue);
				sendBufferQueue.clear();
				isSending = false;
			}
		} catch (SQLException e)
		{
			System.out.print("Connection error.\n");
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				wrapper.close();
				serverListener.stopReceive();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			control.addEvent(new ServerEvent(currentUser.id, ServerEvent.EventIndex.ThreadStop, this));
			control.addEvent(new ServerEvent(currentUser.id, ServerEvent.EventIndex.BroadcastUserStatus, ConstanceValue.STATUS_LOGOUT));
		}
	}

	public void addSendMessage(MessageWrapper msg)
	{
		if (isSending)
			sendBufferQueue.add(msg);
		else
			sendQueue.add(msg);
	}

	public User getCurrentUser()
	{
		return currentUser;
	}
}