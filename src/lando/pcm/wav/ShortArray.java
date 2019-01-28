package lando.pcm.wav;

public final class ShortArray {
	public short[] array = new short[0];
	public int size = 0;
	
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