package com.qwesdfok.ChatRoom.ClientDeploy;

public class ClientEvent
{
	public enum EventIndex
	{
		Login, /* (PageIndex, index) */
		Register,/* (PageIndex, index) */
		Exit, /* (PageIndex, index) */
		SendSingleMSG,/* (UserID, index, String) */
		SendMultiplyMSG,/* (UserID, index, String) */
		SendControlMSG, /* (UserID, index, String) */
		ReceiveMSG, /* (-, index, MessageWrapper) */
		RenderMainPageFriendPanel,/* (PageIndex, index, ArrayList<User>friendList) */
		RenderMainPageMultiplyPanel,/* (PageIndex, index, ArrayList<ChatRoom>ChatRoomList) */
		RenderMultiplyPageMemberList,/* (PageIndex, index, ArrayList<User>friendList) */
		OpenRegister, /* (PageIndex, index) */
		OpenSinglePage, /* (PageIndex, index, FriendID) */
		OpenMultiplyPage,/* (PageIndex, index) */
		OpenManagePanel, /* (PageIndex, index, Int targetType) */
		ManageRelation, /* (targetType, index, String Action+sepa+Name) */
		ListenerClose, /* (-, index) */
		IdleTimeOut, /* (-, index, -) */
		Error, /* (PageIndex, index, IsBackToLogin) */
		SendSingleImg,
		SendMultiplyImg,
		SendSingleFile,
	}

	;
	public int sourceIndex;
	public EventIndex eventIndex;
	public Object parameter;

	public ClientEvent(int sourceIndex, EventIndex eventIndex, Object parameter)
	{
		this.sourceIndex = sourceIndex;
		this.eventIndex = eventIndex;
		this.parameter = parameter;
	}

	public ClientEvent(int sourceIndex, EventIndex eventIndex)
	{

		this.sourceIndex = sourceIndex;
		this.eventIndex = eventIndex;
	}
}
