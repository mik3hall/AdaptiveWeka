package us.hall.gc.benchmark;

public class GCStats {
	long count = 0L, time = 0L;
	
	GCStats(long count, long time) {
		this.count = count;
		this.time = time;
	}
	
	public long getCount() { return count; }
	public long getTime() { return time; }
}