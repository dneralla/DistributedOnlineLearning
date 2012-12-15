package edu.illinois.ml.learning;

public class Feature {
	
	private int featureId;
	private double featureValue;
	
	public Feature(int featureId , double featureValue)
	{
		this.featureId=featureId;
		this.featureValue=featureValue;
	}
	
	public int getFeatureId() {
		return featureId;
	}
	public void setFeatureId(int featureId) {
		this.featureId = featureId;
	}
	public double getFeatureValue() {
		return featureValue;
	}
	public void setFeatureValue(double featureValue) {
		this.featureValue = featureValue;
	}
	
}
