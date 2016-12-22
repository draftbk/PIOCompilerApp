
package com.example.slf.piocompiler.YuFa;


import com.example.slf.piocompiler.CiFa.CiFa;
import com.example.slf.piocompiler.CiFa.Word;

import java.util.Vector;


//语法分析类		使用递归子程序法
public class YuFa {
	private CiFa ciFa;
	private Word word;// 语法分析当前处理的单词

	public String id = null; // 用于登录符号表时的name
	private int lev = -1;// 标明过程所在层次,主程序是第0层
	private int dx;// 标明当前程序局部变量的相对地址
	int cx;// 当前代码的地址

	Vector TABLE = new Vector();// 符号表,存放标识符信息;
	Vector CODE = new Vector();; // 存放编译生成的目标代码

	private MyError myError = new MyError();
	private int lineNum; // 记录语法分析中错误语句的行号
	private int errorNumber = 0; // 记录语法分析中的错误数目

	// 构造函数
	public YuFa(CiFa CF) {
		this.ciFa = CF;
	}

	public void analyse() {
		word = ciFa.GetWord();
		block(); // 然后调用分析程序
	}

	// 分析子程序
	public void block() {
		int tx = TABLE.size(); // 标明当前程序在符号表中的起始地址
		lev++;
		dx = 3;// 前3个地址单元已被占用

		TABLE.addElement(new TableElement(" ", " ", 0, 0, 0));// 在这里给符号表填加一个元素,指示当前程序
		((TableElement) TABLE.get(tx)).setAdr(CODE.size());// 在上面加的那个元素里的私有字段adr里保存下面跳转指令的地址
		CODE.addElement(new CodeElement("jmp", 0, 0)); // 生成跳转指令，由于跳转目标地址未知 暂时添0
		System.out.println("code:"+CODE.size());
		System.out.println(word.getSym());
		// 说明部分的分析和处理
		while (word.getSym().equals("constsym")
				|| word.getSym().equals("varsym")
				|| word.getSym().equals("procsym")) {
			if (word.getSym().equals("constsym"))// 常量
			{
				word = ciFa.GetWord();
				constDefine();// 常量说明处理
				while (word.getSym().equals(",sym"))// 若在常量声明后读取的word为逗号，则要继续进行常量声明,这里采用while循环，直到word不是逗号
				{
					word = ciFa.GetWord();
					constDefine();
				}
				if (word.getSym().equals(";sym"))// 若word不是逗号，则应该是分号了，标志常量说明结束
				{
					word = ciFa.GetWord();
				} else {// 如果常量声明完毕后没有遇到分号；则抛出15号错误
					errorNumber++;
					myError.error(word.getLineNum(), 5);
				}
			}// 变量
			else if (word.getSym().equals("varsym")) {
				word = ciFa.GetWord();
				varDefine(); // 变量量说明处理
				while (word.getSym().equals(",sym")) {// 若在变量声明后读取的word为逗号，则要继续进行变量声明,这里采用while循环，直到word不是逗号
					word = ciFa.GetWord();
					varDefine();
				}
				if (word.getSym().equals(";sym")) {// 若word不是逗号，则应该是分号了，标志变量说明结束
					word = ciFa.GetWord();
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 5);// 如果变量声明完毕后没有遇到分号；则调用错误处理程序抛出5号错误
				}
			}
			// 子过程
			while (word.getSym().equals("procsym")) {
				word = ciFa.GetWord();
				if (word.getSym().equals("ident")) {
					id = word.getContent();
					add("procedure");
					word = ciFa.GetWord();
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 4);// 过程名不是标识符，抛出4号错误
				}
				if (word.getSym().equals(";sym")) {
					word = ciFa.GetWord();
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 5);
				}// 过程名后没有分号

				int tempdx = dx;
				int levv = lev;
				if (lev == 2) {
					System.out.println("程序层数大于3");
				}
				block();// 在这里递归调用子程序分析
				dx = tempdx;
				lev = levv;

				if (word.getSym().equals(";sym")) {
					word = ciFa.GetWord();
					String sym = word.getSym();
					if (!(sym.equals("ident") || sym.equals("beginsym")
							|| sym.equals("callsym") || sym.equals("ifsym")
							|| sym.equals("whilesym") || sym.equals("procsym")
							|| sym.equals("readsym") || sym.equals("writesym"))) {
						// 若;后的符号不在ident,if,begin,call,while,read,write,proc中，则调用6号错误
						errorNumber++;
						myError.error(word.getLineNum(), 6);
					}
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 5);
				}
			}
			for(int i=0;i < TABLE.size(); i++){
				System.out.println(((TableElement)TABLE.get(i)));
			}
		}

		cx = CODE.size();// 当前代码的地址
		System.out.println("code:size" + cx);
		((CodeElement) CODE.get(((TableElement) TABLE.get(tx)).getAdr()))
				.setA(cx);// 把前面生成的跳转指令的目标地址改成当前将要生成的目标代码地址
		((TableElement) TABLE.get(tx)).setAdr(cx);
		CODE.addElement(new CodeElement("int", 0, dx));// 生成空间分配指令分配dx个空间（3个空间+变量的数目）
		System.out.println("!!!!!!!!");
		for(int i=0;i < CODE.size(); i++){
			System.out.println(((CodeElement)CODE.get(i)));
		}
		statement();// 处理当前遇到的语句
		CODE.addElement(new CodeElement("opr", 0, 0));// 生成子程序返回指令;
		for(int i=0;i < CODE.size(); i++){
			System.out.println(((CodeElement)CODE.get(i)));
		}
		System.out.println("$$$$$$$$$");
		System.out.println("code:size!!" + CODE.size());
	}

	// 常量说明处理
	public void constDefine() {
		if (word.getSym().equals("ident"))// 在block调用常量说明处理时，上一个word为const，这一个应该是ident标识符
		{
			id = word.getContent();// 调用Word类的getContent方法，获得当前word的内容，这里将其保存在id中
			word = ciFa.GetWord();// 然后再读取一个word，应该是等号"="了
			if (word.getSym().equals(":=sym")) {// 如果不是等号，而是赋值符号:=，抛出１号错误
				errorNumber++;
				myError.error(word.getLineNum(), 1);
			} else if (word.getSym().equals("=sym")) {
				word = ciFa.GetWord();// 继续读取一个word，现在应该是一个数字了
				if (word.getSym().equals("number")) {
					add("constant");// 如果当前word是数字，则调用符号表插入方法,将常量插入符号表；
					word = ciFa.GetWord();// 再次读取一个word，为后边做准备，应该是一个逗号或者是分号
				} else {// 如果等号后不是数字，调用2号错误
					errorNumber++;
					myError.error(word.getLineNum(), 2);
				}
			} else {// 如果标识符后不是等号，调用3号错误
				errorNumber++;
				myError.error(word.getLineNum(), 3);
			}
		} else {// 如果常量声明过程中遇到的第一个字符不是标识符，调用4号错误
			errorNumber++;
			myError.error(word.getLineNum(), 4);
		}
	}

	// 变量说明处理
	public void varDefine() {
		if (word.getSym().equals("ident")) {
			id = word.getContent();
			add("variable");// 调用符号表注册方法　进行变量的注册
			word = ciFa.GetWord();
		} else
		// 如果变量声明过程中遇到的第一个字符不是标识符，调用4号错误
		{
			errorNumber++;
			myError.error(word.getLineNum(), 4);
		}
	}

	// 在符号表中添加标识符

	public void add(String k) { // k中保存的是传过来的　string
		if (k.equals("constant")) {
			int num = Integer.parseInt(word.getContent());
			TABLE.addElement(new TableElement(id, k, num, 0, 0));
		} else if (k.equals("variable")) {
			TABLE.addElement(new TableElement(id, k, 0, lev, dx));
			dx = dx + 1;
		} else if (k.equals("procedure")) {
			TABLE.addElement(new TableElement(id, k, 0, lev, 0));
		}
	}// 符号表注册完成

	// 查找符号表
	public int search(String id) {
		int i = 0;
		while (i < TABLE.size()
				&& (!((TableElement) TABLE.get(i)).getNam().equals(id))) {
			i++;
		}
		if (i >= TABLE.size()) {// 如果没有找到，则返回负值
			return -1;
		} else {
			return i;
		}
	}

	// 语句的分析处理
	public void statement() {
		int i;
		int cx1;
		int cx2;

		if (word.getSym().equals("ident")) // 如果以标识符开头，则是赋值语句
		{
			i = search(word.getContent()); // 在符号表中找到该标识符所在位置；
			if (i == -1) // 如果返回的i为负值，则表示没有找到；抛出11号错误
			{
				errorNumber++;
				myError.error(word.getLineNum(), 11);
			} else if (!((TableElement) TABLE.get(i)).getKind().equals(
					"variable"))// 如果在符号表中找到了，但是该标识符不是变量名，则抛出12号错误
			{
				errorNumber++;
				myError.error(word.getLineNum(), 12);
				i = -1;// 并将i置为-1做为错误的标志；
			}
			word = ciFa.GetWord(); // 继续读取下一个word，正常应该是赋值符号
			if (word.getSym().equals(":=sym")) {
				word = ciFa.GetWord();// 如果是赋值符号，则继续读取一个word，正常应该是一个表达式；
			} else {
				errorNumber++;
				myError.error(word.getLineNum(), 13);// 如果不是赋值符号，则抛出13号错误
			}
			expression(); // 进行表达式的处理；
			if (i != -1)// 如果i不是负值，则表示未曾出错，i所表示的是当前语句左边标识符在符号表中的位置；
			{// 生成一条把表达式的值写往指定内存的代码；
				CODE.addElement(new CodeElement("sto", lev
						- ((TableElement) TABLE.get(i)).getLev(),
						((TableElement) TABLE.get(i)).getAdr()));
			}
		} else if (word.getSym().equals("callsym"))// 如果遇到了call语句
		{
			word = ciFa.GetWord();// 读取一个word，正常应该是一个过程名型的标识符
			if (!word.getSym().equals("ident"))// 如果不是标识符，抛出14号错误
			{
				errorNumber++;
				myError.error(word.getLineNum(), 14);
			} else {
				i = search(word.getContent());// 查找符号表
				if (i == -1) {
					errorNumber++;
					myError.error(word.getLineNum(), 11);
				}// 如果没有找到，抛出11号错误
				else if (((TableElement) TABLE.get(i)).getKind().equals(
						"procedure")) {// 生成call指令，call这个过程
					CODE.addElement(new CodeElement("cal", lev
							- ((TableElement) TABLE.get(i)).getLev(),
							((TableElement) TABLE.get(i + 1)).getAdr()));
				} else {// 如果找到的不是过程名，抛出15号错误
					errorNumber++;
					myError.error(word.getLineNum(), 15);
				}
				word = ciFa.GetWord();
			}
		} else if (word.getSym().equals("ifsym"))// 如果是if语句
		{
			word = ciFa.GetWord();// if后正常是条件，所以执行condition函数
			condition();
			if (word.getSym().equals("thensym")) {
				word = ciFa.GetWord();
			} else {// 如果if后不是then则抛出16号错误!
				errorNumber++;
				myError.error(word.getLineNum(), 16);
			}
			cx = CODE.size();
			cx1 = cx;
			CODE.addElement(new CodeElement("jpc", 0, 0));
			statement(); // if后需要继续判断后边的语句
			((CodeElement) CODE.get(cx1)).setA(CODE.size());

		} else if (word.getSym().equals("beginsym")) {
			word = ciFa.GetWord();
			statement(); // begin后需要继续判断后边的语句
			while (word.getSym().equals(";sym")
					|| word.getSym().equals("beginsym")
					|| word.getSym().equals("callsym")
					|| word.getSym().equals("ifsym")
					|| word.getSym().equals("whilesym")) {
				if (word.getSym().equals(";sym")) {
					word = ciFa.GetWord();
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 10);
				}
				statement();
			}
			if (word.getSym().equals("endsym")) {
				word = ciFa.GetWord();
			} else {
				errorNumber++;
				myError.error(word.getLineNum(), 17);
			}
		} else if (word.getSym().equals("whilesym")) {
			cx = CODE.size();
			cx1 = cx;// 记录当前代码分配位置，这是while循环的开始地址
			word = ciFa.GetWord();// 读取word，应该是一个条件表达式
			condition(); // while后边肯定有条件语句
			cx = CODE.size();
			cx2 = cx;// 记录当前代码位置，这是while循环do中语句的开始地址
			CODE.addElement(new CodeElement("jpc", 0, 0));// 生成条件跳转指令，跳转位置暂时填0
			if (word.getSym().equals("dosym"))// 判断是否是do,否则抛出18号错误
			{
				word = ciFa.GetWord();
			} else {
				errorNumber++;
				myError.error(word.getLineNum(), 18);
			}
			statement();// 开始分析do后的语句块
			CODE.addElement(new CodeElement("jmp", 0, cx1));// 跳转到cx1处，即再次进行判断是否进行循环
			((CodeElement) CODE.get(cx2)).setA(CODE.size());// 把刚才跳转暂时填0的位置改成当前位置，完成对循环的处理
		} else if (word.getSym().equals("readsym")) {
			word = ciFa.GetWord();// 如果遇到了“读”语句块，则继续读取word，应该是左括号
			if (word.getSym().equals("(sym")) {
				word = ciFa.GetWord();
				if (word.getSym().equals("ident")) {
					i = search(word.getContent());
					if (i == -1) {
						errorNumber++;
						myError.error(word.getLineNum(), 11);
					} else {
						CODE.addElement(new CodeElement("opr", 0, 16));// 生成16号读指令，从键盘读取数字
						CODE.addElement(new CodeElement("sto", lev
								- ((TableElement) TABLE.get(i)).getLev(),
								((TableElement) TABLE.get(i)).getAdr()));// 生成sto指令，存入指定变量
					}
				}
				word = ciFa.GetWord();

				while (word.getSym().equals(",sym"))// 循环得到read语句的参数，直到该参数后不是逗号为止
				{
					word = ciFa.GetWord();
					if (word.getSym().equals("ident")) {
						i = search(word.getContent());
						if (i == -1) {
							errorNumber++;
							myError.error(word.getLineNum(), 11);
						} else {
							CODE.addElement(new CodeElement("opr", 0, 16));
							CODE.addElement(new CodeElement("sto", lev
									- ((TableElement) TABLE.get(i)).getLev(),
									((TableElement) TABLE.get(i)).getAdr()));
						}
					}
					word = ciFa.GetWord();
				}
				if (!word.getSym().equals(")sym")) {// 如果不是预想的右括号，抛出9号错误
					errorNumber++;
					myError.error(word.getLineNum(), 9);
				}
			} else {// 如果不是左括号，抛出0号错误
				errorNumber++;
				myError.error(word.getLineNum(), 0);
			}
			word = ciFa.GetWord();
		} else if (word.getSym().equals("writesym")) {
			word = ciFa.GetWord();
			if (word.getSym().equals("(sym")) {
				word = ciFa.GetWord();
				expression();
				CODE.addElement(new CodeElement("opr", 0, 14));
				while (word.getSym().equals(",sym")) {
					word = ciFa.GetWord();
					expression();
					CODE.addElement(new CodeElement("opr", 0, 14));
				}

				if (!word.getSym().equals(")sym")) {
					errorNumber++;
					myError.error(word.getLineNum(), 9);
				} else {
					word = ciFa.GetWord();
				}
			}
			CODE.addElement(new CodeElement("opr", 0, 15));
		}
	}

	// 条件的处理
	public void condition() {
		String relop = null;
		if (word.getSym().equals("oddsym")) // 如果是一元运算符
		{
			word = ciFa.GetWord();
			expression();// 对一元运算的表达式进行分析
			CODE.addElement(new CodeElement("opr", 0, 6));// 生成奇偶判断指令
		} else
		// 不是一元运算符则是二元运算符
		{
			expression();// 对左边的表达式进行分析
			relop = word.getSym();// 把二元运算符保存起来
			word = ciFa.GetWord();
			expression();// 对右边的表达式进行分析 然后根据刚才保存的运算符，生成相应判断指令
			if (relop.equals("=sym")) {
				CODE.addElement(new CodeElement("opr", 0, 8));
			} else if (relop.equals("#sym")) {
				CODE.addElement(new CodeElement("opr", 0, 9));
			} else if (relop.equals("<sym")) {
				CODE.addElement(new CodeElement("opr", 0, 10));
			} else if (relop.equals(">=sym")) {
				CODE.addElement(new CodeElement("opr", 0, 11));
			} else if (relop.equals(">sym")) {
				CODE.addElement(new CodeElement("opr", 0, 12));
			} else if (relop.equals("<=sym")) {
				CODE.addElement(new CodeElement("opr", 0, 13));
			} else {// 如果刚才保存的不是逻辑运算符，则抛出20号错误
				errorNumber++;
				myError.error(word.getLineNum(), 20);
			}
		}
	}// 条件分析完毕

	// 表达式的处理
	public void expression() {
		String sign = null; // 保存表达式开头的符号 用于表示正负
		if (word.getSym().equals("+sym") || word.getSym().equals("-sym")) {
			sign = word.getSym();
			word = ciFa.GetWord();// 继续读取一个word,正常应该为表达式的一个项
			term();// 进行项的分析
			if (sign.equals("-sym")) // 如果保存下来的是个负号，则生成一条1号指令，取反运算
			{
				CODE.addElement(new CodeElement("opr", 0, 1));
			}
		} else {// 如果不是正负号开头，就应该是一个表达式的项开头，直接进行项的分析
			term();
		}
		String op = null; // 保存加减运算符
		while (word.getSym().equals("+sym") || word.getSym().equals("-sym"))// 项后应该应该是加运算或者减法运算
		{
			op = word.getSym();
			word = ciFa.GetWord();
			term();// 分析项
			if (op.equals("+sym")) // 项分析完毕后，如果刚才保存的是加号，则生成加法指令
			{
				CODE.addElement(new CodeElement("opr", 0, 2));
			} else
			// 否则生成减法指令
			{
				CODE.addElement(new CodeElement("opr", 0, 3));
			}
		}

	}

	// 项的处理（乘法和除法也属于项）
	public void term() {
		String op = null;
		element();// 每个项都是由一个因子开头，所以直接调用因子分析方法
		while (word.getSym().equals("*sym") || word.getSym().equals("/sym"))// 因子后应该遇到乘号或是除号
		{
			op = word.getSym(); // 把运算符保存在op中(乘法或是除法)
			word = ciFa.GetWord();
			element(); // 运算符后应该是一个因子，进行因子分析
			if (op.equals("*sym")) // 如果刚才保存的运算符为乘号则生成opr 4号乘法指令，
			{
				CODE.addElement(new CodeElement("opr", 0, 4));
			} else
			// 如果刚才保存的不是乘号，则生成除法运算指令
			{
				CODE.addElement(new CodeElement("opr", 0, 5));
			}
		}
	}

	// 因子的分析处理
	public void element() {
		int i;
		while (word.getSym().equals("ident") || word.getSym().equals("number")
				|| word.getSym().equals("(sym"))// 循环处理因子
		{
			if (word.getSym().equals("ident"))// 如果遇到的是标识符，则查符号表
			{
				i = search(word.getContent());
				if (i == -1) // 如果查符号表后返回负值，则表示没有找到，抛出11号错误
				{
					errorNumber++;
					myError.error(word.getLineNum(), 11);
				}
				// 如果返回的不是负值，则表示在符号表中找到了该标识符，getKind返回该标识符的类型
				else if (((TableElement) TABLE.get(i)).getKind().equals(
						"constant"))
				// 如果该标识符为常量,则生成lit指令,把值放到栈顶
				{
					CODE.addElement(new CodeElement("lit", 0,
							((TableElement) TABLE.get(i)).getVal()));
					word = ciFa.GetWord();
				} else if (((TableElement) TABLE.get(i)).getKind().equals(
						"variable")) { // 如果该标识符为变量，则生成lod指令，把变量放到栈顶
					CODE.addElement(new CodeElement("lod", lev
							- ((TableElement) TABLE.get(i)).getLev(),
							((TableElement) TABLE.get(i)).getAdr()));
					word = ciFa.GetWord();
				} else if (((TableElement) TABLE.get(i)).getKind().equals(
						"procedure")) {// 如果该标识符为过程名，出错，抛出21号错误
					errorNumber++;
					myError.error(word.getLineNum(), 21);
					word = ciFa.GetWord();
				}
			} else if (word.getSym().equals("number"))// 如果因子分析时遇到的为数字
			{
				int num = Integer.parseInt(word.getContent());
				if (num > 2047)// 判断数字大小超过允许最大值amax
				{
					errorNumber++;
					myError.error(word.getLineNum(), 7);// 如果数字越界，则抛出7号错误，并把数字按0处理；
					num = 0;
				}
				CODE.addElement(new CodeElement("lit", 0, num));// 生成lit指令，把这个数值字面常量放到栈顶
				word = ciFa.GetWord();
			} else if (word.getSym().equals("(sym"))// 如果因子分析遇到的为左括号
			{
				word = ciFa.GetWord();// 继续读取一个word
				expression(); // 递归调用表达式分析
				if (word.getSym().equals(")sym"))// 表达式分析完后，应该遇到右括号
				{
					word = ciFa.GetWord();
				} else {
					errorNumber++;
					myError.error(word.getLineNum(), 22);
				}// 如果表达式分析完后，没有遇到右括号，则抛出22号错误
			}
		}
	}

	public int getErroNumber() {
		return errorNumber;
	}

	public void printTable() // 输出符号表内容
	{
		for (int t = 1; t < TABLE.size(); t++) {
			TableElement name = (TableElement) TABLE.get(t);
			if (name.getKind().equals("constant"))
				name.showConst();
			else if (name.getKind().equals("variable"))
				name.show();
			else if (name.getKind().equals("procedure")) {
				t++;
				TableElement tempname = (TableElement) TABLE.get(t);
				name.setAdr(tempname.getAdr());
				name.show();
			}
		}
	}
	public void printCode()// 输出生成的目标代码
	{
		for (int n = 0; n < CODE.size(); n++) {
			CodeElement code = (CodeElement) CODE.get(n);
			System.out.println(n + "\t" + code.getF() + "\t" + code.getL()
					+ "\t" + code.getA());
		}
	}

	public Vector getCode(){
		return CODE ;

	}
}
