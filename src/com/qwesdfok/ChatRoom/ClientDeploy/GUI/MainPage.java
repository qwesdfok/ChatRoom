package com.qwesdfok.ChatRoom.ClientDeploy.GUI;

import com.qwesdfok.ChatRoom.ClientDeploy.ClientEvent;
import com.qwesdfok.ChatRoom.ClientDeploy.ExtendCompent.IconCellRendererComponent;
import com.qwesdfok.ChatRoom.ClientDeploy.ExtendCompent.IconTreeNode;
import com.qwesdfok.ChatRoom.CommonModule.ChatRoom;
import com.qwesdfok.ChatRoom.CommonModule.ConstanceValue;
import com.qwesdfok.ChatRoom.CommonModule.User;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MainPage extends GUIRoot
{
	private JPanel rootPanel = new JPanel();
	private JTree tree;
	private DefaultMutableTreeNode multiplyNode = new DefaultMutableTreeNode("ChatRoom");
	private DefaultMutableTreeNode friendNode = new DefaultMutableTreeNode("Friend");
	private ArrayList<User> friendList;
	private ArrayList<ChatRoom> chatRoomList;

	public MainPage(GUIControl guiControl, int pageIndex)
	{
		super(guiControl, pageIndex);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(control.name);
		tree = new JTree(rootNode);
		JPanel buttonPanel = new JPanel();
		JButton manageFriedButton = new JButton("ManageFriend");
		JButton manageChatRoomButton = new JButton("ManageChatRoom");
		frame.setTitle("MainPage");
		frame.setSize(300, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rootPanel.add(new JScrollPane(tree));
		tree.setCellRenderer(new IconCellRendererComponent());
		tree.addMouseListener(new MouseAdapter()
		{
			private long clickTime;
			public final long d = 300;

			@Override
			public void mousePressed(MouseEvent e)
			{
				long dis = System.currentTimeMillis() - clickTime;
//				System.out.print(dis + "\n");
				if (dis <= d)
				{
					TreePath path = tree.getPathForRow(tree.getRowForLocation(e.getX(), e.getY()));
					if (path != null)
					{
						DefaultMutableTreeNode treeNode = ((DefaultMutableTreeNode) path.getLastPathComponent());
						if (treeNode != null && treeNode.getUserObject() instanceof User)
							control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.OpenSinglePage, ((User) treeNode.getUserObject()).id));
						else if (treeNode != null && treeNode.getUserObject() instanceof ChatRoom)
							control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.OpenMultiplyPage, ((ChatRoom) treeNode.getUserObject()).id));
					}
				}
				clickTime = System.currentTimeMillis();
			}
		});
		rootNode.add(multiplyNode);
		rootNode.add(friendNode);

		manageFriedButton.addActionListener((ActionEvent e) -> {
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.OpenManagePanel, ConstanceValue.TARGET_TYPE_FRIEND));
		});
		manageChatRoomButton.addActionListener((ActionEvent e) -> {
			control.addEvent(new ClientEvent(index, ClientEvent.EventIndex.OpenManagePanel, ConstanceValue.TARGET_TYPE_CHATROOM));
		});
		buttonPanel.add(manageFriedButton);
		buttonPanel.add(manageChatRoomButton);
		rootPanel.add(buttonPanel);
		frame.add(rootPanel);
	}

	public void renderFriend(ArrayList<User> friendData)
	{
		if (frame == null)
			return;
		this.friendList = friendData;
		friendNode.removeAllChildren();
		if (friendList.size() == 0)
		{
			friendNode.add(new IconTreeNode("No friend", new ImageIcon(getClass().getResource("/com/qwesdfok/Resource/wrong.jpg"))));
		} else
		{
			for (User friend : friendList)
			{
				if (friend.status != 0)
					friendNode.add(new IconTreeNode(friend, new ImageIcon(getClass().getResource("/com/qwesdfok/Resource/user_on.jpg"))));
				else
					friendNode.add(new IconTreeNode(friend, new ImageIcon(getClass().getResource("/com/qwesdfok/Resource/user_off.jpg"))));
			}
		}
		tree.updateUI();
	}

	public void renderMultiply(ArrayList<ChatRoom> chatRoomData)
	{
		if (frame == null)
			return;
		this.chatRoomList = chatRoomData;
		multiplyNode.removeAllChildren();
		if (chatRoomList.size() == 0)
		{
			multiplyNode.add(new DefaultMutableTreeNode("No chatRoom"));
		} else
		{
			for (ChatRoom room : chatRoomList)
			{
				multiplyNode.add(new IconTreeNode(room, new ImageIcon(getClass().getResource("/com/qwesdfok/Resource/room_somebody.jpg"))));
			}
		}
		tree.updateUI();
	}

	public void setFriendList(ArrayList<User> friendList)
	{
		this.friendList = friendList;
	}

	public void setChatRoomList(ArrayList<ChatRoom> chatRoomList)
	{
		this.chatRoomList = chatRoomList;
	}
}
