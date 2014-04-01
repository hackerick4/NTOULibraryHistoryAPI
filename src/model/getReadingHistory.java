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
       public String tittle ="";
       public String borrowDate = "";
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
    	      String[] myLocationTokens = location.split("/");
    	      String myLocation = myLocationTokens[ 2 ];
    	     
    	
    	      /**start to fetch reading history**/
    	     String requestURL = "http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" +  myLocation + "/readinghistory";
    	     Connection.Response cr =Jsoup.connect(requestURL)
    	    		  .cookie("III_SESSION_ID", session)
    	    		  .cookie("III_EXPT_FILE" , "aa17054" )
    	    		  .cookie("SESSION_LANGUAGE","cht")
    	    		  .cookie("SESSION_SCOPE","0")
    	    		  .execute();
    	     Document doc = Jsoup.parse(cr.body(),"utf-8");
    	     
    	     
    	     /***** fetch History*****/
    	     Elements tittles_HTML = doc.select("html > body > div >form > table >tbody >tr > td >a");
    	     Elements borrowDates_HTML = doc.select("html > body > div > form > table > tbody > tr > td");
    	     
    	    
    	     
    	     /*** convert result to json string***/

    	     int NumberOfHistory = tittles_HTML.size() , borrowPostion = 3; //borrow is ar 4th td
    	     JSONArray result = new JSONArray();
    	     for (int historyIndex = (page-1)*10 ; historyIndex < (page-1)*10+10 ; ++historyIndex){
    	    	 if(historyIndex >= NumberOfHistory) break;
    	    	 History h = new History();
    	    	 h.tittle = tittles_HTML.get(historyIndex).html();
    	    	 h.borrowDate = borrowDates_HTML.get(borrowPostion).html();
    	    	 h.chkBox = historyIndex;
    	    	 JSONObject j_history = new JSONObject();
    	    	 try {
					j_history.put("tittle", h.tittle);
				} catch (JSONException e) {
					e.printStackTrace();
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
    	    	 borrowPostion+=5;
    	     }
    	     
    	     out.println(result);      
    }
   
}