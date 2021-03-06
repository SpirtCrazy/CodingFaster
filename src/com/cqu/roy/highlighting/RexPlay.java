package com.cqu.roy.highlighting;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cqu.roy.constant.KeyWord;

/*C语言保留字
 * type:auto char enum float int long   
 * short signed struct typedef union
 * unsigned void volatile double
 * 
 * key word:break case const continue
 * default do else extern for goto if register
 * return sizeof static switch while*/
public class RexPlay {
	//匹配的token是关键字还是normal token
	private final static String type_keyword = "keyword";
	private final static String type_normalToken = "normalToken";
	private String textLine;
	//匹配关键字的前缀
	private final static String prefix = ".*[^A-Za-z0-9_]+";
	//匹配关键字的后缀
	private final static String suffix = "[^A-Za-z0-9_]+.*";
	//识别注释，带前缀
	private final static String prenotes = ".*//.*";
	//识别注释
	private final static String notes = "//.*";
	//识别带前缀字符串
	private final static String precharacterString = "\".*\"";
	//识别带前缀后缀的数字
	private final static String presufInteger = "[^A-Za-z_]*[0-9]+[^A-Za-z_]*";
	//识别数字
	private final static String integer = "[0-9]+";
	//识别函数
	private final static String function = "[A-Za-z_]+[A-Za-z_0-9]*(.*)";
	//只对函数名进行渲染
	private final static String function_name = "[^0-9]+[A-Za-z_0-9]*";
	//每个依赖语言的token的信息
	private Vector<Token> vc_lan_token;
	//每个normal token的信息
	private Vector<Token> vc_normal_token;
	//每个分离出来的词素中能够提取多少个关键词:语言依赖
	private Vector<Integer> keyWordNum;
	//语言不依赖
	private Vector<Integer> normalNum;
	//每个分离出来带前缀后缀依赖于语言关键字的token的集合
	private ArrayList<String> pre_su_keyword = new ArrayList<>();
	//每个分离出来带前缀后缀不依赖于语言的，需要渲染的token
	private ArrayList<String> pre_su_normal = new ArrayList<>();
	//存放每个带前缀后缀依赖语言token的绝对位置
	private ArrayList<Integer> keyWord_absLocation = new ArrayList<>();
	//存放每个带前缀后缀normal token的绝对位置
	private ArrayList<Integer> normal_absLocation = new ArrayList<>();
	//保留关键字的正则表达式
	private String allRegex_KeyWord = null;
	//类型的正则表达式
	private String allRegex_Type = null;
	//所有word模式的正则表达式,带前缀后缀
	private String allRegex = null;
	//带前缀后缀的normal正则表达式
	private String presunormalRegex = null;
	//normal的正则表达式
	private String normalRegex = null;
	//不带前缀后缀关键字的正则表达式
	private String matches_keyword_regex = null;
	//不带前缀后缀类型的正则表达式
	private String matches_type_regex = null;
	//所有保留字
	private String matches_regex = null;
	//计数每个依赖语言词素绝对位置
	private int abs_keyword_count = 0;
	//计数每个normal词素的绝对位置
	private int abs_normal_count = 0;
	//private final static String matchBreak = "break|.*[^A-Za-z0-9]+break|break[^A-Za-z0-9]+.*|.*[^A-Za-z0-9]+break[^A-Za-z0-9]+.*";
	String[] splitString;
	//存放关键词保留字
	private HashMap<String, String> hm_wold_regex = new HashMap<>();
	//存放类型保留字
	private HashMap<String, String> hm_type_regex = new HashMap<>();
	//type表
	private Set<String> typeWord;
	//keyword表
	private Set<String> keyWord;
	public RexPlay(String textLine) {
		// TODO Auto-generated constructor stub
		this.textLine = textLine;
		//C语言

		keyWordNum = new Vector<>();
		normalNum = new Vector<>();
		vc_lan_token = new Vector<>();
		vc_normal_token = new Vector<>();
		typeWord = new HashSet<>();
		keyWord = new HashSet<>();
		TableGet();
		generaterStringReg();
		generaterMatchesReg();
		//获取整个正则表达式
		getAllRegex_C();
		//获取Normal词素的正则表达式
		getpresuNormalTokenRegex();
		getNormalTokenRegex();
		
		String sp = textLine;
		//去掉字符串开头的space
		for(int i = 0; i < textLine.length();i++){
			if (textLine.charAt(i) != ' ') {
				sp = sp.substring(i);
				break;
			}
		}
		//newLine是每行间的分割
		Vector<String> newLine = byNewLine(sp);
		//按照每行的顺序，依次将文本，注释放如splitString中，不能乱啦位置，否则渲染位置会出错
		Vector<String> vc_splitString = new Vector<>();
		for(int i = 0; i < newLine.size();i++){
			//notes是注释和代码的分割
			Vector<String> notes = splitStringBynotes(newLine.get(i));
			if (notes.size() != 0) {
				splitString = splitString(notes.get(0));
				for(int j = 0; j < splitString.length;j++){
					vc_splitString.add(splitString[j]);
				}
			}
			if (notes.size() > 1) {
				vc_splitString.add(notes.get(1));
			}
		}
		matchesprefixAndsuffixKeyWord(vc_splitString,allRegex,matches_regex
				,vc_lan_token,pre_su_keyword,keyWord_absLocation,type_keyword);
		matchesprefixAndsuffixKeyWord(vc_splitString, presunormalRegex, normalRegex
				, vc_normal_token, pre_su_normal,normal_absLocation,type_normalToken);
		for(int k = 0;k < vc_lan_token.size();k++){
			Token token = vc_lan_token.get(k);
			token.setLocation(token.getAbsLocation() + token.getStartPosition());
//			System.out.println(token.getValue() + "  " + "Location:" 
//					+ token.getLocation() +  " AbsLocation:" + token.getAbsLocation() + 
//					"  end:" + (token.getAbsLocation() + token.getStartPosition() + token.getLength())
//					+ " length:" + token.getLength());
		}
		for(int i = 0; i < vc_normal_token.size();i++){
			Token token = vc_normal_token.get(i);
			token.setLocation(token.getAbsLocation() + token.getStartPosition());
//			System.out.println(token.getValue() + "  " + "Location:" 
//					+ token.getLocation() +  " AbsLocation:" + token.getAbsLocation() + 
//					"  end:" + (token.getAbsLocation() + token.getStartPosition() + token.getLength())
//					+ " length:" + token.getLength());
		}
	}
	public HashSet<String> getKeyWord() {
		return (HashSet<String>) keyWord;
	}
	public HashSet<String> getTypeWord() {
		return (HashSet<String>) typeWord;
	}
	public Vector<Token> getLanToken() {
		return vc_lan_token;
	}
	public Vector<Token> getNormalToken() {
		return vc_normal_token;
	}
	
	//数出绝对位置
	public void countingAbsLocation(String textLine,Vector<String> vc_splitString
			,ArrayList<Integer> absLocation,ArrayList<String> presuword) {
		int count_pre_su_keyword = 0;//计数能匹配第几个前缀后缀的词素
		int count_token = 0;//计数第几个分离的串
		for(int i = 0; i < textLine.length();){
			if (textLine.charAt(i) == ' ' || textLine.charAt(i) == '\n' 
					 || textLine.charAt(i) == '\r' ) {
				i++;
			}else {
				if (count_pre_su_keyword == presuword.size()) {
					return;
				}
				//
				if (vc_splitString.get(count_token) == presuword.get(count_pre_su_keyword)) {
					absLocation.add(i);
					count_pre_su_keyword++;
				}
				i = vc_splitString.get(count_token).length() + i;
				count_token++;
			}
		}
	}
	//红色字体
	//匹配带前缀后缀的关键词
	public void matchesprefixAndsuffixKeyWord(Vector<String> vc_splitString,String allRegex
			,String matchRegex,Vector<Token> vc,ArrayList<String> presuWord
			,ArrayList<Integer> absLocation,String type) {
		//if匹配的时候与A-Z a-z 0-9中间至少夹着一个特殊字符
		
		Pattern pattern = Pattern.compile(allRegex);
		for(int j = 0; j < vc_splitString.size();j++){
			Matcher matcher = pattern.matcher(vc_splitString.get(j));
			if (matcher.matches()) {
				presuWord.add(vc_splitString.get(j));
			}	
		}
		countingAbsLocation(textLine,vc_splitString,absLocation,presuWord);
		for(int i = 0; i < presuWord.size();i++){
			matchesKeyWord(presuWord.get(i),matchRegex,allRegex,vc,absLocation,type);
		}
	}
	//从带前缀后缀的关键词中匹配出关键词
	public void matchesKeyWord(String prefixAndSuffixKeyWord,String matchRegex
			,String allRegex,Vector<Token> vc,ArrayList<Integer> absLocation,String type){

		Pattern pattern = Pattern.compile(matchRegex);//匹配关键字
		Pattern pattern_pre_su_keyword = Pattern.compile(allRegex);//在使用subString的时候还要检查新的String是否满足前后缀的条件
		//在带前缀后缀的子串中可能包含着多个关键词的信息如 dwq&if*daw(break)qwq
		//此处就包含了多个关键词，通过while循环代替递归的方式，将起寻找完，并将其信息
		//存在Token对象中
		int count = 0;//计数
		int startPosition = 0;
		int endPosition = 0;
		while(true){
			Matcher matcher_pre_su_keyword = pattern_pre_su_keyword.matcher(prefixAndSuffixKeyWord);
			Matcher matcher = pattern.matcher(prefixAndSuffixKeyWord);
			//ifd^if会出现错误表示
			if (matcher.find() && matcher_pre_su_keyword.matches()) {
				if (count == 0) {
					Token token = new Token(matcher.group(0), matcher.start()
							, matcher.end() - 1, matcher.end() - matcher.start());
					if (type.equals(type_keyword)) {
						token.setAbsLocation(absLocation.get(abs_keyword_count));
					}else {
						token.setAbsLocation(absLocation.get(abs_normal_count));
					}
					vc.add(token);
					startPosition = matcher.end();
				}
				else {
					startPosition = startPosition + matcher.start();
					endPosition = startPosition - matcher.start() + matcher.end() - 1;
					Token token = new Token(matcher.group(0), startPosition
							, endPosition, matcher.end() - matcher.start());
					//不同的token的位置信息在不同的Location集合中
					if (type.equals(type_keyword)) {
						token.setAbsLocation(absLocation.get(abs_keyword_count));
					}else {
						token.setAbsLocation(absLocation.get(abs_normal_count));
					}
					vc.add(token);
					startPosition = startPosition + matcher.end() - matcher.start();
				}
				prefixAndSuffixKeyWord = prefixAndSuffixKeyWord.substring(matcher.end());
				count++;
			}else {
				if (type.equals(type_keyword)) {
					abs_keyword_count++;
					keyWordNum.add(count);
				}else {
					abs_normal_count++;
					normalNum.add(count);
				}
				break;
			}
		}
	}

	//以空格和换行符将单词分开
	public String[] splitString (String str) {
		str.trim();
		Pattern pattern = Pattern.compile("[ ]+|[\\n]");
		String[] temp = pattern.split(str);
		return temp;
	}
	//先用换行符分割
	public Vector<String> byNewLine(String str) {
		Pattern pattern = Pattern.compile("\n");
		String []temp = pattern.split(str);
		Vector<String> byNewLinew = new Vector<>();
		for (int i = 0; i < temp.length; i++) {
			byNewLinew.add(temp[i]);
		}
		return byNewLinew;
	}
	//以//先将注释分开
	public Vector<String> splitStringBynotes(String str) {
		Pattern pattern = Pattern.compile("//");
		Matcher matcher = pattern.matcher(str);
		Vector<String> byNotes = new Vector<>();
		if (matcher.find()) {
			int positon = matcher.start();
			String temp_1 = str.substring(0, positon);
			//System.out.println(temp_1);
			byNotes.add(temp_1);
			String temp_2 = str.substring(positon,str.length());
			byNotes.add(temp_2);
			//System.out.println(temp_2);
		}else {
			byNotes.add(str);
		}
		return byNotes;
	}
	//生成每个关键字需要的正则表达式,带前缀后缀
	public void generaterStringReg() {
		//先生成关键词保留字
		for(int i = 0; i < KeyWord.KeyWord_C.length;i++){
			String kw = KeyWord.KeyWord_C[i] + "|" + prefix + KeyWord.KeyWord_C[i]
					+ "|" + KeyWord.KeyWord_C[i] + suffix + "|"
					+ prefix + KeyWord.KeyWord_C[i] + suffix;
			hm_wold_regex.put(KeyWord.KeyWord_C[i], kw);
		}
		
		//再生成类型保留字
		for(int i = 0; i < KeyWord.Type_C.length;i++){
			String tp = KeyWord.Type_C[i] + "|" + prefix + KeyWord.Type_C[i]
					+ "|" + KeyWord.Type_C[i] + suffix + "|"
					+ prefix + KeyWord.Type_C[i] + suffix;
			hm_type_regex.put(KeyWord.Type_C[i], tp);
		}
	}
	//不带前缀后缀的关键字正则表达式
	public void generaterMatchesReg(){
		//存放从一段带前缀后缀中匹配出来的关键字,并在这个Token中存放其位置长度值信息
		//段内偏移量
		//"if|else|"
		for(int i = 0; i < KeyWord.KeyWord_C.length;i++){
			//begin
			if (i == 0) {
				matches_keyword_regex = KeyWord.KeyWord_C[i];
			}
			else {
				matches_keyword_regex = matches_keyword_regex + "|" + KeyWord.KeyWord_C[i];
			}
		}
		for(int i = 0; i < KeyWord.Type_C.length;i++){
			//begin
			if (i == 0) {
				matches_type_regex = KeyWord.Type_C[i];
			}
			else {
				matches_type_regex = matches_type_regex + "|" + KeyWord.Type_C[i];
			}
		}
		matches_regex = matches_keyword_regex + "|" + matches_type_regex;
		matches_regex = matches_regex + "|" + notes;// + "|" + integer;// + "|" + function;
	}
	public void getAllRegex_C() {
		for(int i = 0; i < KeyWord.KeyWord_C.length;i++){
			if (i == 0) {
				allRegex_KeyWord = hm_wold_regex.get(KeyWord.KeyWord_C[i]);
			}else {
				allRegex_KeyWord = allRegex_KeyWord + "|" + hm_wold_regex.get(KeyWord.KeyWord_C[i]);
			}
		}
		for(int i = 0; i < KeyWord.Type_C.length;i++){
			if (i == 0) {
				allRegex_Type = hm_type_regex.get(KeyWord.Type_C[i]);
			}else {
				allRegex_Type = allRegex_Type + "|" + hm_type_regex.get(KeyWord.Type_C[i]);
			}
		}
		allRegex = allRegex_KeyWord + "|" + allRegex_Type;
		allRegex = allRegex + "|" + prenotes;// + "|" + presufInteger;// + "|" + function;
	}
	public void TableGet() {
		for(int i = 0; i < KeyWord.KeyWord_C.length;i++){
			keyWord.add(KeyWord.KeyWord_C[i]);
		}
		for (int i = 0; i < KeyWord.Type_C.length; i++) {
			typeWord.add(KeyWord.Type_C[i]);
		}
	}
	//带前缀后缀的normal词素
	public void getpresuNormalTokenRegex() {
		presunormalRegex = presufInteger;
	}
	//不带前缀后缀normal词素
	public void getNormalTokenRegex() {
		normalRegex = integer;
	}
}
