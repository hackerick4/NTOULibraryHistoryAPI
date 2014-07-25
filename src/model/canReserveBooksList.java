package model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class canReserveBooksList  extends HttpServlet {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
		 
		 /*****prepare the parameters*****/
	    	response.setContentType("text/html;charset=utf-8");
	    	String account = request.getParameter("account");
	    	String pwd = request.getParameter("password");	 
	    	String reserveURL = request.getParameter("reserveURL");
	    	reserveURL = reserveURL.replaceAll("(ANDCHAR)", "&");
	    	PrintWriter out = response.getWriter();
	    	
	    	String urLoginlParameters = "code=" + account + "&pin=" + pwd + "&submit.x=0&submit.y=0&submit=submit";
	    	String requestLoginURL = "http://ocean.ntou.edu.tw:1083/patroninfo*cht";
	    	URL loginURL = new URL(requestLoginURL);
	    	HttpURLConnection loginConnection = (HttpURLConnection) loginURL.openConnection();
	    	loginConnection.setDoOutput(true);
	    	loginConnection.setDoInput(true);
	    	loginConnection.setInstanceFollowRedirects(false); 
	    	loginConnection.setRequestMethod("POST"); 
	    	loginConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
	    	loginConnection.setRequestProperty("charset", "iso-8859-1");
	    	loginConnection.setRequestProperty("Content-Length", "" + Integer.toString(urLoginlParameters.getBytes().length));
	    	loginConnection.setUseCaches (false);
	    	
	    	
	    	/** send login request**/
	        DataOutputStream wr = new DataOutputStream ( loginConnection.getOutputStream ());
	    		wr.writeBytes (urLoginlParameters);
	    		wr.flush ();
	    		wr.close ();
	    	
	    		
	    	/**detect session**/
	  	      	  String cookie = loginConnection.getHeaderField("Set-Cookie"); 
	    	      String[] cookies = cookie.split(";");
	    	      String[] sessionString = cookies[ 0 ].split("=");
	    	      String session = sessionString[ 1 ];
		     
    	    /*detect individual location*/      
	    	      String location = loginConnection.getHeaderField("Location"); 
	    	      if (location == null){ out.println("Login failed"); return;}


	    	 Document chk_doc =Jsoup.connect(reserveURL)
	              .data("code" , account)
	              .data("pin" , pwd)
	              .data("submit.x" , "0")
	              .data("submit.y" , "0")
	              .data("submit","submit")
   	    		  .cookie("III_SESSION_ID", session)
   	    		  .cookie("III_EXPT_FILE" , "aa17054" )
   	    		  .cookie("SESSION_LANGUAGE","cht")
   	    		  .cookie("SESSION_SCOPE","0")
   	    		  .post();
	    	// out.println(chk_doc);
	    	 
	    	 /**start to fetch reserveBookList**/
	         int bookTable_it;	 
	    	 Elements bookTable = chk_doc.select("html > body > form > table > tbody > tr > td ") ;
	    	 JSONArray result = new JSONArray();
	    	 for ( bookTable_it = 0 ; bookTable_it < bookTable.size() ; bookTable_it +=5){
	    		 if (bookTable.get(bookTable_it+4).text().contains("¦b¬[¤W")) continue;
	    		 else {
	    			 String radioVal = bookTable.get(bookTable_it).select("input").attr("value");
	    			 String bookLocation = bookTable.get(bookTable_it+1).text();
	    			 String bookCall = bookTable.get(bookTable_it+2).text();
	    			 String bookCode = bookTable.get(bookTable_it+3).text();
	    			 String bookResStatus = bookTable.get(bookTable_it+4).text();
	    			 JSONObject resBook = new JSONObject();
	    		   try {
						resBook.put("radioVal", radioVal);
					} catch (JSONException e) {
						e.printStackTrace();
					}
	    			 try {
						resBook.put("bookLocation", bookLocation);
					} catch (JSONException e) {
						e.printStackTrace();
					}
	    			 try {
						resBook.put("bookCall", bookCall);
					} catch (JSONException e) {
						e.printStackTrace();
					}
	    			 try {
						resBook.put("bookCode", bookCode);
					} catch (JSONException e) {
						e.printStackTrace();
					}
	    			 try {
						resBook.put("bookResStatus", bookResStatus);
					} catch (JSONException e) {
						e.printStackTrace();
					}	    	 
	    			 	    		 
	    			 result.put(resBook);
	    		 }
	    	 }//end fetch
	    			   
	    	  out.println(result);         
    }
}