package us.hall.weka.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import weka.classifiers.evaluation.RegressionAnalysis;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MathExpression;
import weka.filters.unsupervised.attribute.Remove;

public class EmpiricalComplexity {

	private static final String regexR2 = "R\\^2 value = ([0-9]{1,13}(\\.[0-9]*)?)";
	private static final String regexAdj = "Adjusted R\\^2 = ([0-9]{1,13}(\\.[0-9]*)?)";
	private static final Pattern pR2 = Pattern.compile(regexR2);
	private static final Pattern pAdj = Pattern.compile(regexAdj);
	
	public static void main(String[] args) {
		Instances data = null;
		String xName = null, yName = null;
		if (args.length < 2 ) {
			System.out.println("Missing arg(s)");
			usage();
			return;
		}
		String fileName = args[0];
		if (args.length == 2) {
			xName = "iteration";	
			yName = args[1];
		}
		else {
			xName = args[1];
			yName = args[2];
		}
		try {
			if (fileName.toLowerCase().endsWith(".arff")) {
				BufferedReader reader = 
					new BufferedReader(new FileReader(args[0]));
				ArffReader arff = new ArffReader(reader);
				data = arff.getData();
				data.setClassIndex(data.numAttributes() - 1);
				reader.close();
			}
			else if (fileName.toLowerCase().endsWith(".csv")) {
				CSVLoader loader = new CSVLoader();
				loader.setSource(new File(fileName));
				data = loader.getDataSet();
			}
			else {
				System.out.println("filename extension must be arff/csv");
				return;
			}
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
		// Do an initial linear regression to see if anything more complex 
		// is involved anywhere
		boolean[] selectedAttributes = new boolean[] { true, false };
		try {
			int[] attrA = new int[data.numAttributes()-1];
			for (int i=1;i<=attrA.length;i++) {
				attrA[i-1] = i;
			}
			Attribute response = data.attribute(yName);			
			List<Integer> attrL = Arrays.stream(attrA)
			                             .boxed()
			                             .collect(Collectors.toList());
			attrL.remove(response.index()-1);
			int[] remA = attrL.stream().mapToInt(i -> i).toArray();
			Remove rem = new Remove();
			rem.setAttributeIndicesArray(remA);
			rem.setInputFormat(data);
			Instances linData = Filter.useFilter(data,rem);
			linData.setClassIndex(linData.numAttributes() - 1);
			// Allow data to not be in iteration order
			// Say, in case extra iterations are done to fill in nonlinear regions data
			linData.sort(linData.attribute("iteration"));
			Instances nonlinData = new Instances(linData);
			nonlinData.delete();		// empty
			LinearRegression lr = new LinearRegression();
			lr.setOutputAdditionalStats(true);
			lr.buildClassifier(linData);
			Evaluation evaluation = new Evaluation(linData);
			double rmse = evaluation.rootMeanSquaredError();
			double corr = evaluation.correlationCoefficient();
			evaluation.evaluateModel(lr, linData);
			Instance lastInst = linData.lastInstance();
			nonlinData.add(lastInst);
			System.out.println("_______");
			System.out.println("Iteration: " + (int)lastInst.value(lastInst.attribute(0)));
			output(lr,evaluation);
			// Assuming linear until runs out of memory then things go nonlinear
			// remove last instances while it improves linearity. 
			// Programmatically figure out when free space runs out
			boolean done = false;
			while (!done) {
				// remove last instance
				Instance inst = linData.remove(linData.numInstances()-1);
				lr = new LinearRegression();
				lr.setOutputAdditionalStats(true);
				lr.buildClassifier(linData);
				evaluation = new Evaluation(linData);
				evaluation.evaluateModel(lr,linData);
				lastInst = linData.lastInstance();
				System.out.println("Iteration: " + (int)lastInst.value(lastInst.attribute(0)));
				output(lr,evaluation);			
				double newRmse = evaluation.rootMeanSquaredError();
				double newCorr = evaluation.correlationCoefficient();
				if (newRmse >= rmse && newCorr <= corr) {
					done = true;
					continue;
				}
				nonlinData.add(0,inst);
				rmse = newRmse;
				corr = newCorr;
			}
			// Last instance, or two? might be low for early outofmemory error
			response = nonlinData.attribute(yName);
			double respLast = nonlinData.lastInstance().value(response);
			double respPrev = nonlinData.get(nonlinData.numInstances()-2).value(response);
			while (respPrev >= respLast) {
				nonlinData.remove(nonlinData.lastInstance());
				respLast = nonlinData.lastInstance().value(response);
				respPrev = nonlinData.get(nonlinData.numInstances()-2).value(response);
			}
			System.out.println("*---------------------*");
			System.out.println("* Data Transform      *");
			System.out.println("*---------------------*");
			System.out.println("num insts = " + nonlinData.numInstances());
			MathExpression ln = new MathExpression();
			ln.setIgnoreClass(true);
			ln.setExpression("log(A)");
			ln.setInputFormat(nonlinData);
			Instances nonlinLogData = Filter.useFilter(nonlinData,ln);
			lr = new LinearRegression();
			lr.setOutputAdditionalStats(true);
			lr.buildClassifier(nonlinLogData);		
			System.out.println(lr);
			double[] res = lr.coefficients();
			System.out.println("Power Model: " + yName + " = " + Math.exp(res[2]) 
				+ " x iteration ^ " +res[0]);
		}
		catch (Exception ex) { System.out.println("ERROR"); ex.printStackTrace(); }
		
	}
	
	/**
	 * This would parse just the R^2 and R^2 adusted values from the Weka 
	 * toString of the linear model. 
	 * Currently unused.
	 * The LinearRegression toString is displayed as-is.
	 **/ 
	private static void output(LinearRegression lr, Evaluation eval) throws Exception {
		String result = lr.toString();
		Matcher matcher = pR2.matcher(result);
		if (matcher.find()) {
			System.out.println("R^2 is " + matcher.group(1));
		}
		else {
			System.out.println("R^2 not found");
		}
		matcher = pAdj.matcher(result);
		if (matcher.find()) {
			System.out.println("R^2 adjusted is " + matcher.group(1));
		}
		else {
			System.out.println("R^2 adjusted not found");
		}
		System.out.println("rmse " + eval.rootMeanSquaredError() + " corr " 
			+ eval.correlationCoefficient());
		System.out.println("_______");
	}
	
	private static void usage() {
		System.out.println("Usage: EmpiricalComplexity training dataset [x attribute] y attribute");
		System.out.println("If not provided x attribute is 'iterations'");
	}
}