
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * State Machine class used to manipulate the underlying state of a given
 * game of 2048. Defensively copies returned state
 */
public class TFESM {
	
	public static final int BOARD_SIZE = 4;
	
	public static int[][] getMoveResult(TFE.Move m, int[][] board, TFETree.DCResult result) {
		int[][] nextBoard = createEmptyBoard();
		int[][] combined = createEmptyBoard();
		boolean forwards = (m == TFE.Move.LEFT || m == TFE.Move.UP);
		for (int i = 0; i < BOARD_SIZE*BOARD_SIZE; i++) {
			int onSquare = forwards ? i : BOARD_SIZE*BOARD_SIZE - i - 1;
			int x = onSquare % BOARD_SIZE;
			int y = onSquare / BOARD_SIZE;
			if (board[x][y] != 0) {
				moveTile(x, y, nextBoard, m, combined, result, board);
			}
		}
		if (result != null)
			result.moves++;
		return nextBoard;
	}
	
	public static int[][] generateNewPieces(int[][] board, int numPieces) {
		for (int i = 0; i < numPieces; i++) {
			int pieceVal = TFE.r.nextBoolean() ? 2 : 4;
			board = placeNewPiece(board, pieceVal);
		}
		return board;
	}
	
	// randomly places a new piece
	public static int[][] placeNewPiece(int[][] board, int value) {
		int blankSpots = numBlankSpots(board);
		int placementIndex = TFE.r.nextInt(blankSpots);
		return placePieceOnBlankIndex(board, value, placementIndex);
	}
	
	public static int numBlankSpots(int[][] board) {
		int blankSpots = 0;
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = 0; y < BOARD_SIZE; y++) {
				if (board[x][y] == 0)
					blankSpots++;
			}
		}
		return blankSpots;
	}
	
	public static int[][] createEmptyBoard() {
		int[][] newBoard = new int[BOARD_SIZE][BOARD_SIZE];
		for (int x = 0; x < BOARD_SIZE; x++) {
			newBoard[x] = new int[BOARD_SIZE];
		}
		return newBoard;
	}
	
	public static int[][] placePieceOnBlankIndex(int[][] board, int value, int index) {
		int[][] newBoard = createEmptyBoard();
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = 0; y < BOARD_SIZE; y++) {
				newBoard[x][y] = board[x][y];
			}
		}
		int onBlankIndex = 0;
		for (int x = 0; x < BOARD_SIZE; x++) {
			for (int y = 0; y < BOARD_SIZE; y++) {
				if (board[x][y] == 0) {
					if (onBlankIndex == index) {
						newBoard[x][y] = value;
						return newBoard;
					}
					onBlankIndex++;
				}
			}
		}
		throw new Error("Couldn't place new piece");
	}
	
	/*
	 * Returns a list of all moves that are legal for the player for this board
	 */
	public static List<TFE.Move> findLegalMoves(int[][] board) {
		List<TFE.Move> legalMoves = new ArrayList<TFE.Move>();
		for (TFE.Move m : TFE.Move.values()) {
			int[][] result = getMoveResult(m, board, null);
			if (!Arrays.deepEquals(result, board))
				legalMoves.add(m);
		}
		return legalMoves;
	}
	
	/*
	 * slides a single tile in a direction gives by the TFE.Move m until it hits an
	 * obstacle or the edge of the board
	 */
	private static void moveTile(int x, int y, int[][] nextBoard, TFE.Move m, int[][] combined, TFETree.DCResult result, int[][] board) {
		int val = board[x][y];
		nextBoard[x][y] = val;
		switch (m) {
			case RIGHT:
				for (int onX = x + 1; onX < BOARD_SIZE; onX++) {
					if (nextBoard[onX][y] == val && combined[onX][y] == 0) {
						// combine
						nextBoard[onX][y] = val*2;
						combined[onX][y] = 1;
						if (result != null)
							result.score += val*2;
					} else if (nextBoard[onX][y] == 0) {
					    // move
					    nextBoard[onX][y] = val;
					} else {
						// done moving
						break;
					}
					nextBoard[onX - 1][y] = 0;
				}
				break;
			case DOWN:
				for (int onY = y + 1; onY < BOARD_SIZE; onY++) {
					if (nextBoard[x][onY] == val && combined[x][onY] == 0) {
						// combine
						nextBoard[x][onY] = val*2;
						combined[x][onY] = 1;
						if (result != null)
							result.score += val*2;
					} else if (nextBoard[x][onY] == 0) {
					    // move
					    nextBoard[x][onY] = val;
					} else {
						// done moving
						break;
					}
					nextBoard[x][onY - 1] = 0;
				}
				break;
			case LEFT:
				for (int onX = x - 1; onX >= 0; onX--) {
					if (nextBoard[onX][y] == val && combined[onX][y] == 0) {
						// combine
						nextBoard[onX][y] = val*2;
						combined[onX][y] = 1;
						if (result != null)
							result.score += val*2;
					} else if (nextBoard[onX][y] == 0) {
					    // move
					    nextBoard[onX][y] = val;
					} else {
						// done moving
						break;
					}
					nextBoard[onX + 1][y] = 0;
				}
				break;
			case UP:
				for (int onY = y - 1; onY >= 0; onY--) {
					if (nextBoard[x][onY] == val && combined[x][onY] == 0) {
						// combine
						nextBoard[x][onY] = val*2;
						combined[x][onY] = 1;
						if (result != null)
							result.score += val*2;
					} else if (nextBoard[x][onY] == 0) {
					    // move
					    nextBoard[x][onY] = val;
					} else {
						// done moving
						break;
					}
					nextBoard[x][onY + 1] = 0;
				}
				break;
		}
	}
}