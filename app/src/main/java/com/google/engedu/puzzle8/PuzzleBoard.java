package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;


public class PuzzleBoard implements Comparable {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };
    private ArrayList<PuzzleTile> tiles;
    private int steps;
    private PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, true);
        tiles = new ArrayList<>();
        Bitmap[][] imgs = new Bitmap[NUM_TILES][NUM_TILES];
        int w, h;
        w = scaledBitmap.getWidth() / NUM_TILES;
        h = scaledBitmap.getHeight() / NUM_TILES;
        int index = 0;
        for (int i = 0; i < NUM_TILES; i++) {
            for (int j = 0; j < NUM_TILES; j++) {
                imgs[i][j] = Bitmap.createBitmap(scaledBitmap, j * w, i * h, w, h);
                //Bitmap.createBitmap()
                tiles.add(new PuzzleTile(imgs[i][j], index++));
            }
        }
        tiles.set(NUM_TILES * NUM_TILES - 1, null);
        this.steps = 0;
        this.previousBoard = null;
    }


    PuzzleBoard(PuzzleBoard otherBoard) {
        this.previousBoard = otherBoard;
        this.tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        this.steps = otherBoard.steps + 1;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }
        }
        return false;
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> boards = new ArrayList<>();
        PuzzleBoard t;

        int nullTileIndex = 0;

        for (int i = 0; i < tiles.size(); i++) {
            if(tiles.get(i) == null) {
                nullTileIndex = i;
                break;
            }
        }

        int nullR = nullTileIndex % 3;
        int nullC = nullTileIndex / 3;

        for (int[] delta : NEIGHBOUR_COORDS) {
            int nX = nullC + delta[0];
            int nY = nullR + delta[1];

            if (nX >= 0 && nX < NUM_TILES && nY >= 0 && nY < NUM_TILES) {
                t = new PuzzleBoard(this);
                t.tryMoving(nX, nY);
                boards.add(t);
            }
        }

        return boards;
    }

    public int priority() {
        return manhattanDistance() + steps;
    }

    private int manhattanDistance() {
        int d = 0;
        int t;
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            if (tiles.get(i) == null) {
                t = 8;
            } else t = tiles.get(i).getNumber();
            if(t != i) {
                d += (Math.abs(t/NUM_TILES - i/NUM_TILES) + Math.abs(t%NUM_TILES - i%NUM_TILES));
            }
        }

        return d;
    }

    public PuzzleBoard getPreviousBoard() {
        return previousBoard;
    }

    @Override
    public int compareTo(Object o) {
        PuzzleBoard p = (PuzzleBoard) o;
        return priority() - p.priority();
    }
}
