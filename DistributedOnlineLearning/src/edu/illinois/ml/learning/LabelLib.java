package edu.illinois.ml.learning;

import java.util.ArrayList;
import java.util.List;


public class LabelLib {
	 
	   private static List<Integer> candidateLabels = new ArrayList<Integer>();

		public static void storeLabel(int label) {
	            if (candidateLabels.contains(label))
				return;
	            candidateLabels.add(label);
			    }

		public static List<Integer> getCandidateLabels() {
			return candidateLabels;
		}

	}


