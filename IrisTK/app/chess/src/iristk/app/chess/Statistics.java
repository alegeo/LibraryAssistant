/*
 * Copyright 2009-2010 Gabriel Skantze.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package iristk.app.chess;

/**
 * Class containing Statistics for a particular search.
 */
public class Statistics {

	public long start_time;
	public int node_count, failed_high, failed_high_first, failed_low,
			null_moves, hash_cutoff;

	public void reset() {
		node_count = 0;
		failed_high = 0;
		failed_high_first = 0;
		failed_low = 0;
		null_moves = 0;
		hash_cutoff = 0;
		start_time = 0;
	}

	public String string() {
		return " n=" + node_count + " nps=" + node_count * 1000
				/ (System.currentTimeMillis() - start_time) + " fh="
				+ failed_high + " ff=" + failed_high_first + " "
				+ (100 * failed_high_first / (failed_high + 1)) + "%" + " fl="
				+ failed_low + " nm=" + null_moves + " hc=" + hash_cutoff;
	}

}