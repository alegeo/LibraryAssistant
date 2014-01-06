package iristk.flow;

public class Timer {

	private long startTime;
	
	public Timer() {
		this.startTime = System.currentTimeMillis();
	}

	public boolean passed(int msec) {
		return (System.currentTimeMillis() - startTime >= msec);
	}

	public void reset() {
		startTime = System.currentTimeMillis();
	}
	
}
