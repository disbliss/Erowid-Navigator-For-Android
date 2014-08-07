package org.erowid.navigatorandroid;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Matthew Dunlap
 * @date 06/2014
 */
public class StoredContentManagerActivity extends Activity {

	org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
	ActionMode mActionMode;
	String chosenItem;
	Menu theMenu; 
	String downloadedHtmlContent = "";
	Boolean inEditMode = false;
	ListView storedContentListView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stored_manager);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Stored Pages"); 
		
		updateOfflineFilenameListView();
		storedContentListView = (ListView) findViewById(R.id.storedListView);
		
		//set view for list when it is empty
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
	}
	
	/**
	 * Inflate the menu; this adds items to the action bar if it is present.
	 */ 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		theMenu = menu;
		MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.menu_stored_content, menu);
		return true;
	}
	
	/**
	 * Defines the actions for the menu items.
	 */ 
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		    case R.id.action_edit_mode:
		    	//toggles edit mode on/off
		    	if(!inEditMode)
		    	{
			    	inEditMode = true;
			    	updateOfflineFilenameListView();
		    	}
		    	else
		    	{
			    	inEditMode = false;
			    	updateOfflineFilenameListView();
		    	}
	    		return true;
		}
		return false;

	}
	
	/**
	 * Creates a list of the files stored offline and populates the spinner that displays those files.
	 */
	public void updateOfflineFilenameListView()
	{
		String path = getFilesDir().getPath();
		List<String[]> offlineFilenameAndDateList = m.getOfflineSiteFilenameAndDateList(path);
	    final ListView storedContentListView = (ListView) findViewById(R.id.storedListView);
		//String[] spinnerArray = offlineFilenameAndDateList.toArray(new String[(offlineFilenameAndDateList.size())]);
	    StoredContentAdapter adapter = new StoredContentAdapter(this, offlineFilenameAndDateList);
		storedContentListView.setAdapter(adapter);
	}
	
	/**
	 * Background web content class.
	 * Downloads html with methods from shared code.
	 * Calls html modification to make readable and displays it.
	 * Informs the user of progress.
	 * 
	 * (coppied and modified from WebDisplay)
	 */
	class webContentAsyncTask extends AsyncTask<Void, Void, Void>    {
			
		String pageURL;
		String chosenPageType;
		String fileName;
		
		public webContentAsyncTask(String url, String type, String fname)
		{
			super();
			pageURL = url;
			chosenPageType = type;
			fileName = fname;
		}
		
		/**
		 * Before: Informs the user that information is being loaded
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		} 
		
		/**
		 * During: Call html downloader shared method.
		 */
		@Override
		protected Void doInBackground(Void... arg0) {

			downloadedHtmlContent = m.getWebContent(pageURL);
			downloadedHtmlContent = m.fixHTML(downloadedHtmlContent, chosenPageType, getBaseContext());
			m.downloadErowidImages(downloadedHtmlContent, pageURL, getFilesDir().getAbsolutePath());  
			return null; 
		}   
		
		/**
		 * After: Calls the html format fixer, loads the html along with stored with css & images.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
        	deleteFile(fileName);
	    	FileOutputStream fos;
	    	 
			try {
				fos = openFileOutput(fileName, Context.MODE_PRIVATE);
		    	fos.write(downloadedHtmlContent.getBytes());
		    	fos.close();
		    	m.pingURL(fileName);
		    	Toast.makeText(getApplicationContext(), "File updated from the mother-ship ::-)", Toast.LENGTH_SHORT).show();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateOfflineFilenameListView();
		}    
	}
	
	/**
	 * Adapter class which defines how the list view interprets an assigned data source,
	 * and how that data source is presented in the rows of the listview
	 * Also defines access methods and click interactions.
	 */
	class StoredContentAdapter extends BaseAdapter {

	    Context context;
	    List<String[]> data;
	    private LayoutInflater inflater = null;

	    public StoredContentAdapter(Context context, List<String[]> data) {
	        this.context = context;
	        this.data = data;
	        inflater = (LayoutInflater) context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }

	    public void sort(Comparator<String> comparator) { }

		@Override
	    public int getCount() {
	        return data.size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return data.get(position)[0]; //returns the name of the item
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

	    /**
	     * Populates and returns a row of the list view.
	     * This includes adding action listeners update/delete buttons, available in edit mode.
	     */
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View vi = convertView;
	        if (vi == null)
	            vi = inflater.inflate(R.layout.stored_pages_row, null);
	        TextView name = (TextView) vi.findViewById(R.id.substance_name);
	        TextView page = (TextView) vi.findViewById(R.id.substance_page);
	        String[] nameParts = data.get(position)[0].split("\\|"); // 0 is type, 1 is psychoactive, 2 is chosen page
	        TextView date = (TextView) vi.findViewById(R.id.date);
	        name.setText(nameParts[1].trim());
	        page.setText(nameParts[2].trim());
	        date.setText(data.get(position)[1] );
	        String substance_type = nameParts[0].trim();
	        if(substance_type.equals("Chemicals"))
	        {
	        	vi.setBackgroundColor(0x36d0d2dd);
	        }
	        else if(substance_type.equals("Plants"))
	        {
	        	vi.setBackgroundColor(0x201b2200);
	        }
	        else if(substance_type.equals("Herbs"))
	        {
	        	vi.setBackgroundColor(0x30baa8ba);
	        }
	        else if(substance_type.equals("Pharms"))
	        {
	        	vi.setBackgroundColor(0x206a4411);
	        }
	        else if(substance_type.equals("Smarts"))
	        {
	        	vi.setBackgroundColor(0x20660000);
	        }
	        else if(substance_type.equals("Animals"))
	        {
	        	vi.setBackgroundColor(0x20261818);
	        }
	        
	        ImageButton delete = (ImageButton) vi.findViewById(R.id.delete_page_button);
	        ImageButton update = (ImageButton) vi.findViewById(R.id.update_page_button);
	        
	        if(inEditMode)
	        {
	        	delete.setVisibility(View.VISIBLE);
	        	update.setVisibility(View.VISIBLE);
	        }
	        else
	        {
	        	delete.setVisibility(View.INVISIBLE);
	        	update.setVisibility(View.INVISIBLE);
	        }
	        
	        delete.setOnClickListener(new OnClickListener() { 
	            public void onClick(View v) {
	            	LinearLayout rl = (LinearLayout)v.getParent();
	                TextView tv = (TextView)rl.findViewById(R.id.substance_name);
	                int position = storedContentListView.getPositionForView(v);
	                String fileName = (String) storedContentListView.getItemAtPosition(position);
	            	deleteFile(fileName);
                	updateOfflineFilenameListView();
	            } 
	        }); 
	        
	        update.setOnClickListener(new OnClickListener() {  
	            public void onClick(View v) {
	            	LinearLayout rl = (LinearLayout)v.getParent();
	                TextView tv = (TextView)rl.findViewById(R.id.substance_name);
	                int position = storedContentListView.getPositionForView(v);
	                String fileName = (String) storedContentListView.getItemAtPosition(position);
	            	String[] parts = fileName.split("\\|"); // 0 is type, 1 is psychoactive, 2 is chosen page
	            	String url = "http://www.erowid.org/" + parts[0].trim().toLowerCase() + "/" 
	            				+ parts[1].trim().toLowerCase() +"/" + parts[1].trim().toLowerCase() + "_" 
	            				+ parts[2].trim().toLowerCase() +".shtml";
	            	String type = parts[2].trim().toLowerCase();	  
            	    webContentAsyncTask myWebFetch = new webContentAsyncTask(url, type, fileName);
					myWebFetch.execute(); 
	            	//Toast.makeText(getBaseContext(), url, Toast.LENGTH_SHORT).show(); 
	            } 
	        }); 
	        
	        return vi;
	        
	    }
	    
	    // TODO: Implement sort functionality
	}
}
