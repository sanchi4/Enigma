package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();
    PriorityQueue<PuzzleBoard> q;

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap, View parent) {
        int width = parent.getWidth();

        puzzleBoard = new PuzzleBoard(imageBitmap, width);
//        shuffle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG).show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for (int i = 0; i < NUM_SHUFFLE_STEPS; i++) {
                ArrayList<PuzzleBoard> boards = puzzleBoard.neighbours();
                puzzleBoard = boards.get(random.nextInt(boards.size()));
            }

            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PuzzleBoardComparator pbc = new PuzzleBoardComparator();
        PuzzleBoard t;
        q = new PriorityQueue<>(10, pbc);
        q.add(puzzleBoard);

        while (!q.isEmpty()) {
            t = q.remove();
            if (t.resolved()) {
                ArrayList<PuzzleBoard> n = new ArrayList<>();

                PuzzleBoard test = t.getPreviousBoard();

                while (test != null) {
                    n.add(test);
                    test = test.getPreviousBoard();
                }

                Collections.reverse(n);

                animation = n;

                invalidate();
            } else {
                ArrayList<PuzzleBoard> n = t.neighbours();
                for (int i = 0; i < n.size(); i++) {
                    q.add(n.get(i));
                }
            }
        }
    }
}
