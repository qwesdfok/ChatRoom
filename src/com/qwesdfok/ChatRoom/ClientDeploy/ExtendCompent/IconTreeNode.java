package com.qwesdfok.ChatRoom.ClientDeploy.ExtendCompent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * Created by qwesd on 2016/4/25.
 */
public class IconTreeNode extends DefaultMutableTreeNode
{
	private ImageIcon icon=null;
	private int iconSize=16;

	public IconTreeNode()
	{
	}

	public IconTreeNode(Object userObject)
	{
		super(userObject);
	}

	public IconTreeNode(Object userObject, ImageIcon icon)
	{
		super(userObject);
		this.icon = new ImageIcon(icon.getImage().getScaledInstance(iconSize,iconSize, Image.SCALE_SMOOTH));
	}

	public IconTreeNode(Object userObject, ImageIcon icon, int iconSize)
	{
		super(userObject);
		this.icon = icon;
		this.iconSize = iconSize;
	}

	public Icon getIcon()
	{
		return icon;
	}

	public void setIcon(ImageIcon icon)
	{
		this.icon = icon;
	}

	public int getIconSize()
	{
		return iconSize;
	}

	public IconTreeNode setIconSize(int iconSize)
	{
		this.iconSize = iconSize;
		return this;
	}
}
