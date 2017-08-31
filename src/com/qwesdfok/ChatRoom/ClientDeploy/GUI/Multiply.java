package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.Libs.ImgFilter;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;
import com.qwesdfok.ChatRoom.CommonModule.User;
import com.qwesdfok.Libs.MyLib;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class Multiply extends GUIRoot
{
	private JTextPane outputArea = new JTextPane();
	private JTextPane inputArea = new JTextPane();
	private JButton sendButton = new JButton("Send");
	private JButton closeButton = new JButton("Close");
	private JButton ImgButton = new JButton("Picture");
	private JPanel memberPanel = new JPanel();
	private int roomID;
	private ArrayList<User> memberList;

	public Multiply(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		JScrollPane outputScroll = new JScrollPane(outputArea);
		JScrollPane inputScroll = new JScrollPane(inputArea);
		JPanel rootPanel = new JPanel();
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		rootPanel.setLayout(new GridBagLayout());
		frame.add(rootPanel);
		memberPanel.setLayout(new GridLayout(0, 1, 5, 5));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.SOUTH;
		constraints.insets = new Insets(4, 8, 5, 8);

		rootPanel.add(outputScroll, configConstraint(constraints, 0, 0, 8, 5, 1, 1));
		rootPanel.add(inputScroll, configConstraint(constraints, 0, 6, 8, 4, 1, 1));
		rootPanel.add(memberPanel, configConstraint(constraints, 8, 0, 2, 9, 0, 1));
		constraints.fill = GridBagConstraints.NONE;
		rootPanel.add(sendButton, configConstraint(constraints, 8, 9, 1, 1, 0, 0));
		rootPanel.add(closeButton, configConstraint(constraints, 9, 9, 1, 1, 0, 0));
		rootPanel.add(ImgButton, configConstraint(constraints, 0, 5, 1, 1, 0, 0));
		closeButton.addActionListener((e) -> {
			frame.dispose();
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Exit));
		});
		sendButton.addActionListener((e) -> {
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.SendMultiplyMSG, inputArea.getText()));
		});
		ImgButton.addActionListener((e) ->{
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			ImgFilter imgFilter = new ImgFilter();
			jfc.setFileFilter(imgFilter);
			jfc.showDialog(new Label(), "选择图片");
			
			File img = jfc.getSelectedFile();
			String message = FiletoString(img);
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.SendMultiplyImg, message));
		});
		renderMember();
		MyLib.infoLog("Multiply Construction OK");
	}

	public void renderMember()
	{
		if (memberList == null || frame == null)
			return;
		memberPanel.removeAll();
		for (User mem : memberList)
		{
			JLabel label = new JLabel(mem.name + "[" + (((mem.status & ConstanceValue.STATUS_LOGIN) != 0 || mem.name.equals(control.name)) ? "Online" : "Offline") + "]");
			memberPanel.add(label);
		}
		frame.validate();
		MyLib.infoLog("Render Member OK");
	}

	public void showMSG(String msg)
	{
		try
		{
			outputArea.getDocument().insertString(outputArea.getDocument().getLength(), msg, new SimpleAttributeSet());
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void showImg(String msg){
		String usedmsg = msg.substring(2);
		int imgint = usedmsg.indexOf(":");
		String user = usedmsg.substring(0, imgint);
		String imgmsg = usedmsg.substring(imgint + 2);
		byte[] data = new byte[imgmsg.length()];
		for(int i = 0;i < imgmsg.length();i++)
		{
			data[i]=(byte)imgmsg.charAt(i);
		}
		ImageIcon temp = new ImageIcon(data);
		//outputArea.insertIcon(temp);
		StyledDocument doc = outputArea.getStyledDocument();
		Style style = doc.addStyle("test", null);
		SimpleAttributeSet attr = new SimpleAttributeSet();  
		StyleConstants.setIcon(style, temp);
		try {
			doc.insertString(doc.getLength(), user + ":\n", attr);
			doc.insertString(doc.getLength(), user + ":\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showMSG("\n");
	}

	public int getRoomID()
	{
		return roomID;
	}

	public void setRoomID(int roomID)
	{
		this.roomID = roomID;
	}

	public void setMemberList(ArrayList<User> memberList)
	{
		this.memberList = memberList;
	}

	public ArrayList<User> getMemberList()
	{
		return memberList;
	}
	public String FiletoString(File f){
		try {
			StringBuilder sb=new StringBuilder();
			FileInputStream inputStream=new FileInputStream(f);
			while(inputStream.available()!=0)
			{
				int i=inputStream.read();
				char d=(char)i;
				sb.append(d);
			}
			return sb.toString();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
