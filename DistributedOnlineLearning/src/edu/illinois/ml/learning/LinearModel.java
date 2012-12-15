package edu.illinois.ml.learning;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.StringUtils;

public class LinearModel {

	// variables
	protected Map<String, Integer> feat2id;
	protected List<InstanceVector> instanceVectors;
	protected List<FeatureWeight> featureWeight;
	protected static List<Candidate> candidates;

	protected Map<Integer, Candidate> labelToCandidate;

	// protected double getWeightByFeatureAndLabel(int labelId,int featureId)
	// {
	// for(FeatureWeight f :featureWeight)
	// if(f.getForLabel()==labelId && f.getFeatureId()==featureId)
	// return f.getFeatureWeight();
	//
	// return 0.0;
	// }
	//
	//
	// protected boolean isExistsFeatureAndLabel(int labelId,int featureId)
	// {
	// for(FeatureWeight f :featureWeight)
	// if(f.getForLabel()==labelId && f.getFeatureId()==featureId)
	// return true;
	//
	// return false;
	// }

	public LinearModel() {
		// feat2id = new HashMap<String, Integer>(FEAT_HASH_INIT);
		// id2feat = new ArrayList<Feature>(FEAT_HASH_INIT);
		// id2feat = new ArrayList<FeatureWeight>();
		labelToCandidate = new HashMap<Integer, Candidate>();
	}

	public void readWeights(JobConf conf) {
		try {
			Path[] patternsFiles = DistributedCache.getLocalCacheFiles(conf);
			for (Path patternsFile : patternsFiles) {
				BufferedReader fis = new BufferedReader(new FileReader(
						patternsFile.toString()));
				readWeights(fis);
			}
		} catch (IOException ioe) {
			System.err.println("Caught exception while getting cached files: "
					+ StringUtils.stringifyException(ioe));
		}
	}

	public void readWeights(BufferedReader fis) throws IOException {
		String line = null;
		while ((line = fis.readLine()) != null) {
			String tokens[] = line.split("\t");
			String intermediateFeature[] = tokens[0]
					.split(Perceptron.DEFAULT_SEPERATOR);
			int label = Integer.parseInt(intermediateFeature[0]);
			int featureId = Integer.parseInt(intermediateFeature[1]);
			addNewFeature(featureId, label, Double.parseDouble(tokens[1]), true);
			// NB need to register label if missing
			LabelLib.storeLabel(Integer.parseInt(tokens[0]
					.split(Perceptron.DEFAULT_SEPERATOR)[0]));
		}
	}

	public int predict(List<Integer> feats) throws Exception {

		List<ScoreLabel> scoreLabels = initScoreLabels(feats);
		Candidate topCand = pickTop(scoreLabels);
		return topCand.getLabelId();
	}

	protected Candidate pickTop(List<ScoreLabel> scoreLabels)
			throws NullPointerException {
		if (scoreLabels.size() == 0)
			throw new NullPointerException(
					"\nError:\nThe list of candidates is empty.");
		ScoreLabel topScoreLabel = scoreLabels.get(0);
		Candidate topCand = topScoreLabel.getC();
		double topOpScore = topScoreLabel.getScore();
		for (int i = 1; i < scoreLabels.size(); i++) {
			ScoreLabel temp = scoreLabels.get(i);
			if (temp.getScore() > topOpScore) {
				topOpScore = temp.getScore();
				topCand = temp.getC();
			}
		}
		return topCand;
	}

	protected List<Candidate> initCands(List<Integer> feats) {
		List<Candidate> cands = new ArrayList<Candidate>();
		for (Integer label : LabelLib.getCandidateLabels()) {
			if (labelToCandidate.containsKey(label))
				labelToCandidate.get(label);
			cands.add(labelToCandidate.get(label));

		}
		return cands;
	}

	protected List<ScoreLabel> initScoreLabels(List<Integer> feats)
			throws Exception {
		List<ScoreLabel> scoreLabels = new ArrayList<ScoreLabel>();

		for (Integer label : LabelLib.getCandidateLabels()) {
			registerAllFeaturesForLabel(feats, label);
			ScoreLabel sc = new ScoreLabel(labelToCandidate.get(label));
			sc.setScore(sc.getC().getScore(feats));
			scoreLabels.add(sc);

		}
		return scoreLabels;
	}

	public void registerAllFeaturesForLabel(List<Integer> features, int labelId)
			throws Exception {
		for (Integer featureId : features)
			addNewFeature(featureId, labelId);

	}

	public void addNewFeature(int feat, int labelId, double weight,
			boolean canAdd) throws IOException {

		if (labelToCandidate.containsKey(labelId)) {
			Candidate temp = labelToCandidate.get(labelId);
			if (temp.ifExistsFeature(feat)) {
				// Add ;
			} else
				temp.getFeatureList().add(new FeatureWeight(feat, weight));
		} else {
			List<FeatureWeight> newfw = new ArrayList<FeatureWeight>();
			newfw.add(new FeatureWeight(feat, weight));
			Candidate c = new Candidate(newfw, labelId);
		}
	}

	public void addNewFeature(int feat, int labelId) throws IOException {
		addNewFeature(feat, labelId, 0.0, false);
	}

	/**
	 * Get the score (sum of weights) of a set of features.
	 * 
	 * @param feats
	 *            A List of feature names.
	 * @return The sum of the weights of the features selected.
	 */
	// private double getScore(InstanceVector feats) {
	// double score = 0;
	//
	// for(Feature feature:feats.getActiveFeatures())
	// {
	// score += this.getWeight(feature.getFeatureId());
	// }
	//
	//
	// return score;
	// }
	//
	// private double getWeight(int featureId)
	// {
	// for(FeatureWeight featureWeight : id2feat)
	// if(featureWeight.getFeatureId()==featureId)
	// return featureWeight.getFeatureWeight();
	//
	// return 0.0;
	// }

}
