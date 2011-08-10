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

public class BoardData {
	private InputStream is = null;
	private BufferedReader br = null;

	private JSONObject jsonObj = null;
	private JSONArray jsonArr = null;

	private ArrayList<PolymatonCell> cells = null;

	private Float originX = new Float(0.0);
	private Float originY = new Float(0.0);
	private Float scale = new Float(1.0);

	public BoardData(Context ctx, int res) {
		// Open stream for board's data file
		is = ctx.getResources().openRawResource(res);
		br = new BufferedReader(new InputStreamReader(is));
		String readLine = null;

		// read the file into a string buffer
		StringBuffer buf = new StringBuffer();
		try {
			int i = 0;
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
			cells = new ArrayList<PolymatonCell>();
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
			// load the cells' data
			JSONArray jsonCellArray = null;
			for (int i = 0; i < jsonArr.length(); i++) {
				jsonObj = jsonArr.getJSONObject(i);

				// First get the coordinates
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
					Integer id = jsonCellArray.getInt(j);
					neighbors.add(new Integer(id));
				}

				// Create the cell
				cells.add(new PolymatonCell(points,neighbors));

			}
		} catch (Exception e) {
			Log.e("Poly","Error building point arrays from JSON data.");
		}

	}

	// Get a cell by index
	public int getCellCount() {
		return this.cells.size();
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
	public PolymatonCell getCell(int i) {
		return this.cells.get(i);
	}

}
