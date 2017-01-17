package com.cqu.roy.highlighting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.text.BadLocationException;

import com.cqu.roy.historyStorage.Node;
import com.cqu.roy.historyStorage.TextInfo;
import com.cqu.roy.historyStorage.VersionTree;
import com.cqu.roy.mywdiget.MyJTextPane;

//计时器，用于redo undo
//每1秒记录一次
public class TimerSchedule implements Runnable{
	private boolean isModified;
	private MyJTextPane jtp;
	private HashSet<Integer> modified;
	private VersionTree vst;
	private ArrayList<Node> currentNode;//显示结点
	private int count;//计数器，
	public TimerSchedule(boolean isModified,MyJTextPane jtp,HashSet<Integer> modified,VersionTree vst) {
		// TODO Auto-generated constructor stub
		this.isModified = isModified;
		this.jtp = jtp;
		this.modified = modified;
		this.vst = vst;
		currentNode = vst.getCurrentNodeSet();
		count = 0;
	}
	public void setIsModified(boolean isModified) {
		this.isModified = isModified;
	}
	public String getModifiedString(int startPosition) {
		String str = null;
		int temp = startPosition;
		try {
			while(jtp.getDocument().getText(temp, 1) != "\n"){
				temp++;
			}
			str = jtp.getDocument().getText(startPosition, temp - startPosition);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Iterator iterator = modified.iterator();
			if (isModified) {
				while(iterator.hasNext()){
					int currentLine = (int)iterator.next();
					Node node = currentNode.get(currentLine);
					
					TextInfo textInfo = node.getText();
					int startPostion = textInfo.getStartPostion();
					try {
						
						if (currentLine == 0) {
							System.out.println(getModifiedString(startPostion));
							//System.out.println(jtp.getDocument().getText(startPostion, 1));
						}else {
							System.out.println(getModifiedString(startPostion + 1));
							System.out.println(jtp.getDocument().getText(startPostion + 1, 1));
						}
						//System.out.println(jtp.getDocument().getText(startPostion, 1));
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			modified.clear();
		}
	}
}
