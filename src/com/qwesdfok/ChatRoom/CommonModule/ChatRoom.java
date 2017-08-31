package com.qwesdfok.ChatRoom.CommonModule;

public class ChatRoom
{
	public String name;
	public int id;

	public ChatRoom(String name, int id)
	{
		this.name = name;
		this.id = id;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ChatRoom)) return false;

		ChatRoom chatRoom = (ChatRoom) o;

		return id == chatRoom.id;

	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
