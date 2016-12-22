package com.example.slf.piocompiler.JieShi;

import com.example.slf.piocompiler.CiFa.CiFa;
import com.example.slf.piocompiler.MainActivity;
import com.example.slf.piocompiler.YuFa.CodeElement;
import com.example.slf.piocompiler.YuFa.YuFa;

import java.util.Scanner;
import java.util.Vector;



public class JieShi {
	private CodeElement i;// 指令寄存器
	public static String output="";
	private int p;// 程序计数器
	private int t;// 栈顶寄存器
	private int b;// 基地址寄存器
	private int[] s;// 数据栈
	private int errorNumber;

	private Vector Code;

	public JieShi(String source) {
		// 声明一个词法分析类
		CiFa cifa = new CiFa(source);
		YuFa yuFa = new YuFa(cifa);
		yuFa.analyse();

		errorNumber = cifa.getErroNumber() + yuFa.getErroNumber();

		System.out.println("语法分析结束！");

		if (errorNumber == 0) {
			System.out.println("错误总数为:" + errorNumber);
			System.out.println("");
			System.out.println("说明部分生成的符号表为：");
			yuFa.printTable();
			System.out.println("");
			System.out.println("生成的目标代码为：");
			yuFa.printCode();
			System.out.println();
		} else {
			System.out.println("错误总数为" + errorNumber);
		}
		p = t = b = 0;
		s = new int[100];
		Code = yuFa.getCode();
		s[0] = s[1] = s[2] = 0;
	}

	// 循环解释目标代码
	public void run() {
		if (!(errorNumber==0)) {
			return;
		}
		System.out.println("执行程序：");
		do {
			i = (CodeElement) Code.get(p); /* 读当前指令 */
			p++;
			String f = i.getF();
			if (f.equals("lit")) {/* 将a的值取到栈顶 */
				s[t] = i.getA();
				t++;
			} else if (f.equals("opr")) {/* 数学、逻辑运算 */
				switch (i.getA()) {
					case 0:
						t = b;
						p = s[t + 2];
						b = s[t + 1];
						break;
					case 1:
						s[t - 1] = -s[t - 1];
						break;
					case 2:
						t--;
						s[t - 1] = s[t - 1] + s[t];
						break;
					case 3:
						t--;
						s[t - 1] = s[t - 1] - s[t];
						break;
					case 4:
						t--;
						s[t - 1] = s[t - 1] * s[t];
						break;
					case 5:
						t--;
						s[t - 1] = s[t - 1] / s[t];
						break;
					case 6:
						s[t - 1] = s[t - 1] % 2;
						break;
					case 8:
						t--;
						if (s[t - 1] == s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 9:
						t--;
						if (s[t - 1] != s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 10:
						t--;
						if (s[t - 1] < s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 11:
						t--;
						if (s[t - 1] >= s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 12:
						t--;
						if (s[t - 1] > s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 13:
						t--;
						if (s[t - 1] <= s[t])
							s[t - 1] = 1;
						else
							s[t - 1] = 0;
						break;
					case 14:
						System.out.print(s[t - 1]);
						output=output+s[t - 1]+" ";
						t--;
						break;
					case 15:
						System.out.println();
						break;
					case 16:
//						Scanner sc = new Scanner(System.in);
						s[t] = 1;
						t++;
						break;
				}

			} else if (f.equals("lod")) {/* 取相对当前过程的数据基地址为a的内存的值到栈顶 */
				s[t] = s[base(i.getL(), b) + i.getA()];
				t++;
			} else if (f.equals("sto")) { /* 栈顶的值存到相对当前过程的数据基地址为a的内存 */
				t--;
				s[base(i.getL(), b) + i.getA()] = s[t];
			} else if (f.equals("cal")) {/* 调用子过程 */
				s[t] = base(i.getL(), b); /* 将父过程基地址入栈 */
				s[t + 1] = b; /* 将本过程基地址入栈，此两项用于base函数 */
				s[t + 2] = p; /* 将当前指令指针入栈 */
				b = t; /* 改变基地址指针值为新过程的基地址 */
				p = i.getA(); /* 跳转 */
			} else if (f.equals("int")) {/* 分配内存 */
				t += i.getA();
			} else if (f.equals("jmp")) {/* 直接跳转 */
				p = i.getA();
			} else if (f.equals("jpc")) {/* 条件跳转 */
				t--;
				if (s[t] == 0)
					p = i.getA();
			}
		} while (p != 0);
	}

	/* 求调用层上l层过程的基址 */
	int base(int l, int b) {
		int b1 = b;
		while (l > 0) {
			b1 = s[b1];
			l--;
		}
		return b1;
	}

}
