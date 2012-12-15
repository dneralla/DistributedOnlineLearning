package edu.illinois.ml.learning;

import java.util.ArrayList;
import java.util.List;

public class FeatureExtractor {
	
	public static InstanceVector getFeatures(String instance)
	{
     
		assert(instance!=null);
		String [] instanceSplit = instance.split(" ");
		int labelId = Integer.parseInt(instanceSplit[0]);
		List<Integer> activeFeatures = new ArrayList<Integer>();
		
		for(int i=1;i<instanceSplit.length;i++)
		{
			String featureSplit[] = instanceSplit[i].split(":");
 			activeFeatures.add(Integer.parseInt(featureSplit[0]));
		}
	LabelLib.storeLabel(labelId);	
    
	return new InstanceVector(labelId,activeFeatures);
	}

}
