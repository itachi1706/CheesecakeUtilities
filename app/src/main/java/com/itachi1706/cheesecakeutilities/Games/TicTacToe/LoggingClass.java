package com.itachi1706.cheesecakeutilities.Games.TicTacToe;

import com.itachi1706.cheesecakeutilities.Util.LogHelper;

/**
 * Created by Kenneth on 19/5/2015
 * for CheesecakeUtilities in package com.itachi1706.cheesecakeutilities.Games.TicTacToe
 * @deprecated Use {@link LogHelper} instead
 */
@Deprecated
class LoggingClass {

    static void logInfo(String title, String message)
    {
        LogHelper.i(title, message);
    }

    static void logError(String title, String message)
    {
        LogHelper.e(title, message);
    }
}
