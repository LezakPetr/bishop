package bishop.tablebaseGenerator;

import java.util.List;
import java.util.Random;

import optimization.IState;
import bishop.interpreter.Bytecode;

public class BytecodeState implements IState<BytecodeState, BytecodeSettings> {
	
	private Bytecode bytecode;
	private BytecodeGenerator generator;
	private int[] items;
	
		
	@Override
	public void randomInitialize(final Random random, final BytecodeSettings settings) {
		this.generator = settings.getGenerator();
		
		final int itemCount = generator.getItemCount();
		this.items = new int[itemCount];
		
		updateBytecode();
	}

	@Override
	public void randomChange(final Random random, final BytecodeSettings settings) {
		for (int i = 0; i < 10000; i++) {
			final int index = random.nextInt(items.length);
			final int oldItem = items[index];
			final int newItem = random.nextInt(generator.getItemVariants(index));
			
			if (newItem != oldItem) {
				items[index] = newItem;
				
				if (generator.isCorrectCombination(items)) {
					updateBytecode();
					return;
				}
				
				items[index] = oldItem;
			}
		}
		
		throw new RuntimeException("Cannot generate next state");
	}

	@Override
	public BytecodeState copy() {
		final BytecodeState state = new BytecodeState();
		final int itemCount = items.length;
		
		state.items = new int[itemCount];
		System.arraycopy(this.items, 0, state.items, 0, itemCount);
		
		state.generator = this.generator;
		state.bytecode = this.bytecode;
		
		return state;
	}
	
	private void updateBytecode() {
		bytecode = generator.getBytecode(items);
	}

	public Bytecode getBytecode() {
		return bytecode;
	}

	/**
	 * Returns item with given index.
	 * @param itemIndex item index
	 * @return item
	 */
	public int getItem(final int itemIndex) {
		return items[itemIndex];
	}

	/**
	 * Sets item with given index.
	 * @param itemIndex item index
	 * @param item item
	 */
	public void setItem(final int itemIndex, final int item) {
		items[itemIndex] = item;
		updateBytecode();
	}
	
	/**
	 * Returns array of items that can be reached from current item by incremental update.
	 * @param itemIndex item index
	 * @return list of successors
	 */
	public List<Integer> getSuccessorsItems (final int itemIndex) {
		return generator.getSuccessorsItems (itemIndex, items[itemIndex]);
	}
	
	public boolean isCorrectCombination() {
		return generator.isCorrectCombination(items);
	}

}
