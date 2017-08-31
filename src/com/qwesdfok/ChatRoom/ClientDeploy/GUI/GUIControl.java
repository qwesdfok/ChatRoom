package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.CommonModule.ChatRoom;
import com.qwesdfok.ChatRoom.CommonModule.User;
import com.qwesdfok.Libs.Encryption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.qwesdfok.ChatRoom.ClientDeploy.ClientControl.debugLog;

public class GUIControl
{
	public static int MAIN_PAGE = 0x1;
	public static int SINGLE = 0x2;
	public static int MULTIPLY = 0x4;
	public static int LOGIN_PAGE = 0x8;
	public static int MANAGE_PAGE = 0x10;
	public static int REGISTER_PAGE = 0x20;
	public static int ALL_PAGE = 0xffffffff;
	private ArrayList<ClientEvent> eventQueue = new ArrayList<>();
	private ArrayList<ClientEvent> eventBufferQueue = new ArrayList<>();
	private boolean isEventScan = false;
	private Encryption encryption = new Encryption();
	private int pageIndex = 1;
	public HashMap<Integer, GUIRoot> pageMap = new HashMap<>();
	public String name, password, address, port;

	public GUIControl()
	{
	}

	public void setVisible(int pageIndex, boolean v)
	{
		for (Map.Entry<Integer, GUIRoot> entry : pageMap.entrySet())
		{
			if (entry.getKey() == pageIndex || pageIndex == ALL_PAGE)
				entry.getValue().frame.setVisible(v);
		}
	}

	public void closeWindow(int pageIndex)
	{
		ArrayList<Integer> remain = new ArrayList<>();
		for (Map.Entry<Integer, GUIRoot> entry : pageMap.entrySet())
		{
			if (entry.getKey() == pageIndex || pageIndex == ALL_PAGE)
			{
				entry.getValue().frame.dispose();
				remain.add(pageIndex);
			}
		}
		for (Integer integer : remain)
		{
			pageMap.remove(integer);
		}
	}

	public void unRegisterWindow(int pageIndex)
	{

		if(pageIndex==ALL_PAGE)
			pageMap.clear();
		if (pageMap.containsKey(pageIndex))
		{
			pageMap.remove(pageIndex);
			addEvent(new ClientEvent(pageIndex, ClientEvent.EventIndex.Exit));
		}
	}

	public int createWindow(int windowType)
	{
		GUIRoot page = null;
		if (windowType == LOGIN_PAGE)
		{
			page = new LoginPage(this, fetchNewPageIndex());
		} else if (windowType == MAIN_PAGE)
		{
			page = new MainPage(this, fetchNewPageIndex());
		} else if (windowType == MULTIPLY)
		{
			page = new Multiply(this, fetchNewPageIndex());
		} else if (windowType == SINGLE)
		{
			page = new Single(this, fetchNewPageIndex());
		} else if (windowType == MANAGE_PAGE)
		{
			page = new ManagePage(this, fetchNewPageIndex());
		} else if (windowType == REGISTER_PAGE)
		{
			page = new RegisterPage(this, fetchNewPageIndex());
		}
		if (page != null)
		{
			pageMap.put(page.index, page);
			return page.index;
		} else
			return 0;
	}

	public void showErrorMSG(int pageIndex, String str)
	{
		pageMap.get(pageIndex).displayError(str);
	}

	public void timeCircle()
	{
		if (eventQueue.size() == 0)
			return;
		isEventScan = true;
		for (ClientEvent event : eventQueue)
		{
			debugLog("GUIControl:Event: " + event.eventIndex.name() + " Index: " + event.sourceIndex + "\n");
			if (event.eventIndex == ClientEvent.EventIndex.Login)
			{
				LoginPage login = (LoginPage) pageMap.get(event.sourceIndex);
				name = login.fetchUserName();
				password = encryption.digest(name + login.fetchPassword() + "password");
				address = login.fetchAddress();
				port = login.fetchPort();
			} else if (event.eventIndex == ClientEvent.EventIndex.Register)
			{
				RegisterPage registerPage = (RegisterPage) pageMap.get(event.sourceIndex);
				name = registerPage.fetchName();
				address = registerPage.fetchAddress();
				port = registerPage.fetchPort();
			} else if (event.eventIndex == ClientEvent.EventIndex.RenderMainPageFriendPanel)
			{
				MainPage mainPage = (MainPage) pageMap.get(event.sourceIndex);
				mainPage.renderFriend((ArrayList<User>) event.parameter);
			} else if (event.eventIndex == ClientEvent.EventIndex.RenderMainPageMultiplyPanel)
			{
				MainPage mainPage = (MainPage) pageMap.get(event.sourceIndex);
				mainPage.renderMultiply((ArrayList<ChatRoom>) event.parameter);
			} else if (event.eventIndex == ClientEvent.EventIndex.RenderMultiplyPageMemberList)
			{
				((Multiply) pageMap.get(event.sourceIndex)).renderMember();
			}
		}
	}

	private int fetchNewPageIndex()
	{
		return pageIndex++;
	}

	public void addEvent(ClientEvent event)
	{
		if (isEventScan)
			eventBufferQueue.add(event);
		else
			eventQueue.add(event);
	}

	public void nextEvents()
	{
		isEventScan = false;
		eventQueue.clear();
		eventQueue.addAll(eventBufferQueue);
		eventBufferQueue.clear();
	}

	public void configSingleTarget(int pageIndex, int tarID)
	{
		((Single) pageMap.get(pageIndex)).setTargetID(tarID);
	}

	public int fetchSingleTarget(int pageIndex)
	{
		return ((Single) pageMap.get(pageIndex)).getTargetID();
	}

	public void showSingleMSG(int pageIndex, String msg)
	{
		if(msg.substring(0, 1).equals("1") && msg.substring(1, 2).equals("~"))
			((Single) pageMap.get(pageIndex)).showImg(msg);
		else if(msg.substring(0, 1).equals("2") && msg.substring(1, 2).equals("~"))
			((Single) pageMap.get(pageIndex)).showFile(msg);
		else
			((Single) pageMap.get(pageIndex)).showMSG(msg);
	}

	public void configMultiplyID(int pageIndex, int roomID)
	{
		((Multiply) pageMap.get(pageIndex)).setRoomID(roomID);
	}

	public int fetchMultiplyRoomID(int pageIndex)
	{
		return ((Multiply) pageMap.get(pageIndex)).getRoomID();
	}

	public void configMultiplyMemberList(int pageIndex, ArrayList<User> list)
	{
		((Multiply) pageMap.get(pageIndex)).setMemberList(list);
	}

	public void showMultiplyMSG(int pageIndex, String msg)
	{
		if(msg.substring(0, 1).equals("1") && msg.substring(1, 2).equals("~"))
			((Multiply) pageMap.get(pageIndex)).showImg(msg);
		else
			((Multiply) pageMap.get(pageIndex)).showMSG(msg);
	}

	public void configManageTargetType(int pageIndex, int targetType)
	{
		((ManagePage) pageMap.get(pageIndex)).setTargetType(targetType);
	}

	public int fetchManageTargetType(int pageIndex)
	{
		return ((ManagePage) pageMap.get(pageIndex)).getTargetType();
	}

	public String fetchRegisterPassword(int pageIndex)
	{
		String p = ((RegisterPage) pageMap.get(pageIndex)).fetchPassword();
		if (p != null)
		{
			password = encryption.digest(name + ((RegisterPage) pageMap.get(pageIndex)).fetchPassword() + "password");
			return password;
		} else
			return null;
	}

	public ArrayList<User> fetchMultiplyMemberList(int pageIndex)
	{
		return ((Multiply) pageMap.get(pageIndex)).getMemberList();
	}

	public ArrayList<ClientEvent> getEventQueue()
	{
		return eventQueue;
	}

	public String fetchPageName(int pageIndex)
	{
		return pageMap.get(pageIndex).getName();
	}

	public void configPageName(int pageIndex, String name)
	{
		pageMap.get(pageIndex).setName(name);
	}
}
