package com.wolliw.polymaton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class Board extends SurfaceView implements SurfaceHolder.Callback
{
	private BoardData boardData = null;

	private Paint paintBorder = null;
	private UpdateThread thread = null;
	private int width = 0;
	private int height = 0;

	// Temporary, rules should be in configurable
	private boolean[] rulesSurvive =
		{true,false,false,false,true,true,
		false,false,false,false,false};
	private boolean[] rulesBorn =
		{true,false,false,false,false,
		true,true,false,false,false,false};

	public Board(Context ctx) {
		super(ctx);

		this.boardData = new BoardData(ctx, R.raw.board000);

		this.boardData.getCell(18).changeState();
		this.boardData.getCell(19).changeState();
		this.boardData.getCell(20).changeState();

		this.paintBorder = new Paint();
		this.paintBorder.setColor(Color.BLACK);
		this.paintBorder.setAntiAlias(true);
		this.paintBorder.setDither(true);
		this.paintBorder.setStyle(Paint.Style.STROKE);
		this.paintBorder.setStrokeWidth(3);

		
		getHolder().addCallback(this);

	}

	// The main action happens here.  This calculates and updates the state
	// of the cells for the next turn.  This is run from the update thread.
	public void updateState() {
		// Buffer for holding the results until we are ready
		// to actually change the cells' states
		ArrayList<Integer> nextStates = new ArrayList<Integer>();

		PolymatonCell cell = null;
		for (int i = 0; i < this.boardData.getCellCount(); i++) {
			int livingNeighbors = 0;
			cell = this.boardData.getCell(i);
			for (int j = 0; j < cell.neighborCount(); j++) {
				int ni = cell.getNeighborId(j);
				if (this.boardData.getCell(ni).isAlive())
					livingNeighbors++;
			}

			// Apply the rules
			if (cell.isAlive()) {
				if (this.rulesSurvive[livingNeighbors])
					nextStates.add(i,1);
				else
					nextStates.add(i,0);
			} else {
				if (this.rulesBorn[livingNeighbors])
					nextStates.add(i,1);
				else
					nextStates.add(i,0);
			}

		}
		// Apply the changes to the cells themselves
		for (int j = 0; j < nextStates.size(); j++) {
			switch (nextStates.get(j)) {
				case 1:
					this.boardData.getCell(j).makeLive();
					break;
				case 0:
					this.boardData.getCell(j).makeDead();
					break;
			}
		}

	}

	public void startUpdateThread() {
		if (thread == null) {
			this.thread = new UpdateThread(this);
			this.thread.startThread();
			Log.d("Poly","Thread started");
		} 
	}

	public void stopUpdateThread() {
		if (thread != null) {
			thread.stopThread();

			// Wait until thread is really stopped
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					Log.d("Poly","Thread stopped");
					retry = false;
				} catch (InterruptedException ie) {
				}
			}
			thread = null;
		}
	}

	public void onDraw(Canvas canvas) {
		this.width = canvas.getWidth();
		this.height = canvas.getHeight();
		canvas.scale(this.boardData.getScale(),
					 this.boardData.getScale(),
					 this.width/2,this.height/2);
		canvas.translate(this.width/2, this.height/2);
		canvas.drawColor(Color.BLACK);
		for (int i = 0; i < this.boardData.getCellCount(); i++) {
			canvas.drawPath(this.boardData.getCell(i),
							this.boardData.getCell(i).getPaint());
			canvas.drawPath(this.boardData.getCell(i), paintBorder);
							
		}
	}

	public void surfaceChanged(SurfaceHolder hldr, int fmt, int w, int h) {

	}

	public void surfaceCreated(SurfaceHolder hldr) {
		startUpdateThread();
	}

	public void surfaceDestroyed(SurfaceHolder hldr) {
		stopUpdateThread();
	}
}
