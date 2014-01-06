package iristk.app.chess;

import iristk.util.Record;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.WindowConstants;

public class ChessGame {

	public static final HashMap<Integer, String> PIECE_NAME = new HashMap<Integer, String>();

	static {
		PIECE_NAME.put(Board.PAWN, "Pawn");
		PIECE_NAME.put(Board.ROOK, "Rook");
		PIECE_NAME.put(Board.KNIGHT, "Knight");
		PIECE_NAME.put(Board.BISHOP, "Bishop");
		PIECE_NAME.put(Board.QUEEN, "Queen");
		PIECE_NAME.put(Board.KING, "King");
	}
	public ChessWindow chessWindow;

	private MoveSet currentMoveSet;

	public ChessGame() {
		chessWindow = new ChessWindow();
		chessWindow.init();
		chessWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		chessWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// getSystem().stop();
				System.exit(0);
			}
		});
		chessWindow.pack();

	}

	private PositionSet identifyPiece(Record piece, boolean mover, boolean own) {
		PositionSet positions = new PositionSet();
		String pieceType = "Piece";
		if (piece != null) {
			pieceType = piece.getString("type", "Piece");
		} 
		// Constrain by piece type
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				int type = chessWindow.board.getSpace(row, col);
				if ((own && type < 0) || (!own && type != 0)) {
					if (pieceType.equals("Piece")
							|| pieceType.equals(PIECE_NAME.get(Math.abs(type)))) {
						positions.add(new Position(row, col));
					}
				}
			}
		}
		if (mover)
			positions.constrain(currentMoveSet.getFrom());
		if (piece != null) {
			// Constrain by position
			if (piece.has("relPiecePos")) {
				positions.constrainRelPiecePos(
						piece.getString("relPiecePos:rel"),
						identifyPiece(piece.getRecord("relPiecePos:piece"), false,
								false));
			}
			if (piece.has("relPos")) {
				positions.constrainRelPos(piece.getString("relPos"));
			}
			if (piece.has("square")) {
				positions.constrainSquare(piece.getString("square:column"),
						piece.getInt("square:row"));
			}
		}
		return positions;
	}

	public Record chooseClarification() {

		Record result = new Record();

		// Which pawn?
		PositionSet from = currentMoveSet.getFrom();
		if (from.size() > 1) {
			HashSet<Integer> types = new HashSet<Integer>();
			for (Position pos : from) {
				types.add(chessWindow.board.getSpace(pos.getRow(), pos.getCol()));
			}
			if (types.size() == 1) {
				int pieceType = Math.abs(types.iterator().next());
				result.put("piece", PIECE_NAME.get(pieceType));
				return result;
			} else {
				result.put("piece", "piece do you want to move");
				return result;
			}
		}
		
		// In which direction?
		if (from.size() == 1) {
			HashMap<Integer, Integer> directions = currentMoveSet.getTo()
					.getDirections(from.get(0));
			if (directions.get(PositionSet.LEFT) == 1
					&& directions.get(PositionSet.RIGHT) == 1) {
				result.put("direction", true);
				return result;
			}
		}

		// How many steps?
		if (currentMoveSet.sizeFrom() == 1
				&& currentMoveSet.getTo().hasDistinctDistances(
						currentMoveSet.get(0).getFrom())) {
			result.put("steps", true);
			return result;
		}

		// To which square?
		//if (from.size() == 1) {
			result.put("square", true);
			return result;
		//}

		//return null;
	}
	
	public void newMove() {
		currentMoveSet = new MoveSet(chessWindow.board.legalMoves(Board.R_SIDE));
	}

	public void identifyMoves(Record sem) {
		if (sem.getRecord("piece") != null) {
			PositionSet pfrom = identifyPiece(sem.getRecord("piece"), true,
					true);
			currentMoveSet.constrainFrom(pfrom);
		}
		if (sem.has("capture")) {
			PositionSet pto = identifyPiece(sem.getRecord("capture"), false, false);
			currentMoveSet.constrainTo(pto);
		}
		if (sem.has("movement:steps")) {
			currentMoveSet.constrainDistance(sem.getInt("movement:steps"));
		}
		if (sem.has("movement:direction")) {
			currentMoveSet.constrainDirection(sem
					.getString("movement:direction"));
		}
		if (sem.has("movement:relPiecePos")) {
			PositionSet pset = currentMoveSet.getTo();
			pset.constrainRelPiecePos(
					sem.getString("movement:relPiecePos:rel"),
					identifyPiece(sem.getRecord("movement:relPiecePos:piece"),
							false, false));
			currentMoveSet.constrainTo(pset);
		}
		if (sem.has("movement:square")) {
			PositionSet pset = currentMoveSet.getTo();
			pset.constrainSquare(sem.getString("movement:square:column"),
					sem.getInt("movement:square:row"));
			currentMoveSet.constrainTo(pset);
		}
	}

	public int availableMoves() {
		return currentMoveSet.size();
	}

	public void performMove() {
		if (availableMoves() == 1) {
			chessWindow.boardDisplay.setAvailableMoves(null);
			currentMoveSet.get(0).apply(chessWindow.board);
			chessWindow.boardDisplay.switchTurns();
		}
	}

	public void displayAvailableMoves() {
		chessWindow.boardDisplay.setAvailableMoves(currentMoveSet);
		chessWindow.boardDisplay.repaint();
	}

	public void start() {
		chessWindow.start();
	}

}
