package us.hall.weka.benchmark;

import us.hall.gc.benchmark.MemPoolData;

// Data that changes each iteration
public class IterationRecord {
	int iteration = 0;
	long itermem = 0L;
	long elapsed = 0L;
	long rttotal = 0L;
	double user = 0D;
	MemPoolData poolData = null;
	DataRecord datarec = null;
	private static final String NL = System.getProperty("line.separator","\n");

	private static final String arffPrefix = "@RELATION RFIterationBenchmark" + NL +
	    "" + NL +
	    "@ATTRIBUTE iteration     NUMERIC" + NL +
	    "@ATTRIBUTE instances     NUMERIC" + NL +
	    "@ATTRIBUTE attributes    NUMERIC" + NL + 
	    "@ATTRIBUTE runtime_total NUMERIC" + NL +
	    "@ATTRIBUTE runtime_max   NUMERIC" + NL +
	    "@ATTRIBUTE elapsed       NUMERIC" + NL + 
	    "@ATTRIBUTE eden_count    NUMERIC" + NL + 
	    "@ATTRIBUTE eden_time     NUMERIC" + NL +
	    "@ATTRIBUTE eden_peak     NUMERIC" + NL + 
	    "@ATTRIBUTE eden_used     NUMERIC" + NL +
	    "@ATTRIBUTE old_count     NUMERIC" + NL +
	    "@ATTRIBUTE old_time      NUMERIC" + NL +
	    "@ATTRIBUTE old_peak      NUMERIC" + NL + 
	    "@ATTRIBUTE old_used      NUMERIC" + NL +
	    "@ATTRIBUTE surv_peak     NUMERIC" + NL + 
	    "@ATTRIBUTE surv_used     NUMERIC" + NL + 
	    "@ATTRIBUTE thread_user   NUMERIC" + NL +
	    "@ATTRIBUTE iter_mem      NUMERIC" + NL + 
	    "" + NL +
	    "@DATA" + NL;
	
	private static final String csvPrefix = "iteration,instances,attributes,runtime_total," +
	    "runtime_max,elapsed,eden_count,eden_time,eden_peak,eden_used,old_count,old_time," +
	    "old_peak,old_used,surv_peak,surv_used,thread_used,iter_mem" + NL;
	    
	public IterationRecord(int iteration,DataRecord datarec) {
		this.iteration = iteration;
		this.datarec = datarec;
	}
	
	public static final String getArffPrefix() {
		return arffPrefix;
	}
	
	public static final String getCsvPrefix() {
		return csvPrefix;
	}
	
	public void setRTMax(long rttotal) {
		this.rttotal = rttotal;
	}
	
	public void setElapsed(long elapsed) {
		this.elapsed = elapsed;
	}
	
	public void setUser(double user) {
		this.user = user;
	}
	
	public void setIterMem(long itermem) {
		this.itermem = itermem;
	}
	
	public void setPoolData(MemPoolData poolData) {
		this.poolData = poolData;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(iteration).append(",");
		sb.append(datarec.instances).append(",");
		sb.append(datarec.attributes).append(",");
		sb.append(datarec.rtMax).append(",");
		sb.append(rttotal).append(",");
		sb.append(elapsed).append(",");
		sb.append(poolData.getEdenGCCount()).append(",");
		sb.append(poolData.getEdenGCTime()).append(",");
		sb.append(poolData.getEdenPeak()).append(",");
		sb.append(poolData.getEdenUsed()).append(",");
		sb.append(poolData.getOldGCCount()).append(",");
		sb.append(poolData.getOldGCTime()).append(",");
		sb.append(poolData.getOldPeak()).append(",");
		sb.append(poolData.getOldUsed()).append(",");
		sb.append(poolData.getSurvPeak()).append(",");
		sb.append(poolData.getSurvUsed()).append(",");
		sb.append(user).append(",");
		sb.append(itermem).append(NL);
		return sb.toString();
	}
}