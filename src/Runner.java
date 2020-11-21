import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Runner {
	static String[] test = {"1.cpp", "2.cpp", "3.cpp", "4.cpp", "5.cpp", "6.cpp",
			"7.cpp", "8.cpp", "9.cpp", "10.cpp", "11.cpp", "12.cpp", "13.cpp"};
	
	static String code, code1;
	static String inputFile, inputFile1;
	static InputStream is, is1;
	static ANTLRInputStream input, input1;
	static CPP14Lexer lexer, lexer1;
	static CommonTokenStream tokens, tokens1;
	static CPP14Parser parser, parser1;
	static ParseTree tree, tree1;
	static ParseTreeWalker walker, walker1;
	static CPP14Loader loader, loader1;
	static List<Vector<CPP14Loader.ASTNode>> ans, ans1;
	static int MinChildNode;
	static int NumOfSimNode;
	static int NodeSum, NodeSum1;
	static double SimOfNode;
	static PlagResult result;
	
	static boolean [][]sim;
	static boolean [][]sim1;
	
	public static ArrayList<MatchVals> tiles;
	public static ArrayList<Queue<MatchVals>> matchList;
	
	
	 // �����ַ���s1��s2��ƥ�����
	public static PlagResult CompareWithGST(String s1, String s2, int mML, float threshold) {
		// mML����Сƥ�䳤��
		// threshold��ȡֵ0.0~1.0���������ֵ����Ϊ����
		if (mML < 1)
			System.err
			.println("OutOfRangeError: minimum Matching Length mML needs to be greater than 0");
		if (!((0 <= threshold) && (threshold <= 1)))
			System.err
			.println("OutOfRangeError: treshold t needs to be 0<=t<=1");
		if (s1.isEmpty() || s2.isEmpty())
			System.err
			.println("NoValidArgumentError: input must be of type string not None");
		if (s1.equals("") || s2.equals(""))
			System.err
			.println("NoValidArgumentError: input must be of type string not None");

		// ����Tiles
		tiles = RKR_GST(s1, s2, mML, 20);

		// �������ƶ�
		/*SimVal simResult = SimilarityCalculator.calcSimilarity(
				Arrays.asList(s1.split("[\\s+|\\W+]")), Arrays.asList(s2.split("\\s+|\\W+")),
				tiles, threshold);*/
		SimVal simResult = SimilarityCalculator.calcSimilarity(
				Arrays.asList(s1.split("[\\s+]")), Arrays.asList(s2.split("[\\s+]")),
				tiles, threshold);
		
		float similarity = simResult.similarity;
		//if (similarity >= 0.992429)
			//similarity = 1;

		// Create Plagiarism result and set attributes
		result = new PlagResult(0, 0);
		result.setIdentifier(createKRHashValue(s1), createKRHashValue(s2));
		result.setTiles(tiles);
		result.setSimilarity(similarity);
		result.setSuspectedPlagiarism(simResult.suspPlag);


		//System.out.println("Identifiers: "+result.getIdentifier().id1+":"+result.getIdentifier().id2);
		//System.out.println("Similarity: "+result.getSimilarity());
		/*System.out.print("Plagiriasm tiles: ");
		for(MatchVals tiles:result.getTiles()){
			System.out.print("("+tiles.patternPostion+",");
			System.out.print(tiles.textPosition+",");
			System.out.print(tiles.length+")");
		}*/
		//System.out.println("Suspected Plagirism: "+result.suspectedPlagiarism);
		
		return result;
	}

	 // ����Running-Karp-Rabin-Greedy-String-Tiling
	public static ArrayList<MatchVals> RKR_GST(String P, String T, 
			int minimalMatchingLength, int initsearchSize) {
		// P��ģʽ��  T��Ŀ�괮
		if (minimalMatchingLength < 1)
			minimalMatchingLength = 3;

		if (initsearchSize < 5)
			initsearchSize = 20;

		int s = 0;
		// "\\s+"��ƥ��һ�������հ��ַ�
		// "\\w+"��ƥ��һ��������ĸ�����֡��»���
		//String[] PList = P.split("[\\s+|\\W+]");
		//String[] TList = T.split("[\\s+|\\W+]");
		String[] PList = P.split("[\\s+]");
		String[] TList = T.split("[\\s+]");

		s = initsearchSize;
		boolean stop = false;

		while (!stop) {
			// Lmax is size of largest maximal-matches from this scan
			int Lmax = scanpattern(s, PList, TList);
			// if very long string no tiles marked. Iterate with larger s
			if (Lmax > 2 * s)
				s = Lmax;
			else {
				markStrings(s, PList, TList);
				if (s > (2 * minimalMatchingLength))
					s = s/2;
				else if (s > minimalMatchingLength)
					s = minimalMatchingLength;
				else
					stop = true;
			}
		}
		return tiles;
	}

	 // �������ƥ��
	public static int scanpattern(int s, String[] P, String[] T) {
		// ɨ��ģʽ��P��Ŀ�괮T�Ա������һ��ƥ��
		//  ����ҵ���ƥ����ĳ������������ȵ���������ô�СΪs���أ�������������scanpattern
		//  �����ҵ���ƥ����洢�ڶ����е�ƥ�����б���
		int longestMaxMatch = 0;
		Queue<MatchVals> queue = new LinkedList<MatchVals>();
		GSTHashTable hashtable = new GSTHashTable();
		/**
		 * ��T�еĵ�һ��û�б�ǵ�token��ʼ����ÿһ��û�б�ǵ�token Tt
		 * ���������һ��tile�ľ���<=s����t�ƽ�����һ��tile֮��ĵ�һ��δ��ǵı��
		 * ����Ϊ�Ӵ�Tt��Tt+s-1����KR-hashֵ�����Ҽ��뵽��ϣ����
		 */
		int t = 0;
		boolean noNextTile = false;
		int h;
		while (t < T.length) {
			if (isMarked(T[t])) {
				t = t+1;
				continue;
			}

			int dist;
			if(distToNextTile(t, T) instanceof Integer)
				dist = (int)distToNextTile(t, T);
			else{
				dist = 0;
				dist = T.length - t;
				noNextTile = true;
			}
			//int dist = distToNextTile(t, T);
			// No next tile found

			if (dist < s) {
				if (noNextTile)
					t = T.length;
				else {
					if(jumpToNextUnmarkedTokenAfterTile(t, T) instanceof Integer)
						t = (int)jumpToNextUnmarkedTokenAfterTile(t, T);
					else
						t = T.length;
				}
			} else {
				StringBuilder sb = new StringBuilder();

				for (int i = t; i <= t + s-1; i++)
					sb.append(T[i]);
				String substring = sb.toString();
				h = createKRHashValue(substring);
				hashtable.add(h, t);
				t = t+1;
			}
		}

		/**
		 * ��P�еĵ�һ��δ��ǵ�token��ʼ������ÿһ��δ��ǵ�Pp
		 * �������һ��tile�ľ���<=s����p�ƽ�����һ��tile֮��ĵ�һ��δ��ǵı��
		 * ����Ϊ�ִ�Pp��Pp+s-1����KR��ϣֵ
		 * �ڹ�ϣ���в��Һ͸�KR��ϣֵ��ȵĹ�ϣ������
		 * ����������е�j��ȡֵ0~s-1����Pp+j=Tt+j����ôk=s
		 * ��Pp+k=Tt+k����Pp+k��Tt+k��û�б��ʱ��ִ��k=k+1
		 * ���k>2*s������k�������¼���ƥ��
		 */
		noNextTile = false;
		int p = 0;
		while (p < P.length) {
			if (isMarked(P[p])) {
				p = p + 1;
				continue;
			}

			int dist;

			if(distToNextTile(p, P) instanceof Integer){
				dist = (int)distToNextTile(p, P);
			}
			else{
				dist = 0;
				dist = P.length - p;
				noNextTile = true;
			}

			if (dist < s) {
				if (noNextTile)
					p = P.length;
				else {

					if(jumpToNextUnmarkedTokenAfterTile(p, P) instanceof Integer)
						p = (int)jumpToNextUnmarkedTokenAfterTile(p, P);
					else{
						p = 0;
						p = P.length;
					}
				}
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = p; i <= p + s-1; i++) {
					sb.append(P[i]);
				}
				String substring = sb.toString();
				h = createKRHashValue(substring);
				ArrayList<Integer> values = hashtable.get(h);
				if (values != null) {
					for (Integer val : values) {
						StringBuilder newsb = new StringBuilder();
						for (int i = val; i <= val + s-1; i++) {
							newsb.append(T[i]);
						}
						if (newsb.toString().equals(substring)) {
							t = val;
							int k = s;

							while (p + k < P.length && t + k < T.length
									&& P[p + k].equals(T[t + k])
									&& isUnmarked(P[p + k])
									&& isUnmarked(T[t + k]))
								k = k + 1;

							if (k > 2 * s)
								return k;
							else {
								if (longestMaxMatch < s)
									longestMaxMatch = s;
								MatchVals mv = new MatchVals(p, t, k);
								queue.add(mv);
							}
						}
					}
				}
				p += 1;
			}

		}
		if (!queue.isEmpty()){
			matchList.add(queue);
		}
		return longestMaxMatch;
	}

	private static void markStrings(int s, String[] P, String[] T) {
		for(Queue<MatchVals> queue:matchList){
			while (!queue.isEmpty()) {
				MatchVals match = queue.poll();
				if (!isOccluded(match, tiles)) {
					for (int j = 0; j < match.length; j++) {
						P[match.patternPostion + j] = markToken(P[match.patternPostion + j]);
						T[match.textPosition + j] = markToken(T[match.textPosition + j]);
					}
					tiles.add(match);
				}
			}
		}
		matchList = new ArrayList<Queue<MatchVals>>(); 
	}

	 // ����������substring��KR��ϣֵ
	private static int createKRHashValue(String substring) {
		int hashValue = 0;
		for (int i = 0; i < substring.length(); i++)
			hashValue = ((hashValue << 1) + (int) substring.charAt(i));
		return hashValue;
	}

	 // �жϴ�string�Ƿ񱻱��
	private static boolean isUnmarked(String string) {
		if (string.length() > 0 && string.charAt(0) != '*')
			return true;
		else
			return false;
	}

	private static boolean isMarked(String string) {
		return (!isUnmarked(string));
	}

	 //����ַ����������ַ���ǰ���*
	private static String markToken(String string) {
		StringBuilder sb = new StringBuilder();
		sb.append("*");
		sb.append(string);
		return sb.toString();
	}

	 // �ж���tiles�б��У�match�Ƿ��Ѿ�����
	private static boolean isOccluded(MatchVals match, ArrayList<MatchVals> tiles) {
		if(tiles.equals(null) || tiles == null || tiles.size() == 0)
			return false;
		for (MatchVals matches : tiles) {
			if ((matches.patternPostion + matches.length == match.patternPostion
					+ match.length)
					&& (matches.textPosition + matches.length == match.textPosition
					+ match.length))
				return true;
		}
		return false;
	}

	// ���㵽��һ��tile�ľ��루������һ������ǵ�token�ľ��룩
	private static Object distToNextTile(int pos, String[] stringList) {
		if (pos == stringList.length)
			return null;
		int dist = 0;
		while (pos+dist+1<stringList.length && isUnmarked(stringList[pos+dist+1]))
			dist += 1;
		if (pos+dist+1 == stringList.length) 
			return null;
		return dist+1;
	}

	// ����tile���һ��δ���token����λ��
	private static Object jumpToNextUnmarkedTokenAfterTile(int pos, String[] stringList) {
		Object dist = distToNextTile(pos, stringList);
		if(dist instanceof Integer)
			pos = pos+ (int)dist;
		else
			return null;
		while (pos+1<stringList.length && (isMarked(stringList[pos+1])))
			pos = pos+1;
		if (pos+1> stringList.length-1) 
			return null;
		return pos+1;
	}
	
	// ��ȡAST������AST���洢�ڵ���Ϣ
	public static void Init() throws Exception {
		//System.out.println("------------------------------------------");
		int i, j;
		// �ļ�����
        try (FileReader reader = new FileReader(inputFile);
        		 // ����һ�����������ļ�����ת�ɼ�����ܶ���������
        		BufferedReader br = new BufferedReader(reader)
           ) {
        	   //System.out.println("����1��");
               String line;
               while ((line = br.readLine()) != null) {
                   //System.out.println(line);
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
		is = new FileInputStream(inputFile);
		
		// �ʷ�����+�﷨���������﷨��
		// �½�һ��CharStream	
		input = new ANTLRInputStream(is);  	
		//System.out.println(input);
		// �½�һ���ʷ������������������CharSteam
		lexer = new CPP14Lexer(input);  
		// �½�һ���ʷ����ŵĻ����������ڴ洢�ʷ������������ɵĴʷ�����
		tokens = new CommonTokenStream(lexer); 
		// �½�һ���﷨������������ʷ����Ż������е�����
		parser = new CPP14Parser(tokens);
		// ��translationunit����ʼ�﷨����
		tree = parser.translationunit(); 
		
		code = tree.toStringTree(parser);
		// ��LISP����ӡ���ɵ���
		//System.out.println("������:\n" + code);  
		
		// �½�һ����׼��ANTLR�﷨������������
		walker = new ParseTreeWalker();
				
		// �½�һ�������������䴫�ݸ�������
		loader = new CPP14Loader();
		walker.walk(loader, tree); // �����﷨������
		ans = loader.list;
		
		// ����﷨��1�ڵ���Ϣ
		/*System.out.println("----------------------�﷨���ڵ���Ϣ-----------------------");
		System.out.println("      �ڵ�����                                              Hashֵ");
		for (i = 0; i < ans.size(); i ++) {
			System.out.println("�ӽڵ���Ϊ"+i+"�Ľڵ�:");
			Vector<CPP14Loader.ASTNode> v = ans.get(i);
			for (j = 0; j < v.size(); j ++) {
				CPP14Loader.ASTNode node = v.get(j);
				System.out.println(node.type + "       " + node.hash);
			}
		}
		System.out.println("--------------------------------------------------------");*/
				
		//System.out.println("------------------------------------------");
		
		// �ļ�����
        try (FileReader reader = new FileReader(inputFile1);
        		BufferedReader br = new BufferedReader(reader) // ����һ�����������ļ�����ת�ɼ�����ܶ���������
           ) {
        	   //System.out.println("����2��");
               String line;
               while ((line = br.readLine()) != null) {
                   //System.out.println(line);
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
		is1 = new FileInputStream(inputFile1);
		
		// �ʷ�����+�﷨���������﷨��
		// �½�һ��CharStream
		input1 = new ANTLRInputStream(is1);  		
		//System.out.println(input1);     
		// �½�һ���ʷ������������������CharSteam
		lexer1 = new CPP14Lexer(input1);  
		// �½�һ���ʷ����ŵĻ����������ڴ洢�ʷ������������ɵĴʷ�����
		tokens1 = new CommonTokenStream(lexer1);
		// �½�һ���﷨������������ʷ����Ż������е�����
		parser1 = new CPP14Parser(tokens1); 
		// ��translationunit����ʼ�﷨����
		tree1 = parser1.translationunit();  
		
		code1 = tree1.toStringTree(parser1);
		// ��LISP����ӡ���ɵ���
		//System.out.println("������:\n" + code1);  
		
		// �½�һ����׼��ANTLR�﷨������������
		walker1 = new ParseTreeWalker();
				
		// �½�һ�������������䴫�ݸ�������
		loader1 = new CPP14Loader();
		walker.walk(loader1, tree1); // �����﷨������
		ans1 = loader1.list;
		
		// ����﷨��2�ڵ���Ϣ
		/*System.out.println("----------------------�﷨���ڵ���Ϣ-----------------------");
		System.out.println("      �ڵ�����                                              Hashֵ");
		for (i = 0; i < ans1.size(); i ++) {
			System.out.println("�ӽڵ���Ϊ"+i+"�Ľڵ�:");
			Vector<CPP14Loader.ASTNode> v = ans1.get(i);
			for (j = 0; j < v.size(); j ++) {
				CPP14Loader.ASTNode node = v.get(j);
				System.out.println(node.type + "       " + node.hash);
			}
		}
		System.out.println("--------------------------------------------------------");*/
	}
	
	// �﷨���ڵ����ƶȱȶ�
	private static void CompareOfTreeNode() {
		int i, j, t, f, row, col, ii;
		
		// ��¼�ڵ��ܸ���
		NodeSum = 0;
		NodeSum1 = 0;
		for (t = MinChildNode; t < 20; t ++) {
			Vector<CPP14Loader.ASTNode> v = ans.get(t);
			Vector<CPP14Loader.ASTNode> v1 = ans1.get(t);
			NodeSum += v.size();
			NodeSum1 += v1.size();
		}
		
		// ��ʼ�������ı��������
		for (i = 0; i < 1000; i ++) {
			for (j = 0; j < 100; j ++) {
				sim[i][j] = false;
				sim1[i][j] = false;
			}
		}
			
		// �������ƽڵ����
		for (t = MinChildNode; t < 20; t ++) {
			Vector<CPP14Loader.ASTNode> v = ans.get(t);
			Vector<CPP14Loader.ASTNode> v1 = ans1.get(t);
			NodeSum += v.size();
			NodeSum1 += v1.size();
			
			boolean[] x = new boolean[v1.size() + 10];
			for (i = 0; i < v1.size() + 5; i ++)
				x[i] = false;
			
			int sum = 0;
			
			for (i = 0; i < v.size(); i ++) {
				f = 0;
				for (j = 0; j < v1.size(); j ++) {
					CPP14Loader.ASTNode node, node1;
					node = v.get(i);
					node1 = v1.get(j);
					// �����ڵ�����ͺ�Hashֵ�������Ϊ����
					if (node.hash == node1.hash && node.type.equals(node1.type)) {
						// һ���ڵ�Ͷ���ڵ����ƣ�ֻ��1������
						if (x[j] == false) {
							sum ++;
							x[j] = true;
						}
						
						// TODO ��¼�����ı�λ��
						row = node.StartRow;
						col = node.StartCol;
						while(row < node.StopRow) {
							sim[row][col] = true;
							col ++;
							if (col >= 100) {
								row ++;
								col = 0;
							}
						}
						for (ii = 0; ii < node.StopCol; ii ++)
							sim[node.StopRow][ii] = true;
						
						
						row = node1.StartRow;
						col = node1.StartCol;
						while(row < node1.StopRow) {
							sim1[row][col] = true;
							col ++;
							if (col >= 100) {
								row ++;
								col = 0;
							}
						}
						for (ii = 0; ii < node1.StopCol; ii ++)
							sim1[node1.StopRow][ii] = true;
					}
				}
				//if (f == 0) {
					//System.out.println("ûƥ����");
				//}
			}
			
			if (sum > v.size())
				sum = v.size();
			sum *= 2;
			NumOfSimNode += sum;
			
		}
		
		/*System.out.println("���ƽڵ����: " + NumOfSimNode / 2);
		System.out.println("�﷨��1�ڵ����: " + NodeSum);
		System.out.println("�﷨��2�ڵ����: " + NodeSum1);
		SimOfNode = NumOfSimNode * 2.0 / (NodeSum + NodeSum1);
		System.out.println("���ƶȣ�" + SimOfNode);*/
		
		/*System.out.println("-----------------�ı�1����������----------------");
		try (FileReader reader = new FileReader(inputFile);
       		 // ����һ�����������ļ�����ת�ɼ�����ܶ���������
       		BufferedReader br = new BufferedReader(reader)
          ) {
              String line;
              int r = -1, c;
              while ((line = br.readLine()) != null) {
            	  r ++;
            	  for (c = 0; c < line.length(); c ++) {
            		  if (sim[r][c])
            			  System.out.print(line.charAt(c));
            		  else
            			  System.out.print(" ");
            	  }
            	  System.out.println();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
		System.out.println("----------------------------------------------");*/
	
	
		/*System.out.println("-----------------�ı�2����������----------------");
		try (FileReader reader = new FileReader(inputFile1);
       		 // ����һ�����������ļ�����ת�ɼ�����ܶ���������
       		BufferedReader br = new BufferedReader(reader)
          ) {
              String line;
              int r = -1, c;
              while ((line = br.readLine()) != null) {
            	  r ++;
            	  for (c = 0; c < line.length(); c ++) {
            		  if (sim1[r][c])
            			  System.out.print(line.charAt(c));
            		  else
            			  System.out.print(" ");
            	  }
            	  System.out.println();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
		System.out.println("----------------------------------------------");*/
	
	}
	
	public static void main(String[] args) throws Exception {
		// Ԥ�����޸��ķ�������ע�͡��հ׷���->skip��
		// ��while�����for���ȼ�?
		int i, j;
		//for (i = 1; i <= 12; i ++) {
			//for (j = i + 1; j <= 12; j ++) {
				//inputFile = "1.cpp";
				//inputFile = test[i];
				//inputFile1 = test[j];
				//inputFile1 = "6.cpp";
				
				inputFile = args[0];
				inputFile1 = args[1];
				
				sim = new boolean[1000][100];
				sim1 = new boolean[1000][100];
				
				tiles = new ArrayList<MatchVals>();
				matchList = new ArrayList<Queue<MatchVals>>();
				
				//System.out.println("----------------" + i + "------------------");
				//System.out.print(inputFile + "��" + inputFile1 + "���ƶȼ������� ");
				
				// ��ȡAST������AST���洢�ڵ���Ϣ
				Init();
				
				// GST�㷨�ȶ�
				code1 = code1.replace("(", "");
				code1 = code1.replace(")", "");
				code = code.replace("(", "");
				code = code.replace(")", "");
				
				//CompareWithGST(code, code1, 1,(float)0.6);
				
				// ������ֵ
				MinChildNode = 1;
				
				// ���ƽڵ�����
				NumOfSimNode = 0;
				
				// �﷨���ڵ����ƶȱȶ�
				CompareOfTreeNode();	
				
				//System.out.println("      ���ƶ�1: "+ result.getSimilarity());
				
				//System.out.println("���ƽڵ����: " + NumOfSimNode / 2);
				//System.out.println("�﷨��1�ڵ����: " + NodeSum);
				//System.out.println("�﷨��2�ڵ����: " + NodeSum1);
			
				SimOfNode = NumOfSimNode * 2.0 / (NodeSum + NodeSum1);
				//System.out.println(inputFile + "��" + inputFile1 + "���ƶȼ�������" + SimOfNode);
				long SimOfNode1 = (long)(SimOfNode * 100);
				if (SimOfNode1 > 100)
					SimOfNode1 = 100;
				//System.out.println(SimOfNode1 + " %");
				System.out.println(SimOfNode1);
				//System.out.println("      ���ƶ�2: " + SimOfNode);
				
				//System.out.println("------------------------------------------");
			//}
			//System.out.println();
		//}
		
		
	}

}
