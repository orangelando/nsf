package lando.wav;

public final class ShortArray {
	private short[] array = new short[0];
	private int size = 0;
	
	public short[] getArray() {
	    return array;
	}
	
	public int getSize() {
	    return size;
	}
	
	public void append(short s) {
		if( size >= array.length ) {
			int newLen = (array.length + 1)*3/2;
			short[] newArray = new short[newLen];
			
			System.arraycopy(array, 0, newArray, 0, size);
			
			array = newArray;
		}
		
		array[size++] = s;
	}
}