package com.qwesdfok.ChatRoom.CommonModule;

public class User
{
	public String name=null;
	public String password=null;
	public int id=0;
	public int status=0;

	public User(String name, int id, int status)
	{
		this.name = name;
		this.id = id;
		this.status = status;
	}

	public User()
	{
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof User)) return false;

		User user = (User) o;

		return id == user.id;

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
