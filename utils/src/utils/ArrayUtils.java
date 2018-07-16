package utils;


public class ArrayUtils {
	
	public static int[] copyItems (final int... items) {
		int count = 0;
		
		for (int i = 1; i < items.length; i += 2)
			count += items[i];
		
		final int[] result = new int[count];
		int index = 0;
		
		for (int i = 0; i < items.length; i += 2) {
			final int actualItem = items[i];
			final int actualCount = items[i+1];
			
			for (int j = 0; j < actualCount; j++) {
				result[index] = actualItem;
				index++;
			}
		}
		
		return result;
	}

	public static int compare (final int[] a, final int[] b) {
		if (a.length != b.length)
			return Integer.compare(a.length, b.length);

		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return Integer.compare(a[i], b[i]);
		}

		return 0;
	}

	public static <T> int findSameObject(final T[] array, final T item) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == item)
				return i;
		}
		
		return -1;
	}
}
