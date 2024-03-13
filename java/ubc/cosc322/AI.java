package ubc.cosc322;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collections;
import java.util.Stack;
public class AI {

	ArrayList<Integer> board;
	int player;
	int opponent;
	int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
	public static final int DEPTH = 3;
	public static final int WIDTH = 3;

	 public AI(ArrayList<Integer> startBoard, int id) {
	        this.board = startBoard;
	        this.player = id;
	        this.opponent = (player == 1) ? 2 : 1;
	    }

	    public Map<String, Object> calculateNextMove() {
	        ArrayList<Move> moveTree = new ArrayList<>();
	        fillMoveTree(player, 0, moveTree, null);

	        if (moveTree.isEmpty()) {
	            return null;
	        }

	        miniMaxAI treeSearch = new miniMaxAI(moveTree);
	        return treeSearch.calculateNextMove();
	    }

	    public void fillMoveTree(int person, int depth, ArrayList<Move> tree, Move current) {
	        if (depth >= DEPTH) {
	            return;
	        }
	        
	        getBestMove(person, tree, current);
	        
	        for (Move move : tree) {
	            int nextPerson = (person == player) ? opponent : player;
	            fillMoveTree(nextPerson, depth + 1, move.children, move);
	        }
	    }

	    public void getBestMove(int person, ArrayList<Move> moveTree, Move current) {
	        ArrayList<ArrayList<Integer>> queens = getQueenLocations(person);
	        TreeMap<Integer, ArrayList<ArrayList<Integer>>> scores = new TreeMap<>(Collections.reverseOrder());
	        ArrayList<Integer> originalBoard = (ArrayList<Integer>) board.clone();
	        Stack<Move> moveStack = new Stack<>();
	        
	        if (current != null) {
	            Move cur = current;
	            while (cur.parent != null) {
	                moveStack.add(cur.parent);
	                cur = cur.parent;
	            }
	            while (!moveStack.isEmpty()) {
	                cur = moveStack.pop();
	                updateGameState(cur.getMapMove(), cur.person == player);
	            }
	        }

	        for (ArrayList<Integer> queen : queens) {
	            ArrayList<ArrayList<Integer>> moves = getPossibleMoves(queen);
	            for (ArrayList<Integer> move : moves) {
	                ArrayList<Integer> backup = (ArrayList<Integer>) board.clone();
	                updateQueen(queen, move, person);
	                ArrayList<ArrayList<Integer>> data = new ArrayList<>();
	                data.add(queen);
	                data.add(move);
	                scores.put(calculateBoard(person), data);
	                board = backup;
	            }
	        }

	        TreeMap<Integer, Move> bestMoves = new TreeMap<>(Collections.reverseOrder());
	        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> moveEntry : scores.entrySet()) {
	            ArrayList<Integer> queenCur = moveEntry.getValue().get(0);
	            ArrayList<Integer> queenNext = moveEntry.getValue().get(1);
	            ArrayList<Integer> backup = (ArrayList<Integer>) board.clone();
	            updateQueen(queenCur, queenNext, person);
	            ArrayList<ArrayList<Integer>> arrowMoves = getPossibleMoves(queenNext);
	            for (ArrayList<Integer> arrowMove : arrowMoves) {
	                ArrayList<Integer> backup2 = (ArrayList<Integer>) board.clone();
	                updateArrow(arrowMove);
	                int score = calculateBoard(person);
	                bestMoves.put(score, new Move(queenCur, queenNext, arrowMove, board, score, current, person));
	                board = backup2;
	            }
	            board = backup;
	        }

	        int count = 0;
	        for (Map.Entry<Integer, Move> moveEntry : bestMoves.entrySet()) {
	            if (count >= WIDTH) {
	                break;
	            }
	            moveTree.add(moveEntry.getValue());
	            count++;
	        }
	        board = originalBoard;
	    }

	    public ArrayList<ArrayList<Integer>> getPossibleMoves(ArrayList<Integer> start) {
	        ArrayList<ArrayList<Integer>> possibleMoves = new ArrayList<>();
	        for (int[] direction : directions) {
	            int row = start.get(0) + direction[0];
	            int column = start.get(1) + direction[1];
	            while (isWithinBounds(row, column)) {
	                int position = (row * 11) + column;
	                if (board.get(position) == 0) {
	                    ArrayList<Integer> move = new ArrayList<>(List.of(row, column));
	                    possibleMoves.add(move);
	                } else {
	                    break;
	                }
	                row += direction[0];
	                column += direction[1];
	            }
	        }
	        return possibleMoves;
	    }

	    private boolean isWithinBounds(int row, int column) {	//helper method. 
	        return row >= 1 && row <= 10 && column >= 1 && column <= 10;
	    }

	    public int calculateBoard(int person) {
	        int playerScore = 0;
	        int opponentScore = 0;

	        for (int row = 1; row <= 10; row++) {
	            for (int col = 1; col <= 10; col++) {
	                int cell = board.get(getMoveFromCoordinates(row, col)); //calculate the index of each cell in the board
	                if (cell != 0) {
	                    continue;
	                }

	                int closestPlayer = getPlayerClosest(getMoveFromCoordinates(row, col));
	                if (closestPlayer == player) {
	                    playerScore++;
	                } else if (closestPlayer == opponent) {
	                    opponentScore++;
	                }
	            }
	        }

	        return (person == player) ? (playerScore - opponentScore) : (opponentScore - playerScore);
	    }

	    public ArrayList<ArrayList<Integer>> getQueenLocations(int id) {
	        ArrayList<ArrayList<Integer>> queens = new ArrayList<ArrayList<Integer>>();
	        for (int row = 1; row <= 10; row++) {
	            for (int col = 1; col <= 10; col++) {
	                int index = getMoveFromCoordinates(row, col);
	                if (board.get(index) == id) {
	                    ArrayList<Integer> queen = new ArrayList<Integer>();
	                    queen.add(row);
	                    queen.add(col);
	                    queens.add(queen);
	                }
	            }
	        }
	        return queens;
	    }

	    public int getPlayerClosest(int space){
	        List<Integer> playerDistances = new ArrayList<>();
	        List<Integer> opponentDistances = new ArrayList<>();

	        List<ArrayList<Integer>> playerQueens = getQueenLocations(player);
	        for(ArrayList<Integer> queen: playerQueens){
	            int distance = getDistance(queen, space);
	            playerDistances.add(distance);
	        }

	        List<ArrayList<Integer>> opponentQueens = getQueenLocations(opponent);
	        for(ArrayList<Integer> queen: opponentQueens){
	            int distance = getDistance(queen, space);
	            opponentDistances.add(distance);
	        }

	        int playerMin = Collections.min(playerDistances);	//find the minimum value in the list. 
	        int opponentMin = Collections.min(opponentDistances);

	        if(playerMin < opponentMin){
	            return player;
	        }
	        if(opponentMin < playerMin){
	            return opponent;
	        }
	        return -1;
	    }

	    public int getMoveFromCoordinates(int x, int y) {
	        return x * 11 + y;
	    }

	    public int getMoveFromCoordinates(ArrayList<Integer> space) {
	        return space.get(0) * 11 + space.get(1);
	    }

	    public ArrayList<Integer> getCoordinatesFromSpace(int space) {
	        int row = space / 11;
	        int col = space % 11;
	        return new ArrayList<Integer>(List.of(row, col));
	    }

	    public int getDistance(ArrayList<Integer> queen, int space) {
	        int queenSpace = getMoveFromCoordinates(queen);
	        if (queenSpace == space) {
	            return 0;
	        }

	        ArrayList<ArrayList<Integer>> queenMoves = getPossibleMoves(queen);
	        if (queenMoves.stream().anyMatch(move -> getMoveFromCoordinates(move) == space)) {
	            return 1;
	        }

	        ArrayList<ArrayList<Integer>> spaceMoves = getPossibleMoves(getCoordinatesFromSpace(space));
	        if (spaceMoves.stream().anyMatch(move -> queenMoves.stream().anyMatch(queenMove -> getMoveFromCoordinates(queenMove) == getMoveFromCoordinates(move)))) {
	            return 2;
	        }

	        return 3;
	    }

	    public Map<String, Object> createMove(ArrayList<Integer> queen, ArrayList<Integer> queenNext, ArrayList<Integer> arrow){
	        Map<String, Object> move = new HashMap<>();
	        move.put(AmazonsGameMessage.QUEEN_POS_CURR, queen);
	        move.put(AmazonsGameMessage.QUEEN_POS_NEXT, queenNext);
	        move.put(AmazonsGameMessage.ARROW_POS, arrow);
	        return move;
	    }

	    public void updateGameState(Map<String, Object> move, boolean self){
	        ArrayList<Integer> cur = (ArrayList<Integer>) move.get(AmazonsGameMessage.QUEEN_POS_CURR);
	        board.set((cur.get(0) * 11) + cur.get(1), 0);

	        ArrayList<Integer> next = (ArrayList<Integer>) move.get(AmazonsGameMessage.QUEEN_POS_NEXT);
	        board.set((next.get(0) * 11) + next.get(1), self ? player : opponent);

	        ArrayList<Integer> arrow = (ArrayList<Integer>) move.get(AmazonsGameMessage.ARROW_POS);
	        board.set((arrow.get(0) * 11) + arrow.get(1), 3);
	    }

	    public void updateQueen(ArrayList<Integer> queen, ArrayList<Integer> nextQueen, int person) {
	        int currIndex = queen.get(0) * 11 + queen.get(1);
	        int nextIndex = nextQueen.get(0) * 11 + nextQueen.get(1);
	        board.set(currIndex, 0);
	        board.set(nextIndex, person);
	    }

	    public void updateArrow(ArrayList<Integer> arrow) {
	        int arrowIndex = arrow.get(0) * 11 + arrow.get(1);
	        board.set(arrowIndex, 3);
	    }


		public class Move {
			public ArrayList<Integer> queenCur;
			public ArrayList<Integer> queenNext;
			public ArrayList<Integer> arrow;
			public ArrayList<Move> children;
			public ArrayList<Integer> board;
			public Move parent;
			public int score;
			public int person;

			public Move(ArrayList<Integer> current, ArrayList<Integer> next, ArrayList<Integer> ar, ArrayList<Integer> bo, int sc, Move parent, int player){
				queenCur = current;
				queenNext = next;
				arrow = ar;
				board = bo;
				score = sc;
				children = new ArrayList<>();
				person = player;
			}

			public Map<String, Object> getMapMove(){
				return createMove(queenCur, queenNext, arrow);
			}		

		}
}