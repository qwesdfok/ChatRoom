package com.qwesdfok.Libs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by qwesd on 2015/11/20.
 */
public class MyLib
{
	private static int logSetting = 0xffffffff;
	public static final int TRACE_LOG = 0x1;
	public static final int DEBUG_LOG = 0x2;
	public static final int INFO_LOG = 0x4;
	public static final int WARNING_LOG = 0x8;
	public static final int ERROR_LOG = 0x10;
	public static final int OUTPUT_LOG = 0x20;
	public static final int ALL_LOG=0xffffffff;

	public static String byteToHexStr(byte[] bytes)
	{
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char str[] = new char[bytes.length * 2];
		int k = 0;
		for (int i = 0; i < bytes.length; i++)
		{
			byte byte0 = bytes[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}

	public static void outputLog(String output)
	{
		if ((logSetting & OUTPUT_LOG) != 0) wrapper("[OUTPUT] " + output);
	}

	public static void simplyLog(String data)
	{
		if (!data.endsWith("\n"))
			data = data + "\n";
		writeToStdOut(data);
	}

	public static void traceLog(String trace)
	{
		if ((logSetting & TRACE_LOG) != 0) wrapper("[TRACE] " + trace);
	}

	public static void debugLog(String debug)
	{
		if ((logSetting & DEBUG_LOG) != 0) wrapper("[DEBUG] " + debug);
	}

	public static void infoLog(String info)
	{
		if ((logSetting & INFO_LOG) != 0) wrapper("[INFO] " + info);
	}

	public static void warningLog(String warning)
	{
		if ((logSetting & WARNING_LOG) != 0) wrapper("[WARNING] " + warning);
	}

	public static void errorLog(String error)
	{
		if ((logSetting & ERROR_LOG) != 0) wrapper("[ERROR] " + error);
	}

	private static void wrapper(String str)
	{
		Calendar calendar = Calendar.getInstance();
		if (!str.endsWith("\n"))
			str = str + "\n";
		writeToStdOut(String.format("[%04d-%02d-%02d|%02d:%02d:%02d] %s",
				calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), str));
	}

	private static void writeToStdOut(String str)
	{
		System.out.print(str);
	}

	public static void enableLog(int logIndex)
	{
		logSetting |= logIndex;
	}

	public static void disableLog(int logIndex)
	{
		logSetting &= ~logIndex;
	}

	public static void convertToHex(String inputPath, String outputPath, int skip, String separator) throws IOException
	{
		FileInputStream inputStream = new FileInputStream(inputPath);
		FileOutputStream outputStream = new FileOutputStream(outputPath);
		HashMap<Character, Byte> hexMap = new HashMap<Character, Byte>()
		{{
			put('0', (byte) 0x0);
			put('1', (byte) 0x1);
			put('2', (byte) 0x2);
			put('3', (byte) 0x3);
			put('4', (byte) 0x4);
			put('5', (byte) 0x5);
			put('6', (byte) 0x6);
			put('7', (byte) 0x7);
			put('8', (byte) 0x8);
			put('9', (byte) 0x9);
			put('A', (byte) 0xA);
			put('a', (byte) 0xA);
			put('B', (byte) 0xB);
			put('b', (byte) 0xB);
			put('C', (byte) 0xC);
			put('c', (byte) 0xC);
			put('D', (byte) 0xD);
			put('d', (byte) 0xD);
			put('E', (byte) 0xE);
			put('e', (byte) 0xE);
			put('F', (byte) 0xF);
			put('f', (byte) 0xF);
			put(' ', null);
			put('\r', null);
			put('\n', (byte) 0x20);
			put('\t', null);
			put('\f', null);
			put('\uffff', (byte) 0x20);
		}};
		int part = 0;
		byte temp = 0;
		while (inputStream.available() != 0)
		{
			for (int i = 0; i < skip; i++)
				inputStream.read();
			boolean nextLine = false;
			while (!nextLine)
			{
				char input = (char) inputStream.read();
				Byte data = hexMap.get(input);
				if (data != null)
				{
					if (data == 0x20)
					{
						nextLine = true;
						outputStream.write(separator.getBytes());
					}
					if (data >= 0x0 && data <= 0xF)
					{
						if (part == 0)
						{
							temp = (byte) (data << 4);
							part = 1;
						} else
						{
							temp = (byte) (temp + data);
							outputStream.write(new byte[]{temp});
							temp = 0;
							part = 0;
						}
					}
				}
			}
		}
	}
}