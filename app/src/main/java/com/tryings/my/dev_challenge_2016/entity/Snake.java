package com.tryings.my.dev_challenge_2016.entity;

/**
 * Entity for the snake object
 */
public class Snake {

    private int length;

    private SnakeCell[] snakeCells;
    // TODO: 22.03.2016 may be transform array to LinkedList or something like this ? \

    public Snake(int length) {
        this.length = length;
        snakeCells = new SnakeCell[length];
    }

    public int getLength() {
        return length;
    }

    public void addCell(int index, SnakeCell newCell) {
        snakeCells[index] = newCell;
    }

    public SnakeCell getCell(int index) {
        return snakeCells[index];
    }

    /**
     * definition for every cell inside the snake object \
     */
    public static class SnakeCell {

        private int indexOfRow, indexOfSymbol;

        // i decided to set every cell as a simple object \
        public SnakeCell(int indexOfRow, int indexOfSymbol) {
            this.indexOfRow = indexOfRow;
            this.indexOfSymbol = indexOfSymbol;
        }

        public int getIndexOfRow() {
            return indexOfRow;
        }

        public int getIndexOfSymbol() {
            return indexOfSymbol;
        }

        public void setIndexOfRow(int indexOfRow) {
            this.indexOfRow = indexOfRow;
        }

        public void setIndexOfSymbol(int indexOfSymbol) {
            this.indexOfSymbol = indexOfSymbol;
        }
    }
}