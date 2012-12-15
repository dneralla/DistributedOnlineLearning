package edu.illinois.ml.learning;

public class PerceptronStandard extends Perceptron {

	void update(Candidate promote, Candidate penalize) {
		updateFeat(promote.getFeatureList(), +1);
		updateFeat(penalize.getFeatureList(), -1);

	}

}
