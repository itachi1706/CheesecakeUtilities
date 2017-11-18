package com.itachi1706.cheesecakeutilities.Games.TicTacToe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Random;

public class TicTacToeActivity extends BaseActivity implements Button.OnClickListener {

    //Init Stuff
    private Button r11,r12,r13,r21,r22,r23,r31,r32,r33,btnStart,btnReset;
    private TextView instructions, counter;
    private RadioButton rbSP;
    private RadioGroup grpGamePlay;
    Thread counterThread;


    //Game Variables
    //Game Start (0 - Nope, 1 - Start, 2 - Need Reset, 3 - To Be Reset)
    private static int gameStart = 1;
    private static String currentPlayer = TicTacToeValues.EMPTY;
    private static int turnNo = -1;
    private static int lastRow = -1, lastCol = -1;
    private static int timerDuration = 0;

    public static String[][] gameBoard = new String[3][3];


    @Override
    public String getHelpDescription() {
        return "A simple Tic Tac Toe game with either an AI or another player";
    }

    //Init
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);
        r11 = findViewById(R.id.r11);
        r12 = findViewById(R.id.r12);
        r13 = findViewById(R.id.r13);
        r21 = findViewById(R.id.r21);
        r22 = findViewById(R.id.r22);
        r23 = findViewById(R.id.r23);
        r31 = findViewById(R.id.r31);
        r32 = findViewById(R.id.r32);
        r33 = findViewById(R.id.r33);
        instructions = findViewById(R.id.lblInstructions);
        counter = findViewById(R.id.countUpTimerLabel);
        rbSP = findViewById(R.id.rbSP);
        grpGamePlay = findViewById(R.id.grpGamePlay);
        btnReset = findViewById(R.id.btnReset);
        btnStart = findViewById(R.id.btnStart);

        r11.setOnClickListener(this);
        r12.setOnClickListener(this);
        r13.setOnClickListener(this);
        r21.setOnClickListener(this);
        r22.setOnClickListener(this);
        r23.setOnClickListener(this);
        r31.setOnClickListener(this);
        r32.setOnClickListener(this);
        r33.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnStart.setOnClickListener(this);
    }

    //Start of stuff
    @Override
    public void onResume(){
        super.onResume();

        initializeGame();
        disableGame();
    }

    private void initializeGame(){
        LoggingClass.logInfo("SYSTEM", "Init Game");
        r11.setText(TicTacToeValues.EMPTY);
        r12.setText(TicTacToeValues.EMPTY);
        r13.setText(TicTacToeValues.EMPTY);
        r21.setText(TicTacToeValues.EMPTY);
        r22.setText(TicTacToeValues.EMPTY);
        r23.setText(TicTacToeValues.EMPTY);
        r31.setText(TicTacToeValues.EMPTY);
        r32.setText(TicTacToeValues.EMPTY);
        r33.setText(TicTacToeValues.EMPTY);

        for (int x = 0; x < gameBoard.length; x++){
            for (int y = 0; y < gameBoard[x].length; y++){
                gameBoard[x][y] = TicTacToeValues.EMPTY;
            }
        }
        LoggingClass.logInfo("SYSTEM", "Init Complete");
    }

    private void disableGame(){
        LoggingClass.logInfo("SYSTEM", "Game Area Disabled");
        gameStart = 0;
        timerDuration = 0;
        counter.setText(R.string.tic_tac_toe_placeholder_time);
        updateGamePlayArea(false);
        btnReset.setEnabled(false);
    }

    private boolean isSinglePlayer(){
        return rbSP.isChecked();
    }

    private void promptReset(){
        LoggingClass.logInfo("SYSTEM", "Init Reset Prompt");
        gameStart = 2;
        updateGamePlayArea(false);
    }

    private void checkWon()
    {
        if (AlgorithmCheck.hasWon(TicTacToeValues.X, gameBoard))
        {
            LoggingClass.logInfo("GAME-WIN", "X WON");
            gameStart = 3;
            new AlertDialog.Builder(this).setTitle("X Won!").setMessage("X has won the game!\nTime Taken: " + timerDuration + " seconds")
                    .setPositiveButton(android.R.string.ok, null).show();
            promptReset();
        }
        else if (AlgorithmCheck.hasWon(TicTacToeValues.O, gameBoard))
        {
            LoggingClass.logInfo("GAME-WIN", "O WON");
            gameStart = 3;
            new AlertDialog.Builder(this).setTitle("O Won!").setMessage("O has won the game!\n" +
                    "Time Taken: " + timerDuration + " seconds")
                    .setPositiveButton(android.R.string.ok, null).show();
            promptReset();
        }

        //If not won, update
        if (gameStart == 1)
        {
            nextTurn();
        }
    }

    private void nextTurn()
    {
        if (isSinglePlayer())
        {
            LoggingClass.logInfo("GAME", "Single Player Mode");
            turnNo++;
            LoggingClass.logInfo("GAME", "Incremented Turn No");
            if (currentPlayer.equals(TicTacToeValues.X))
            {
                //AI's Turn
                currentPlayer = TicTacToeValues.AI;
                updateTurnDisplay();
                updateGamePlayArea(false);
                ComputerAI.determineNextMove(gameBoard, lastRow, lastCol, turnNo, this);
                updateAIRefreshes();
            }
            else
            {
                currentPlayer = TicTacToeValues.X;
                updateGamePlayArea(true);
                updateTurnDisplay();
            }

            //Check drawn
            if (AlgorithmCheck.hasDrawn(turnNo))
            {
                if (gameStart == 2)
                {
                    LoggingClass.logInfo("SYSTEM", "Already prompted Draw Message");
                }
                else
                {
                    gameStart = 3;
                    new AlertDialog.Builder(this).setTitle("Game Drawn").setMessage("This game is a draw!\n" +
                            "Time Taken: " + timerDuration + " seconds")
                                    .setPositiveButton(android.R.string.ok, null).show();
                    LoggingClass.logInfo("SYSTEM", "Game Drawn");
                    promptReset();
                }
            }

        }
        else
        {
            LoggingClass.logInfo("GAME", "Multiplayer Mode");
            turnNo++;
            LoggingClass.logInfo("GAME", "Incremented Turn No");
            if (currentPlayer.equals(TicTacToeValues.X))
                currentPlayer = TicTacToeValues.O;
            else
                currentPlayer = TicTacToeValues.X;
            updateTurnDisplay();

            //Check drawn
            if (AlgorithmCheck.hasDrawn(turnNo))
            {
                gameStart = 3;
                new AlertDialog.Builder(this).setTitle("Game Drawn").setMessage("This game is a draw!\n" +
                        "Time Taken: " + timerDuration + " seconds")
                        .setPositiveButton(android.R.string.ok, null).show();
                LoggingClass.logInfo("SYSTEM", "Game Drawn");
                promptReset();
            }
        }
    }

    private void startGameSP()
    {
        LoggingClass.logInfo("SYSTEM", "Game Start (SP)");
        instructions.setText(getString(R.string.tic_tac_toe_hint_hud, "Player", 0));
        currentPlayer = TicTacToeValues.X;
        turnNo = 0;
    }

    private void startGameMultiplayer()
    {
        LoggingClass.logInfo("SYSTEM", "Game Start (MP)");
        Random random = new Random();
        int whoStartsFirst = random.nextInt(2);
        LoggingClass.logInfo("Random", "Rolled " + whoStartsFirst);
        if (whoStartsFirst == 1)
        {
            //X
            instructions.setText(getString(R.string.tic_tac_toe_hint_hud, "X", 0));
            currentPlayer = TicTacToeValues.X;
        }
        else
        {
            //O
            instructions.setText(getString(R.string.tic_tac_toe_hint_hud, "O", 0));
            currentPlayer = TicTacToeValues.O;
        }
        turnNo = 0;

    }

    private void resetGame()
    {
        gameStart = 3;
        LoggingClass.logInfo("SYSTEM", "Game Reset");
        initializeGame();
        disableGame();
        btnStart.setEnabled(true);
        grpGamePlay.setEnabled(true);
        instructions.setText(R.string.tic_tac_toe_hint_start);
    }

    private void updateTurnDisplay()
    {
        if (isSinglePlayer())
        {
            if (currentPlayer.equals(TicTacToeValues.AI))
                instructions.setText(getString(R.string.tic_tac_toe_hud, currentPlayer, turnNo));
            else
                instructions.setText(getString(R.string.tic_tac_toe_hud, "Player", turnNo));
        }
        else
        {
            instructions.setText(getString(R.string.tic_tac_toe_hud, currentPlayer, turnNo));
        }
    }

    private void updateMove(int row, int col)
    {
        if (checkValid(row, col))
        {
            LoggingClass.logInfo("Player Move", "Turn " + turnNo + ": Placed at " + row + ":" + col);
            gameBoard[row][col] = currentPlayer;
            updateButtons();
            lastCol = col;
            lastRow = row;
            checkWon();
        }
    }

    public static void updateAIMove(int row, int col)
    {
        LoggingClass.logInfo("AI Move", "Turn " + turnNo + ": Placed at " + row + ":" + col);
        gameBoard[row][col] = TicTacToeValues.O;
    }

    private void updateAIRefreshes()
    {
        LoggingClass.logInfo("Refresh AI", "Refreshing AI");
        updateButtons();
        checkWon();
    }

    private void updateButtons()
    {
        LoggingClass.logInfo("Update Button", "Updating Button");
        r11.setText(gameBoard[0][0]);
        r12.setText(gameBoard[0][1]);
        r13.setText(gameBoard[0][2]);
        r21.setText(gameBoard[1][0]);
        r22.setText(gameBoard[1][1]);
        r23.setText(gameBoard[1][2]);
        r31.setText(gameBoard[2][0]);
        r32.setText(gameBoard[2][1]);
        r33.setText(gameBoard[2][2]);
        LoggingClass.logInfo("Update Button", "Update Complete");
    }

    private Boolean checkValid(int row, int col){
        if (!gameBoard[row][col].equals(TicTacToeValues.EMPTY)){
            new AlertDialog.Builder(this).setTitle("Invalid Move")
                    .setMessage("Invalid Selection. It is taken up by '" + gameBoard[row][col] + "' Please choose another selection.")
                    .setPositiveButton(android.R.string.ok, null).show();
            return false;
        }
        return true;
    }

    private void updateGamePlayArea(boolean value){
        r11.setEnabled(value);
        r12.setEnabled(value);
        r13.setEnabled(value);
        r21.setEnabled(value);
        r22.setEnabled(value);
        r23.setEnabled(value);
        r31.setEnabled(value);
        r32.setEnabled(value);
        r33.setEnabled(value);
    }

    private class CountupTimer implements Runnable {

        private long start;

        CountupTimer(){
            this.start = System.currentTimeMillis();
            LoggingClass.logInfo("Timer", "Timer started");
        }

        @Override
        public void run() {
            while (gameStart != 3 || !Thread.currentThread().isInterrupted()) {
                if (gameStart == 1) {
                    // Display the new time left
                    // by updating the Time Left label.
                    timerDuration = (int) (System.currentTimeMillis()-start)/1000;
                } else if (gameStart == 3){
                    counterThread.interrupt();
                } else {
                    timerDuration = 0;
                }
                runOnUiThread(() -> counter.setText("Time Taken: " + timerDuration + " seconds"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            LoggingClass.logInfo("Timer", "Timer stopped");
        }
    }


    //Button Onclick listener
    @Override
    public void onClick(View v) {
        if (v.getId() == r11.getId()){
            updateMove(0,0);
            return;
        }
        if (v.getId() == r12.getId()){
            updateMove(0,1);
            return;
        }
        if (v.getId() == r13.getId()){
            updateMove(0,2);
            return;
        }
        if (v.getId() == r21.getId()){
            updateMove(1,0);
            return;
        }
        if (v.getId() == r22.getId()){
            updateMove(1,1);
            return;
        }
        if (v.getId() == r23.getId()){
            updateMove(1,2);
            return;
        }
        if (v.getId() == r31.getId()){
            updateMove(2,0);
            return;
        }
        if (v.getId() == r32.getId()){
            updateMove(2,1);
            return;
        }
        if (v.getId() == r33.getId()){
            updateMove(2,2);
            return;
        }
        if (v.getId() == btnStart.getId()){
            LoggingClass.logInfo("Timer", "Creating new Timer thread");
            if (counterThread != null) {
                if (counterThread.isAlive()) {
                    counterThread.interrupt();
                }
            }
            counterThread = new Thread(new CountupTimer());
            counterThread.start();
            LoggingClass.logInfo("Timer", "Calling start of thread");
            if (isSinglePlayer())
            {
                //Single Player
                instructions.setText(R.string.tic_tac_toe_hint_sp);
                gameStart = 1;
                btnStart.setEnabled(false);
                btnReset.setEnabled(true);
                grpGamePlay.setEnabled(false);
                updateGamePlayArea(true);
                startGameSP();
            }
            else
            {
                //Multi Player
                instructions.setText(R.string.tic_tac_toe_hint_mp);
                gameStart = 1;
                btnStart.setEnabled(false);
                btnReset.setEnabled(true);
                grpGamePlay.setEnabled(false);
                updateGamePlayArea(true);
                startGameMultiplayer();
            }
            return;
        }
        if (v.getId() == btnReset.getId()){
            resetGame();
        }
    }
}


