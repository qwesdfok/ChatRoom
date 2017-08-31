package com.qwesdfok.ChatRoom.ClientDeploy.ExtendCompent;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Created by qwesd on 2016/4/25.
 */
public class IconCellRendererComponent extends DefaultTreeCellRenderer
{
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (value instanceof IconTreeNode)
		{
			IconTreeNode node = ((IconTreeNode) value);
			setIcon(node.getIcon());
		}
		return this;
	}
}
