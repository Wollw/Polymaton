package com.wolliw.polymaton;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.os.SystemClock;

// Thread to update the cell state
public class UpdateThread extends Thread {
	
	private boolean running = false;
	private Board board = null;
	private SurfaceHolder surfaceHolder = null;

	public UpdateThread(Board board) {
		super();
		this.board = board;
		this.surfaceHolder = board.getHolder();
		Log.d("Poly","Constructing Thread");
	}

	public void startThread() {
		running = true;
		super.start();
	}

	public void stopThread() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	long ms_last = SystemClock.uptimeMillis();
	public void run() {
		Canvas c = null;
		while (running) {
			c = surfaceHolder.lockCanvas();
			if (c != null) {
				long ms = SystemClock.uptimeMillis();
				if (ms - ms_last > this.board.getSpeed()) {
					long t = ms - ms_last;
					ms_last = ms;
					board.updateState();
				}
				synchronized (surfaceHolder) {
					if (c != null) {
						// Change live/dead state of cells
						board.onDraw(c);
					}
				}
				// Do this finally to keep Surface from
				// being in inconsistent state due to an exception
				surfaceHolder.unlockCanvasAndPost(c);
			} 
		}
	}

}
