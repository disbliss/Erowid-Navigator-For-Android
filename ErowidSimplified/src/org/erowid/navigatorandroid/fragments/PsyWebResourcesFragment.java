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
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.erowid.navigatorandroid.PsychoNavigatorActivity;
import org.erowid.navigatorandroid.R;
import org.erowid.navigatorandroid.xmlXstream.Substance;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PsyWebResourcesFragment extends Fragment {

    org.erowid.navigatorandroid.SharedMethods m = new org.erowid.navigatorandroid.SharedMethods();
    Activity psychoActivity;
    Substance substance;
    WebView webView;

    View mMyView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        psychoActivity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mMyView == null) {
            mMyView = inflater.inflate(R.layout.fragment_psy_web_resources, container, false);
        } else {
            ((ViewGroup) mMyView.getParent()).removeView(mMyView);
        }

        return mMyView;
    }

    @Override
    public void onResume() {
        super.onResume();

        substance = ((PsychoNavigatorActivity) getActivity()).getSubstance();
        webView = (WebView) psychoActivity.findViewById(R.id.resourcesWebView);
        String urlForGrab = "http://erowid.org" + substance.getVault();
        webContentAsyncTask myWebFetch = new webContentAsyncTask(urlForGrab);
        myWebFetch.execute();


    }

    class webContentAsyncTask extends AsyncTask<Void, Void, Void> {

        //Other variables
        String url = "";
        String content;

        webContentAsyncTask(String passedUrl) {
            url = passedUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * This contains the actual webCall takes its returned content to populate strings for use in the view.
         */
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                //Map<String, String> content = new HashMap<String, String>();
                content = m.getWebContent(url);

                //this is so inefficient
                content = content.substring(content.indexOf("<div class=\"index-links-int\">")); // first gets the first half the string
                content = content.substring(0, content.indexOf("</div><!-- end index-links-ext -->")); // then gets the second half


                content = "<body>" +
                        "<style>" +
                        ".links-list:not(:first-child)" +
                        "{" +
                        "padding-top: 25px;" +
                        "}" +
//                        ".body{ " +
//                        "text-align: center;" +
////                        ".link-int:before {" +
////                        "content: \"\u2022 \"; " +
////                        "margin-left: -5px" +
//
//                        "    }" +
                        "body{" +
                        "background-color: #b0c4de;" +
                        "text-align: center;" +
                        "}" +
//                        "body {" +
//                    " background-color: linen;" +
//                " } " +
//                "h1 { " +
//                    " color: maroon;" +
//                    " margin-left: 40px;" +
//                " } " +

                        //"    display: list-item;\n" +
                       // "    list-style-type: disc;\n" +

                "</style>" +
                        "</body>"
                        + content;

                //content.put("Description", description);
            } catch (Exception e) {

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

                webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
                webView.getSettings().setTextZoom(120);
                webView.setWebViewClient(new WebViewClient() {
//                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                        return true;
                    }
                });
                //webView.loadData("http://erowid.org", "text/html", null);
                String filesPath = "http://erowid.org/"
                        +((PsychoNavigatorActivity)getActivity()).getPsyType()+"/"
                        +substance.getName()+"/";
                webView.loadDataWithBaseURL(filesPath, content, "text/html", "UTF-8", null); //content
                // /webView.loadDataWithBaseURL(filesPath, "hello", "text/html", "UTF-8", null); //content
//                TextView webResTextView = (TextView) psychoActivity.findViewById(R.id.webResourcesText);
//                webResTextView.setMovementMethod(new ScrollingMovementMethod());
//                webResTextView.setText(Html.fromHtml(content));
//
//                webResTextView.setMovementMethod(LinkMovementMethod.getInstance()); //
            } catch (Exception e) {

            }
        }
    }
}
