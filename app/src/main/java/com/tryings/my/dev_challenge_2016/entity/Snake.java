package com.tryings.my.dev_challenge_2016.entity;

/**
 * Entity for the snake object
 */
public class Snake {

    private int length;

    private SnakeCell[] snakeCells;
    // TODO: 22.03.2016 transform array to linked list \

    public Snake(int length) {
        this.length = length;
        snakeCells = new SnakeCell[length];
    }

    public int getLength() {
        return length;
    }

    public void addSnakeCell(int index, SnakeCell newCell) {
        snakeCells[index] = newCell;
    }

    public SnakeCell getSnakeCell(int index) {
        return snakeCells[index];
    }

    /**
     * definition for every cell inside the snake object \
     */
    public static class SnakeCell {

        private int rowIndex, indexInRow;

        // i decided to set every cell as a simple object \
        public SnakeCell(int rowIndex, int indexInRow) {
            this.rowIndex = rowIndex;
            this.indexInRow = indexInRow;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public int getIndexInRow() {
            return indexInRow;
        }
    }
}