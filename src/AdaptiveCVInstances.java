package weka.core;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.evaluation.CVFoldInfo;

public class AdaptiveCVInstances extends Instances {
	
	CVFoldInfo info = null;
	Instances instances = null;
    /** The class attribute's index */
    //protected int m_ClassIndex;
  	
	public AdaptiveCVInstances(Instances instances, CVFoldInfo info) throws IOException {
		super(instances.m_RelationName,instances.m_Attributes,0);
		initialize(instances);
		this.instances = instances;
		this.info = info;
	}
	
	public AdaptiveCVInstances(Instances instances, CVFoldInfo info, Random random) 
		throws IOException {
		
		this(instances, info);
		
		// TODO
		// randomize first part of train 
		// randomize second part of train
	}
 
  /**
   * initializes with the header information of the given dataset and sets the
   * capacity of the set of instances.
   * 
   * @param dataset the dataset to use as template
   * @param capacity the number of rows to reserve
   */
  public void initialize(Instances dataset) {
    /*
    if (capacity < 0) {
      capacity = 0;
    }
	*/
    // Strings only have to be "shallow" copied because
    // they can't be modified.
    m_ClassIndex = dataset.m_ClassIndex;
    m_RelationName = dataset.m_RelationName;
    m_Attributes = dataset.m_Attributes;
    m_NamesToAttributeIndices = dataset.m_NamesToAttributeIndices;
    //m_Instances = new ArrayList<Instance>(capacity);
  }
   
  /**
   * Returns the class attribute.
   * 
   * @return the class attribute
   * @throws UnassignedClassException if the class is not set
   */
  // @ requires classIndex() >= 0;
  public/* @pure@ */Attribute classAttribute() {
    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    return attribute(m_ClassIndex);
  }

 /**
   * Returns the class attribute's index. Returns negative number if it's
   * undefined.
   * 
   * @return the class index as an integer
   */
  // ensures \result == m_ClassIndex;
  public/* @pure@ */int classIndex() {

    return m_ClassIndex;
  }
  	
	// Does this need to be synchronized?
	// The only shared resource is the actual instances and
	// all accesses are read so I'm thinking not
	// also given that info isn't shared so another thread invoking with 
	// a different index shouldn't mess it up?
	 /**
	  * Returns the instance at the given position.
      * 
      * @param index the instance's index (index starts with 0)
      * @return the instance at the given position
      */
    // @ requires 0 <= index;
    // @ requires index < numInstances();
    int cnt = 0;
	public Instance instance(int i) {
		//return instances.instance(info.instance(i));
		Instance instance = instances.instance(info.instance(i));
		/*
		if (cnt < 10) {
			System.out.println(instance);
			cnt++;
		}
		*/
		return instance;
	}
	
	public void debugInstance(int i) {
		System.out.println("For requested instance index " + i);
		System.out.println("Base instances index " + info.instance(i));
		System.out.println("Base instances num instances " + instances.numInstances());
		System.out.println(info);
	}
	
   /**
    * Returns the number of instances in the dataset.
    * 
    * @return the number of instances in the dataset as an integer
    */
    // @ ensures \result == m_Instances.size();
    public/* @pure@ */int numInstances() {
    	return info.numInstances();
    }

  /**
   * Copies instances from one set to the end of another one.
   * 
   * @param from the position of the first instance to be copied
   * @param dest the destination for the instances
   * @param num the number of instances to be copied
   */
  // @ requires 0 <= from && from <= numInstances() - num;
  // @ requires 0 <= num;
  protected void copyInstances(int from, /* @non_null@ */Instances dest, int num) {
    for (int i = 0; i < num; i++) {
      dest.add(instance(from + i));
    }
  }
}

