package com.itachi1706.cheesecakeutilities.games.ticTacToe;

import android.app.AlertDialog;
import android.content.Context;

import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.util.Random;

/**
 * Created by Kenneth on 19/5/2015
 * for CheesecakeUtilities in package com.itachi1706.cheesecakeutilities.games.TicTacToe
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
            LogHelper.i("AI", "First Move Execute");
            firstMove(gameBoard, lastRow, lastCol, context);
        }
        else if (turn > 8)
        {
            LogHelper.i("AI", "Game Ended");
        }
        else
        {
            LogHelper.i("AI", "Check Win");
            //See if AI can win
            if (checkForPossibleAIWinOrBlock(gameBoard, TicTacToeValues.O))
                return;
            LogHelper.i("AI", "Check Block");
            //Check if I can block
            if (checkForPossibleAIWinOrBlock(gameBoard, TicTacToeValues.X))
                return;
            //Randomize
            LogHelper.i("AI", "Randomize");
            randomlyChuckValue(context);
        }
    }

    private static void throwError(String errorMsg, Context context)
    {
        new AlertDialog.Builder(context).setTitle("ERROR")
                .setMessage(errorMsg)
                .setPositiveButton(android.R.string.ok, null).show();
        LogHelper.e("ERROR", errorMsg);
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
        boolean move = true;

        // Check Rows
        if (checkMatch(0, 1, moveVal) && isValidMove(2)) makeMove(2);
        else if (checkMatch(0, 2, moveVal) && isValidMove(1)) makeMove(1);
        else if (checkMatch(1, 2, moveVal) && isValidMove(0)) makeMove(0);
        else if (checkMatch(3, 4, moveVal) && isValidMove(5)) makeMove(5);
        else if (checkMatch(3, 5, moveVal) && isValidMove(4)) makeMove(4);
        else if (checkMatch(4, 5, moveVal) && isValidMove(3)) makeMove(3);
        else if (checkMatch(6, 7, moveVal) && isValidMove(8)) makeMove(8);
        else if (checkMatch(6, 8, moveVal) && isValidMove(7)) makeMove(7);
        else if (checkMatch(7, 8, moveVal) && isValidMove(6)) makeMove(6);

        // Check Columns
        else if (checkMatch(0, 3, moveVal) && isValidMove(6)) makeMove(6);
        else if (checkMatch(0, 6, moveVal) && isValidMove(3)) makeMove(3);
        else if (checkMatch(3, 6, moveVal) && isValidMove(0)) makeMove(0);
        else if (checkMatch(1, 4, moveVal) && isValidMove(7)) makeMove(7);
        else if (checkMatch(1, 7, moveVal) && isValidMove(4)) makeMove(4);
        else if (checkMatch(4, 7, moveVal) && isValidMove(1)) makeMove(1);
        else if (checkMatch(2, 5, moveVal) && isValidMove(8)) makeMove(8);
        else if (checkMatch(2, 8, moveVal) && isValidMove(5)) makeMove(5);
        else if (checkMatch(5, 8, moveVal) && isValidMove(2)) makeMove(2);

        // Check Diagonal
        else if (checkMatch(0, 4, moveVal) && isValidMove(8)) makeMove(8);
        else if (checkMatch(0, 8, moveVal) && isValidMove(4)) makeMove(4);
        else if (checkMatch(4, 8, moveVal) && isValidMove(0)) makeMove(0);
        else if (checkMatch(2, 4, moveVal) && isValidMove(6)) makeMove(6);
        else if (checkMatch(2, 6, moveVal) && isValidMove(4)) makeMove(4);
        else if (checkMatch(4, 6, moveVal) && isValidMove(2)) makeMove(2);
        else move = false;

        // Cannot Win/Block
        return move;
    }

    private static Boolean checkMatch(int gameMove1, int gameMove2, String value)
    {
        if (getValueAtSlot(tmpGameBoard, gameMove1).equals(getValueAtSlot(tmpGameBoard, gameMove2)))
            return getValueAtSlot(tmpGameBoard, gameMove1).equals(value);
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
        int num;
        switch (Integer.toString(lastRow) + Integer.toString(lastCol)) {
            case "00": num = 0; break;
            case "01": num = 1; break;
            case "02": num = 2; break;
            case "10": num = 3; break;
            case "11": num = 4; break;
            case "12": num = 5; break;
            case "20": num = 6; break;
            case "21": num = 7; break;
            case "22": num = 8; break;
            default: num = -1; break;
        }
        return num;
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

    private static int getRow(int nextMove) {
        return (nextMove / 3);
    }

    private static int getCol(int nextMove) {
        return (nextMove % 3);
    }


    //Make a move
    private static void makeMove(int nextMove) {
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
            default: throw new UnsupportedOperationException("Invalid Move");
        }
    }
}
