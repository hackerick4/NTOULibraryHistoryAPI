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
import org.json.JSONException;
import org.json.JSONObject;



public class reserveBook  extends HttpServlet {
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
	    	String radioValue = request.getParameter("radioValue");	 
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
	    	 
	    	 /**start to fetch reserveBookRatio**/
	    	// int minRes=-1;
	    	// int bookTable_it,radio_it=0, minRadio=0;	 
	    	/* Elements bookTable = chk_doc.select("html > body > form > table > tbody > tr > td ") ;
	    	 
	    	 for ( bookTable_it = 4 ; bookTable_it < bookTable.size() ; bookTable_it +=5,radio_it+=5){
	    		 if (bookTable.get(bookTable_it).text().contains("在架上")) continue;
	    		 if (!bookTable.get(bookTable_it).text().contains("+")) { //nobody res this book
	    			 radioValue = bookTable.get(radio_it).select("input").attr("value");
	    			 break;
	    		 }
	    		 String resNumString = bookTable.get(bookTable_it).text();
	    		 resNumString = resNumString.substring(resNumString.indexOf("+")).replace(" 預約", "");
	    		 int t = Integer.parseInt(resNumString);
	    		 if (t < minRes){
	    			 minRes = t;
	    			 minRadio = radio_it;	    			 
	    		 }
	    		  radioValue = bookTable.get(minRadio).select("input").attr("value"); 	    	
	    	 }*/
	    	// out.println(radioValue);
	    	 
	        /**post the reserveMSG**/
	    	 Document reserveInfoDoc =Jsoup.connect(reserveURL)
	              .data("code" , account)
	              .data("pin" , pwd)
	              .data("radio",radioValue)
	              .data("submit.x" , "0")
	              .data("submit.y" , "0")
	              .data("submit","submit")
   	    		  .cookie("III_SESSION_ID", session)
   	    		  .cookie("III_EXPT_FILE" , "aa17054" )
   	    		  .cookie("SESSION_LANGUAGE","cht")
   	    		  .cookie("SESSION_SCOPE","0")
   	    		  .post();
	    	 
	    	 
	    	 /**fetch reserve successInfo**/
	    	 int bookLocationIndex = 0, bookDeadLineIndex = 3;
	    	 String booktitle= chk_doc.select("html > body > strong").text() ;
	    	
	    	 
	    	 //out.println(reserveInfoDoc);
	    	  Elements reserveInfo = reserveInfoDoc.select("html > body > center > table > tbody > tr >td");
	    	  Elements errMsg = reserveInfoDoc.select("html > body > center > p > font");
	    	  
	    	 
	    	  if (reserveInfo.hasText()) {
			    	 String bookLocation = reserveInfo.get(bookLocationIndex) . text();
			         String bookDeadLine = reserveInfo.get(bookDeadLineIndex) . text();
			         	         
			         /**prepare response**/
			         JSONObject jsonResponse = new JSONObject();

					        try {
								jsonResponse.put("querySuccess", "true");
							} catch (JSONException e) {
								e.printStackTrace();
							}
					         try {
									jsonResponse.put("title", booktitle);
								} catch (JSONException e) {
									e.printStackTrace();
								}
					         try {
									jsonResponse.put("status", bookDeadLine);
								} catch (JSONException e) {
									e.printStackTrace();
								}
					         try {
									jsonResponse.put("location", bookLocation);
								} catch (JSONException e) {
									e.printStackTrace();
								}
					         
					         out.println(jsonResponse);
			         
	    	  }	else {
	    		        JSONObject jsonResponse = new JSONObject();
			        	 try {
								jsonResponse.put("querySuccess", "false");
								jsonResponse.put("errorMsg", errMsg.text());
							} catch (JSONException e) {
								e.printStackTrace();
							}
			        	 out.println(jsonResponse);
	              }
	 }
	
}