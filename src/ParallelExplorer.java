import java.util.*;
import java.util.concurrent.*;

public class ParallelExplorer implements Explorer {
	private final Set<Pair> result;
	private final BlockingQueue<Pair> writeQueue;
	private final List<ThreadAndPosition> threadsAndPositions;
	private ThreadsFactory threadsFactory;
	private Table2D table;
	private volatile boolean[] visited;
	private volatile boolean[] explored;
	private volatile int[] values;
	private CountDownLatch latch;
	private boolean resultsReady;
	private int rows;
	private int cols;
	private static final Pair POISON_PILL = new Pair(new Position2D(0, 0), new Position2D(0, 0));

	public ParallelExplorer() {
		this.result = ConcurrentHashMap.newKeySet();
		this.writeQueue = new LinkedBlockingQueue<>();
		this.threadsAndPositions = new CopyOnWriteArrayList<>();
		this.resultsReady = false;
	}

	@Override
	public void setThreadsFactory(ThreadsFactory factory) {
		this.threadsFactory = factory;
	}

	@Override
	public void setTable(Table2D table) {
		this.table = table;
		this.rows = table.rows();
		this.cols = table.cols();

		this.visited = new boolean[rows * cols];
		this.explored = new boolean[rows * cols];
		this.values = new int[rows * cols];
	}

	@Override
	public void start(int sum) {
		this.latch = new CountDownLatch(threadsFactory.readersThreads());

		for (int i = 0; i < threadsFactory.readersThreads(); i++) {
			ThreadAndPosition tp = threadsFactory.readerThread(() -> exploreFromPosition(sum));
			threadsAndPositions.add(tp);

			Thread reader = tp.thread();
			reader.start();
		}

		Thread writerThread = threadsFactory.writterThread(() -> {
			try {
				while (true) {
					Pair pair = writeQueue.take();
					if (pair.equals(POISON_PILL)) {
						break;
					}
					result.add(pair);
					table.set0(pair.first());
					table.set0(pair.second());
					markVisited(pair.first());
					markVisited(pair.second());
				}
				resultsReady = true;
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		});
		writerThread.start();
	}

	@Override
	public Set<Pair> result() {
		if (resultsReady) {
			return result;
		}
		return Collections.emptySet();
	}

	private void exploreFromPosition(int sum) {
		ThreadAndPosition threadAndPosition = threadsAndPositions.stream()
				.filter(tp -> tp.thread().equals(Thread.currentThread()))
				.findFirst()
				.orElse(null);

		if (threadAndPosition == null) {
			return;
		}

		Position2D startingPosition = threadAndPosition.position();
		Queue<Position2D> toExplore = new ArrayDeque<>();
		toExplore.add(startingPosition);

		while (!toExplore.isEmpty()) {
			Position2D current = toExplore.poll();
			if (isExplored(current)) {
				continue;
			}
			int currentValue;
			if (isVisited(current)) {
				currentValue = getValue(current);
			} else {
				currentValue = table.get(current);
				setValue(current, currentValue);
				markVisited(current);
			}
			markExplored(current);

			boolean localPairFound = false;

			for (Position2D neighbor : getNeighbors(current)) {
				if (isExplored(neighbor)) {
					continue;
				}
				int neighborValue;
				if (isVisited(neighbor)) {
					neighborValue = getValue(neighbor);
				} else {
					neighborValue = table.get(neighbor);
					setValue(neighbor, neighborValue);
					markVisited(neighbor);
				}

				if (currentValue + neighborValue == sum && !localPairFound) {
					Pair pair = new Pair(current, neighbor);
					writeQueue.add(pair);
					localPairFound = true;
				}

				toExplore.add(neighbor);
			}
		}
		latch.countDown();
		if (latch.getCount() == 0) {
			writeQueue.add(POISON_PILL);
		}
	}

	private List<Position2D> getNeighbors(Position2D position) {
		List<Position2D> neighbors = new ArrayList<>();
		int row = position.row();
		int col = position.col();

		for (int diffRow = -1; diffRow <= 1; diffRow++) {
			for (int diffCol = -1; diffCol <= 1; diffCol++) {
				if (diffRow == 0 && diffCol == 0) {
					continue;
				}

				int newRow = row + diffRow;
				int newCol = col + diffCol;

				if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
					neighbors.add(new Position2D(newCol, newRow));
				}
			}
		}

		return neighbors;
	}

	private boolean isVisited(Position2D position) {
		int index = position.row() * cols + position.col();
		return visited[index];
	}

	private synchronized void markVisited(Position2D position) {
		int index = position.row() * cols + position.col();
		visited[index] = true;
	}

	private boolean isExplored(Position2D position) {
		int index = position.row() * cols + position.col();
		return explored[index];
	}

	private synchronized void markExplored(Position2D position) {
		int index = position.row() * cols + position.col();
		explored[index] = true;
	}

	private int getValue(Position2D position) {
		int index = position.row() * cols + position.col();
		return values[index];
	}

	private synchronized void setValue(Position2D position, int value) {
		int index = position.row() * cols + position.col();
		values[index] = value;
	}
}