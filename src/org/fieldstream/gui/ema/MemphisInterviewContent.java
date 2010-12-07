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

//@author Andrew Raij


package org.fieldstream.gui.ema;

import android.widget.ListView;

/**
 * @author Andrew Raij
 *
 */
public class MemphisInterviewContent implements IContent {
	static private MemphisInterviewContent INSTANCE = null;
	
	
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
		new Response(0, new String[] {"Strongly Agree", "Agree", "Agree Somewhat", "Neither Agree Nor Disagree", "Disagree Somewhat", "Disagree", "Strongly Disagree"}, false),
		new Response(1, new String[] {"On your feet", "Sitting", "Lying down"}, false),
		new Response(2, new String[] {"Yes", "No"}, false),
		new Response(3, new String[] {"Men", "Women"}, false), 
		new Response(4, new String[] {"Significant Other", "Own Children", "Other Relatives", "Friends", "Colleagues", "Clients/Customers", "Boss", "Subordinates"}, false),
		new Response(5, new String[] {"Meal", "Snack", "Alcohol", "Caffeine", "Drug"}, false),
		new Response(6, new String[] {"Home", "Work", "Vehicle", "Outside", "Other"}, false),
		new Response(7, new String[] {}, true),
		new Response(8, new String[] {"Leisure/Recreation", "Work/task/errand"}, false),
		new Response(9, new String[] {"Limited (write)", "Light (walk)", "Moderate (jog)", "Heavy (run)"}, false),
		new Response(10, new String[] {"Comfortable", "Too Hot", "Too Cold"}, false)
	};
	
	public static final Question[] questions = {
		new Question(0, "Just before the prompt, how were you feeling? (select Next)", -1, -1, -1, false),
		new Question(1, "Cheerful?", 0, -1, -1, false),
		new Question(2, "Happy?", 0, -1, -1, false),
		new Question(3, "Frustrated/Angry?", 0, -1, -1, false),
		new Question(4, "Nervous/Stressed?", 0, -1, -1, false), 
		new Question(5, "Sad?", 0, -1, -1, false), 
		new Question(6, "Your posture?", 1, -1, -1, false),
		new Question(7, "In social interaction?", 2, -1, -1, false), 
		new Question(8, "Talking?", 2, -1, -1, false), 
		new Question(9, "If talking, on the phone?", 2, 8, 1, false),
		new Question(10, "If talking, with whom? (select all that apply)", 3, 8, 1, true),
		new Question(11, "If talking, with whom? (scroll down - select all that apply)", 4, 8, 1, true),
		new Question(12, "During the past 10 minutes (select Next): ", -1, -1, -1, false),
		new Question(13, "Smoking?", 2, -1, -1, false),
		new Question(14, "Any food or drink?", 2, -1, -1, false),
		new Question(15, "If yes, type of consumption?", 5, 14, 1, true),
		new Question(16, "Facing a problem?", 2, -1, -1, false),
		new Question(17, "Thinking about things that upset you?", 0, -1, -1, false),
		new Question(18, "Difficulties seem to be piling up?", 0, -1, -1, false),
		new Question(19, "Able to control important things?", 0, -1, -1, false),
		new Question(20, "Your location?", 6, -1, -1, false),
		new Question(21, "If other, please describe.", 7, 20, 16, false), 
		new Question(22, "Your activity?", 8, -1, -1, false),
		new Question(23, "Describe physical movement", 9, -1, -1, false), 
		new Question(24, "Temperature comfort?", 10, -1, -1, false),
		new Question(25, "Disruption by this interview made you (select Next):", -1, -1, -1, false),
		new Question(26, "Frustrated/Angry?", 0, -1, -1, false),
		new Question(27, "Nervous/Stressed?", 0, -1, -1, false),
		new Question(28, "Annoyed?", 0, -1, -1, false)
	};

	public static final Response[] delayResponses = {
		new Response(0, new String[] {"Driving", "Meeting/Job", "Telephone", "Eating", "Chores", "Other"}, false),
		new Response(1, new String[] {"Strongly Agree", "Agree", "Agree Somewhat", "Neither Agree Nor Disagree", "Disagree Somewhat", "Disagree", "Strongly Disagree"}, false),
	};
	
	public static final Question[] delayQuestions = { 
		new Question(0, "Reason for requesting delay?", 0, -1, -1, false),
		new Question(1, "Just before you requested delay, how feeling? Nervous/stressed?", 1, -1, -1, false)
	};
	
	private MemphisInterviewContent() {
		
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
			int response = (Integer) data.getResponse(conditionalID);			
			if (response != question.getConditionalResponseChoice()) {
				return false;
			}
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
	
	
	public static MemphisInterviewContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MemphisInterviewContent();
		}
		return INSTANCE;
	}
}
