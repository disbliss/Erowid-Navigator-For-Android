package org.erowid.erowidnavigator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
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

public class PsychoNavigatorActivity extends Activity {

	org.erowid.erowidnavigator.SharedMethods m = new org.erowid.erowidnavigator.SharedMethods();
	String psyName;
	String psyType;
	Menu theMenu;
	String pagesStoredStatus;
	int pagesCanBeHeld = 0;
	boolean isResumed = false;
	List<String> possiblePageTypes = new ArrayList<String>();
	String downloadedHtmlContent = "";
	Boolean pullingReportCardText = false; 
	
	/**
	 * On creation, start UI and handle incoming intents
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_psycho_navigator);

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

	/**
	 * Handles the navigation intent from the main page, loading the sent psychoactive.
	 * This is defined in android manifest (look for meta-data, its unobvious)
	 * TODO: Fix this documentation
	 * 
	 * @param intent
	 */
	private void handleIntent(Intent intent) { 
		if (true) { //removed check to allow main page dropdown //Intent.ACTION_SEARCH.equals(intent.getAction())

			String tempPsyName;
			if(intent.getStringExtra(SearchManager.QUERY) != null)
			{ //if search term
				tempPsyName = intent.getStringExtra(SearchManager.QUERY); // seems to have some junk
			}
			if(intent.getStringExtra("SENT_PSYCHOACTIVE") != null)
			{ //if main page dropdown
				tempPsyName = intent.getStringExtra("SENT_PSYCHOACTIVE");
			}
			else
			{ // if selected search hint
				tempPsyName = intent.getDataString();
				//tempPsyName = "cannabis";
			}
			List<String[]> psyTable = m.getStoredPsyChoicesList(getBaseContext());
			boolean searchFound = false;
			//iterate through the table and find the row for the chosen psychactive, then use info to pull web content.
			for(int i = 0; i < psyTable.size(); i++)
			{
				if(psyTable.get(i)[0].equals(tempPsyName))
				{
					psyName = tempPsyName.replaceAll(" ", "_");
					psyType = psyTable.get(i)[2];
					
					ActionBar actionBar = getActionBar();
					actionBar.setTitle("Substance Info");
					actionBar.setSubtitle(psyName);
					
					initializeButtons(psyTable.get(i)[0], i, psyTable);
					String urlForGrab = "http://erowid.org/"+ psyType + "/" + psyName + "/";
					String psyType = psyTable.get(i)[getResources().getInteger(R.integer.psy_table_type)];
					webContentAsyncTask myWebFetch = new webContentAsyncTask(urlForGrab, psyType, psyName);
					myWebFetch.execute();
					searchFound= true;
					break;
					
				} 
			}  
			if(!searchFound)
			{   //If the navigation came through the actual search, instead of the hints, it is due to a bug in the user's keyboard's IME implementation
				//This is pretty rare, and my code does as it should, so I am handling this in a less-than-graceful manner.
				//May be more often than thought if a lot of users have badly implemented custom keyboards.
				Toast.makeText(getBaseContext(),"This is a search bug. You should not be able to push enter, only select hints. Please choose a hint from the search to get a result.", Toast.LENGTH_LONG).show();
				Intent intentz = new Intent(getBaseContext(), MainPageActivity.class);
				intentz.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // to prevent back navigation to the old intent after launch
				startActivity(intentz);
			}
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


		if(pagesCanBeHeld == 0)
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
		else if(pageCount >= pagesCanBeHeld) //all pages saved. Should never be greater, but might as well react elegantly.
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
		    	
		    	if(pageCount >= pagesCanBeHeld)//full, make empty
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
	 * Handles the button generation, which is dynamic depending on the content available for the psychoactive.
	 * TODO: Passing the psyTable is probably overkill. I'm not sure if its that big though.
	 * 
	 * @param item - the chosen psychoactive.
	 * @param psyPos - the position of the psychoactive in the table
	 * @param psychoactiveTable - the table containing all psychoactives.
	 */
	private void initializeButtons(final String item, int psyPos, List<String[]> psychoactiveTable)
	{
		Button basicsButton = (Button) findViewById(R.id.basics_button); 		//04 in array
		Button effectsButton = (Button) findViewById(R.id.effects_button); 		//10 in array
		Button imagesButton = (Button) findViewById(R.id.images_button); 		//05 in array
		Button healthButton = (Button) findViewById(R.id.health_button);		//13 in array
		Button lawButton = (Button) findViewById(R.id.law_button);				//06 in array
		Button doseButton = (Button) findViewById(R.id.dose_button);			//07 in array
		Button chemistryButton = (Button) findViewById(R.id.chemistry_button);	//11 in array
		Button researchChemicalButton = (Button) findViewById(R.id.research_chemical_button);	//not populated from general...
		researchChemicalButton.setVisibility(View.INVISIBLE); //because its not being populated now

		TextView psychoName = (TextView) findViewById(R.id.psychoName);
		
		psychoName.setText(Html.fromHtml("<b>"+ item.replaceAll("_", " ") + "</b>"));
		
		//For each button, check the table to see if it should be visible
		if(psychoactiveTable.get(psyPos)[4].equals("0")) {
			basicsButton.setVisibility(View.INVISIBLE); }
		else{
			basicsButton.setVisibility(View.VISIBLE);
			possiblePageTypes.add("basics");
			pagesCanBeHeld++;
			}
		if(psychoactiveTable.get(psyPos)[10].equals("0") || psyType.equals("smarts")) {
			effectsButton.setVisibility(View.INVISIBLE); }
		else{
			effectsButton.setVisibility(View.VISIBLE); 
			possiblePageTypes.add("effects");
			pagesCanBeHeld++;}
		
		if(psychoactiveTable.get(psyPos)[05].equals("0")) {
			imagesButton.setVisibility(View.INVISIBLE); }
		else{
			imagesButton.setVisibility(View.VISIBLE);
			//possiblePageTypes.add("images");
			//pagesCanBeHeld++; //images page isn't stored
			}
		if(psychoactiveTable.get(psyPos)[13].equals("0") || psyType.equals("pharms") || psyType.equals("smarts")) {
			healthButton.setVisibility(View.INVISIBLE); }
		else{
			healthButton.setVisibility(View.VISIBLE); 
			possiblePageTypes.add("health");
			pagesCanBeHeld++;}
		
		if(psychoactiveTable.get(psyPos)[6].equals("0")) {
			lawButton.setVisibility(View.INVISIBLE); }
		else{
			lawButton.setVisibility(View.VISIBLE);
			possiblePageTypes.add("law");
			pagesCanBeHeld++;}
		
		if(psychoactiveTable.get(psyPos)[7].equals("0")) {
			doseButton.setVisibility(View.INVISIBLE); }
		else{
			doseButton.setVisibility(View.VISIBLE);
			possiblePageTypes.add("dose");
			pagesCanBeHeld++;}
		
		
		//not used currently
		if(psychoactiveTable.get(psyPos)[11].equals("0")) {
			chemistryButton.setVisibility(View.INVISIBLE); }
		else{
			chemistryButton.setVisibility(View.VISIBLE); }

		//Set all click listeners.
		basicsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{
					if (m.isOnline(getBaseContext()))
					{
						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
						intent.putExtra("SENT_PSY_TYPE", psyType);
						intent.putExtra("SENT_PAGE", "basics");
						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		effectsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{ 
					if (m.isOnline(getBaseContext()))
					{
						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
						intent.putExtra("SENT_PSY_TYPE", psyType);
						intent.putExtra("SENT_PAGE", "effects");
						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		imagesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { 
				if(item != null)
				{
					if (m.isOnline(getBaseContext()))
					{	
						//This loads in the browser instead of through the internal webview, because the images page requires clicking links
						//This could be enabled internally in the future, but there isn't a lot of gain without writing more parsing code
						//to make the images more accessible.
						String pageURL = "http://erowid.org/"  + psyType + "/" + psyName + "/" + psyName + "_images.shtml";
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pageURL));
				    	//"http://erowid.org/" + psyType + "/" + psyName + "/images/" + psyName
				    	startActivity(browserIntent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		healthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{
					if (m.isOnline(getBaseContext()))
					{
						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
						intent.putExtra("SENT_PSY_TYPE", psyType);
						intent.putExtra("SENT_PAGE", "health");
						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			} 
		}); 
		lawButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{
					if (m.isOnline(getBaseContext()))
					{
						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
						intent.putExtra("SENT_PSY_TYPE", psyType);
						intent.putExtra("SENT_PAGE", "law");
						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		doseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{
					if (m.isOnline(getBaseContext()))
					{
						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
						intent.putExtra("SENT_PSY_TYPE", psyType);
						intent.putExtra("SENT_PAGE", "dose");
						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		chemistryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(item != null)
				{  
					if (m.isOnline(getBaseContext()))
					{
						//This loads in the browser instead of through the internal webview, because the formatting was broken.
						//Could be changed later
						String pageURL = "http://erowid.org/"  + psyType + "/" + psyName + "/" + psyName + "_chemistry.shtml";
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pageURL));
				    	//"http://erowid.org/" + psyType + "/" + psyName + "/images/" + psyName
				    	startActivity(browserIntent);

						//Old code for loading chemistry internally. 
				    	
//						Intent intent = new Intent(getBaseContext(), WebDisplayActivity.class);
//						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
//						intent.putExtra("SENT_PSY_TYPE", psyType);
//						intent.putExtra("SENT_PAGE", "chemistry");
//						startActivity(intent);
					}
					else
					{ 
						Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	} // End setting click listeners
	
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
	
	
	//Web Content population class
	class webContentAsyncTask extends AsyncTask<Void, Void, Void>    {
		//Defining text pulled from the web to be displayed to the user.
		String description = "";
		String effects = "";
		String chemical_name = "";
		String caution = "";
		String common_names = "";
		
		//Other variables
		String url = ""; 
		String psyType = "";
		String psyName = "";
		WebView infoWebView = (WebView) findViewById(R.id.infoWebView);		
		
		webContentAsyncTask(String passedUrl)    { 
			url= passedUrl;           
		} 

		webContentAsyncTask(String passedUrl, String pType, String pName )    { 
			url = passedUrl;
			psyType = pType;
			psyName = pName;
		} 
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			TextView psyDescription = (TextView) findViewById(R.id.psychoactiveDescription); 
			psyDescription.setText("Loading...");
		}  

		/**
		 * This contains the actual webCall takes its returned content to populate strings for use in the view.
		 */
		@Override 
		protected Void doInBackground(Void... arg0) {
			pullingReportCardText = true;
			Map<String, String> reportCard = m.getReportCardInfo(url); //calls a shared method that grabs info needed from the report card page.
			description = reportCard.get("Description");
			effects = reportCard.get("Effects"); //this messes up formatting
			chemical_name = reportCard.get("Chemical Name");
			caution = reportCard.get("Caution");
			common_names = reportCard.get("Common Names");

			//this pulls the summary jpg and resizes it for use.
			//TODO: make sure this works at different page sizes.
			try {
				URL psyImageUrl = new URL("http://erowid.org/" + psyType + "/" + psyName + "/images/" + psyName +"_summary1.jpg");
				final Bitmap psyImageBitmap = BitmapFactory.decodeStream(psyImageUrl.openConnection().getInputStream());
				final ImageView psyImageView = (ImageView) findViewById(R.id.psyImage);
				runOnUiThread(new Runnable() {
				     @Override
					public void run() {
				    	 int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
				    	 int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 225, getResources().getDisplayMetrics());
						 psyImageView.getLayoutParams().height = height;
						 psyImageView.getLayoutParams().width = width;
						 //psyImageView.requestLayout();
				    	 psyImageView.setImageBitmap(psyImageBitmap);
				    	 //calculating the dpi for the image height/width once we know we have an image.
				    	 //not based on attributes of 
				    }
				});
				
				System.out.println("Image Grab Finishes");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				//Happens if no image
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Generates the scrolling text box content.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			//Create the scrolling textbox containing text parsed out of the web content.
			TextView psyDescription = (TextView) findViewById(R.id.psychoactiveDescription); 
			
			if(!psyType.equalsIgnoreCase("herbs"))
			{
				psyDescription.setText(Html.fromHtml(
						"<b>Effects Classification:</b><br>" + effects + "<br/>" + 
						"<b>Description:</b><br>" + description + "<br/>" + 
						"<b>Common Names:</b><br>" + common_names ));
			}
			else
			{ //herbs does not have effects class, so don't show.
				psyDescription.setText(Html.fromHtml(
						"<b>Description:</b><br>" + description + "<br/>" + 
						"<b>Common Names:</b><br>" + common_names ));
			}
			psyDescription.setMovementMethod(new ScrollingMovementMethod()); 		  
			pullingReportCardText = false;
		}  
	}
}
