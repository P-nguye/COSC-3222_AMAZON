package ubc.cosc322;

import java.util.ArrayList;
import java.util.Map;

import ubc.cosc322.AI.Move;

public class miniMaxAI {

    private final int MAX = 1000;
    private final int MIN = -1000;
    private final int DEPTH = AI.DEPTH;

    private ArrayList<Move> tree;

    public miniMaxAI(ArrayList<Move> t) {
        tree = t;
    }

    public Map<String, Object> calculateNextMove() {
        int treeSize = tree.size();
        Position bestPosition = new Position(MIN, null);
        for (int i = 0; i < treeSize; i++) {
            ArrayList<Integer> pos = new ArrayList<>();
            pos.add(i);
            Position bestMove = calculateMiniMax(pos, 0, true, MIN, MAX);
            if (bestMove.value > bestPosition.value) {
                bestPosition = bestMove;
            }
        }
        return getParent(bestPosition.position).getMapMove();
    }

    public Position calculateMiniMax(ArrayList<Integer> position, int depth, boolean self, int alpha, int beta) {
        Move current = getNode(position);
        if (depth >= DEPTH || current.children.isEmpty()) {
            return new Position(current.score, position);
        }
        Position bestPosition = new Position(self ? MIN : MAX, null);
        int childSize = current.children.size();
        for (int i = 0; i < childSize; i++) {
            ArrayList<Integer> newPosition = new ArrayList<>(position);
            newPosition.add(i);
            Position pos = calculateMiniMax(newPosition, depth + 1, !self, alpha, beta);
            if ((self && pos.value > bestPosition.value) || (!self && pos.value < bestPosition.value)) {
                bestPosition = pos;
            }
            if (self) {
                alpha = Math.max(bestPosition.value, alpha);
            } else {
                beta = Math.min(bestPosition.value, beta);
            }
            if (alpha >= beta) {
                break;
            }
        }
        return bestPosition;
    }

    private Move getNode(ArrayList<Integer> position) {
        Move current = tree.get(position.get(0));
        int size = position.size();
        for (int i = 1; i < size; i++) {
            current = current.children.get(position.get(i));
        }
        return current;
    }

    private Move getParent(ArrayList<Integer> position) {
        return tree.get(position.get(0));
    }

    private static class Position {
        int value;
        ArrayList<Integer> position;

        public Position(int v, ArrayList<Integer> p) {
            value = v;
            position = p;
        }
    }
}
