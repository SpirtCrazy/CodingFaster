package com.cqu.roy.fileOperation;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.cqu.roy.attribute.TextAtrr;
import com.cqu.roy.mainframe.MainFrame;
import com.cqu.roy.mywdiget.JpathButton;
import com.cqu.roy.mywdiget.MainJpanel;
import com.cqu.roy.mywdiget.MyJTextPane;

public class CloseWindow implements FileOperation{
	private MainFrame mainFrame = MainFrame.getInstance();

	@Override
	public void use(JPanel jp, JScrollPane jsp, JPanel northjp, Vector<Integer> close_id, Vector<Integer> untitled_vc,
			Vector<String> sequece_name, String currentAreaName, JpathButton currentButton,
			HashMap<String, MainJpanel> hmTextArea, HashMap<String, TextAtrr> hm_name_atrr,
			HashMap<String, JpathButton> hm_name_btn) {
		// TODO Auto-generated method stub
		System.exit(0);
		//mainFrame.setVisible(false);
	}
}
