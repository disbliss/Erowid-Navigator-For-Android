package org.erowid.erowidnavigator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PsychoNavigatorActivity extends Activity {

	org.erowid.erowidnavigator.SharedMethods m = new org.erowid.erowidnavigator.SharedMethods();
	String psyName;
	String psyType;
	
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
			List<String[]> psyTable = m.getStoredPsyList(getBaseContext());
			boolean searchFound = false;
			//iterate through the table and find the row for the chosen psychactive, then use info to pull web content.
			for(int i = 0; i < psyTable.size(); i++)
			{
				if(psyTable.get(i)[0].equals(tempPsyName))
				{
					psyName = tempPsyName.replaceAll(" ", "_");
					psyType = psyTable.get(i)[2];
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_psycho_navigator, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));

		return true;		 
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
			basicsButton.setVisibility(View.VISIBLE); }
		if(psychoactiveTable.get(psyPos)[10].equals("0") || psyType.equals("smarts")) {
			effectsButton.setVisibility(View.INVISIBLE); }
		else{
			effectsButton.setVisibility(View.VISIBLE); }
		if(psychoactiveTable.get(psyPos)[05].equals("0")) {
			imagesButton.setVisibility(View.INVISIBLE); }
		else{
			imagesButton.setVisibility(View.VISIBLE); }
		if(psychoactiveTable.get(psyPos)[13].equals("0") || psyType.equals("pharms") || psyType.equals("smarts")) {
			healthButton.setVisibility(View.INVISIBLE); }
		else{
			healthButton.setVisibility(View.VISIBLE); }
		if(psychoactiveTable.get(psyPos)[6].equals("0")) {
			lawButton.setVisibility(View.INVISIBLE); }
		else{
			lawButton.setVisibility(View.VISIBLE); }
		if(psychoactiveTable.get(psyPos)[7].equals("0")) {
			doseButton.setVisibility(View.INVISIBLE); }
		else{
			doseButton.setVisibility(View.VISIBLE); }
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

		}  
	}
}
