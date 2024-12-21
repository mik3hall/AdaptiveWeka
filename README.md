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

## Benchmarking

I have moved the current [benchmarking](benchmark/benchmark.md) related.

## AdaptiveCVInstances

A Instances subclass that includes a CVFoldInfo which allows indexing off of a reference to a single Instances object for cross validation with AdaptiveEvaluation. This is instead of using the trainCV and testCV Instances methods which take copies of the base Instances for each fold. As far as I know all access to Instances in cross validation is read only. The CVFoldInfo is
unique to each fold so is not a shared resource. Therefore I think, for now, that this is thread safe. 

I included displays of memory pool information at [memory](benchmark/memory). This didn't work with the ZGC garbage collector which had seemed best so this is based on default garbage collection.

To allow comparison with or without AdaptiveCVInstances I added a system property at lauch. ACVI. Used as in...  

`java --add-exports java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar -DACVI=true RFAdaptive -b mnist.arff`

If the -DACVI=true is omitted then AdaptiveCVInstances aren't used. 

The memory benchmark runs didn't appear to show the dramatic improvement I thought I might get. I still haven't tried coming up with a test case that shows that it might avoid some out of memory errors. 

## Miscellaneous

I had included the [cifar 10](https://en.wikipedia.org/wiki/CIFAR-10) dataset in the project. GitHub objected to the size. After looking at the [Weka MOOC for imaging](https://www.youtube.com/watch?app=desktop&v=XBSJOkuAtCw&t=185s) I made the changes needed for that and uploaded to my site [cifar10.zip](http://mikehall.pairserver.com/cifar10.zip). I have successfully managed to totally blow out memory a number of times with this. My best classification was low to mid 50%. I was thinking about including it in my benchmarking but was hoping for better results first and haven't done so yet. If anyone cracks 60% accuracy let me know.

