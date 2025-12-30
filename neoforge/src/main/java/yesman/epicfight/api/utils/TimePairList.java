package yesman.epicfight.api.utils;

public class TimePairList {
	private final TimePair[] timePairs;
	
	private TimePairList(TimePair[] timePairs) {
		this.timePairs = timePairs;
	}
	
	public boolean isTimeInPairs(float time) {
		for (TimePair timePair : this.timePairs) {
			if (timePair.isTimeIn(time)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static class TimePair {
		public final float begin;
		public final float end;
		
		private TimePair(float begin, float end) {
			this.begin = begin;
			this.end = end;
		}
		
		private boolean isTimeIn(float time) {
			return time >= this.begin && time < end;
		}
	}
	
	public static TimePairList create(float... times) {
		if (times.length % 2 != 0) {
			throw new IllegalArgumentException("Time pair exception : number of given times is not an even number");
		}
		
		TimePair[] timePairs = new TimePair[times.length / 2];
		
		for (int i = 0; i < times.length / 2; i++) {
			timePairs[i] = new TimePair(times[i * 2], times[i * 2 + 1]);
		}
		
		return new TimePairList(timePairs);
	}
}
