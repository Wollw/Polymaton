package com.wolliw.polymaton;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;


import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BoardData {
	private BufferedReader br = null;

	private JSONObject jsonObj = null;
	private JSONArray jsonArr = null;

	private HashMap<Integer, PolymatonCell> cells = null;

	private Float originX = new Float(0.0);
	private Float originY = new Float(0.0);
	private Float scale = new Float(1.0);
	private Float width = new Float(0.0);
	private Float height = new Float(0.0);
	private Float distFromOriginX = new Float(0.0);
	private Float distFromOriginY = new Float(0.0);
	// largest and smallest points in set.  used to calculate scale
	private Float max_x = new Float(0.0);
	private Float min_x = new Float(0.0);
	private Float max_y = new Float(0.0);
	private Float min_y = new Float(0.0);
	private int speed_bpm = 100;

	private Paint borderPaint = null;
	private int bgColor = 0;

	private ArrayList<Boolean> rulesBorn = null;
	private ArrayList<Boolean> rulesSurvive = null;
	private HashMap<Integer,Boolean> initialState = null;

	public BoardData(Context ctx, String fileName) {
		// Set default paint for board to black
        this.borderPaint = new Paint();
        this.borderPaint.setColor(Color.BLACK);
		this.bgColor = Color.rgb(0,0,0);

		// Open stream for board's data file
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String filePath = baseDir+File.separator+"Polymaton/"+fileName;
		Log.d("Poly",filePath);
		File file = new File(filePath);
		String readLine = null;

		// read the file into a string buffer
		StringBuffer buf = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(file));
			while ((readLine = br.readLine()) != null) {
				buf.append(readLine);
			}
			Log.d("Poly",buf.toString());
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
			Log.d("Poly",buf.toString());
			Log.e("Poly","Error reading JSON data.");
		}
		// create this board
		try {
			// get scale if defined
			if (jsonObj.has("scale")) {
				Double s = jsonObj.getDouble("scale");
				this.scale = new Float(s.floatValue());
			}
			// get speed if defined
			if (jsonObj.has("speed_bpm")) {
				this.speed_bpm = jsonObj.getInt("speed_bpm");
			}
			
			// load the rules
			if (jsonObj.has("rules_born")) {
				rulesBorn = new ArrayList<Boolean>();
				JSONArray ja = jsonObj.getJSONArray("rules_born");
				for (int i=0;i<ja.length();i++) { 
					this.rulesBorn.add(ja.getBoolean(i)); 
				}
			}
			if (jsonObj.has("rules_survive")) {
				rulesSurvive = new ArrayList<Boolean>();
				JSONArray ja = jsonObj.getJSONArray("rules_survive");
				for (int i=0;i<ja.length();i++) { 
					this.rulesSurvive.add(ja.getBoolean(i)); 
				}
			}

			// load the initial life state of the cells
			if (jsonObj.has("initial_state")) {
				initialState = new HashMap<Integer,Boolean>();
				JSONArray ja = jsonObj.getJSONArray("initial_state");
				Boolean b;
				Integer id;
				for (int i=0;i<ja.length();i+=2) { 
					id = ja.getInt(i);
					if (ja.getInt(i+1) != 0)
						b = true;
					else
						b = false;
					this.initialState.put(id, b);
				}
				Log.d("Poly",initialState.toString());
			}

			// Load paints
			if (jsonObj.has("color_board")) {
				JSONArray ja = jsonObj.getJSONArray("color_board");
				this.bgColor = Color.rgb(ja.getInt(0),
											 ja.getInt(1),
											 ja.getInt(2));
			}
			if (jsonObj.has("color_border")) {
				JSONArray ja = jsonObj.getJSONArray("color_border");
				this.borderPaint.setColor(Color.rgb(ja.getInt(0),
											 ja.getInt(1),
											 ja.getInt(2)));
			}
			Paint paintLive = null;
			if (jsonObj.has("color_live")) {
				JSONArray ja = jsonObj.getJSONArray("color_live");
				paintLive = new Paint();
				paintLive.setColor(Color.rgb(ja.getInt(0),
											 ja.getInt(1),
											 ja.getInt(2)));
			}
			Paint paintDead = null;
			if (jsonObj.has("color_dead")) {
				JSONArray ja = jsonObj.getJSONArray("color_dead");
				paintDead = new Paint();
				paintDead.setColor(Color.rgb(ja.getInt(0),
											 ja.getInt(1),
											 ja.getInt(2)));
			}

			// load the cells' data
			JSONArray jsonCellArray = null;
			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject jsonCellObj = jsonArr.getJSONObject(i);

				// Skip this id if it doesn't exists
				if (jsonCellObj == null)
					continue;

				//Get the cell's identification (not always the json array index!)
				int id = jsonCellObj.getInt("id");
	
				// then get the coordinates
				jsonCellArray = jsonCellObj.getJSONArray("points");
				int len = jsonCellArray.length();
				ArrayList<Float> points = new ArrayList<Float>();
				for (int j = 0; j < len; j++) {
					Float f = new Float(new Double(jsonCellArray.getDouble(j)).floatValue());
					points.add(f);
					
					// keep track of max and min x and y values for scaling
					switch (j%2) {
						case 0:
							if (f > max_x)
								this.max_x = f;
							if (f < min_x)
								this.min_x = f;
							break;
						case 1:
							if (f > max_y)
								this.max_y = f;
							if (f < min_y)
								this.min_y = f;
							break;
					}
				}

				// Next get the cell neighbors list
				jsonCellArray = jsonCellObj.getJSONArray("neighbors");
				len = jsonCellArray.length();
				ArrayList<Integer> neighbors = new ArrayList<Integer>();
				for (int j = 0; j < len; j++) {
					Integer nid = jsonCellArray.getInt(j);
					neighbors.add(new Integer(nid));
				}

				// Create the cell
				cells.put(new Integer(id),new PolymatonCell(points,neighbors));

				// If there are custom paints apply them
				if (paintLive != null) 
					this.getCell(id).setPaintLive(paintLive);
				if (paintDead != null)
					this.getCell(id).setPaintDead(paintDead);

				// If it starts out alive make it so
				if (initialState != null)
					if (initialState.get(id))
						this.getCell(id).makeLive();

			}


		} catch (Exception e) {
			Log.e("Poly","Error building point arrays from JSON data.");
		}

		// Calculate width and height of board in pixels
		this.width = Math.abs(this.min_x)+Math.abs(this.max_x);
		this.height = Math.abs(this.min_y)+Math.abs(this.max_y);
		this.distFromOriginX = Math.abs(this.min_x) > Math.abs(this.max_x)
								? Math.abs(this.min_x) : Math.abs(this.max_x);
		this.distFromOriginY = Math.abs(this.min_y) > Math.abs(this.max_y)
								? Math.abs(this.min_y) : Math.abs(this.max_y);

	}

	// get the board's stroke paint
	public Paint getBorderPaint() {
		return borderPaint;
	}
	// get the board's background color
	public int getBackgroundColor() {
		return bgColor;
	}

	// Get the dimensions of the board (distance between extremes of
	// x and y axes
	public float getWidth() {
		return this.width;
	}
	public float getHeight() {
		return this.height;
	}
	// Distances from origin used to calculte scale factor
	public float getDistOriginX() {
		return this.distFromOriginX;
	}
	public float getDistOriginY() {
		return this.distFromOriginY;
	}
	// Information about the bounds of the board
	public float getMaxX() {
		return this.max_x;
	}
	public float getMinX() {
		return this.min_x;
	}
	public float getMaxY() {
		return this.max_y;
	}
	public float getMinY() {
		return this.min_y;
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
				if (this.rulesSurvive.get(livingNeighbors))
					nextStates.put(i,1);
				else
					nextStates.put(i,0);
			} else {
				if (this.rulesBorn.get(livingNeighbors))
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

	public float getScale() {
		return this.scale.floatValue();
	}

	// Get a cell by index
	public PolymatonCell getCell(int id) {
		return this.cells.get(id);
	}

}
