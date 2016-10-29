package com.itachi1706.cheesecakeutilities.Games.TicTacToe;

import android.app.AlertDialog;
import android.content.Context;

import java.util.Random;

/**
 * Created by Kenneth on 19/5/2015
 * for CheesecakeUtilities in package com.itachi1706.cheesecakeutilities.Games.TicTacToe
 */
class ComputerAI {
    //AI IS ALWAYS O

    /**
     * Gameboard
     * 0 1 2
     * 3 4 5
     * 6 7 8
     */

    private static String[][] tmpGameBoard;

    static void determineNextMove(String[][] gameBoard, int lastRow, int lastCol, int turn, Context context)
    {
        //Check if first turn and is middle
        if (turn == 1)
        {
            LoggingClass.logInfo("AI", "First Move Execute");
            firstMove(gameBoard, lastRow, lastCol, context);
        }
        else if (turn > 8)
        {
            LoggingClass.logInfo("AI", "Game Ended");
        }
        else
        {
            LoggingClass.logInfo("AI", "Check Win");
            //See if AI can win
            if (checkForPossibleAIWinOrBlock(gameBoard, TicTacToeValues.O))
                return;
            LoggingClass.logInfo("AI", "Check Block");
            //Check if I can block
            if (checkForPossibleAIWinOrBlock(gameBoard, TicTacToeValues.X))
                return;
            //Randomize
            LoggingClass.logInfo("AI", "Randomize");
            randomlyChuckValue(context);
        }
    }

    private static void throwError(String errorMsg, Context context)
    {
        new AlertDialog.Builder(context).setTitle("ERROR")
                .setMessage(errorMsg)
                .setPositiveButton(android.R.string.ok, null).show();
        LoggingClass.logError("ERROR", errorMsg);
    }

    @SuppressWarnings("ConstantConditions")
    private static void randomlyChuckValue(Context context)
    {
        Random random = new Random();
        int randomMove, count = 0;
        while (true)
        {
            randomMove = random.nextInt(9);
            if (isValidMove(randomMove))
            {
                makeMove(randomMove);
                return;
            }
            if (count > 1000){
                throwError("Random Moves unable to generate. Either the RNG gods are against you"
                        + ", or there is a bug you need to report to the dev.", context);
            }
        }
    }

    private static Boolean checkForPossibleAIWinOrBlock(String[][] gameBoard, String moveVal)
    {
        tmpGameBoard = gameBoard;

        // Check Rows
        if (checkMatch(0, 1, moveVal) && isValidMove(2)) { makeMove(2); return true; }
        if (checkMatch(0, 2, moveVal) && isValidMove(1)) { makeMove(1); return true; }
        if (checkMatch(1, 2, moveVal) && isValidMove(0)) { makeMove(0); return true; }
        if (checkMatch(3, 4, moveVal) && isValidMove(5)) { makeMove(5); return true; }
        if (checkMatch(3, 5, moveVal) && isValidMove(4)) { makeMove(4); return true; }
        if (checkMatch(4, 5, moveVal) && isValidMove(3)) { makeMove(3); return true; }
        if (checkMatch(6, 7, moveVal) && isValidMove(8)) { makeMove(8); return true; }
        if (checkMatch(6, 8, moveVal) && isValidMove(7)) { makeMove(7); return true; }
        if (checkMatch(7, 8, moveVal) && isValidMove(6)) { makeMove(6); return true; }

        // Check Columns
        if (checkMatch(0, 3, moveVal) && isValidMove(6)) { makeMove(6); return true; }
        if (checkMatch(0, 6, moveVal) && isValidMove(3)) { makeMove(3); return true; }
        if (checkMatch(3, 6, moveVal) && isValidMove(0)) { makeMove(0); return true; }
        if (checkMatch(1, 4, moveVal) && isValidMove(7)) { makeMove(7); return true; }
        if (checkMatch(1, 7, moveVal) && isValidMove(4)) { makeMove(4); return true; }
        if (checkMatch(4, 7, moveVal) && isValidMove(1)) { makeMove(1); return true; }
        if (checkMatch(2, 5, moveVal) && isValidMove(8)) { makeMove(8); return true; }
        if (checkMatch(2, 8, moveVal) && isValidMove(5)) { makeMove(5); return true; }
        if (checkMatch(5, 8, moveVal) && isValidMove(2)) { makeMove(2); return true; }

        // Check Diagonal
        if (checkMatch(0, 4, moveVal) && isValidMove(8)) { makeMove(8); return true; }
        if (checkMatch(0, 8, moveVal) && isValidMove(4)) { makeMove(4); return true; }
        if (checkMatch(4, 8, moveVal) && isValidMove(0)) { makeMove(0); return true; }
        if (checkMatch(2, 4, moveVal) && isValidMove(6)) { makeMove(6); return true; }
        if (checkMatch(2, 6, moveVal) && isValidMove(4)) { makeMove(4); return true; }
        if (checkMatch(4, 6, moveVal) && isValidMove(2)) { makeMove(2); return true; }

        // Cannot Win/Block
        return false;
    }

    private static Boolean checkMatch(int gameMove1, int gameMove2, String value)
    {
        if (getValueAtSlot(tmpGameBoard, gameMove1).equals(getValueAtSlot(tmpGameBoard, gameMove2)))
        {
            if (getValueAtSlot(tmpGameBoard, gameMove1).equals(value))
                return true;
        }
        return false;
    }

    private static String getValueAtSlot(String[][] gameBoard, int gameMove)
    {
        int row = getRow(gameMove);
        int col = getCol(gameMove);
        return gameBoard[row][col];
    }

    @SuppressWarnings("unused")
    private static String getValueAtSlot(String[][] gameBoard, int row, int col)
    {
        return gameBoard[row][col];
    }

    private static void firstMove(String[][] gameBoard, int lastRow, int lastCol, Context context)
    {
        //Get Current Move
        int moveMade = getGameBoardNum(lastRow, lastCol);
        //Check if middle
        if (moveMade == 4 && gameBoard[1][1].equals(TicTacToeValues.X))
        {
            while (true)
            {
                //Randomly select a corner
                Random random = new Random();
                int nextMove = random.nextInt(9);
                if ((nextMove == 0 || nextMove == 2 || nextMove == 6 || nextMove == 8)
                        && isValidMove(gameBoard, nextMove))
                {
                    makeMove(nextMove);
                    return;
                }
            }
        }

        //Check corner
        if ((moveMade == 0 || moveMade == 2 || moveMade == 6 || moveMade == 8)
                && gameBoard[lastRow][lastCol].equals(TicTacToeValues.X))
        {
            //Select center
            if (isValidMove(gameBoard, 4))
            {
                makeMove(4);
                return;
            }

        }

        //Check side
        if ((moveMade == 1 || moveMade == 3 || moveMade == 5 || moveMade == 7)
                && gameBoard[lastRow][lastCol].equals(TicTacToeValues.X))
        {
            //Select other side
            int plannedMove = -1;
            switch (moveMade){
                case 1: plannedMove = 7; break;
                case 3: plannedMove = 5; break;
                case 5: plannedMove = 3; break;
                case 7: plannedMove = 1; break;
            }

            if (isValidMove(gameBoard, plannedMove))
            {
                makeMove(plannedMove);
                return;
            }
        }

        //Error
        throwError("An error occured trying to make first move. Please contact dev.", context);
    }

    private static int getGameBoardNum(int lastRow, int lastCol)
    {
        switch (lastRow)
        {
            case 0:
                switch (lastCol)
                {
                    case 0: return 0;
                    case 1: return 1;
                    case 2: return 2;
                }
                break;
            case 1:
                switch (lastCol)
                {
                    case 0: return 3;
                    case 1: return 4;
                    case 2: return 5;
                }
                break;
            case 2:
                switch (lastCol)
                {
                    case 0: return 6;
                    case 1: return 7;
                    case 2: return 8;
                }
                break;
        }
        return -1;
    }

    private static Boolean isValidMove(int nextMove) {
        int row = getRow(nextMove);
        int col = getCol(nextMove);
        return !(row == -1 || col == -1) && tmpGameBoard[row][col].equals(TicTacToeValues.EMPTY);
    }

    private static Boolean isValidMove(String[][] gameBoard, int nextMove) {
        int row = getRow(nextMove);
        int col = getCol(nextMove);
        return !(row == -1 || col == -1) && gameBoard[row][col].equals(TicTacToeValues.EMPTY);
    }

    private static int getRow(int nextMove)
    {
        switch (nextMove)
        {
            case 0:
            case 1:
            case 2: return 0;
            case 3:
            case 4:
            case 5: return 1;
            case 6:
            case 7:
            case 8: return 2;
        }
        return -1;
    }

    private static int getCol(int nextMove)
    {
        switch (nextMove)
        {
            case 0:
            case 3:
            case 6: return 0;
            case 1:
            case 4:
            case 7: return 1;
            case 2:
            case 5:
            case 8: return 2;
        }
        return -1;
    }


    //Make a move
    private static void makeMove(int nextMove)
    {
        switch (nextMove)
        {
            case 0: TicTacToeActivity.updateAIMove(0, 0); break;
            case 1: TicTacToeActivity.updateAIMove(0, 1); break;
            case 2: TicTacToeActivity.updateAIMove(0, 2); break;
            case 3: TicTacToeActivity.updateAIMove(1, 0); break;
            case 4: TicTacToeActivity.updateAIMove(1, 1); break;
            case 5: TicTacToeActivity.updateAIMove(1, 2); break;
            case 6: TicTacToeActivity.updateAIMove(2, 0); break;
            case 7: TicTacToeActivity.updateAIMove(2, 1); break;
            case 8: TicTacToeActivity.updateAIMove(2, 2); break;
        }
    }
}
