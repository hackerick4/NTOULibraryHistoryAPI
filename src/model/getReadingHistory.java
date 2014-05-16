package model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 
// Extend HttpServlet class
public class getReadingHistory  extends HttpServlet {
	 
    public class History
    {
       public String title ="";
       public String borrowDate = "";
       public String bookDetailURL = "";
       public String detail = "";
       public int  chkBox = 0;
    }

	private static final long serialVersionUID = 1L;
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	response.setContentType("text/html;charset=utf-8");
    	String account = request.getParameter("account");
    	String pwd = request.getParameter("password");
    	int page = Integer.parseInt( request.getParameter("segment"));
    	PrintWriter out = response.getWriter();
    	
    	/*****prepare the parameters*****/
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
    	      //out.println(session);
       
    	/*detect individual location*/      
    	      String location = loginConnection.getHeaderField("Location"); 
    	      if (location == null){ out.println("Login failed"); return;}
    	      String[] myLocationTokens = location.split("/");
    	      String myLocation = myLocationTokens[ 2 ];    	    

    	/**start to fetch reading history**/
    	    String requestURL = "http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" +  myLocation + "/readinghistory";
    	     Connection.Response cr =Jsoup.connect(requestURL)
    	    		  .cookie("III_SESSION_ID", session)
    	    		  .cookie("III_EXPT_FILE" , "aa17054" )
    	    		  .cookie("SESSION_LANGUAGE","cht")
    	    		  .cookie("SESSION_SCOPE","0")
    	    		  .timeout(15*1000)
    	    		  .execute();
    	     
    	     
    	    /*String requestURL = "http://127.0.0.1:8080/LibraryHistoryAPI/LibraryTest.html";
     	     Connection.Response cr =Jsoup.connect(requestURL)
     	    		   .execute();*/
    	    
    	      
    	/*  String requestURL = "http://127.0.0.1:8080/LibraryHistoryAPI/bigReadingHistoyData.html";
     	     Connection.Response cr =Jsoup.connect(requestURL)
     	    		   .execute();*/
    	     
    	     
    	     Document doc = Jsoup.parse(cr.body(),"utf-8");
    	     
    	     
	     /***** fetch History*****/
	     Elements tittles_HTML = doc.select("html > body > div >form > table >tbody >tr > td >a");
	     Elements borrowDates_HTML = doc.select("html > body > div > form > table > tbody > tr > td");
	    
	     
	     /*** convert result to json string***/

	     int NumberOfHistory = tittles_HTML.size() , borrowPostion = 3 + (NumberOfHistory-1) * 5; //borrow is at 4th td
	     JSONArray result = new JSONArray();
	     for (int historyIndex  = NumberOfHistory - ( (page-1)*10 )-1 ; historyIndex > NumberOfHistory - ( (page-1)*10 ) -10 -1  ; --historyIndex){
	    	 
	    	 if(historyIndex < 0) break;
	    	 History h = new History();
	    	 h.title = tittles_HTML.get(historyIndex).text();
	    	 h.bookDetailURL = tittles_HTML.get(historyIndex).attr("href");
	    	 h.borrowDate = borrowDates_HTML.get(borrowPostion).text();
	    	 h.detail = borrowDates_HTML.get(borrowPostion+1).text();
	    	 h.chkBox = historyIndex;
	    	 JSONObject j_history = new JSONObject();
	    	 try {
				j_history.put("title", h.title);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    	 try {
					j_history.put("detail", h.detail);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	 try {
				j_history.put("bookDetailURL", "http://ocean.ntou.edu.tw:1083/" + h.bookDetailURL);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
	    	 
	    	 try {
				j_history.put("borrowDate", h.borrowDate);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    	 try {
				j_history.put("chkBox", h.chkBox);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    	 result.put(j_history);
	    	 borrowPostion -=5;
	     }
 
	      out.println(result);      
    }
   
}