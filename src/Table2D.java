/**
 * Interface of a 2D array of integers. Correct positions
 * are: <br>
 * <ul>
 * <li>For row from 0 to rows() - 1
 * <li>For columns from 0 to cols() - 1
 * </ul>
 */
public interface Table2D {
	/**
	 * Method returns the amount of columns in the table
	 *
	 * @return Amount of columns
	 */
	int cols();

	/**
	 * Method returns the amount of rows in the table
	 *
	 * @return liczba wierszy
	 */
	int rows();

	/**
	 * Value saved in the table on a given position
	 *
	 * @param position Position to read
	 * @return Value saved in the table
	 */
	int get(Position2D position);

	/**
	 * Writes 0 do the table on a given position
	 *
	 * @param position Position to write 0 to
	 */
	void set0(Position2D position);
}
