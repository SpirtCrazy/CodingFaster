package com.cqu.roy.editOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.cqu.roy.attribute.TextAtrr;
import com.cqu.roy.fileOperation.FileOperation;
import com.cqu.roy.mywdiget.JpathButton;
import com.cqu.roy.mywdiget.MainJpanel;

public class Redo implements FileOperation{
	private Stack<ArrayList<Integer>> RedoStack;
	
	public Redo() {
		// TODO Auto-generated constructor stub
		RedoStack = new Stack<>();
	}
	@Override
	public void use(JPanel jp, JScrollPane jsp, JPanel northjp, Vector<Integer> close_id, Vector<Integer> untitled_vc,
			Vector<String> sequece_name, String currentAreaName, JpathButton currentButton,
			HashMap<String, MainJpanel> hmTextArea, HashMap<String, TextAtrr> hm_name_atrr,
			HashMap<String, JpathButton> hm_name_btn) {
		// TODO Auto-generated method stub
		System.out.println("here is Redo");
	}

}
