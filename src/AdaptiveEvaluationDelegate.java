package weka.classifiers.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.ParallelIteratedSingleClassifierEnhancer;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.core.UnassignedClassException;
import weka.core.Instance;
import weka.core.AdaptiveCVInstances;
import weka.core.Instances;

public class AdaptiveEvaluationDelegate extends Evaluation {
  
  private int threadCnt = 0;
  private static final boolean isACVI;   // Use AdaptiveCVInstances?
  
  static {
  	isACVI = Boolean.getBoolean("ACVI");
  }
  /**
   * Initializes all the counters for the evaluation. Use
   * <code>useNoPriors()</code> if the dataset is the test set and you can't
   * initialize with the priors from the training set via
   * <code>setPriors(Instances)</code>.
   *
   * @param data set of training instances, to get some header information and
   *             prior class distribution information
   * @throws Exception if the class is not defined
   * @see #useNoPriors()
   * @see #setPriors(Instances)
   */
  public AdaptiveEvaluationDelegate(Instances data) throws Exception {
    super(data, null);
  }

  /**
   * Initializes all the counters for the evaluation and also takes a cost
   * matrix as parameter. Use <code>useNoPriors()</code> if the dataset is the
   * test set and you can't initialize with the priors from the training set via
   * <code>setPriors(Instances)</code>.
   *
   * @param data       set of training instances, to get some header information and
   *                   prior class distribution information
   * @param costMatrix the cost matrix---if null, default costs will be used
   * @throws Exception if cost matrix is not compatible with data, the class is
   *                   not defined or the class is numeric
   * @see #useNoPriors()
   * @see #setPriors(Instances)
   */
  public AdaptiveEvaluationDelegate(Instances data, CostMatrix costMatrix) throws Exception {
    super(data, costMatrix);
  }
   /**
    * Thread count to be used in cross validation if manually set
    * 
    * @param threadCnt  number of threads to use
    */
  public void setThreadCnt(int threadCnt) {
  	this.threadCnt = threadCnt;
  }
  
  /**
   * Return setting of thread count to be used in cross validation.
   * May or may not be manually set by setThreadCnt
   *
   * @see #setThreadCount
   */
  public int getThreadCnt() {
  	return threadCnt;
  }
  
  public void crossValidateModel(Classifier classifier, Instances data,
                                 int numFolds, Random random, Object... forPrinting)
          throws Exception {
	
    // Make a copy of the data we can reorder
    data = new Instances(data);
    data.randomize(random);
    if (data.classAttribute().isNominal()) {
      data.stratify(numFolds);
    }
	// We assume that the first element is a
	// weka.classifiers.evaluation.output.prediction.AbstractOutput object
	AbstractOutput classificationOutput = null;
	if (forPrinting.length > 0 && forPrinting[0] instanceof AbstractOutput) {
	  // print the header first
	  classificationOutput = (AbstractOutput) forPrinting[0];
	  classificationOutput.setHeader(data);
	  classificationOutput.printHeader();
	}
	int avail = Runtime.getRuntime().availableProcessors();
	if (avail > 3) {
		if (threadCnt == 0) {
			if (avail > numFolds / 2 + 2) {
				threadCnt = numFolds / 2;
				if (numFolds % 2 != 0) {
					threadCnt += 1;
				} 
			}
			else {
				threadCnt = avail - 2;
			}
			if (threadCnt > numFolds) {		// Q. can this happen?
				threadCnt = numFolds;
			}
		}
	}
	else {
		threadCnt = 1;
	}
    ExecutorService executor = Executors.newFixedThreadPool(threadCnt);
    List<Callable<Void>> cvTasks = new ArrayList<>();
    // Do the folds
    for (int i = 0; i < numFolds; i++) {
      cvTasks.add(new CrossValidation(numFolds, random, i+1, data, classifier, forPrinting));
    }
    executor.invokeAll(cvTasks);
    executor.shutdown();
  }  
  
  public class CrossValidation implements Callable<Void> {
  	int numFolds = 0;
  	Random random = null;
  	int fold = 0;
  	Instances data;
  	Classifier classifier = null;
  	Object[] forPrinting;
  	AbstractOutput classificationOutput = null;
  	
  	CrossValidation(int numFolds, Random random, int fold, Instances data, Classifier classifier, Object... forPrinting) {
  		this.numFolds = numFolds;
  		this.random = random;
  		this.fold = fold;
  		this.classifier = classifier;
  		this.forPrinting = forPrinting;
  		this.data = data;
  	}
  	
  	public Void call() throws Exception {
		try {
			Instances train, test;
			if (!isACVI) {
				train = data.trainCV(numFolds, fold-1, random);
		        test = data.testCV(numFolds, fold-1);  	
			}
			else {
				CVFoldInfo trainInfo = new CVFoldInfo(data, numFolds, fold-1, true);
				CVFoldInfo testInfo = new CVFoldInfo(data, numFolds, fold-1, false);
				//verify(data, trainInfo, testInfo);
				train = new AdaptiveCVInstances(data, trainInfo);
				test = new AdaptiveCVInstances(data, testInfo); 
			}
			if (forPrinting.length > 0 && forPrinting[0] instanceof AbstractOutput) {
				// print the header first
				classificationOutput = (AbstractOutput) forPrinting[0];
				classificationOutput.setHeader(train);
				classificationOutput.printHeader();
			}
			setPriors(train);
			Classifier copiedClassifier = AbstractClassifier.makeCopy(classifier);
			if (copiedClassifier instanceof ParallelIteratedSingleClassifierEnhancer) {
				int avail = Runtime.getRuntime().availableProcessors();
				int active = Thread.activeCount();
				if (avail - active > 3) {
					((ParallelIteratedSingleClassifierEnhancer)copiedClassifier).setNumExecutionSlots(2);
				}
			}
			copiedClassifier.buildClassifier(train);
			if (classificationOutput == null && forPrinting.length > 0) {
				((StringBuffer)forPrinting[0]).append("\n=== Classifier model (training fold " + fold +") ===\n\n" +
					classifier);
			}
			if (classificationOutput != null){
				evaluateModel(copiedClassifier, test, forPrinting);
			} else {
				try {
					evaluateModel(copiedClassifier, test);
				}
				catch (Exception ex) { ex.printStackTrace(); }
			}
			if (classificationOutput != null) {
				classificationOutput.printFooter();
			}
		}
		catch (Throwable tossed) {
			tossed.printStackTrace();
		}
  		return null;
  	}
  	
  	private void verify(Instances data, CVFoldInfo trainInfo, CVFoldInfo testInfo) 
  		throws IOException {
  		
		Instances train = data.trainCV(numFolds, fold-1, random);
		Instances test = data.testCV(numFolds, fold-1);
		Instances trainI = new AdaptiveCVInstances(data, trainInfo);
		Instances testI = new AdaptiveCVInstances(data, testInfo); 	
		int trainNum = train.numInstances();
		System.out.println("train number of instances " + trainNum);
		if (trainNum != trainI.numInstances()) {
			throw new IllegalStateException("AED mismatch on number of instances");
		}
		for (int i=0; i<trainNum; i++) {
			if (!instanceEquals(train.instance(i),trainI.instance(i))) {
				System.out.println("AED Failed on train for fold " + fold);
				System.out.println(trainInfo);
				throw new IllegalStateException("AED Train error on instance " + i + " info " + trainInfo.instance(i));
			}
		}
		System.out.println("AED train success on fold " + fold);
		int testNum = test.numInstances();
		for (int i=0; i<testNum; i++) {
			if (!instanceEquals(test.instance(i),testI.instance(i))) {
				System.out.println("AED Failed on test for fold " + fold);
				System.out.println(testInfo);
				throw new IllegalStateException("AED Test error on instances " + i + " info " + trainInfo.instance(i));
			}
		}
		System.out.println("AED test success on fold " + fold);
  	}
  	 
  	private boolean instanceEquals(Instance instance, Instance other) {
  		int instanceAttrNum = instance.numAttributes();
  		int otherAttrNum = other.numAttributes();
  		if (instanceAttrNum != otherAttrNum) {
  			return false;
  		}
  		for (int i = 0; i < instanceAttrNum; i++) {
  			if (!instance.attribute(i).equals(other.attribute(i))) {
  				return false;
  			}
  		}
  		return true;
  	}
  }
}