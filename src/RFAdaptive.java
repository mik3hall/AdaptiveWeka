import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.AdaptiveEvaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.supportVector.NormalizedPolyKernel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import us.hall.gc.benchmark.MemPoolData;
import us.hall.gc.benchmark.Util;

public class RFAdaptive {

	private static final Runtime rt = Runtime.getRuntime();
	static int seed = 1;
	
	RFAdaptive() {
        rt.addShutdownHook(new Completion());
	}
	
	public static void main(String... args) {
		RandomForest rf = new RandomForest();	// Set the RandomForest classifier
		RandomForest srf = new RandomForest();  // One to use for slotted
		int idx = 0;
		int idxMax = args.length-1;		// - 1 for positional training dataset parameter
		boolean slots = false, threads = false, benchmark = false;
		int threadCnt = 0;
		
		for (;idx < args.length-1; idx++) {
			if (args[idx].equals("-i")) {		// set number of iterations
				try {
					rf.setNumIterations(Integer.parseInt(args[idx+1]));
					idx++;
				}
				catch(NumberFormatException nfex) {
					nfex.printStackTrace();
					return;
				}
			}
			else if (args[idx].equals("-t")) {   // Use adaptive threads
				if (idxMax > idx) {
					threads = true;
					try {
						threadCnt = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch(NumberFormatException nfex) {
						nfex.printStackTrace();
						return;
					}
				}
				else {
					System.out.println("RFAdaptive: missing parameters");
					return;
				}
			}
			else if (args[idx].equals("-s")) {   // Use RF slots
				if (idxMax > idx) {
					slots = true;
					try {
						rf.setNumExecutionSlots(Integer.parseInt(args[idx+1]));
						idx++;
					}
					catch (NumberFormatException nfex) {
						nfex.printStackTrace();
						return;
					}
				}
				else {
					System.out.println("RFAdaptive: missing parameters");
					return;
				}
			}
			else if (args[idx].equals("-b")) {
				benchmark = true;
			}
			else if (args[idx].equals("-i")) {
				if (idxMax > idx) {
					try {
						rf.setNumIterations(Integer.parseInt(args[idx+1]));
						idx++;
					}
					catch (NumberFormatException nfex) {
						nfex.printStackTrace();
						return;
					}
				}
				else {
					System.out.println("RFAdaptive: missing parameters");
					return;
				}
			}
			else if (args[idx].equals("-seed")) {
				if (idxMax > idx) {
					seed = Integer.parseInt(args[idx+1]);
					idx++;
				}
				else {
					System.out.println("RFAdaptive: missing parameters");
					return;
				}
			}
		}
		if (idxMax > idx) {
			System.out.println("RFAdaptive: missing training dataset parameter");
			return;
		}
		Instances trainingData = null;
		if (idx >= args.length) {
			System.out.println("Missing path to training dataset");
			return;
		}
		try {
			String trainingName = args[idx];
			BufferedReader trainingRdr = new BufferedReader(new FileReader(new File(trainingName)));
			trainingData = new Instances(trainingRdr);
			trainingData.setClassIndex(trainingData.numAttributes() - 1);
			if (benchmark) {
				doBenchmark(rf, srf, trainingData);
				SMO smo = new SMO();
				//smo.setBuildLogisticModels(true);
				NormalizedPolyKernel npk = new NormalizedPolyKernel();
				smo.setKernel(npk);
				benchmarkSMO(smo, trainingData);
				return;
			}
			if (threads) {
				long timeIn = System.currentTimeMillis();
				Evaluation eval = adaptiveEvaluate(rf, 10, threadCnt, trainingData);
				long rtime = System.currentTimeMillis() - timeIn;
				System.out.println("Default Adaptive(10 fold): used " + 
					((AdaptiveEvaluation)eval).getThreadCnt() + 
					" threads. Elapsed(sec) " + String.format("%.2f",(rtime/1000d)) + 
					". accuracy " + String.format("%.2f",eval.pctCorrect()));	 
				return;
			}
			else {
				long timeIn = System.currentTimeMillis();
				Evaluation eval = evaluate(rf, 10, trainingData);
				long rtime = System.currentTimeMillis() - timeIn;
				System.out.println("Single Thread: Elapsed(sec) " +
						String.format("%.2f", (rtime/1000d)) +
						". accuracy " + String.format("%.2f", eval.pctCorrect()));	
				
			}
			System.out.println("Evaluation complete.");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}
	
	static void doBenchmark(RandomForest rf, RandomForest srf, Instances trainingData) {
		double runTime = 0L;
		System.out.println("*** RandomForest ***");
		int[] folds = new int[] { 3, 5,8,10 };
		boolean[] singlesDone = new boolean[folds.length];
		for (int f = 0; f < folds.length; f++) {
			ArrayList<Evaluation> slotThreaded = new ArrayList<Evaluation>();
			ArrayList<Evaluation> adaptiveThreaded = new ArrayList<Evaluation>();
			ArrayList<Long> slotTimes = new ArrayList<Long>();
			ArrayList<Long> adaptiveTimes = new ArrayList<Long>();
			double avgSlotTime = 0d, avgAdaptTime = 0d;
			Long minSlotTime = Long.MAX_VALUE, minAdaptTime = Long.MAX_VALUE;
			Long maxSlotTime = 0L, maxAdaptTime = 0L;
			int minSlotThread = 0, maxSlotThread = 0;
			int minAdaptThread = 0, maxAdaptThread = 0;
			System.out.println("*** CV Fold " + folds[f]);
			try {
				long timeIn = System.currentTimeMillis();
				Evaluation eval = adaptiveEvaluate(rf, folds[f], 0, trainingData);
				long rtime = System.currentTimeMillis() - timeIn;
				System.out.println("Default Adaptive(" + folds[f] + " fold): used " + 
					((AdaptiveEvaluation)eval).getThreadCnt() + 
					" threads. Elapsed(sec) " + String.format("%.2f",(rtime/1000d)) + 
					". accuracy " + String.format("%.2f",eval.pctCorrect()));	 
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			for (int t = 2; t < 8; t++) {
				try {
					if (!singlesDone[f]) {
						long singleIn = System.currentTimeMillis();
						Evaluation singleThread = evaluate(rf, folds[f], trainingData);
						runTime = System.currentTimeMillis() - singleIn;
						System.out.println("Single Thread for fold " + folds[f] + " Elapsed(sec) " +
							String.format("%.2f", (runTime/1000d)) +
							". accuracy " + String.format("%.2f", singleThread.pctCorrect()));	
						singlesDone[f] = true;							
					}
					long adaptIn = System.currentTimeMillis();
					Evaluation adapted = adaptiveEvaluate(rf, folds[f], t, trainingData);
					adaptiveThreaded.add(adapted);
					long adaptTime = System.currentTimeMillis() - adaptIn;
					adaptiveTimes.add(adaptTime);
					System.out.println("\t"+ t + " " + adaptTime + " used " + 
						((AdaptiveEvaluation)adapted).getThreadCnt());
					srf.setNumExecutionSlots(t);
					long slotIn = System.currentTimeMillis();
					slotThreaded.add(evaluate(srf, folds[f], trainingData));
					slotTimes.add(System.currentTimeMillis() - slotIn);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			int len = adaptiveThreaded.size();
			long sumSlotTime = 0L;
			for (int i = 0; i < slotTimes.size(); i++) {
				long l = slotTimes.get(i);
				if (l < minSlotTime) {
					minSlotTime = l;
					minSlotThread = i+2;
				}
				else if (l > maxSlotTime) {
					maxSlotTime = l;
					maxSlotThread = i+2;
				}
				sumSlotTime += l;
			}
			long sumAdaptTime = 0L;
			for (int i = 0; i < adaptiveTimes.size(); i++) {
				long l = adaptiveTimes.get(i);
				if (l < minAdaptTime) {
					minAdaptTime = l;
					minAdaptThread = i+2;
				}
				else if (l > maxAdaptTime) {
					maxAdaptTime = l;
					maxAdaptThread = i+2;
				}
				sumAdaptTime += l;
			}
			System.out.println("Slotted:");
			System.out.println("Avg: " + (sumSlotTime / len) + "\tMin: "  + minSlotTime +
				" Thread " + minSlotThread + ".\tMax: " + maxSlotTime + " Thread " +
				maxSlotThread);
			System.out.println("Adaptive:");
			System.out.println("Avg: " + (sumAdaptTime / len) + "\tMin: " + minAdaptTime + 
				" Thread " + minAdaptThread + ".\tMax: " + maxAdaptTime + " Thread " +
				maxAdaptThread);
		}
	}
	
	static void benchmarkSMO(SMO smo, Instances trainingData) {
		double runTime = 0L;
		System.out.println("*** SMO ***");
		int[] folds = new int[] { 3, 5, 8, 10};
		boolean[] singlesDone = new boolean[folds.length];
		for (int f = 0; f < folds.length; f++) {
			ArrayList<Evaluation> adaptiveThreaded = new ArrayList<Evaluation>();
			ArrayList<Long> adaptiveTimes = new ArrayList<Long>();
			double time = 0d, avgAdaptTime = 0d;
			Long minAdaptTime = Long.MAX_VALUE;
			Long maxAdaptTime = 0L;
			int minAdaptThread = 0, maxAdaptThread = 0;
			System.out.println("*** CV Fold " + folds[f]);
			try {
				long timeIn = System.currentTimeMillis();
				Evaluation eval = adaptiveEvaluate(smo, folds[f], 0, trainingData);
				long rtime = System.currentTimeMillis() - timeIn;
				System.out.println("Default Adaptive(" + folds[f] +
					" fold): used " + ((AdaptiveEvaluation)eval).getThreadCnt() + 
					" threads. Elapsed(sec) " + 
					String.format("%.2f",(rtime/1000d)) + 
					". accuracy " + String.format("%.2f",eval.pctCorrect()));	
			}	
			catch (Exception ex) {
				ex.printStackTrace();
			}
			for (int t = 2; t < 8; t++) {
				try {		
					if (!singlesDone[f]) {
						long singleIn = System.currentTimeMillis();
						Evaluation singleThread = evaluate(smo, folds[f], trainingData);
						runTime = System.currentTimeMillis() - singleIn;
						System.out.println("Single Thread for fold " + folds[f] + " Elapsed(sec) " +
							String.format("%.2f", (runTime/1000d)) +
							". accuracy " + String.format("%.2f", singleThread.pctCorrect()));	
						singlesDone[f] = true;							
					}
					long adaptIn = System.currentTimeMillis();
					Evaluation adapted = adaptiveEvaluate(smo, folds[f], t, trainingData);
					adaptiveThreaded.add(adapted);
					long adaptTime = System.currentTimeMillis() - adaptIn;
					adaptiveTimes.add(adaptTime);
					System.out.println("\t"+ t + " " + adaptTime + " used " + 
						((AdaptiveEvaluation)adapted).getThreadCnt());
				}
				catch (Exception ex) { ex.printStackTrace(); }
			}
			int len = adaptiveThreaded.size();
			long sumAdaptTime = 0L;
			for (int i = 0; i < adaptiveTimes.size(); i++) {
				long l = adaptiveTimes.get(i);
				if (l < minAdaptTime) {
					minAdaptTime = l;
					minAdaptThread = i+2;
				}
				else if (l > maxAdaptTime) {
					maxAdaptTime = l;
					maxAdaptThread = i+2;
				}
				sumAdaptTime += l;
			}
			System.out.println("Adaptive:");
			System.out.println("Avg: " + (sumAdaptTime / len) + "\tMin: " + minAdaptTime + 
				" Thread " + minAdaptThread + ".\tMax: " + maxAdaptTime + " Thread " +
				maxAdaptThread);
		}
	}
	
	static Evaluation evaluate(Classifier classifier, int folds, Instances trainingData) throws Exception {
		Evaluation eval = new Evaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, folds, new java.util.Random(seed));
		return eval;
	}

	static Evaluation adaptiveEvaluate(Classifier classifier, int folds, int threadCnt, Instances trainingData) throws Exception {
		Evaluation eval = (Evaluation)new AdaptiveEvaluation(trainingData);
		if (threadCnt != 0) {
			((AdaptiveEvaluation)eval).setThreadCnt(threadCnt);
		}
		eval.crossValidateModel(classifier, trainingData, folds, new java.util.Random(seed));
		return eval;
	}
	
	static class Completion extends Thread {
	
		public void run() {

		}
	}
}