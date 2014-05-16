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


public class getCurrentHolds  extends HttpServlet {
	  public class ReserveBook
	    {
	       public String tittle ="";
	       public String status = "";
	       public String location = "";
	       public String radioValue = "";
	       public String bookURL = "";
	       public int  chkBox = 0;
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
	    	Document chk_doc =Jsoup.connect("http://ocean.ntou.edu.tw:1083/patroninfo~S0*cht/" + myLocation +"/holds")
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
	    	
	    	
	    	//**fetch infomation*//
	    	 Elements reserveBookTittleList = chk_doc.select("html > body > div >form > table > tbody > tr > td > label > a ");
	    	// out.println(reserveBookTittleList);
	    	 Elements reserveBookAnotherInfo = chk_doc.select("html > body > div >form > table > tbody > tr > td ");
	    	
	    	// out.println(reserveBookAnotherInfo);
	    	 
	    	 int reserveInfoPosition= 1, reserveBookCount = reserveBookTittleList.size();
	    	 JSONArray result = new JSONArray();
	    	 for (int chkBox = 0 ; chkBox < reserveBookCount ; ++chkBox ){
	    		 ReserveBook  reservebook  = new ReserveBook();
	    		 JSONObject j_reservebook = new JSONObject();
	    		 reservebook.tittle = reserveBookTittleList.get(chkBox). text();
	    		 reservebook.bookURL = reserveBookTittleList.get(chkBox). attr("href");
	    		 reservebook.radioValue = reserveBookAnotherInfo.get(reserveInfoPosition-1).select("input").attr("id").substring(6);
	    		 reservebook.status = reserveBookAnotherInfo.get(reserveInfoPosition+1) . text();
	    		 reservebook.location = reserveBookAnotherInfo.get(reserveInfoPosition+2) . text();
	    		  try {
					j_reservebook.put("title",  reservebook.tittle);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    		 try {
					j_reservebook.put("status",  reservebook.status);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    		 try {
					j_reservebook.put("location",  reservebook.location);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    		 try {
						j_reservebook.put("chkBox", chkBox);
					} catch (JSONException e) {
						e.printStackTrace();
				}
	    		 try {
						j_reservebook.put("bookDetailURL",   "http://ocean.ntou.edu.tw:1083/" + reservebook.bookURL);
					} catch (JSONException e) {
						e.printStackTrace();
				}
	    		 try {
						j_reservebook.put("radioValue",  reservebook.radioValue);
					} catch (JSONException e) {
						e.printStackTrace();
				}
	    		 reserveInfoPosition +=5;
	    		 result.put(j_reservebook);
	    	 }
	    	  out.println(result);    
	    	 
	}
	 
}