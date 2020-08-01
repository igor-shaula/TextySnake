package com.igor_shaula.texty_snake.v1.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.igor_shaula.texty_snake.v1.logic.GameLogic;

public final class MainViewModel extends ViewModel {

    private GameLogic logic;

    public void initGameLogic(@NonNull MainActivity ui) {
        logic = new GameLogic(ui);
    }

    public void destroyGameLogic() {
        logic.clearUiLink();
        logic = null;
    }

}