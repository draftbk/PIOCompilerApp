package com.example.slf.piocompiler.YuFa;

public class CodeElement // 目标指令类
{
	private String f = ""; // 第一个参数：功能码
	private int l; // 第二个参数：层次差
	private int a; // 第三个参数：位移量

	public CodeElement(String x, int y, int z) {
		this.f = x;
		this.l = y;
		this.a = z;
	}

	public void setA(int t) {
		this.a = t;
	}

	public String getF() {
		return f;
	}

	public int getL() {
		return l;
	}

	public int getA() {
		return a;
	}

	public String toString() {
		return f + l + a;
	}
}
