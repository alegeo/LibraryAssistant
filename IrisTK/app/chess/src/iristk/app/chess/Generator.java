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
 * Abstract class containing the a move generator for a particular game.
 */
public abstract class Generator {
	/**
	 * Generate a list of legal moves from the given board configuration.
	 */
	public abstract Move generateMoves(Board b, int side);
}