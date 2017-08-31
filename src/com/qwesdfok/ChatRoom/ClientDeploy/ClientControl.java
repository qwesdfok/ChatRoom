package com.qwesdfok.ChatRoom.ClientDeploy;

import com.qwesdfok.ChatRoom.ClientDeploy.GUI.GUIControl;
import com.qwesdfok.ChatRoom.CommonModule.*;
import com.qwesdfok.Libs.MyLib;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientControl
{
	enum Status
	{
		NOT_INIT, STARTING
	}

	private SocketWrapper wrapper;
	private GUIControl control = new GUIControl();
	private ErrorCode errorCode = ErrorCode.NONE;
	private ArrayList<User> friendList = new ArrayList<>();
	private ArrayList<ChatRoom> chatroomList = new ArrayList<>();
	private User currentUser = new User();
	private ClientListener clientListener;
	private int loginPage = 0;
	private int mainPage = 0;
	private int registerPage = 0;
	private ArrayList<Integer> singlePage = new ArrayList<>();
	private ArrayList<Integer> multiplyPage = new ArrayList<>();
	private ArrayList<Integer> managePage = new ArrayList<>();
	private int idleTime = 0;
	private Status status = Status.NOT_INIT;
	private Thread timer;

	private enum ErrorCode
	{
		NONE, WRONG_PASSWORD, WRONG_USER_INFO, HOST_NOT_FIND, NO_TARGET_OR_EXISTED, SYSTEM_ERROR, IDLE_MESSAGE_NOT_REPOSE,
		TIME_CIRCLE_ERROR, LOGIN_ERROR, REGISTER_ERROR
	}

	private void initEnv()
	{
		timer = new Thread("Timer")
		{
			@Override
			public void run()
			{
				boolean flag = true;
				while (flag)
				{
					try
					{
						timeCircle();
						Thread.sleep(ConstanceValue.period);
					} catch (Exception e)
					{
						e.printStackTrace();
						errorCode = ErrorCode.SYSTEM_ERROR;
						control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.Error));
						flag = false;
					}
				}
			}
		};
		timer.start();
	}

	public void start()
	{
		initEnv();
		loginPage = control.createWindow(GUIControl.LOGIN_PAGE);
		control.setVisible(loginPage, true);
	}

	public void close()
	{
		control.closeWindow(GUIControl.ALL_PAGE);
		try
		{
			if (wrapper != null)
				wrapper.close();
			if (clientListener != null)
				clientListener.stopReceive();
		} catch (IOException e)
		{
			//ignore
		}
	}

	private void timeCircle()
	{
		try
		{
			if(ConstanceValue.ENABLE_IDLE_DETECT)
			{
				if (status == Status.STARTING)
					idleTime += ConstanceValue.period;
				else
					idleTime = 0;
				if (idleTime >= ConstanceValue.warningPeriod && (idleTime - ConstanceValue.warningPeriod) % ConstanceValue.sendWarningPeriod == 0)
					control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.IdleTimeOut, 0));
				if (idleTime >= ConstanceValue.errorPeriod)
				{
					errorCode = ErrorCode.IDLE_MESSAGE_NOT_REPOSE;
					control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.Error, true));
				}
			}
			if (errorCode != ErrorCode.NONE && control.getEventQueue().size() == 0)
				control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.Error));
			if (control.getEventQueue().size() == 0)
				return;
			//GUI Control timeCircle
			control.timeCircle();
			//Event Control timeCircle
			for (ClientEvent event : control.getEventQueue())
			{
				debugLog("ClientControl:Event: " + event.eventIndex.name() + " Index: " + event.sourceIndex + "\n");
				if (event.eventIndex == ClientEvent.EventIndex.Login)
				{
					login();
				} else if (event.eventIndex == ClientEvent.EventIndex.OpenRegister)
				{
					int rPage = control.createWindow(GUIControl.REGISTER_PAGE);
					control.setVisible(rPage, true);
					registerPage = rPage;
				} else if (event.eventIndex == ClientEvent.EventIndex.Register)
				{
					register();
				} else if (event.eventIndex == ClientEvent.EventIndex.Exit)
				{
					control.unRegisterWindow(event.sourceIndex);
					if (singlePage.contains(event.sourceIndex))
						singlePage.remove(new Integer(event.sourceIndex));
					if (multiplyPage.contains(event.sourceIndex))
						multiplyPage.remove(new Integer(event.sourceIndex));
					if (control.pageMap.size() == 0)
					{
						close();
						System.exit(0);
					}
				} else if (event.eventIndex == ClientEvent.EventIndex.OpenSinglePage)
				{
					int spage = control.createWindow(GUIControl.SINGLE);
					control.setVisible(spage, true);
					control.configSingleTarget(spage, (Integer) event.parameter);
					User user = friendList.get(friendList.indexOf(new User(null, (Integer) event.parameter, 0)));
					control.configPageName(spage, user.name + "(" + currentUser.name + ")");
					singlePage.add(spage);
				} else if (event.eventIndex == ClientEvent.EventIndex.OpenMultiplyPage)
				{
					int mpage = control.createWindow(GUIControl.MULTIPLY);
					control.setVisible(mpage, true);
					control.configMultiplyID(mpage, (Integer) event.parameter);
					ChatRoom room = chatroomList.get(chatroomList.indexOf(new ChatRoom(null, (Integer) event.parameter)));
					control.configPageName(mpage, room.name + "(" + currentUser.name + ")");
					control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.SendControlMSG, ConstanceValue.READ_MEMBER_LIST + ConstanceValue.CONTROL_SEPARATOR + ((Integer) event.parameter)));
					multiplyPage.add(mpage);
				} else if (event.eventIndex == ClientEvent.EventIndex.OpenManagePanel)
				{
					int mpage = control.createWindow(GUIControl.MANAGE_PAGE);
					control.setVisible(mpage, true);
					managePage.add(mpage);
					control.configManageTargetType(mpage, (Integer) event.parameter);
				} else if (event.eventIndex == ClientEvent.EventIndex.SendSingleMSG)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, control.fetchSingleTarget(event.sourceIndex), MessageWrapper.SINGLE, currentUser.name + ":\n" + event.parameter);
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				} else if (event.eventIndex == ClientEvent.EventIndex.SendMultiplyMSG)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, control.fetchMultiplyRoomID(event.sourceIndex), 
							MessageWrapper.MULTIPLY, currentUser.name + ":\n" + event.parameter);
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				} else if (event.eventIndex == ClientEvent.EventIndex.SendControlMSG)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, event.sourceIndex, MessageWrapper.CONTROL, (String) event.parameter);
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				} else if(event.eventIndex == ClientEvent.EventIndex.SendSingleImg)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, control.fetchSingleTarget(event.sourceIndex), MessageWrapper.SINGLE,
							"1" + ConstanceValue.CONTROL_SEPARATOR + currentUser.name + ":\n" + event.parameter,"ISO-8859-1");
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				}else if(event.eventIndex == ClientEvent.EventIndex.SendSingleFile)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, control.fetchSingleTarget(event.sourceIndex), MessageWrapper.SINGLE,
							"2" + ConstanceValue.CONTROL_SEPARATOR + currentUser.name + ":\n" + event.parameter,"ISO-8859-1");
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				}
				else if(event.eventIndex == ClientEvent.EventIndex.SendMultiplyImg)
				{
					MessageWrapper msgW = new MessageWrapper(currentUser.id, control.fetchMultiplyRoomID(event.sourceIndex), MessageWrapper.MULTIPLY, 
							"1" + ConstanceValue.CONTROL_SEPARATOR + currentUser.name + ":\n" + event.parameter,"ISO-8859-1");
					wrapper.writeWrapper(msgW);
					traceLog("SMSG: " + msgW.fetchSendByte() + "\n");
				}
				else if (event.eventIndex == ClientEvent.EventIndex.ReceiveMSG)
				{
					idleTime = 0;
					traceLog("RMSG: " + ((MessageWrapper) event.parameter).data);
					receiveMSG(event);
				} else if (event.eventIndex == ClientEvent.EventIndex.ManageRelation)
				{
					int targetType = event.sourceIndex;
					String rawData = ((String) event.parameter);//rawData= actionType+sepa+targetName
					String data = ConstanceValue.MANAGE_RELATION + ConstanceValue.CONTROL_SEPARATOR + targetType + ConstanceValue.CONTROL_SEPARATOR + rawData;
					control.addEvent(new ClientEvent(currentUser.id, ClientEvent.EventIndex.SendControlMSG, data));
				} else if (event.eventIndex == ClientEvent.EventIndex.ListenerClose)
				{
					infoLog("Listener Closed.\n");
					backToLoginPage();
				} else if (event.eventIndex == ClientEvent.EventIndex.Error)
				{
					if (mainPage == 0)
					{
						errorCode = ErrorCode.NONE;
						continue;
					}
					if (event.sourceIndex == 0)
						event.sourceIndex = mainPage;
					control.showErrorMSG(event.sourceIndex, errorCode.name());
					errorLog(errorCode.name() + "\n");
					errorCode = ErrorCode.NONE;
					if (event.parameter != null && (Boolean) event.parameter)
						backToLoginPage();
				} else if (event.eventIndex == ClientEvent.EventIndex.IdleTimeOut)
				{
					control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.SendControlMSG, ConstanceValue.IDLE_MESSAGE));
				}
				debugLog("Event Deal End.\n");
			}//Iterator end
		} catch (IOException e)
		{
			errorCode = ErrorCode.TIME_CIRCLE_ERROR;
			control.addEvent(new ClientEvent(0, ClientEvent.EventIndex.Error, true));
		} finally
		{
			control.nextEvents();
		}
	}

	private void login()
	{
		try
		{
			wrapper = new SocketWrapper(new Socket(control.address, Integer.parseInt(control.port)));
		} catch (IOException e)
		{
			errorCode = ErrorCode.HOST_NOT_FIND;
			return;
		}
		try
		{
			wrapper.writeString(control.name);
			wrapper.writeString(control.password);
			String repose = wrapper.readNextItem("UTF-8");
			if (repose.equals(ConstanceValue.WRONG_PASSWORD))
			{
				errorCode = ErrorCode.WRONG_PASSWORD;
				return;
			} else if (repose.equals(ConstanceValue.WRONG_USER_INFO))
			{
				errorCode = ErrorCode.WRONG_USER_INFO;
				return;
			}
			currentUser.name = control.name;
			currentUser.password = control.password;
			currentUser.id = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			currentUser.status = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			control.closeWindow(loginPage);
			showMainPage();
			infoLog("Login.\n");
		} catch (IOException e)
		{
			errorCode = ErrorCode.LOGIN_ERROR;
			return;
		}
	}

	private void showMainPage() throws IOException
	{

		if (mainPage == 0)
		{
			loginPage = 0;
			mainPage = control.createWindow(GUIControl.MAIN_PAGE);
			control.setVisible(mainPage, true);
			readFriendData();
			readChatroomData();
			control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.RenderMainPageFriendPanel, friendList));
			control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.RenderMainPageMultiplyPanel, chatroomList));
			int port = Integer.parseInt(wrapper.readNextItem());
			SocketWrapper serverHandel = new SocketWrapper(new Socket(control.address, port));
			clientListener = new ClientListener(serverHandel, control);
			clientListener.start();
			status = Status.STARTING;
		}

	}

	private void readFriendData() throws IOException
	{
		int friendSize = Integer.parseInt(wrapper.readNextItem("UTF-8"));
		friendList.clear();
		for (int i = 0; i < friendSize; i++)
		{
			String fname = wrapper.readNextItem("UTF-8");
			int fid = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			int fstatus = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			friendList.add(new User(fname, fid, fstatus));
		}
	}

	private void readChatroomData() throws IOException
	{
		int chatroomSize = Integer.parseInt(wrapper.readNextItem("UTF-8"));
		chatroomList.clear();
		for (int i = 0; i < chatroomSize; i++)
		{
			String rname = wrapper.readNextItem("UTF-8");
			int rid = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			chatroomList.add(new ChatRoom(rname, rid));
		}
	}

	private void register()
	{
		try
		{
			while (control.password == null)
			{
				control.fetchRegisterPassword(registerPage);
			}
			try
			{
				wrapper = new SocketWrapper(new Socket(control.address, Integer.parseInt(control.port)));
			} catch (IOException e)
			{
				errorCode = ErrorCode.HOST_NOT_FIND;
				return;
			}
			wrapper.writeString(control.name);
			wrapper.writeString(control.password);
			String repose = wrapper.readNextItem("UTF-8");
			if (repose.equals(ConstanceValue.WRONG_PASSWORD))
			{
				errorCode = ErrorCode.WRONG_PASSWORD;
				return;
			} else if (repose.equals(ConstanceValue.WRONG_USER_INFO))
			{
				errorCode = ErrorCode.WRONG_USER_INFO;
				return;
			}
			currentUser.name = control.name;
			currentUser.password = control.password;
			currentUser.id = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			currentUser.status = Integer.parseInt(wrapper.readNextItem("UTF-8"));
			control.closeWindow(registerPage);
			showMainPage();
			infoLog("Registered.\n");
		} catch (IOException e)
		{
			errorCode = ErrorCode.REGISTER_ERROR;
			return;
		}
	}

	private void backToLoginPage()
	{
		try
		{
			wrapper.close();
			clientListener.stopReceive();
		} catch (IOException e)
		{
		}
		status = Status.NOT_INIT;
		control.closeWindow(GUIControl.ALL_PAGE);
		singlePage.clear();
		multiplyPage.clear();
		mainPage = 0;
		loginPage = control.createWindow(GUIControl.LOGIN_PAGE);
		control.setVisible(loginPage, true);
	}

	private void receiveMSG(ClientEvent event)
	{
		/* (0,index,MessageWrapper) */
		MessageWrapper msgW = (MessageWrapper) event.parameter;
		if (msgW.msgType == MessageWrapper.SINGLE)
		{
			for (Integer s : singlePage)
			{
				if (control.fetchSingleTarget(s) == msgW.sender)
				{
					control.showSingleMSG(s, msgW.data + "\n");
				}
			}
		} else if (msgW.msgType == MessageWrapper.MULTIPLY)
		{
			for (Integer m : multiplyPage)
			{
				if (control.fetchMultiplyRoomID(m) == msgW.receiver)
				{
					control.showMultiplyMSG(m, msgW.data + "\n");
				}
			}
		} else if (msgW.msgType == MessageWrapper.CONTROL)
		{
			/* 0:CommandName */
			String[] rawData = msgW.data.split(ConstanceValue.CONTROL_SEPARATOR);
			String commandName = rawData[0];
			if (commandName.equals(ConstanceValue.READ_MEMBER_LIST))
			{
							/* 1:RoomID 2...n:UserData */
				int roomid = Integer.parseInt(rawData[1]);
				ArrayList<User> users = new ArrayList<>();
				for (int i = 2; i < rawData.length; i += 3)
				{
					users.add(new User(rawData[i], Integer.parseInt(rawData[i + 1]), Integer.parseInt(rawData[i + 2])));
				}
				for (Integer m : multiplyPage)
				{
					if (control.fetchMultiplyRoomID(m) == roomid)
					{
						control.configMultiplyMemberList(m, users);
						control.addEvent(new ClientEvent(m, ClientEvent.EventIndex.RenderMultiplyPageMemberList));
					}
				}
			} else if (commandName.equals(ConstanceValue.MANAGE_RELATION))
			{

				int targetType = Integer.parseInt(rawData[1]);
				int actionType = Integer.parseInt(rawData[2]);
				boolean done = Boolean.parseBoolean(rawData[3]);
				if (done)
				{
					if (targetType == ConstanceValue.TARGET_TYPE_FRIEND)
					{
						User user = new User(rawData[4], Integer.parseInt(rawData[5]), Integer.parseInt(rawData[6]));
						if (actionType == ConstanceValue.ACTION_TYPE_ADD)
							friendList.add(user);
						else if (actionType == ConstanceValue.ACTION_TYPE_DELETE)
							friendList.remove(user);
						control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.RenderMainPageFriendPanel, friendList));
					} else if (targetType == ConstanceValue.TARGET_TYPE_CHATROOM)
					{
						ChatRoom chatRoom = new ChatRoom(rawData[4], Integer.parseInt(rawData[5]));
						if (actionType == ConstanceValue.ACTION_TYPE_ADD)
							chatroomList.add(chatRoom);
						else if (actionType == ConstanceValue.ACTION_TYPE_DELETE)
							chatroomList.remove(chatRoom);
						else if (actionType == ConstanceValue.ACTION_TYPE_CREATE)
							chatroomList.add(chatRoom);
						control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.RenderMainPageMultiplyPanel, chatroomList));
					}
				} else
				{
					errorCode = ErrorCode.NO_TARGET_OR_EXISTED;
					return;
				}
			} else if (commandName.equals(ConstanceValue.UPDATE_STATUS))
			{
				//StatusChangeUserID, StatusIndex,CONTROL,UPDATE_STATUS
				for (User friend : friendList)
				{
					if (friend.id == msgW.sender)
						if (msgW.receiver == ConstanceValue.STATUS_LOGIN)
							friend.status |= ConstanceValue.STATUS_LOGIN;
						else if (msgW.receiver == ConstanceValue.STATUS_LOGOUT)
							friend.status &= ~ConstanceValue.STATUS_LOGIN;
				}
				for (Integer pageIndex : multiplyPage)
				{
					control.addEvent(new ClientEvent(pageIndex, ClientEvent.EventIndex.SendControlMSG, ConstanceValue.READ_MEMBER_LIST + ConstanceValue.CONTROL_SEPARATOR + control.fetchMultiplyRoomID(pageIndex)));
				}
				control.addEvent(new ClientEvent(mainPage, ClientEvent.EventIndex.RenderMainPageFriendPanel, friendList));
			} else if (commandName.equals(ConstanceValue.UPDATE_CHATROOM_MEMBER))
			{
				//NewMemberID, ChatRoomID, CONTROL, UPDATE_CHATROOM_MEMBER~ActionType
				int chatRoomID = msgW.receiver;
				for (int pageIndex : multiplyPage)
				{
					if (control.fetchMultiplyRoomID(pageIndex) == chatRoomID)
					{
						control.addEvent(new ClientEvent(pageIndex, ClientEvent.EventIndex.SendControlMSG, ConstanceValue.READ_MEMBER_LIST + ConstanceValue.CONTROL_SEPARATOR + chatRoomID));
					}
				}
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

	public static void warningLoh(String warning)
	{
		MyLib.warningLog(warning);
	}

	public static void errorLog(String error)
	{
		MyLib.errorLog(error);
	}
}
