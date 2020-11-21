
import java.util.ArrayList;
import java.util.HashMap;


// GST算法使用的HASH表
public class GSTHashTable {
	
	HashMap<Long, ArrayList<Integer>> dict;
	
	public GSTHashTable(){
		dict = new HashMap<Long,ArrayList<Integer>>();
	}
	

	// 在关键字h对应的列表中加入obj
	public void add(long h, int obj){
		ArrayList<Integer> newlist;
		if(dict.containsKey(h)){
			newlist = dict.get(h);
			newlist.add(obj);
			dict.put(h, newlist);
		}
		else{
			newlist = new ArrayList<Integer>();
			newlist.add(obj);
			dict.put(h, newlist);
		}
	}
	
	// 返回关键字key对应的list
	public ArrayList<Integer> get(long key){
		if(dict.containsKey(key))
			return dict.get(key);
		else
			return null;
	}
	
	
	// 清空哈希表
	public void clear(){
		dict = new HashMap<Long,ArrayList<Integer>>();
	}
	
}
