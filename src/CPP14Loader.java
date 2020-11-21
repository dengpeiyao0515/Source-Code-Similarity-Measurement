/**
 * [@1,6:10='parrt',<2>,1:6]
 *    这个词法符号位于第二个位置（从0开始计数）（@x代表在AST中节点的编号为x？）
 *    由输入文本的第6个到第10个位置之间的字符组成（包括第6个和第10个，同样从0开始计数）
 *    包含的文本内容是parrt
 *    词法类型是2（见token）
 *    位于输入文本的第一行、第6个位置处（从0开始计数，tab符号被看作一个字符）
 */


import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CPP14Loader extends CPP14BaseListener{
	// TODO：设计相似节点计算方法
	// 存储每一个节点的信息:节点类型、Hash值、子节点数量、节点对应文本位置
	// 相似节点衡量标准：Hash值、节点类型、子节点数量
	// 按子节点数量分组比较，Hash值和节点类型相同则认为相似
	
	// TODO:研究一下Hash值计算方法，子节点Hash值相加怎么实现
	// 为语法树每种节点类型设置类型值
	// 设置栈，exit一个节点时，计算该节点Hash值：
	// 		该节点类型值+子节点Hash值（可以得到该节点的子节点数count，从栈中取count个值出来相加）
	//		将计算完的Hash值再存入栈中
	
	// TODO：设计数据存储
	// 存储每一个节点的信息:节点类型、Hash值、子节点数量、节点对应文本位置
	// 按子节点数量分组
	// 注意: 叶子节点不存储也不比对
	
	// TODO:设计比对算法
	// 设置阈值，对于子节点数大于该阈值的节点进行比较
	// 若相同子节点数量的节点的节点类型、Hash值相同，则认为这两个节点相似，存储该节点对应文本位置
	
	class ASTNode{
		long hash; //当前节点Hash值
		int childcount; // 当前节点子节点数
		String type; // 当前节点类型
		int StartRow, StartCol, StopRow, StopCol; // 对应文本起止位置 
	}
	Stack<Long> st = new Stack<Long>(); // 存储子节点Hash值
	List<Vector<ASTNode>> list = new Vector<Vector<ASTNode>>();
	String text; // 记录预处理后的文本
	
	public CPP14Loader() {
		int i;
		for (i = 0; i < 20; i ++) {
			Vector<ASTNode> tmp = new Vector<ASTNode>();
			list.add(tmp);
		}
		//System.out.println(list.size());
	}
	
	// 计算Hash值
	public long ComputeHash(String x) {
        long h = 0;
        int i;
        for (i = 0; i < x.length(); i++) {
        	h = (long) ((31 * h + x.charAt(i)) % 1e10);
        }
        return h;
	}
	
	@Override public void enterTranslationunit(CPP14Parser.TranslationunitContext ctx) { 
		//System.out.println(ctx.getText());
		text = ctx.getText();
	}
	
	@Override public void exitEveryRule(ParserRuleContext ctx) {
		// 存储节点信息
		ASTNode Node = new ASTNode();
		Node.childcount = ctx.getChildCount();
		String s = ctx.getClass().toString();
		Node.type = s.substring(18, s.length() - 7);
		//System.out.println(Node.type + "     " + ComputeHash(Node.type));
		Node.StartRow = ctx.getStart().getLine();
		Node.StartCol = ctx.getStart().getCharPositionInLine();
		Node.StopRow = ctx.getStop().getLine();
		Node.StopCol = ctx.getStop().getCharPositionInLine();
		
		// 计算Hash值
		int i;
		Node.hash = ComputeHash(Node.type);
		for (i = 0; i < Node.childcount; i ++) {
			Node.hash += st.pop();
		}
		st.push(Node.hash);
		
		// 节点信息按子节点数分组存入集合
		int num = Node.childcount;
		Vector<ASTNode> v = list.get(num);
		v.add(Node);
	}

	@Override public void visitTerminal(TerminalNode node) { 
		String type = node.getClass().toString().substring(32);
		//System.out.println(type);
		long hash = ComputeHash(type);
		st.push(hash);
	}
}
