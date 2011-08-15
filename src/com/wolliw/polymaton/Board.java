package com.wolliw.polymaton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;


// The Board the game is played on.
public class Board extends SurfaceView implements SurfaceHolder.Callback
{
	private BoardData boardData = null;

	private Paint borderPaint = null;
	private int bgColor = 0;

	private UpdateThread thread = null;

	private HashMap<Integer,PolymatonCell> cellsToDraw = null;

	private boolean paused = true;

	private int width = 0;
	private int height = 0;

	public Board(Context ctx) {
		super(ctx);

		this.boardData = new BoardData(ctx, "default.json");

		this.bgColor = this.boardData.getBackgroundColor();

		this.borderPaint = this.boardData.getBorderPaint();
		this.borderPaint.setAntiAlias(true);
		this.borderPaint.setDither(true);
		this.borderPaint.setStyle(Paint.Style.STROKE);
		this.borderPaint.setStrokeWidth(3);

		
		getHolder().addCallback(this);

	}

	// Called each frame.  calculate the next frame if not paused.
	public void updateState() {
		// Don't update if paused
		if (this.paused)
			return;

		this.boardData.updateData();
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

	// methods for pausing/unpauses and related actions
	public void pauseUnpause() {
		if (this.paused)
			this.paused = false;
		else
			this.paused = true;
	}
	public boolean isPaused() {
		return this.paused;
	}
	
	// methods for getings/setting the beats per minute
	public int getBPM() {
		return this.boardData.getBPM();
	}
	public void setBPM(int bpm) {
		this.boardData.setBPM(bpm);
	}

	public void onDraw(Canvas canvas) {
		this.width = canvas.getWidth();
		this.height = canvas.getHeight();
		float scaleX = this.width/(boardData.getDistOriginX()*2);
		float scaleY = this.height/(boardData.getDistOriginY()*2);
		float scale = scaleX < scaleY ? scaleX : scaleY;
		canvas.scale(scale, scale, this.width/2, this.height/2);
		canvas.translate(this.width/2, this.height/2);
		canvas.drawColor(this.bgColor);

		cellsToDraw = this.boardData.getCells();
		for (int i : cellsToDraw.keySet()) {
			canvas.drawPath(this.boardData.getCell(i),
							this.boardData.getCell(i).getPaint());
			canvas.drawPath(this.boardData.getCell(i), this.borderPaint);
							
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
