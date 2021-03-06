package com.cqu.roy.editOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import com.cqu.roy.attribute.TextAtrr;
import com.cqu.roy.fileOperation.FileOperation;
import com.cqu.roy.highlighting.SyntaxHighlighter;
import com.cqu.roy.historyStorage.Node;
import com.cqu.roy.historyStorage.VersionTree;
import com.cqu.roy.historyStorage.historyInfo;
import com.cqu.roy.mainframe.MainFrame;
import com.cqu.roy.mywdiget.JpathButton;
import com.cqu.roy.mywdiget.MainJpanel;
import com.cqu.roy.mywdiget.MyFontStyle;
import com.cqu.roy.mywdiget.MyJTextPane;

//此只是一个undo操作，具体数据结构在jtp里
public class Undo implements FileOperation{
	//版本树策略
	private Stack<HashSet<Integer>> UndoStack_vst;//Undo栈
	private Stack<HashSet<Integer>> RedoStack_vst;//Redo栈
	//整文本策略
	private Stack<historyInfo> UndoStack_text;
	private Stack<historyInfo> RedoStack_text;
	private MainFrame mainFrame;
	private MyJTextPane jtp;
	private VersionTree vst;
	private ArrayList<Node> currentNodeSet;
	@Override
	public void use(JPanel jp, JScrollPane jsp, JPanel northjp, Vector<Integer> close_id, Vector<Integer> untitled_vc,
			Vector<String> sequece_name, String currentAreaName, JpathButton currentButton,
			HashMap<String, MainJpanel> hmTextArea, HashMap<String, TextAtrr> hm_name_atrr,
			HashMap<String, JpathButton> hm_name_btn) {
		// TODO Auto-generated method stub
		mainFrame = MainFrame.getInstance();
		MainJpanel mainJp = hmTextArea.get(mainFrame.getCurrentAreaName());
		if (mainJp != null) {
			jtp = hmTextArea.get(mainFrame.getCurrentAreaName()).getTextPane();
		}else {
			return;
		}
		//版本树策略
		//versionTreeStrategy();
		//整文本策略
		textStrategy();
	}
	public void textStrategy() {
		RedoStack_text = jtp.getRedoStack_text();
		UndoStack_text = jtp.getUndoStack_text();
		
		//文本样式
		StyledDocument document = jtp.getStyledDocument();
		MyFontStyle myFontStyle = new MyFontStyle(document);
		document = myFontStyle.getStyleDoc();
		jtp.setStyledDocument(document);
		
		//获取上一个历史
		historyInfo hif = null;
		if (!UndoStack_text.isEmpty()) {
			hif = UndoStack_text.pop();
		}else {
			return;
		}
		//行号显示Pane的刷新
		HashMap<String, MainJpanel> hm_textPane = mainFrame.getHashTextPane();
		JPanel linePanel = hm_textPane.get(mainFrame.getCurrentAreaName()).getlinePanel();
		
		String text = hif.getTextInfo();
		int caretPosition = hif.getCaretPosition();
		
		try {
			jtp.setIsUndoRedo(true);
			jtp.getDocument().remove(0, jtp.getDocument().getLength());
			for(int i = jtp.getLine() - 1;i > 0;i--){
				linePanel.remove(i);
			}
			jtp.setLine(0);
			jtp.getDocument().insertString(0, text, document.getStyle("Style06"));
			jtp.setCaretPosition(caretPosition);
			jtp.setIsUndoRedo(false);
			

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//将当前信息压入Redo栈
		RedoStack_text.push(jtp.getHistoryInfo());
		//将文本域当前的history信息设置为hif
		jtp.setHistoryInfo(hif);
	}
	public void versionTreeStrategy() {
		vst = jtp.getVersionTree();//获取版本树
		currentNodeSet = vst.getCurrentNodeSet();//获取当前节点集合
		//获取Redo和Undo栈
		UndoStack_vst = jtp.getUndoStack_vst();
		RedoStack_vst = jtp.getRedoStack_vst();
		HashSet<Integer> modified = null;
		if (!UndoStack_vst.isEmpty()) {
			modified = UndoStack_vst.pop();
		}else {
			return;
		}
		Iterator<Integer> iterator = modified.iterator();
		//文本样式
		StyledDocument document = jtp.getStyledDocument();
		MyFontStyle myFontStyle = new MyFontStyle(document);
		document = myFontStyle.getStyleDoc();
		jtp.setStyledDocument(document);
		
		while(iterator.hasNext()){
			int lineNum = iterator.next();
			//当前节点
			Node node = currentNodeSet.get(lineNum);
			//其父节点
			Node parentNode = node.getParentNode();
			//若父节点为null，则继续
			if (parentNode == null) {
				continue;
			}
			//当前节点属性
			int node_startPosition = node.getText().getStartPostion();
			int node_length = node.getText().getLength();
			//父节点属性
			int parent_startPosition = parentNode.getText().getStartPostion();
			String parent_text = parentNode.getText().getText();
			int parent_length = parentNode.getText().getLength();
			int caretPosition = parentNode.getCaretPosition();
			try {
				jtp.getDocument().remove(node_startPosition, node_length);
				jtp.getDocument().insertString(parent_startPosition, parent_text
						, document.getStyle("Style06"));
				jtp.setCaretPosition(caretPosition);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//将当前节点集合赋为前一节点的父节点
			if (lineNum < jtp.getLine()) {
				currentNodeSet.set(lineNum, parentNode);
			}
		}
		//将Undo栈中pop的元素，压栈Redo
		RedoStack_vst.push(modified);
		System.out.println(UndoStack_vst.size());
	}
}
