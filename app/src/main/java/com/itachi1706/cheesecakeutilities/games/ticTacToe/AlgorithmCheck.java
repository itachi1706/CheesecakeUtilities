package com.itachi1706.cheesecakeutilities.games.ticTacToe;

import com.itachi1706.helperlib.helpers.LogHelper;

/**
 * Created by Kenneth on 19/5/2015
 * for CheesecakeUtilities in package com.itachi1706.cheesecakeutilities.games.TicTacToe
 */
class AlgorithmCheck {

    private static String[][] gameBoardCheck = null;

    static boolean hasDrawn(int value){ return value >= 9; }

    static boolean hasWon(String value, String[][] gameBoard){
        boolean winValue = false;
        gameBoardCheck = gameBoard;
        if (wonVert(value) || wonHorizontal(value) || wonDiagonal(value))
            winValue = true;
        LogHelper.i("WIN-Check", "Has Won Value: " + winValue);
        return winValue;
    }

    private static boolean wonVert(String value)
    {
        for (int col = 0; col < gameBoardCheck[0].length; col++)
        {
            if (gameBoardCheck[0][col].equals(value) && gameBoardCheck[0][col].equals(gameBoardCheck[1][col])
            && gameBoardCheck[1][col].equals(gameBoardCheck[2][col]))
            return true;
        }
        return false;
    }

    private static boolean wonHorizontal(String value)
    {
        for (String[] aGameBoardCheck : gameBoardCheck) {
            if (aGameBoardCheck[0].equals(value) && aGameBoardCheck[0].equals(aGameBoardCheck[1])
                    && aGameBoardCheck[2].equals(aGameBoardCheck[1]))
                return true;
        }
        return false;
    }

    private static boolean wonDiagonal(String value)
    {
        return ((gameBoardCheck[0][0].equals(gameBoardCheck[1][1]) && gameBoardCheck[1][1].equals(gameBoardCheck[2][2]))
                || (gameBoardCheck[0][2].equals(gameBoardCheck[1][1]) && gameBoardCheck[1][1].equals(gameBoardCheck[2][0])))
                && gameBoardCheck[1][1].equals(value);
    }
}
