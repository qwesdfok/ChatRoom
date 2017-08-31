package com.qwesdfok.ChatRoom.ServerDeploy;

import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;
import com.qwesdfok.ChatRoom.CommonModule.MessageWrapper;
import com.qwesdfok.ChatRoom.CommonModule.SocketWrapper;
import com.qwesdfok.Libs.MyLib;
import com.qwesdfok.Libs.MySqlDB;

import java.net.ServerSocket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ServerControl
{
	public HashMap<Integer, ServerPoint> serverMap = new HashMap<>();
	private MySqlDB database = new MySqlDB();
	private ArrayList<ServerEvent> eventQueue = new ArrayList<>();
	private ArrayList<ServerEvent> eventBuffedQueue = new ArrayList<>();
	private boolean isEventScan = false;
	private String databaseUserName = "root";
	private String databasePassword = "qwesdfok";
	private String databaseIPAddress = "localhost";

	public void start()
	{
		try
		{
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					timeCircle();
				}
			};
			timer.schedule(timerTask, 33, 33);
			ServerSocket serverSocket = new ServerSocket(ConstanceValue.DEFAULT_LISTENER_PORT);
			ServerSocket serverSend = new ServerSocket(ConstanceValue.DEFAULT_SECOND_CONNECT_PORT);
			initMySqlDB();
			while (true)
			{
				infoLog("Waiting.");
				SocketWrapper wrapper = new SocketWrapper(serverSocket.accept());
				infoLog("A Client is connecting");
				ServerPoint point = new ServerPoint(wrapper, serverSend, createNewDB(), this);
				point.setName(Integer.toString(serverMap.size()));
				point.start();
			}// while block
		} catch (Exception e)/* catch any exceptions */
		{
			System.out.print(e.getMessage() + "\n");
		}
	}

	private void initMySqlDB()
	{
		database.setDatabaseName("chatroomdb");
		database.setHostName(databaseIPAddress);
		database.setUserName(databaseUserName);
		database.setPassword(databasePassword);
		try
		{
			database.connect();
		} catch (Exception e)
		{
			errorLog("MySql database connection error.\n");
			return;
		}
		HashMap<String, String> tableList = new HashMap<String, String>()
		{{
			put("usertable", "create table usertable(name varchar(64), id int, password char(32), status int, primary key(id))default charset='utf8'");
			put("chatroomtable", "create table chatroomtable(name varchar(64),id int, status int, primary key(id))default charset='utf8'");
			put("friendtable", "create table friendtable(id1 int, id2 int, primary key(id1,id2))default charset='utf8'");
			put("userinchatroomtable", "create table userinchatroomtable(userid int, chatroomid int,primary key(userid, chatroomid))default charset='utf8'");
			put("managertable", "create table managertable(userid int, chatroomid int, primary key(userid, chatroomid))default charset='utf8'");
		}};
		try
		{
			ResultSet rs = database.executeQuery("show tables");
			ArrayList<String> existedTable = new ArrayList<>();
			while (rs.next())
				existedTable.add(rs.getString(1));
			for (Map.Entry<String, String> entry : tableList.entrySet())
			{
				if (!existedTable.contains(entry.getKey()))
				{
					database.executeCommand(entry.getValue());
					if (entry.getKey().equals("usertable"))
						database.executeUpdate("insert into usertable values('root', 10000, '7BCF84BFE3DFA20327F5F790A6C03C23',0)");
					else if (entry.getKey().equals("chatroomtable"))
						database.executeUpdate("insert into chatroomtable values('root',10000,0)");
				}
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private MySqlDB createNewDB()
	{
		MySqlDB db = new MySqlDB();
		db.setDatabaseName("chatroomdb");
		db.setHostName(databaseIPAddress);
		db.setUserName(databaseUserName);
		db.setPassword(databasePassword);
		try
		{
			db.connect();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return db;
	}

	public void addEvent(ServerEvent event)
	{
		if (isEventScan)
			eventBuffedQueue.add(event);
		else
			eventQueue.add(event);
	}

	public void nextEvents()
	{
		isEventScan = false;
		eventQueue.clear();
		eventQueue.addAll(eventBuffedQueue);
		eventBuffedQueue.clear();
	}

	public void timeCircle()
	{
		if (eventQueue.size() == 0)
			return;
		isEventScan = true;
		try
		{
			for (ServerEvent event : eventQueue)
			{
				debugLog("Event: " + event.eventIndex.name() + " sourceIndex: " + event.sourceIndex);
				if (event.sourceIndex != 0 && serverMap.get(event.sourceIndex) == null)
				{
					warningLog("Null ServerPoint. Event Name: " + event.eventIndex.name() + ". SourceIndex: " + event.sourceIndex + ".\n");
				}
				if (event.eventIndex == ServerEvent.EventIndex.ThreadStarted)
				{
					infoLog("Client login. Current Size:" + serverMap.size() + "\n");
				} else if (event.eventIndex == ServerEvent.EventIndex.ThreadStop)
				{
					serverMap.remove(((ServerPoint) event.parameter).getCurrentUser().id);
					infoLog("Logout. Current Size:" + serverMap.size() + "\n");
				} else if (event.eventIndex == ServerEvent.EventIndex.ReceiveMSG)
				{
					receiveMSG(event);
				} else if (event.eventIndex == ServerEvent.EventIndex.BroadcastUserStatus)
				{
					ServerPoint sp = null;
					ResultSet rs = database.executeQuery("(select id2 as id from friendtable where id1=" + event.sourceIndex + " ) union (select id1 as id from friendtable where id2= " + event.sourceIndex + ")");
					while (rs.next())
					{
						sp = serverMap.get(rs.getInt(1));
						if (sp != null)
							sp.addSendMessage(new MessageWrapper(event.sourceIndex, (Integer) event.parameter, MessageWrapper.CONTROL, ConstanceValue.UPDATE_STATUS));
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			nextEvents();
		}
	}

	private void receiveMSG(ServerEvent event) throws SQLException
	{
		/*(UserID,Index,MessageWrapper(String))*/
		MessageWrapper msgW = (MessageWrapper) event.parameter;
		traceLog(msgW.data);
		String seprator = ConstanceValue.CONTROL_SEPARATOR;
		if (msgW.msgType == MessageWrapper.SINGLE)
		{
			ServerPoint targetSP = serverMap.get(msgW.receiver);
			if (targetSP != null)
				targetSP.addSendMessage(msgW);
			else
			{
				int senderID = msgW.sender;
				msgW.sender = msgW.receiver;
				msgW.data = "OFFLINE.";
				serverMap.get(senderID).addSendMessage(msgW);
			}
		} else if (msgW.msgType == MessageWrapper.MULTIPLY)
		{
			int roomId = msgW.receiver;
			ArrayList<Integer> userIdList = new ArrayList<>();
			ResultSet rs = database.executeQuery("select userid from userinchatroomtable where chatroomid=" + roomId);
			while (rs.next())
				userIdList.add(rs.getInt(1));
			for (Integer userId : userIdList)
			{
				if (serverMap.containsKey(userId))
				{
					ServerPoint sp = serverMap.get(userId);
					sp.addSendMessage(msgW);
				}
			}
		} else if (msgW.msgType == MessageWrapper.CONTROL)
		{
			/* 0:CommandName */
			ServerPoint sp = serverMap.get(event.sourceIndex);
			String[] rawData = msgW.data.split(seprator);
			String commandName = rawData[0];
			if (commandName.equals(ConstanceValue.READ_MEMBER_LIST))
			{
				/* 1:ChatRoomID*/
				int roomId = Integer.parseInt(rawData[1]);
				ResultSet rs = database.executeQuery("select usertable.name,usertable.id,usertable.status from usertable,userinchatroomtable where userinchatroomtable.userid=usertable.id and userinchatroomtable.chatroomid=" + roomId);
				StringBuilder sb = new StringBuilder();
				while (rs.next())
				{
					int userStatus = rs.getInt(3);
					if (serverMap.containsKey(rs.getInt(2)))
						userStatus |= ConstanceValue.STATUS_LOGIN;
					String stringData = seprator + rs.getString(1) + seprator + rs.getInt(2) + seprator + userStatus;
					sb.append(stringData);
				}
				msgW.data = ConstanceValue.READ_MEMBER_LIST + seprator + roomId + sb.toString();
				sp.addSendMessage(msgW);
			} else if (commandName.equals(ConstanceValue.MANAGE_RELATION))
			{
				int targetType = Integer.parseInt(rawData[1]);
				int actionType = Integer.parseInt(rawData[2]);
				String name = rawData[3];
				MessageWrapper sendMSG = new MessageWrapper(msgW.sender, msgW.receiver, msgW.msgType, null);
				boolean done = false;
				ResultSet rs;
				if (targetType == ConstanceValue.TARGET_TYPE_FRIEND)
				{
					rs = database.executeQuery("select id,status from usertable where name='" + name + "'");
					rs.last();
					int size = rs.getRow();
					rs.first();
					int targetID = 0;
					int targetStatus = 0;
					if (size != 0)
					{
						targetID = rs.getInt(1);
						targetStatus = rs.getInt(2);
						if (serverMap.containsKey(targetID))
							targetStatus |= ConstanceValue.STATUS_LOGIN;
						if (actionType == ConstanceValue.ACTION_TYPE_ADD)
						{
							try
							{
								database.executeUpdate("insert into friendtable values(" + msgW.sender + "," + targetID + ")");
								done = true;
							} catch (SQLException e)
							{
								done = false;
							}
						} else if (actionType == ConstanceValue.ACTION_TYPE_DELETE)
						{
							database.executeUpdate("delete from friendtable where ( id1=" + msgW.sender + " and id2=" + targetID + " ) or ( id1=" + targetID + " and id2=" + msgW.sender + ")");
							done = true;
						}
					}
					if (done)
					{
						sendMSG.data = ConstanceValue.MANAGE_RELATION + seprator + targetType + seprator + actionType + seprator + Boolean.toString(true) + seprator + name + seprator + targetID + seprator + targetStatus;
						sp.addSendMessage(sendMSG);
						MessageWrapper notifyMSG = sendMSG.clone();
						notifyMSG.data = ConstanceValue.MANAGE_RELATION + seprator + targetType + seprator + actionType + seprator + Boolean.toString(true) + seprator + sp.getCurrentUser().name + seprator + sp.getCurrentUser().id + seprator + ConstanceValue.STATUS_LOGIN;
						if (serverMap.containsKey(targetID))
							serverMap.get(targetID).addSendMessage(notifyMSG);
					}
				} else if (targetType == ConstanceValue.TARGET_TYPE_CHATROOM)
				{
					ArrayList<Integer> userInChatRoomIDs = new ArrayList<>();
					rs = database.executeQuery("select userid from userinchatroomtable,chatroomtable where chatroomid=id and name='" + name + "'");
					while (rs.next())
						userInChatRoomIDs.add(rs.getInt(1));
					rs = database.executeQuery("select id,status from chatroomtable where name='" + name + "'");
					rs.last();
					int size = rs.getRow();
					rs.first();
					int targetID = 0;
					int targetStatus = 0;
					if (size != 0)
					{
						targetID = rs.getInt(1);
						targetStatus = rs.getInt(2);
						if (actionType == ConstanceValue.ACTION_TYPE_ADD)
						{
							try
							{
								database.executeUpdate("insert into userinchatroomtable values(" + msgW.sender + "," + targetID + ")");
								done = true;
							} catch (SQLException e)
							{
								done = false;
							}
						} else if (actionType == ConstanceValue.ACTION_TYPE_DELETE)
						{
							database.executeUpdate("delete from userinchatroomtable where userid=" + msgW.sender + " and chatroomid=" + targetID);
							done = true;
						}
					} else
					{
						if (actionType == ConstanceValue.ACTION_TYPE_CREATE)
						{
							rs = database.executeQuery("select MAX(id) from chatroomtable");
							rs.next();
							int newChatRoomID = rs.getInt(1) + 1;
							database.executeUpdate("insert into chatroomtable values('" + name + "'," + newChatRoomID + ",0)");
							database.executeUpdate("insert into userinchatroomtable values(" + msgW.sender + "," + newChatRoomID + ")");
							done = true;
							targetID = newChatRoomID;
						}
					}
					if (done)
					{
						sendMSG.data = ConstanceValue.MANAGE_RELATION + seprator + targetType + seprator + actionType + seprator + Boolean.toString(true) + seprator + name + seprator + targetID;
						sp.addSendMessage(sendMSG);
						for (Map.Entry<Integer, ServerPoint> entry : serverMap.entrySet())
						{
							if (userInChatRoomIDs.contains(entry.getKey()) && entry.getKey() != msgW.sender)
								entry.getValue().addSendMessage(new MessageWrapper(msgW.sender, targetID, MessageWrapper.CONTROL, ConstanceValue.UPDATE_CHATROOM_MEMBER + seprator + actionType));
						}
					}
				}
				if (!done)
				{
					sendMSG.data = ConstanceValue.MANAGE_RELATION + seprator + targetType + seprator + actionType + seprator + Boolean.toString(false);
					sp.addSendMessage(sendMSG);
				}
			} else if (commandName.equals(ConstanceValue.IDLE_MESSAGE))
			{
				sp.addSendMessage(msgW);
			}
		}
	}

	public static void traceLog(String trace)
	{
		MyLib.traceLog(trace);
	}

	public static void debugLog(String debug)
	{
		MyLib.debugLog(debug);
	}

	public static void infoLog(String info)
	{
		MyLib.infoLog(info);
	}

	public static void errorLog(String error)
	{
		MyLib.errorLog(error);
	}

	public static void warningLog(String warning)
	{
		MyLib.warningLog(warning);
	}

	public void setDatabaseUserName(String databaseUserName)
	{
		this.databaseUserName = databaseUserName;
	}

	public void setDatabasePassword(String databasePassword)
	{
		this.databasePassword = databasePassword;
	}

	public void setDatabaseIPAddress(String databaseIPAddress)
	{
		this.databaseIPAddress = databaseIPAddress;
	}

	public String getDatabaseUserName()
	{
		return databaseUserName;
	}

	public String getDatabasePassword()
	{
		return databasePassword;
	}

	public String getDatabaseIPAddress()
	{
		return databaseIPAddress;
	}
}
