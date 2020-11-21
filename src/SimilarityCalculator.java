
import java.util.ArrayList;
import java.util.List;


// 计算相似度
public class SimilarityCalculator {
	
	// 2 * Sum(length of each tile) / (length of document1 + length of document2)
	public static SimVal calcSimilarity(List<String> s1List, List<String> s2List, ArrayList<MatchVals> tiles, float threshold){
		float similarity = sim(s1List, s2List, tiles);
		boolean suspPlag = false;
		
		if(similarity >= threshold)
			suspPlag = true;
		
		return (new SimVal(similarity, suspPlag));
	}

	private static float sim(List<String> s1List,
			List<String> s2List, ArrayList<MatchVals> tiles) {
		
		/*System.out.println("s1list: ");
		for (String ss: s1List) {
			System.out.print("$" + ss);
		}
		System.out.println();*/
		
		/*System.out.println("s2list: ");
		for (String ss: s2List) {
			System.out.print("$" + ss);
		}
		System.out.println();*/
		
		//System.out.println("coverage(tiles): " + coverage(tiles));
		//System.out.println("s1List.size(): " + s1List.size());
		//System.out.println("s2List.size(): " + s2List.size());
		
		float ans = ((float)(2*coverage(tiles))/(float)(s1List.size()+s2List.size()));
		
		return ans;
	}

	// 计算所有tiles的总个数
	private static int coverage(ArrayList<MatchVals> tiles) {
		int accu = 0;
		boolean[] f1 = new boolean[10000000];
		boolean[] f2 = new boolean[10000000];
		for (MatchVals tile : tiles){
			if (!f1[tile.patternPostion] && !f2[tile.textPosition]) {
				accu += tile.length;
				f1[tile.patternPostion] = true;
				f2[tile.textPosition] = true;
			}
		}	
		return accu;
	}
	
}
