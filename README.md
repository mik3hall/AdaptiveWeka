# Adaptive Weka

## Background

The idea of adaptive Weka is to have code that dynamically takes advantage of current hardware capabilities and execution state. 

The idea initially came after I purchased a new Mac. It is an M3 that the System Information shows having 11 processors, 5 performance and 6 efficiency. I think previously I've never had a machine with more than 4 cores. 

A thought of what could take advantage of this was Weka cross-validation. If the machine had the 
processors these could be executed separately. 

I added this to an old existing project where I had been looking at Weka memory use. In doing some testing I copyied the garbage collection parameters from one of my applications and saw that this improved memory usage for the Weka test. I started looking at this but didn't complete anything useful. That is most of what is included here. I added what I am currently doing to it.
I may go back and cleanup and complete some of that for this effort.

Currently though a lot of this is just incomplete mess though. One thing possibly of interest is shown by the [Rplot.pdf](Rplot.pdf) file. This was testing elapsed time with repeated iterations. It can be seen that elapsed performance degrades linearly to a point. That point being a tipping point, where suddenly it starts to degrade exponentially. I remember this interested me anyhow to the extent that I spent a fair amount of time trying to determine what the exponent was. 14, as I recall, for what it's worth. Maybe I thought something could be done to somehow control or improve on that exponent so it would be good to know what it was. For most practical purposes it is probably sufficient to know that it suddently starts getting much worse, very fast, as the available memory runs out. 

I was next going to start putting something in place for measurement to csv or arff files and this is where I ran out of gas on the effort.

## Introduction

I have a couple Weka evaluation subclass's that do parallelism of cross validation. In looking at this I saw that some parallelism support has already been added to executing some of the classifiers themselves. 
When you do the buildClassifier methods I think. From the GUI it is controlled by the  
For cross-validation the wekaServer package was suggested to me on the Weka mailing list. I looked at that but for weka proper I did it somewhat differently. I have also just noticed that some attribute selection algorithms also appear to support parallelism. 

These are handled the same as is normal in Weka in that they are parameter controlled. You can indicate execution slots or it defaults to one, single threaded.. My code differs from this static approach in attempting a more dynamic default where it selects some number of parallel execution threads based on processor availability and number of folds requested. 

## Comparison Applications release

**04/06/25**

Apparently, I had corrected the problem with unrealistically better results and not noticed. After rebuilding the applications and recompiling the command line Cifar class I now get consistent results against all versions. Normal, WekaReference.app or no switches command line - adapted, WekaAdaptive.app or -AEVD=true switch - adapted with my CV instances, WekaAdaptiveCVI or 
both -AEVD=true and -ACVI=true switches.

Again, this provides multithreading on cross validations. This is probably a minimum of twice as fast as without multithreading. It can be used with any classifiers, not just the ones where Weka has determined that the classifier itself can be multi-threaded and provided 'slots' parameters for. 

For large datasets memory can be a problem. Doing more things at the same time means more memory is needed at that time and peak memory usage is higher. I have increased heap memory, -Xmx12g, on my applications and command line usage. My instances classes that reference the same single instances so far doesn't seem to do much to mitigate this. Adaptive was intended to also adapt to available memory and the size of the instances. Somehow determining how many additional threads to use or possibly throttling execution of active threads.

That is where any additional effort will probably go.

I believe that this proves that Weka could get significant performance gains by multithreading cross validation. Mark Hall's wekaServer package already proved the feasibility, by doing it there. This shows the feasibility of doing it from the GUI and Weka core.

**01/15/25**

I have put out a MacOS dmg release with two applications. These are not intended to be official [Weka](https://sourceforge.net/projects/weka/) distributions. They are certainly incomplete and very probably very buggy for much of the usual functionality. They are for comparing results from changes that are made or additions to Weka code as part of this project. There is a WekaReference.app which is pretty much a (stripped down) version of the usual Weka Explorer application. Except, that after running a classification it should show a summary of memory pool and garbage collection information as well as classification elapsed time. Mainly of interest is cross validation where my multi-threaded changes apply. The other application is WekaAdaptive.app, which includes changes for this purpose. 

This should make it easier to test these changes a little bit without needing to mess with command line or coding your own java for classifications. It also is a sort of demo that changes like this are possible for the Weka Explorer application itself.  

This is MacOS only because although jpackage works fine, VirtualBox seemed to become more difficult. It was some effort to provide cross-platform versions even when VirtualBox worked fairly reliably. You could look at weka_jpkg.sh or weka_adapt_jpkg.sh to get an idea of how I did this for MacOS. Maybe anyone interested could get an idea how to use jpackage for this on other platforms. The jpackage difference between the two is the inclusion of the -DAEVD=true switch in the adapt version to activate my changes in the Weka ClassifierPanel class. 

This also includes -Djava.security.manager=allow which allows the application to work with a bundled jdk 22 release. I think this was a question at one point on the Weka mailing list. Whether my versions work at jdk 22 with the Weka package manager custom classloader I haven't tested. 

This did require more changes to Weka proper code. I put those into the src_weka directory. 

I have noticed two current issues. The adapt app sometimes shows working on a fold 11. Whether it is actually doing an extra fold or showing an incorrect fold number on a bad index I don't know yet.

weka.log shows this error...

```java.lang.Exception: PlotData2D: Shape type vector must have the same number of entries as number of data points!```

This one is suddenly new...

```weka.core.WekaException: Cannot run program "python": error=2, No such file or directory```

The application again supports things that are currently not a concern of my testing.

## Benchmarking

I have moved the current [benchmarking](benchmark/benchmark.md) related.

## AdaptiveCVInstances

**12/23/24**

I started testing the Cifar data mentioned below. I was getting unusually good results using this. With the same classifiers, the same random seed(s(?)), and the same data there seemed to be no good reason for this. I thought I must have messed up and was training with the test data somehow. I did some debugging and ended up fixing some CVFoldInfo indexing on the instance method. This included adding a verify method in the AdaptiveEvaluationDelegate method to ensure my instances matched the buitin. 

The better results persist. With AdaptiveCVInstances...

```Correct: 70.16248466127873. Time: 118530```

Without AdaptiveCVInstances...

```Correct: 47.05246944450001. Time: 130558```

Yes, the little bit quicker is I think reasonable. Although I still don't understand the improved accuracy. For now anyhow I have bested the **Miscellaneous** challenge below.

The current tests invoked with...

```java --add-exports java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar:/Users/mjh/wekafiles/packages/imageFilters/imageFilters.jar:/Users/mjh/wekafiles/packages/ImageFilters/lib/lire.jar -DACVI=true Cifar /Users/mjh/Documents/weka_other/cifar10/cifar.arff```

___

A Instances subclass that includes a CVFoldInfo which allows indexing off of a reference to a single Instances object for cross validation with AdaptiveEvaluation. This is instead of using the trainCV and testCV Instances methods which take copies of the base Instances for each fold. As far as I know all access to Instances in cross validation is read only. The CVFoldInfo is
unique to each fold so is not a shared resource. Therefore I think, for now, that this is thread safe. 

I included displays of memory pool information at [memory](benchmark/memory). This didn't work with the ZGC garbage collector which had seemed best so this is based on default garbage collection.

To allow comparison with or without AdaptiveCVInstances I added a system property at lauch. ACVI. Used as in...  

`java --add-exports java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar -DACVI=true RFAdaptive -b mnist.arff`

If the -DACVI=true is omitted then AdaptiveCVInstances aren't used. 

The memory benchmark runs didn't appear to show the dramatic improvement I thought I might get. I still haven't tried coming up with a test case that shows that it might avoid some out of memory errors. 

## Miscellaneous

I had included the [cifar 10](https://en.wikipedia.org/wiki/CIFAR-10) dataset in the project. GitHub objected to the size. After looking at the [Weka MOOC for imaging](https://www.youtube.com/watch?app=desktop&v=XBSJOkuAtCw&t=185s) I made the changes needed for that and uploaded to my site [cifar10.zip](http://mikehall.pairserver.com/cifar10.zip). I have successfully managed to totally blow out memory a number of times with this. My best classification was low to mid 50%. I was thinking about including it in my benchmarking but was hoping for better results first and haven't done so yet. If anyone cracks 60% accuracy let me know.

