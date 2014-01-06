package iristk.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ParsedInputStream extends FilterInputStream {

	public ParsedInputStream(InputStream stream) {
		super(stream);
	}
	
	public String readTo(int... stopChars) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		READ:
		while (true) {
			b = super.read();
			if (b == -1) break READ;
			for (int c : stopChars) {
				if (b == c) break READ;
			}
			baos.write(b);
		}
		return baos.toString();
	}
	
	public String readLine() throws IOException {
		return readTo(10, 13);
	}

}
