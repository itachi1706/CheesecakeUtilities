package com.itachi1706.cheesecakeutilities.Games.TicTacToe;

import android.util.Log;

/**
 * Created by Kenneth on 19/5/2015
 * for CheesecakeUtilities in package com.itachi1706.cheesecakeutilities.Games.TicTacToe
 */
class LoggingClass {

    static void logInfo(String title, String message)
    {
        Log.i(title, message);
    }

    static void logError(String title, String message)
    {
        Log.e(title, message);
    }
}
