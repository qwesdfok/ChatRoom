package com.qwesdfok.ChatRoom.ServerDeploy;

import com.qwesdfok.Libs.MyLib;

public class ChatRoomServer
{
	public static void main(String[] argv)
	{
		ServerControl control = new ServerControl();
		if (argv.length == 2)
		{
			control.setDatabaseUserName(argv[0]);
			control.setDatabasePassword(argv[1]);
		} else if (argv.length == 3)
		{
			control.setDatabaseUserName(argv[0]);
			control.setDatabasePassword(argv[1]);
			control.setDatabaseIPAddress(argv[2]);
		}
		MyLib.infoLog("Database UserName: " + control.getDatabaseUserName());
		MyLib.infoLog("Database Password: " + control.getDatabasePassword());
		MyLib.infoLog("Database IPAddress: " + control.getDatabaseIPAddress());
		control.start();
	}

}