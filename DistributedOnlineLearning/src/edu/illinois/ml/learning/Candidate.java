package edu.illinois.ml.learning;
import java.util.List;


public class Candidate {
	
	
	
	private int labelId;
    private List<FeatureWeight> featureList;
	
	public Candidate(List<FeatureWeight> features,int labelId)
	{
	  this.featureList=features ;
	  this.setLabelId(labelId);
	  
	}
	public List <FeatureWeight> getFeatureList() {
		return featureList;
	}
   
   
	public int getLabelId() {
		return labelId;
	}
	public void setLabelId(int labelId) {
		this.labelId = labelId;
	}
    
    public boolean ifExistsFeature(int featureId)
    {
    	
    	for(FeatureWeight fw:this.featureList)
    	    if(fw.getFeatureId()==featureId)
    	        return true;
    	
    	return false;
    	
    }
    
    
    public double getScore(List <Integer> features)
    {
    	double score = 0.0;
    	
    	for(FeatureWeight fw: this.featureList)
    	       if(features.contains(fw.getFeatureId()))
    	    	     score+=fw.getFeatureWeight();
    	
    	
    	return score;
    	
    	
    }
    
    public double getWeightOfFeatureId(int featureId)
    {
    	for (FeatureWeight fw:this.featureList)
    		 if(fw.getFeatureId()==featureId)
                   return fw.getFeatureWeight();
    	return 0.0;
    }              
	

}
