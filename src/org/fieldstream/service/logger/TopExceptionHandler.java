package org.fieldstream.service.logger;

// Class for logging exceptions.
// Borrowed from http://jyro.blogspot.com/2009/09/crash-report-for-android-app.html

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import java.io.*;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultUEH;

	private Activity app = null;

	public TopExceptionHandler(Activity app) {
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		this.app = app;
	}

	public void uncaughtException(Thread t, Throwable e)
	{
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString()+"\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i=0; i<arr.length; i++)
		{
			report += "    "+arr[i].toString()+"\n";
		}
		report += "-------------------------------\n\n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "--------- Cause ---------\n\n";
		Throwable cause = e.getCause();
		if(cause != null) {
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i=0; i<arr.length; i++)
			{
				report += "    "+arr[i].toString()+"\n";
			}
		}
		try {
			FileOutputStream trace = app.openFileOutput(
					"stack.trace", Context.MODE_APPEND);
			trace.write(report.getBytes());
			trace.close();
		} catch(IOException ioe) {
			// ...
		}

		defaultUEH.uncaughtException(t, e);
	}
}