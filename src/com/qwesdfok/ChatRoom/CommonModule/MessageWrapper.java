package com.qwesdfok.ChatRoom.CommonModule;


public class MessageWrapper implements Cloneable
{
	public static final int SINGLE = 1;
	/*
	  SendUserId,TargetUserId,SINGLE,msg
	 */
	public static final int MULTIPLY = 2;
	/*
	  SendUserId,ChatRoomID,MULTIPLY,msg
	 */
	public static final int CONTROL = 3;
	/*
	    UPDATE_STATUS:
	      Server -> Client: StatusChangeUserID, StatusIndex,CONTROL,UPDATE_STATUS
	    MANAGE_RELATION:
	      Client -> Server: SenderID, SenderID, CONTROL, MANAGE_RELATION~targetType~actionType~TargetName
	      Server -> Client: -, -, CONTROL, MANAGE_RELATION~targetType~actionType~done~INFO...
	                        //Client will flush panel
	    READ_MEMBER_LIST:
	      Client -> Server: SenderID, -, CONTROL, READ_MEMBER_LIST~ChatRoomID
	      Server -> Client: -, -, CONTROL,READ_MEMBER_LIST~INFO...
	    UPDATE_CHATROOM_MEMBER:
	      Server -> Client: NewMemberID, ChatRoomID, CONTROL, UPDATE_CHATROOM_MEMBER~ActionType
	    IDLE_MESSAGE:
	      Client -> Server: SenderID, -, CONTROL, IDLE_MESSAGE
	      Server -> Client: -, -, CONTROL, IDLE_MESSAGE
	 */
	public int sender;
	public int receiver;
	public int msgType = 1;
	public String data;
	public String encoding = "UTF-8";
	private static final String sepa = "#";

	public MessageWrapper(int sender, int receiver, int msgType, String data)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.msgType = msgType;
		this.data = data;
	}

	public MessageWrapper(int sender, int receiver, int msgType, String data,String encoding)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.msgType = msgType;
		this.data = data;
		this.encoding = encoding;
	}

	public MessageWrapper(String infoStr)
	{
		try
		{
			int flag0 = infoStr.indexOf(sepa);
			sender = Integer.parseInt(infoStr.substring(0, flag0));
			int flag1 = infoStr.indexOf(sepa, flag0 + 1);
			receiver = Integer.parseInt(infoStr.substring(flag0 + 1, flag1));
			int flag2 = infoStr.indexOf(sepa, flag1 + 1);
			msgType = Integer.parseInt(infoStr.substring(flag1 + 1, flag2));
			int flag3 = infoStr.indexOf(sepa, flag2 + 1);
			encoding = infoStr.substring(flag2 + 1, flag3);
			this.data = new String(infoStr.substring(flag3 + 1).getBytes("ISO-8859-1"), encoding);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public byte[] fetchSendByte()
	{
		try
		{
			return ("" + sender + sepa + receiver + sepa + msgType + sepa + encoding + sepa + data).getBytes(encoding);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public MessageWrapper clone()
	{
		MessageWrapper o = null;
		try
		{
			o = (MessageWrapper) super.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return o;
	}
}
