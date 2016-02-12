package bishop.tablebase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.IoUtils;
import utils.ShortRingBuffer;

public enum FileTableIteratorMode {
	FULL (0, 64) {
		@Override
		public short read (final InputStream stream) throws IOException {
			short result = (short) IoUtils.readUnsignedNumberBinary(stream, 2);
			
			if ((result & 0x2000) != 0)
				result |= 0xC000;
			
			return result;
		}
		
		@Override
		public void write (final ShortRingBuffer buffer, final OutputStream stream) throws IOException {
			writeFullSequence(buffer, stream, 1);
		}
	},
	COMPRESSED (64, 128) {
		@Override
		public short read (final InputStream stream) throws IOException {
			return (short) TableResult.decompress(IoUtils.readByteBinary(stream));
		}

		@Override
		public void write (final ShortRingBuffer buffer, final OutputStream stream) throws IOException {
			final int size = buffer.getSize();
			final int maxCount = Math.min(size, getMaxCount());
			int length = 0;
			
			while (length < maxCount) {
				final int result = buffer.getAt(length);
				
				if (!TableResult.canBeCompressed(result))
					break;
				
				final int nextIndex = length + 1;
				
				if (nextIndex < size && (result == TableResult.ILLEGAL || result == TableResult.DRAW) && buffer.getAt(nextIndex) == result)
					break;
				
				length = nextIndex;
			}
			
			writeCompressedSequence(buffer, stream, length);
		}		
	},
	DRAW (128, 192) {
		@Override
		public short read (final InputStream stream) throws IOException {
			return TableResult.DRAW;
		}

		@Override
		public void write (final ShortRingBuffer buffer, final OutputStream stream) throws IOException {
			final int length = getSequenceLength (buffer, TableResult.DRAW, getMaxCount());
			stream.write(getDescriptor(length));
			buffer.pop(length);
		}
	},
	ILLEGAL (192, 256) {
		@Override
		public short read (final InputStream stream) throws IOException {
			return TableResult.ILLEGAL;
		}

		@Override
		public void write (final ShortRingBuffer buffer, final OutputStream stream) throws IOException {
			final int length = getSequenceLength (buffer, TableResult.ILLEGAL, getMaxCount());
			stream.write(getDescriptor(length));
			buffer.pop(length);
		}
	};
	
	private final int minDescriptor;
	private final int maxDescriptor;
	private final int maxCount;
	
	private FileTableIteratorMode (final int minDescriptor, final int maxDescriptor) {
		this.minDescriptor = minDescriptor;
		this.maxDescriptor = maxDescriptor;
		this.maxCount = maxDescriptor - minDescriptor;
	}

	protected int getSequenceLength(final ShortRingBuffer buffer, final int result, final int modeMaxCount) {
		final int maxCount = Math.min(buffer.getSize(), modeMaxCount);
		int length = 0;
		
		while (length < maxCount && buffer.getAt(length) == result)
			length++;
		
		return length;
	}

	public int getMinDescriptor() {
		return minDescriptor;
	}

	public int getMaxDescriptor() {
		return maxDescriptor;
	}

	public static FileTableIteratorMode forDescriptor(final int descriptor) {
		for (FileTableIteratorMode mode: values()) {
			if (descriptor >= mode.minDescriptor && descriptor < mode.maxDescriptor)
				return mode;
		}
		
		throw new RuntimeException("Unknown mode for descriptor " + descriptor);
	}

	public int getCount(final int descriptor) {
		return descriptor - minDescriptor + 1;
	}
	
	/**
	 * Returns maximal count (inclusive).
	 * @return maximal count (inclusive)
	 */
	public int getMaxCount() {
		return maxCount;
	}
	
	public int getDescriptor (final int count) {
		return count + minDescriptor - 1;
	}
	
	abstract public short read (final InputStream stream) throws IOException;
	abstract public void write (final ShortRingBuffer buffer, final OutputStream stream) throws IOException;
	
	public static void writeCompressedSequence(final ShortRingBuffer buffer, final OutputStream stream, final int size) throws IOException {
		stream.write(FileTableIteratorMode.COMPRESSED.getDescriptor(size));
		
		for (int i = 0; i < size; i++)
			stream.write(TableResult.compress(buffer.getAt(i)));
		
		buffer.pop(size);
	}

	public static void writeFullSequence(final ShortRingBuffer buffer, final OutputStream stream, final int size) throws IOException {
		for (int i = 0; i < size; i++)
			IoUtils.writeNumberBinary(stream, buffer.getAt(i) & 0x3FFF, 2);
		
		buffer.pop(size);
	}

}
