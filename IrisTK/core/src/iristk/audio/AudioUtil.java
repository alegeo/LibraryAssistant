package iristk.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

public class AudioUtil {

	/**
	 * Converts doubles (from -1.0 to 1.0) to bytes according to format
	 */
	public static void doublesToBytes(AudioFormat format, double[] source, int sourcePos, int sourceLen, byte[] target, int targetPos) {
		if (format.getSampleSizeInBits() == 16) {
			ByteBuffer bb = ByteBuffer.wrap(target);
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < sourceLen; i++) {
				double val = source[i + sourcePos];
				short sval = (short) (val * Short.MAX_VALUE);
				bb.putShort(targetPos + i * 2, sval);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < sourceLen; i++) {
				double val = source[i + sourcePos];
				byte bval = (byte) (val * Byte.MAX_VALUE);
				target[targetPos + i] = bval;
			}
		}
	}

	/**
	 * Converts floats (from -1.0 to 1.0) to bytes according to format
	 */
	public static void floatsToBytes(AudioFormat format, float[] source, int sourcePos, int sourceLen, byte[] target, int targetPos) {
		if (format.getSampleSizeInBits() == 16) {
			ByteBuffer bb = ByteBuffer.wrap(target);
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < sourceLen; i++) {
				double val = source[i + sourcePos];
				short sval = (short) (val * Short.MAX_VALUE);
				bb.putShort(targetPos + i * 2, sval);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < sourceLen; i++) {
				double val = source[i + sourcePos];
				byte bval = (byte) (val * Byte.MAX_VALUE);
				target[targetPos + i] = bval;
			}
		}
	}

	/**
	 * Converts bytes to doubles (from -1.0 to 1.0) according to format
	 */
	public static void bytesToDoubles(AudioFormat format, byte[] source, int sourcePos, int sourceLen, double[] target, int targetPos) {
		if (format.getSampleSizeInBits() == 16) {
			ByteBuffer bb = ByteBuffer.wrap(source);
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < sourceLen / 2; i++) {
				short val = bb.getShort(i * 2 + sourcePos);
				double dval = ((double)val) / (double)Short.MAX_VALUE;
				target[i + targetPos] = dval;
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < sourceLen; i++) {
				double dval = ((double)source[i + sourcePos]) / (double)Byte.MAX_VALUE;
				target[i + targetPos] = dval;
			}
		}
	}

	public static void bytesToShorts(AudioFormat format, byte[] source, int sourcePos, int sourceLen, short[] target, int targetPos) {
		if (format.getSampleSizeInBits() == 16) {
			ByteBuffer bb = ByteBuffer.wrap(source);
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < sourceLen / 2; i++) {
				target[i + targetPos] = bb.getShort(i * 2 + sourcePos);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < sourceLen; i++) {
				target[i + targetPos] = (short) (source[i + sourcePos] * 256);
			}
		}
	}

	public static void bytesToIntegers(AudioFormat format, byte[] source, int sourcePos, int sourceLen, int[] target, int targetPos) {
		if (format.getSampleSizeInBits() == 16) {
			ByteBuffer bb = ByteBuffer.wrap(source);
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < sourceLen / 2; i++) {
				target[i + targetPos] = bb.getShort(i * 2 + sourcePos);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < sourceLen; i++) {
				target[i + targetPos] = source[i + sourcePos] * 256;
			}
		}
	}

	public static short bytesToShort(AudioFormat format, byte b1, byte b2) {
		byte big;
		byte little;
		if (!format.isBigEndian()) {
			little = b1;
			big = b2;
		} else {
			little = b2;
			big = b1;
		}
		int val = big;
		if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
			val &= 0xff; 
		}
		return (short) ((val << 8) + (little & 0xff));
	}

	public static void scaleDoubles(double[] doubles, double scaleFactor) {
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = doubles[i] * scaleFactor;
		}
	}

	public static void scaleDoubles(double[] doubles, int pos, int len, double scaleFactor) {
		for (int i = 0; i < len; i++) {
			doubles[i + pos] = doubles[i + pos] * scaleFactor;
		}
	}

	public static void resample(byte[] fromBuffer, byte[] toBuffer, int frameSize, int fromSampleRate, int toSampleRate) {
		resample(fromBuffer, 0, fromBuffer.length, toBuffer, 0, frameSize, fromSampleRate, toSampleRate);		
	}

	public static void resample(byte[] fromBuffer, int fromPos, int fromLen, byte[] toBuffer, int toPos, int frameSize, int fromSampleRate, int toSampleRate) {
		float resampleRatio = fromSampleRate / toSampleRate;
		int toLen = (int) (fromLen / resampleRatio);
		int resamplePos;
		for (int i = 0; i < toLen; i += frameSize) {
			resamplePos = (int) (i * resampleRatio);
			resamplePos = resamplePos - (resamplePos % frameSize); 
			for (int j = 0; j < frameSize; j++) {
				//TODO: Should interpolate when upsampling!
				toBuffer[toPos + i + j] = fromBuffer[fromPos + resamplePos + j];
			}
		}
	}

	public static int power(double[] samples, int pos, int length) {
		double sumOfSquares = 0.0f;
		Double prev = null;
		for (int i = 0; i < length; i++) {
			if (prev != null) {
				double sample = (samples[i + pos] - prev) * Short.MAX_VALUE;
				sumOfSquares += (sample * sample);
			}
			prev = samples[i + pos];
		}
		double power = (10.0 * (Math.log10(sumOfSquares) - Math.log10(length))) + 0.5;
		if (power < 0) power = 1.0;
		return (int) power;
	}

}
