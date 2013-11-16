package util.java;

import java.lang.reflect.Array;

/**
 * AArray
 * An abstraction of arrays which includes a setNext(val) method, which sets
 * the value at nextIndex to val, and then increments nextIndex, wrapping around
 * when nextIndex == arr.length.
 *
 * Used http://stackoverflow.com/questions/529085/how-to-generic-array-creation
 * as a guideline for making a generic array.
 */
public class AArray<E> {
    private E[] arr;
    private int nextIndex;

    public AArray(Class<E> componentType, int length)
            throws NegativeArraySizeException {
	@SuppressWarnings("unchecked")
	final E[] arr = (E[]) Array.newInstance(componentType, length);
	this.arr = arr;
	this.nextIndex = 0;
    }
    public E get(int i) throws ArrayIndexOutOfBoundsException {
	return arr[i];
    }
    public void set(int i, E val) throws ArrayIndexOutOfBoundsException {
	arr[i] = val;
    }
    public void setNext(E val) {
	arr[nextIndex] = val;
	nextIndex = (nextIndex + 1) % arr.length;
}   }
