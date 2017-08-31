package com.qwesdfok.ChatRoom.CommonModule;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class SocketWrapper
{
	private Socket socket;
	private String encoding = "ISO-8859-1";
	private BufferedOutputStream outputStream;
	private BufferedInputStream inputStream;
	private byte[] buffer = new byte[1024];
	private int length = 0;
	private int index = 0;

	public SocketWrapper(Socket socket) throws IOException
	{
		this.socket = socket;
		init();
	}

	private void init() throws IOException
	{
		outputStream = new BufferedOutputStream(socket.getOutputStream());
		inputStream = new BufferedInputStream(socket.getInputStream());
	}

	public void close() throws IOException
	{
		outputStream.close();
		inputStream.close();
		socket.close();
	}

	public MessageWrapper readNextWrapper() throws IOException
	{
		return new MessageWrapper(readNextItem());
	}

	public String readNextItem(String readEncoding) throws IOException
	{
		ArrayList<Byte> sizeByte = new ArrayList<>();
		ArrayList<Byte> dataByte = new ArrayList<>();
		int size = 0;
		while (true)
		{
			if (index == length)
			{
				length = inputStream.read(buffer);
				if (length == -1)
					throw new EOFException("Connection Closed.");
				index = 0;
			}
			if (size == 0)
			{
				if (buffer[index] != 0)
				{
					sizeByte.add(buffer[index]);
					index++;
				} else
				{
					size = Integer.parseInt(new String(byteListToArray(sizeByte), encoding));
					index++;
				}
			} else
			{
				dataByte.add(buffer[index]);
				index++;
				if (dataByte.size() == size)
					return new String(byteListToArray(dataByte), readEncoding);
			}
		}
	}

	public String readNextItem() throws IOException
	{
		return readNextItem(encoding);
	}

	public void writeWrapper(MessageWrapper wrapper) throws IOException
	{
		writeItem(wrapper.fetchSendByte());
	}

	public void writeItem(byte[] dataBytes) throws IOException
	{
		byte[] appendBytes = Integer.toString(dataBytes.length).getBytes(encoding);
		ArrayList<Byte> transData = new ArrayList<>();
		for (byte b : appendBytes)
			transData.add(b);
		transData.add((byte) 0);
		for (byte b : dataBytes)
			transData.add(b);
		outputStream.write(byteListToArray(transData));
		outputStream.flush();
	}

	public void writeString(String data) throws IOException
	{
		byte[] dataBytes = data.getBytes("UTF-8");
		byte[] appendBytes = Integer.toString(dataBytes.length).getBytes(encoding);
		ArrayList<Byte> transData = new ArrayList<>();
		for (byte b : appendBytes)
			transData.add(b);
		transData.add((byte) 0);
		for (byte b : dataBytes)
			transData.add(b);
		outputStream.write(byteListToArray(transData));
		outputStream.flush();
	}

	private byte[] byteListToArray(ArrayList<Byte> list)
	{
		byte[] arr = new byte[list.size()];
		for (int i = 0; i < list.size(); i++)
			arr[i] = list.get(i);
		return arr;
	}

	/*             Getter & Setter             */

	public Socket getSocket()
	{
		return socket;
	}
}
