//Copyright (c) 2010, University of Memphis, Carnegie Mellon University
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
//    * Neither the names of the University of Memphis and Carnegie Mellon University nor the names of its 
//      contributors may be used to endorse or promote products derived from this software without specific 
//      prior written permission.
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

//@author Brian French
//@author Andrew Raij


package org.fieldstream.gui.ema;

//TODO load this from an xml file, removing the need for multiple implementations
public class InterviewContent implements IContent {

	private static InterviewContent INSTANCE = null;
	
	// array contains the text for all interview questions
	public static  final String[] questions = { "At the time of the prompt, how are you feeling?", "Frustrated/Angry?", "Nervous/Stressed?", "Sad?", "During the past 10 minutes, were you:", "Facing a problem?", "Thinking about things that upset you?", "Difficulties seem to be piling up?", "Able to control important things?", "Your location?", "Your current physical activity/posture?", "Temperature comfort?", "Talking?", "Currently in a social interaction?", "Since last interview.", "Any food or drink?", "If yes, type of consumption." };
    // array contains the mapping from question index to response list index
	public static  final int[] mapping = {-1 ,0, 0, 0, -1, 0, 0, 0, 0, 1, 2, 3, 4, 4, -1, 4, 5};
	// array contains the skip responses
	public static final int[] skip = {-1 ,-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1};
	// array contains the skip counts
	public static final int[] skipCount = {-1 ,-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1};
    // array contains all possible response lists used by interview
	public static  final String[][] responses = { {"YES", "yes", "no", "NO"}, {"Home", "Work/School", "Shopping", "Other", "Entertainment", "Restaurant", "Quiet Place"}, {"Lying down","Sitting / working / eating / meeting","Standing / talking", "Walking", "Light sport (e.g., jogging)", "Intense sport"}, {"Comfortable", "Too hot", "Too Cold"}, {"Yes", "No"}, {"Meal", "Snack", "Alcohol", "Caffeine", "Drug"} };
	
	public static final String[] delayQuestions = {"Reason for delay?"};
	public static final String[][] delayResponses = {{"Playing", "Watching TV/Movie/Videos", "Lecture", "Meeting", "Telephone call", "Minor Emergency", "Working/Studying", "Other"}};


	public static InterviewContent getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InterviewContent();
		}
		return INSTANCE;
	}
	
	private InterviewContent() {
		
	}
	
	public int getNumberQuestions(boolean realQuestionsOnly) {
		int numQuestions = questions.length;
		if (realQuestionsOnly) {
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
		return questions[index];
	}
	
	public String[] getResponses(int index) {
		String[] value = null;
		if (index >= 0 && index < questions.length) {
			int responseMap = mapping[index];
			if (responseMap >= 0 && responseMap < responses.length) {
				value = responses[responseMap];
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
		return delayQuestions[index];
	}

	public String[] getDelayResponses(int index) {
		return delayResponses[index];
	}
	
	public int getNumberDelayQuestions() {
		return delayQuestions.length;
	}

	public boolean isQuestionActive(int index, InterviewData data) {
		int i=0;
		while (i < index) {
			if (i + skipCount[i] >= index) {
				int response = (Integer)data.getResponse(i);
				
				if (i >=0 && i < mapping.length) {
					int map = mapping[i];
					if (map >=0 && map < responses.length) {
						if (skip[i] == response)
							return false;
					}
				}
			}
			i++;
		}
		
		return true;
	}
	
	public boolean isQuestionMultipleChoice(int index) {
		return false;
	}

	public boolean isQuestionFreeResponse(int index) {
		return false;
	}

	public boolean hasResponses(int index) {
		return mapping[index] != -1;
	}
	
	public boolean hasDelayResponses(int index) {
		return true;
	}
}

