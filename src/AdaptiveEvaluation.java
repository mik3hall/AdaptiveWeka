package weka.classifiers;

import java.util.Random;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class AdaptiveEvaluation extends Evaluation {

  /** The actual evaluation object that we delegate to */
  //protected weka.classifiers.evaluation.AdaptiveEvaluationDelegate m_delegate;
  
  public AdaptiveEvaluation(Instances data) throws Exception {
  	super(data);
    m_delegate = new weka.classifiers.evaluation.AdaptiveEvaluationDelegate(data);
  }

  public AdaptiveEvaluation(Instances data, CostMatrix costMatrix) throws Exception {
  	super(data, costMatrix);
    m_delegate = new weka.classifiers.evaluation.AdaptiveEvaluationDelegate(data, costMatrix);
  }
  
  public void setThreadCnt(int threadCnt) {
  	((weka.classifiers.evaluation.AdaptiveEvaluationDelegate)m_delegate).setThreadCnt(threadCnt);
  }
  
  public int getThreadCnt() {
  	return ((weka.classifiers.evaluation.AdaptiveEvaluationDelegate)m_delegate).getThreadCnt();
  }
  
  /**
   * Performs a (stratified if class is nominal) cross-validation for a
   * classifier on a set of instances. Now performs a deep copy of the
   * classifier before each call to buildClassifier() (just in case the
   * classifier is not initialized properly).
   *
   * @param classifier the classifier with any options set.
   * @param data the data on which the cross-validation is to be performed
   * @param numFolds the number of folds for the cross-validation
   * @param random random number generator for randomization
   * @throws Exception if a classifier could not be generated successfully or
   *           the class is not defined
   */
  public void crossValidateModel(Classifier classifier, Instances data, int numFolds, Random random)
          throws Exception {
    m_delegate.crossValidateModel(classifier, data, numFolds, random);
  }
  
} 