package ru.sooslick.seabattle.result;

import java.util.List;

public class FieldResult {
    private List<FieldResult.Row> rows;

    public FieldResult(List<Row> rows) {
        this.rows = rows;
    }

    public static class Row {
        private List<Integer> cols;

        public Row(List<Integer> cols) {
            this.cols = cols;
        }
    }
}
