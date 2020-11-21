

import java.util.ArrayList;

// 存储检测结果
public class PlagResult {
	public ArrayList<MatchVals> tiles;
	public float similarity;
	public int id1;
	public int id2;
	public int id1StringLength;
	public int id2StringLength;
	public String algName;
	public String normName;
	public boolean suspectedPlagiarism;
	
	public PlagResult(int id1, int id2){
		this.tiles = new ArrayList<MatchVals>();
		this.similarity = (float) 0.0;
		this.id1 = id1;
		this.id2 = id2;
		this.id1StringLength = Integer.toString(id1).length();
		this.id2StringLength = Integer.toString(id2).length();
		this.algName = "";
		this.normName = "";
		this.suspectedPlagiarism = false;
	}
	
	public void setTiles(ArrayList<MatchVals> tiles){
		if(tiles.getClass() != new ArrayList<MatchVals>().getClass())
			 System.out.println("NoValidArgumentError: tiles must be of type list");
		else
			this.tiles = tiles;
	}
	
	public ArrayList<MatchVals> getTiles(){
		return this.tiles;
	}
	
	public void setSimilarity(float similarity){
        if (! (0 <= similarity) && (similarity <= 1))
            System.out.println("OutOfRangeError: Similarity value should be out of range 0 to 1.0");
        else
            this.similarity = similarity;
	}

	public float getSimilarity(){
		return this.similarity;
	}
	
    public void setIdentifier(int i, int j){
        this.id1 = i;
        this.id2 = j;
    }
	
    public Identifiers getIdentifier(){
        if (this.id1 == 0 || this.id2 == 0)
            System.out.println("NoIdentifierSetError: One or both identifier were not set.");
        return (new Identifiers(this.id1, this.id2));
    }
    
    // 检查id是否是当前结果中的一个identifier
    public boolean containsIdentifier(String id){
        return (id.equals(this.id1) || id.equals(this.id2));
    }
    
    public void setIdStringLength(int id1StringLength, int id2StringLength){
        this.id1StringLength = id1StringLength;
        this.id2StringLength = id2StringLength;
    }
    
    public StringLengths getIdStringLength(){
        return (new StringLengths(this.id1StringLength, this.id2StringLength));
    }
    
    public void setSuspectedPlagiarism(boolean value){
        this.suspectedPlagiarism = value;
	}
    
    public boolean isSuspectPlagiarism(){
        return this.suspectedPlagiarism;
    }
    
    // 设置用于比较的算法id
    public void setAlgorithmName(String algName){
        this.algName = algName;
    }
    
    // 得到用于比较的算法id
    public String getAlgorithmName(){
        return this.algName;
    }
    
    // 设置用于比较的标准id
    public void setNormalizerName(String normName){
        this.normName = normName;
    }
    
    // 得到用于比较的标准id
    public String getNormalizerName(){
        return this.normName;
    }
    
    // 判断该对象是否与other相等
    public boolean __eq__(PlagResult other){
        if (other.equals(null))
            return false;
        else if ((this.getIdentifier().equals(other.getIdentifier())) && (this.getSimilarity() == other.getSimilarity()) && (this.getTiles().equals(other.getTiles())) && (this.getIdStringLength() == other.getIdStringLength()))
            return true;
        return false;
    }
  
    // 判断该对象是否与other不相等
    public boolean __ne__(PlagResult other){
        return (! this.__eq__(other));
    }
    
    // 以指定形式返回结果
    public String __str__(){
    	String val = "PlagResult:\n"
                + " Identifier: " + this.getIdentifier().toString() + '\n'
                + " Similarity: " + this.getSimilarity() + '\n'
                + " Tiles: " + this.getTiles() + "\n"
                + " supected Plagiarism: " + this.isSuspectPlagiarism() + '\n';
        return val;
    }
  
    // 以指定形式返回结果
    public String __repr__(){
        return this.getIdentifier().toString()+" "+this.getSimilarity()+" "+
                this.getTiles()+" "+this.isSuspectPlagiarism();
    }
}
