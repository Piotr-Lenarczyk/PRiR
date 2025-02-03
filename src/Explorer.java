import java.util.Set;

/**
 * The main interface of a program looking for totals in an array.
 */
public interface Explorer {
	/**
	 * Setting up a thread factory.
	 *
	 * @param factory Thread factory
	 */
	void setThreadsFactory(ThreadsFactory factory);

	/**
	 * Set access to the data array.
	 *
	 * @param table Table with data
	 */
	void setTable(Table2D table);

	/**
	 * Start searching for adjacent items containing combining sum values.
	 *
	 * @param sum Searched for sum
	 */
	void start(int sum);

	/**
	 * The result of the program operation. When the program runs, the method returns an empty set.
	 *
	 * @return A set with pairs of adjacent positions, the sum of which gave the value of sum.
	 */
	Set<Pair> result();
}
