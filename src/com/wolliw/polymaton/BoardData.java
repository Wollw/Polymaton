package com.wolliw.polymaton;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BoardData {
	private InputStream is = null;
	private BufferedReader br = null;

	private JSONObject jsonObj = null;
	private JSONArray jsonArr = null;

	private HashMap<Integer, PolymatonCell> cells = null;

	private Float originX = new Float(0.0);
	private Float originY = new Float(0.0);
	private Float scale = new Float(1.0);
	private int speed_bpm = 100;


	// Temporary, rules should be in configurable
	private boolean[] rulesSurvive =
		{true,false,false,false,true,true,
		false,false,false,false,false};
	private boolean[] rulesBorn =
		{true,false,false,false,false,
		true,true,false,false,false,false};

	public BoardData(Context ctx, int res) {
		// Open stream for board's data file
		is = ctx.getResources().openRawResource(res);
		br = new BufferedReader(new InputStreamReader(is));
		String readLine = null;

		// read the file into a string buffer
		StringBuffer buf = new StringBuffer();
		try {
			while ((readLine = br.readLine()) != null) {
				buf.append(readLine);
			}
			is.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Parse the json into the board data
		try {
			cells = new HashMap<Integer, PolymatonCell>();
			jsonObj = new JSONObject(buf.toString());
			jsonArr = jsonObj.getJSONArray("cells");
		} catch (Exception e) {
			Log.e("Poly","Error reading JSON data.");
		}
		// create this board
		try {
			// get a defined origin if it exists
			if (jsonObj.has("origin")) {
				JSONArray ja = jsonObj.getJSONArray("origin");
				Double x = ja.getDouble(0);
				Double y = ja.getDouble(1);
				this.originX = new Float(x.floatValue());
				this.originY = new Float(y.floatValue());
			}
			// get scale if defined
			if (jsonObj.has("scale")) {
				Double s = jsonObj.getDouble("scale");
				this.scale = new Float(s.floatValue());
			}
			// get speed if defined
			if (jsonObj.has("speed_bpm")) {
				this.speed_bpm = jsonObj.getInt("speed_bpm");
			}
			// load the cells' data
			JSONArray jsonCellArray = null;
			for (int i = 0; i < jsonArr.length(); i++) {
				jsonObj = jsonArr.getJSONObject(i);

				// Skip this id if it doesn't exists
				if (jsonObj == null)
					continue;

				//Get the cell's identification (not always the json array index!)
				int id = jsonObj.getInt("id");
	
				// then get the coordinates
				jsonCellArray = jsonObj.getJSONArray("points");
				int len = jsonCellArray.length();
				ArrayList<Float> points = new ArrayList<Float>();
				for (int j = 0; j < len; j++) {
					Double dbl = jsonCellArray.getDouble(j);
					points.add(new Float(dbl.floatValue()));
				}

				// Next get the cell neighbors list
				jsonCellArray = jsonObj.getJSONArray("neighbors");
				len = jsonCellArray.length();
				ArrayList<Integer> neighbors = new ArrayList<Integer>();
				for (int j = 0; j < len; j++) {
					Integer nid = jsonCellArray.getInt(j);
					neighbors.add(new Integer(nid));
				}

				// Create the cell
				cells.put(new Integer(id),new PolymatonCell(points,neighbors));
				Log.d("Poly",id+"");

			}
		} catch (Exception e) {
			Log.e("Poly","Error building point arrays from JSON data.");
		}

	}

	// The main action happens here.  This calculates and updates the state
	// of the cells for the next turn.  This is run from the update thread.
	public void updateData() {

		// Buffer for holding the results until we are ready
		// to actually change the cells' states
		HashMap<Integer,Integer> nextStates = new HashMap<Integer,Integer>();

		PolymatonCell cell = null;

		for (int i : cells.keySet()) {
			int livingNeighbors = 0;
			cell = this.getCell(i);
			for (int j = 0; j < cell.neighborCount(); j++) {
				int ni = cell.getNeighborId(j);
				if (this.getCell(ni).isAlive())
					livingNeighbors++;
			}

			// Apply the rules
			if (cell.isAlive()) {
				if (this.rulesSurvive[livingNeighbors])
					nextStates.put(i,1);
				else
					nextStates.put(i,0);
			} else {
				if (this.rulesBorn[livingNeighbors])
					nextStates.put(i,1);
				else
					nextStates.put(i,0);
			}

		}
		// Apply the changes to the cells themselves
		for (int i : nextStates.keySet()) {
			switch (nextStates.get(i)) {
				case 1:
					this.getCell(i).makeLive();
					break;
				case 0:
					this.getCell(i).makeDead();
					break;
			}
		}

	}


	//methods for gettings/setting beats per minute
	public int getBPM() {
		return this.speed_bpm;
	}
	public void setBPM(int bpm) {
		this.speed_bpm = bpm;
	}

	// return a copy (i think?) of the internal cell HashMap 
	public HashMap<Integer,PolymatonCell> getCells() {
		return this.cells;
	}

	// get x and y coord to offset canvas
	public float getOriginX() {
		return this.originX.floatValue();
	}
	public float getOriginY() {
		return this.originY.floatValue();
	}
	public float getScale() {
		return this.scale.floatValue();
	}

	// Get a cell by index
	public PolymatonCell getCell(int id) {
		return this.cells.get(id);
	}

}
