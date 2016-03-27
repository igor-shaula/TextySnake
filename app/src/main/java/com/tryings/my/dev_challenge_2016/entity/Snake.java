package com.tryings.my.dev_challenge_2016.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Entity for the snake object
 */
public class Snake implements Parcelable {

    private ArrayList<SnakeCell> snakeCellArrayList = new ArrayList<>();

    public Snake() {
        // required after Parcelable implementation \
    }

    protected Snake(Parcel in) {
        // added after Parcelable implementation \
    }

    public static final Creator<Snake> CREATOR = new Creator<Snake>() {
        @Override
        public Snake createFromParcel(Parcel in) {
            return new Snake(in);
        }

        @Override
        public Snake[] newArray(int size) {
            return new Snake[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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