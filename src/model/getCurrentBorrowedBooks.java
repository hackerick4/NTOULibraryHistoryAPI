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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class getCurrentBorrowedBooks  extends HttpServlet {
	public class BorrowedBook
    {
       public String title ="";
       public String status = "";
       public String bookURL = "";
       public String radioVal = "";
       public String barcode = "";
       public String call  = "";
    }	

	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
    	String account = request.getParameter("account");
    	String pwd = request.getParameter("password");
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
    
      /*detect individual location*/      
    	      String location = loginConnection.getHeaderField("Location"); 
    	      if (location == null){ out.println("Login failed"); return;}
    	      String[] myLocationTokens = location.split("/");    	     
    	      String myLocation = myLocationTokens[ 2 ];
    	     
    	
    	      
       /**get reserveInfo**/
  	    	Document chk_doc =Jsoup.connect("http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" +  myLocation + "/items")
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
  	   Elements currentBorBooks = chk_doc.select("html > body > div > form > table > tbody > tr > td ");
  	   int bookPosition = 1 , infoPerBooks = 5;
  	   JSONArray result = new JSONArray();
  	   for (int books_iter =0 ; books_iter < currentBorBooks.size()/infoPerBooks ; ++books_iter){
  		   BorrowedBook  borrowedBook  = new BorrowedBook();
  		   JSONObject j_borrowedBook= new JSONObject();
  		   borrowedBook.radioVal = currentBorBooks.get(bookPosition-1).select("input").attr("value");
  		   borrowedBook.title = currentBorBooks.get(bookPosition).select("label > a").text().replace("</span>", "");
  		   borrowedBook.bookURL = "http://ocean.ntou.edu.tw:1083" + currentBorBooks.get(bookPosition).select("label > a").attr("href").toString();
  		   borrowedBook.barcode = currentBorBooks.get(bookPosition+1).text();
  		   borrowedBook.status = currentBorBooks.get(bookPosition+2).text().replace("</td>", "");
  		   borrowedBook.call = currentBorBooks.get(bookPosition+3).text();
  		   bookPosition+=infoPerBooks;
	  		   try {
				j_borrowedBook.put("title", borrowedBook.title);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	  		   try {
				j_borrowedBook.put("bookURL", borrowedBook.bookURL);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	  		   try {
				j_borrowedBook.put("status", borrowedBook.status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	  		 try { 
					j_borrowedBook.put("barcode", borrowedBook.barcode);
		  		} catch (JSONException e) {
					e.printStackTrace();
		  		}
		  		try { 
					j_borrowedBook.put("call", borrowedBook.call);
		  		} catch (JSONException e) {
					e.printStackTrace();
		  		}
	  		  try { 
					j_borrowedBook.put("radioValue", borrowedBook.radioVal);
			} catch (JSONException e) {
					e.printStackTrace();
			}
	  		
  		 result.put(j_borrowedBook);	
  	   } //end of for
  	   
  	   out.println(result);
	}
}