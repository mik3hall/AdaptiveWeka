import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import weka.core.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.AdaptiveEvaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Vote;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.imagefilter.ColorLayoutFilter;
import weka.filters.unsupervised.instance.imagefilter.EdgeHistogramFilter;
import us.hall.gc.benchmark.MemPoolData;
import us.hall.gc.benchmark.Util;

public class Cifar {

	private static final int seed = 123;
	private static long[] peakUseds = new long[] { 0L, 0L, 0L };
	private static long[] maxUseds = new long[] { 0L, 0L, 0L}; 
		
	public static void main(String... args) {
		String trainingName = args[0];
		try {
			weka.core.WekaPackageManager.loadPackages(false, true, false);
		}
		catch (Exception ex) { 
			ex.printStackTrace();
			return;
		}
		try {
			BufferedReader trainingRdr = new BufferedReader(new FileReader(new File(trainingName)));
			Instances trainingData = new Instances(trainingRdr);
			trainingData.setClassIndex(trainingData.numAttributes() - 1);
			ColorLayoutFilter clf = new ColorLayoutFilter();
			clf.setInputFormat(trainingData);
			clf.setImageDirectory("/Users/mjh/Documents/weka_other/cifar10/train");
			trainingData = Filter.useFilter(trainingData, clf);
			EdgeHistogramFilter ehf = new EdgeHistogramFilter();
			ehf.setInputFormat(trainingData);
			trainingData = Filter.useFilter(trainingData, ehf);
			Remove remove = new Remove();
			remove.setAttributeIndices("1");			// filename attribute
			remove.setInputFormat(trainingData);
			trainingData = Filter.useFilter(trainingData, remove);
			Vote voter = new Vote();	// Get the Vote meta classifier 
			Classifier dl4j = (Classifier)Utils.forName(Classifier.class,
				"weka.classifiers.functions.Dl4jMlpClassifier",new String[0]);	
			RandomForest rf = new RandomForest();	
			System.out.println("RF parameters:");
			String[] options = rf.getOptions();
			for (int i = 0; i < options.length; i += 2) {
				System.out.println("\t"  + options[i] + "\t" + options[i+1]);
			}
			IBk ibk = new IBk();
			System.out.println("IBk parameters:");
			options = ibk.getOptions();
			for (int i = 0; i < options.length; i += 2) {
				System.out.println("\t"  + options[i] + "\t" + options[i+1]);
			}
			NaiveBayes nb = new NaiveBayes();
			options = nb.getOptions();
			System.out.println("nb parameters:");
			for (int i = 0; i < options.length; i += 2) {
				System.out.println("\t"  + options[i] + "\t" + options[i+1]);
			}			
			voter.setClassifiers(new Classifier[] { rf, ibk, nb});
			long adaptIn = System.currentTimeMillis();
			Evaluation eval = adaptiveEvaluate(voter, 10, trainingData);	
			long adaptTime = System.currentTimeMillis() - adaptIn;
			System.out.println("Correct: " + eval.pctCorrect() + ". Time: " + adaptTime);		
			MemPoolData poolData = Util.pools(peakUseds,maxUseds);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static Evaluation adaptiveEvaluate(Classifier classifier, int folds, Instances trainingData) throws Exception {
		Evaluation eval = (Evaluation)new AdaptiveEvaluation(trainingData);
		eval.crossValidateModel(classifier, trainingData, folds, new java.util.Random(seed));
		System.out.println(eval.toClassDetailsString());

        // Print confusion matrix
        System.out.println("Confusion matrix:");
        double[][] cm = eval.confusionMatrix();
        for (int i = 0; i < cm.length; i++) {
            for (int j = 0; j < cm[i].length; j++) {
                System.out.print(cm[i][j] + " ");
            }
            System.out.println();
        }
		return eval;
	}
}
