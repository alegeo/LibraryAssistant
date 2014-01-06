package iristk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Works like an ArrayBlockingQueue but with bytes. This means that one thread may write bytes to the queue while another thread reads them, blocking if not enough bytes have been written. The capacity is dynamically adjusted.
 */
public class BlockingByteQueue {

	private byte[] buffer;
	
	private int bufWritePos = 0;
	private int bufReadPos = 0;
	private long writePos = 0;
	private long readPos = 0;

	private boolean writing = true;
		
	public BlockingByteQueue() {
		buffer = new byte[100];
	}
	
	public int capacity() {
		return buffer.length;
	}
	
	//public String name = null;
	
	/**
	 * Writes {@code len} bytes from {@code bytes}, starting at position {@code pos} to the queue
	 */
	public void write(byte[] bytes, int pos, int len) {
		synchronized (this) {
			//if  (name != null)
			//	System.out.println(this.name + " WRITING: writePos:" + writePos + " bufWritePos:" + bufWritePos + " readPos:" + readPos + " bufReadPos: " + bufReadPos);
			writing = true;
			if (writePos + len > readPos + capacity()) {
				//if  (name != null)
				//	System.out.println(this.name + " EXPANDING: writePos:" + writePos + " readPos:" + readPos);
				// Buffer too small, expand
				int newSize = capacity();
				while (writePos + len > readPos + newSize) {
					newSize = newSize * 2;
				}
				byte[] oldBuffer = buffer;
				buffer = new byte[newSize];
				System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
			}
			if (bufWritePos + len > capacity()) {
				//if  (name != null)
				//	System.out.println(this.name + " RECYCLING");
				// Reached the end of the buffer, restart at the beginning
				int split = capacity() - bufWritePos;
				System.arraycopy(bytes, pos, buffer, bufWritePos, split);
				System.arraycopy(bytes, pos + split, buffer, 0, len - split);
				bufWritePos = len - split;
			} else {
				System.arraycopy(bytes, pos, buffer, bufWritePos, len);
				bufWritePos += len;
			}
			writePos += len;
			this.notify();
		}
	}

	/**
	 * Writes {@code bytes} to the queue
	 */
	public void write(byte[] bytes) {
		this.write(bytes, 0, bytes.length);
	}
	
	/**
	 * Ends the writing, which means that reading from the queue will no longer block. To restore the blocking, {@code reset()} muse be called.
	 */
	public void endWrite() {
		synchronized (this) {
			writing  = false;
			this.notify();
		}
	}
	
	/**
	 * Reads {@code len} bytes from the queue into {@code bytes} starting at position {@code pos}. Blocks if there are not enough bytes to read.  
	 * @return the number of bytes actually read (which will always be the same as {@code len} unless the the {@code endWrite()} method has been called).
	 */
	public int read(byte[] bytes, int pos, int len) throws InterruptedException {
		//if  (name != null)
		//	System.out.println(this.name + " R0EADING: writePos:" + writePos + " bufWritePos:" + bufWritePos + " readPos:" + readPos + " bufReadPos: " + bufReadPos);
		synchronized (this) {
			while (readPos + len > writePos && writing) {
				// Reading faster than writing, block
				this.wait();
			}
			if (readPos + len > writePos) {
				len = (int) (writePos - readPos);
			}
			if (len > 0) {
				if (bufReadPos + len > capacity()) {
					// Reached the end of the buffer, restart at the beginning
					//if  (name != null)
					//	System.out.println(this.name + " RESTART: writePos:" + writePos + " readPos:" + readPos);
					int split = capacity() - bufReadPos;
					System.arraycopy(buffer, bufReadPos, bytes, pos, split);
					System.arraycopy(buffer, 0, bytes, pos + split, len - split);
					bufReadPos = len - split;
				} else {
					System.arraycopy(buffer, bufReadPos, bytes, pos, len);
					bufReadPos += len;
				}
				readPos += len;
			} else if (!writing) {
				len = -1;
			} else {
				len = 0;
			}
			return len;
		}
	}
	
	/**
	 * Reads {@code len} bytes from the queue and writes them to {@code stream}. Blocks if there are not enough bytes to read.  
	 * @return the number of bytes actually read (which will always be the same as {@code len} unless the the {@code endWrite()} method has been called).
	 */
	public int read(OutputStream stream, int len) throws InterruptedException, IOException {
		synchronized (this) {
			while (readPos + len > writePos && writing) {
				// Reading faster than writing, block
				this.wait();
			}
			if (readPos + len > writePos) {
				len = (int) (writePos - readPos);
			}
			if (len > 0) {
				if (bufReadPos + len > capacity()) {
					// Reached the end of the buffer, restart at the beginning
					int split = capacity() - bufReadPos;
					stream.write(buffer, bufReadPos, split);
					stream.write(buffer, 0, len - split);
					bufReadPos = len - split;
				} else {
					stream.write(buffer, bufReadPos, len);
					bufReadPos += len;
				}
				readPos += len;
			} else if (!writing) {
				len = -1;
			} else {
				len = 0;
			}
			return len;
		}
	}

	/**
	 * Skips {@code len} bytes for reading. Blocks if there are not enough bytes to skip. 
	 */
	public int skip(int len) throws InterruptedException {
		//if  (name != null)
		//	System.out.println(this.name + " SKIPPING: writePos:" + writePos + " readPos:" + readPos);
		synchronized (this) {
			while (readPos + len > writePos && writing) {
				// Reading faster than writing, block
				this.wait();
			}
			if (readPos + len > writePos) {
				len = (int) (writePos - readPos);
			}
			if (len > 0) {
				if (bufReadPos + len > capacity()) {
					// Reached the end of the buffer, restart at the beginning
					int split = capacity() - bufReadPos;
					bufReadPos = len - split;
				} else {
					bufReadPos += len;
				}
				readPos += len;
			} else if (!writing) {
				len = -1;
			} else {
				len = 0;
			}
			return len;
		}
	}
	
	/**
	 * @return the write position since start or reset 
	 */
	public long getWritePos() {
		return writePos;
	}
	
	public int read(byte[] bytes) throws InterruptedException {
		return this.read(bytes, 0, bytes.length);
	}
	
	/**
	 * Resets and clears the queue
	 */
	public void reset() {
		bufWritePos = 0;
		bufReadPos = 0;
		writePos = 0;
		readPos = 0;
		writing = true;
	}
	
	/**
	 * 
	 * @return An InputStream that reads from the queue
	 */
	public InputStream getInputStream() {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				byte[] bb = new byte[1];
				try {
					int len = BlockingByteQueue.this.read(bb);
					if (len == -1)
						return -1;
					else
						return bb[0] & 0xFF;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return -1;
				}			
			}
			
			@Override
			public int read(byte[] bytes, int off, int len) throws IOException {
				try {
					int read = BlockingByteQueue.this.read(bytes, off, len);
					return read;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return -1;
				}
			}
			
		};
	}

	/**
	 * 
	 * @return the number of bytes that are available if the reading should not block
	 */
	public int available() {
		return (int) (writePos - readPos);
	}
	
	/**
	 * @return the read position since start or reset 
	 */
	public int getReadPos() {
		return (int) readPos;
	}

}
