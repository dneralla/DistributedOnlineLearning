package edu.illinois.ml.learning;

import java.util.List;

public class InstanceVector {
	
	public int labelId ;
	public List<Integer> activeFeatures ;
	public InstanceVector(int labelId, List<Integer> activeFeatures) {
		super();
		this.labelId = labelId;
		this.activeFeatures = activeFeatures;
	}
	
	public InstanceVector(List<Integer> activeFeatures)
	{
		this.activeFeatures=activeFeatures;
	}
	
	public int getLabelId() 
	{
		return labelId;
	}
	public void setLabelId(int labelId) {
		this.labelId = labelId;
	}
	public List<Integer> getActiveFeatures() {
		return activeFeatures;
	}
	public void setActiveFeatures(List<Integer> activeFeatures) {
		this.activeFeatures = activeFeatures;
	}
	
	
	
	

}
