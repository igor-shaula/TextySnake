package com.tryings.my.dev_challenge_2016.entity;

/**
 * Entity for the snake object
 */
public class Snake {

    private int length;
    private SnakeCell[] snakeCells = new SnakeCell[length];

    public Snake(int length) {
        this.length = length;

    }

    public int getLength() {
        return length;
    }

    public SnakeCell getSnakeCell(int index) {
        return snakeCells[index];
    }

    /**
     * definition for every cell inside the snake object \
     */
    public static class SnakeCell {

        private int rowNumber, symbolInRow;

        public int getRowNumber() {
            return rowNumber;
        }

        public int getSymbolInRow() {
            return symbolInRow;
        }
    }
}