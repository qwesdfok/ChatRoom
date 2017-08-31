package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ManagePage extends GUIRoot
{
	private JRadioButton manageAddButton = new JRadioButton("Add");
	private JRadioButton manageDeleteButton = new JRadioButton("Delete");
	private JRadioButton manageCreateButton = new JRadioButton("CreateChatRoom");
	private ButtonGroup group = new ButtonGroup();
	private JTextField inputArea = new JTextField();
	private JPanel rootPanel = new JPanel();
	private JPanel actionPanel = new JPanel();
	private JPanel inputPanel = new JPanel();
	private JPanel typePanel = new JPanel();
	private int targetType;
	private int actionType;

	public ManagePage(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		JButton confirmButton = new JButton("Confirm");
		JButton cancelButton = new JButton("Cancel");
		frame.setSize(500, 150);
		frame.setLocationRelativeTo(null);
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		group.add(manageAddButton);
		group.add(manageDeleteButton);

		typePanel.add(manageAddButton);
		typePanel.add(manageDeleteButton);
		rootPanel.add(typePanel);

		inputArea.setPreferredSize(new Dimension(300, 30));
		inputArea.setMinimumSize(new Dimension(300, 30));
		inputPanel.add(new JLabel("Name:"));
		inputPanel.add(inputArea);
		rootPanel.add(inputPanel);

		confirmButton.addActionListener((ActionEvent e) -> {
			if (manageAddButton.isSelected())
				actionType = ConstanceValue.ACTION_TYPE_ADD;
			else if (manageDeleteButton.isSelected())
				actionType = ConstanceValue.ACTION_TYPE_DELETE;
			else if (targetType == ConstanceValue.TARGET_TYPE_CHATROOM && manageCreateButton.isSelected())
				actionType = ConstanceValue.ACTION_TYPE_CREATE;
			control.addEvent(new ClientEvent(targetType, ClientEvent.EventIndex.ManageRelation, "" + actionType + ConstanceValue.CONTROL_SEPARATOR + inputArea.getText()));
		});
		cancelButton.addActionListener((ActionEvent e) -> {
			frame.dispose();
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.Exit));
		});

		actionPanel.add(confirmButton);
		actionPanel.add(cancelButton);
		rootPanel.add(actionPanel);

		frame.add(rootPanel);
	}

	public int getTargetType()
	{
		return targetType;
	}

	public void setTargetType(int targetType)
	{
		this.targetType = targetType;
		if (targetType == ConstanceValue.TARGET_TYPE_CHATROOM)
		{
			group.add(manageCreateButton);
			typePanel.add(manageCreateButton);
		}
		rootPanel.validate();
	}
}
