package com.wolliw.polymaton;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

// Thread to update the cell state
public class UpdateThread extends Thread {
	
	private boolean running = false;
	private Board board = null;
	private SurfaceHolder surfaceHolder = null;

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

	public boolean isRunning() {
		return running;
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
				int bpm = this.board.getBPM();
				// Prevent division by zero.  Values under 1 will be unthrottled.
				if (bpm > 0)
					sleep(60000/bpm);
			} catch (InterruptedException ie) {
				Log.e("Poly","Error in thread trying to render frame.");
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
