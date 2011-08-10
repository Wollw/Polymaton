package com.wolliw.polymaton;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;

// Thread to update the cell state
public class UpdateThread extends Thread {
	
	private boolean running = false;
	private Board board = null;
	private SurfaceHolder surfaceHolder = null;

	private int bpm = 150;
	private int sleepTime = 60000/bpm;

	public UpdateThread(Board board) {
		super();
		this.board = board;
		this.surfaceHolder = board.getHolder();
	}

	public void startThread() {
		running = true;
		super.start();
	}

	public void stopThread() {
		running = false;
	}

	public void run() {
		Canvas c = null;
		while (running) {
			c = null;
			try {
				c = surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					if (c != null) {
						// Change live/dead state of cells
						board.updateState();
						board.onDraw(c);
					}
				}
				sleep(this.sleepTime);
			} catch (InterruptedException ie) {
			}
			finally {
				// Do this finally to keep Surface from
				// being in inconsistent state due to an exception
				if (c != null) {
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}

}
