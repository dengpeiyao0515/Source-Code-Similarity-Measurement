/**
 * [@1,6:10='parrt',<2>,1:6]
 *    ����ʷ�����λ�ڵڶ���λ�ã���0��ʼ��������@x������AST�нڵ�ı��Ϊx����
 *    �������ı��ĵ�6������10��λ��֮����ַ���ɣ�������6���͵�10����ͬ����0��ʼ������
 *    �������ı�������parrt
 *    �ʷ�������2����token��
 *    λ�������ı��ĵ�һ�С���6��λ�ô�����0��ʼ������tab���ű�����һ���ַ���
 */


import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CPP14Loader extends CPP14BaseListener{
	// TODO��������ƽڵ���㷽��
	// �洢ÿһ���ڵ����Ϣ:�ڵ����͡�Hashֵ���ӽڵ��������ڵ��Ӧ�ı�λ��
	// ���ƽڵ������׼��Hashֵ���ڵ����͡��ӽڵ�����
	// ���ӽڵ���������Ƚϣ�Hashֵ�ͽڵ�������ͬ����Ϊ����
	
	// TODO:�о�һ��Hashֵ���㷽�����ӽڵ�Hashֵ�����ôʵ��
	// Ϊ�﷨��ÿ�ֽڵ�������������ֵ
	// ����ջ��exitһ���ڵ�ʱ������ýڵ�Hashֵ��
	// 		�ýڵ�����ֵ+�ӽڵ�Hashֵ�����Եõ��ýڵ���ӽڵ���count����ջ��ȡcount��ֵ������ӣ�
	//		���������Hashֵ�ٴ���ջ��
	
	// TODO��������ݴ洢
	// �洢ÿһ���ڵ����Ϣ:�ڵ����͡�Hashֵ���ӽڵ��������ڵ��Ӧ�ı�λ��
	// ���ӽڵ���������
	// ע��: Ҷ�ӽڵ㲻�洢Ҳ���ȶ�
	
	// TODO:��Ʊȶ��㷨
	// ������ֵ�������ӽڵ������ڸ���ֵ�Ľڵ���бȽ�
	// ����ͬ�ӽڵ������Ľڵ�Ľڵ����͡�Hashֵ��ͬ������Ϊ�������ڵ����ƣ��洢�ýڵ��Ӧ�ı�λ��
	
	class ASTNode{
		long hash; //��ǰ�ڵ�Hashֵ
		int childcount; // ��ǰ�ڵ��ӽڵ���
		String type; // ��ǰ�ڵ�����
		int StartRow, StartCol, StopRow, StopCol; // ��Ӧ�ı���ֹλ�� 
	}
	Stack<Long> st = new Stack<Long>(); // �洢�ӽڵ�Hashֵ
	List<Vector<ASTNode>> list = new Vector<Vector<ASTNode>>();
	String text; // ��¼Ԥ�������ı�
	
	public CPP14Loader() {
		int i;
		for (i = 0; i < 20; i ++) {
			Vector<ASTNode> tmp = new Vector<ASTNode>();
			list.add(tmp);
		}
		//System.out.println(list.size());
	}
	
	// ����Hashֵ
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
		// �洢�ڵ���Ϣ
		ASTNode Node = new ASTNode();
		Node.childcount = ctx.getChildCount();
		String s = ctx.getClass().toString();
		Node.type = s.substring(18, s.length() - 7);
		//System.out.println(Node.type + "     " + ComputeHash(Node.type));
		Node.StartRow = ctx.getStart().getLine();
		Node.StartCol = ctx.getStart().getCharPositionInLine();
		Node.StopRow = ctx.getStop().getLine();
		Node.StopCol = ctx.getStop().getCharPositionInLine();
		
		// ����Hashֵ
		int i;
		Node.hash = ComputeHash(Node.type);
		for (i = 0; i < Node.childcount; i ++) {
			Node.hash += st.pop();
		}
		st.push(Node.hash);
		
		// �ڵ���Ϣ���ӽڵ���������뼯��
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
