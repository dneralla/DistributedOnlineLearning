package edu.illinois.ml.learning;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;

public abstract class Perceptron extends LinearModel {

	public static final String DEFAULT_SEPERATOR = "|";

	public void collectOutput(OutputCollector<Text, DoubleWritable> output)
			throws IOException {

		for (Map.Entry<Integer, Candidate> entry : labelToCandidate.entrySet()) {
			int label = entry.getKey();
			Candidate temp = entry.getValue();
			for (FeatureWeight fw : temp.getFeatureList())
				output.collect(new Text(label + Perceptron.DEFAULT_SEPERATOR
						+ fw.getFeatureId()),
						new DoubleWritable(fw.getFeatureWeight()));

		}

	}

	public int train(InstanceVector instanceVector) throws Exception {
		int predicted;
		List<ScoreLabel> scoreLabels = initScoreLabels(instanceVector
				.getActiveFeatures());

		// pick best
		Candidate topCand = pickTop(scoreLabels);
		predicted = topCand.getLabelId();

		// if best is wrong
		boolean isCorrect = false;
		if (predicted == instanceVector.getLabelId())
			isCorrect = true;
		if (!isCorrect) {
			// find gold
			Candidate goldCand = null;
			for (ScoreLabel sl : scoreLabels) {
				Candidate cand = sl.getC();
				if (cand.getLabelId() == instanceVector.getLabelId()) {
					goldCand = cand;
					break;
				}
			}

			update(goldCand, topCand);

		}
		return predicted;
	}

	abstract void update(Candidate promote, Candidate penalize);

	protected void updateFeat(List<FeatureWeight> feats, double para) {
		if (para == 0.0) {
			System.err.println("*** ZERO UPDATING***");
		}
		for (FeatureWeight fw : feats)
			fw.setFeatureWeight(fw.getFeatureWeight() + para);
	}
}
