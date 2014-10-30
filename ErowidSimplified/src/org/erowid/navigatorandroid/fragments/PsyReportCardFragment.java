package org.erowid.navigatorandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.erowid.navigatorandroid.PsychoNavigatorActivity;
import org.erowid.navigatorandroid.R;
import org.erowid.navigatorandroid.WebDisplayActivity;
import org.erowid.navigatorandroid.xmlXstream.Substance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PsyReportCardFragment extends Fragment {

    Substance substance;
    String psyName;
    String psyType;

    Boolean pullingReportCardText = false;

    View mMyView = null;

    Activity psychoActivity;
    org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        psychoActivity = getActivity();

    }

    @Override
    public void onResume()
    {
        super.onResume();

        substance = ((PsychoNavigatorActivity)getActivity()).getSubstance(); //
        Log.d("Substance in Frag", substance.getName());
        psyType = ((PsychoNavigatorActivity)getActivity()).getPsyType();
        psyName = substance.getName();

        String urlForGrab = "http://erowid.org/"+ psyType + "/" + psyName + "/";
        webContentAsyncTask myWebFetch = new webContentAsyncTask(urlForGrab, psyType, psyName);
        myWebFetch.execute();

        initializeButtons();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mMyView == null) {
            mMyView = inflater.inflate(R.layout.fragment_psy_report_card, container, false);
        } else {
            ((ViewGroup) mMyView.getParent()).removeView(mMyView);
        }

        return mMyView;
    }


    /**
     * Handles the button generation, which is dynamic depending on the content available for the psychoactive.
     * Blocks a few pages even though they exist, because I didn't want to support them (e.g. modafinil effects, the only effects in smarts)
     */
    private void initializeButtons()
    {
        ImageButton basicsButton = (ImageButton) psychoActivity.findViewById(R.id.basics_button);
        ImageButton effectsButton = (ImageButton) psychoActivity.findViewById(R.id.effects_button);
        ImageButton healthButton = (ImageButton) psychoActivity.findViewById(R.id.health_button);
        ImageButton lawButton = (ImageButton) psychoActivity.findViewById(R.id.law_button);
        ImageButton doseButton = (ImageButton) psychoActivity.findViewById(R.id.dose_button);

        Button imagesButton = (Button) psychoActivity.findViewById(R.id.images_button);
        Button chemistryButton = (Button) psychoActivity.findViewById(R.id.chemistry_button);
        //Button researchChemicalButton = (Button) psychoActivity.findViewById(R.id.research_chemical_button);	//not populated from general...
        //researchChemicalButton.setVisibility(View.INVISIBLE); //because its not being populated now

        TextView psychoName = (TextView) psychoActivity.findViewById(R.id.psychoName);

        psychoName.setText(Html.fromHtml("<b>" + substance.getNiceName() + "</b>")); //.replaceAll("_", " ")

        //For each button, check the table to see if it should be visible
        if(substance.getBasics() == null || substance.getBasics().isEmpty()) {
            basicsButton.setVisibility(View.GONE); }
        else{
            basicsButton.setVisibility(View.VISIBLE);
        }
        if(substance.getEffects() == null || substance.getEffects().isEmpty() || psyType.equals("smarts")) {
            effectsButton.setVisibility(View.GONE); }
        else{
            effectsButton.setVisibility(View.VISIBLE);}

        if(substance.getImages() == null || substance.getImages().isEmpty()) {
            imagesButton.setVisibility(View.GONE); }
        else{
            imagesButton.setVisibility(View.VISIBLE);
            //possiblePageTypes.add("images");
            //pagesCanBeHeld++; //images page isn't stored
        }
        if(substance.getHealth() == null || substance.getHealth().isEmpty() || psyType.equals("pharms") || psyType.equals("smarts")) {
            healthButton.setVisibility(View.GONE); }
        else{
            healthButton.setVisibility(View.VISIBLE);}

        if(substance.getLaw() == null || substance.getLaw().isEmpty()) {
            lawButton.setVisibility(View.GONE); }
        else{
            lawButton.setVisibility(View.VISIBLE);}

        if(substance.getDose() == null || substance.getDose().isEmpty()) {
            doseButton.setVisibility(View.GONE); }
        else{
            doseButton.setVisibility(View.VISIBLE);}


        //not used currently
        if(substance.getChemistry() == null || substance.getChemistry().isEmpty()) {
            chemistryButton.setVisibility(View.GONE); }
        else{
            chemistryButton.setVisibility(View.VISIBLE); }

        //Set all click listeners.
        basicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
                        intent.putExtra("SENT_PSYCHOACTIVE", psyName);
                        intent.putExtra("SENT_PSY_TYPE", psyType);
                        intent.putExtra("SENT_PAGE", "basics");
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        effectsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
                        intent.putExtra("SENT_PSYCHOACTIVE", psyName);
                        intent.putExtra("SENT_PSY_TYPE", psyType);
                        intent.putExtra("SENT_PAGE", "effects");
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        imagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
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
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        healthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
                        intent.putExtra("SENT_PSYCHOACTIVE", psyName);
                        intent.putExtra("SENT_PSY_TYPE", psyType);
                        intent.putExtra("SENT_PAGE", "health");
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        lawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
                        intent.putExtra("SENT_PSYCHOACTIVE", psyName);
                        intent.putExtra("SENT_PSY_TYPE", psyType);
                        intent.putExtra("SENT_PAGE", "law");
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        doseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
                        intent.putExtra("SENT_PSYCHOACTIVE", psyName);
                        intent.putExtra("SENT_PSY_TYPE", psyType);
                        intent.putExtra("SENT_PAGE", "dose");
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        chemistryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true) // was if(item != null) , dunno why
                {
                    if (m.isOnline(psychoActivity.getBaseContext()))
                    {
                        //This loads in the browser instead of through the internal webview, because the formatting was broken.
                        //Could be changed later
                        String pageURL = "http://erowid.org/"  + psyType + "/" + psyName + "/" + psyName + "_chemistry.shtml";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pageURL));
                        //"http://erowid.org/" + psyType + "/" + psyName + "/images/" + psyName
                        startActivity(browserIntent);

                        //Old code for loading chemistry internally.

//						Intent intent = new Intent(psychoActivity.getBaseContext(), WebDisplayActivity.class);
//						intent.putExtra("SENT_PSYCHOACTIVE", psyName);
//						intent.putExtra("SENT_PSY_TYPE", psyType);
//						intent.putExtra("SENT_PAGE", "chemistry");
//						startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(psychoActivity.getBaseContext(), "Reconnect to the internet" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    } // End setting click listeners

    //Web Content population class
    class webContentAsyncTask extends AsyncTask<Void, Void, Void> {
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
        WebView infoWebView = (WebView) psychoActivity.findViewById(R.id.infoWebView);

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
            //TextView psyDescription = (TextView) findViewById(R.id.psychoactiveDescription);
            //psyDescription.setText("Loading...");
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
                final ImageView psyImageView = (ImageView) psychoActivity.findViewById(R.id.psyImage);
                psychoActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    try {

                        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
                        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 225, getResources().getDisplayMetrics());
                        psyImageView.getLayoutParams().height = height;
                        psyImageView.getLayoutParams().width = width;
                        //psyImageView.requestLayout();
                        psyImageView.setImageBitmap(psyImageBitmap);
                        //calculating the dpi for the image height/width once we know we have an image.
                        //not based on attributes of
                    }
                    catch (Exception e)
                    {

                    }
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

            try {
                //Create the scrolling textbox containing text parsed out of the web content.
                TextView psyDescription = (TextView) psychoActivity.findViewById(R.id.psychoactiveDescription);

                if (!psyType.equalsIgnoreCase("herbs")) {
                    psyDescription.setText(Html.fromHtml(
                            "<b>Effects Classification:</b><br>" + effects + "<br/>" +
                                    "<b>Description:</b><br>" + description + "<br/>" +
                                    "<b>Common Names:</b><br>" + common_names));
                } else { //herbs does not have effects class, so don't show.
                    psyDescription.setText(Html.fromHtml(
                            "<b>Description:</b><br>" + description + "<br/>" +
                                    "<b>Common Names:</b><br>" + common_names));
                }
                psyDescription.setMovementMethod(new ScrollingMovementMethod());
            }
            catch (Exception e)
            {

            }
            pullingReportCardText = false;
        }
    }

}