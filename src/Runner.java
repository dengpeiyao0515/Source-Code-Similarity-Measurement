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
	
	
	 // 计算字符串s1和s2的匹配情况
	public static PlagResult CompareWithGST(String s1, String s2, int mML, float threshold) {
		// mML：最小匹配长度
		// threshold：取值0.0~1.0，大于这个值则认为剽窃
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

		// 计算Tiles
		tiles = RKR_GST(s1, s2, mML, 20);

		// 计算相似度
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

	 // 计算Running-Karp-Rabin-Greedy-String-Tiling
	public static ArrayList<MatchVals> RKR_GST(String P, String T, 
			int minimalMatchingLength, int initsearchSize) {
		// P：模式串  T：目标串
		if (minimalMatchingLength < 1)
			minimalMatchingLength = 3;

		if (initsearchSize < 5)
			initsearchSize = 20;

		int s = 0;
		// "\\s+"可匹配一个或多个空白字符
		// "\\w+"可匹配一个或多个字母、数字、下划线
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

	 // 搜索最大匹配
	public static int scanpattern(int s, String[] P, String[] T) {
		// 扫描模式串P和目标串T以便进行下一步匹配
		//  如果找到的匹配项的长度是搜索长度的两倍，则该大小为s返回，用它重新启动scanpattern
		//  所有找到的匹配项都存储在队列中的匹配项列表中
		int longestMaxMatch = 0;
		Queue<MatchVals> queue = new LinkedList<MatchVals>();
		GSTHashTable hashtable = new GSTHashTable();
		/**
		 * 从T中的第一个没有标记的token开始，对每一个没有标记的token Tt
		 * 如果距离下一个tile的距离<=s，则将t推进到下一个tile之后的第一个未标记的标记
		 * 否则，为子串Tt到Tt+s-1建立KR-hash值，并且加入到哈希表中
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
		 * 从P中的第一个未标记的token开始，对于每一个未标记的Pp
		 * 如果到下一个tile的距离<=s，则将p推进到下一个tile之后的第一个未标记的标记
		 * 否则，为字串Pp到Pp+s-1建立KR哈希值
		 * 在哈希表中查找和该KR哈希值相等的哈希表的入口
		 * 如果对于所有的j（取值0~s-1），Pp+j=Tt+j，那么k=s
		 * 当Pp+k=Tt+k并且Pp+k和Tt+k均没有标记时，执行k=k+1
		 * 如果k>2*s，返回k；否则记录最大匹配
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

	 // 创建并返回substring的KR哈希值
	private static int createKRHashValue(String substring) {
		int hashValue = 0;
		for (int i = 0; i < substring.length(); i++)
			hashValue = ((hashValue << 1) + (int) substring.charAt(i));
		return hashValue;
	}

	 // 判断串string是否被标记
	private static boolean isUnmarked(String string) {
		if (string.length() > 0 && string.charAt(0) != '*')
			return true;
		else
			return false;
	}

	private static boolean isMarked(String string) {
		return (!isUnmarked(string));
	}

	 //标记字符串，即在字符串前面加*
	private static String markToken(String string) {
		StringBuilder sb = new StringBuilder();
		sb.append("*");
		sb.append(string);
		return sb.toString();
	}

	 // 判断在tiles列表中，match是否已经堵塞
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

	// 计算到下一个tile的距离（即到下一个被标记的token的距离）
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

	// 计算tile后的一个未标记token的首位置
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
	
	// 获取AST，遍历AST并存储节点信息
	public static void Init() throws Exception {
		//System.out.println("------------------------------------------");
		int i, j;
		// 文件读入
        try (FileReader reader = new FileReader(inputFile);
        		 // 建立一个对象，它把文件内容转成计算机能读懂的语言
        		BufferedReader br = new BufferedReader(reader)
           ) {
        	   //System.out.println("程序1：");
               String line;
               while ((line = br.readLine()) != null) {
                   //System.out.println(line);
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
		is = new FileInputStream(inputFile);
		
		// 词法分析+语法分析生成语法树
		// 新建一个CharStream	
		input = new ANTLRInputStream(is);  	
		//System.out.println(input);
		// 新建一个词法分析器，处理输入的CharSteam
		lexer = new CPP14Lexer(input);  
		// 新建一个词法符号的缓冲区，用于存储词法分析器将生成的词法符号
		tokens = new CommonTokenStream(lexer); 
		// 新建一个语法分析器，处理词法符号缓冲区中的内容
		parser = new CPP14Parser(tokens);
		// 从translationunit规则开始语法分析
		tree = parser.translationunit(); 
		
		code = tree.toStringTree(parser);
		// 用LISP风格打印生成的树
		//System.out.println("解析树:\n" + code);  
		
		// 新建一个标准的ANTLR语法分析树遍历器
		walker = new ParseTreeWalker();
				
		// 新建一个监听器，将其传递给遍历器
		loader = new CPP14Loader();
		walker.walk(loader, tree); // 遍历语法分析树
		ans = loader.list;
		
		// 输出语法树1节点信息
		/*System.out.println("----------------------语法树节点信息-----------------------");
		System.out.println("      节点类型                                              Hash值");
		for (i = 0; i < ans.size(); i ++) {
			System.out.println("子节点数为"+i+"的节点:");
			Vector<CPP14Loader.ASTNode> v = ans.get(i);
			for (j = 0; j < v.size(); j ++) {
				CPP14Loader.ASTNode node = v.get(j);
				System.out.println(node.type + "       " + node.hash);
			}
		}
		System.out.println("--------------------------------------------------------");*/
				
		//System.out.println("------------------------------------------");
		
		// 文件读入
        try (FileReader reader = new FileReader(inputFile1);
        		BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
           ) {
        	   //System.out.println("程序2：");
               String line;
               while ((line = br.readLine()) != null) {
                   //System.out.println(line);
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
		is1 = new FileInputStream(inputFile1);
		
		// 词法分析+语法分析生成语法树
		// 新建一个CharStream
		input1 = new ANTLRInputStream(is1);  		
		//System.out.println(input1);     
		// 新建一个词法分析器，处理输入的CharSteam
		lexer1 = new CPP14Lexer(input1);  
		// 新建一个词法符号的缓冲区，用于存储词法分析器将生成的词法符号
		tokens1 = new CommonTokenStream(lexer1);
		// 新建一个语法分析器，处理词法符号缓冲区中的内容
		parser1 = new CPP14Parser(tokens1); 
		// 从translationunit规则开始语法分析
		tree1 = parser1.translationunit();  
		
		code1 = tree1.toStringTree(parser1);
		// 用LISP风格打印生成的树
		//System.out.println("解析树:\n" + code1);  
		
		// 新建一个标准的ANTLR语法分析树遍历器
		walker1 = new ParseTreeWalker();
				
		// 新建一个监听器，将其传递给遍历器
		loader1 = new CPP14Loader();
		walker.walk(loader1, tree1); // 遍历语法分析树
		ans1 = loader1.list;
		
		// 输出语法树2节点信息
		/*System.out.println("----------------------语法树节点信息-----------------------");
		System.out.println("      节点类型                                              Hash值");
		for (i = 0; i < ans1.size(); i ++) {
			System.out.println("子节点数为"+i+"的节点:");
			Vector<CPP14Loader.ASTNode> v = ans1.get(i);
			for (j = 0; j < v.size(); j ++) {
				CPP14Loader.ASTNode node = v.get(j);
				System.out.println(node.type + "       " + node.hash);
			}
		}
		System.out.println("--------------------------------------------------------");*/
	}
	
	// 语法树节点相似度比对
	private static void CompareOfTreeNode() {
		int i, j, t, f, row, col, ii;
		
		// 记录节点总个数
		NodeSum = 0;
		NodeSum1 = 0;
		for (t = MinChildNode; t < 20; t ++) {
			Vector<CPP14Loader.ASTNode> v = ans.get(t);
			Vector<CPP14Loader.ASTNode> v1 = ans1.get(t);
			NodeSum += v.size();
			NodeSum1 += v1.size();
		}
		
		// 初始化相似文本标记数组
		for (i = 0; i < 1000; i ++) {
			for (j = 0; j < 100; j ++) {
				sim[i][j] = false;
				sim1[i][j] = false;
			}
		}
			
		// 计算相似节点个数
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
					// 两个节点的类型和Hash值相等则认为相似
					if (node.hash == node1.hash && node.type.equals(node1.type)) {
						// 一个节点和多个节点相似，只计1次相似
						if (x[j] == false) {
							sum ++;
							x[j] = true;
						}
						
						// TODO 记录相似文本位置
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
					//System.out.println("没匹配上");
				//}
			}
			
			if (sum > v.size())
				sum = v.size();
			sum *= 2;
			NumOfSimNode += sum;
			
		}
		
		/*System.out.println("相似节点对数: " + NumOfSimNode / 2);
		System.out.println("语法树1节点个数: " + NodeSum);
		System.out.println("语法树2节点个数: " + NodeSum1);
		SimOfNode = NumOfSimNode * 2.0 / (NodeSum + NodeSum1);
		System.out.println("相似度：" + SimOfNode);*/
		
		/*System.out.println("-----------------文本1中相似内容----------------");
		try (FileReader reader = new FileReader(inputFile);
       		 // 建立一个对象，它把文件内容转成计算机能读懂的语言
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
	
	
		/*System.out.println("-----------------文本2中相似内容----------------");
		try (FileReader reader = new FileReader(inputFile1);
       		 // 建立一个对象，它把文件内容转成计算机能读懂的语言
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
		// 预处理：修改文法，忽略注释、空白符（->skip）
		// 将while语句与for语句等价?
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
				//System.out.print(inputFile + "与" + inputFile1 + "相似度计算结果： ");
				
				// 获取AST，遍历AST并存储节点信息
				Init();
				
				// GST算法比对
				code1 = code1.replace("(", "");
				code1 = code1.replace(")", "");
				code = code.replace("(", "");
				code = code.replace(")", "");
				
				//CompareWithGST(code, code1, 1,(float)0.6);
				
				// 设置阈值
				MinChildNode = 1;
				
				// 相似节点总数
				NumOfSimNode = 0;
				
				// 语法树节点相似度比对
				CompareOfTreeNode();	
				
				//System.out.println("      相似度1: "+ result.getSimilarity());
				
				//System.out.println("相似节点对数: " + NumOfSimNode / 2);
				//System.out.println("语法树1节点个数: " + NodeSum);
				//System.out.println("语法树2节点个数: " + NodeSum1);
			
				SimOfNode = NumOfSimNode * 2.0 / (NodeSum + NodeSum1);
				//System.out.println(inputFile + "与" + inputFile1 + "相似度计算结果：" + SimOfNode);
				long SimOfNode1 = (long)(SimOfNode * 100);
				if (SimOfNode1 > 100)
					SimOfNode1 = 100;
				//System.out.println(SimOfNode1 + " %");
				System.out.println(SimOfNode1);
				//System.out.println("      相似度2: " + SimOfNode);
				
				//System.out.println("------------------------------------------");
			//}
			//System.out.println();
		//}
		
		
	}

}
