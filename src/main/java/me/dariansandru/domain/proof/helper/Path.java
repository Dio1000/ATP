package me.dariansandru.domain.proof.helper;

import java.util.List;

/**
 * A path is a list of directions that specifies how to get from a Node
 * in a Tree to another Node.
 * @param directions List of directions needed to be taken to reach the final Node
 */
public record Path(List<Direction> directions) {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < directions.size() ; i++) {
            if (i == directions.size() - 1) builder.append(directions.get(i).toString());
            else builder.append(directions.get(i).toString()).append("\n");
        }

        return builder.toString();
    }
}
