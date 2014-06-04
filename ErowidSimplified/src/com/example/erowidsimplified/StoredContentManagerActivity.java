package com.example.erowidsimplified;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class StoredContentManagerActivity extends Activity {

	com.example.erowidsimplified.SharedMethods m = new com.example.erowidsimplified.SharedMethods();
	ActionMode mActionMode;
	String chosenItem;	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stored_manager);
		//createMenu(); //pulls web content for the spinners

		updateOfflineFilenameListView();
		final ListView storedContentListView = (ListView) findViewById(R.id.storedListView);
		
		View empty = getLayoutInflater().inflate(R.layout.empty_list_item, null, false);
		addContentView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		storedContentListView.setEmptyView(empty);
		
	    //click listener for opening saved pages
	    storedContentListView.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view,
	    			int position, long id) {

	    		if (mActionMode == null) { 
		    		String selectedItem = (String) storedContentListView.getItemAtPosition(position);
					Intent intent = new Intent(getBaseContext(),WebDisplayActivity.class);
					intent.putExtra("STORED_PAGE", selectedItem);
					startActivity(intent);
	    		}
	    		else
	    		{
	    			mActionMode.finish();
	    			mActionMode = null;
		            view.setSelected(false);
	    		}
	    	}
	    });
	    
	  //this allows modifications of the actions bar
	    final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	        // Called when the action mode is created; startActionMode() was called
	        @Override
	        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	            // Inflate a menu resource providing context menu items
	            MenuInflater inflater = mode.getMenuInflater();
	            inflater.inflate(R.menu.stored_context_menu, menu);
	            return true;
	        }
   
	        // Called each time the action mode is shown. Always called after onCreateActionMode, but
	        // may be called multiple times if the mode is invalidated.
	        @Override
	        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	            return false; // Return false if nothing is done
	        }

	        // Called when the user selects a contextual menu item
	        @Override
	        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            switch (item.getItemId()) {
	                case R.id.delete_page:
	                	//find file, delete, update list
	                	deleteFile(chosenItem);
	                	updateOfflineFilenameListView();
	                	mode.finish();
	                	return true;
	                default:
	                    return false;
	            }
	        }

	        // Called when the user exits the action mode
	        @Override
	        public void onDestroyActionMode(ActionMode mode) {
	            mActionMode = null;
	            storedContentListView.clearChoices();
	            storedContentListView.requestLayout();
	        }
	    };
	    
	    //long-click listener for creating context menu
	    //TODO This interaction is not that obvious. Do something about it, maybe documentation?
	    storedContentListView.setOnItemLongClickListener(new OnItemLongClickListener(){
	    	@Override
	    	public boolean onItemLongClick(AdapterView<?> parent, View view,
	    			int position, long id)
	    	{
	    		chosenItem = (String) storedContentListView.getItemAtPosition(position);
//	    		Toast.makeText(getApplicationContext(),"A context menu should open.",Toast.LENGTH_SHORT).show();
//	    		storedContentListView.setItemChecked(position, true);
//	    		return true;
	    		
				if (mActionMode != null) {
	    			mActionMode.finish();
	    			mActionMode = null;
		            view.setSelected(false);
	            }

	            // Start the CAB using the ActionMode.Callback defined above
	            mActionMode = startActionMode(mActionModeCallback);
	            view.setSelected(true);
	            return true;
	    	}
	    });
	}
	
	/**
	 * Creates a list of the files stored offline and populates the spinner that displays those files.
	 */
	public void updateOfflineFilenameListView()
	{
		List<String> offlineFilenameList = new ArrayList<String>();
		String path = getFilesDir().getPath();
		File directory;
	    directory = new File(path);
	    for (File f : directory.listFiles()) {
	        if (f.isFile())
	        {
	            offlineFilenameList.add(f.getName());
	        }
	    } 
	    final ListView storedContentListView = (ListView) findViewById(R.id.storedListView);
		String[] spinnerArray = offlineFilenameList.toArray(new String[(offlineFilenameList.size())]);
		ArrayAdapter<String> typeAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray );
	    storedContentListView.setAdapter( typeAdapter2);
	}
}
