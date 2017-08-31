package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;
import com.qwesdfok.Libs.ImgFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Single extends GUIRoot
{
	private JTextPane outputArea = new JTextPane();
	private JTextPane inputArea = new JTextPane();
	private JButton sendButton = new JButton("Send");
	private JButton closeButton = new JButton("Close");
	private JButton ImgButton = new JButton("Picture");
	private JButton fileButton = new JButton("File");
	private int targetID;

	public Single(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		JPanel rootPanel = new JPanel();
		JScrollPane outputScroll = new JScrollPane(outputArea);
		JScrollPane inputScroll = new JScrollPane(inputArea);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.add(rootPanel);
		rootPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.insets = new Insets(4, 8, 5, 8);

		rootPanel.add(outputScroll, configConstraint(constraints, 0, 0, 4, 2, 100, 100));
		rootPanel.add(inputScroll, configConstraint(constraints, 0, 3, 4, 1, 100, 100));
		constraints.fill = GridBagConstraints.NONE;
		rootPanel.add(sendButton, configConstraint(constraints, 0, 4, 1, 1, 0, 0));
		rootPanel.add(closeButton, configConstraint(constraints, 3, 4, 1, 1, 0, 0));
		rootPanel.add(ImgButton, configConstraint(constraints, 0, 2, 1, 1, 0, 0));
		rootPanel.add(fileButton, configConstraint(constraints, 1, 2, 1, 1, 0, 0));
		closeButton.addActionListener(e -> {
			frame.dispose();
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Exit));
		});
		sendButton.addActionListener(e -> {
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.SendSingleMSG, inputArea.getText()));
			showMSG(control.name + ":\n");
			showMSG(inputArea.getText() + "\n");
		});
		ImgButton.addActionListener((e) ->{
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			ImgFilter imgFilter = new ImgFilter();
			jfc.setFileFilter(imgFilter);
			jfc.showDialog(new Label(), "选择图片");
			
			File img = jfc.getSelectedFile();
			String message = FiletoString(img);
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.SendSingleImg, message));
			showImg("1~" + control.name + ":\n" + message);
		});
		fileButton.addActionListener((e) ->{
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.showDialog(new Label(), "选择文件");
			
			File file = jfc.getSelectedFile();
			String file_message = FiletoString(file);
			int position = file.getName().lastIndexOf(".");
			String type = file.getName().substring(position + 1);
			file_message = type + ConstanceValue.CONTROL_SEPARATOR + file_message;
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.SendSingleFile, file_message));
		});
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
	
	public void showFile(String msg){
		String usedmsg = msg.substring(2);
		int imgint = usedmsg.indexOf(":");
		String user = usedmsg.substring(0, imgint);
		String file_info = usedmsg.substring(imgint + 2);
		int ctrl_position = file_info.indexOf("~");
		String type = file_info.substring(0, ctrl_position);
		String file_msg = file_info.substring(ctrl_position + 1);
		
		JFileChooser jsf = new JFileChooser();
		int returnVal = jsf.showSaveDialog(new Label("确认"));
		try {
			File file = jsf.getSelectedFile();
			if(file == null)
				return;
			if(!file.getPath().endsWith("." + type))
				file = new File(file.getPath() + "." + type);
			FileOutputStream fis = new FileOutputStream(file);
			fis.write(file_msg.getBytes("ISO-8859-1"));
			fis.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public int getTargetID()
	{
		return targetID;
	}

	public void setTargetID(int targetID)
	{
		this.targetID = targetID;
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
