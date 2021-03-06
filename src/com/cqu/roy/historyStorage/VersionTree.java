package com.cqu.roy.historyStorage;

import java.util.ArrayList;
//第一种history策略，使用版本树，一行一行的存储，在undo redo的时候，计算position和同步都存在一定的问题
//Node,Root,TextInfo,VersionTree，为该策略使用到的类
public class VersionTree {
	private Root root;
	private ArrayList<Node> subNodeSet;//第一代版本
	private ArrayList<Node> currentNodeSet;//当前显示版本
	public VersionTree() {
		// TODO Auto-generated constructor stub
		root = new Root();
		subNodeSet = root.getSubNodeSet();
		currentNodeSet = new ArrayList<>();
	}
	//每增加一行，增加一个node,每行的改变，同行节点下一个节点,并且第一代版本不能有指向根节点的引用
	//当退格，退掉一行后，该行信息不删除，只将node的赋为null，但第一代版本退个
	public void InsertNode(int index,Node SubNode) {
		subNodeSet.add(index, SubNode);
	}
	
	//第一代退格删除，新一行没有进行任何操作，不会产生新版本节点
	public void removeNode(int index) {
		subNodeSet.remove(index);
	}
	
	//获取当前显示节点集合
	public ArrayList<Node> getCurrentNodeSet() {
		return currentNodeSet;
	}
}
