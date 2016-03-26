package com.tryings.my.dev_challenge_2016.entity;

import java.util.ArrayList;

/**
 * Entity for the snake object
 */
public class Snake {

    private ArrayList<SnakeCell> snakeCellArrayList = new ArrayList<>();

    public void addCell(int index, SnakeCell newCell) {
        snakeCellArrayList.add(index, newCell);
    }

    public void removeCell(int index) {
        snakeCellArrayList.remove(index);
    }

    public SnakeCell getCell(int index) {
        return snakeCellArrayList.get(index);
    }

    public int getLength() {
        return snakeCellArrayList.size();
    }

    /**
     * definition for every cell inside the snake object \
     */
    public static class SnakeCell {

        private int indexOfRow, indexOfSymbol;

        // i decided to set every cell as a simple object \
        public SnakeCell(int x, int y) {
            this.indexOfSymbol = x;
            this.indexOfRow = y;
        }

        public int getIndexOfRow() {
            return indexOfRow;
        }

        public void setIndexOfRow(int indexOfRow) {
            this.indexOfRow = indexOfRow;
        }

        public int getIndexOfSymbol() {
            return indexOfSymbol;
        }

        public void setIndexOfSymbol(int indexOfSymbol) {
            this.indexOfSymbol = indexOfSymbol;
        }
    }
}