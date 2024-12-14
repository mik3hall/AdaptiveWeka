## Benchmarking

My current build is...

```javac -d . --add-exports java.base/java.lang=ALL-UNNAMED -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar src/*.java```

Currently a simple benchmark is run with...

```java --add-exports java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -cp .:/Applications/weka-3.9.6.app/Contents/app/weka.jar RFAdaptive -b mnist.arff```

It is the 11.2MB mnist dataset as [here](https://github.com/christopher-beckham/weka-pyscript/blob/master/datasets/mnist.arff)

Initally, Random Forest execution slot performance was somewhat faster than threaded cross validation. So I mixed using that as well if the processors seem available with...

``` 		if (copiedClassifier instanceof ParallelIteratedSingleClassifierEnhancer) {
  			int avail = Runtime.getRuntime().availableProcessors();
  			int active = Thread.activeCount();
  			if (avail - active > 3) {
  				((ParallelIteratedSingleClassifierEnhancer)copiedClassifier).setNumExecutionSlots(2);
  			}
```

Although activeCount seems to only apply to the current thread group, possibly underestimating actual processesors active(?), this improved performance to give the best execution times. If you set this yourself I might override it, I generally assume threading is being left to my code.

The simple code I have to choose default thread pool sizes per fold doesn't always, even often, select the optimal size. If you, maybe, benchmark youself and determine some other size is better it can be forced to that size with...

```((AdaptiveEvaluation)eval).setThreadCnt(threadCnt);```

The per thread output shows that the requested always matches the used, as well as milliseconds.

```2 7817 used 2```

The [initial benchmark](initial.md) results.

Memory was the motivation for how this effort began. I noticed in profiling, with jconsole if I recall correctly, that using the same garbage collection parameters as one of my applications resulted in better management of a large amount of data.

My goals were determining which classification algorithms scaled best in memory size. Which garbage collection options worked best, possibly per classifier. Could something possibly be done with Weka's memory management code? Something from newer releases of the jdk.

Currently to start I did the same benchmark using the different available garbage collection options. Default with no parameter tuning. Except for G1GC I used the values I had originally tested with from my other application. I think I accidentally carried these over to the Shenandoah testing which I should re-do that at some point. It is possible the -Xmx1024m I use there is actually not helping the results. It still works fine with my app but could currently probably be much larger. Or be omitted. 

For Shenendoah I couldn't use the Oracle jdk which doesn't include it. I used the
```
OpenJDK Runtime Environment Zulu17.32+13-CA (build 17.0.2+8-LTS)
```
that is included with the Weka 3.9.6 application.

The benchmark/default_gc/default_gc.txt file contains what was done and gc log summaries from the [Garbage Collection and Memory Visualizer](https://www.ibm.com/support/pages/garbage-collection-and-memory-visualizer) application. [GCViewer](https://www.tagtraum.com/gcviewer.html) also worked fine but didn't allow cut and paste of the summary information. Neither worked with the modern Shenandoah or ZGC collector logs. 

The gclog\_\*.txt files are the raw gc logs. The log\_\*.txt files are the benchmark runs.
