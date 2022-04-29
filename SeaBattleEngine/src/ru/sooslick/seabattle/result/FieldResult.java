package ru.sooslick.seabattle.result;

import java.util.List;

public class FieldResult {
    private List<Row> rows;

    @SuppressWarnings("unused")
    public FieldResult() {}

    public FieldResult(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() {
        return rows;
    }

    public static class Row {
        private List<Integer> cols;

        public Row() {}

        public Row(List<Integer> cols) {
            this.cols = cols;
        }

        public List<Integer> getCols() {
            return cols;
        }
    }
}
