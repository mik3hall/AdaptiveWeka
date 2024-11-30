package us.hall.weka;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Puk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Remove attributes and then check with IBk if classification performance improves with them gone
 * If it does then double check with RandomForest and see if that improves as well
 * Based on the Weka Remove example:
 * http://weka.wikispaces.com/Remove+Attributes
 * To-do - variation of code that checks for improvement of a specific class or classes with attribute 
 * removal.
 **/ 
public class LessIsMore {

	public static void main(String[] args) {
		Instances       inst;
		Instances       instNew;
		Remove          remove;
		double			knn_iCorrect = 0d, knn_iWrong = 0d;
		double			knn_pCorrect = 0d, knn_pWrong = 0d;
		double			rf_iCorrect = 0d, rf_iWrong = 0d;
		double			rf_pCorrect = 0d, rf_pWrong = 0d;
		double			smo_iCorrect = 0d, smo_iWrong = 0d;
		double			smo_pCorrect = 0d, smo_pWrong = 0d;
		
		try {
			inst   = new Instances(new BufferedReader(new FileReader(args[0])));
			inst.setClassIndex(inst.numAttributes() - 1);
			System.out.println("Doing initial RF evaluation...");
/*
			SMO smo = new SMO();
			smo.setBuildLogisticModels(true);
			smo.setC(6.0d);
			Puk puk = new Puk();
			smo.setKernel(puk);
			Evaluation eval = new Evaluation(inst);
			eval.crossValidateModel(smo, inst, 10, new java.util.Random(1));
			smo_iCorrect = eval.correct();
			smo_iWrong = eval.incorrect();
			smo_pCorrect = eval.pctCorrect();
			smo_pWrong = eval.pctIncorrect();
			System.out.println("SMO: Correct\t" + smo_iCorrect + "\t(" + smo_pCorrect + ")");
			System.out.println("SMO: Incorrect\t" + smo_iWrong + "\t(" + smo_pWrong + ")");

			System.out.println("Doing initial IBk evaluation...");
			IBk knn = new IBk();
			Evaluation eval = new Evaluation(inst);
			eval.crossValidateModel(knn, inst, 10, new java.util.Random(1));
			knn_iCorrect = eval.correct();
			knn_iWrong = eval.incorrect();
			knn_pCorrect = eval.pctCorrect();
			knn_pWrong = eval.pctIncorrect();
			System.out.println("IBk: Correct\t" + knn_iCorrect + "\t(" + knn_pCorrect + ")");
			System.out.println("IBk: Incorrect\t" + knn_iWrong + "\t(" + knn_pWrong + ")");
*/
			System.out.println("Doing initial Random Forest evaluation...");
			RandomForest rf = new RandomForest();
			rf.setMaxDepth(0);
			rf.setNumFeatures(14);
			rf.setNumTrees(30);
			Evaluation rfEval = new Evaluation(inst);
			rfEval.crossValidateModel(rf, inst, 10, new java.util.Random(1));
			rf_iCorrect = rfEval.correct();
			rf_iWrong = rfEval.incorrect();
			rf_pCorrect = rfEval.pctCorrect();
			rf_pWrong = rfEval.pctIncorrect();
			System.out.println("RF: Correct\t" + rf_iCorrect + "\t(" + rf_pCorrect + ")");
			System.out.println("RF: Incorrect\t" + rf_iWrong + "\t(" + rf_pWrong + ")");

			for (int i=0;i<inst.numAttributes();i++) {
				System.out.println("Checking attribute " + inst.attribute(i).name());
				remove = new Remove();
				remove.setAttributeIndices(new Integer(i+1).toString());
	//			remove.setInvertSelection(new Boolean(args[2]).booleanValue());
				remove.setInputFormat(inst);
				instNew = Filter.useFilter(inst, remove);
				instNew.setClassIndex(instNew.numAttributes() - 1);
//				knn = new IBk();
//				eval = new Evaluation(instNew);

/*
				smo = new SMO();
				smo.setBuildLogisticModels(true);
				smo.setC(6.0d);
				puk = new Puk();
				smo.setKernel(puk);
				eval.crossValidateModel(smo, instNew, 10, new java.util.Random(1));
//				eval.crossValidateModel(knn, instNew, 10, new java.util.Random(1));
//				if (eval.correct() > knn_iCorrect) {		// Q. Improvement?
				if (eval.correct() > smo_iCorrect) {		// Q. Improvement?
					System.out.println("***** IMPROVEMENT ****** on removal of (" + i + ") " + inst.attribute(i).name());
					System.out.println("SMO:\t+" + (eval.correct()-smo_iCorrect) + "\t(+" + (eval.pctCorrect()-smo_pCorrect)+")");
//					System.out.println("IBk:\t+" + (eval.correct()-knn_iCorrect) + "\t(+" + (eval.pctCorrect()-knn_pCorrect)+")");
					System.out.println("Checking Random Forest for improvement...");
*/

					rf = new RandomForest();
					rf.setMaxDepth(0);
					rf.setNumFeatures(14);
					rf.setNumTrees(30);
					rfEval = new Evaluation(instNew);
					rfEval.crossValidateModel(rf, instNew, 10, new java.util.Random(1));
					if (rfEval.correct() > rf_iCorrect) {
						System.out.println("***** IMPROVEMENT ****** on removal of (" + i + ") " + inst.attribute(i).name());
						System.out.println("RF:\t+" + (rfEval.correct()-rf_iCorrect) + "\t(+" + (rfEval.pctCorrect()-rf_pCorrect)+")");
					}
//					else System.out.println("Random Forest did not improve");
				else System.out.println(inst.attribute(i).name() + " removal did not improve");
					System.out.println("*********************************************************************************");
//				}
//				else System.out.println(inst.attribute(i).name() + " removal did not improve");
			}

			// Check for pairs
/*
			for (int i=0;i<inst.numAttributes();i++) { 
				for (int j=0;j<inst.numAttributes();j++) {
					if (i == j) continue;
					remove = new Remove();
					remove.setAttributeIndices(new StringBuilder(new Integer(i+1).toString()).append(",").append(new Integer(j+1).toString()).toString());
					remove.setInputFormat(inst);
					instNew = Filter.useFilter(inst, remove);
					instNew.setClassIndex(instNew.numAttributes() - 1);		
					rf = new RandomForest();
					rf.setMaxDepth(0);
					rf.setNumFeatures(14);
					rf.setNumTrees(30);
					rfEval = new Evaluation(instNew);
					rfEval.crossValidateModel(rf, instNew, 10, new java.util.Random(1));
					if (rfEval.correct() > rf_iCorrect) {
						System.out.println("***** IMPROVEMENT ****** on removal of (" + i + ") " + inst.attribute(i).name() + " and (" + j + ") " + inst.attribute(j).name());
						System.out.println("RF:\t+" + (rfEval.correct()-rf_iCorrect) + "\t(+" + (rfEval.pctCorrect()-rf_pCorrect)+")");
					}
					else System.out.println(inst.attribute(i).name() + " removal did not improve");			
				}
			}
*/
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
		catch (Exception ex) { ex.printStackTrace(); }
	}
}