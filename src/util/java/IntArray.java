package util.java;

/**
 * IntArray
 * A class which makes a simple array of ints, and includes a nextIndex value,
 * which is the index that values are stored at when the append() method is
 * called, and is incremented (modulo length) after the value has been
 * appended. This is not thread-safe, and so if multiple threads call append()
 * simultaneously, you will lose some counts, so you need to use some sort of
 * synchronization when using this concurrently.
 */
public class IntArray {
    private int[] arr;
    private int nextIndex;
    public IntArray(int length) {
	this.arr = new int[length];
	this.nextIndex = 0;
    }
    public int get(int i) throws ArrayIndexOutOfBoundsException {
	return arr[i];
    }
    public void set(int i, int val) throws ArrayIndexOutOfBoundsException {
	arr[i] = val;
    }
    public void append(int val) {
	arr[nextIndex] = val;
	nextIndex = (nextIndex + 1) % arr.length;
}   }
