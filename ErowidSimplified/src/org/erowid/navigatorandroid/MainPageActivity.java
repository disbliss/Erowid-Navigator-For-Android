package org.erowid.navigatorandroid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import org.apache.commons.lang3.text.WordUtils;
import org.erowid.navigatorandroid.xmlXstream.ErowidPsychoactiveVaults;
import org.erowid.navigatorandroid.xmlXstream.Substance;
//import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainPageActivity extends Activity {
	org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
	static String rawMainWebContent; //from pulling the list of psychoactives
	//List<String[]> psychoactiveTable;
	static boolean listPsyReady = false; 	//has the content been processed
	static boolean listPsyResumed = false; 	//has this page been resumed after going to another page
	static boolean searchPopulated = false; //has the search content been populated
    ListView psyTypeListView;
    ListView psyChoiceListView;
    ActionMode mActionMode;
	Menu theMenu;
    String parsedBigChart = "";
    //ErowidPsychoactiveVaults vault;
    List<String[]> vaultTable;
    //String bigChartXML;
    //int subTypeIndex;

    // for vaultTable
    int vTName = 0;
    int vTNiceName = 1;
    int vTSubType = 2;
    int vTSumImage = 3;
    int vTSumEffects = 4;
    int vTAltNameListString = 4;


//    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//    ActivityManager activityManager;

	/**
	 * On load, downloads web content for big array of psychoactive information chocies
	 * Then uses this information to populate the psychoactive choice spinners
	 * This also includes the listeners for the created spinners
	 */
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);
		
		createMenu(); //gets content for the listviews, possibly from web xml and parsing

		//Initializes the text views
		TextView navTextView = (TextView) findViewById(R.id.navigationInstructionsTextView);
		String navString = "<b> Navigation: </b> Choose a psychoactive with the search above or the dropdowns below."; 
		navTextView.setText(Html.fromHtml(navString));

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Erowid Navigator");

		//Makes sure pushing back does not relaunch the spinner
		if(listPsyResumed == true)
		{
			listPsyReady = false;
			listPsyResumed = false;
		}

        updateSubTypeListView();
        psyTypeListView = (ListView) findViewById(R.id.psyTypeListView);

        //click listener for opening saved pages
        psyTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(listPsyReady) {
                    if (mActionMode == null) {
                        String psyType = (String) psyTypeListView.getItemAtPosition(position);
                        //Toast.makeText(getBaseContext(), "You clicked " + psyType , Toast.LENGTH_LONG).show();

                        //Populates the psyChoiceSpinner with values


//                        subTypeIndex = 0;
//                        while(!vault.getSection().get(subTypeIndex).getSectionName().trim().toLowerCase().equals(psyType.toLowerCase()))
//                        {
//                            subTypeIndex++;
//                        }


                        final List<String[]> smallVaultTable = new ArrayList<String[]>();
                        for(int i = 0; i < vaultTable.size(); i++)
                        {
                            if(vaultTable.get(i)[vTSubType].equalsIgnoreCase(psyType))
                            {
                                smallVaultTable.add(vaultTable.get(i));
                            }
                        }

                        psyChoiceListView = (ListView) findViewById(R.id.psyChoiceListView);
                        SubstanceContentAdapter adapter = new SubstanceContentAdapter(getBaseContext(), smallVaultTable);
                        psyChoiceListView.setAdapter(adapter);

                        TextView psyTypeChooseTextView = (TextView) findViewById(R.id.psyTypeChooseTextView);
                        psyTypeChooseTextView.setText(psyType);
                        //psyChoiceListView.setLayoutParams(new AbsListView.LayoutParams(wrap 3));
                        psyTypeListView.setVisibility(View.GONE);
                        Button closeSubstanceListButton = (Button) findViewById(R.id.closeSubstanceListButton);
                        closeSubstanceListButton.setVisibility(View.VISIBLE);
                        psyChoiceListView.setVisibility(View.VISIBLE);

                        psyChoiceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

//                                String[] psychoactive = (String[]) psyChoiceListView.getItemAtPosition(position);

//                                Toast.makeText(getBaseContext(), psychoactive[0] , Toast.LENGTH_LONG).show();




                                if (m.isOnline(getBaseContext())) {
                                    Intent intent = new Intent(getBaseContext(), PsychoNavigatorActivity.class);
                                    //intent.putExtra("SECTION_INDEX", subTypeIndex);
                                    String psyName = smallVaultTable.get(position)[vTName];
                                    String psyType = smallVaultTable.get(position)[vTSubType];
                                    intent.putExtra("SUBSTANCE_NAME", psyName);
                                    intent.putExtra("SUBSTANCE_TYPE", psyType);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getBaseContext(), "Reconnect to the internet", Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    } else {
                        mActionMode.finish();
                        mActionMode = null;
                        view.setSelected(false);
                    }
                }
            }
        });

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

    @Override
    public void onBackPressed()
    {
        Button closeSubstanceListButton = (Button)findViewById(R.id.closeSubstanceListButton);

        //closeSubstanceListButton.getRootView();

        if(closeSubstanceListButton.getVisibility() == View.VISIBLE) {

            closeSubstanceListButton_onClick(closeSubstanceListButton.getRootView());
        }
        else {
            super.onBackPressed();
        }
    }

	/**
	 * Resets the spinner
	 * For some reason the type spinner won't reset, but its not necessary anyways, and might even be preferred.
	 */
	@Override
	protected void onResume()
	{
        Button closeSubstanceListButton = (Button)findViewById(R.id.closeSubstanceListButton);
        TextView psyTypeChooseTextView = (TextView)findViewById(R.id.psyTypeChooseTextView);
        ListView psyTypeListView = (ListView) findViewById(R.id.psyTypeListView);
        ListView psyChoiceListView = (ListView) findViewById(R.id.psyChoiceListView);

        psyTypeChooseTextView.setText("Choose a type");
        psyChoiceListView.setVisibility(View.GONE);
        closeSubstanceListButton.setVisibility(View.GONE);
        psyTypeListView.setVisibility(View.VISIBLE);

		super.onResume();
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
	 * Adds the search bar to the top of the page and makes it active
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		theMenu = menu;
		if(vaultTable != null)
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
		    	//webContentAsyncTask myWebFetch = new webContentAsyncTask();
		    	//myWebFetch.execute();
		        return true;
		    // case R.id.action_about:
			//	Intent intent2 = new Intent(getBaseContext(),AboutPageActivity.class);
			//	startActivity(intent2);
		    //    return true;
		}
		return false;

	}

//    class MySearchManager extends SearchManager
//    {
//
////        MySearchManager() {
////            super.searc();
////
////        }
//
//        @Override
//        public boolean onKeyDown(int keyCode, KeyEvent event)
//        {
//            if (keyCode==KeyEvent.KEYCODE_ENTER)
//            {
//                // Just ignore the [Enter] key
//                return true;
//            }
//            // Handle all other keys in the default way
//            return super.onKeyDown(keyCode, event);
//        }
//    }


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
            searchView.setOnQueryTextListener(queryTextListener);
			searchView.setSearchableInfo(
					searchManager.getSearchableInfo(getComponentName()));
			searchPopulated = true;
		}
		else
		{	//this actually restarts the application to use the updated list.
			Intent mStartActivity = new Intent(getBaseContext(), MainPageActivity.class);
			mStartActivity.putExtra("PAGE_UPDATED", true);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
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
		vaultTable = m.getStoredPsyChoicesList(getBaseContext());

        if(vaultTable == null || vaultTable.isEmpty())
		{   
			if(m.isOnline(this))
			{
				//if online but no stored table    
				Button reloadButton = (Button)findViewById(R.id.reloadButton);
				reloadButton.setVisibility(View.GONE);
				TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView); 
				loadingTextView.setVisibility(View.VISIBLE);

                //bigChartXML = m.getSubstancesClassString(this); //this is pretty slow
//
//                if (bigChartXML == null || bigChartXML.isEmpty())
//                {
                    webFetchBigChartAsyncTask webFetchBigChartFetch = new webFetchBigChartAsyncTask();
                    webFetchBigChartFetch.execute();
//                }
//                else
//                {
//                    processXmlToVaultAsyncTask processXmlToVaultAsyncTask = new processXmlToVaultAsyncTask();
//                    processXmlToVaultAsyncTask.execute();
//                }



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
            listPsyReady = true;
            if(!(theMenu == null)) {
                populateSearchLater(theMenu);
            }
		}
	}

    public List<String[]> createVaultTable(ErowidPsychoactiveVaults vault)
    {
       List <String[]> vaultTable = new ArrayList<String[]>();

        for(int i = 0; i < vault.getSection().size(); i++)
        {
            for (int j = 0; j < vault.getSection().get(i).getSubstance().size(); j++) {

//                List<String> tempAltNameSet = vault.getSection().get(i).getSubstance().get(j).alternateNameSet;
//                String joinedAltNames = "";
//                for(int k = 0; i < tempAltNameSet.size(); i++)
//                {
//                    joinedAltNames += tempAltNameSet.get(k) +"| ";
//                }
//                joinedAltNames.replaceAll("(\\r|\\n)", "").replaceAll("| $", "");
//                System.out.println("Names: " + joinedAltNames);
                String[] substance = {
                    "" + vault.getSection().get(i).getSubstance().get(j).getName(),                  //name
                    "" + vault.getSection().get(i).getSubstance().get(j).getNiceName(),              //nice name
                    "" + vault.getSection().get(i).getSectionName() ,                                //substance type
                    "" + vault.getSection().get(i).getSubstance().get(j).getSummaryCardImage() ,     //summary image url
                    "" + vault.getSection().get(i).getSubstance().get(j).getSummaryEffects()         //summary effects description""
                       // ,         + joinedAltNames
                };
                vaultTable.add(substance);
            }
        }
        m.storePsyChoicesList(vaultTable, this);
        return vaultTable;
    }

	/**
	 * Background web content class.
	 * Downloads html with methods from shared code.
	 * Parses the html to the big list of all psychoactive information on erowid
	 * Informs user of the progress
	 */
//	class webContentAsyncTask extends AsyncTask<Void, Void, Void>    {
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//		}
//
//		//In here I need to do my actual web call
//		@Override
//		protected Void doInBackground(Void... arg0) {
//			rawMainWebContent = m.getWebContent("http://www.erowid.org/general/");
//			return null;
//		}
//
//		/**
//		 * Takes the downloaded web content and parses it.
//		 * Uses the parsed content to populate big arraylist of all psychoactives.
//		 * This list is then stored for future use.
//		 * Consult the http://www.erowid.org/general/ page for its formatting.
//		 *
//		 * Note: I do apologize, this method is pretty crude and convoluted.
//		 * I should have used sophisticaed parsing, but this works well as long at the page formatting does not change.
//		 */
//		@Override
//		protected void onPostExecute(Void result) {
//			super.onPostExecute(result);
//			psychoactiveTable = new ArrayList<String[]>();
//
//			List<String> psychoactives = new ArrayList<String>();
//			String[] psyTypes = rawMainWebContent.split( "<div class='h8'>" ); //div..h8 is used for each psychoactive type
//			for(int i = 0; i < psyTypes.length; i++)
//			{
//				String[] psychoactivesOfType = psyTypes[i].split("<td class=\"subname\">"); //td..subname comes before each psychoactive
//				for(int j = 1; j < psychoactivesOfType.length; j++) //j = 1 weeds out initial non-subname, which is junk (I don't remember what)
//				{
//					String[] currentPsychoactive = psychoactivesOfType[j].split( "<th>" ); //raw content for the current psychactive
//					String[] pDown = new String[currentPsychoactive.length+2]; //the row about the current psychoactive which will be added to the table
//					if(currentPsychoactive.length > 1)
//					{
//						if(currentPsychoactive[0].contains("<a href="))
//						{	//if the psychoactive has a main page to link to pull correctly
//							//TODO: These offsets are messy and problem-prone. They do work in a stable manner currently.
//							pDown[0] = currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("\">")+2,currentPsychoactive[0].indexOf("</a"));
//							pDown[2] =  currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("<a")+10,currentPsychoactive[0].indexOf("/"+pDown[0]));
//						}
//						else
//						{	//if the psychoactive does not have a main page, also pull correctly
//							pDown[0] = currentPsychoactive[0].substring(currentPsychoactive[0].indexOf("\">")+1,currentPsychoactive[0].indexOf("</td>")); //these + checks are so sketch
//							pDown[2] = psychoactivesOfType[j].substring(psychoactivesOfType[j].indexOf("<a")+10,psychoactivesOfType[j].indexOf("/"+pDown[0]));
//
//						}
//                        pDown[1] = "0";
//                        pDown[0] = pDown[0].replaceAll("_", " ");
//						psychoactives.add(pDown[0]);
//
//						//Adds all the pages to the row.
//						//If there is a space character (&nbsp;), there is not a page and the cell is set to 0 (false)
//						for(int k = 1; k < currentPsychoactive.length; k++)
//						{
//							if(currentPsychoactive[k].contains("&nbsp;"))
//							{
//								pDown[k+2] = "0";
//							}
//							else
//							{
//								pDown[k+2] = "1";
//							}
//						}
//						psychoactiveTable.add(pDown);
//					}
//				}
//			}
//			m.storePsyChoicesList(psychoactiveTable, getBaseContext());
//			TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView);
//			loadingTextView.setVisibility(View.GONE);
//			populateSearchLater(theMenu);
//		}
//	} //End webContentAsyncClass

	public void reloadButton_onClick(View v) {
		createMenu();
	}

    public void closeSubstanceListButton_onClick(View v) {
        Button closeSubstanceListButton = (Button)findViewById(R.id.closeSubstanceListButton);
        TextView psyTypeChooseTextView = (TextView)findViewById(R.id.psyTypeChooseTextView);
        psyTypeChooseTextView.setText("Choose a type");

        psyChoiceListView.setVisibility(View.GONE);
        closeSubstanceListButton.setVisibility(View.GONE);
        psyTypeListView.setVisibility(View.VISIBLE);
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

    /**
     * Creates a list of the files stored offline and populates the spinner that displays those files.
     */
    public void updateSubTypeListView()
    {
        //String path = getFilesDir().getPath();
        //List<String> offlineFilenameAndDateList = m.getOfflineSiteFilenameAndDateList(path);
        final ListView storedContentListView = (ListView) findViewById(R.id.psyTypeListView);
        //String[] spinnerArray = offlineFilenameAndDateList.toArray(new String[(offlineFilenameAndDateList.size())]);

        String[] subTypes = getResources().getStringArray(R.array.psy_types_array);
        SubstanceTypeContentAdapter adapter = new SubstanceTypeContentAdapter(this, subTypes);

        storedContentListView.setAdapter(adapter);
    }

    /**
     * Adapter class which defines how the list view interprets an assigned data source,
     * and how that data source is presented in the rows of the listview
     * Also defines access methods and click interactions.
     */
    class SubstanceTypeContentAdapter extends BaseAdapter {

        Context context;
        String[] subTypes;
        private LayoutInflater inflater = null;

        public SubstanceTypeContentAdapter(Context context, String[] subTypes) {
            this.context = context;
            this.subTypes = subTypes;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void sort(Comparator<String> comparator) { }

        @Override
        public int getCount() {
            return subTypes.length;
        }

        @Override
        public Object getItem(int position) {
            return subTypes[position]; //returns the name of the item
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
                vi = inflater.inflate(R.layout.sub_type_row, null);
            TextView type = (TextView) vi.findViewById(R.id.substance_type);
//            String[] nameParts = data.get(position)[0].split("\\|"); // 0 is type, 1 is psychoactive, 2 is chosen page
//            TextView date = (TextView) vi.findViewById(R.id.date);
            String substance_type = subTypes[position].trim();
            type.setText(substance_type);

            return vi;

        }

        // TODO: Implement sort functionality
    }

    /**
     * Adapter class which defines how the list view interprets an assigned data source,
     * and how that data source is presented in the rows of the listview
     * Also defines access methods and click interactions.
     */
    class SubstanceContentAdapter extends BaseAdapter {

        Context context;
        List<String[]> vaultTable;
        private LayoutInflater inflater = null;

        public SubstanceContentAdapter(Context context, List<String[]> vaultTable) {
            this.context = context;
            this.vaultTable = vaultTable;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void sort(Comparator<String> comparator) { }

        @Override
        public int getCount() {
            return vaultTable.size();
            // return vault.getSection().get(subTypeIndex).getSubstance().size();
        }

        @Override
        public Object getItem(int position) {
            return vaultTable.get(position);
            //return vault.getSection().get(subTypeIndex).getSubstance().get(position).getName(); //returns the name of the item
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
                vi = inflater.inflate(R.layout.sub_row, null);
            TextView type = (TextView) vi.findViewById(R.id.substance);
            TextView substanceEffects = (TextView) vi.findViewById(R.id.substance_effects);

            String substance_name = vaultTable.get(position)[vTNiceName];
            String substance_effects = "";
            if(vaultTable.get(position).length > 4) { //if not herbs
               substance_effects = vaultTable.get(position)[vTSumEffects];//[vTSumEffects];
            }
                    //vault.getSection().get(subTypeIndex).getSubstance().get(position).getNiceName();
            type.setText(substance_name);
            substanceEffects.setText(substance_effects);

            return vi;

        }

        // TODO: Implement sort functionality
    }

    class webFetchBigChartAsyncTask extends AsyncTask<Void, Void, Void> {
        String chartXmlString;

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

            String bigChartXML = m.getWebContent("http://pages.cpsc.ucalgary.ca/~madunlap/big_chart_xml_mod.php");
            m.splitVaultXmlUpAndStore(bigChartXML, getFilesDir().getAbsolutePath());

            ErowidPsychoactiveVaults vault = m.getPsyVaultFromXML(bigChartXML);
            vaultTable = createVaultTable(vault);

            return null;
        }

        /**
         * After: Calls the html format fixer, loads the html along with stored with css & images.
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                //m.storeSubstancesClassString(bigChartXML, getBaseContext()); //saves a copy for later
                //processXmlToVaultAsyncTask processXmlToVaultAsyncTask = new processXmlToVaultAsyncTask();
                //processXmlToVaultAsyncTask.execute();
                listPsyReady = true;
                TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView);
                loadingTextView.setVisibility(View.GONE);
                populateSearchLater(theMenu);
            }
            catch (Exception e)
            {
                Log.d("Error!.", " " + e.getMessage());
            }
        }
    }


//    class processXmlToVaultAsyncTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... arg0) {
////            activityManager.getMemoryInfo(mi);
////            long availableMegs = mi.availMem / 1048576L;
////
////            Log.d("Memory Test before vault.", availableMegs +" mb");
//            //String singleChartXML = m.getSubXML(bigChartXML, "zopiclone", "PHARMS");
//
//            vault = m.getPsyVaultFromXML(bigChartXML);
////            activityManager.getMemoryInfo(mi);
////            availableMegs = mi.availMem / 1048576L;
////
////            Log.d("Memory Test after vault.", availableMegs +" mb");
//            bigChartXML = null; //hopefully free memory
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            //VaultSingleton.getInstance().setVault(vault);
//
//            vaultTable = createVaultTable(vault);
//            listPsyReady = true;
//            TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView);
//            loadingTextView.setVisibility(View.GONE);
//            populateSearchLater(theMenu);
//
////
//        }
//    }
}
