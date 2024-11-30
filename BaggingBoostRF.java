package us.hall.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.RandomForest;

/**
 * Wrap a Bagging around a AdaBoostM1 of a RandomForest classification
 **/
public class BaggingBoostRF {
	private Instances trainingData = null, testData = null;
	private int depth = 0,numFeatures = 14,numTrees = 35;
	private Classifier classifier = null;
	private File test = null, training = null;
	private boolean reverse = false, evaluate = false;
	
	public static void main(String[] args) {
		File training = null;
		File test = null;
		int depth = 0, numFeatures = 14, numTrees = 35;
		boolean reverse = false, evaluate = false;
		
		for (int i=0;i<args.length;i++) {
			if (args[i].equals("-t")) {			// Q. training ARFF?
				i++;
				training = new File(args[i]);
			}
			else if (args[i].equals("-T")) {		// Q. test ARFF?
				i++;
				test = new File(args[i]);
			}	
			else if (args[i].equals("depth")) {		// Q. depth?
				i++;
				depth = new Integer(args[i]).intValue();
			}
			else if (args[i].equals("-I")) {		// Q. -I number of trees?
				i++;
				numTrees = new Integer(args[i]).intValue();
			}
			else if (args[i].equals("-K")) {		// Q. -K number of features?
				i++;
				numFeatures = new Integer(args[i]).intValue();
			}
			else if (args[i].equals("-r")) {		// Reverse Bagging and AdaBoostM1?
				reverse = true;
			}
			else if (args[i].equals("-e")) {		// Evaluate instead of predict?
				evaluate = true;
			}
		}
		if (training == null || !training.exists()) {
			System.out.println("Training arff dataset " + training + " is invalid or missing");
			return;
		}
		if (test == null || !test.exists()) {
			System.out.println("Test arff dataset " + test + " is invalid or missing");
			return;
		}
		BaggingBoostRF bbrf = new BaggingBoostRF(training,test,depth,numFeatures,numTrees,reverse,evaluate);
		try {
			bbrf.process();
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public BaggingBoostRF(File training,File test,int depth,int numFeatures,int numTrees,boolean reverse,boolean evaluate) {
		this.depth = depth;
		this.numFeatures = numFeatures;
		this.numTrees = numTrees;
		this.training = training;
		this.test = test;
		this.reverse = reverse;
		this.evaluate = evaluate;
		try {
			Bagging bagging = new Bagging();			// Get the bagging classifier wrapper
			AdaBoostM1 boost = new AdaBoostM1();		// Get the AdaBoostM1 meta classifier
			RandomForest rf = new RandomForest();		// Get the RandomForest classifier
			rf.setMaxDepth(depth);
			rf.setNumFeatures(numFeatures);
			rf.setNumTrees(numTrees);
			if (reverse) {
				classifier = boost;
				boost.setClassifier(bagging);
				bagging.setClassifier(rf);
			}
			else {
				classifier = bagging;
				bagging.setClassifier(boost);
				boost.setClassifier(rf);
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}
	
	private void process() throws Exception {
		BufferedReader trainingRdr = new BufferedReader(new FileReader(training));
		trainingData = new Instances(trainingRdr);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
		if (evaluate) {
			evaluate();
		}
		else {
			build();
			predict();
		}
	}
	
	private void build() throws Exception {
		classifier.buildClassifier(trainingData);
		trainingData = null;
	}
	
	private void evaluate() throws Exception {
		Evaluation eval = new Evaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, 10, new java.util.Random(1));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println("");
		System.out.println(eval.toMatrixString());
		System.out.println("");
		System.out.println(eval.toClassDetailsString());
	}
	
	private void predict() throws Exception {
		BufferedReader testRdr = new BufferedReader(new FileReader(test));
		testData = new Instances(testRdr);
		testData.setClassIndex(testData.numAttributes() - 1);
		for (int i = 0; i < testData.numInstances(); i++) {
			int pred = (int)classifier.classifyInstance(testData.instance(i)) + 1;
			System.out.println(pred);
		}
	}
}