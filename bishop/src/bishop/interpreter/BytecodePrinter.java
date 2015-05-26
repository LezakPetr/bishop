package bishop.interpreter;

import java.io.PrintStream;

public class BytecodePrinter extends BytecodeProcessor {

	private PrintStream outputStream;
	private boolean isFirstToken;
	
	public void printBytecode (final Bytecode bytecode, final PrintStream stream) {
		this.outputStream = stream;
		this.isFirstToken = true;
		
		try {
			processBytecode(bytecode);
		}
		finally {
			this.outputStream = null;
		}
	}
	
	private void addTokenDelimiter() {
		if (isFirstToken)
			isFirstToken = false;
		else
			outputStream.print(' ');
	}

	@Override
	protected void processOperand(final long value) {
		addTokenDelimiter();
		outputStream.print(value);
	}

	@Override
	protected void processOperator(final OperatorRecord record) {
		addTokenDelimiter();
		outputStream.print(record.getToken());
	}

	@Override
	protected void processSwitch(final int caseCount) {
		addTokenDelimiter();
		outputStream.print(Bytecode.TOKEN_SWITCH);
		outputStream.print(caseCount);
	}
}
