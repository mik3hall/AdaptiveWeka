package us.hall.weka;

import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.concurrent.TimeUnit;

public class BenchMarkClassifier {
	static long start = System.currentTimeMillis();
	static long edenold = 0L, oldold = 0L, survold = 0L;
	static long edeninit = 0L, oldinit = 0L, survinit = 0L;
		
	public static void main(String[] args) {
		// Args should be classifer, dataset to evaluate, number of iterations
		if (args.length != 3) {
			System.out.println("BenchMarkClassifier: missing or invalid parm(s)");
			return;
		}
		for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getType() == MemoryType.HEAP) {
				if (mpBean.getName().endsWith("Eden Space")) {
					edeninit = mpBean.getUsage().getUsed();
					edenold = edeninit;
				}
				else if (mpBean.getName().endsWith("Old Gen")) {
					oldinit = mpBean.getUsage().getUsed();
					oldold = oldinit;
				}
				else if (mpBean.getName().endsWith("Survivor Space")) {
					survinit = mpBean.getUsage().getUsed();
					survold = survinit;
				}
				else {
					System.out.println("Unknown memory bean " + mpBean.getName());
				}
			}
		}
		String classifierName = args[0];
		String trainingName = args[1];
		Class c = null;
		Constructor<?> cnst = null;
		Classifier classifier = null;
		int iters = 0;
		try {
			c = Class.forName(classifierName);
			cnst = c.getConstructor(new Class<?>[0]);
			classifier = (Classifier)cnst.newInstance(new Object[0]);
			iters = Integer.valueOf(args[2]);
		}
		catch (Throwable tossed) {
			tossed.printStackTrace();
			return;
		}
		// Do iterations with evaluations not saved, and gc done to get accurate memory
		for (int i=0; i<iters;i++) {
			try {
				Evaluation eval = evaluate(classifier,trainingName);
				elapsed();
				System.out.println("(1." + i + ") Heap space and non-Heap space:");
				System.out.println(getSettledUsedMemory());
				System.out.println("");
				pools();
			}
			catch (Throwable tossed) { tossed.printStackTrace(); }
		}
		// Save evaluations 
		System.out.println("Iterations saving evaluations");
		Evaluation[] evals = new Evaluation[iters];
		for (int i=0; i<iters;i++) {
			try {
				evals[i] = evaluate(classifier,trainingName);
				elapsed();
				System.out.println("(2." + i + ") Heap space and non-Heap space:");
				System.out.println(getSettledUsedMemory());
				System.out.println("");
				pools();
			}
			catch (Throwable tossed) { tossed.printStackTrace(); }
		}
		System.out.println("Iterations saving evaluations, not forcing gc");
		evals = new Evaluation[iters];
		for (int i=0; i<iters;i++) {
			try {
				evals[i] = evaluate(classifier,trainingName);
				elapsed();
				System.out.println("3." + i + ") Heap space and non-Heap space:");
				System.out.println(getCurrentlyUsedMemory());
				System.out.println("");
				pools();
			}
			catch (Throwable tossed) { tossed.printStackTrace(); }
		}		
	}

	static void pools() {
		System.out.println("Memory Pool");
		for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getType() == MemoryType.HEAP) {
				System.out.println("\tName: " + mpBean.getName());
				System.out.println("\t\tPeak: " + mpBean.getPeakUsage().getUsed());
				if (mpBean.getName().endsWith("Eden Space")) {
					long edenused = mpBean.getUsage().getUsed();
					System.out.println("\t\tSince start: " + (edenused-edeninit));
					System.out.println("\t\tSince last: " + (edenused-edenold));
					edenold = edenused;
				}
				else if (mpBean.getName().endsWith("Old Gen")) {
					long oldused = mpBean.getUsage().getUsed();
					System.out.println("\t\tSince start: " + (oldused-oldinit));
					System.out.println("\t\tSince last: " + (oldused-oldold));
					oldold = oldused; 
				}
				else if (mpBean.getName().endsWith("Survivor Space")) {
					long survused = mpBean.getUsage().getUsed();
					System.out.println("\t\tSince start: " + (survused-survinit));
					System.out.println("\t\tSince last: " + (survused-survold));
					survold = survused; 
				}
				else {
					System.out.println("Unknown memory bean " + mpBean.getName());
				}
			}
		}
	}
	
	static void elapsed() {
		long elapsed = System.currentTimeMillis() - start;
		String elapsedOut = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsed),
				TimeUnit.MILLISECONDS.toMinutes(elapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed)),
				TimeUnit.MILLISECONDS.toSeconds(elapsed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));
		System.out.println("Elapsed: " + elapsedOut);
	}
	
	static Evaluation evaluate(Classifier classifier, String trainingName) throws Exception {
		BufferedReader trainingRdr = new BufferedReader(new FileReader(new File(trainingName)));
		Instances trainingData = new Instances(trainingRdr);
		trainingData.setClassIndex(trainingData.numAttributes() - 1);
		Evaluation eval = new Evaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, 10, new java.util.Random(1));
		return eval;
	}
	
	static long getCurrentlyUsedMemory() {
	  return
		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
		ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
	}
	
	static long getPossiblyReallyUsedMemory() {
	  System.gc();
	  return getCurrentlyUsedMemory();
	}
	
	static long getGcCount() {
	  long sum = 0;
	  for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
		long count = b.getCollectionCount();
		if (count != -1) { sum +=  count; }
	  }
	  return sum;
	}
	
	static long getReallyUsedMemory() {
	  long before = getGcCount();
	  System.gc();
	  while (getGcCount() == before);
	  return getCurrentlyUsedMemory();
	}
	
	static long getSettledUsedMemory() {
  		long m;
  		long m2 = getReallyUsedMemory();
  		do {
  			try {
    			Thread.sleep(567);
    		}
    		catch (InterruptedException iex) {}
    		m = m2;
    		m2 = getCurrentlyUsedMemory();
  		} while (m2 < getReallyUsedMemory());
  		return m;
  	}
}