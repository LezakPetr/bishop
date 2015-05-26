package bishop.tablebaseGenerator;

import bishop.tablebase.ITable;

public class BytecodeSettings {

	private ITable table;
	private BytecodeGenerator generator;

	public ITable getTable() {
		return table;
	}

	public void setTable(final ITable table) {
		this.table = table;
	}

	public BytecodeGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(final BytecodeGenerator generator) {
		this.generator = generator;
	}
}
