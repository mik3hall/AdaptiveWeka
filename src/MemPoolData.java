package us.hall.gc.benchmark;

public class MemPoolData {

	long edencount = 0L, edentime = 0L, edenpeak = 0L, edenused = 0L;
	long oldcount = 0L, oldtime = 0L, oldpeak = 0L, oldused = 0L;
	long survpeak = 0L, survused = 0L;
	
	void setEdenGCCount(long edencount) {
		this.edencount = edencount;
	}
	
	public long getEdenGCCount() { return edencount; }
	
	void setEdenGCTime(long edentime) {
		this.edentime = edentime;
	}
	
	public long getEdenGCTime() { return edentime; }
	
	void setEdenPeak(long edenpeak) {
		this.edenpeak = edenpeak;
	}
	
	public long getEdenPeak() { return edenpeak; }
	
	void setEdenUsed(long edenused) {
		this.edenused = edenused;
	}
	
	public long getEdenUsed() { return edenused; }
	
	void setOldGCCount(long oldcount) {
		this.oldcount = oldcount;
	}
	
	public long getOldGCCount() { return oldcount; }
	
	void setOldGCTime(long oldtime) {
		this.oldtime = oldtime;
	}
	
	public long getOldGCTime() { return oldtime; }
	
	void setOldPeak(long oldpeak) {
		this.oldpeak = oldpeak;
	}
	
	public long getOldPeak() { return oldpeak; }
	
	void setOldUsed(long oldused) {
		this.oldused = oldused;
	}
	
	public long getOldUsed() { return oldused; }
	
	void setSurvPeak(long survpeak) {
		this.survpeak = survpeak;
	}
	
	public long getSurvPeak() { return survpeak; }
	
	void setSurvUsed(long survused) {
		this.survused = survused;
	}
	
	public long getSurvUsed() { return survused; }
}