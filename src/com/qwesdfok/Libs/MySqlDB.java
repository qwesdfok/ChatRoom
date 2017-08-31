package com.qwesdfok.Libs;

/**
 * Created by qwesd on 2015/11/20.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qwesd on 2015/9/17.
 */
public class MySqlDB
{
	private String url;
	private Connection connection;
	private Statement stmt;
	private String hostName = "localhost";
	private String port = "3306";
	private String databaseName = "";
	private String userName = "root";
	private String password = "199667";

	public MySqlDB(String url)
	{
		this.url = url;
	}

	public MySqlDB() {}

	public void connect() throws SQLException,ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		generateURL();
		connection = DriverManager.getConnection(url);
		stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public synchronized ResultSet executeQuery(String sql)throws SQLException
	{
		return stmt.executeQuery(sql);
	}
	public synchronized boolean executeCommand(String sql)throws SQLException
	{
		return stmt.execute(sql);
	}
	public synchronized int executeUpdate(String sql)throws SQLException
	{
		return stmt.executeUpdate(sql);
	}
	public void close() throws SQLException
	{
		if(connection!=null)
			connection.close();
	}

	private String generateURL()
	{
		url = "jdbc:mysql://" + hostName + ":" + port + "/" + databaseName + "?user=" + userName + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8";
		return url;
	}
	public synchronized List<Object []> executeQueryObjects(String sql) throws SQLException
	{
		List<Object []> objLists=new ArrayList<Object []>();

		ResultSet rs=stmt.executeQuery(sql);
		ResultSetMetaData rmd=rs.getMetaData();
		int colCount=rmd.getColumnCount();
		while(rs.next())
		{
			Object [] objData=new Object[colCount];
			for(int i=0;i<colCount;i++)
			{
				objData[i]=rs.getObject(i+1);
			}
			objLists.add(objData);
		}
		return objLists ;
	}

	/*                                     Getter & Setter                                         */
	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getHostName()
	{

		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public void setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}
}
