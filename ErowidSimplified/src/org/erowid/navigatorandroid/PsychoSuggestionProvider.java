package org.erowid.navigatorandroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.erowid.navigatorandroid.xmlXstream.ErowidPsychoactiveVaults;

public class PsychoSuggestionProvider extends ContentProvider {
	//SQLiteDatabase db;
    //SQLiteQueryBuilder qb;
	org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
    //ErowidPsychoactiveVaults vault = VaultSingleton.getInstance().getVault();

    // for vaultTable
    int vTName = 0;
    int vTNiceName = 1;
    int vTSubType = 2;
    int vTSumImage = 3;
    int vTSumEffects = 4;

	public PsychoSuggestionProvider() {
		// TODO Auto-generated constructor stub
	}
  
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}
 
	@Override	
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

        //Log.d("TEST!!!!!!!!!!!!!!!", VaultSingleton.getInstance().getString());

		String[] menuCols = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA};
		int[] to = new int[] { R.id.id, R.id.item };
		MatrixCursor menuCursor = new MatrixCursor(menuCols);

        //vault = VaultSingleton.getInstance().getVault();



        List<String[]> vaultList = m.getStoredPsyChoicesList(getContext());
		List<PsyQueryChoice> queryToSort = new ArrayList<PsyQueryChoice>();

        //Toast.makeText(getContext(), " " + vault, Toast.LENGTH_LONG);
        //Log.d("TEST!!!!!!!!!!!!!!!", vault.getSection().get(0).getSectionName() );

        int total = 0;

        for(int i = 0; i < vaultList.size(); i++)
        {
            if(vaultList.get(i)[vTNiceName].toLowerCase().contains(selectionArgs[0].trim()))
            {
                queryToSort.add(new PsyQueryChoice(total,
                        vaultList.get(i)[vTNiceName],
                        vaultList.get(i)[vTName] + "," + vaultList.get(i)[vTSubType] /*+","+i+","+j*/)); //TODO If you figure out search, fix this for passing data, its not the most safe...
                total++;// does this do anything? probably something outside my control
            }
        }

//        for(int i = 0; i < vault.getSection().size(); i++)
//        {
//            for (int j = 0; j < vault.getSection().get(i).getSubstance().size(); j++) {
//                if (vault.getSection().get(i).getSubstance().get(j).getNiceName().toLowerCase().contains(selectionArgs[0].trim()))
//			    {
//                    queryToSort.add(new PsyQueryChoice(total,
//                            vault.getSection().get(i).getSubstance().get(j).getNiceName(),
//                            vault.getSection().get(i).getSubstance().get(j).getName()+","+i+","+j)); //TODO If you figure out search, fix this for passing data, its not the most safe...
//                    //Log.d("Search Test.", " " + vault.getSection().get(i).getSubstance().get(j).getName());
//                    total++;
//                }
//            }
//        }


        //TODO: I don't think the search sort works, make it work
		
		//this sorts all the hinted queries by name
		Collections.sort(queryToSort, PsyQueryChoice.PsyQueryComparator);

		//after sorting, we add each element to the matrix cursor. Could not find a way to add multiple.
		for(int i =0; i< queryToSort.size(); i++)
		{
			//I'm sure there is a better way to do this than create another object
			menuCursor.addRow(new Object[] { queryToSort.get(i).id , queryToSort.get(i).name, queryToSort.get(i).intentName});
		}
		
		
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter menuItems = new SimpleCursorAdapter(getContext(), R.layout.menu_row, menuCursor, menuCols, to); //nullls
		//(this, R.layout.menu_row, menuCursor, menuCols, to);
		
		return menuItems.getCursor();
		
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		//
		return 0;
	}
	
}




