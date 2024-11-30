package us.hall.gc.benchmark;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean; 
import java.util.HashMap;

public class Util {
	
	private static final double BILLION = 1000000000D; 
	private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	private static final boolean cpuTimeSupported = 
										threadMXBean.isThreadCpuTimeSupported();
	private static final boolean currentSupported = 
										threadMXBean.isCurrentThreadCpuTimeSupported();

    static {
		if (cpuTimeSupported && !threadMXBean.isThreadCpuTimeEnabled()) {
			threadMXBean.setThreadCpuTimeEnabled(true);
		}
	}
											
	private static HashMap<String,GCStats> collectStats() {
		HashMap<String,GCStats> gcstats = new HashMap<String,GCStats>();
		for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
			String name = "Eden";
			if (collector.getName().endsWith("MarkSweep")  || collector.getName().endsWith("Old Generation")) 
				name = "Old";
			gcstats.put(name,
				new GCStats(collector.getCollectionCount(),collector.getCollectionTime()));        	
    	}
    	return gcstats;
	}
	
	public static long getCurrentlyUsedMemory() {
	  return
		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
		ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
	}
	
	public static long getGcCount() {
	  long sum = 0;
	  for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
		long count = b.getCollectionCount();
		if (count != -1) { sum +=  count; }
	  }
	  return sum;
	}
	
	public static long getReallyUsedMemory() {
	  long before = getGcCount();
	  System.gc();
	  while (getGcCount() == before);
	  return getCurrentlyUsedMemory();
	}
	
	public static long getSettledUsedMemory() {
  		long m;
  		long m2 = getReallyUsedMemory();
  		do {
  			try {
    			Thread.sleep(567);
    		}
    		catch (InterruptedException iex) {}
    		m = m2;
    		m2 = getCurrentlyUsedMemory();
  		} while (m2 < getReallyUsedMemory());
  		return m;
  	}
  	
  	public static MemPoolData pools(long[] peakUseds, long[] maxUseds) { 
		System.out.println("Memory Pool");
		MemPoolData poolData = new MemPoolData();
		HashMap<String,GCStats> collect = collectStats();
		for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
			if (mpBean.getType() == MemoryType.HEAP) {
				System.out.println("\tName: " + mpBean.getName());
				long peak = mpBean.getPeakUsage().getUsed();
				long used = mpBean.getUsage().getUsed();
				System.out.println("\t\tPeak: " + peak + ". Used: " + used);
				System.out.println("\t\tCollection");
				if (mpBean.getName().endsWith("Eden Space")) {
					GCStats stats = collect.get("Eden");
					long count = stats.getCount();
					poolData.setEdenGCCount(count);
					long time = stats.getTime();
					poolData.setEdenGCTime(time);
					System.out.println("\t\t\tCount: " + count + ". Time(ms): " + time);
					if (peak > peakUseds[0]) peakUseds[0] = peak;
					poolData.setEdenPeak(peak);
					if (used > maxUseds[0]) maxUseds[0] = used;
					poolData.setEdenUsed(used);
				}
				else if (mpBean.getName().endsWith("Old Gen")) {
					GCStats stats = collect.get("Old");
					long count = stats.getCount();
					poolData.setOldGCCount(count);
					long time = stats.getTime();
					poolData.setOldGCTime(time);
					System.out.println("\t\t\tCount: " + count + ". Time(ms): " + time);
					if (peak > peakUseds[1]) peakUseds[1] = peak;
					poolData.setOldPeak(peak);
					if (used > maxUseds[1]) maxUseds[1] = used;
					poolData.setOldUsed(used);
				}
				else if (mpBean.getName().endsWith("Survivor Space")) {
					if (peak > peakUseds[2]) peakUseds[2] = peak;
					poolData.setSurvPeak(peak);
					if (used > maxUseds[2]) maxUseds[2] = used;
					poolData.setSurvUsed(used);
				}
				else {
					System.out.println("Unknown memory bean " + mpBean.getName());
				}
			}
		}
		return poolData;
	}
		
	public static double threads(long currentTid, HashMap<Long,
											long[]>oldThreadStats) {
		System.out.println("Threads");
		long[] tids = threadMXBean.getAllThreadIds();
		long[] old = new long[0];
		String thread_hdr = null;
		double main_user = 0D;
		 
		if (cpuTimeSupported || currentSupported) {
			thread_hdr = String.format("%-3s %-20s %-11s %-11s","TID","NAME","USER","CPU");
		}
		else {
			thread_hdr = String.format("%-4s %-20s %-11s %-11s","TID","NAME","USER");
		}
		System.out.println(thread_hdr);
		for (long tid : tids) {
			if (oldThreadStats.containsKey(tid)) {
				old = oldThreadStats.get(tid);
			}
			else {
				old = new long[2];
			}
			String name = threadMXBean.getThreadInfo(tid).getThreadName();
			long user = threadMXBean.getThreadUserTime(tid);			
			double curr_user = (double)(user - old[0]) / BILLION;
			if (name.equals("main")) {
				main_user = curr_user;
			}
			old[0] = user;
			if (cpuTimeSupported || 
					(currentSupported && tid == currentTid)) {
				long cpu = threadMXBean.getThreadCpuTime(tid);
				double curr_cpu = (double)(cpu - old[1]) / BILLION;
				old[1] = cpu;
				String s = String.format("%03d %-20s %-11s %-11s",tid,name,curr_user,curr_cpu);
				System.out.println(s);
			}
			else {
				String s = String.format("%03d %-20s %-11s",tid,name,curr_user);
				System.out.println(s);
			}
			oldThreadStats.put(tid,old);
		}
		return main_user;
	}
}
