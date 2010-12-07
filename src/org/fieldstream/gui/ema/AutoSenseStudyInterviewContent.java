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

import android.widget.ListView;

/**
 * @author Andrew Raij
 *
 */
public class AutoSenseStudyInterviewContent implements IContent {
	static private AutoSenseStudyInterviewContent INSTANCE = null;
	
	
	private static class Question {
		private int id;
		private String text;

		// A -1 response means the question isn't really a question, or is 
		// a header/title for the next question
		private int responseID;
		
		// If not -1, this question is conditional on the response to the 
		// specified question.  This question should appear if the index of the 
		// response choice of the specified question corresponds to this conditional
		private int conditionalOnQuestionID;
		private int conditionalResponseChoice;
		private boolean multipleChoice;
		
		public Question(int id, String text, int responseID, int conditionalOnQuestionID, int conditionalResponseChoice, boolean multipleChoice) {
			this.id = id;
			this.text = text;
			this.responseID = responseID;
			this.conditionalOnQuestionID = conditionalOnQuestionID;			
			this.conditionalResponseChoice = conditionalResponseChoice;
			this.multipleChoice = multipleChoice;
		}
				
		public int getID() { return id; }
		public String getText() { return text; }
		public int getResponseID() { return responseID; }
		public int getConditionalQuestionID() { return conditionalOnQuestionID; }
		public int getConditionalResponseChoice() { return conditionalResponseChoice; }
		public boolean isMultipleChoice() {return multipleChoice;}
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
	}

    // array contains all possible response lists used by interview
	public static final Response[] responses = {
		new Response(0, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false),
		new Response(1, new String[] {"Home", "Work", "Store", "Restaurant", "Vehicle", "Outside", "Other"}, false),
		new Response(2, new String[] {"Leisure/Recreation", "Work/Task/Errand"}, false),		
		new Response(3, new String[] {"Sports", "Video Game", "Surfing the Internet", "Watching Movies/TV/Video", "Listenting to Music", "Other"}, false),
		new Response(4, new String[] {}, true),
		new Response(5, new String[] {"Meeting", "E-mail", "Reading", "Phone Call", "Writing", "Other"}, false),		
		new Response(6, new String[] {"Limited (writing)", "Light (walking)", "Moderate (jogging)", "Heavy (running)"}, false),
		new Response(7, new String[] {"Too Cold", "Comfortable", "Too Hot"}, false),
		new Response(8, new String[] {"On Your Feet", "Sitting", "Lying Down"}, false),		
		new Response(9, new String[] {"Meal", "Snack", "Drink", "Drug", "None"}, false),
		new Response(10, new String[] {"Mixed Dishes", "Salty/Crunchy Foods", "Sweet Foods", "Creamy Foods", "Beverages"}, false),
		new Response(11, new String[] {"Alcohol", "Juice", "Water", "Soda/Pop", "Smoothie", "Caffeine"}, false),		
		new Response(12, new String[] {"Over the Counter", "Prescription", "Illicit Drug"}, false),
		new Response(13, new String[] {"No", "Yes"}, false),
		new Response(14, new String[] {"Men", "Women"}, false),
		new Response(15, new String[] {"Spouse/Significant Other", "Other Family Member", "Friends", "Co-workers", "Clients/Customers", "Boss", "Subordinates", "Stranger/Unknown"}, false),
		new Response(16, new String[] {"10 minutes or less", "Greater than 20 minutes", "Greater than 30 minutes",  "Greater than 45 minutes", "Greater than 1 hour"}, false),						
		new Response(17, new String[] {"Driving", "Biking", "Walking", "Riding as a Passenger", "Riding Public Transportation", "Did not Commute"}, false),		
		new Response(18, new String[] {"0", "1", "2", "3", "4", "5 or more"}, false),				
		new Response(19, new String[] {"Good Tasting", "A Rush/Buzz", "Relaxing", "Reduced the Urge", "Pleasant", "Bad Tasting", "Made Me Feel Dizzy/Nauseated", "Unpleasant"}, false),		
		new Response(20, new String[] {"No One", "Spouse Partner", "Other Family Member", "Other Person You Know", "Stranger"}, false),				
		new Response(21, new String[] {"Cigarette Buds", "Ashtrays", "Cigarette Pots", "Cigarette Packs", "None of the Above"}, false),				
	};
	
	public static final Question[] questions = {
		new Question(0, "Just before the prompt, how were you feeling? (select Next)", -1, -1, -1, false),
		new Question(1, "Cheerful?", 0, -1, -1, false),
		new Question(2, "Happy?", 0, -1, -1, false),
		new Question(3, "Energetic?", 0, -1, -1, false),		
		new Question(4, "Frustrated/Angry?", 0, -1, -1, false),
		new Question(5, "Nervous/Stressed?", 0, -1, -1, false), 
		new Question(6, "Sad?", 0, -1, -1, false), 
		new Question(7, "Sleepy?", 0, -1, -1, false), 		
		new Question(8, "Facing a problem?", 0, -1, -1, false),
		new Question(9, "Thinking about things that upset you?", 0, -1, -1, false),
		new Question(10, "Difficulties seem to be piling up?", 0, -1, -1, false),
		new Question(11, "Able to control important things?", 0, -1, -1, false),
		new Question(12, "Your location?", 1, -1, -1, false),
		new Question(13, "How would you describe your current activity?", 2, -1, -1, false),
		new Question(14, "What's going on? (select all that apply)", 3, 13, 1, true),	
		new Question(15, "If other, please describe what�s going on below. Note that you do not have to type every detail here. Just type in a few words that will help you remember. We�ll ask for more details when you come back to the lab.", 4, 14, 32, false), 
		new Question(16, "What's going on? (select all that apply)", 5, 13, 2, true),	
		new Question(17, "If other, please describe what�s going on below. Note that you do not have to type every detail here. Just type in a few words that will help you remember. We�ll ask for more details when you come back to the lab.", 4, 16, 32, false), 
		new Question(18, "Describe physical movement?", 6, -1, -1, false),
		new Question(19, "Temperature Comfort?", 7, -1, -1, false),
		new Question(20, "Your Posture?", 8, -1, -1, false),
		new Question(21, "Have you consumed any of the following since the last prompt (select the most recent)?", 9, -1, -1, false),
		new Question(22, "If meal, what kind (select all that apply)?", 10, 21, 1, true),
		new Question(23, "If snack, what kind (select all that apply)?", 10, 21, 2, true),		
		new Question(24, "If drink, what kind? (select all that apply)", 11, 21, 4, true),
		new Question(25, "If drug, what kind? (select all that apply)", 12, 21, 8, true),		
		new Question(26, "In social interaction?", 13, -1, -1, false), 
		new Question(27, "Talking?", 13, -1, -1, false), 
		new Question(28, "If talking, on the phone?", 13, 27, 2, false),
		new Question(29, "If talking, with whom? (select all that apply)", 14, 27, 2, true),
		new Question(30, "If talking, with whom? (scroll down - select all that apply)", 15, 27, 2, true),
		new Question(31, "If not talking, how long ago was your last conversation?", 16, 27, 1, false),
		new Question(32, "Who was it with? (select all that apply)", 14, 27, 1, true),		
		new Question(33, "Who was it with? (select all that apply)", 15, 27, 1, true),				
		new Question(34, "If you commuted since the last interview, what type?", 17, -1, -1, false),
		new Question(35, "How many cigarettes have you had since the last prompt?", 18, -1, -1, false),
		new Question(36, "What was your most recent cigarette like (select all that apply)?", 19, -1, -1, true),
		new Question(37, "In the last ten minutes, have you had a strong temptation/urge to smoke?", 13, -1, -1, false),
		new Question(38, "In the last ten minutes, have you seen any of the following people smoke? (select all that apply)", 20, -1, -1, true),
		new Question(39, "In the last ten minutes, have you smelled smoke?", 13, -1, -1, false),
		new Question(40, "In the last ten minutes, have you seen (select all that apply):", 21, -1, -1, true),
		new Question(41, "Is there any other information you would like us to know about what your are doing or what is happening around you?", 13, -1, -1, false),
		new Question(42, "If yes, please type this information below. Note that you do not have to type every detail here. Just type in a few words that will help you remember what you want to tell us. We�ll ask for more details when you come back to the lab.", 4, 41, 2, false), 
	};

	public static final Response[] delayResponses = {
		new Response(0, new String[] {"Driving", "Meeting/Job", "Telephone", "Eating", "Chores", "Other"}, false),
		new Response(1, new String[] {"NO!", "NO", "no", "yes", "YES", "YES!"}, false),
	};
	
	public static final Question[] delayQuestions = { 
		new Question(0, "Reason for requesting delay at previous prompt?", 0, -1, -1, false),
		new Question(1, "Just before you requested delay, how feeling? Nervous/stressed?", 1, -1, -1, false)
	};
	
	private AutoSenseStudyInterviewContent() {
		
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
			return responses[questions[index].responseID].freeResponse;
		
		return false;
	}
	
	public boolean hasResponses(int index) {
		return questions[index].responseID != -1;
	}
	
	public boolean hasDelayResponses(int index) {
		return delayQuestions[index].responseID != -1;
	}
	
	
	public static AutoSenseStudyInterviewContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AutoSenseStudyInterviewContent();
		}
		return INSTANCE;
	}
}
