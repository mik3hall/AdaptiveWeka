package us.hall.weka.benchmark;

import weka.core.Instances;

// Data fixed for entire run
public class DataRecord {
	int instances = 0, attributes = 0;
	long rtMax = 0L;
	
	DataRecord(Instances trainingData, long rtMax) {
		instances = trainingData.numInstances();
		attributes = trainingData.numAttributes();
		this.rtMax = rtMax;
	}
	
}