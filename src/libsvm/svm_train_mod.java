package libsvm;
import java.io.*;
import java.util.*;

import android.os.Environment;

public class svm_train_mod {
	private svm_parameter param;		// set by parse_command_line
	private svm_problem prob;		// set by read_problem
	private static svm_model model;
	private String error_msg;
	private int cross_validation;
	private int nr_fold;
	public static ArrayList<String> SVlist = new ArrayList<String>();
	//boolean WRITE = false;
	private void do_cross_validation()
	{
		int i;
		int total_correct = 0;
		double total_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double[] target = new double[prob.l];

		svm.svm_cross_validation(prob,param,nr_fold,target);
		if(param.svm_type == svm_parameter.EPSILON_SVR ||
		   param.svm_type == svm_parameter.NU_SVR)
		{
			for(i=0;i<prob.l;i++)
			{
				double y = prob.y[i];
				double v = target[i];
				total_error += (v-y)*(v-y);
				sumv += v;
				sumy += y;
				sumvv += v*v;
				sumyy += y*y;
				sumvy += v*y;
			}
			System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
			System.out.print("Cross Validation Squared correlation coefficient = "+
				((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
				((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
				);
		}
		else
		{
			for(i=0;i<prob.l;i++)
				if(target[i] == prob.y[i])
					++total_correct;
			System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
		}
	}
	
	private void run(boolean retrain, String[] newFeat) throws IOException
	{
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 2;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		cross_validation = 0;

		read_problem(retrain, newFeat);
		error_msg = svm.svm_check_parameter(prob,param);

		if(error_msg != null)
		{
			System.err.print("Error: "+error_msg+"\n");
			System.exit(1);
		}

		if(cross_validation != 0)
		{
			do_cross_validation();
		}
		else
		{
			model = svm.svm_train(prob,param);
		}
	}
	public ArrayList<String> getSV(){
		return SVlist;
	}
	public static svm_model getModel(){
		return model;
	}
	
	public static void go(boolean retrain, String[] newFeat) throws IOException
	{
		svm_train_mod t = new svm_train_mod();
		t.run(retrain, newFeat);
	}

	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}
	public static void setModel(svm_model md){
		model = md;
	}
	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	
	// read in a problem (in svmlight format)

	private void read_problem(boolean retrain, String[] newFeat) throws IOException
	{
		BufferedReader fp;
		//initial read (no longer used)
		if(retrain==false){
			fp = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/model/model.txt"));
		}
		//if retrain is requested
		else{
			readModelMemory(newFeat);
			return;
		}

		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0)
			param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}

		fp.close();
	}

	/**read previous model, and add new vectors
	 * then model will be retrained
	 * @param newFeat
	 */
	public void readModelMemory(String[] newFeat){
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;
		for(int i =0; i<newFeat.length; i++)
		{
			String line = newFeat[i];
			
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}
		prob = new svm_problem();
		prob.l = model.l + newFeat.length;
		prob.x = new svm_node[prob.l][];
		prob.y = new double[prob.l];
		svm_node[][] SV = model.SV;
		int classID = 0;
		int vectorCT = 0;
		//adding new vectors
		for(int i = 0; i< model.l; i++){			 
			prob.x[i] = SV[i];
			if(vectorCT == model.nSV[classID]){
				classID++;
				
				vectorCT = 0;
			}
			prob.y[i] = model.label[classID];
			/*if(WRITE==true){
				String line ="";
				for(svm_node n: SV[i])
					line += " "+n.index+":"+n.value;
				SVlist.add(model.label[classID]+line+"\n");
			}*/
			vectorCT++;
		}
		for(int i=0; i<newFeat.length; i++)
			prob.x[i+model.l] = vx.elementAt(i);

		for(int i =0; i<newFeat.length; i++)
			prob.y[i+model.l] = vy.elementAt(i); 
		if(param.gamma == 0 && max_index > 0)
			param.gamma = 1.0/max_index;
		
		//NOT NECESSARY (since precomputed model is not used)
		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}
	}
	
	/** Modularized write model **/
	public void model_write(){
		svm_node[][] SV = model.SV;
		int l = model.l;
		int k = 0;
		int classN = 0;
		int labelS = model.label[classN];
		for(int i=0;i<l;i++)
		{
			if(k==model.nSV[classN]){
				classN++;
				labelS = model.label[classN];
				k=0;
			}
			String inputF = "";
			svm_node[] p = SV[i];
			inputF += labelS+" ";
				for(int j=0;j<p.length;j++)
					inputF += p[j].index+":"+p[j].value+" ";
			inputF+="\n";
			SVlist.add(inputF);
			k++;
		}
	}
}
