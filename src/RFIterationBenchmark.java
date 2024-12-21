package us.hall.weka.benchmark;

import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.AdaptiveEvaluation;
import weka.classifiers.trees.RandomForest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class RFIterationBenchmark {

	private static int ITER_INIT = 100;
	private static int ITER_INC = 100;
	private static int ITER_END = Integer.MAX_VALUE;
	private static final int mb = 1024*1024;
	private static final Runtime rt = Runtime.getRuntime();
	private static long[] peakUseds = new long[] { 0L, 0L, 0L };
	private static long[] maxUseds = new long[] { 0L, 0L, 0L}; 
	private static BufferedWriter writer = null;
	private static final Charset cs = Charset.forName("US-ASCII");
	
	RFIterationBenchmark() {
        rt.addShutdownHook(new Completion());
	}
	
	/**
	 * Run RandomForest starting with the given iteration number increasing by 100 
	 * iterations until a memory error occurs.
	 * Memory stats given each round
	 **/
	public static void main(String[] args) {
		int idx = 0;
		int idxmax = args.length - 1;   // - 1 for positional training dataset parameter
		boolean showPools = false, showThreads = false;
		long currentTid = Thread.currentThread().getId();
		DataRecord datarec = null;
		HashMap<Long,long[]> threadStats = new HashMap<Long,long[]>();
		RFIterationBenchmark instance = new RFIterationBenchmark();

         
		if (args.length == 0) {
			System.out.println("RFIterationBenchmark: missing required argument(s)");
			return;
		}
		for (; idx < args.length-1; idx++) {
			if (args[idx].equals("-p")) {		// Display pool information
				showPools = true;
				continue;
			}
			if (args[idx].equals("-t")) {		// Display thread times
				showThreads = true;
				continue;
			}
			if (args[idx].equals("-o")) {		// Write results to arff/csv file
				try {
					if (idxmax > idx) {
						String outputFile = null;
						if (args[idx+1].endsWith(".arff") || args[idx+1].endsWith(".csv")) {
							outputFile = args[idx+1];
							idx++;
						}
						else {
							System.out.println("output file extension must be arff or csv");
							return;
						}
						Path outPath = Paths.get(outputFile);
						if (Files.exists(outPath)) {
							Scanner fileOpt = new Scanner(System.in);
							System.out.println(outputFile + " already exists. Overwrite? (y)es/(n)o/(a)ppend");
							String choice = fileOpt.nextLine().toLowerCase();
							if (choice.equals("y")) {
								writer = Files.newBufferedWriter(outPath,cs,CREATE,TRUNCATE_EXISTING,WRITE);
								if (outputFile.endsWith(".arff")) {
									String arffPrefix = IterationRecord.getArffPrefix();
									writer.write(arffPrefix,0,arffPrefix.length());
								}
								else {
									String csvPrefix = IterationRecord.getCsvPrefix();
									writer.write(csvPrefix,0,csvPrefix.length());
								}
							}
							else if (choice.equals("a")) {
								writer = Files.newBufferedWriter(outPath,cs,CREATE,APPEND,WRITE);
							}
							else { 
								return; 
							}
						}
						else {
							writer = Files.newBufferedWriter(outPath,CREATE,WRITE);
							if (outputFile.endsWith(".arff")) {
								String arffPrefix = IterationRecord.getArffPrefix();
								writer.write(arffPrefix,0,arffPrefix.length());
							}
							else {
								String csvPrefix = IterationRecord.getCsvPrefix();								
								writer.write(csvPrefix,0,csvPrefix.length());
							}
						}
					}
					else {
						System.out.println("Missing output file parameter");
					}
				}
				catch (IOException ioex) { 
					ioex.printStackTrace();
					return;
				}
			}
			if (args[idx].equals("-i")) {		// Use a different iteration increment
				if (idxmax > idx) {
					try {
						ITER_INC = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch (NumberFormatException nfe) {
						System.out.println("Invalid iteration increment " + args[idx+1]);
						return;
					}
				}
				else {
					System.out.println("Missing iteration increment parameter");
					return;
				}
			}
			if (args[idx].equals("-s")) {       // starting iteration number
				if (idxmax > idx) {
					try {
						ITER_INIT = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch (NumberFormatException nfe) {
						System.out.println("Invalid starting number " + args[idx+1]);
					}
				}
				else {
					System.out.println("Missing starting iteration number");
					return;
				}
			}
			if (args[idx].equals("-e")) {		// ending iteration number
				if (idxmax > idx) {
					try {
						ITER_END = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch (NumberFormatException nfe) {
						System.out.println("Invalid ending iteration number " + args[idx+1]);
					}
				}
				else {
					System.out.println("Missing ending iteration number");
				}
			}
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
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		datarec = new DataRecord(trainingData,rt.totalMemory());

		long old_cpu = 0L, old_user = 0L;
		long initialMem = Util.getSettledUsedMemory();
		System.out.println("Initial memory: " + initialMem);
		for (int i = ITER_INIT; i <= ITER_END ; i += ITER_INC) {
			System.out.println("Starting iteration " + i); 
			IterationRecord iterrec = new IterationRecord(i,datarec);	
			System.out.println("Runtime total: " + rt.totalMemory());	
			iterrec.setRTMax(rt.maxMemory());		
			long start = System.currentTimeMillis();
			long cpuStartTime = -1L;
			
			RandomForest rf = new RandomForest();	// Set the RandomForest classifier
			rf.setNumIterations(i);
			try {
				evaluate(rf,trainingData);
				System.out.println("Evaluation complete. Iterations: " + i);
			}
			catch (Throwable tossed) { 
				tossed.printStackTrace(); 
			}
			long duration = System.currentTimeMillis() - start; 
			iterrec.setElapsed(duration);		
			elapsed(duration);
			MemPoolData poolData = Util.pools(peakUseds,maxUseds);
			iterrec.setPoolData(Util.pools(peakUseds,maxUseds));
			System.out.println("");
			iterrec.setUser(Util.threads(currentTid,threadStats));
			System.out.println("");
			long endingMem = Util.getSettledUsedMemory();
			iterrec.setIterMem(endingMem);
			if (writer != null) {
				writeIter(iterrec);
			}
			System.out.println("Iteration " + i + " final memory: " + endingMem +
				" Runtime max: " + rt.maxMemory());	
			System.out.println("_____________________________");
			System.out.println("");	
		}
		System.exit(0);		// trigger shutdown hook
	}

	static void writeIter(IterationRecord iterrec) {

		try {
			String rec = iterrec.toString();
			writer.write(rec,0,rec.length());
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
	}
		
	static Evaluation evaluate(Classifier classifier, Instances trainingData) throws Exception {
		Evaluation eval = (Evaluation)new AdaptiveEvaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, 10, new java.util.Random(1));
		return eval;
	}
	
	static void elapsed(long duration) {
		String elapsedOut = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
				TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
				TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
		System.out.println("Elapsed: " + elapsedOut);
	}
  	
  	static class Completion extends Thread {
  		
  		public void run() {
  		    if (writer != null) {
  		    	try {
  		    		System.out.println("closing I/O");
  		    		writer.flush();
  		    		writer.close();
  		    	}
  		    	catch (IOException ioex) { ioex.printStackTrace(); }
  		    }
  			printFinal(peakUseds, maxUseds);
  		}
  		
  		static void printFinal(long[] peakUseds, long[] maxUseds) {
  			System.out.println("Final maximums");
  			System.out.println("\tEden Space");
  			System.out.println("\t\tMaximum peak: " + peakUseds[0]);
  			System.out.println("\t\tMaximum used: " + maxUseds[0]);
  			System.out.println("\tOld Gen");
  			System.out.println("\t\tMaximum peak: " + peakUseds[1]);
  			System.out.println("\t\tMaximum used: " + maxUseds[1]);
  			System.out.println("\tSurvivor");
  			System.out.println("\t\tMaximum peak: " + peakUseds[2]);
  			System.out.println("\t\tMaximum used: " + maxUseds[2]);  		
  		}
  	}
  	
}