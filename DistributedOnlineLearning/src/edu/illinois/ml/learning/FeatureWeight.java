package edu.illinois.ml.learning;

public class FeatureWeight {
	
	private int  featureId;
	private double featureWeight;
   
	
	public FeatureWeight(int featureId , double featureWeight)
	{
	  this.featureId=featureId;
	  this.featureWeight=featureWeight;
	  
	}
	public int getFeatureId() {
		return featureId;
	}
	public void setFeatureId(int featureId) {
		this.featureId = featureId;
	}
	public double getFeatureWeight() {
		return featureWeight;
	}
	public void setFeatureWeight(double featureWeight) {
		this.featureWeight = featureWeight;
	}

}
