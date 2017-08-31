package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GUIRoot
{
	protected JFrame frame = new JFrame();
	protected GUIControl control = null;
	protected int index;
	protected String name;

	public GUIRoot(GUIControl control, int index)
	{
		this.control = control;
		this.index = index;
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				control.unRegisterWindow(index);
			}
		});
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				resize(e);
			}
		});
	}

	public void displayError(String str)
	{
		JDialog dialog = new JDialog(frame, "Error");
		JTextField textField = new JTextField(str);
		textField.setEditable(false);
		dialog.setSize(200, 100);
		dialog.setLocationRelativeTo(frame);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.add(textField);
		dialog.setVisible(true);
	}

	public void resize(ComponentEvent e)
	{

	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		frame.setTitle(name);
	}

	protected GridBagConstraints configConstraint(GridBagConstraints constraints, int x, int y)
	{
		return configConstraint(constraints, x, y, 1, 1);
	}
	protected GridBagConstraints configConstraint(GridBagConstraints constraints, int x, int y, int width, int height)
	{
		return configConstraint(constraints, x, y, width, height, 100, 100);
	}

	protected GridBagConstraints configConstraint(GridBagConstraints constraints, int x, int y, int width, int height, double dx, double dy)
	{
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.weightx = dx;
		constraints.weighty = dy;
		return constraints;
	}
}
