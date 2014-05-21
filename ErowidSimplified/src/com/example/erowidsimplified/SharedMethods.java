package com.example.erowidsimplified;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SharedMethods {
 	
	/**
	 * capitalizes the first character in a string.
	 */
	public String capitalize(String line)
	{
	  return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	/** 
	 * Large html modifying class.
	 * Takes in the raw html and massages it to make it work in the webview.
	 * This also downloads the images before returning the html.
	 * Am not using an actual XML parser because the HTML threw invalid XML errors.
	 * If I need to do more advanced parsing, parseHTML() needs to be used.
	 * 
	 * @param htmlText - the raw html passed in
	 * @param pageType - the type of web-page being processed (e.g. basic, images, law, etc.)
	 * @return the modified html
	 */
	public String fixHTML(String htmlText, String pageType)
	{
		/* 
		 * General HTML parsing section
		 * (Has to be horribly inefficient to go through the html over and over and over)
		 */
		//TODO: see if multiple replaces can be done with the same pass.
		htmlText = htmlText.replace("/includes/", "includes/"); // replaces absolute file paths to includes with relative ones
		// replaces all <a... links
		htmlText = htmlText.replaceAll("<a [^>]*>", "\1");
		htmlText = htmlText.replaceAll("#</a>", "");
		htmlText = htmlText.replaceAll("# </a>", "");
		htmlText = htmlText.replaceAll("</a>", "");
		 
		/*
		 * Specified HTML parsing section
		 * Depending on the page type, does different modifications of the html
		 * Generally, all paths get a subection of the html to display, moving the menu and footer.
		 */
		if(pageType.equals("chemistry"))
		{
			try{
				
			htmlText =
					htmlText.substring(htmlText.indexOf("</td></tr></table><br/><br/>"),htmlText.indexOf("<br/><br/><br/>\n\n\n</b></font>"))
					+ "</body></html>";
			} catch (Exception e)
			{
		
			}
		} 
		else if(pageType.equals("effects"))
		{
			//htmlText = htmlText.replaceAll("(<img [^>]* width=\")([0-9]*)(\"[^>]*>)","$1"+0+"$3"); //removes the image by being janky. Fixes the table sorta
			//htmlText = htmlText.replaceAll("(<img [^>]* )(height=\"[0-9]*\")([^>]*>)","$1$3");// removes all image height
		}
		else if(pageType.equals("basics"))
		{
			//TODO: deal with the image better.
			htmlText = htmlText.replaceAll("(<img [^>]* width=\")([0-9]*)(\"[^>]*>)","$1"+345+"$3"); //replaces all image width with 300, which is not the best fix for showing an image
			htmlText = htmlText.replaceAll("(<img [^>]* )(height=\"[0-9]*\")([^>]*>)","$1$3");// removes all image height
			
		}   
		if(!pageType.equals("chemistry")) //"general" case
		{
			//Extracts the body, adds back a head
			//Currently this code is unused since chemistry is loaded externally.
			//If used, need to fix offset in code here, try using shared indexAfter()
			htmlText = htmlText.substring(0,htmlText.indexOf("</head>")+7)
					 + "<body>" 
					 + htmlText.substring(htmlText.indexOf("<div id=\"content-body-frame\">"),htmlText.indexOf("</div><!-- end content-body-frame-->"))
					 + "</body></html>";
		}
		return htmlText;
	}
	
	public void downloadErowidImages(String htmlText, String pageURL, String absFilesDir)
	{
		
		/* 
		 * Finds each image url and downloads the image to the local directory
		 * Also sets the width of the image to 100% for better viewing
		 * TODO: Fix offset in code here, try using shared indexAfter()
		 */
		String[] values = htmlText.split("<img ");
		for (int i = 1; i < values.length; i++) {
			String tempValue = values[i].substring(values[i].indexOf("src=\"")+5);
		    String url = tempValue.substring(0,tempValue.indexOf("\""));
		    downloadImage(pageURL, url, absFilesDir);
		}
	}
	
	/**
	 * 
	 * @param baseURL - the url of the webpage the image was on
	 * @param relImgURL - the relative location of the image to the webpage 
	 */
	public void downloadImage(String baseURL, String relImgURL, String absFilesDir)
	{
		String[] baseHolderArray = baseURL.split("/");
		String trueBaseURL = baseURL.substring(0,baseURL.indexOf(baseHolderArray[baseHolderArray.length-1])); //get the last content after split
		String fullURL = trueBaseURL + relImgURL;
		
		//get the filename from relImgURL, which means content after last /
		String imageName = relImgURL.substring(relImgURL.indexOf("/")+1);
		System.out.println(imageName);
		
		
		/*
		 * This crazy mess first checks for/creates a folder
		 * and stores the image in the correct location on the file system
		 */
		URL url = null;
		try {
			url = new URL (fullURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream input = null;
		try {
			input = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
		    //First make folder
		    File storagePath = new File(absFilesDir+"/images/");
		    if (!storagePath.exists()) {
		        storagePath.mkdir();
		    }
		    OutputStream output = new FileOutputStream (new File(storagePath,imageName));
		    try {
		        byte[] buffer = new byte[1024];
		        int bytesRead = 0;
		        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
		            output.write(buffer, 0, bytesRead);
		        }
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
		        try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	//Accesses the webpage content to make a string
	public String getWebContent(String url)
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		String content = null; 
		try
		{
			response = client.execute(httpget);
			// Examine the response status

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				content= convertStreamToString(instream);
				// now you have the string representation of the HTML request
				instream.close();
				System.out.println("Content downloaded"); //debug 
				return content;
			} 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	} 

	//Creates a map of report card info for use in PsychoNavigatorActivity
	Map<String, String> getReportCardInfo(String url)
	{
		Map<String, String> content = new HashMap<String, String>();
		String rawContent = getWebContent(url);
		
		//this is so inefficient
		String description = rawContent.substring(indexAfter(rawContent, "<div class=\"sum-description\">")); // first gets the first half the string
		description = description.substring(0,description.indexOf("</div>")); // then gets the second half
		content.put("Description", description);
		
		String commonNames = rawContent.substring(indexAfter(rawContent, "<div class=\"sum-common-name\">"));
		commonNames = commonNames.substring(0,commonNames.indexOf("</div>"));
		content.put("Common Names", commonNames);
		
		String effects = rawContent.substring(indexAfter(rawContent, "<div class=\"sum-effects\">"));
		effects = effects.substring(0,effects.indexOf("</div>"));
		content.put("Effects", effects);
		
		String chemicalName = rawContent.substring(indexAfter(rawContent, "<div class=\"sum-chem-name\">"));
		chemicalName = chemicalName.substring(0,chemicalName.indexOf("</div>"));
		content.put("Chemical Name", chemicalName);
		
		String caution = rawContent.substring(indexAfter(rawContent, "<div class=\"sum-caution\">"));
		caution = caution.substring(0,caution.indexOf("</div>"));
		content.put("Caution", caution);
		
		return content;
	}
	
	/**
	 * Gets the first instance of the index after a searched for substring
	 * @param theString - The string to be searched on
	 * @param splitString - The substring searched for
	 */
	int indexAfter(String theString, String splitString)
	{
		int length = splitString.length();
		int index = theString.indexOf(splitString) + length; 
		return index;
	} 
	
	/**
	 * Takes the list of psychoactive information and saves it as a comma seperated SharedPreferences
	 */
	void storePsyList(List<String[]> psyTable, Context passedContext)
	{
		SharedPreferences.Editor edit= passedContext.getSharedPreferences("PSYTABLE", Context.MODE_PRIVATE).edit();
		StringBuilder tableStringB = new StringBuilder();
		for(int i = 0; i < psyTable.size(); i++)
		{
			StringBuilder row = new StringBuilder();
			for(int j = 0; j < psyTable.get(0).length; j++)
			{
				row.append(psyTable.get(i)[j]).append(",");
			}
			row.append("\n");
			tableStringB.append(row);
		}
		tableStringB.replace(tableStringB.lastIndexOf("\n"), tableStringB.lastIndexOf("\n") + 2, "" );
		String tableString = tableStringB.toString();
		edit.putString("table", tableString);
		edit.commit();
	}
	
	/**
	 * Pulls the stored psychoactive CSV from SharedPreferences and parses it into a List.
	 */
	List<String[]> getStoredPsyList(Context passedContext)
	{
		SharedPreferences prefs = passedContext.getSharedPreferences("PSYTABLE", Context.MODE_PRIVATE);
		String tableString = prefs.getString("table", "");
		if(tableString.equals(""))
		{	//if there is no table to pull
			return null;
		}
		String[] splitTableStringArray = tableString.split("\n");
		List<String[]> psyTable = new ArrayList<String[]>();
		
		for (int i = 0; i < splitTableStringArray.length; i++) {
			//this probably adds an extra row of "" values on the end, after the last ,
			psyTable.add(splitTableStringArray[i].split(","));
		}
		
		return psyTable;
	}

	
	//Does the actual html-to-string creation
	//Keep Private
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	/**
	 * Checks if the phone is online and returns true/false based upon this.
	 */
	public boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
