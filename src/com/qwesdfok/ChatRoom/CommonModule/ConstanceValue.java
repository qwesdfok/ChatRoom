package com.qwesdfok.ChatRoom.CommonModule;

public class ConstanceValue
{
	public static String WRONG_USER_INFO = "wrong_user_info";
	public static String USER_LOGIN_OK = "user_login_ok";
	public static String WRONG_PASSWORD = "wrong_password";
	public static String READ_MEMBER_LIST = "read_member_list";
	public static String MANAGE_RELATION = "manage_relation";
	public static String UPDATE_STATUS = "update_status";
	public static String UPDATE_CHATROOM_MEMBER = "update_chatroom_member";
	public static String IDLE_MESSAGE = "idle_message";
	public static String CONTROL_SEPARATOR = "~";
	public static int period = 33;
	public static int warningPeriod = 100 * period;
	public static int errorPeriod = 200 * period;
	public static int sendWarningPeriod = 30 * period;
	public static int STATUS_LOGIN = 0x1;
	public static int STATUS_LOGOUT = 0x2;
	public static int TARGET_TYPE_FRIEND = 0x1;
	public static int TARGET_TYPE_CHATROOM = 0x2;
	public static int ACTION_TYPE_ADD = 0x1;
	public static int ACTION_TYPE_DELETE = 0x2;
	public static int ACTION_TYPE_CREATE = 0x3;
	public static int DEFAULT_LISTENER_PORT = 10492;
	public static int DEFAULT_SECOND_CONNECT_PORT = 10106;
	public static boolean ENABLE_IDLE_DETECT = false;
}
