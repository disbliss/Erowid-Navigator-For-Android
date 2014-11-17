package org.erowid.navigatorandroid;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.ActionBar;

import org.erowid.navigatorandroid.xmlXstream.ErowidPsychoactiveVaults;

//import android.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
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
    ProgressDialog dialog;
    //String bigChartXML;
    //int subTypeIndex;

    // for vaultTable
    int vTName = 0;
    int vTNiceName = 1;
    int vTSubType = 2;
    int vTSumImage = 3;
    int vTSumEffects = 4;
    int vTAltNameListString = 4;

//Toast.makeText(getBaseContext(), "Downloading updated list of psychoactives and restarting. Please wait..." , Toast.LENGTH_LONG).show();

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

//        Intent intent = getIntent();
//        if(intent.getBooleanExtra("PAGE_UPDATED", false))
//        {
//            Toast.makeText(getBaseContext(), "Application restarted" , Toast.LENGTH_LONG).show();
//        }

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
            if(true) {
                if (mActionMode == null) {
                    String psyType = (String) psyTypeListView.getItemAtPosition(position);

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

                    if (m.isOnline(getBaseContext())) {
                        Intent intent = new Intent(getBaseContext(), PsychoNavigatorActivity.class);
                        //intent.putExtra("SECTION_INDEX", subTypeIndex);
                        String psyName = smallVaultTable.get(position)[vTName];
                        String psyType = smallVaultTable.get(position)[vTSubType];
                        intent.putExtra("SUBSTANCE_NAME", psyName);
                        intent.putExtra("SUBSTANCE_TYPE", psyType);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getBaseContext(), "Reconnect to the internet, or access your stored pages from the top menu", Toast.LENGTH_LONG).show();
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

    public void createVisualAndStuff()
    {

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

//        choiceListView.setItemChecked(-1, true);
//        if(searchPopulated) {
        super.onResume();
//        ListView choiceListView = (ListView)findViewById(R.id.psyChoiceListView);
//        if(theMenu != null) {
//            SearchView searchItem = (SearchView) theMenu.findItem(R.id.action_search).getActionView();
//            searchItem.setIconified(true);
//        }
        Log.d("OnResume","happened");
        invalidateOptionsMenu();
        //PopulateViewsAndStuff();
//        }
//        else{
//            Log.d("Resume Test","Resumed!");
//        }

	}



//    //I think this was added later, not sure if it does anything at all.
//	@Override
//	 public boolean onSearchRequested() {
//		if(theMenu != null)
//		{
//			MenuItem searchMenuItem = theMenu.findItem(R.id.action_search);
//			searchMenuItem.expandActionView();
//		}
//		return true; // don't go ahead and show the search box
//	}

	/**
	 * Adds the search bar to the top of the page and makes it active
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        //Log.d("OnCreateOptionsMenu","happened");
        theMenu = menu;
		if(vaultTable != null)
		{
            //Log.d("OnCreateOptionsMenuActually","happened");
			populateSearchLater(theMenu);
		}
		return true;		 
	}
//
//    //happens every time menu is displayed
//    @Override
//    public boolean  onPrepareOptionsMenu(Menu menu)
//    {
//        Log.d("OnPrepareOptionsMenu","happened");
//        theMenu = menu;
//        if(vaultTable != null)
//        {
//            populateSearchLater(theMenu);
//        }
//        return true;
//    }

    public void runApplicationRestart()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            // Do something after 5s = 5000ms
            File storagePath = new File(getFilesDir().getAbsolutePath()+"/chartXml/");
            m.deleteRecursive(storagePath); //clear out the old
            m.clearPsyChoicesList(getBaseContext());
            clearVariables();

            Intent mStartActivity = new Intent(getBaseContext(), MainPageActivity.class);
            mStartActivity.putExtra("PAGE_UPDATED", true);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

            System.exit(0);

            }
        }, 1500);
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
                Toast.makeText(getBaseContext(), "Restarting application to reload data" , Toast.LENGTH_LONG).show();
                runApplicationRestart();


                 //http://stackoverflow.com/questions/6609414/howto-programatically-restart-android-app
		        return true;

		    // case R.id.action_about:
			//	Intent intent2 = new Intent(getBaseContext(),AboutPageActivity.class);
			//	startActivity(intent2);
		    //    return true;
		}
		return false;

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
	 * This is to be called to populate the search bar for the menu
	 * It is called after the psyName table has been populated, otherwise it will break. 
	 * @param menu the menu which holds the search bar
	 */
	public void populateSearchLater(Menu menu)
	{
//		if(!searchPopulated)
//		{
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
//		}
//		else
//		{	//this actually restarts the application to use the updated list.
//
//		}
	}

    private void clearVariables()
    {
        rawMainWebContent = null;
        listPsyReady = false; 	//has the content been processed
        listPsyResumed = false; 	//has this page been resumed after going to another page
        searchPopulated = false; //has the search content been populated
        psyTypeListView = null;
        psyChoiceListView = null;
        mActionMode = null;
        theMenu = null;
        parsedBigChart = null;
        //ErowidPsychoactiveVaults vault;
        vaultTable = null;

    }

	/** Accesses the Erowid Substance main page (http://www.erowid.org/general/)
	 * Parses the html to fill a list of substances to choose from
	 */ 
	private void createMenu() 
	{ 
		m.clearOldPsyChoicesList(getBaseContext());
        vaultTable = m.getStoredPsyChoicesList(getBaseContext());

        if(vaultTable == null || vaultTable.isEmpty())
		{   
			if(m.isOnline(this))
			{
				//if online but no stored table    
//				Button reloadButton = (Button)findViewById(R.id.reloadButton);
//				reloadButton.setVisibility(View.GONE);
//				TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView);
//				loadingTextView.setVisibility(View.VISIBLE);
                dialog = ProgressDialog.show(this, "",
                        "Downloading the Erowid index, may take a minute, please wait . . .", true);
                webFetchBigChartAsyncTask webFetchBigChartFetch = new webFetchBigChartAsyncTask();
                webFetchBigChartFetch.execute();
			}
			else
			{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Reconnect to the internet and push \"OK\" to download the Erowid index")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            createMenu();
                        }
                    });
                AlertDialog alert = builder.create();
                alert.show();
			}      
		}
		else 
		{   //if there is a table and we are online
//			Button reloadButton = (Button)findViewById(R.id.reloadButton);
//			reloadButton.setVisibility(View.GONE);
//			TextView loadingTextView = (TextView)findViewById(R.id.loadingTextView);
//			loadingTextView.setVisibility(View.GONE);
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
                //Log.d("substancePrintOut",vault.getSection().get(i).getSectionName()  + " !! " +  vault.getSection().get(i).getSubstance().get(j).getName());
                vaultTable.add(substance);
            }
        }
        m.storePsyChoicesList(vaultTable, this);
        return vaultTable;
    }

	public void reloadButton_onClick(View v) {
		createMenu();
	}

    public void closeSubstanceListButton_onClick(View v) {
        Button closeSubstanceListButton = (Button)findViewById(R.id.closeSubstanceListButton);
        TextView psyTypeChooseTextView = (TextView)findViewById(R.id.psyTypeChooseTextView);
        psyTypeChooseTextView.setText("Choose a substance type");

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
     *
     */
    public void updateSubTypeListView()
    {
        //String path = getFilesDir().getPath();
        //List<String> offlineFilenameAndDateList = m.getOfflineSiteFilenameAndDateList(path);

        //String[] spinnerArray = offlineFilenameAndDateList.toArray(new String[(offlineFilenameAndDateList.size())]);

        psyTypeListView = (ListView) findViewById(R.id.psyTypeListView);

        String[] subTypes = getResources().getStringArray(R.array.psy_types_array);
        SubstanceTypeContentAdapter adapter = new SubstanceTypeContentAdapter(this, subTypes);

        psyTypeListView.setAdapter(adapter);
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

//
//            runOnUiThread(new Runnable() {
//                public void run() {
//
//
//                    Toast.makeText(getBaseContext(),"Downloading psychoactive data, please wait", Toast.LENGTH_LONG).show();
//                }
//            });



            String bigChartXML = m.getWebContent("http://www.erowid.org/general/big_chart_xml.php");

            bigChartXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><?xml-stylesheet type=\"text/css\" href=\"big_chart_xml.css\" ?>\n"
                    + bigChartXML.substring(bigChartXML.indexOf("<erowid-psychoactive-vaults"), bigChartXML.length());

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
                dialog.dismiss();
            }
            catch (Exception e)
            {
                Log.d("Error!.", " " + e.getMessage());
            }
        }
    }
}
