package weka.classifiers.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import weka.core.*;

public class CVFoldInfo {
	
	static int numInstForFold, first, numInstances;
	static int numFolds, numFold;
	
	boolean train = true;
	
	public static void main(String... args) throws IOException {
		// TEST
		String trainingName = args[0];
		BufferedReader trainingRdr = new BufferedReader(new FileReader(new File(trainingName)));
		Instances instances = new Instances(trainingRdr);
		instances.setClassIndex(instances.numAttributes() - 1);
		for (int i = 0; i < 10; i++) {
			trainCV(instances, 10, i);
		}
	}
		
	public CVFoldInfo(Instances instances, int numFolds, int numFold, boolean train) {
		this.train = train;
		this.numFolds = numFolds;
		this.numFold = numFold;
		
		int offset;
		
		if (numFolds < 2) {
			throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > instances.numInstances()) {
			throw new IllegalArgumentException("Can't have more folds than instances!");
		}
		// DEBUG
		//System.out.println("instances " + instances.getClass() + " numInstances " + instances.numInstances());
		numInstances = instances.numInstances();
		numInstForFold = numInstances / numFolds;
		// DEBUG
		//System.out.println("CVFoldInfo numInstForFold " + numInstForFold + " = numInstances " + numInstances + " / numFolds " + numFolds);
		if (numFold < numInstances % numFolds) {
			numInstForFold++;
			offset = numFold;
		}
		else {
			offset = numInstances % numFolds;
		}
		first = numFold * (numInstances / numFolds) + offset; 
		if (numInstForFold+1 < numInstances/numFolds) {
			throw new IllegalStateException(this.toString());
		}
	}
	
	public int instance(int index) {
		if (numInstForFold+1 < numInstances/numFolds) {
			throw new IllegalStateException(this.toString());
		}
		if (train) {
			if (index < first) {           
				return index;
			}
			return index + numInstForFold;
		}
		return first + index;
	}
	
	public int numInstances() {
		if (train) {
			return numInstances - numInstForFold;
		}
		return numInstForFold;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("CVFoldInfo: ");
		if (train) {
			sb.append("train instances\n");
		}
		else {
			sb.append("test instances\n");
		}
		sb.append("\tFirst offset: ").append(first).append("\n");
		sb.append("\tFolds: number ").append(numFold);
		sb.append(". CV folds ").append(numFolds).append("\n");
		sb.append("\tNumber of instances per fold ").append(numInstForFold).append("\n");
		return sb.toString();
	}
	
	// TEST
	private static void trainCV(Instances instances, int numFolds, int numFold) {
		int numInstForFold, first, offset;
		Instances train;

		if (numFolds < 2) {
		  throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > instances.numInstances()) {
		  throw new IllegalArgumentException(
			"Can't have more folds than instances!");
		}
		numInstForFold = instances.numInstances() / numFolds;
		if (numFold < instances.numInstances() % numFolds) {
		  numInstForFold++;
		  offset = numFold;
		} else {
		  offset = instances.numInstances() % numFolds;
		}
		System.out.println("Fold: " + numFold);
		System.out.println("\tOffset - " + offset + " ");
		//train = new Instances(this, numInstances() - numInstForFold);
		first = numFold * (instances.numInstances() / numFolds) + offset;
		System.out.println("\tFirst - " + first);
		System.out.println("\tPart1 0 to " + first);
		System.out.println("\tPart2 " + (first + numInstForFold) + " to " + 
			(instances.numInstances() - first - numInstForFold));
		//copyInstances(0, train, first);
		//copyInstances(first + numInstForFold, train, instances.numInstances() - first
		//  - numInstForFold);

		//return train;'
	}
}

/*
  public Instances testCV(int numFolds, int numFold) {

    int numInstForFold, first, offset;
    Instances test;

    if (numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2!");
    }
    if (numFolds > numInstances()) {
      throw new IllegalArgumentException(
        "Can't have more folds than instances!");
    }
    numInstForFold = numInstances() / numFolds;
    if (numFold < numInstances() % numFolds) {
      numInstForFold++;
      offset = numFold;
    } else {
      offset = numInstances() % numFolds;
    }
    test = new Instances(this, numInstForFold);
    first = numFold * (numInstances() / numFolds) + offset;
    copyInstances(first, test, numInstForFold);
    return test;
  }
*/