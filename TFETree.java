

import java.util.Arrays;
import java.util.List;

/*
 * Game state tree used by Monte Carlo Tree Search to find the best
 * move by estimating the expected future score from taking that move
 */
public class TFETree {

	// parameter that balances exploration and exploitation
	public static final double C = 15.0;
	
	private TFETree[] children;
	private int visits;
	private boolean isRand;
	private int totalScore;
	private int[][] board;
	private TFETree parent;
	private TFE.Move move;
	private DCResult score;
	
	public TFETree(int[][] board, boolean isRand, TFE.Move move, TFETree parent, DCResult score) {
		this.board = board;
		this.visits = 0;
		this.totalScore = 0;
		this.isRand = isRand;
		this.children = null;
		this.parent = parent;
		this.move = move;
		this.score = score;
	}
	
	/*
	 * Select a node to learn more about. Starts at the root and recurses down as we
	 * explore more nodes
	 */
	public TFETree select() {
		if (visits == 0 || children == null)
			return this;
		for (int i = 0; i < children.length; i++) {
			if (children[i].visits == 0)
				return children[i];
		}
		if (!isRand) {
			double score = -1;
			TFETree result = null;
			for (int i = 0; i < children.length; i++) {
				double newscore = children[i].selectfn(isRand);
				if (newscore>score || result == null) {
					score = newscore;
					result = children[i];
				}
			}
			return result.select();
		} else {
			return children[TFE.r.nextInt(children.length)].select();
		}
	}
	
	/*
	 * Prune the game state tree to reflect a new move or state
	 */
	public TFETree findAndPrune(int[][] board) {
		for (TFETree child : children) {
			if (Arrays.deepEquals(child.board, board)) {
				System.out.println("Pruned game tree with visits: " + child.visits);
				child.parent = null;
				return child;
			}
		}
		throw new Error("Failed to find desired child state in " + children.length + " children");
	}
	
	/*
	 * Expand all possible state children of this node
	 */
	public void expand() {
		if (children != null)
			return;
		if (!isRand) {
			List<TFE.Move> legalMoves = TFESM.findLegalMoves(board);
			if (legalMoves.size() == 0)
				return;
			children = new TFETree[legalMoves.size()];
			for (int i = 0; i < legalMoves.size(); i++) {
				DCResult res = new DCResult();
				res.score = score.score;
				int[][] nextBoard = TFESM.getMoveResult(legalMoves.get(i), board, res);
				children[i] = new TFETree(nextBoard, true, legalMoves.get(i), this, res);
			}
		} else {
			int numBlanks = TFESM.numBlankSpots(board);
			children = new TFETree[numBlanks*2];
			for (int i = 0; i < numBlanks; i++) {
				DCResult res = new DCResult();
				res.score = score.score;
				int[][] nextBoard1 = TFESM.placePieceOnBlankIndex(board, 2, i);
				children[2*i] = new TFETree(nextBoard1, false, null, this, res);
				res = new DCResult();
				res.score = score.score;
				int[][] nextBoard2 = TFESM.placePieceOnBlankIndex(board, 4, i);
				children[2*i + 1] = new TFETree(nextBoard2, false, null, this, res);
			}
		}
	}
	
	/*
	 * Randomly estimate the expected score for this game state by playing randomly
	 * until the game terminates
	 */
	public int depthCharge() {
		DCResult result = new DCResult();
		result.score = score.score;
		int[][] curBoard = board;
		while (true) {
			List<TFE.Move> legalMoves = TFESM.findLegalMoves(curBoard);
			if (legalMoves.isEmpty()) {
				return result.score;
			}
			TFE.Move m = legalMoves.get(TFE.r.nextInt(legalMoves.size()));
			curBoard = TFESM.getMoveResult(m, curBoard, result);
			curBoard = TFESM.generateNewPieces(curBoard, 1);
		}
	}
	
	/*
	 * Used to select a node based on a tradeoff between exploration and exploitation
	 */
	private double selectfn(boolean rand) {
		if (!rand) {
			return getScore()/200.0 + C*Math.sqrt(Math.log(parent.visits)/visits);
		} else {
			throw new Error("Calling selectfn on random node");
		}
	}
	
	public double getScore() {
		return ((double)totalScore)/visits;
	}
	
	public void backpropagate(int score) {
		TFETree onNode = this;
		while (onNode != null) {
			onNode.visits++;
			onNode.totalScore += score;
			onNode = onNode.parent;
		}
	}
	
	public TFE.Move getBestMove() {
		if (isRand) {
			throw new Error("Calling best move on random node!");
		}
		double bestScore = 0;
		TFETree bestMove = null;
		for (TFETree child : children) {
			if (bestMove == null || child.getScore() > bestScore) {
				bestScore = child.getScore();
				bestMove = child;
			}
			System.out.println("Move " + child.move + " has visits: " + child.visits + " and score: " + child.getScore());
		}
		System.out.println("Best move has expected score: " + bestMove.getScore());
		return bestMove.move;
	} 
	
	public static class DCResult {
		public int score = 0;
		public int moves = 0;
	}

}