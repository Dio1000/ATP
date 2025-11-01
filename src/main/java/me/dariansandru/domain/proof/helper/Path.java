package me.dariansandru.domain.proof.helper;

import java.util.List;

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
