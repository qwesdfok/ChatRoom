package com.qwesdfok.ChatRoom.ServerDeploy;

public class ServerEvent
{
	public enum EventIndex{
		ThreadStarted,/*(UserID,Index,ServerPoint)*/
		ThreadStop,/*(UserID,Index,ServerPoint)*/
		ReceiveMSG,/*(UserID,Index,MessageWrapper(String))*/
		BroadcastUserStatus,/*(UserId,Index,Int status)*/
	};
	public int sourceIndex;
	public EventIndex eventIndex;
	public Object parameter;

	public ServerEvent(int sourceIndex, EventIndex eventIndex, Object parameter)
	{
		this.sourceIndex = sourceIndex;
		this.eventIndex = eventIndex;
		this.parameter = parameter;
	}

	public ServerEvent(int sourceIndex, EventIndex eventIndex)
	{

		this.sourceIndex = sourceIndex;
		this.eventIndex = eventIndex;
	}
}
