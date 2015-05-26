package bishop.interpreter;

import java.io.IOException;
import java.io.InputStream;

import utils.IoUtils;

public abstract class BytecodeProcessor {
	
	private InputStream bytecodeStream;

	protected void processBytecode(final Bytecode bytecode) {
		bytecodeStream = bytecode.getStream();
		
		try {
			try {
				processAllOpcodes();
			}
			finally {
				bytecodeStream.close();
				bytecodeStream = null;
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Interpret error", ex);
		}
	}


	private void processAllOpcodes() throws IOException {
		while (true) {
			final int opcode = bytecodeStream.read();
			
			if (opcode < 0)
				break;
			
			processOpcode((byte) opcode);
		}
	}
	
	protected abstract void processOperand(final long value);
	protected abstract void processOperator(final OperatorRecord record);
	protected abstract void processSwitch (final int caseCount);
	
	private void processOpcode (final byte opcode) throws IOException {
		switch (opcode) {
			case Bytecode.OPCODE_BYTE:
				{
					final byte value = IoUtils.readByteBinary(bytecodeStream);
					processOperand(value);
				}
				break;
				
			case Bytecode.OPCODE_LONG:
				{
					final long value = IoUtils.readNumberBinary(bytecodeStream, Long.SIZE);
					processOperand(value);
				}
				break;
				
			case Bytecode.OPCODE_SWITCH:
				{
					final int caseCount = IoUtils.readByteBinary(bytecodeStream) & 0xFF;
					processSwitch(caseCount);
				}
				break;
			
			default:
				final OperatorRecord record = Operators.getRecordForOpcode(opcode);
				
				if (record == null)
					throw new RuntimeException("Unknown opcode: " + opcode);
				
				processOperator(record);
		}
	}

}
