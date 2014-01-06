package iristk.app.chess;

public interface GameListener {

	void tentativeMove(Move move);

	void move(Move move);
	
	void gameRestart();

}
