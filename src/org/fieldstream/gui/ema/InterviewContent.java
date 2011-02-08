//Copyright (c) 2010, University of Memphis
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification, are permitted provided 
//that the following conditions are met:
//
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
//      the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
//      and the following disclaimer in the documentation and/or other materials provided with the 
//      distribution.
//    * Neither the name of the University of Memphis nor the names of its contributors may be used to 
//      endorse or promote products derived from this software without specific prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
//WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
//PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
//ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
//TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
//NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
//
/**
 * 
 */
package org.fieldstream.gui.ema;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fieldstream.Constants;

import android.os.Environment;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Andrew Raij
 *
 */
public class InterviewContent implements IContent {
	static private InterviewContent INSTANCE = null;
	
	
	private static class Question {
		private int id;
		private String text;

		// A -1 response means the question isn't really a question, or is 
		// a header/title for the next question
		private int qresponseID;
		
		// If not -1, this question is conditional on the response to the 
		// specified question.  This question should appear if the index of the 
		// response choice of the specified question corresponds to this conditional
		private int qconditionalOnQuestionID;
		private int qconditionalResponseChoice;
		private boolean qmultipleChoice;
		
		public Question(int id, String text, int responseID, int conditionalOnQuestionID, int conditionalResponseChoice, boolean multipleChoice) {
			this.id = id;
			this.text = text;
			this.qresponseID = responseID;
			this.qconditionalOnQuestionID = conditionalOnQuestionID;			
			this.qconditionalResponseChoice = conditionalResponseChoice;
			this.qmultipleChoice = multipleChoice;
		}
				
		public int getID() { return id; }
		public String getText() { return text; }
		public int getResponseID() { return qresponseID; }
		public int getConditionalQuestionID() { return qconditionalOnQuestionID; }
		public int getConditionalResponseChoice() { return qconditionalResponseChoice; }
		public boolean isMultipleChoice() {return qmultipleChoice;}
	}
	
	private static class Response {
		private int id;
		private String[] choices;
		private boolean freeResponse;
		
		public Response(int id, String[] choices, boolean freeResponse){
			this.id = id;
			this.choices = choices;
			this.freeResponse = freeResponse;
		}
		
		public int getID() {return id; }
		public String[] getChoices() {return choices; }
		public boolean isFreeResponse() {return freeResponse;}
		
		public void setResponseFields(int id, String[] choices, boolean freeResponse){
			this.id = id;
			this.choices = choices;
			this.freeResponse = freeResponse;		
		}
	}

    // array contains all possible response lists used by interview
	public Response[] responses;
	public Question[] questions;
	

	
public void loadResponses(){
	
	File root = Environment.getExternalStorageDirectory();
	File dir = new File(root+"/"+Constants.CONFIG_DIR);
	dir.mkdirs();

	File questionFile = new File(dir, Constants.EMA_QUESTION_CONFIG_FILENAME);
	if (!questionFile.exists())
		return;
	
	DocumentBuilderFactory docBuilderFactory = null;
	DocumentBuilder docBuilder = null;
	Document doc = null;
	
	    try {

            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            
			doc = docBuilder.parse(questionFile); // normalize text representation
            doc.getDocumentElement().normalize();
            System.out.println ("Root element of the doc is: " + doc.getDocumentElement().getNodeName()); 
	   
	    }
	    catch (SAXParseException err) {
	        System.out.println (" Parsing error" + ", line " + err.getLineNumber () + ", uri "+ err.getSystemId ());
	        System.out.println("" + err.getMessage ()); }
	        catch (SAXException e) {
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace (); }
	        
	        catch (Throwable t) {
	        t.printStackTrace ();
	        }
	        
    
	        NodeList listOfResponses = doc.getElementsByTagName("response");
            
            
            int totalResponses=0;
            if (listOfResponses!=null)
            	totalResponses =  listOfResponses.getLength();
            
            
            responses = new Response[totalResponses];
            
            
            for(int iterator=0; iterator<listOfResponses.getLength() ; iterator++){
            	Node responseNode = listOfResponses.item(iterator);
            	 if(responseNode!=null && responseNode.getNodeType() == Node.ELEMENT_NODE){
            		 Element responseElement = (Element)responseNode;
            		 //Element firstPersonElement = (Element)firstPersonNode; //——-
                     NodeList response_idList = responseElement.getElementsByTagName("response_id");
                     Element response_idElement = (Element)response_idList.item(0); 
                     NodeList textresponse_idList = response_idElement.getChildNodes();                                         
                     int qid = Integer.parseInt(((Node)textresponse_idList.item(0)).getNodeValue().trim());
            
                     
                     NodeList listOfStrings = responseElement.getElementsByTagName("response_string");
                     int totalStrings = 0;
                     
                     if (listOfStrings!= null)
                    	 totalStrings =  listOfStrings.getLength();
                     String response_string[] = new String[totalStrings]; 
                     if(totalStrings != 0){
                    	 for(int striterator = 0; striterator<listOfStrings.getLength();striterator++){
                    		 Node stringsNode = listOfStrings.item(striterator);
                    		 if(stringsNode!=null && stringsNode.getNodeType() == Node.ELEMENT_NODE){
                    			 Element stringElement = (Element)stringsNode;
                    			 NodeList stringList = stringElement.getElementsByTagName("response_string");
                    			 Element sElement = (Element)stringList.item(0);
                    			 NodeList textstringList = sElement.getChildNodes();
                    			 String str = textstringList.item(0).getNodeValue().trim();
                    			 response_string[striterator] = str;
                    		 }
                    		 
                    	 }
                    		 
                     }
                     
                     
                     //NodeList response_stringList = questionElement.getElementsByTagName("response_string");
                     //Element question_stringElement = (Element)question_stringList.item(0); 
                     //NodeList textquestion_stringList = question_stringElement.getChildNodes();                                         
                     //String qstring = ((Node)textquestion_stringList.item(0)).getNodeValue().trim();
                     
                     // System.out.println("First Name : " + ((Node)textFNList.item(0)).getNodeValue().trim()); //——-
                    // boolean ismcq = Boolean.parseBoolean(multipleChoiceElement.getNodeValue().toString());
                     
                     //rid = 11;
                     //cqid =1;
                     //crc =1;
                     //ismcq = false;
                     responses[iterator]= new Response(iterator, response_string,false);
              		 /*here the arrays to keep the components for the questions will be filled*/
            	 }
            }         
}	
	
public void loadQuestions(){
		
	File root = Environment.getExternalStorageDirectory();

	File dir = new File(root+"/"+Constants.CONFIG_DIR);
	dir.mkdirs();

	File questionFile = new File(dir, Constants.EMA_QUESTION_CONFIG_FILENAME);
	if (!questionFile.exists())
		return;
	
	
/*
	//responses = new Response[totalQuestions];
	  responses = new Response[22];
      responses[0] = new Response(0, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      //responses[0] = new Response(0, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[1] = new Response(1, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[2] = new Response(2, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[3] = new Response(3, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[4] = new Response(4, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[5] = new Response(5, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[6] = new Response(6, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[7] = new Response(7, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[8] = new Response(8, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[9] = new Response(9, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[10] = new Response(10, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[11] = new Response(11, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[12] = new Response(12, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[13] = new Response(13, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[14] = new Response(14, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[15] = new Response(15, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[16] = new Response(16, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[17] = new Response(17, new String[] {"NO!", "No!", "no", "yes", "YES", "YES!"}, false);
      responses[18] = new Response(18, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false);
      responses[19] = new Response(19, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false);
      responses[20] = new Response(20, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false);
      responses[21] = new Response(21, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false);

*/	
	
	
	
	/*File root = Environment.getExternalStorageDirectory();

	File dir = new File(root+"/"+Constants.CONFIG_DIR);
	dir.mkdirs();

	File questionFile = new File(dir, Constants.EMA_QUESTION_CONFIG_FILENAME);
	if (!questionFile.exists())
		return;
*/	
    //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //DocumentBuilder builder = null;
	DocumentBuilderFactory docBuilderFactory = null;
	DocumentBuilder docBuilder = null;
	Document doc = null;
	
	    try {

            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            
			doc = docBuilder.parse(questionFile); // normalize text representation
            doc.getDocumentElement().normalize();
            System.out.println ("Root element of the doc is: " + doc.getDocumentElement().getNodeName()); 
	   
	    }
	    catch (SAXParseException err) {
	        System.out.println (" Parsing error" + ", line " + err.getLineNumber () + ", uri "+ err.getSystemId ());
	        System.out.println("" + err.getMessage ()); }
	        catch (SAXException e) {
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace (); }
	        
	        catch (Throwable t) {
	        t.printStackTrace ();
	        }
	        
    
	        NodeList listOfQuestions = doc.getElementsByTagName("question");
            
            
            int totalQuestions=0;
            if (listOfQuestions!=null)
            	totalQuestions =  listOfQuestions.getLength();
            
            
            questions = new Question[totalQuestions];
            
            
            for(int iterator=0; iterator<listOfQuestions.getLength() ; iterator++){
            	Node questionNode = listOfQuestions.item(iterator);
            	 if(questionNode!=null && questionNode.getNodeType() == Node.ELEMENT_NODE){
            		 Element questionElement = (Element)questionNode;
            		 //Element firstPersonElement = (Element)firstPersonNode; //——-
                     NodeList question_idList = questionElement.getElementsByTagName("question_id");
                     Element question_idElement = (Element)question_idList.item(0); 
                     NodeList textquestion_idList = question_idElement.getChildNodes();                                         
                     int qid = Integer.parseInt(((Node)textquestion_idList.item(0)).getNodeValue().trim());
                     
                     NodeList question_stringList = questionElement.getElementsByTagName("question_string");
                     Element question_stringElement = (Element)question_stringList.item(0); 
                     NodeList textquestion_stringList = question_stringElement.getChildNodes();                                         
                     String qstring = ((Node)textquestion_stringList.item(0)).getNodeValue().trim();
                     
                     // System.out.println("First Name : " + ((Node)textFNList.item(0)).getNodeValue().trim()); //——-
                     NodeList respID = questionElement.getElementsByTagName("responseID");
                     Element responseIDElement = (Element)respID.item(0); 
                     NodeList textr_idList = responseIDElement.getChildNodes();                                         
                     int rid = Integer.parseInt(((Node)textr_idList.item(0)).getNodeValue().trim());
                     
                     NodeList condQuestionID = questionElement.getElementsByTagName("conditionalOnQuestionID");
                     Element conditionalOnQuestionIDElement = (Element)condQuestionID.item(0); 
                     NodeList textcqList = conditionalOnQuestionIDElement.getChildNodes();                                         
                     int cqid = Integer.parseInt(((Node)textcqList.item(0)).getNodeValue().trim());
                     
                     NodeList condRespChoice = questionElement.getElementsByTagName("conditionalResponseChoice");
                     Element conditionalResponseChoiceElement = (Element)condRespChoice.item(0); 
                     NodeList textcRList = conditionalResponseChoiceElement.getChildNodes();                                         
                     int crc = Integer.parseInt(((Node)textcRList.item(0)).getNodeValue().trim());
                     
                     NodeList mChoice = questionElement.getElementsByTagName("multipleChoice");
                     Element multipleChoiceElement = (Element)mChoice.item(0); 
                     NodeList textmcqList = conditionalResponseChoiceElement.getChildNodes();                                         
                     boolean ismcq = Boolean.parseBoolean(((Node)textmcqList.item(0)).getNodeValue().trim());
                    // boolean ismcq = Boolean.parseBoolean(multipleChoiceElement.getNodeValue().toString());
                     
                     //rid = 11;
                     //cqid =1;
                     //crc =1;
                     //ismcq = false;
                     questions[iterator]= new Question(iterator, qstring, rid, cqid, crc, ismcq);
              		 /*here the arrays to keep the components for the questions will be filled*/
            	 }
            }
            
            		           
                    
            
  
            
            /*
            
            NodeList listOfResponses = doc.getElementsByTagName("response");
            int totalResponses =  listOfResponses.getLength();
            System.out.println("Total no of Responses : " + totalResponses); 
            //public static final Question questions[];
            
  
            /* following are the arrays to keep the components for the questions. similar arrays to be used for responses 
             * later data from these arrays would be used to make questions and responses array*/
           /* 
            int[] question_id = new int[listOfQuestions.getLength()];
            String[] question_text = new String[listOfQuestions.getLength()];
            int[] response_id = new int[listOfQuestions.getLength()];
            int[] conditional_on_question_id = new int[listOfQuestions.getLength()];
            int[] conditionalResponse_id = new int[listOfQuestions.getLength()];
            boolean[] isMCQ = new boolean[listOfQuestions.getLength()];
            
            for(int iterator=0; iterator<listOfQuestions.getLength() ; iterator++){
            	Node questionNode = listOfQuestions.item(iterator);
            	 if(questionNode.getNodeType() == Node.ELEMENT_NODE){
            		 Element questionElement = (Element)questionNode;
           
            		 /*here the arrays to keep the components for the questions will be filled*/
            //	 }
           // }
            
        	    
	    
     }

	
	
	
	public static final Response[] delayResponses = {
		new Response(0, new String[] {"Driving", "Meeting/Job", "Telephone", "Eating", "Chores", "Other"}, false),
		new Response(1, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false),
	};
	
	public static final Question[] delayQuestions = { 
		new Question(0, "Reason for requesting delay at previous prompt?", 0, -1, -1, false),
		new Question(1, "Just before you requested delay, how feeling? Nervous/stressed?", 1, -1, -1, false)
	};
	
	private InterviewContent() {
		loadResponses();
		loadQuestions();
	}
	
	
	private void loadstaticQuestions(){
		
		questions = new Question[43];
        questions[0] = new Question(0, "Just before the prompt, how were you feeling? (select Next)", -1, -1, -1, false);
        questions[1] = new Question(1, "Cheerful?", 0, -1, -1, false);
        questions[2] = new Question(2, "Happy?", 0, -1, -1, false);
        questions[3] = new Question(3, "Energetic?", 0, -1, -1, false);		
        questions[4] = new Question(4, "Frustrated/Angry?", 0, -1, -1, false);
        questions[5] = new Question(5, "Nervous/Stressed?", 0, -1, -1, false); 
        questions[6] = new Question(6, "Sad?", 0, -1, -1, false);
        questions[7] = new Question(7, "Sleepy?", 0, -1, -1, false); 		
        questions[8] = new Question(8, "Facing a problem?", 0, -1, -1, false);
        questions[9] = new Question(9, "Thinking about things that upset you?", 0, -1, -1, false);
        questions[10] = new Question(10, "Difficulties seem to be piling up?", 0, -1, -1, false);
        questions[11] = new Question(11, "Able to control important things?", 0, -1, -1, false);
        questions[12] = new Question(12, "Your location?", 1, -1, -1, false);
        questions[13] = new Question(13, "How would you describe your current activity?", 2, -1, -1, false);
        questions[14] = new Question(14, "What's going on? (select all that apply)", 3, 13, 1, true);
        questions[15] = new Question(15, "If other, please describe what�s going on below. Note that you do not have to type every detail here. Just type in a few words that will help you remember. We�ll ask for more details when you come back to the lab.", 4, 14, 32, false); 
        questions[16] = new Question(16, "What's going on? (select all that apply)", 5, 13, 2, true);	
        questions[17] = new Question(17, "If other, please describe what�s going on below. Note that you do not have to type every detail here. Just type in a few words that will help you remember. We�ll ask for more details when you come back to the lab.", 4, 16, 32, false); 
        questions[18] = new Question(18, "Describe physical movement?", 6, -1, -1, false);
        questions[19] = new Question(19, "Temperature Comfort?", 7, -1, -1, false);
        questions[20] = new Question(20, "Your Posture?", 8, -1, -1, false);
        questions[21] = new Question(21, "Have you consumed any of the following since the last prompt (select the most recent)?", 9, -1, -1, false);
        questions[22] = new Question(22, "If meal, what kind (select all that apply)?", 10, 21, 1, true);
        questions[23] = new Question(23, "If snack, what kind (select all that apply)?", 10, 21, 2, true);		
        questions[24] = new Question(24, "If drink, what kind? (select all that apply)", 11, 21, 4, true);
        questions[25] = new Question(25, "If drug, what kind? (select all that apply)", 12, 21, 8, true);		
        questions[26] = new Question(26, "In social interaction?", 13, -1, -1, false);
        questions[27] = new Question(27, "Talking?", 13, -1, -1, false);
        questions[28] = new Question(28, "If talking, on the phone?", 13, 27, 2, false);
        questions[29] = new Question(29, "If talking, with whom? (select all that apply)", 14, 27, 2, true);
        questions[30] = new Question(30, "If talking, with whom? (scroll down - select all that apply)", 15, 27, 2, true);
        questions[31] = new Question(31, "If not talking, how long ago was your last conversation?", 16, 27, 1, false);
        questions[32] = new Question(32, "Who was it with? (select all that apply)", 14, 27, 1, true);
        questions[33] = new Question(33, "Who was it with? (select all that apply)", 15, 27, 1, true);				
        questions[34] = new Question(34, "If you commuted since the last interview, what type?", 17, -1, -1, false);
        questions[35] = new Question(35, "How many cigarettes have you had since the last prompt?", 18, -1, -1, false);
        questions[36] = new Question(36, "What was your most recent cigarette like (select all that apply)?", 19, -1, -1, true);
        questions[37] = new Question(37, "In the last ten minutes, have you had a strong temptation/urge to smoke?", 13, -1, -1, false);
        questions[38] = new Question(38, "In the last ten minutes, have you seen any of the following people smoke? (select all that apply)", 20, -1, -1, true);
        questions[39] = new Question(39, "In the last ten minutes, have you smelled smoke?", 13, -1, -1, false);
        questions[40] = new Question(40, "In the last ten minutes, have you seen (select all that apply):", 21, -1, -1, true);
        questions[41] = new Question(41, "Is there any other information you would like us to know about what your are doing or what is happening around you?", 13, -1, -1, false);
        questions[42] = new Question(42, "If yes, please type this information below. Note that you do not have to type every detail here. Just type in a few words that will help you remember what you want to tell us. We�ll ask for more details when you come back to the lab.", 4, 41, 2, false); 
        	
     
        responses = new Response[22];
        //responses[0].setResponseFields(0, new String[] {""+totalQuestions, "No!", "no", "yes", "YES", "YES!"}, false);
        
    	
        responses[0] = new Response(0, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false);
        responses[1] = new Response(1, new String[] {"Home", "Work", "Store", "Restaurant", "Vehicle", "Outside", "Other"}, false);
        responses[2] = new Response(2, new String[] {"Leisure/Recreation", "Work/Task/Errand"}, false);
        responses[3] = new Response(3, new String[] {"Sports", "Video Game", "Surfing the Internet", "Watching Movies/TV/Video", "Listenting to Music", "Other"}, false);
        responses[4] = new Response(4, new String[] {}, true);
        responses[5] = new Response(5, new String[] {"Meeting", "E-mail", "Reading", "Phone Call", "Writing", "Other"}, false);
        responses[6] = new Response(6, new String[] {"Limited (writing)", "Light (walking)", "Moderate (jogging)", "Heavy (running)"}, false);
        responses[7] = new Response(7, new String[] {"Too Cold", "Comfortable", "Too Hot"}, false);
        responses[8] = new Response(8, new String[] {"On Your Feet", "Sitting", "Lying Down"}, false);		
        responses[9] = new Response(9, new String[] {"Meal", "Snack", "Drink", "Drug", "None"}, false);
        responses[10] = new Response(10, new String[] {"Mixed Dishes", "Salty/Crunchy Foods", "Sweet Foods", "Creamy Foods", "Beverages"}, false);
        responses[11] = new Response(11, new String[] {"Alcohol", "Juice", "Water", "Soda/Pop", "Smoothie", "Caffeine"}, false);
        responses[12] = new Response(12, new String[] {"Over the Counter", "Prescription", "Illicit Drug"}, false);
        responses[13] = new Response(13, new String[] {"No", "Yes"}, false);
        responses[14] = new Response(14, new String[] {"Men", "Women"}, false);
        responses[15] = new Response(15, new String[] {"Spouse/Significant Other", "Other Family Member", "Friends", "Co-workers", "Clients/Customers", "Boss", "Subordinates", "Stranger/Unknown"}, false);
        responses[16] = new Response(16, new String[] {"10 minutes or less", "Greater than 20 minutes", "Greater than 30 minutes",  "Greater than 45 minutes", "Greater than 1 hour"}, false);		
        responses[17] = new Response(17, new String[] {"Driving", "Biking", "Walking", "Riding as a Passenger", "Riding Public Transportation", "Did not Commute"}, false);
        responses[18] = new Response(18, new String[] {"0", "1", "2", "3", "4", "5 or more"}, false);				
        responses[19] = new Response(19, new String[] {"Good Tasting", "A Rush/Buzz", "Relaxing", "Reduced the Urge", "Pleasant", "Bad Tasting", "Made Me Feel Dizzy/Nauseated", "Unpleasant"}, false);		
        responses[20] = new Response(20, new String[] {"No One", "Spouse Partner", "Other Family Member", "Other Person You Know", "Stranger"}, false);
        responses[21] = new Response(21, new String[] {"Cigarette Buds", "Ashtrays", "Cigarette Pots", "Cigarette Packs", "None of the Above"}, false);				
    	
	}
	
	public int getNumberQuestions(boolean realQuestionsOnly) {
		int numQuestions = questions.length;
		if (realQuestionsOnly){
			for (int i=0; i < questions.length; i++) {
				if (!hasResponses(i)) {
					numQuestions--;
				}
			}
				
				
		}
		
		return numQuestions;
	}
	
	public String getQuestion(int index) {
		if (index < 0 || index > questions.length - 1)
			return "";
		return questions[index].getText();
	}
	
	public String[] getResponses(int index) {
		String[] value = null;
		if (index >= 0 && index < questions.length) {
			int responseID = questions[index].getResponseID();
			if (responseID >= 0 && responseID < responses.length) {
				value = responses[responseID].choices;
			}
		}
		return value;
	}
	
//	public int skipCount(int index, int response) {
//		int value = 0;
//		if (index < 0 || index > skip.length - 1)
//			return value;
//		if (skip[index] == response) {
//			value = skipCount[index];
//		}
//		return value;
//	}

	public String getDelayQuestion(int index) {
		if (index < 0 || index > delayQuestions.length - 1)
			return "";
		return delayQuestions[index].getText();

	}

	public String[] getDelayResponses(int index) {
		String[] value = null;
		if (index >= 0 && index < delayQuestions.length) {
			int responseID = delayQuestions[index].getResponseID();
			if (responseID >= 0 && responseID < delayResponses.length) {
				value = delayResponses[responseID].choices;
			}
		}
		return value;
	}

	public boolean isQuestionActive(int index, InterviewData data) {
		if (index < 0 || index >= questions.length)
			return false;
		
		Question question = questions[index];		
		
		int conditionalID = question.getConditionalQuestionID();
		if (conditionalID != -1) {
			Integer response = (Integer) data.getResponse(conditionalID);	
			if (response == null)
				return false;
			
			if (response != question.getConditionalResponseChoice()) 
				return false;			
		}
		
		return true;
	}

	public int getNumberDelayQuestions() {
		return delayQuestions.length;
	}
	
	public boolean isQuestionMultipleChoice(int index) {
		return questions[index].isMultipleChoice();
	}

	public boolean isQuestionFreeResponse(int index) {
		if (hasResponses(index))
			return responses[questions[index].qresponseID].freeResponse;
		
		return false;
	}
	
	public boolean hasResponses(int index) {
		return questions[index].qresponseID != -1;
	}
	
	public boolean hasDelayResponses(int index) {
		return delayQuestions[index].qresponseID != -1;
	}
	
	
	public static InterviewContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InterviewContent();
		}
		return INSTANCE;
	}
}
