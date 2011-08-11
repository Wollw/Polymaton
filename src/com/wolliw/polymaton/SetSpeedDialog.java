package com.wolliw.polymaton;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SetSpeedDialog extends Dialog {
	
	private Board board;
	final private EditText et;
	private Integer bpm;
	private Context mContext;


	public SetSpeedDialog(final Context context, Board board) {
		super(context);
		mContext = context;

		this.board = board;
		this.bpm = new Integer(this.board.getBPM());

		this.setContentView(R.layout.dialog_setspeed);
		this.setTitle("Beats Per Minute");

		this.et = (EditText) this.findViewById(R.id.dialog_setspeed_edittext);
		this.et.setText(bpm.toString());

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll=(LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_setspeed, null);
		setContentView(ll);
	}

}

