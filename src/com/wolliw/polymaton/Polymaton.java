package com.wolliw.polymaton;

import android.app.Activity;
import android.os.Bundle;

public class Polymaton extends Activity
{
	private Board board;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		board = new Board(this);
        setContentView(board);
    }

	@Override
	public void onPause() {
		super.onPause();
		board.stopUpdateThread();
	}

	@Override
	public void onResume() {
		super.onResume();
		board.startUpdateThread();
	}

}
