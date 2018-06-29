package com.example.isszym.aeroplanechess;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.INotificationSideChannel;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class Board {
    private int status;         // 状态（游戏未开始，游戏已开始，游戏结束）
    private int turn;
    private float screenWidth, screenHeight;
    private float boardLength;
    private float gridLength;
    private float xOffset;      // 棋盘在屏幕X方向即右方向的偏移
    private float yOffset;      // 棋盘在屏幕Y方向即下方向的偏移
    private Context context;
    private ImageView boardView;
    private TextView diceView;
    private int diceNumber;
    private int selectPlane;    // 点击移动的飞机
    private ArrayList<Integer>[] positions;
    private Airplane[] planes;

    Board(ImageView boardView, TextView diceView, float screenWidth, float screenHeight){
        this.boardView = boardView;
        this.diceView = diceView;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.status = Commdef.GAME_NOT_START;
        boardLength = (int)(screenWidth / 18) * 18;
        gridLength = boardLength / 36;
        System.out.println("boardLength");
        System.out.println(boardLength);
        ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
        boardParams.width = (int)boardLength;
        boardParams.height = (int)boardLength;
        boardView.setLayoutParams(boardParams);
    }

    public void initPlanes(ImageView[] planeViews){
        planes = new Airplane[]{
                new Airplane(Commdef.BLUE, 0, 0, gridLength, xOffset, yOffset, planeViews[0]),
                new Airplane(Commdef.BLUE, 1, 1, gridLength, xOffset, yOffset, planeViews[1]),
                new Airplane(Commdef.BLUE, 2, 2, gridLength, xOffset, yOffset, planeViews[2]),
                new Airplane(Commdef.BLUE, 3, 3, gridLength, xOffset, yOffset, planeViews[3]),
                new Airplane(Commdef.GREEN, 4, 5, gridLength, xOffset, yOffset, planeViews[4]),
                new Airplane(Commdef.GREEN, 5, 6, gridLength, xOffset, yOffset, planeViews[5]),
                new Airplane(Commdef.GREEN, 6, 7, gridLength, xOffset, yOffset, planeViews[6]),
                new Airplane(Commdef.GREEN, 7, 8, gridLength, xOffset, yOffset, planeViews[7]),
                new Airplane(Commdef.RED, 8, 10, gridLength, xOffset, yOffset, planeViews[8]),
                new Airplane(Commdef.RED, 9, 11, gridLength, xOffset, yOffset, planeViews[9]),
                new Airplane(Commdef.RED, 10, 12, gridLength, xOffset, yOffset, planeViews[10]),
                new Airplane(Commdef.RED, 11, 13, gridLength, xOffset, yOffset, planeViews[11]),
                new Airplane(Commdef.YELLOW, 12, 15, gridLength, xOffset, yOffset, planeViews[12]),
                new Airplane(Commdef.YELLOW, 13, 16, gridLength, xOffset, yOffset, planeViews[13]),
                new Airplane(Commdef.YELLOW, 14, 17, gridLength, xOffset, yOffset, planeViews[14]),
                new Airplane(Commdef.YELLOW, 15, 18, gridLength, xOffset, yOffset, planeViews[15]),
        };
    }

    public void gameStart(Context context){
        status = Commdef.GAME_START;
        // 还原飞机位置
        for (int i : Commdef.COLOR_PLANE[turn]) {
            planes[i].restore();
        }
        // 随机决定哪方先开始
        this.context = context;
        Random rand = new Random();
        turn = rand.nextInt(4);
        // 调整骰子大小
        ViewGroup.LayoutParams params = diceView.getLayoutParams();
        params.width = (int)(Commdef.DICE_GRID_NUM*gridLength);
        params.height = (int)(Commdef.DICE_GRID_NUM*gridLength);
        diceView.setLayoutParams(params);
        diceView.setText("骰子?");
        beginTurn();
    }

    public void beginTurn(){
        // 调整骰子的位置
        if (turn == Commdef.BLUE) {
            diceView.setX(0);
            diceView.setY(yOffset + boardLength);
        } else if (turn == Commdef.GREEN) {
            diceView.setX(0);
            diceView.setY(yOffset - Commdef.DICE_GRID_NUM*gridLength);
        } else if (turn == Commdef.RED) {
            diceView.setX(screenWidth - Commdef.DICE_GRID_NUM*gridLength);
            diceView.setY(yOffset - Commdef.DICE_GRID_NUM*gridLength);
        } else if (turn == Commdef.YELLOW) {
            diceView.setX(screenWidth - Commdef.DICE_GRID_NUM*gridLength);
            diceView.setY(yOffset + boardLength);
        }
        diceView.setVisibility(View.VISIBLE);
        diceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rand = new Random();
                diceNumber = rand.nextInt(6) + 1;
                diceView.setText(String.valueOf(diceNumber));
                ArrayList<Integer> outsidePlanes = new ArrayList<Integer>();
                // 是否全在机场
                boolean isAllInAirport = true;
                for (int i : Commdef.COLOR_PLANE[turn]) {
                    if (!planes[i].isInAirport()) {
                        isAllInAirport = false;
                        outsidePlanes.add(i);
                    }
                }
                // 是否是起飞的点数
                boolean ableToTakeOff = false;
                for (int each : Commdef.TAKE_OFF_NUMBER) {
                    if (each == diceNumber) {
                        ableToTakeOff = true;
                        break;
                    }
                }
                if (ableToTakeOff) {
//                    diceView.setClickable(false);
                    Toast.makeText(context, "飞", Toast.LENGTH_SHORT).show();
                    for (int i : Commdef.COLOR_PLANE[turn]) {
                        planes[i].setListner(diceNumber);
                    }
                } else {
                    if (isAllInAirport) {
                        Toast.makeText(context, "无法起飞", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "飞", Toast.LENGTH_SHORT).show();
                        for (Integer i : outsidePlanes) {
                            planes[i].setListner(diceNumber);
                        }
                        outsidePlanes.clear();
                    }
                }
                turn = (turn + 1) % 4;
                beginTurn();
            }
        });
    }

    public ImageView getBoardView() {
        return boardView;
    }

    public float getXFromIndex(int index){
        return xOffset + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffset + gridLength * Commdef.POSITIONS[index][1];
    }

    public float getGridLength() {
        return gridLength;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public void setBoardView(ImageView boardView){
        this.boardView = boardView;
    }

    public void setDiceView(TextView diceView){
        this.diceView = diceView;
    }


    public void setLength(float screenWidth){
        this.screenWidth = screenWidth;
        boardLength = (int)(screenWidth / 18) * 18;
        gridLength = boardLength / 36;
        System.out.println("boardLength");
        System.out.println(boardLength);
        ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
        boardParams.width = (int)boardLength;
        boardParams.height = (int)boardLength;
        boardView.setLayoutParams(boardParams);
    }
}