package com.example.slf.piocompiler.CiFa;
public class Word {
	public String content;
	public String SYM;//单词类型
	public int lineNum;

	public Word() {
		this.SYM = null;
		this.content = null;
		this.lineNum = 0;
	}

	public Word(Word w) { // 拷贝构造函数
		this.SYM = w.SYM;
		this.content = w.content;
		this.lineNum = w.lineNum;
	}

	public Word(String content, String sym) {
		this.SYM = sym;
		this.content = content;
	}

	public Word(String content, String sym, int linenum) {
		this.SYM = sym;
		this.content = content;
		this.lineNum = linenum;
	}

	public void setLineNum(int l) {
		lineNum = l;
	}

	public int getLineNum() {
		return lineNum;
	}

	public String getContent() {
		return content;
	}

	public String getSym() {
		return SYM;
	}

	public String toString() {
		return SYM + " " + content + " " + lineNum;
	}

}
