package com.wolliw.polymaton;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

public class PolymatonCell extends Path {
	// alive or dead
	private boolean alive = false;
	private Paint paintLive = null;
	private Paint paintDead = null;

	private ArrayList<Integer> neighbors = null;

	public PolymatonCell(ArrayList<Float> p, ArrayList<Integer> n) {
		// Setup paints
		this.paintLive = new Paint();
		this.paintLive.setColor(Color.WHITE);
		//this.paintLive.setAntiAlias(true);
		this.paintLive.setDither(true);
		//this.paintLive.setStyle(Paint.Style.FILL_AND_STROKE);
		//this.paintLive.setStrokeWidth(5);
		this.paintDead = new Paint();
		this.paintDead.setColor(Color.DKGRAY);
		//this.paintDead.setAntiAlias(true);
		this.paintDead.setDither(true);
		//this.paintDead.setStyle(Paint.Style.FILL_AND_STROKE);
		//this.paintDead.setStrokeWidth(5);

		// Add the points to this object's internal points array
		for (int i = 0; i < p.size(); i+=2) {
			if (i == 0) {
				this.moveTo(p.get(i),p.get(i+1));
			}
			else {
				this.lineTo(p.get(i),p.get(i+1));
			}
		}
		this.lineTo(p.get(0),p.get(1));
	
		// Add the neighboring cell ids to the neighbor list
		neighbors = new ArrayList<Integer>();
		for (int i = 0; i < n.size(); i++) {
			neighbors.add(n.get(i));
		}

	}

	// Get length of neighbor list
	public int neighborCount() {
		return neighbors.size();
	}

	// Return the id of a neighbor
	public int getNeighborId(int i) {
		return neighbors.get(i);
	}

	// Get current state of cell
	public boolean isAlive() {
		return this.alive;
	}

	// Toggle cell state
	public void changeState() {
		if (this.alive)
			this.alive = false;
		else
			this.alive = true;
	}
	public void makeLive() {
		this.alive = true;
	}
	public void makeDead() {
		this.alive = false;
	}

	// Set/Get the cell's paint
	public void setPaintLive(Paint p) {
		this.paintLive = p;
	}
	public void setPaintDead(Paint p) {
		this.paintDead = p;
	}
	public Paint getPaint() {
		if (this.alive)
			return this.paintLive;
		else
			return this.paintDead;
	}

}
