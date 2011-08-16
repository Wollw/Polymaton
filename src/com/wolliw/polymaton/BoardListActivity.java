package com.wolliw.polymaton;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONObject;

public class BoardListActivity extends ListActivity {

	private ArrayList<String> boardNames = new ArrayList<String>();
	private ArrayList<String> boardPaths = new ArrayList<String>();

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		// Open stream for board's data file
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String folderPath = baseDir+File.separator+"Polymaton/";
		File file = new File(folderPath);
		String readLine = null;

		BufferedReader br = null;
		StringBuffer buf = null;
		JSONObject jo = null;
		File[] files = file.listFiles();
		// Get name of each board and add it to an array of names
		// that will be listed in the ListView
		for (int i = 0; i < files.length; i++) {
			try {
				buf = new StringBuffer();
				br = new BufferedReader(new FileReader(files[i]));
				while ((readLine = br.readLine()) != null) {
					buf.append(readLine);
				}
				// try to parse board data and get the name property for listing
				try {
					jo = new JSONObject(buf.toString());
					if (jo.has("name")) {
						// names and paths stored at same indexes
						boardNames.add(jo.getString("name"));
						boardPaths.add(files[i].getName());
					}
				} catch (Exception e) {
					android.util.Log.e("Poly","Error parsing board files");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Add the board names to the List
		this.setListAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, boardNames));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);
		
		// Get clicked item
		Object o = this.getListAdapter().getItem(pos);
		android.util.Log.d("Poly",pos+": "+boardPaths.get(pos));

		// Send board path to main activity for rendering
		Intent i = new Intent(this,Polymaton.class);
		i.putExtra("file_path",boardPaths.get(pos));
		startActivity(i);
		
	}

}
