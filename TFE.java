
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

/*
 * Uses MonteCarlo Tree Search to attempt to play the game 2048
 */
public class TFE {

	public static final int BOARD_SIZE = 4;
	public static final int MAX_CHARGES = 5000;
	
	public static final int TRIALS = 50;
	
	private JFrame invisFrame;
	private MoveListener input;
	public static Random r;
	
	public enum Move {
		UP("∧"),
		DOWN("∨"),
		RIGHT(">"),
		LEFT("<");
		
		private String symbol;
		
		Move(String symbol) {
			this.symbol = symbol;
		}
		
		@Override
		public String toString() {
			return symbol;
		}
	}
	
	public TFE() {
		r = new Random(System.currentTimeMillis());
		input = new MoveListener();
		invisFrame = new JFrame();
		invisFrame.addKeyListener(input);
		invisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		invisFrame.setVisible(true);
	}
	
	/* Represent the board as a string */
	public static String getBoardString(int[][] board) {
		StringBuilder str = new StringBuilder();
		for (int y = 0; y < BOARD_SIZE; y++) {
			for (int x = 0; x < BOARD_SIZE; x++) {
				str.append(board[x][y]).append(" ");
			}
			str.append("\n");
		}
		return str.toString();
	}
	
	/* Play an iteration of the game.
	 * if manual, accepts user input; otherwise attempts to
	 * find the best possible move using MCTS */
	public int play(boolean manual) {
		TFETree.DCResult gameResults = new TFETree.DCResult();
		int[][] board = TFESM.createEmptyBoard();
		board = TFESM.generateNewPieces(board, 2);
		TFETree gameTree = new TFETree(board, false, null, null, gameResults);
		while (true) {
			System.out.println(getBoardString(board));
			System.out.println("Current score: " + gameResults.score);
			List<Move> legalMoves = TFESM.findLegalMoves(board);
			if (legalMoves.isEmpty()) {
				System.out.println("No legal moves found! you didn't win this time ;)");
				System.out.println("Final score: " + gameResults.score + " After " + gameResults.moves + " moves");
				return gameResults.score;
			}
			Move m = null;
			if (manual) {
				m = input.getUserInputMove(legalMoves);
			} else {
			    // perform a fixed number of depth charges to improve score estimation
				for (int i = 0; i < MAX_CHARGES; i++) {
					TFETree node = gameTree.select();
					node.expand();
					int score = node.depthCharge();
					node.backpropagate(score);
				}
				m = gameTree.getBestMove();
			}
			// Get result of chosen move
			board = TFESM.getMoveResult(m, board, gameResults);
			gameTree = gameTree.findAndPrune(board);
			// Randomly generate new pieces for next round
			board = TFESM.generateNewPieces(board, 1);
			gameTree = gameTree.findAndPrune(board);
		}
	}

	public static void main(String[] argv) {
		double total = 0;
		for (int i = 0; i < TRIALS; i++) {
			TFE game = new TFE();
			total += game.play(false);
		}
		System.out.println("Average score using MCS: " + (total/TRIALS));
	}
	
	/* Listens for user input */
	public class MoveListener implements KeyListener {
		private Move lastMove;
		private boolean acceptingInput;
		private List<Move> legals;
	
		public MoveListener() {
			acceptingInput = false;
			lastMove = null;
		}
	
		public void keyReleased(KeyEvent event) {
			//System.out.println("Got key press");
			if (!acceptingInput)
				return;
			synchronized(this) {
				if (event.getKeyCode() == KeyEvent.VK_UP && legals.contains(Move.UP))
					lastMove = Move.UP;
				if (event.getKeyCode() == KeyEvent.VK_DOWN && legals.contains(Move.DOWN))
					lastMove = Move.DOWN;
				if (event.getKeyCode() == KeyEvent.VK_LEFT && legals.contains(Move.LEFT))
					lastMove = Move.LEFT;
				if (event.getKeyCode() == KeyEvent.VK_RIGHT && legals.contains(Move.RIGHT))
					lastMove = Move.RIGHT;
				if (lastMove != null) {
					acceptingInput = false;
					this.notifyAll();
				}
			}
		}
		public void keyPressed(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
		
		private Move getUserInputMove(List<Move> legalMoves) {
			legals = legalMoves;
			System.out.println(legals);
			System.out.print(" Please enter a move: ");
			lastMove = null;
			acceptingInput = true;
			synchronized(this) {
				try {
					this.wait();
				} catch (Exception e) {
					throw new Error("Error waiting for user input");
				}
			}
			System.out.println("");
			return lastMove;
		}
	}

}