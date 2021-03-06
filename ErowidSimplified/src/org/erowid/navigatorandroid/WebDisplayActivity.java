package org.erowid.navigatorandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

/** 
 * Activity for displaying Erowid web content.
 * Loads the specified page of the chosen psychoactive
 * chosen in the PsychoNavigatorActivity.
 * 
 * @author quartz 
 */
public class WebDisplayActivity extends Activity {
	//My variables
	static String rawHtmlContent; 
	static String chosenPageType;
	String psyType;
	String psyName;
	org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
	String pageURL; 
	WebView infoWebView;
	boolean pageFullyLoaded;
	Menu theMenu;
	boolean pageHasBeenStored = false; //has the page been stored for the app
	boolean pageFromStorage = false; //if page was pulled from storage (for offline viewing)
	
	/**
	 * Upon load, gets passed psychoactive chosen variables.
	 * Uses these to launch a background download of content (HTML, images, etc) 
	 */
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_display); 
		infoWebView = (WebView) findViewById(R.id.infoWebView);
		pageFullyLoaded = false;
		
		infoWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		infoWebView.getSettings().setTextZoom(140);
		infoWebView.loadData("Loading knowledge from web...", "text/html", null);
		
		//SEARCH FOR NAME IN EACH ONE
		
		//TODO: Retry this scroll fix.
		
		// This disables all scrolling, I just wanted to disable horizontal
		// The other fix doesn't seem to work in 4.4+ (or with my code)
		// http://stackoverflow.com/questions/2527899/disable-scrolling-in-webview
				
		//		infoWebView.setOnTouchListener(new View.OnTouchListener() {
		//
		//			@Override
		//		    public boolean onTouch(View v, MotionEvent event) {
		//				infoWebView.scrollTo(0, (int)event.getY());
		//				return true;
		////				if(event.getAction() == MotionEvent.ACTION_MOVE)
		////				{
		////
		////				}
		////				else
		////				{
		////					return (event.getAction() == MotionEvent.ACTION_MOVE);
		////				}
		//		    }
		//
		//
		//
		//		  });
		
		
		if(android.os.Build.VERSION.SDK_INT < 19)
		{
			//older, can use normal built in web controls, so disable the view buttons.
			LinearLayout zoomButtonLinearLayout = (LinearLayout) findViewById(R.id.zoomButtonLinearLayout);
			zoomButtonLinearLayout.setVisibility(View.GONE);
			infoWebView.getSettings().setBuiltInZoomControls(true);
			infoWebView.getSettings().setDisplayZoomControls(true);
			//View bottomLineView = (View) findViewById(R.id.bottomLineView);
			//bottomLineView.setVisibility(View.GONE);

			//for 4.4, I set up external buttons for zoom in/out. also no pinch zoom.
			//I'm sure there is some way around this, but it might be horribly obnoxious and I don't care, at least now.
			//Tried using this fix and couldn't work it. http://stackoverflow.com/questions/19986305/no-more-text-reflow-after-zoom-in-kitkat-webview/20000193#20000193			
		}

		//I don't see the point of this check seeing as the build target is 19, but I guess this makes it downgradable if needed.
		if (android.os.Build.VERSION.SDK_INT > 9) { StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); StrictMode.setThreadPolicy(policy); }
		 
		copyAssets( "webview/includes", "includes/");
		
		//Pulls the page chosen from the psychonavigator. Handles both online and offline pages
		Bundle extras = getIntent().getExtras();  
		if (extras != null) {
			if(extras.containsKey("STORED_PAGE"))
			{ //if launching a stored page, get and launch
				pageFromStorage = true;
				String storedPageString = extras.getString("STORED_PAGE");
				String[] separated = storedPageString.split("\\|");
				psyType = separated[0].trim();
				psyName = separated[1].trim();
				chosenPageType = separated[2].trim();
				openOfflinePage(storedPageString);
			}
			else
			{ //else get normal page content
				psyType = extras.getString("SENT_PSY_TYPE");
				psyName = extras.getString("SENT_PSYCHOACTIVE");
				chosenPageType = extras.getString("SENT_PAGE");
			    pageURL = "http://www.erowid.org/" + psyType + "/" + psyName +"/" + psyName + "_"+chosenPageType+".shtml";

				webContentAsyncTask myWebFetch = new webContentAsyncTask();
				myWebFetch.execute(); 
			}
			ActionBar actionBar = getActionBar();
			actionBar.setTitle("Substance Info");
			actionBar.setSubtitle(WordUtils.capitalize(psyName.replaceAll("_", " ") + " - " + chosenPageType));
		}
		else
		{	//create a default. Use when debugging.
//			chosenPageType = "basics";
//			pageURL = "http://www.erowid.org/plants/mushrooms/mushrooms_basics.shtml";
//
//			webContentAsyncTask myWebFetch = new webContentAsyncTask();
//			myWebFetch.execute(); 
		}
		

	}    
	
	@Override
	 public boolean onSearchRequested() {
		if(theMenu != null)
		{
			MenuItem searchMenuItem = theMenu.findItem(R.id.action_search);
			searchMenuItem.expandActionView();
		}
		return false; // don't go ahead and show the search box
	}
	
	/**
	 * Switch case for choices in the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_load_in_browser:
	    	if(!pageFromStorage)
	    	{
		    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pageURL));
		    	startActivity(browserIntent);
		        return true; 
	    	}
	    case R.id.action_store_page_offline:
	    	if(pageFullyLoaded && !pageFromStorage)
	    	{
	    		String FILENAME = m.capitalize(psyType) + " | " + m.capitalize(psyName) + " | " + m.capitalize(chosenPageType); //change other define
	    		if(!pageHasBeenStored)
		    	{
			    	FileOutputStream fos;
			    	 
					try {
						fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
				    	fos.write(rawHtmlContent.getBytes());
				    	fos.close();
				    	m.pingURL(FILENAME);
				    	Toast.makeText(getApplicationContext(), "Page stored for offline use", Toast.LENGTH_SHORT).show();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MenuItem searchMenuItem = theMenu.findItem(R.id.action_store_page_offline);
					searchMenuItem.setIcon(R.drawable.ic_action_important);
					pageHasBeenStored = true;
		    	}
		    	else
		    	{
					//actually delete page from storage here
		    		deleteFile(FILENAME);
		    		MenuItem searchMenuItem = theMenu.findItem(R.id.action_store_page_offline);
					searchMenuItem.setIcon(R.drawable.ic_action_not_important);
					Toast.makeText(getApplicationContext(), "Page deleted from offline storage", Toast.LENGTH_SHORT).show();
					pageHasBeenStored = false;
		    	}
				

				//pageIsStored
				
	    	}
	    	else
	    	{
	    		Toast.makeText(getApplicationContext(), "Wait for the page to load", Toast.LENGTH_SHORT).show();
	    	}

	    	
	        return true;
	        
//// Debug code for extra options with page saving
//	    case R.id.clear_page_test: 
//	    	//infoWebView2.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
//	    	String filesPath2 =  "file://" + getFilesDir().getAbsolutePath()+"/"; //this path loads images right, but this one does css "file:///android_asset/webview/"
//	    	infoWebView.loadDataWithBaseURL(filesPath2 , "", "text/html", "UTF-8", null);
//	    	return true;
//	    case R.id.test_offline_page:
//	    	String OPEN_FILENAME = psyType + "/" + psyName + "/" + chosenPageType;
//	    	openOfflinePage(OPEN_FILENAME);
	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

 public void openOfflinePage(String filename)
 {
 	try {
 		infoWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
 		FileInputStream fis = openFileInput(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		        sb.append(line);
		    }
		    inputStreamReader.close();
		    String filesPath =  "file://" + getFilesDir().getAbsolutePath()+"/"; //this path loads images right, but this one does css "file:///android_asset/webview/"
			infoWebView.loadDataWithBaseURL(filesPath , sb.toString(), "text/html", "UTF-8", null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 }

    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            // Do something
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            //Toast.makeText(, "Please search by selecting a hinted item", Toast.LENGTH_SHORT);

            // Do something
            return true;
        }
    };

	/**
	 * Adds the search bar to the top of the page and makes it active
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		theMenu = menu;
		MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.menu_web_display, menu);
		
		 // Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));
	    
	    if(!pageFromStorage)
	    {
			List psyList = m.getOfflineSiteFilenameList(getFilesDir().getPath());
			String testname = m.capitalize(psyType) + " | " + m.capitalize(psyName) + " | " + m.capitalize(chosenPageType); //change other define
			for (int i = 0; i < psyList.size(); i++)
			{
				if (psyList.get(i).equals(testname))
				{ 
					MenuItem offlineMenuItem = theMenu.findItem(R.id.action_store_page_offline);
					offlineMenuItem.setIcon(R.drawable.ic_action_important);
					pageHasBeenStored = true;
					//drawable/ic_action_not_important
					//+id/action_store_page_offline
					//set star to starred
				}
			}
		}
	    else
	    {
	    	MenuItem storeMenuItem = theMenu.findItem(R.id.action_store_page_offline);
	    	storeMenuItem.setVisible(false);
	    	storeMenuItem.setEnabled(false);
	    	MenuItem searchMenuItem = theMenu.findItem(R.id.action_search);
	    	searchMenuItem.setVisible(false);
	    	searchMenuItem.setEnabled(false);
	    	MenuItem loadMenuItem = theMenu.findItem(R.id.action_load_in_browser);
	    	loadMenuItem.setVisible(false);
	    	loadMenuItem.setEnabled(false);
	    }

		return true;		
	}  

	/**
	 * Background web content class.
	 * Downloads html with methods from shared code.
	 * Calls html modification to make readable and displays it.
	 * Informs the user of progress.
	 * 
	 * (This is almost duplicated in the stored content manager.)
	 */
	class webContentAsyncTask extends AsyncTask<Void, Void, Void>    {
			
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

			rawHtmlContent = m.getWebContent(pageURL);
			return null;
		}   
		
		/**
		 * After: Calls the html format fixer, loads the html along with stored with css & images.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			//Gets the html to be fixed, depending on the type of page loaded.
			rawHtmlContent = m.fixHTML(rawHtmlContent, chosenPageType, getBaseContext());
			m.downloadErowidImages(rawHtmlContent, pageURL, getFilesDir().getAbsolutePath());  
			
			//Tells the web code where to look for css & images which are stored locally.
			String filesPath =  "file://" + getFilesDir().getAbsolutePath()+"/"; //this path loads images right, but this one does css "file:///android_asset/webview/"
			
			infoWebView.loadDataWithBaseURL(filesPath , rawHtmlContent, "text/html", "UTF-8", null);
			pageFullyLoaded = true;
			 
		}    
	}
	
	/**
	 * For copying content from the assets folder.
	 * Most of the code based on: http://stackoverflow.com/questions/4447477/
	 */
	private void copyAssets(String assetPath, String relStoragePath) {
	    AssetManager assetManager = getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list(assetPath);
	        File outPath = new File(getFilesDir().getAbsolutePath() + "/" +relStoragePath);
	        outPath.mkdirs();
	    } catch (IOException e) {
	        //Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open(assetPath + "/" + filename);
	          File outFile = new File(getFilesDir().getAbsolutePath() + "/" +relStoragePath, filename);
	          outFile.delete(); //kept for safety, was getting "this file is a folder" errors. I think it was due to an error during dev.
	          out = new FileOutputStream(outFile);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(IOException e) {
	            //Log.e("tag", "Failed to copy asset file: " + filename, e);
	        	System.out.println("Fail: " + e.getMessage());
	        }       
	    }
	}
	
	
	/**
	 * Helper for copying files from assets to internal memory. Surprised this has to be done this low level.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	// UI listeners
	public void zoomOutButton_onClick(View v) {
		int zoom = infoWebView.getSettings().getTextZoom();
		if(zoom >= 40)
      	{
	      	zoom = zoom - 10;
	      	infoWebView.getSettings().setTextZoom(zoom);
      	}
	}
	public void zoomInButton_onClick(View v) {
		int zoom = infoWebView.getSettings().getTextZoom();
		if(zoom <= 1000)
      	{
	      	zoom = zoom + 10;
	      	infoWebView.getSettings().setTextZoom(zoom);
      	}
	}
	
}
