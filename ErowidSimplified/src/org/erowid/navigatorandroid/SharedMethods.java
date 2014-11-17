package org.erowid.navigatorandroid;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.erowid.navigatorandroid.xmlXstream.AlternateNameSet;
import org.erowid.navigatorandroid.xmlXstream.ErowidPsychoactiveVaults;
import org.erowid.navigatorandroid.xmlXstream.ImageEntry;
import org.erowid.navigatorandroid.xmlXstream.ImageSet;
import org.erowid.navigatorandroid.xmlXstream.Section;
import org.erowid.navigatorandroid.xmlXstream.Substance;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.thoughtworks.xstream.XStream;

public class SharedMethods {
 	
	/**
	 * capitalizes the first character in a string.
	 */
	public String capitalize(String line)
	{
        String returnString = "";
        if(line.length() > 0)
        {
            returnString += Character.toUpperCase(line.charAt(0));
        }
        if(line.length() > 1)
        {
            returnString += line.substring(1);
        }
        return returnString;
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
	public String fixHTML(String htmlText, String pageType, Context context)
	{
		/* 
		 * General HTML parsing section
		 * (Has to be horribly inefficient to go through the html over and over and over)
		 */
		//TODO: see if multiple replaces can be done with the same pass.
		//	Maybe try: http://stackoverflow.com/questions/7532042/
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
			////unused window size code
			//WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			//Display display = wm.getDefaultDisplay();
			//Point size = new Point();
			//display.getSize(size);
			//int width = size.x;
			//int height = size.y;
			
			htmlText = htmlText.replaceAll("(<img [^>]* width=\")([0-9]*)(\"[^>]*>)","$1"+225+"$3"); //replaces all image width with 345, which is not the best fix for showing an image
			htmlText = htmlText.replaceAll("(<img [^>]* )(height=\"[0-9]*\")([^>]*>)","$1$3");// removes all image height
			
		}   
		if(!pageType.equals("chemistry")) //"general" case
		{
			//TODO: This parse breaks
			//Extracts the body, adds back a head
			//Currently this code is unused since chemistry is loaded externally.
			//If used, need to fix offset in code here, try using shared indexAfter()
            try {
                //not sure if when these break they cause an exception or if they just insert null text. Covering all bases.
                String text1 = htmlText.substring(0, htmlText.indexOf("</head>") + 7);
                String text2 = htmlText.substring(htmlText.indexOf("<div id=\"content-body-frame\">"), htmlText.indexOf("</div><!-- end content-body-frame-->"));
                if(null != text1 && null != text2) {
                    htmlText = text1
                            + "<body>"
                            + text2
                            + "</body></html>";
                }
                else
                {
                    htmlText = "Page error, please reopen page. If this does not work, try loading the page externally using the top menu.";
                }
            }
            catch(Exception e)
            {
                htmlText = "Page error, please reopen page. If this does not work, try loading the page externally using the top menu.";
            }//empty because it should fail elegantly
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
	
	public void pingURL(String fileName)
	{
		//http://www.erowid.org/star/plants/cannabis/cannabis_basics.shtml
		String[] parts = fileName.split("\\|"); // 0 is type, 1 is psychoactive, 2 is chosen page
    	String url = "http://www.erowid.org/star/" + parts[0].trim().toLowerCase() + "/" 
    				+ parts[1].trim().toLowerCase() +"/" + parts[1].trim().toLowerCase() + "_" 
    				+ parts[2].trim().toLowerCase() +".shtml";
		getWebContent(url);
		
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

//    public void storeSubstancesClassString(String classXML, Context context)
//    {
//        try {
//
//            //String filePath = "/sdcard/utf8_file.txt";
//            String UTF8 = "utf8";
//            int BUFFER_SIZE = 8192;
//
//
//            String basePath = context.getFilesDir().getPath();
//            File folder = new File(basePath+ "/files");
//            Boolean folderMade = folder.mkdir(); // only happens if no folder
//
//            Log.d("Folder Test", folderMade.toString());
//
//            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), UTF8),BUFFER_SIZE);
//            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), UTF8),BUFFER_SIZE);
//            File file = new File(basePath+ "/files/big_chart_xml.php");
//
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(file), UTF8), BUFFER_SIZE ); //(context.openFileOutput("files/big_chart_xml.php", Context.MODE_PRIVATE)
//            bw.write(classXML);
//            bw.close();
//
//            //FileOutputStream fos = context.openFileOutput("substanceVaultClass.xml", Context.MODE_PRIVATE);
//            //ObjectOutputStream oos = new ObjectOutputStream(fos);
////            oos.writeObject(classXML);
////            oos.close();
////            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public String getSubstancesClassString(Context context)
//    {
//        StringBuilder sb = new StringBuilder();
//        try{
//
//            String basePath = context.getFilesDir().getPath();
//            File file = new File(basePath+ "/files/big_chart_xml.php");
//            FileInputStream fis = new FileInputStream(file);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//            fis.close();
//        } catch(OutOfMemoryError om){
//            om.printStackTrace();
//            return null;
//        } catch(Exception ex){
//            ex.printStackTrace();
//            return null;
//        }
//        String result = sb.toString();
//        return result;
//    }

//    public String getSubXML(String chartXmlString, String psyName)
//    {
//
//        //this is close: http://stackoverflow.com/questions/16069425/xmlpullparser-get-inner-text-including-xml-tags
//        // what i can probably do is just detect the start of a substance tag, and then start adding tags to a string
//        // if the name tag inside is the name we want, we keep the string
//        // This could be really inefficient if the name tags are at the end, but they are currently placed at the beginning
//
//        //this could actually be more efficient if I first check if the section name matches, so I only check in the section that matters
//
//        //there are no attributes in the whole xml, so I'm not dealing with them.
//
//        //1.45s mid, 2.14 long
//
//        //long startTime = System.nanoTime();
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            XmlPullParser parser = factory.newPullParser();
//            //parser.
//            parser.setInput(new StringReader(chartXmlString));
//
//            Boolean inSubstance = false;
//            Boolean inName = false;
//            Boolean substanceFound = false;
//            Boolean wrongSubstance = false;
//
//            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
//                if (parser.getEventType() == XmlPullParser.START_TAG  && !wrongSubstance) {
//                    if(parser.getName().equalsIgnoreCase("substance")) {
//                        inSubstance = true;
//                        sb.setLength(0); //clears section tag xml being added to sb
//                        //sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
//                        //sb.append("<substance>");
//                    }
//                    else if(inSubstance)
//                    {
//                        if(parser.getName().equalsIgnoreCase("name"))
//                        {
//                            inName = true;
//                            //sb.append("<name>");
//                        }
//                    }
//                    sb.append("<" + parser.getName() + ">");
//
//                }
//                else if(parser.getEventType() == XmlPullParser.TEXT && inSubstance && !wrongSubstance)
//                {
//                    if(inName)
//                    {
//                        if(parser.getText().trim().equals(psyName))
//                        {
//                            substanceFound = true;
//                        }
//                        else
//                        {
//                            wrongSubstance = true;
//                        }
//                        inName = false; //theoretically you are still "in name" at the end tag, but this is more efficient
//                    }
//                    sb.append(escapeXml10(parser.getText())); //this text conversion would probably start to slow things down if
//                }
//                else if(parser.getEventType() == XmlPullParser.END_TAG && inSubstance)
//                {
//                    if(!wrongSubstance) { //not sure this helps at all
//                        sb.append("</" + parser.getName() + ">");
//                    }
//                    if(parser.getName().equals("substance"))
//                    {
//                        wrongSubstance = false;
//                        if(substanceFound) {
//                            inSubstance = false; //this shouldn't matter, just seems right
//                            break; //not sure if this works
//                        }
//                        else //If clearing was managed better elsewhere, I don't think this would matter
//                        {
//                            sb.setLength(0);
//                            //sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
//                        }
//                    }
//
//                }
//
//
//                parser.next();
//            }
//
//
////                    name = parser.getAttributeValue(null, "name");
////                    site = parser.getAttributeValue(null, "site");
////                    phone = parser.getAttributeValue(null, "phone");
////                    adds = parser.getAttributeValue(null, "address");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //long endTime = System.nanoTime();
//        //Log.d("Time Test.", (endTime - startTime)/1000000000.0 + " s");
//        String result = sb.toString();
//        return sb.toString();
//
//    }

    /**
     * This isn't actually better. It has more checks, but for the size of the XML the simpler check seems a bit faster
     * The testing was very unscientific
     * TODO: recheck this, maybe there is a bug or inefficiency slowing it down
     * TODO: The other parser had a bug with the first element in each section getting xml junk
     * @param chartXmlString
     * @return
     */
//    public String getSubXMLComplex(String chartXmlString, String psyName, String psyType)
//    {
//
//        //this is close: http://stackoverflow.com/questions/16069425/xmlpullparser-get-inner-text-including-xml-tags
//        // what i can probably do is just detect the start of a substance tag, and then start adding tags to a string
//        // if the name tag inside is the name we want, we keep the string
//        // This could be really inefficient if the name tags are at the end, but they are currently placed at the beginning
//
//        //this could actually be more efficient if I first check if the section name matches, so I only check in the section that matters
//
//        //there are no attributes in the whole xml, so I'm not dealing with them.
//
//
//        //long startTime = System.nanoTime();
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            XmlPullParser parser = factory.newPullParser();
//            parser.setInput(new StringReader(chartXmlString));
//
//            Boolean inSubstance = false;
//            Boolean inName = false;
//            Boolean substanceFound = false;
//            Boolean wrongSubstance = false;
//            Boolean wrongSection = false;
//
//            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
//                if (parser.getEventType() == XmlPullParser.START_TAG  && !wrongSubstance && !wrongSection) {
//                    if(parser.getName().equalsIgnoreCase("substance")) {
//                        inSubstance = true;
//                        sb.setLength(0); //clears section tag xml being added to sb
//                        //sb.append("<substance>");
//                    }
//                    else if(inSubstance)
//                    {
//                        if(parser.getName().equalsIgnoreCase("name"))
//                        {
//                            inName = true;
//                            //sb.append("<name>");
//                        }
//                    }
//                    sb.append("<" + parser.getName() + ">");
//                    if(parser.getName().trim().equals("section-name"))
//                    {   //done at the end to stop breaking from checking of different tag types
//                        //immediately get next tag, to check content. section-name only has text in it.
//                        //if this changes this may be problematic, though will likely only pull something else unimportant
//                        parser.next();
//                        if(!parser.getText().equals(psyType))
//                        {
//                            wrongSection = true;
//                        }
//                    }
//
//                }
//                else if(parser.getEventType() == XmlPullParser.TEXT && inSubstance && !wrongSubstance && !wrongSection)
//                {
//                    if(inName)
//                    {
//                        if(parser.getText().trim().equals(psyName))
//                        {
//                            substanceFound = true;
//                        }
//                        else
//                        {
//                            wrongSubstance = true;
//                        }
//                        inName = false; //theoretically you are still "in name" at the end tag, but this is more efficient
//                    }
//                    sb.append(parser.getText());
//                }
//                else if(parser.getEventType() == XmlPullParser.END_TAG && inSubstance)
//                {
//                    if(!wrongSection && !wrongSubstance) { //not sure this helps at all
//                        sb.append("</" + parser.getName() + ">");
//                    }
//                    if(parser.getName().equals("substance"))
//                    {
//                        wrongSubstance = false;
//                        if(substanceFound) {
//                            inSubstance = false; //this shouldn't matter, just seems right
//                            break; //not sure if this works
//                        }
//                        else //If clearing was managed better elsewhere, I don't think this would matter
//                        {
//                            sb.setLength(0);
//                        }
//                    }
//                    if(parser.getName().equals("section"))
//                    {
//                        wrongSection = false;
//                    }
//
//                }
//
//
//                parser.next();
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //long endTime = System.nanoTime();
//        //Log.d("Time Test.", (endTime - startTime)/1000000000.0 + " s");
//        String result = sb.toString();
//        return sb.toString();
//
//    }

    public ErowidPsychoactiveVaults getPsyVaultFromXML(String chartXmlString) {
        try {
            long startTime = System.nanoTime();

            ErowidPsychoactiveVaults vault;


            XStream xstream = new XStream();

            xstream.alias("erowid-psychoactive-vaults", ErowidPsychoactiveVaults.class);
            xstream.alias("substance", Substance.class);
            //xstream.addImplicitCollection(ErowidPsychoactiveVaults.class, "section");

            //xstream.alias("alternate-name-set", AlternateNameSet.class);
            //xstream.alias("image-set", ImageSet.class);
            //xstream.alias("image-entry", ImageEntry.class);

            xstream.processAnnotations(ErowidPsychoactiveVaults.class);
            xstream.processAnnotations(Section.class);
            xstream.processAnnotations(Substance.class);
            xstream.processAnnotations(AlternateNameSet.class);
            xstream.processAnnotations(ImageSet.class);
            xstream.processAnnotations(ImageEntry.class);

            xstream.ignoreUnknownElements("slang-name-set"); //could just pass blank unknown elements, but then would make hard to debug
            xstream.ignoreUnknownElements("experience-set");

            vault = (ErowidPsychoactiveVaults) xstream.fromXML(chartXmlString);

            long endTime = System.nanoTime();
            //Log.d("Time Test.", (endTime - startTime) / 1000000000.0 + " s");
            return vault;
        } catch (Exception e) {
            Log.d("Error shared!.", " " + e);
            return null;
        }

    }
    public Substance getSubstanceFromXML(String chartXmlString)
    {
        try {
            //long startTime = System.nanoTime();

            Substance vault;


            XStream xstream = new XStream();

            //xstream.alias("erowid-psychoactive-vaults", ErowidPsychoactiveVaults.class);
            xstream.alias("substance", Substance.class);
            //xstream.addImplicitCollection(ErowidPsychoactiveVaults.class, "section");

            //xstream.alias("alternate-name-set", AlternateNameSet.class);
            //xstream.alias("image-set", ImageSet.class);
            //xstream.alias("image-entry", ImageEntry.class);

            //xstream.processAnnotations(ErowidPsychoactiveVaults.class);
            //xstream.processAnnotations(Section.class);
            xstream.processAnnotations(Substance.class);
            xstream.processAnnotations(AlternateNameSet.class);
            xstream.processAnnotations(ImageSet.class);
            xstream.processAnnotations(ImageEntry.class);

            xstream.ignoreUnknownElements("slang-name-set"); //could just pass blank unknown elements, but then would make hard to debug
            xstream.ignoreUnknownElements("experience-set");

            vault = (Substance) xstream.fromXML(chartXmlString);

           //long endTime = System.nanoTime();
            //Log.d("Time Test.", (endTime - startTime)/1000000000.0 + " s");
            return vault;
        }
        catch (Exception e)
        {
            Log.d("Error shared!.", " " + e);
            return null;
        }
    }

    // Pull substance from a parsed xml with only one substance in it. Uses getSubstanceFromXML
    public Substance getSubstanceFromShortXML(String psyType, String psyName, String absFileDir)
    {
        StringBuilder sb = new StringBuilder();
        try{
            String subFileName = absFileDir+"/chartXml/" + psyType.toLowerCase() + "/" + psyName;
            File subFile = new File(subFileName);
            FileInputStream fis = new FileInputStream(subFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            fis.close();
        } catch(OutOfMemoryError om){
            om.printStackTrace();
            return null;
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        String fileString = sb.toString();

        return getSubstanceFromXML(fileString);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * Using bigchart.xml is proving to be too slow, having to load and use the giant xml file
     * This method parses the xml once into a set of folders and with smaller substance xml files.
     *
     * Things to consider:
     *      This should probably delete all the folders before starting, old xml should just be wiped
     */
    public void splitVaultXmlUpAndStore(String chartXmlString, String absFilesDir)
    {

        //Very first, create a new substances folder (and delete the old instance)

        //Parse for each <section-name>
        //Create a folder for the section
        //File deleteRootFolderPath = new File("/chartXml/");
        //deleteRecursive(absFilesDir);

        String[] chartSectionsSplit = chartXmlString.split("<section *>"); //assuming the start of the string is split
        File mainFolderPath = new File(absFilesDir+"/chartXml/");
        mainFolderPath.mkdir();

        String UTF8 = "utf8";
        int BUFFER_SIZE = 8192;

        for(int i = 1; i < chartSectionsSplit.length; i++)
        {
            //make sure the non-existant section doesn't break this. it doesn't but make sure later doesn't
            String substanceType = chartSectionsSplit[i].substring(chartSectionsSplit[i].indexOf("<section-name>") + 14, chartSectionsSplit[i].indexOf("</section-name>"));
            if(substanceType != "")
            {
                //first, create a folder
                File storagePath = new File(absFilesDir+"/chartXml/" + substanceType.toLowerCase());
                storagePath.mkdir();
                //Then pull each substance xml
                //Throw each substance xml into a file in folder

                String[] chartSubstanceSplit = chartSectionsSplit[i].split("<substance>"); //assuming the start of the string is split
                for(int j = 1; j < chartSubstanceSplit.length; j++)
                {
                    String substanceName = chartSubstanceSplit[j].substring(chartSubstanceSplit[j].indexOf("<name>") + 6, chartSubstanceSplit[j].indexOf("</name>"));//[^a-zA-Z]
                    Log.d("Split Name: ", substanceType +" | " + substanceName );
                    String subFileName = absFilesDir+"/chartXml/" + substanceType.toLowerCase() + "/" + substanceName;
                    File subFile = new File(subFileName);

                    BufferedWriter bw = null; //(context.openFileOutput("files/big_chart_xml.php", Context.MODE_PRIVATE)
                    try {
                        bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(subFile), UTF8), BUFFER_SIZE );
                        bw.write("<substance>"+chartSubstanceSplit[j]);
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Then pull each substance xml
        //Throw each substance xml into the folder
    }

    /**
     * This is commented out because trying to generate new XML from the objects
     * was causing duplicate tags, and I couldn't figure out why.
     *
     * Instead, the code saves the big_chart and just uses that every time.
     */


//    public String getXMLFromPsyVault(ErowidPsychoactiveVaults vault)
//    {
//        try {
//
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            XmlPullParser xpp = factory.newPullParser();
//
//            XStream xstream = new XStream();
//
//
//            String objStructuredXML = xstream.toXML(vault);
//            return objStructuredXML;
//        }
//        catch (Exception e){
//            return null;
//        }
//
//    }

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
				instream.close(); //this is breaking
				//System.out.println("Content downloaded"); //debug 
				return content;
			} 
		}
		catch (Exception e)
		{
			//TODO: This exception catch totally broke things.
			e.printStackTrace();
		}

		return content;
	} 

	//Creates a map of report card info for use in PsychoNavigatorActivity
	public Map<String, String> getReportCardInfo(String url)
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
	public int indexAfter(String theString, String splitString)
	{
		int length = splitString.length();
		int index = theString.indexOf(splitString) + length; 
		return index;
	}

	void clearPsyChoicesList(Context context)
    {
        SharedPreferences.Editor edit= context.getSharedPreferences("VAULTTABLENEW", Context.MODE_PRIVATE).edit();
        edit.remove("table");
        edit.commit();
    }

    void clearOldPsyChoicesList(Context context)
    {
        SharedPreferences.Editor edit= context.getSharedPreferences("VAULTTABLE", Context.MODE_PRIVATE).edit();
        if(null != edit) {
            edit.remove("table");
            edit.commit();
        }
    }

    /**
	 * Takes the list of psychoactive information and saves it as a comma seperated SharedPreferences
	 */
	void storePsyChoicesList(List<String[]> psyTable, Context passedContext)
	{
		SharedPreferences.Editor edit= passedContext.getSharedPreferences("VAULTTABLENEW", Context.MODE_PRIVATE).edit();
		StringBuilder tableStringB = new StringBuilder();
		for(int i = 0; i < psyTable.size(); i++)
		{
			StringBuilder row = new StringBuilder();
			for(int j = 0; j < psyTable.get(0).length; j++)
			{
				row.append(psyTable.get(i)[j]).append("|");
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
     * Checks to see if there is an old version of the list, to redownload list after a bugfix/update
	 */
	List<String[]> getStoredPsyChoicesList(Context passedContext)
	{
        clearOldPsyChoicesList(passedContext);
        SharedPreferences prefs = passedContext.getSharedPreferences("VAULTTABLENEW", Context.MODE_PRIVATE);
		String tableString = prefs.getString("table", "");
		if(tableString.equals(""))
		{	//if there is no table to pull
			return null;
		}
		String[] splitTableStringArray = tableString.split("\n");
		List<String[]> psyTable = new ArrayList<String[]>();
		
		for (int i = 0; i < splitTableStringArray.length; i++) {
			//this probably adds an extra row of "" values on the end, after the last ,
			psyTable.add(splitTableStringArray[i].split("\\|"));
		}
		
		return psyTable;
	}
	
	public List<String> getOfflineSiteFilenameList(String path)
	{
		List<String> offlineFilenameList = new ArrayList<String>();
		File directory;
	    directory = new File(path);
	    for (File f : directory.listFiles()) {
	        if (f.isFile())
	        {
	            offlineFilenameList.add(f.getName());
	        }
	    }
	    return offlineFilenameList;
	}
	
	public List<String[]> getOfflineSiteFilenameAndDateList(String path)
	{ //getFilesDir().getPath();
		List<String[]> offlineFilenameAndDateList = new ArrayList<String[]>();
		File directory = new File(path);
	    for (File f : directory.listFiles()) {
	        if (f.isFile())
	        {
	        	String[] attributes = new String[2];
	        	attributes[0] = f.getName(); //filename
	        	attributes[1] = new SimpleDateFormat("dd-MM-yyyy").format(new Date(f.lastModified()));
	        	offlineFilenameAndDateList.add(attributes);
	        }
	    } 
	    return offlineFilenameAndDateList;
	}
	
	//Does the actual html-to-string creation
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
