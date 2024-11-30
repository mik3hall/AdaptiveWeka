package us.hall.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;


/**
 * Use AdaBoostM1 with Random Forest
 **/			
public class BoostRF {
	private Instances trainingData = null, testData = null;
	private Classifier classifier = null;
	private File test = null, training = null;
	private boolean evaluate = false;
	private int depth = 0,numFeatures = 14,numTrees = 35;
	
	public static void main(String[] args) {
		File training = null;
		File test = null;
		boolean evaluate = false;
		int depth = 0, numFeatures = 14, numTrees = 35;
		
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
		BoostRF boostRF = new BoostRF(training,test,evaluate,depth,numFeatures,numTrees);
		try {
			boostRF.process();
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public BoostRF(File training,File test,boolean evaluate,int depth,int numFeatures,int numTrees) {
		this.training = training;
		this.test = test;
		this.evaluate = evaluate;
		try {
			AdaBoostM1 boost = new AdaBoostM1();		// Get the AdaBoostM1 meta classifier
			RandomForest rf = new RandomForest();		// Get the RandomForest classifier
			rf.setMaxDepth(depth);
			rf.setNumFeatures(numFeatures);
			rf.setNumTrees(numTrees);
			classifier = boost;
			boost.setClassifier(rf);
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