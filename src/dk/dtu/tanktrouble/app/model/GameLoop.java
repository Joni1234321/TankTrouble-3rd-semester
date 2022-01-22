package dk.dtu.tanktrouble.app.model;

public abstract class GameLoop {
	protected static final long TPS = 100;
	protected static final long MSPT = 1000 / TPS;
	protected static final long TICKS_PER_SNAPSHOT = 5;
	protected static final double SPT = 1. / TPS;
	protected static final long TICK_DELAY = 1 * TICKS_PER_SNAPSHOT;

	protected long tickNumber;

	private boolean running = true;

	// Loop
	public void startGameLoop() {
		// Calibrate
		tickNumber = 0;
		long startTime = System.currentTimeMillis();
		startTime -= startTime % MSPT;
		long currTime;

		// Run game
		try {
			while (running && doTick(System.nanoTime(), tickNumber % TICKS_PER_SNAPSHOT == 0)) {
				startTime += MSPT;
				tickNumber += 1;
				currTime = System.currentTimeMillis();
				if (startTime > currTime) {
					//noinspection BusyWait
					Thread.sleep(startTime - currTime);
				}
			}
		} catch (InterruptedException ignored) {
		}
	}

	public void stopGameLoop() {
		running = false;
	}


	protected abstract boolean doTick(long now, boolean sendSnapshot);

}
