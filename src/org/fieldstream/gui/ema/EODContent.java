//Copyright (c) 2010, Carnegie Mellon University
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
//    * Neither the name of Carnegie Mellon University nor the names of its contributors may be used to 
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

// @author Brian French


package org.fieldstream.gui.ema;

public class EODContent implements IContent {

	// array contains the text for all interview questions
	static  final String[] questions = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
    // array contains the mapping from question index to response list index
	static  final int[] mapping = {0 ,-1, 0, 0, 0, 1, 0, 0, 0, 2};
    // array contains all possible response lists used by interview
	//static  final String[][] responses = { {"YES", "yes", "no", "NO"}, {"Home", "Work", "Shopping", "Other", "Entertainment", "Restaurant", "Quiet Place"}, {"Comfortable", "Too hot", "Too Cold"} };
	
	static final String delayQuestion = "Reason for delay?";
	static final String[] delayResponses = {"Playing", "Watching TV/Movie/Videos", "Lecture", "Meeting", "Telephone call", "Minor Emergency", "Working/Studying", "Other"};

	
	public EODContent() {
		
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
//		if (index >= 0 && index < questions.length) {
//			int responseMap = mapping[index];
//			if (responseMap >= 0 && responseMap < responses.length) {
//				value = responses[responseMap];
//			}
//		}
		return value;
	}
	
//	public int skipCount(int index, int response) {
//		return 0;
//	}

	public String getDelayQuestion(int index) {
		return delayQuestion;
	}

	public String[] getDelayResponses(int index) {
		return delayResponses;
	}

	public int getNumberDelayQuestions() {
		return 1;
	}

	public boolean isQuestionActive(int index, InterviewData data) {		
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
