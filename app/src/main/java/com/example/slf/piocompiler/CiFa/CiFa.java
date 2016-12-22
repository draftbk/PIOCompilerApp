
package com.example.slf.piocompiler.CiFa;


import com.example.slf.piocompiler.YuFa.MyError;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;

//词法分析类		使用状态转换图实现
public class CiFa {
	private BufferedReader in;
	private String line; // 存放读取的一行源代码
	private int lineNum = 0;// 存放line的行号
	private int lineLength = 0;// 存放line中的字符总数
	private char Cha; // 存放当前读取的字符
	private int index = -1;// 存放当前读取的字符索引 索引从0开始

	private Word word[];// 保留字表
	int id_max_len = 10; // 定义标识符最大长度

	private boolean end = false;// 指示源代码是否读完

	public MyError error;
	public int errorNumber = 0; // 词法分析中的错误数目

	// 根据原代码构造词法分析类
	public CiFa(String filename) {
		// 定义保留字表 共13个保留字
		word = new Word[13];
		word[0] = new Word("begin", "beginsym");
		word[1] = new Word("call", "callsym");
		word[2] = new Word("const", "constsym");
		word[3] = new Word("do", "dosym");
		word[4] = new Word("end", "endsym");
		word[5] = new Word("if", "ifsym");
		word[6] = new Word("odd", "oddsym");
		word[7] = new Word("procedure", "procsym");
		word[8] = new Word("read", "readsym");
		word[9] = new Word("then", "thensym");
		word[10] = new Word("var", "varsym");
		word[11] = new Word("while", "whilesym");
		word[12] = new Word("write", "writesym");

		// 根据文件名建立文件输入流
		try {
			FileInputStream fis = new FileInputStream(filename);
			in = new BufferedReader(new InputStreamReader(fis));
		} catch (FileNotFoundException a) {
			System.out.println("没有找到文件！！");
		}
		in= new BufferedReader(new StringReader(filename)); ;
	}

	// 从原代码中取得一个字符
	public void GetCh() {
		try {
			if (index == lineLength - 1)// 如果到达行尾,则重新读取一行源码
			{
//				获取一行
				line = in.readLine().trim();
				lineLength = line.length();
//				行号++
				lineNum++;
//				用lineLength=0循环来跳过空行
				while (lineLength == 0) {
					line = in.readLine().trim();
					lineLength = line.length();
					lineNum++;
				}
				index = 0;
				Cha = line.charAt(index);
			} else {// 否则 读取下个字符
				index++;
				Cha = line.charAt(index);
			}
		} catch (Exception e) {
			end = true;// 源代码读取完毕
		}
	}

	// 从原代码中取得一个单词
	public Word GetWord() {
		int charNum1;// 记录当前单词的开始索引
		int charNum2;// 记录当前单词的结束索引
		String tokenContent;// 当前单词内容

		GetCh();
//		和上面跳过空行一样，跳过空格
		while (Cha == ' ') {
			GetCh();
		}
		// 判断是标识符
		if (Character.isLetter(Cha)) {//是字母
			charNum1 = index;
			while ((Character.isLetter(Cha) || Character.isDigit(Cha))
					&& index < lineLength - 1) {
				GetCh();
			}

			// 判断while为什么停止循环 如果cha不是数字或字母表示读到非数字和字母而停止
			if (!Character.isDigit(Cha) && !Character.isLetter(Cha)) {
				index--;
			}
			charNum2 = index;

			// 标识符长度越界
			if ((charNum2 - charNum1 + 1) > id_max_len) {
				error.error(lineNum, 19);
				errorNumber++;
				tokenContent = line.substring(charNum1 - 1, charNum1 - 1
						+ id_max_len);
			} else {
				tokenContent = line.substring(charNum1, charNum2 + 1);
			}

			// 判断是否是保留字
			int n = 0;
			while (n < 13 && (!word[n].getContent().equals(tokenContent))) {
				n++;
			}
			if (n == 13) // 保留字表中没有找到 则为用户定义标识符
			{
				return new Word(tokenContent, "ident", lineNum);
			} else {
//				找到并返回该保留字
				word[n].setLineNum(lineNum);
				return word[n];
			}
		} else if (Character.isDigit(Cha)) {// 判断是整数
			charNum1 = index;
//			是数字且没读完
			while (Character.isDigit(Cha) && index < lineLength - 1) {
				GetCh();
			}
// 判断while为什么停止循环 如果cha不是数字表示读到非数字而停止
			if (!Character.isDigit(Cha)) {
				index--;
			}
			charNum2 = index;
//			提取这一段
			tokenContent = line.substring(charNum1, charNum2 + 1);
			return new Word(tokenContent, "number", lineNum);
		} else if (Cha == ':') {
			GetCh();
			if (Cha == '=') {
				return new Word(":=", ":=sym", lineNum);
			} else // 无定义符号：
			{
				index--;
				return new Word(":", "null", lineNum);
			}
		} else if (Cha == '<') {
			GetCh();
			if (Cha == '=') {
				return new Word("<=", "<=sym", lineNum);
			} else {
				index--;
				return new Word("<", "<sym", lineNum);
			}
		} else if (Cha == '>') {
			GetCh();
			if (Cha == '=') {
				return new Word(">=", ">=sym", lineNum);
			} else {
				index--;
				return new Word(">", ">sym", lineNum);
			}
		} else {
			String sym;
			String sr = Character.toString(Cha);
			switch (Cha) {
				case '+':
					sym = "+sym";
					break;
				case '-':
					sym = "-sym";
					break;
				case '*':
					sym = "*sym";
					break;
				case '/':
					sym = "/sym";
					break;
				case '(':
					sym = "(sym";
					break;
				case ')':
					sym = ")sym";
					break;
				case '=':
					sym = "=sym";
					break;
				case '#':
					sym = "#sym";
					break;
				case ',':
					sym = ",sym";
					break;
				case '.':
					sym = ".sym";
					end = true;
					break;
				case ';':
					sym = ";sym";
					break;
				default:
					sym = "null";
			}
			return new Word(sr, sym, lineNum);

		}
	}

	public int getErroNumber() {
		return errorNumber;
	}

	public boolean getEnd() {
		return end;
	}

	public static void main(String[] args) {
		CiFa cifa = new CiFa("test.txt");
//		一个个输出
		while (!cifa.getEnd()) {
			System.out.println(cifa.GetWord());
		}

	}
}
