package com.igor.shaula.snake_in_text.util;

/**
 * Created by igor shaula
 */
public class MyPSF {

    // basic symbols to create starting game field \
    public static final char SNAKE = '@';
    public static final char SPACE = ' ';
    public static final char BORDER = '+';

    // types of food for the snake \
    public static final char LENGTH_PLUS = '$'; // +1 to length - this is the main type of food \
    public static final char LENGTH_MINUS = '-'; // -1 from length
    public static final char SPEED_UP = '#'; // +1 to speed
    public static final char SPEED_SLOW = '*'; // -1 from speed

    public static final int SHORT_VIBRATION = 100;
    public static final int LONG_VIBRATION = 300;
    public static final int STARTING_SNAKE_LENGTH = 3;
    public static final int STARTING_SNAKE_SPEED = 3;
/*
        // keys for saving-restoring after screen orientation change \
        public static final String KEY_SNAKE = "snake";
        public static final String KEY_SNAKE_SPEED = "snakeSpeed";
        public static final String KEY_SNAKE_DIRECTION = "snakeDirection";
        public static final String KEY_SCORE = "score";
        public static final String KEY_ALREADY_LAUNCHED = "alreadyLaunched";
        public static final String KEY_WAS_GAME_OVER = "wasGameOver";
*/
}