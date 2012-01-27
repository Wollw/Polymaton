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
	private Integer speed;
	private Context mContext;


	public SetSpeedDialog(final Context context, Board board) {
		super(context);
		mContext = context;

		this.board = board;
		this.speed = new Integer(this.board.getSpeed());

		this.setContentView(R.layout.dialog_setspeed);
		this.setTitle("Speed in MS");

		this.et = (EditText) this.findViewById(R.id.dialog_setspeed_edittext);
		this.et.setText(speed.toString());

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll=(LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_setspeed, null);
		setContentView(ll);
	}

}

