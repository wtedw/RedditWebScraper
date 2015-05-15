import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.StringBuilder;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RedditTrends5 {

    public static void main(String[] args) throws IOException {
         String htmlUpvoteKey	=	"<div class=\"score unvoted\">";
         String htmlRankKey		=	"<span class=\"rank\">";
         String htmlTitleKey	=	"<p class=\"title\">";
         String htmlWebsiteKey	=	"<span class=\"domain\">";
         String htmlTimeKey		=	"<p class=\"tagline\">";
         String htmlCommentKey	=	"<li class=\"first\">";
         String htmlEndKey		=	"<span class=\"nextprev\">";
         
         String textUserKey		=	"&#32;by&#32;";
         //String site			=	"http://www.reddit.com/";
         String site			=	"http://www.reddit.com/r/askreddit";
         
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         Date date = new Date();
         
         String fileName = dateFormat.format(date) + ".txt";
         File file = new File(fileName);
         FileWriter fw = new FileWriter(file);
         BufferedWriter bw = new BufferedWriter(fw);
		
         // Reserved for future 75 entries
         // After 25 entries, reddit blocks all "bots"
         for (int i = 1; i <= 1; i++) {
         	
         	URL url = new URL(site);
	        URLConnection con = url.openConnection();
	        InputStream is = con.getInputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
         	
         	for (int j = (i*25 - 24); j <= (i*25); j++) {
		       
		       System.out.println("OuterI = " + i + " InnerJ = " + j);
		       String dummy			= 	"";
		       String upvotes		=	"Upvotes: ";
		       String rank			= 	"Rank: ";
		       String website		= 	"Website: ";
		       String title			= 	"Title: ";
		       String rawTime		= 	"";
		       String time			= 	"Time: ";
		       String user			= 	"User: ";
		       String numComments	=	"# Comments: ";
		       StringBuilder data	=	new StringBuilder();
		       
		       boolean found = false;
								
				/*
				*	There are new entries that are within the reddit html source
				*	Hardcoding this, but after htmlRankKey, there isn't anything
				*	after it, therefore we know it's a shady entry.
				*	
				*	Therefore we have to runthrough the first 9 / 10 or so
				*	dummy entries
				*/
				
				do {
					runThrough(br, htmlRankKey);
					dummy = getText(br);
					
					if (dummy.equals("")) {
						//System.out.println("Dummy");	//It's a dummy entry
					} else {
						found = true;
						stall();
						// Get rank
						rank += j;
						
						//Get # of upvotes
						runThrough(br, htmlUpvoteKey);
						upvotes += getText(br);						
						
						// Get title
						runThrough(br, htmlTitleKey);
						skipElements(br, 1);
						title += getText(br);
						
						// Get website
						runThrough(br, htmlWebsiteKey);
						skipElements(br, 1);
						website += getText(br);
						
						// Get time
						runThrough(br, htmlTimeKey);
						rawTime = getElement(br);
						time += getRefinedTime(rawTime);
						
						// Get user 2.0
						runThroughText(br, textUserKey);
						user += getText(br);
						
						// Get # comments
						runThrough(br, htmlCommentKey);
						skipElements(br, 1);
						numComments += getText(br);
						
						// Put into StringBuilder
						data.append(rank);
						data.append(" | ");
						data.append(upvotes);
						data.append(" | ");
						data.append(title);
						data.append(" | ");
						data.append(website);
						data.append(" | ");
						data.append(time);
						data.append(" | ");
						data.append(user);
						data.append(" | ");
						data.append(numComments);
						data.append(" | ");
						
					}
								
					System.out.println();
					System.out.println(data.toString());
					System.out.println();
					
					if (j == (i*25))
						found = true;
					
				} while (!found);
				
				bw.write(rank);
				bw.newLine();
				bw.write(upvotes);
				bw.newLine();
				bw.write(title);
				bw.newLine();
				bw.write(website);
				bw.newLine();
				bw.write(time);
				bw.newLine();
				bw.write(user);
				bw.newLine();
				bw.write(numComments);
				bw.newLine();
				bw.write('=');
				bw.newLine();
				
			}
			
			// End of inner loop / viewing 25 entries
			runThrough(br, htmlEndKey);
			site = getElement(br);
			site = site.substring( nthOccurrence(site, '"',0) + 1, nthOccurrence(site, '"', 1) );
			site = site.replace("&amp;", "");	// Unicode something, but messes program up
			//site = site.substring(0, nthOccurrence(site, '&', 0));
			System.out.println(site);
			
			is.close();
			br.close();
			
			System.out.println("Outer: " + i);
         // End of outerloop	
         	
         }
         bw.close();         	  
    }
    
    /*
     * Input:	BufferedReader, String of the form < ... >
     * Output:	None
     * Result:	Will have "run through" the match string
     *			Next char will not be '>' , but the char after
     */ 
    public static void runThrough(BufferedReader br, String match) throws IOException {
    	String htmlElement = "";
    	char input; //Don't think this is necessary anymore
    	
    	do 
    		htmlElement = getElement(br);
    		
    	} while (!htmlElement.equals(match));
    }
    
    /*
     * Input:	BufferedReader, String to find / run through
     * Result:	Will find the match string and will run through it
     * Warning: It will take in a '<' in order to end, keep this in mind
     */
    public static void runThroughText(BufferedReader br, String match) throws IOException {
    	String text = "";
    	do {
    		text = getText(br);    		    		
    	} while (!text.equals(match));
    }
    
    /*
     * Input:	BufferedReader
     * Output:	Returns the next < ... > 
     * Warning:	Have to have read a < first
     *			Therefore, suppose getText() is executed
     *			It needs to read a < to end
     *			If you do getElement(), it will overlook the next element that's actually there
     */
    public static String getElement(BufferedReader br) throws IOException {
    	char input;
    	String ret = ""; // Found an element, have to append extra
    	boolean canBegin = false;
    	boolean canEnd = false;
    	
    	//Goes through junk if there is
		while (!canBegin) {
			input = (char) br.read();
			
			if (input == '<') {
    			canBegin = true;
    			ret += input;
			}
		}
		
		while (!canEnd) {
			input = (char) br.read();
			
			if (input == '>') {
				canEnd = true;
				ret += input;
			} else {
				ret += input;
			}
			
		}
			
		return ret;
    }
    
    /*
     * Input:	BufferedReader, Number of elements to skip
     * Result:	Skips '>' part of an element skipNum amounts of time
     */  
    public static void skipElements(BufferedReader br, int skipNum) throws IOException{
		for (int i = 0; i < skipNum; i++) {
			char input = (char) br.read();
	
			while (input != '>') {
				input = (char) br.read();
			}	
		}
    }
    
    /*
     * Input:	BufferedReader
     * Output:	The text located between <...>Text<...>
     *			Text is begins with =Text, (it was easier to code it like so)
     * Result:	This method will read a text, then a '<' to indicate it to stop
     *			Keep in mind
     *
     * Assumes the next character will be pure text
     * If it detects a >, method realizes that it's not pure text
     * Instead, it was reading part of an html element.
     * 
     * Therefore, once it detects a >, it will try again
     * assuming the next reading will be pure text
     */
    public static String getText(BufferedReader br) throws IOException {
    	String ret = "";
    	char input = '=';	// Dummy character
    	boolean gucci = false;

    	while (!gucci) {

    		input = (char) br.read();
	    	
	    	switch (input) {
	    		case '>':
	    			ret = "";
	    			break;
	    		case '<':
	    			gucci = true;
	    			break;
	    		default:
	    			ret += input;
	    	}
    	}

    	return ret;    	
    }
    
    /*
     * Input:	String of the form
     *			<time title="Thu Jan 29 ... " ... "2015-01-29T12:17:32-08:00" ...">
     * Output:	Gets the text between 3rd or 4th quotes
     *			In this case
     *			"2015-01-29T12:17:32-08:00"
     */ 
    public static String getRefinedTime(String s) {
    	int index = nthOccurrence(s, '"', 2);
    	int index2 = nthOccurrence(s, '"', 3);
    	
    	return s.substring(index+1, index2);
    }
    
    /*
     * Input:	Any string, character to look for, n = occurence
     * Output:	Int indicating the position of the nth occurence of c
     * 
     * Taken from aioobe of Stackoverflow.com
     * Thanks
     */
    public static int nthOccurrence(String str, char c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}
    
    /*
     * Input:	none
     * Output:	Delays the program a wee bit
     *
     * For debugging purposes
     */
    public static void stall() {
    	try {
			    Thread.sleep(500);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
    }
    
}
