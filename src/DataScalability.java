package us.hall.weka.benchmark;

import weka.core.*;
import weka.core.converters.ArffLoader.ArffReader;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;
import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.Scanner;
import weka.classifiers.AdaptiveEvaluation;

public class DataScalability {

	private static BufferedWriter writer = null;
	private static final Charset cs = Charset.forName("US-ASCII");
	
	/**
	 * Check performance as amount of data increases 
	 **/
	public static void main(String[] args) {
		int idx = 0;
		int PER_INIT = 10;
		int PER = PER_INIT;
		int PER_INC = 10;
		int PER_END = Integer.MAX_VALUE;	
		String dataset = null;
		Instances data = null;
			
		for (; idx < args.length; idx++) {
			if (args[idx].equals("-pi")) {		// Use a different iteration increment
				if (args.length > idx) {
					try {
						PER_INC = Integer.parseInt(args[idx+1]);
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
			else if (args[idx].equals("-ps")) {       // starting iteration number
				if (args.length > idx) {
					try {
						PER_INIT = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch (NumberFormatException nfe) {
						System.out.println("Invalid starting number " + args[idx]);
					}
				}
				else {
					System.out.println("Missing starting iteration number");
					return;
				}
			}
			else if (args[idx].equals("-pe")) {		// ending iteration number
				if (args.length > idx) {
					try {
						PER_END = Integer.parseInt(args[idx+1]);
						idx++;
					}
					catch (NumberFormatException nfe) {
						System.out.println("Invalid ending iteration number");
					}
				}
				else {
					System.out.println("Missing ending iteration number");
					return;
				}
			}
			else if (args[idx].equals("-o")) {
				try {
					if (args.length > idx+1) {
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
			else { break; }  // Remaining parms s/b Weka ones
		}
		// Assume after '-' command parameters the test/train dataset is given
		if (args.length > idx) {
			dataset = args[idx];
			idx++;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dataset));
				ArffReader arff = new ArffReader(reader);
				data = arff.getData();
				data.setClassIndex(data.numAttributes() - 1);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
		else {
			System.out.println("missing file with data");
			return;
		}
		// Then assume the classifier and it's parameters are given
		Object scheme = null;
		try {
			String classname = args[idx];
			idx++;
			String[] wargs = new String[args.length-idx];
			System.arraycopy(args,idx,wargs,0,wargs.length);
			scheme = Class.forName(classname).newInstance();
			if (scheme instanceof OptionHandler) {
			  ((OptionHandler) scheme).setOptions(wargs);
			}
			else {
				System.out.println(classname + " is not an option handler");
			}
			for (;;) {
				System.out.println("DataScalablity: sample size " + PER + "%");
				Resample resample = new Resample();
				resample.setRandomSeed(5);
				resample.setSampleSizePercent(PER);
				resample.setInputFormat(data);
				Instances sample = Filter.useFilter(data, resample);
				((Classifier)scheme).buildClassifier(sample);
				AdaptiveEvaluation evaluation = new AdaptiveEvaluation(sample);
				evaluation.evaluateModel((Classifier)scheme,sample);
				System.out.println(evaluation.toSummaryString());
				PER += PER_INC;
			}
		}
		catch (Exception ex) { ex.printStackTrace(); }

	}
}