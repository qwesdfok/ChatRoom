package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class LoginPage extends GUIRoot
{
	private JTextField userNameText = new JTextField();
	private JPasswordField passwordText = new JPasswordField();
	private JTextField addressText = new JTextField("localhost");
	private JTextField portText = new JTextField(Integer.toString(ConstanceValue.DEFAULT_LISTENER_PORT));
	private JButton confirmButton = new JButton("Confirm");
	private JButton registerButton = new JButton("Register");


	public LoginPage(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		frame.setLayout(new GridLayout(0, 2, 16, 9));

		JButton exitButton = new JButton("Exit");
		int emptyLength = 10;
		frame.setSize(300, 180);
		frame.setLocationRelativeTo(null);

		frame.add(new JLabel("UserName:"));
		frame.add(userNameText);
		frame.add(new JLabel("Password:"));
		frame.add(passwordText);
		frame.add(new JLabel("HostIP:"));
		frame.add(addressText);
		frame.add(new JLabel("Port:"));
		frame.add(portText);
		frame.add(confirmButton);
		frame.add(registerButton);

		confirmButton.registerKeyboardAction(e -> {
			confirmAction();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.VK_UNDEFINED), JComponent.WHEN_IN_FOCUSED_WINDOW);
		confirmButton.addActionListener((e) -> {
			confirmAction();
		});
		registerButton.addActionListener((ActionEvent e) -> {
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.OpenRegister));
		});
		exitButton.addActionListener((ActionEvent e) -> {
			frame.dispose();
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Exit));
		});
	}

	private void confirmAction()
	{
		if (userNameText.getText() == null || userNameText.getText().length() == 0 || passwordText.getPassword() == null || passwordText.getPassword().length == 0)
		{
			displayError("UserName or Password is empty");
			return;
		}
		control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Login));
	}

	public String fetchUserName()
	{
		return userNameText.getText();
	}

	public String fetchPassword()
	{
		String str = new String(passwordText.getPassword());
		passwordText.setText("");
		return str;
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
