package com.example.erowidsimplified;
 
import java.io.File;
import java.util.ArrayList;
import java.util.List;







//import android.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
 
/** 
 * Activity for choosing Erowid web content to load
 * This also creates a big array detailing what information exists for all psychoactives
 * Also for other actions, some to be implemented in the future
 * (e.g. donation, managing cached pages)
 * 
 * @author quartz  
 */
public class MainPageActivity extends Activity {
	com.example.erowidsimplified.SharedMethods m = new com.example.erowidsimplified.SharedMethods();
	static String rawMainWebContent; //from pulling the list of psychoactives
	List<String[]> psychoactiveTable; 
	static boolean listPsyReady = false; 	//has the content been processed
	static boolean listPsyResumed = false; 	//has this page been resumed after going to another page
	static boolean searchPopulated = false; //has the search content been populated
	Menu theMenu; 
           
  
	/**
	 * On load, downloads web content for big array of psychoactive information chocies
	 * Then uses this information to populate the psychoactive choice spinners
	 * This also includes the listeners for the created spinners
	 */
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);
		
		createMenu(); //pulls web content for the spinners

		//Initializes the psychoactive type spinner
		Spinner psyTypeSpinner = (Spinner) findViewById(R.id.psyTypeSpinner); //load the psyType spinner	
		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
				R.array.psy_types_array, R.layout.my_spinner_item); // Create an ArrayAdapter using the string array and a default spinner layout
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
		psyTypeSpinner.setAdapter(typeAdapter); // Apply the adapter to the spinner
    
		//Initializes the text views
		TextView navTextView = (TextView) findViewById(R.id.navigationInstructionsTextView);
		String navString = "<b> Navigation: </b> Choose a psychoactive with the search above or the dropdowns below."; 
		navTextView.setText(Html.fromHtml(navString));
		
		TextView aboutTextView = (TextView) findViewById(R.id.aboutTextView);
		String aboutString = "<b> About: </b> Erowid is a member-supported organization providing access to reliable, "
							+ "non-judgmental information about psychoactive plants, chemicals, and related issues. <br>"
							+ "<br>" 
							+ "<i>Support accurate psychoactive information with a donation to Erowid!</i>"; 
		aboutTextView.setText(Html.fromHtml(aboutString)); 
		

		//Makes sure pushing back does not relaunch the spinner
		if(listPsyResumed == true)
		{
			listPsyReady = false;
			listPsyResumed = false;
		} 
		
		//When an item is selected for the type spinner the psychoactive spinner is populated with psychoactives of that type
		//This also contains the listener for the psychoactive spinner, which launches the psychoactive report card activity
		psyTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override 
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id) {

				String psyType = parentView.getItemAtPosition(position).toString().toLowerCase();
				if(!psyType.equalsIgnoreCase("-type-"))
				{
					//Populates the psyChoiceSpinner with values
					List<Object> psyChoiceArray =  new ArrayList<Object>();
					String psyString = "-<i>Psychoactive</i>-"; 
					psyChoiceArray.add(Html.fromHtml(psyString));
					for(int i = 0; i < psychoactiveTable.size(); i++)
					{
						//TODO: Can I replicate this error? I haven't seen it in a very long time.
						//this breaks when restoring from save. For some reason there are a bunch of null strings entries in the array.
						//will add a check, but it should be fixed in the save
						
						//this breaks if there are any nulls in the string set. Should add a check.
						if(psychoactiveTable.get(i) != null)
						{
							String psyValueType = psychoactiveTable.get(i)[getResources().getInteger(R.integer.psy_table_type)]; //this should be pulling the type of each substance chosen and checking, but something is wrong.
							if(psyValueType.equals(psyType))
							{
								psyChoiceArray.add(psychoactiveTable.get(i)[getResources().getInteger(R.integer.psy_table_name)]); // this also breaks
							}
						}
					}
					
					Spinner psyChoiceSpinner = (Spinner) findViewById(R.id.psyChoiceSpinner);
					ArrayAdapter choiceAdapter = new ArrayAdapter(getBaseContext(), R.layout.my_spinner_item, psyChoiceArray);
					choiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
					
					psyChoiceSpinner.setAdapter(choiceAdapter);

					//Makes sure changing the psychoactive type after a back navigation does not launch the psynavigator window
//					if(listPsyResumed == true)
//					{
//						listPsyReady = false;
//					} 

					

					
					
					//When a psychoactive is chosen, the psyNavigator activity is launched with options for the chosen psychoactive
					psyChoiceSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
					{
						@Override
						public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
								int position, long id) {
							
							//checks to make sure the list is ready and the chosen psychoactive is a real one.
							if(listPsyReady && !parentView.getItemAtPosition(position).toString().equalsIgnoreCase("-psychoactive-") && !parentView.getItemAtPosition(position).toString().equalsIgnoreCase(" "))
							{

								//Makes sure pushing back does not relaunch the spinner
//								if(listPsyResumed == true)
//								{
//									listPsyReady = false;
//									listPsyResumed = false;
//								} 
								
								//Toast.makeText(getBaseContext(), parentView.getItemAtPosition(position).toString().toLowerCase() +"Ready:" + listPsyReady + " Resumed:" + listPsyResumed + " " + parentView.getItemAtPosition(position).toString().toLowerCase() , Toast.LENGTH_LONG).show();	
								if (m.isOnline(getBaseContext()))
								{
									Intent intent = new Intent(getBaseContext(),PsychoNavigatorActivity.class);
									intent.putExtra("SENT_PSYCHOACTIVE", parentView.getItemAtPosition(position).toString().toLowerCase());
									startActivity(intent);
								} 
								else
								{ 
									Toast.makeText(getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
								}
							}
							else
							{	//first time through (upon population) search should not happen
								listPsyReady = true;
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) { /**do nothing*/ }
					}); 
				}
				else
				{
					List<String> psyChoiceArray =  new ArrayList<String>();
					psyChoiceArray.add(" ");
					Spinner psyChoiceSpinner = (Spinner) findViewById(R.id.psyChoiceSpinner);
					ArrayAdapter choiceAdapter = new ArrayAdapter(getBaseContext(), R.layout.my_spinner_item, psyChoiceArray);
					choiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
					psyChoiceSpinner.setAdapter(choiceAdapter);
				}

			}
			// end inner (psyName) listener
			
			//Required to override this method 
			@Override
			public void onNothingSelected(AdapterView<?> arg0) { 
				 /**do nothing*/ 
			}
			
		}); //end outer (psyType) listener
	} 

	/**
	 * When the spinners are used to navigate, and then the back button is used to return
	 * The variable to prevent the spinner from initially launching is set wrong
	 * This makes sure the spinners don't act up.
	 */
	@Override
	protected void onPause() {
		listPsyResumed = true;
		super.onPause();
	}

	/**
	 * Resets the spinner
	 * For some reason the type spinner won't reset, but its not necessary anyways, and might even be preferred.
	 */
	@Override
	protected void onResume()
	{ 
		Spinner psyChoiceSpinner = (Spinner) findViewById(R.id.psyChoiceSpinner);
		Spinner psyTypeSpinner = (Spinner) findViewById(R.id.psyChoiceSpinner);
		psyChoiceSpinner.setSelection(0);
		psyTypeSpinner.setSelection(0);
		super.onResume();
	}

	/**
	 * Adds the search bar to the top of the page and makes it active
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		theMenu = menu;
		if(psychoactiveTable != null)
		{
			populateSearchLater(theMenu);
		}
		return true;		 
	}
 
	/**
	 * Defines the actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		    case R.id.action_stored_manager:
				Intent intent = new Intent(getBaseContext(),StoredContentManagerActivity.class);
				startActivity(intent);
		        return true;
		    case R.id.action_reload_list:
		    	Toast.makeText(getBaseContext(), "Downloading updated list of psychoactives and restarting. Please wait..." , Toast.LENGTH_LONG).show();
		    	webContentAsyncTask myWebFetch = new webContentAsyncTask();
		    	myWebFetch.execute();
		        return true;
		    // case R.id.action_about:
			//	Intent intent2 = new Intent(getBaseContext(),AboutPageActivity.class);
			//	startActivity(intent2);
		    //    return true;
		}
		return false;

	}

	/**
	 * This is to be called to populate the search bar for the menu
	 * It is called after the psyName table has been populated, otherwise it will break. 
	 * @param menu the menu which holds the search bar
	 */
	public void populateSearchLater(Menu menu)
	{
		if(!searchPopulated)
		{		
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu_main, menu);
	
			// Associate searchable configuration with the SearchView
			SearchManager searchManager =
					(SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView =
					(SearchView) menu.findItem(R.id.action_search).getActionView();
			searchView.setSearchableInfo( 
					searchManager.getSearchableInfo(getComponentName()));
			searchPopulated = true;
			Spinner psyTypeSpinner = (Spinner)findViewById(R.id.psyTypeSpinner);
			psyTypeSpinner.setVisibility(View.VISIBLE);
			Spinner psyChoiceSpinner = (Spinner)findViewById(R.id.psyChoiceSpinner);
			psyChoiceSpinner.setVisibility(View.VISIBLE);
		}
		else
		{	//this actually restarts the application to use the updated list.
			//TODO: (is this true?) Currently the toast does not work		
			Intent mStartActivity = new Intent(getBaseContext(), MainPageActivity.class);
			mStartActivity.putExtra("PAGE_UPDATED", true);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
		}	
	}
				
	/** Accesses the Erowid Substance main page (http://www.erowid.org/general/)
	 * Parses the html to fill a list of substances to choose from
	 */ 
	private void createMenu() 
	{ 
		psychoactiveTable = m.getStoredPsyList(getBaseContext());
		
		if(psychoactiveTable == null)
		{   
			if(m.isOnline(this))
			{	   
				//if online but no stored table    
				Button reloadButton = (Button)findViewById(R.id.reloadButton);
				reloadButton.setVisibility(View.GONE);
				TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView); 
				loadingTextView.setVisibility(View.VISIBLE);
				webContentAsyncTask myWebFetch = new webContentAsyncTask();
				myWebFetch.execute();
			} 
			else
			{
				Toast.makeText(getBaseContext(), "Connect to internet and click 'Load Psychoactives'" , Toast.LENGTH_LONG).show();
			}      
		}
		else 
		{   //if there is a table and we are online
			Button reloadButton = (Button)findViewById(R.id.reloadButton);
			reloadButton.setVisibility(View.GONE);
			TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView); 
			loadingTextView.setVisibility(View.GONE);
			Spinner psyTypeSpinner = (Spinner)findViewById(R.id.psyTypeSpinner);
			psyTypeSpinner.setVisibility(View.VISIBLE);
			Spinner psyChoiceSpinner = (Spinner)findViewById(R.id.psyChoiceSpinner);
			psyChoiceSpinner.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Background web content class.
	 * Downloads html with methods from shared code.
	 * Parses the html to the big list of all psychoactive information on erowid
	 * Informs user of the progress
	 */
	class webContentAsyncTask extends AsyncTask<Void, Void, Void>    {
		@Override
		protected void onPreExecute() {
			super.onPreExecute(); 
		} 

		//In here I need to do my actual web call
		@Override
		protected Void doInBackground(Void... arg0) {
			rawMainWebContent = m.getWebContent("http://www.erowid.org/general/");
			return null;
		}   

		/**
		 * Takes the downloaded web content and parses it.
		 * Uses the parsed content to populate big arraylist of all psychoactives.
		 * This list is then stored for future use.
		 * Consult the http://www.erowid.org/general/ page for its formatting.
		 * 
		 * Note: I do apologize, this method is pretty crude and convoluted. 
		 * I should have used sophisticaed parsing, but this works well as long at the page formatting does not change.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			psychoactiveTable = new ArrayList<String[]>();
			
			List<String> psychoactives = new ArrayList<String>();
			String[] psyTypes = rawMainWebContent.split( "<div class='h8'>" ); //div..h8 is used for each psychoactive type
			for(int i = 0; i < psyTypes.length; i++)
			{
				String[] psychoactivesOfType = psyTypes[i].split("<td class=\"subname\">"); //td..subname comes before each psychoactive
				for(int j = 1; j < psychoactivesOfType.length; j++) //j = 1 weeds out initial non-subname, which is junk (I don't remember what)
				{
					String[] currentPsychoactive = psychoactivesOfType[j].split( "<th>" ); //raw content for the current psychactive
					String[] pDown = new String[currentPsychoactive.length+2]; //the row about the current psychoactive which will be added to the table
					if(currentPsychoactive.length > 1)
					{
						if(currentPsychoactive[0].contains("<a href="))
						{	//if the psychoactive has a main page to link to pull correctly
							//TODO: These offsets are messy and problem-prone. They do work in a stable manner currently.
							pDown[0] = currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("\">")+2,currentPsychoactive[0].indexOf("</a"));
							pDown[1] = "0";
							pDown[2] =  currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("<a")+10,currentPsychoactive[0].indexOf("/"+pDown[0]));
							pDown[0] = pDown[0].replaceAll("_", " ");
						}
						else
						{	//if the psychoactive does not have a main page, also pull correctly
							pDown[0] = currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("\">")+1,currentPsychoactive[0].indexOf("</td>")); //these + checks are so sketch
							pDown[1] = "0";
							pDown[2] = psychoactivesOfType[j].substring(psychoactivesOfType[j].indexOf("<a")+10,psychoactivesOfType[j].indexOf("/"+pDown[0]));							
							pDown[0] = pDown[0].replaceAll("_", " ");
						}
						psychoactives.add(pDown[0]);

						//Adds all the pages to the row. 
						//If there is a space character (&nbsp;), there is not a page and the cell is set to 0 (false)
						for(int k = 1; k < currentPsychoactive.length; k++)
						{
							if(currentPsychoactive[k].contains("&nbsp;"))
							{
								pDown[k+2] = "0";
							} 
							else
							{
								pDown[k+2] = "1";
							}
						}
						psychoactiveTable.add(pDown);
					}
				}
			}
			m.storePsyList(psychoactiveTable, getBaseContext()); 
			TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView); 
			loadingTextView.setVisibility(View.GONE);
			populateSearchLater(theMenu);
		}  
	} //End webContentAsyncClass

	/** 
	 * UI Definitions 
	 */
	public void donateButton5_onClick(View v) {
		String url = "https://www.paypal.com/xclick/business=donations@erowid.org&amount=5.00&item_name=Erowid%20Center%20Donation%20Drive";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	public void donateButton10_onClick(View v) {
		String url = "https://www.paypal.com/xclick/business=donations@erowid.org&amount=10.00&item_name=Erowid%20Center%20Donation%20Drive";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i); 
	}
	public void donateButton25_onClick(View v) {
		String url = "https://www.paypal.com/xclick/business=donations@erowid.org&amount=25.00&item_name=Erowid%20Center%20Donation%20Drive";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i); 
	}

	public void reloadButton_onClick(View v) {
		createMenu();
	}

	public class MySpinner extends Spinner {
		OnItemSelectedListener listener;

		public MySpinner(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setSelection(int position) {
			super.setSelection(position);
			if (listener != null)
				listener.onItemSelected(null, null, position, 0);
		}

		public void setOnItemSelectedEvenIfUnchangedListener(
				OnItemSelectedListener listener) {
			this.listener = listener;
		}
	}
}
