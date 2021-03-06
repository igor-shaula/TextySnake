package com.igor.shaula.snake_in_text.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.igor.shaula.snake_in_text.R;
import com.igor.shaula.snake_in_text.custom_views.MyTextView;
import com.igor.shaula.snake_in_text.entity.Snake;
import com.igor.shaula.snake_in_text.utils.MyLog;
import com.igor.shaula.snake_in_text.utils.MyPSF;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by igor shaula - main class holding others to avoid too many code lines \
 */
public class MainActivity extends AppCompatActivity {

   private final char[] mFoodTypeArray = // LENGTH_PLUS will be as often as other values in sum \
            {MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS,
                     MyPSF.LENGTH_MINUS, MyPSF.SPEED_SLOW, MyPSF.SPEED_UP};

   // definition of the field \
   private char mFoodType;
   private int mFoodPositionRow, mFoodPositionSymbol;
   private int mOldFoodPositionX, mOldFoodPositionY;
   private int mFieldPixelWidth, mFieldPixelHeight; // in pixels
   private int mSymbolsInFieldLine, mFieldLinesCount; // in items - for arrays \

   // main data storage \
   private ArrayList<char[]> mCharsArrayList;

   private MyTextView mtvUserGuide, mtvMainField, mtvScore, mtvTime;

   // utils from the system \
   private Vibrator mVibrator;
   private Random mRandom = new Random();
   private Timer mTimer;

   // definition of the snake model \
   private Snake mSnake;
   private int mSnakeSpeed = MyPSF.STARTING_SNAKE_SPEED;
   private MyDirections mSnakeDirection;

   // game parameters \
   private boolean mGameEnded = false, mGamePausedSwitch = false;
   private int mCurrentScore, mBestScore;
   private long mCurrentTime, mBestTime;

   private GestureDetector mGestureDetector;

   // LIFECYCLE ===================================================================================

   @SuppressLint("InflateParams")
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // game depends on screen orientation changes \
      int screenOrientation = getResources().getConfiguration().orientation;

      // setting action bar \
      Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
      setSupportActionBar(myToolbar);

      assert myToolbar != null;
      myToolbar.setContentInsetsAbsolute(0, 0);

      View myToolbarView = getLayoutInflater().inflate(R.layout.my_toolbar_view, null);

      Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
               ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

      myToolbar.addView(myToolbarView, layoutParams);

      // setting show-scores-button and set-speed-button \
      MyTextView mtvShowScores = null, mtvSetSpeed = null;

      if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
         mtvShowScores = (MyTextView) findViewById(R.id.mtvShowScores_P);
         mtvSetSpeed = (MyTextView) findViewById(R.id.mtvSetSpeed_P);
      } else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
         mtvShowScores = (MyTextView) findViewById(R.id.mtvShowScores_L);
         mtvSetSpeed = (MyTextView) findViewById(R.id.mtvSetSpeed_L);
      }

      assert mtvShowScores != null;
      mtvShowScores.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            showScoresDialog();
         }
      });

      assert mtvSetSpeed != null;
      mtvSetSpeed.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            showSetSpeedDialog();
         }
      });

      mtvUserGuide = (MyTextView) findViewById(R.id.mtvUserGuide);
      mtvUserGuide.setMovementMethod(new ScrollingMovementMethod());
      mtvUserGuide.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
            initializeGame();
            return false;
         }
      });

      // initializing other informative views \
      mtvScore = (MyTextView) findViewById(R.id.mtvScore);
      mtvTime = (MyTextView) findViewById(R.id.mtvTime);

      // defining main game field \
      mtvMainField = (MyTextView) findViewById(R.id.viewMainField);

      // vibrator will be used when eating food or something else happens \
      mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

      // now setting the top - gesture sensitive interface \
      mGestureDetector = new GestureDetector(this, new MyGestureListener());

      // reading previous achievements from SP \
      SharedPreferences sharedPreferences = getSharedPreferences(MyPSF.S_P_NAME, MODE_PRIVATE);
      mBestScore = sharedPreferences.getInt(MyPSF.KEY_SCORE, 0);
      mBestTime = sharedPreferences.getLong(MyPSF.KEY_TIME, 0);
   } // end of onCreate-method \\

   @Override
   public boolean onTouchEvent(MotionEvent motionEvent) {
      return mGestureDetector.onTouchEvent(motionEvent);
   }

   // SAVE_RESTORE ================================================================================

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      MyLog.i("onSaveInstanceState worked");
   }

   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      MyLog.i("onRestoreInstanceState worked");
   }

   // MAIN SEQUENCE ===============================================================================

   // 0 - from onGlobalLayout-method

   private void initializeGame() {
      MyLog.i("initializeGame started");

      // hiding the readme - now it's useless \
      mtvUserGuide.setVisibility(View.GONE);

      // revealing the main field to get it ready for playing \
      final FrameLayout flMain = (FrameLayout) findViewById(R.id.flMain);
      assert flMain != null;
      flMain.setVisibility(View.VISIBLE);

      mtvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
               new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override
                  public void onGlobalLayout() {

                     // getting current screen size in pixels \
                     mFieldPixelWidth = mtvMainField.getWidth();
                     MyLog.i("mFieldPixelWidth " + mFieldPixelWidth);

                     mFieldPixelHeight = flMain.getHeight();
                     MyLog.i("mFieldPixelHeight " + mFieldPixelHeight);

                     ViewTreeObserver.OnGlobalLayoutListener listener = this;
                     // now removing the listener - it's not needed any more \
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                        mtvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
                     else
                        //noinspection deprecation
                        mtvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(listener);

                     // here we get pixel width of a single symbol - initial TextView has only one symbol \
                     mtvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                     int measuredSymbolWidth = mtvMainField.getMeasuredWidth();
                     MyLog.i("measuredSymbolWidth " + measuredSymbolWidth);

                     // getting our first valuable parameter - the size of main array \
                     mSymbolsInFieldLine = mFieldPixelWidth / measuredSymbolWidth;
                     MyLog.i("mSymbolsInFieldLine " + mSymbolsInFieldLine);

                     prepareGameIn4Steps();
                     actionStartGame();
                  }
               });
      MyLog.i("initializeGame ended");
   } // end of initializeGame-method \\

   // main repeatable sequence of steps \
   private void prepareGameIn4Steps() {
      step_1_prepareTextField(); // 1
      step_2_setFieldBorders(); // 2
      step_3_setInitialSnake(); // 3
      step_4_setInitialFood(); // 4
   }

   // 1 - from onGlobalLayout-method
   private void step_1_prepareTextField() {

      // clearing the text field to properly initialize it for game \
      mtvMainField.setText(null);

      // fixing the total height of the line in our main field \
      assert mtvMainField != null;
      mtvMainField.setSquareSymbols();

      // now preparing our model and initializing text field \
      int i = 0, measuredTextHeight;
      mCharsArrayList = new ArrayList<>();
      do {
         // filling up out TextView to measure its lines and set initial state at once \
         char[] charArray = new char[mSymbolsInFieldLine + 1];
         // setting end element for a new line on the next array \
         charArray[mSymbolsInFieldLine] = '\n';
         // setting other elements to their default values \
         for (int j = 0; j < mSymbolsInFieldLine; j++) {
            charArray[j] = MyPSF.SPACE;
         }
         // now single char array is ready and has to be added to the list \
         mCharsArrayList.add(i, charArray);
//            MyLog.i("added " + new String(mCharsArrayList.get(i)));

         String previousText = mtvMainField.getText().toString();
         String newText = previousText + new String(mCharsArrayList.get(i));
         mtvMainField.setText(newText);
//            MyLog.i("newText \n" + newText);

         i++;

         mtvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
         measuredTextHeight = mtvMainField.getMeasuredHeight();
//            MyLog.i("measuredTextHeight " + measuredTextHeight);

      } while (measuredTextHeight <= mFieldPixelHeight);

      mFieldLinesCount = i;
      MyLog.i("mFieldLinesCount " + mFieldLinesCount);
   } // end of step_1_prepareTextField-method \\

   // 2 - from onGlobalLayout-method
   private void step_2_setFieldBorders() {
      for (int i = 0; i < mFieldLinesCount; i++)
         for (int j = 0; j < mSymbolsInFieldLine; j++)
            if (i == 0 || i == mFieldLinesCount - 1 || j == 0 || j == mSymbolsInFieldLine - 1)
               mCharsArrayList.get(i)[j] = MyPSF.BORDER;
   }

   // 3 - from onGlobalLayout-method
   private void step_3_setInitialSnake() {

      mSnake = new Snake();

      // defining start directions to properly set up the mSnake \
      switch (mRandom.nextInt(3) + 1) {
         case 0:
            mSnakeDirection = MyDirections.RIGHT;
            break;
         case 1:
            mSnakeDirection = MyDirections.UP;
            break;
         case 2:
            mSnakeDirection = MyDirections.LEFT;
            break;
         case 3:
            mSnakeDirection = MyDirections.DOWN;
      }

      for (int i = 0; i < MyPSF.STARTING_SNAKE_LENGTH; i++) {
         // here we define position of every mSnake's cell \
         int cellPositionX = 0, cellPositionY = 0;
         // setting tail in the opposite direction here - to free space for head \
         switch (mSnakeDirection) {
            case RIGHT: {
               cellPositionX = mSymbolsInFieldLine / 2 - i;
               cellPositionY = mFieldLinesCount / 2;
               break;
            }
            case UP: {
               cellPositionX = mSymbolsInFieldLine / 2;
               cellPositionY = mFieldLinesCount / 2 + i;
               break;
            }
            case LEFT: {
               cellPositionX = mSymbolsInFieldLine / 2 + i;
               cellPositionY = mFieldLinesCount / 2;
               break;
            }
            case DOWN: {
               cellPositionX = mSymbolsInFieldLine / 2;
               cellPositionY = mFieldLinesCount / 2 - i;
               break;
            }
         }
         // now it is time to create new mSnake cell \
         Snake.SnakeCell newCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
         // updating the mSnake's model \
         mSnake.addCell(i, newCell);
         // placing the this mSnake cell to our field \
         mCharsArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SNAKE;
      } // end of for-loop
      updateMainField();
   } // end of step_3_setInitialSnake-method \\

   // 4 - from onGlobalLayout-method
   private void step_4_setInitialFood() {
      updateFood();
      updateMainField();
   } // end of step_4_setInitialFood-method \\

   // UTILS =======================================================================================

   private void updateFood() {
/*
        this method gets called after the mSnake is initialized -so we have to check collisions \
        i decided to do all in one cycle because of low probability of collisions \
*/
      // first of all clearing old place \
      char replaceToSpace = mCharsArrayList.get(mOldFoodPositionY)[mOldFoodPositionX];
      if (replaceToSpace != MyPSF.BORDER) // this might happen at the very start \
         mCharsArrayList.get(mOldFoodPositionY)[mOldFoodPositionX] = MyPSF.SPACE;
      // now everything is clear and we can set new food type and position \
      do {
            /*
            range for mRandom:
            decreased by 2 to exclude visible field borders \
            again decreased by 2 to exclude near-border dangerous positions \
            increased by 1 to include the whole range of values because of method specifics \
            */
         mFoodPositionRow = mRandom.nextInt(mFieldLinesCount - 2 - 2) + 1;
         mFoodPositionSymbol = mRandom.nextInt(mSymbolsInFieldLine - 2 - 2) + 1;
         // -1 instead of +1 just to avoid placing food on the boards \
         MyLog.i("mRandom mFoodPositionRow " + mFoodPositionRow);
         MyLog.i("mRandom mFoodPositionSymbol " + mFoodPositionSymbol);
      } while (mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] == MyPSF.SNAKE);

      mOldFoodPositionX = mFoodPositionSymbol;
      mOldFoodPositionY = mFoodPositionRow;

      mFoodType = mFoodTypeArray[mRandom.nextInt(mFoodTypeArray.length)];

      // substracting 1 because we know that these are indexes - counted from zero \
      mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] = mFoodType;
//        mCharsArrayList.get(mFoodPositionRow - 1)[mFoodPositionSymbol - 1] = FOOD;
   }

   private void updateMainField() {

      StringBuilder newStringToSet = new StringBuilder();
      for (int i = 0; i < mFieldLinesCount; i++) {
         newStringToSet.append(mCharsArrayList.get(i));
      }
      mtvMainField.setText(newStringToSet);
   }

   private void saveNewBestResults() {
      getSharedPreferences(MyPSF.S_P_NAME, MODE_PRIVATE)
               .edit()
               .clear()
               .putInt(MyPSF.KEY_SCORE, mBestScore)
               .putLong(MyPSF.KEY_TIME, mBestTime)
               .commit();
   }

   private boolean showScoresDialog() {

      actionPauseGame();

      // preparing view for the dialog \
      @SuppressLint("InflateParams")
      View dialogView = getLayoutInflater().inflate(R.layout.scores, null);

      // setting all elements for this view \
      MyTextView mtvCurrentScore = (MyTextView) dialogView.findViewById(R.id.mtvCurrentScore);
      MyTextView mtvCurrentTime = (MyTextView) dialogView.findViewById(R.id.mtvCurrentTime);
      final MyTextView mtvBestScore = (MyTextView) dialogView.findViewById(R.id.mtvBestScore);
      final MyTextView mtvBestTime = (MyTextView) dialogView.findViewById(R.id.mtvBestTime);

      mtvCurrentScore.setText(String.valueOf(mCurrentScore));
      mtvCurrentTime.setText(DateFormat.format("mm:ss", mCurrentTime));
      mtvBestScore.setText(String.valueOf(mBestScore));
      mtvBestTime.setText(DateFormat.format("mm:ss", mBestTime));

      final MyTextView mtvClearBestResults = (MyTextView) dialogView.findViewById(R.id.mtvClearBestResults);
      final LinearLayout llHidden = (LinearLayout) dialogView.findViewById(R.id.llHidden);
      final boolean[] longButtonClickedOnce = {false};

      // preparing special listener for two hidden buttons \
      final View.OnClickListener hiddenViewClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (v.getId() == R.id.mtvYes) { // other button just hides this view again \
               mBestScore = 0;
               mBestTime = 0;
               mtvBestScore.setText(String.valueOf(mBestScore));
               mtvBestTime.setText(DateFormat.format("mm:ss", mBestTime));
               saveNewBestResults();
            }
            llHidden.setVisibility(View.GONE); // click at NO-button is done by this line \
            longButtonClickedOnce[0] = false;
         }
      };

      mtvClearBestResults.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            llHidden.setVisibility(View.VISIBLE);
            llHidden.findViewById(R.id.mtvNo).setOnClickListener(hiddenViewClickListener);
            llHidden.findViewById(R.id.mtvYes).setOnClickListener(hiddenViewClickListener);
            if (longButtonClickedOnce[0]) llHidden.setVisibility(View.GONE);
            longButtonClickedOnce[0] = !longButtonClickedOnce[0];
         }
      });

      // preparing builder for the dialog \
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setView(dialogView);

      // building and showing the dialog itself \
      AlertDialog alertDialog = builder.create();
      alertDialog.show();

      return true;
   } // end of showScoresDialog-method \\

   private boolean showSetSpeedDialog() {

      actionPauseGame();

      // preparing view for the dialog \
      @SuppressLint("InflateParams")
      View dialogView = getLayoutInflater().inflate(R.layout.speed, null);

      // preparing builder for the dialog \
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setView(dialogView);

      // building and showing the dialog itself \
      final AlertDialog alertDialog = builder.create();
      alertDialog.show();

      // setting all elements for this view \
      RadioGroup rgSpeed = (RadioGroup) dialogView.findViewById(R.id.rgSpeed);

      int[] radioButtons = {
               R.id.mrb1,
               R.id.mrb2,
               R.id.mrb3,
               R.id.mrb4,
               R.id.mrb5,
               R.id.mrb6,
               R.id.mrb7,
               R.id.mrb8,
               R.id.mrb9
      };
      try {
         rgSpeed.check(radioButtons[mSnakeSpeed - 1]);
      } catch (ArrayIndexOutOfBoundsException aioobe) {
         MyLog.i("ArrayIndexOutOfBoundsException: mSnakeSpeed = " + mSnakeSpeed);
      }
      rgSpeed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
            int newSnakeSpeed = 0;
            // pretty nice incrementation - without breaks! \
            switch (checkedId) {
               case R.id.mrb9:
                  newSnakeSpeed++;
               case R.id.mrb8:
                  newSnakeSpeed++;
               case R.id.mrb7:
                  newSnakeSpeed++;
               case R.id.mrb6:
                  newSnakeSpeed++;
               case R.id.mrb5:
                  newSnakeSpeed++;
               case R.id.mrb4:
                  newSnakeSpeed++;
               case R.id.mrb3:
                  newSnakeSpeed++;
               case R.id.mrb2:
                  newSnakeSpeed++;
               case R.id.mrb1:
                  newSnakeSpeed++;
            }
            MyLog.i("newSnakeSpeed = " + newSnakeSpeed);
            mSnakeSpeed = newSnakeSpeed;

            alertDialog.dismiss();
         }
      });

      return true;
   } // end of showSetSpeedDialog-method \\

   private void actionPauseGame() {

      mGamePausedSwitch = true;

      if (mTimer != null) {
         mTimer.cancel();
         mTimer = null;
      }
      mtvMainField.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_light));
   }

   private void actionStartGame() {

      mGamePausedSwitch = false;
      mGameEnded = false;

      int startTextColor = ContextCompat.getColor(getApplicationContext(), android.R.color.white);
      mtvMainField.setTextColor(startTextColor);
      mtvMainField.setBackgroundResource(R.color.primary_dark);
      // launching everything \
      int delay; // amount of time for game to wait = realization of speed \
      try {
         delay = 500 / mSnakeSpeed;
      } catch (ArithmeticException ae) {
         MyLog.i("ArithmeticException with delay = 500 / 0: mSnakeSpeed = 0");
         actionEndGame();
         return;
      }
      mTimer = new Timer();
      mTimer.schedule(new SnakeMoveTimerTask(), 0, delay);
      mTimer.schedule(new TimeUpdateTimerTask(), 0, 1000);
//                    mTimer.schedule(new TimeUpdateTimerTask(), 0, 1); // requirements of dev-challenge \
      mTimer.schedule(new FoodUpdateTimerTask(),
               MyPSF.STARTING_UPDATE_FOOD_PERIOD,
               MyPSF.STARTING_UPDATE_FOOD_PERIOD);
   }

   private void actionEndGame() {

      mGameEnded = true;
      mGamePausedSwitch = true;

      // stopping all timers \
      if (mTimer != null) {
         mTimer.cancel();
         mTimer.purge();
         mTimer = null;
      }

      // checking if current result is the best \
      if (mCurrentScore > mBestScore || mCurrentTime > mBestTime) {
         if (mCurrentScore > mBestScore)
            mBestScore = mCurrentScore;
         if (mCurrentTime > mBestTime)
            mBestTime = mCurrentTime;
         saveNewBestResults();
      }

      // resetting the start-stop button to its primary state \
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            int endTextColor = ContextCompat.getColor(MainActivity.this, android.R.color.primary_text_light);
            mtvMainField.setTextColor(endTextColor);
            mtvMainField.setBackgroundResource(R.color.primary_light);
         }
      });
      MyLog.i("game ended with mCurrentScore " + mCurrentScore);
   }

// MOVEMENT ========================================================================================

   public enum MyDirections {RIGHT, UP, LEFT, DOWN}

   public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

      MyDirections prohibitedDirection;

      @Override
      public boolean onDoubleTap(MotionEvent e) {
         MyLog.i("onDoubleTap - start / pause");
         MyLog.i("mGamePausedSwitch = " + mGamePausedSwitch);
         MyLog.i("mGameEnded = " + mGameEnded);

         // 1 - prepare new field after game over \
         if (mGameEnded) prepareGameIn4Steps();

         // 2 - pause or continue game \
         if (!mGamePausedSwitch)
            actionPauseGame();
         else actionStartGame();

         return super.onDoubleTap(e);
      }

      @Override
      public void onLongPress(MotionEvent e) {
         MyLog.i("onLongPress - full restart from zero");
         MyLog.i("mGamePausedSwitch = " + mGamePausedSwitch);
         MyLog.i("mGameEnded = " + mGameEnded);

         mVibrator.vibrate(MyPSF.SHORT_VIBRATION);

         actionEndGame();
         mCurrentScore = 0;

         // 1 - preparing the field for the new game \
         prepareGameIn4Steps();

         // 2 - restarting the game \
         actionStartGame();

         super.onLongPress(e);
      }

      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

         // detecting current direction for desired move \
         if (Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY()))
            if (isLeftMove(velocityX, velocityY)) {
               MyLog.i("turned left");
               mSnakeDirection = MyDirections.LEFT;
            } else {
               MyLog.i("turned right");
               mSnakeDirection = MyDirections.RIGHT;
            }
         else { // moving along Y-axis was more noticeable than along X-axis \
            if (isUpMove(velocityX, velocityY)) {
               MyLog.i("turned up");
               mSnakeDirection = MyDirections.UP;
            } else {
               MyLog.i("turned down");
               mSnakeDirection = MyDirections.DOWN;
            }
         }

         // checking if direction is not reversed as before \
         if (mSnakeDirection == prohibitedDirection)
            mSnakeDirection = null;
         else
            // saving current last direction conditions for the next swipe \
            switch (mSnakeDirection) {
               case LEFT:
                  prohibitedDirection = MyDirections.RIGHT;
                  break;
               case RIGHT:
                  prohibitedDirection = MyDirections.LEFT;
                  break;
               case UP:
                  prohibitedDirection = MyDirections.DOWN;
                  break;
               case DOWN:
                  prohibitedDirection = MyDirections.UP;
                  break;
            }

         return super.onFling(e1, e2, velocityX, velocityY);
      }

      private boolean isLeftMove(float velocityX, float velocityY) {
         return velocityX < 0 // determining the LEFT direction of the swipe \
//                    && absDeltaX > absDeltaY // assuring that this swipe was more along X-axis than along Y \
                  && Math.abs(velocityX) > Math.abs(velocityY); // checking if we're right completely \
      }

      private boolean isUpMove(float velocityX, float velocityY) {
         return velocityY < 0 // determining the UP direction of the swipe \
//                    && absDeltaX < absDeltaY // assuring that this swipe was more along Y-axis than along X \
                  && Math.abs(velocityX) < Math.abs(velocityY); // checking if we're right completely \
      }
   } // end of MyGestureListener-class \\

// TIMER CLASSES ===================================================================================

   // special class defined for repeating operations and usage of Timer \
   private class SnakeMoveTimerTask extends TimerTask {

      // this method handles movement of the mSnake \
      @Override
      public void run() {

         // is null when direction is prohibited - have to check it here to avoid crash \
         if (mSnakeDirection == null) return;

         // defining all reusable variables here \
         int newCellX = 0, newCellY = 0;
         Snake.SnakeCell snakeCell; // to use only one object for the whole mSnake \

         // 1 - saving information about the last cell position - before it will be freed in the end \
         snakeCell = mSnake.getCell(mSnake.getLength() - 1);
         int cellToFreeX = snakeCell.getIndexByX();
         int cellToFreeY = snakeCell.getIndexByY();

         // 2 - making single change to every cell in the model of mSnake - except the head \
         for (int i = mSnake.getLength() - 1; i > 0; i--) {
//            for (int i = 1; i < mSnake.getLength(); i++) {
            snakeCell = mSnake.getCell(i - 1); // to get the position of every previous cell
            // shifting 1 cell at a step \
            newCellX = snakeCell.getIndexByX();
            newCellY = snakeCell.getIndexByY();
            // moving every cell of the mSnake's body \
            snakeCell = mSnake.getCell(i); // to update current cell vith position of previous \
            snakeCell.setIndexByX(newCellX);
            snakeCell.setIndexByY(newCellY);
         }

         // 3 - finally moving mSnake's head to selected direction \
         snakeCell = mSnake.getCell(0); // for head of the mSnake
         switch (mSnakeDirection) {
            case RIGHT: // tail to right - head moves to left \
               newCellX = snakeCell.getIndexByX() + 1;
               newCellY = snakeCell.getIndexByY();
               break;
            case UP: // tail is set up - head moves down \
               newCellX = snakeCell.getIndexByX();
               newCellY = snakeCell.getIndexByY() - 1;
               break;
            case LEFT: // tail to left - head moves to right \
               newCellX = snakeCell.getIndexByX() - 1;
               newCellY = snakeCell.getIndexByY();
               break;
            case DOWN: // tail is set down - head moves up \
               newCellX = snakeCell.getIndexByX();
               newCellY = snakeCell.getIndexByY() + 1;
               break;
         }
         // saving info to the mSnake's model \
         snakeCell.setIndexByX(newCellX);
         snakeCell.setIndexByY(newCellY);
/*
            // here we have just finished to update mSnake's model \
*/
         // now it's obvious to update model for field with mSnake's new data and display this all \
         for (int i = 0; i < mSnake.getLength(); i++) {
            snakeCell = mSnake.getCell(i);
            newCellX = snakeCell.getIndexByX();
            newCellY = snakeCell.getIndexByY();

            // setting the mSnake body \
            mCharsArrayList.get(newCellY)[newCellX] = MyPSF.SNAKE;
            // recovering the field after the mSnake's tail \
            mCharsArrayList.get(cellToFreeY)[cellToFreeX] = MyPSF.SPACE;
         }
         // updating the field with new mSnake's position \
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               updateMainField();
               String scoreComplex = getString(R.string.score) + mCurrentScore;
               mtvScore.setText(scoreComplex);
            }
         });

         // exit conditions check is the last thing to do \
         if (collisionHappened()) actionEndGame(); // this is the only way out from loop \
         else mCurrentScore++;

         // now handling eating of food and bonuses - it's taken by the head only \
         if (isFoodFound()) eatFood();
      } // end of run-method \\

      // VERIFICATIONS ===========================================================================

      private boolean isFoodFound() {
         return mSnake.getCell(0).getIndexByY() == mFoodPositionRow
                  && mSnake.getCell(0).getIndexByX() == mFoodPositionSymbol;
      }

      private void eatFood() {
         // user must be happy with such a vibration :)
         mVibrator.vibrate(MyPSF.SHORT_VIBRATION);

         int cellPositionX, cellPositionY;
         Snake.SnakeCell currentCell;
         MyLog.i("eaten = " + mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol]);

         switch (mFoodType) {

            case MyPSF.LENGTH_PLUS: // length +1
                    /*
                    i decided to add a new cell at the mSnake 's head - because we know the direction \
                    new cell will get visible only at the end of the move \
                    right now i 'm only updating the model - not the view \
                    */
               cellPositionX = mFoodPositionSymbol + shiftAfterFood(mSnakeDirection, true);
               cellPositionY = mFoodPositionRow + shiftAfterFood(mSnakeDirection, false);
               currentCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
               mSnake.addCell(mSnake.getLength(), currentCell);
               mSnakeSpeed++;
               MyLog.i("LENGTH_PLUS taken!");
               mCurrentScore += 100;
               break;

            case MyPSF.LENGTH_MINUS: // length -1
               // just removing the last cell \
               if (mSnake.getLength() > 1) { // to avoid mSnake's dissappearing \
                  // first updating field to clear mSnake's tail - while it's available \
                  currentCell = mSnake.getCell(mSnake.getLength() - 1);
                  cellPositionX = currentCell.getIndexByX();
                  cellPositionY = currentCell.getIndexByY();
                  mCharsArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SPACE;
                  // now it is safe to update the model \
                  mSnake.removeCell(mSnake.getLength() - 1);
                  if (mSnakeSpeed > 1) mSnakeSpeed--;
               }
               MyLog.i("LENGTH_MINUS taken!");
               mCurrentScore -= 100;
               break;

            case MyPSF.SPEED_UP: // speed +1
               if (mSnakeSpeed < 9) {
                  mSnakeSpeed++;
                  mCurrentScore += 50;
               } else mCurrentScore += 150;
               MyLog.i("SPEED_UP taken!");
               break;

            case MyPSF.SPEED_SLOW: // speed -1
               if (mSnakeSpeed > 1) // to avoid falling after division by zero \
                  mSnakeSpeed--;
               MyLog.i("SPEED_SLOW taken!");
               mCurrentScore -= 50;
               break;
         }
      } // end of eatFood-method \

      private int shiftAfterFood(MyDirections direction, boolean isForX) {
         if (isForX)
            switch (direction) {
               case RIGHT: // to right - for X
                  return 1;
               case LEFT: // to left - for X
                  return -1;
               default: // up and dowm - 1, 3 cases don't affect X
                  return 0;
            }
         else
            switch (direction) {
               case UP: // up - for Y
                  return 1;
               case DOWN: // down - for Y
                  return -1;
               default: // to right and left - 0, 2 cases don't affect Y
                  return 0;
            }
      }

      private boolean collisionHappened() {
         // we have only to check what happens to the mSnake's head - other cells are inactive \
         int snakeHeadY = mSnake.getCell(0).getIndexByY();
         int snakeHeadX = mSnake.getCell(0).getIndexByX();

         if (mSnake.getLength() <= 4) { // mSnake with less length cannot collide with itself \
            return touchedBounds(snakeHeadY, snakeHeadX);
         } else
            return touchedBounds(snakeHeadY, snakeHeadX) || touchedItself(snakeHeadY, snakeHeadX);
      }

      // connected to collisionHappened-method \
      private boolean touchedBounds(int snakeHeadY, int snakeHeadX) {
         // we can avoid loop here assuming that mSnake's head has index of 0 \
         return snakeHeadY == 0 || snakeHeadY == mFieldLinesCount - 1 ||
                  snakeHeadX == 0 || snakeHeadX == mSymbolsInFieldLine - 1;
      }

      // connected to collisionHappened-method \
      private boolean touchedItself(int snakeHeadY, int snakeHeadX) {
         // mSnake can collide with itself beginning only from fifth element = fourth index \
         for (int i = 4; i < mSnake.getLength(); i++) {
            Snake.SnakeCell snakeCell = mSnake.getCell(i);
            if (snakeHeadY == snakeCell.getIndexByY() && snakeHeadX == snakeCell.getIndexByX()) {
               MyLog.i("touchedItself");
               return true;
            }
         }
         return false;
      }
   } // end of SnakeMoveTimerTask-class \\

   private class FoodUpdateTimerTask extends TimerTask {

      @Override
      public void run() {
         updateFood();
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               updateMainField();
            }
         });
         MyLog.i("mFoodType updated");
      }
   }

   private class TimeUpdateTimerTask extends TimerTask {
      // code here is called every milisecond - it has to be really fast \
      private long initialSystemTime = System.currentTimeMillis();

      @Override
      public void run() {
         mCurrentTime = System.currentTimeMillis() - initialSystemTime;

         final String stringToSet = getString(R.string.time)
                  + DateFormat.format("mm:ss", mCurrentTime);
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               mtvTime.setText(stringToSet);
            }
         });
      } // end of run-method \\
   } // end of TimeUpdateTimerTask-class \\
}