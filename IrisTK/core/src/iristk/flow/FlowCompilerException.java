package iristk.flow;

public class FlowCompilerException extends Exception {

	private final int lineNumber;
	
	public FlowCompilerException(String message) {
		super(message);
		lineNumber = 1;
	}
	
	public FlowCompilerException(String message, Integer lineNumber) {
		super(message);
		if (lineNumber == null || lineNumber < 1)
			this.lineNumber = 1;
		else
			this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	
}
