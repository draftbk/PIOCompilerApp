package com.example.slf.piocompiler.YuFa;

public class TableElement {
	private String name, kind;
	private int val, lev, adr;

	public TableElement() {
		this.name = null;
		this.kind = null;
		this.val = -1;
		this.lev = 0;
		this.adr = 0;
	}

	public TableElement(TableElement t)// 拷贝构造函数
	{
		this.name = t.name;
		this.kind = t.kind;
		this.val = t.val;
		this.lev = t.lev;
		this.adr = t.adr;
	}

	public TableElement(String name, String kind, int val, int lev, int adr) {
		this.name = name;
		this.kind = kind;
		this.val = val;
		this.lev = lev;
		this.adr = adr;
	}

	public void setAdr(int cx0) {
		adr = cx0;
	}

	public int getAdr() {
		return adr;
	}

	public String getKind() {
		return kind;
	}

	public int getVal() {
		return val;
	}

	public String getNam() {
		return name;
	}

	public int getLev() {
		return lev;
	}

	public void showConst() {
		System.out.println("NAME: " + name + "\t" + "KIND: " + kind + "\t"
				+ "VAL: " + val);
	}

	public void show() {
		System.out.println("NAME: " + name + "\t" + "KIND: " + kind + "\t"
				+ "LEVEL: " + lev + "\t" + "ADR: " + adr);
	}

	public String toString() {
		return "NAME: " + name + "\t" + "KIND: " + kind + "\t" + "VAL:" + val + "\t"
				+ "LEVEL: " + lev + "\t" + "ADR: " + adr;
	}
}
