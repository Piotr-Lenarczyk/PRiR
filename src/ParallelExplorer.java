import java.util.*;
import java.util.concurrent.*;

public class ParallelExplorer implements Explorer {
	private final Set<Pair> result;
	private final BlockingQueue<Pair> writeQueue;
	private final List<ThreadAndPosition> threadsAndPositions;
	private ThreadsFactory threadsFactory;
	private Table2D table;
	private volatile boolean[] globalVisited;
	private CountDownLatch latch;
	private boolean resultsReady;
	private int rows;
	private int cols;

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

		this.globalVisited = new boolean[rows * cols];
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
				while (latch.getCount() != 0 || !writeQueue.isEmpty()) {
					Pair pair = writeQueue.poll(10, TimeUnit.MILLISECONDS);
					if (pair != null && table.get(pair.first()) + table.get(pair.second()) == sum) {
						result.add(pair);
						table.set0(pair.first());
						table.set0(pair.second());
						markVisited(pair.first());
						markVisited(pair.second());
					}
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
			if (isVisited(current)) {
				continue;
			}

			int currentValue = table.get(current);
			boolean localPairFound = false;

			for (Position2D neighbor : getNeighbors(current)) {
				if (isVisited(neighbor)) {
					continue;
				}
				int neighborValue = table.get(neighbor);

				if (currentValue + neighborValue == sum && !localPairFound) {
					Pair pair = new Pair(current, neighbor);
					writeQueue.add(pair);
					localPairFound = true;
				}

				toExplore.add(neighbor);
			}
			markVisited(current);
		}
		latch.countDown();
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
		return globalVisited[index];
	}

	private synchronized void markVisited(Position2D position) {
		int index = position.row() * cols + position.col();
		globalVisited[index] = true;
	}
}
