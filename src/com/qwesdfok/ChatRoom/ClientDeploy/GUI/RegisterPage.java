package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterPage extends GUIRoot
{
	private JTextField userNameText = new JTextField();
	private JPasswordField passwordText = new JPasswordField();
	private JPasswordField confirmText = new JPasswordField();
	private JTextField addressText = new JTextField("localhost");
	private JTextField portText = new JTextField(Integer.toString(ConstanceValue.DEFAULT_LISTENER_PORT));
	private String password = null;

	public RegisterPage(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		JButton confirmButton = new JButton("Confirm");
		JButton cancelButton = new JButton("Exit");
		frame.setLayout(new GridLayout(0, 2, 16, 9));
		frame.setSize(300, 200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.add(new JLabel("UserName:"));
		frame.add(userNameText);
		frame.add(new JLabel("Password:"));
		frame.add(passwordText);
		frame.add(new JLabel("Confirm:"));
		frame.add(confirmText);
		frame.add(new JLabel("HostIP:"));
		frame.add(addressText);
		frame.add(new JLabel("Port:"));
		frame.add(portText);
		frame.add(confirmButton);
		frame.add(cancelButton);
		confirmButton.addActionListener((ActionEvent e) -> {
			if (new String(passwordText.getPassword()).equals(new String(confirmText.getPassword())))
			{
				password = new String(passwordText.getPassword());
				control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Register));
			} else
				password = null;
		});
		cancelButton.addActionListener((ActionEvent e) -> {
			frame.dispose();
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Exit));
		});
	}

	public String fetchPassword()
	{
		return password;
	}

	public String fetchName()
	{
		return userNameText.getText();
	}

	public String fetchAddress()
	{
		return addressText.getText();
	}

	public String fetchPort()
	{
		return portText.getText();
	}
}
