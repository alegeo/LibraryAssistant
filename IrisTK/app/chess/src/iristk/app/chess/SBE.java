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
 * Class containing Static Board Evaluator for a particular game.
 */
public abstract class SBE {

	/**
	 * Return an integer score indicating which side is winning in the board, b.
	 */
	public abstract int evaluate(Board b);
}