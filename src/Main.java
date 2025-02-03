import java.util.Random;
import java.util.Set;

public class Main {
	public static void main(String[] args) {
		// Create the 2D array
		//int size = 5000;
		//int sumTarget = 10;
		//long seed = 42; // Fixed seed for deterministic generation

		int[][] array = {
				{1, 1, 3, 3, 2, 7, 11, 5},
				{0, 0, 1, 5, 1, 3, 2, 3},
				{0, 3, 1, 1, 11, 41, 0, 8},
				{9, 0, 5, 5, 0, 1, 12, 5},
				{1, 3, 4, 11, 11, 1, 3, 3}
		};

		//int[][] array = generateDeterministicTable(size, sumTarget, seed);

		// Use Table2DImpl to implement Table2D
		Table2D table = new Table2DImpl(array);

		// Use ThreadFactoryImpl to implement ThreadsFactory
		ThreadsFactory factory = new ThreadsFactoryImpl(5, table.rows());

		// Initialize the ParallelExplorer
		ParallelExplorer explorer = new ParallelExplorer();
		explorer.setThreadsFactory(factory);
		explorer.setTable(table);

		// Start the search for pairs summing to 10
		int targetSum = 10;
		long startTime = System.currentTimeMillis();
		explorer.start(targetSum);
		long endTime = System.currentTimeMillis();
		System.out.println("Start method execution time: " + (endTime - startTime) + "ms");

		// Wait for the result and print it
		Set<Pair> results;
		while ((results = explorer.result()).isEmpty()) {
			try {
				//System.out.println("Checking for results");
				//checkThreadState();
				Thread.sleep(10); // Check periodically for results
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		endTime = System.currentTimeMillis();


		// Print the results
		System.out.println("Pairs found:");
		for (Pair pair : results) {
			System.out.println(pair);
		}
		System.out.println("Execution time: " + (endTime - startTime) + "ms");

	}

	public static void checkThreadState() {
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		for (Thread thread : threads) {
			if (thread.getName().startsWith("Thread")) {
				System.out.println(thread.getName() + " state: " + thread.getState());
			}
		}
	}

	public static int[][] createTable(int rows, int cols, int max) {
		Random rand = new Random();
		int[][] table = new int[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				table[i][j] = rand.nextInt(max + 1);  // Random integer between 0 and max (inclusive)
			}
		}

		return table;
	}

	private static int[][] generateDeterministicTable(int size, int targetSum, long seed) {
		Random random = new Random(seed);
		int[][] table = new int[size][size];

		// Fill table with random values (1-50)
		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				table[row][col] = random.nextInt(50) + 1;
			}
		}

		// Ensure some valid pairs exist
		for (int row = 0; row < size - 1; row++) {
			for (int col = 0; col < size - 1; col++) {
				if (random.nextDouble() < 0.05) { // 5% chance to set a valid pair
					int value = random.nextInt(targetSum - 1) + 1;
					table[row][col] = value;
					table[row][col + 1] = targetSum - value;
				}
			}
		}

		return table;
	}
}
