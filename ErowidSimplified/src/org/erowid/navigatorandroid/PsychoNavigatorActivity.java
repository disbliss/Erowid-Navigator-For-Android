package org.erowid.navigatorandroid;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.erowid.navigatorandroid.fragments.PsyReportCardFragment;
import org.erowid.navigatorandroid.fragments.PsyWebResourcesFragment;
import org.erowid.navigatorandroid.xmlXstream.Substance;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;


public class PsychoNavigatorActivity extends FragmentActivity {

	org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
    //ErowidPsychoactiveVaults vault = VaultSingleton.getInstance().getVault();
	String psyName;
	String psyType;
    Substance substance;
	Menu theMenu;
	String pagesStoredStatus;
	boolean isResumed = false;
	List<String> possiblePageTypes = new ArrayList<String>();
	String downloadedHtmlContent = "";
	Boolean pullingReportCardText = false;
    //int subTypeIndex;
    //int subPosition;

    private FragmentTabHost mTabHost;

	/**
	 * On creation, start UI and handle incoming intents
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_psycho_navigator);
        setContentView(R.layout.activity_psycho_navigator_with_tabs);

		//handle incoming search info
		handleIntent(getIntent()); 
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
	 * Resets the star
	 * For some reason this happens on start
	 */
	@Override
	protected void onResume()
	{ 
		if(isResumed)
		{
			setPageSaveStar();
		}
		super.onResume(); 
	}
	
	@Override
	protected void onPause()
	{
		isResumed = true;
		super.onPause(); 
	}

	@Override
	public void onNewIntent(Intent intent) { 
		setIntent(intent);  
		handleIntent(intent); 
	}

    public Substance getSubstance()
    {
        return substance;
    }

    public String getPsyType()
    {
        return psyType;
    }

	/**
	 * Handles the navigation intent from the main page, loading the sent psychoactive.
	 * This is defined in android manifest (look for meta-data, its unobvious)
	 * TODO: Fix this documentation
	 * 
	 * @param intent
	 */
	private void handleIntent(Intent intent) { 
		if (true)
        { //removed check to allow main page dropdown //Intent.ACTION_SEARCH.equals(intent.getAction())


            if(intent.getDataString() != null)
			{ //if search term
                String[] tempDataArray = intent.getDataString().split(",");
                psyName = tempDataArray[0];
                psyType = tempDataArray[1].toLowerCase();
 //               subTypeIndex = Integer.parseInt(tempData[1]);
 //               subPosition = Integer.parseInt(tempData[2]);
                //int[] psyAndSectionId = intent.getEx
                //subTypeIndex = intent.getIntExtra("SECTION_INDEX",-1);
                //subPosition = intent.getIntExtra("SUBSTANCE_INDEX",-1);
			}
			else
			{ //if main page dropdown
                //subTypeIndex = intent.getIntExtra("SECTION_INDEX",-1);
                //subPosition = intent.getIntExtra("SUBSTANCE_INDEX",-1);
                //tempPsyName = vault.getSection().get(subTypeIndex).getSubstance().get(subPosition).getName();
                //Toast.makeText(this, "subTypeIndex " + subTypeIndex + " | subPosition " +subPosition, Toast.LENGTH_LONG);
                psyName = intent.getStringExtra("SUBSTANCE_NAME");
                psyType = intent.getStringExtra("SUBSTANCE_TYPE").toLowerCase();
			}


            //Substance substance;
            //String psyName;
            //String psyType;

//            PsyReportCardFragment psyReportCardFragment = new PsyReportCardFragment();
//            // Supply index input as an argument.
//            Bundle args = new Bundle();
//            args.putString("psyName", psyName);
//            args.putString("psyType", psyType);
//            args.put
//            psyReportCardFragment.setArguments(args);




//            psyType = vault.getSection().get(subTypeIndex).getSectionName().toLowerCase();
//            psyName = tempPsyName.replaceAll(" ", "_").toLowerCase();

            Log.d("SubFrag Test", "Substance not created");
            substance = m.getSubstanceFromXML(m.getSubXML(m.getSubstancesClassString(this), psyName)); //this still takes a while, maybe async it?
            Log.d("Substance in Nav", substance.getName());
            Log.d("SubFrag Test", "Substance Created");

            ActionBar actionBar = getActionBar();
            actionBar.setTitle("Substance Info");
            actionBar.setSubtitle(WordUtils.capitalize(psyName.replaceAll("_", " ")));


            mTabHost = (FragmentTabHost) findViewById(R.id.tabHost); //does nothing
            mTabHost.setup(this, getSupportFragmentManager(), R.id.tabFrameLayout);

            mTabHost.addTab(
                    mTabHost.newTabSpec("tab1").setIndicator("Report Card",
                            getResources().getDrawable(android.R.drawable.star_on)),
                    PsyReportCardFragment.class, null);
            mTabHost.addTab(
                    mTabHost.newTabSpec("tab2").setIndicator("Web Resources",
                            getResources().getDrawable(android.R.drawable.star_on)),
                    PsyWebResourcesFragment.class, null);


            //keeps track of the number of pages that can be saved, for the pagesavestar

            if(substance.getBasics() != null)   possiblePageTypes.add("basics");
            if(substance.getEffects() != null)  possiblePageTypes.add("effects");
            if(substance.getHealth() != null)   possiblePageTypes.add("health");
            if(substance.getLaw() != null)      possiblePageTypes.add("law");
            if(substance.getDose() != null)     possiblePageTypes.add("dose");






            //TODO: Fix this with new implementation. Probably add a check with the if/else above
//			if(!searchFound)
//			{   //If the navigation came through the actual search, instead of the hints, it is due to a bug in the user's keyboard's IME implementation
//				//This is pretty rare, and my code does as it should, so I am handling this in a less-than-graceful manner.
//				//May be more often than thought if a lot of users have badly implemented custom keyboards.
//				Toast.makeText(getBaseContext(),"This is a search bug. You should not be able to push enter, only select hints. Please choose a hint from the search to get a result.", Toast.LENGTH_LONG).show();
//				Intent intentz = new Intent(getBaseContext(), MainPageActivity.class);
//				intentz.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // to prevent back navigation to the old intent after launch
//				startActivity(intentz);
//			}

        }
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		theMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_psycho_navigator, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));

		//Toast.makeText(getBaseContext(),"available pages: " + pagesCanBeHeld, Toast.LENGTH_LONG).show();
		
		if(possiblePageTypes.size() == 0)
		{
			MenuItem offlineMenuItem = theMenu.findItem(R.id.action_store_page_offline);
			offlineMenuItem.setVisible(false);
		}

		setPageSaveStar();

		return true;		 
	}   
	
	/**
	 * Sets the star for the save page, depending on the quantity of sub-pages saved.
	 */
	public void setPageSaveStar()
	{
		int pageCount = 0;
		List psyList = m.getOfflineSiteFilenameList(getFilesDir().getPath());
		String subTestName = m.capitalize(psyType) + " | " + m.capitalize(psyName) + " | "; //change other define
//


        //Log.d("Star Test", substance.getBasics());

        for (int i = 0; i < psyList.size(); i++)
		{
			if (psyList.get(i).equals(subTestName + "Basics")
					|| psyList.get(i).equals(subTestName + "Effects")
					|| psyList.get(i).equals(subTestName + "Health")
					|| psyList.get(i).equals(subTestName + "Law")
					|| psyList.get(i).equals(subTestName + "Dose" )  )
			{
				pageCount++;
			}
		}
		//Toast.makeText(this, "pageCount: " + pageCount + " | pagesCanBeHeld: " + pagesCanBeHeld, Toast.LENGTH_LONG).show();
		
		if(pageCount == 0) //no pages saved
		{
			MenuItem offlineMenuItem = theMenu.findItem(R.id.action_store_page_offline);
			offlineMenuItem.setIcon(R.drawable.ic_action_not_important);
			pagesStoredStatus = "none";
		}
		else if(pageCount >= possiblePageTypes.size()) //all pages saved. Should never be greater, but might as well react elegantly.
		{
			MenuItem offlineMenuItem = theMenu.findItem(R.id.action_store_page_offline);
			offlineMenuItem.setIcon(R.drawable.ic_action_important);
			pagesStoredStatus = "all";
		}
		else //some pages saved
		{
			MenuItem offlineMenuItem = theMenu.findItem(R.id.action_store_page_offline);
			offlineMenuItem.setIcon(R.drawable.ic_action_half_important);
			pagesStoredStatus = "some";
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection from the menu
	    switch (item.getItemId()) {
	    case R.id.action_load_in_browser:
	    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.erowid.org/" + psyType + "/" + psyName + "/" + psyName + ".shtml"));
	    	//"http://erowid.org/" + psyType + "/" + psyName + "/images/" + psyName
	    	startActivity(browserIntent);
	        return true;
	    case R.id.action_store_page_offline:
	    	if(!pullingReportCardText)
	    	{
		    	List<String> psyOfTypeList = new ArrayList<String>();
		    	
	    		int pageCount = 0;
	    		List offlineFileList = m.getOfflineSiteFilenameList(getFilesDir().getPath());
	    		String subTestName = m.capitalize(psyType) + " | " + m.capitalize(psyName) + " | "; //change other define
	    		
	    		// goes through all the save files to see which ones are for this substance
	    		// for each one that is, add its filename to the list and increment the number counter
	    		// TODO: Remove counter
	    		for (int i = 0; i < offlineFileList.size(); i++)
	    		{
	    			String psyListName = (String) offlineFileList.get(i);
	    			if ( psyListName.equals(subTestName + "Basics")  
	    					|| psyListName.equals(subTestName + "Effects")  
	    					|| psyListName.equals(subTestName + "Health")  
	    					|| psyListName.equals(subTestName + "Law")  
	    					|| psyListName.equals(subTestName + "Dose" )  )
	    			{ 
	    				//offlineFileList.add(psyListName);
	    				pageCount++; 
	    			}
	    		}
		    	
		    	if(pageCount >= possiblePageTypes.size())//full, make empty
		    	{
		    		for(String possible : possiblePageTypes)
		    		{ //for each possible page
		    			deleteFile(subTestName + m.capitalize(possible));
			    		//MenuItem searchMenuItem = theMenu.findItem(R.id.action_store_page_offline);
						//searchMenuItem.setIcon(R.drawable.ic_action_not_important);
						//Toast.makeText(getApplicationContext(), "Page deleted from offline storage", Toast.LENGTH_SHORT).show();
						//pageHasBeenStored = false;
		    		}
		    		Toast.makeText(getBaseContext(),"Cleared saved pages", Toast.LENGTH_SHORT).show();
		    		setPageSaveStar();
		    	}
		    	else //some or empty, make full
		    	{
		    		//find ones not in psyOfTypeList and add
		    		//String[] pageTypes = {"Basics", "Effects", "Health", "Law", "Dose"};
		    		String[] pageTypes = {"basics", "effects", "health", "law", "dose"};
		    		for(String possible : possiblePageTypes)
		    		{ //for each possible page
		    			boolean typeIsStored = false;
		    			for(String stored : psyOfTypeList)
		    			{ //for each stored page
		    				if ((subTestName + possible).equals(stored))
							{ //if they match we don't do more
		    					typeIsStored = true;
		    					break;
							}
		    			}
		    			if(!typeIsStored)
		    			{
		    				//Toast.makeText(getBaseContext(),"Storing "+ possible +", only not", Toast.LENGTH_LONG).show();
		    				for(String page : pageTypes)
		    				{
		    					if(possiblePageTypes.contains(page))
		    					{
		    						//this is convoluted because it was taken from StoredContent. I seriously don't think I have to do this again, so...
		    		                String fileName = subTestName + m.capitalize(page);
		    		            	String[] parts = fileName.split("\\|"); // 0 is type, 1 is psychoactive, 2 is chosen page
		    		            	String url = "http://www.erowid.org/" + parts[0].trim().toLowerCase() + "/" 
		    		            				+ parts[1].trim().toLowerCase() +"/" + parts[1].trim().toLowerCase() + "_" 
		    		            				+ parts[2].trim().toLowerCase() +".shtml";
		    		            	String type = parts[2].trim().toLowerCase();	  
		    						
		    		            	webContentAsyncTask2 myWebFetch = new webContentAsyncTask2(url, type, fileName);
		    						myWebFetch.execute();
		    					}
		    				}
		    			}
		    		}
		    		Toast.makeText(getBaseContext(),"Pulling available pages", Toast.LENGTH_SHORT).show();
		    	}
		    	
		    	setPageSaveStar(); //this may be less efficient than just setting it again.
	    	}
	    	else
	    	{
	    		Toast.makeText(getBaseContext(),"Wait for page to load", Toast.LENGTH_SHORT).show();
	    	}
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	

	
	/**
	 * Background web content class.
	 * Downloads html with methods from shared code.
	 * Calls html modification to make readable and displays it.
	 * Informs the user of progress.
	 * 
	 * (coppied and modified from WebDisplay)
	 */
	class webContentAsyncTask2 extends AsyncTask<Void, Void, Void>    {
			
		String pageURL;
		String chosenPageType;
		String fileName;
		
		public webContentAsyncTask2(String url, String type, String fname)
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
		    	setPageSaveStar(); //this may be less efficient than just setting it again.

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//updateOfflineFilenameListView();
		}     
	}
	
	

}
