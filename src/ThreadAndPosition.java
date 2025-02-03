/**
 * Thread and its starting position pair
 *
 * @param thread   Thread with read from table permission
 * @param position First position to read from table for Thread
 */
public record ThreadAndPosition(Thread thread, Position2D position) {

}