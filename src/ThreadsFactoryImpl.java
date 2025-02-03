public class ThreadsFactoryImpl implements ThreadsFactory {
	private final int maxReaders;
	private int readersCreated = 0;

	public ThreadsFactoryImpl(int maxReaders, int rows) {
		this.maxReaders = maxReaders;
	}

	@Override
	public int readersThreads() {
		return maxReaders;
	}

	@Override
	public ThreadAndPosition readerThread(Runnable run) {
		if (readersCreated >= maxReaders) return null;
		Position2D startPos = new Position2D(0, readersCreated); // Assign start row for each thread
		Thread thread = new Thread(run);
		readersCreated++;
		return new ThreadAndPosition(thread, startPos);
	}

	@Override
	public Thread writterThread(Runnable run) {
		return new Thread(run);
	}
}
