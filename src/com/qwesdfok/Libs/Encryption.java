package com.qwesdfok.Libs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by qwesd on 2015/11/20.
 */
public class Encryption
{
	private String arithmetic = "MD5";

	/**
	 * Choice a arithmetic. Default is MD5.
	 * @param arithmetic Arithmetic name.
	 */
	public Encryption(String arithmetic)
	{
		this.arithmetic = arithmetic;
	}

	public Encryption()
	{
	}

	/**
	 * Generate result.
	 * @param data To be digest.
	 * @return Result.
	 */
	public String digest(String data)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance(arithmetic);
			md.update(data.getBytes());
			return byteToHexStr(md.digest());
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	private String byteToHexStr(byte[] bytes)
	{
		return MyLib.byteToHexStr(bytes);
	}

	public String getArithmetic()
	{
		return arithmetic;
	}

	public void setArithmetic(String arithmetic)
	{
		this.arithmetic = arithmetic;
	}
}
