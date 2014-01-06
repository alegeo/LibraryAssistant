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

public class Position {

	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	private int row;
	private int col;

	public void setRow(int row) {
		this.row = row;
	}

	public int getRow() {
		return row;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getCol() {
		return col;
	}

}
