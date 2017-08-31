package com.qwesdfok.Libs;

import java.io.File;

public class ImgFilter extends javax.swing.filechooser.FileFilter{

	@Override
	public boolean accept(File f) {
		// TODO Auto-generated method stub
		String name = f.getName();
		if(name.toLowerCase().endsWith(".gif") || name.toLowerCase().endsWith(".jpg") 
				||name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpeg"))
			return true;
		else
			return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "*.gif,*.png,*.jpg,*,jpeg";
	}
}
