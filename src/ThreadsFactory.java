/**
 * Thread factory interface
 */
public interface ThreadsFactory {
	/**
	 * Amount of threads with read data permission
	 *
	 * @return Amount of threads
	 */
	int readersThreads();

	/**
	 * Method returns Thread object in NEW state (not started yet). Thread are returned in a
	 * pair with the starting table position from which they should start reading
	 * data. After reading readersThreads threads, next call will return NULL
	 *
	 * @param run Code do run in a thread
	 * @return Thread with read permissions and his starting position
	 */
	ThreadAndPosition readerThread(Runnable run);

	/**
	 * Method returns Thread object in NEW state. Thread returned from this method
	 * is the only one allowed to modify state of data table. All calls to this methods
	 * will return one and the same thread
	 *
	 * @param run Code to run in a thread
	 * @return Thread with write permissions
	 */
	Thread writterThread(Runnable run);
}
