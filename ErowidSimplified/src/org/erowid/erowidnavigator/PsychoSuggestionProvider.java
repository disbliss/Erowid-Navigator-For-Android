package org.erowid.erowidnavigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.SimpleCursorAdapter;

public class PsychoSuggestionProvider extends ContentProvider {
	//SQLiteDatabase db;
    //SQLiteQueryBuilder qb;
	org.erowid.erowidnavigator.SharedMethods m = new org.erowid.erowidnavigator.SharedMethods();
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
		
		String[] menuCols = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA};
		int[] to = new int[] { R.id.id, R.id.item };
		MatrixCursor menuCursor = new MatrixCursor(menuCols);
		
		List<String[]> psyList = m.getStoredPsyList(getContext());
		List<PsyQueryChoice> queryToSort = new ArrayList<PsyQueryChoice>();
		
		//this pulls each searched item and adds it to a list to be sorted by name
		for(int i = 0; i < psyList.size(); i++)
		{
			if(psyList.get(i)[0].contains(selectionArgs[0].trim()))
			{
				queryToSort.add(new PsyQueryChoice ( i , psyList.get(i)[0], psyList.get(i)[0]));
				//menuCursor.addRow(new Object[] { i , psyList.get(i)[0], psyList.get(i)[0]});
			}
		}
		
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




