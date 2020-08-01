package com.igor_shaula.texty_snake.v1.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.igor_shaula.texty_snake.v1.R;
import com.igor_shaula.texty_snake.v1.custom_views.MyTextView;
import com.igor_shaula.texty_snake.v1.databinding.ActivityMainBinding;
import com.igor_shaula.texty_snake.v1.logic.GameLogic;
import com.igor_shaula.texty_snake.v1.utils.L;
import com.igor_shaula.texty_snake.v1.utils.MyPSF;

/**
 * Created by igor_shaula texty_snake - main class holding others to avoid too many code lines \
 */
public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private GameLogic logic;
    private ActivityMainBinding viewBinding;

    // utils from the system \
    private Vibrator vibratorService;

    private GestureDetector gestureDetector;

    // LIFECYCLE ===================================================================================

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        setSupportActionBar(viewBinding.myToolbar);

        viewBinding.myToolbar.setContentInsetsAbsolute(0, 0);

        logic = new GameLogic(this);

        viewBinding.mtvShowScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.onShowScoresClick();
            }
        });

        viewBinding.mtvSetSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.onSetSpeedClick();
            }
        });

        viewBinding.mtvUserGuide.setMovementMethod(new ScrollingMovementMethod());
        viewBinding.mtvUserGuide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                logic.onUserGuideLongClick();
                return false;
            }
        });

        // vibrator will be used when eating food or something else happens \
        vibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // now setting the top - gesture sensitive interface \
        gestureDetector = new GestureDetector(this, new MyGestureListener());

        // reading previous achievements from SP \
        final SharedPreferences sharedPreferences = getSharedPreferences(MyPSF.S_P_NAME, MODE_PRIVATE);
        logic.setBestScore(sharedPreferences.getInt(MyPSF.KEY_SCORE, 0));
        logic.setBestTime(sharedPreferences.getLong(MyPSF.KEY_TIME, 0));

        final ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
    } // onCreate \\

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        L.l("onTouchEvent");
        return gestureDetector.onTouchEvent(motionEvent);
    }

    // SAVE_RESTORE ================================================================================

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        L.i("onSaveInstanceState worked");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        L.i("onRestoreInstanceState worked");
    }

    // MAIN SEQUENCE ===============================================================================

    // TODO: 31.07.2020 simplify this monstrous construction
    public void measureAvailableSpace() {
        viewBinding.mtvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        // getting current screen size in pixels \
                        logic.setFieldPixelWidth(viewBinding.mtvMainField.getWidth());
                        logic.setFieldPixelHeight(viewBinding.flMain.getHeight());

                        // TODO: 31.07.2020 investigate if it's safe to remove the following block
                        final ViewTreeObserver.OnGlobalLayoutListener listener = this;
                        // now removing the listener - it's not needed any more \
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            viewBinding.mtvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
                        else
                            viewBinding.mtvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(listener);

                        // here we get pixel width of a single symbol - initial TextView has only one symbol \
                        viewBinding.mtvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int measuredSymbolWidth = viewBinding.mtvMainField.getMeasuredWidth();
                        L.i("measuredSymbolWidth " + measuredSymbolWidth);

                        // getting our first valuable parameter - the size of main array \
                        logic.detectSymbolsInFieldLine(measuredSymbolWidth);
//                        logic.setSymbolsInFieldLine(mFieldPixelWidth / measuredSymbolWidth);

                        logic.prepareGameIn4Steps();
                        logic.actionStartGame();
                    }
                });
    } // measureAvailableSpace \\

    public void showScoresDialog() {

        logic.actionPauseGame();

        // preparing view for the dialog \
        final View dialogView = getLayoutInflater().inflate(R.layout.scores, null);

        // setting all elements for this view \
        final MyTextView mtvCurrentScore = dialogView.findViewById(R.id.mtvCurrentScore);
        final MyTextView mtvCurrentTime = dialogView.findViewById(R.id.mtvCurrentTime);
        final MyTextView mtvBestScore = dialogView.findViewById(R.id.mtvBestScore);
        final MyTextView mtvBestTime = dialogView.findViewById(R.id.mtvBestTime);

        mtvCurrentScore.setText(String.valueOf(logic.getCurrentScore()));
        mtvCurrentTime.setText(DateFormat.format("mm:ss", logic.getCurrentTime()));
        mtvBestScore.setText(String.valueOf(logic.getBestScore()));
        mtvBestTime.setText(DateFormat.format("mm:ss", logic.getBestTime()));

        final MyTextView mtvClearBestResults = dialogView.findViewById(R.id.mtvClearBestResults);
        final LinearLayout llHidden = dialogView.findViewById(R.id.llHidden);
        final boolean[] longButtonClickedOnce = {false};

        // preparing special listener for two hidden buttons \
        final View.OnClickListener hiddenViewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.mtvYes) { // other button just hides this view again \
                    logic.setBestScore(0);
                    logic.setBestTime(0);
                    mtvBestScore.setText(String.valueOf(logic.getBestScore()));
                    mtvBestTime.setText(DateFormat.format("mm:ss", logic.getBestTime()));
                    saveNewBestResults(0, 0);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        // building and showing the dialog itself \
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    } // showScoresDialog \\

    public void showSetSpeedDialog() {

        logic.actionPauseGame();

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
        RadioGroup rgSpeed = dialogView.findViewById(R.id.rgSpeed);

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
            rgSpeed.check(radioButtons[logic.getSnakeSpeed() - 1]);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            L.i("ArrayIndexOutOfBoundsException: mSnakeSpeed = " + logic.getSnakeSpeed());
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
                L.i("newSnakeSpeed = " + newSnakeSpeed);
                logic.setSnakeSpeed(newSnakeSpeed);

                alertDialog.dismiss();
            }
        });
    } // showSetSpeedDialog \\

    public void changeTextFields() {
        // hiding the readme - now it's useless \
        viewBinding.mtvUserGuide.setVisibility(View.GONE);
        // revealing the main field to get it ready for playing \
        viewBinding.flMain.setVisibility(View.VISIBLE);
    }

    public void setMainFieldText(@Nullable String text) {
        viewBinding.mtvMainField.setText(text);
    }

    public void setMainFieldTextSquareSymbols() {
        viewBinding.mtvMainField.setSquareSymbols();
    }

    @NonNull
    public String getMainFieldText() {
        return viewBinding.mtvMainField.getText().toString();
    }

    public void measureMainField() {
        viewBinding.mtvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    }

    public int getMainFieldHeight() {
        return viewBinding.mtvMainField.getMeasuredHeight();
    }

    public void setMainFieldTextColor(int color) {
        viewBinding.mtvMainField.setTextColor(color);
    }

    public void setMFTBackgroundResource(int colorId) {
        viewBinding.mtvMainField.setBackgroundResource(colorId);
    }

    public void setScoreText(@NonNull String scoreComplex) {
        viewBinding.mtvScore.setText(scoreComplex);
    }

    public void setTimeText(@NonNull String stringToSet) {
        viewBinding.mtvTime.setText(stringToSet);
    }

    public void vibrate() {
        vibratorService.vibrate(MyPSF.SHORT_VIBRATION);
    }

    public void saveNewBestResults(int bestScore, long bestTime) {
        getSharedPreferences(MyPSF.S_P_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putInt(MyPSF.KEY_SCORE, bestScore)
                .putLong(MyPSF.KEY_TIME, bestTime)
                .apply();
    }

    // MOVEMENT ====================================================================================

    public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            logic.onDoubleTap();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            logic.onLongPress();
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            logic.onFling(e1, e2, velocityX, velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    } // MyGestureListener \\
}