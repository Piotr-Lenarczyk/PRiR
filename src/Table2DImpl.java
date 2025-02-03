import java.util.Arrays;

public class Table2DImpl implements Table2D {
	private final int[][] array;

	public Table2DImpl(int[][] array) {
		this.array = array;
	}

	@Override
	public int cols() {
		return array[0].length;
	}

	@Override
	public int rows() {
		return array.length;
	}

	@Override
	public int get(Position2D position) {
		return array[position.row()][position.col()];
	}

	@Override
	public void set0(Position2D position) {
		array[position.row()][position.col()] = 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int[] row : array) {
			builder.append(Arrays.toString(row)).append("\n");
		}
		return builder.toString();
	}
}
