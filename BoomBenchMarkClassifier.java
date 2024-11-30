package us.hall.weka;

import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.RandomForest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * run until memory error
 */
public class BoomBenchMarkClassifier {
		
	public static void main(String[] args) {
		int count = 0;
		
		// Args should be classifer, dataset to evaluate
		if (args.length != 2) {
			System.out.println("BoomBenchMarkClassifier: missing or invalid parm(s)");
			return;
		}
		String classifierName = args[0];
		String trainingName = args[1];
		Bagging bagging = new Bagging();
		RandomForest rf = new RandomForest();		// Get the RandomForest classifier
		bagging.setClassifier(rf);
		ArrayList<Evaluation> evals = new ArrayList<Evaluation>();
		for (;;) {
			try {
				long start = System.currentTimeMillis();
				evals.add(evaluate(bagging,trainingName));
				count++;
				System.out.print(count + " ");
				elapsed(start);
			}
			catch (Throwable tossed) { tossed.printStackTrace(); }
		}
	}
	
	static Evaluation evaluate(Classifier classifier, String trainingName) throws Exception {
		BufferedReader trainingRdr = new BufferedReader(new FileReader(new File(trainingName)));
		Instances trainingData = new Instances(trainingRdr);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
		Evaluation eval = new Evaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, 10, new java.util.Random(1));
		return eval;
	}
	
	static void elapsed(long start) {
		long elapsed = System.currentTimeMillis() - start;
		String elapsedOut = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsed),
				TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)),
				TimeUnit.MILLISECONDS.toSeconds(elapsed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));
		System.out.println("Elapsed: " + elapsedOut);
	}
}