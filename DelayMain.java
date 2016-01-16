package FlightDelay;

import javax.swing.ImageIcon;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.apache.mahout.classifier.evaluation.Auc;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.CrossFoldLearner;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class DelayMain {

	public static final int NUM_CATEGORIES = 2;
	
	//@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String Year = JOptionPane.showInputDialog("Enter Year of flight");
		String Month = JOptionPane.showInputDialog("Enter Month of flight");
		String DayofMonth = JOptionPane.showInputDialog("Enter Date of flight");
		String DayOfWeek = JOptionPane.showInputDialog("Enter which day of week are you flying");
		String FlightNum = JOptionPane.showInputDialog("Enter your Flight Number");
		String CRSDepTime = JOptionPane.showInputDialog("Enter departure time");
		String CRSArrTime = JOptionPane.showInputDialog("Enter arrival time");
		String Origin = JOptionPane.showInputDialog("Enter origin airport");
		String Dest = JOptionPane.showInputDialog("Enter destination airport");

		//int num1 = Integer.parseInt(fn);
		//int num2 = Integer.parseInt(sn);
		//int sum = num1 + num2;
		//null, “The answer is” +sum, “the title”, JOptionPane.PLAIN_MESSAGE
		//JOptionPane.showMessageDialog(null, "The answer is" +sum, "the title", JOptionPane.PLAIN_MESSAGE);
			//String str = String.valueOf(sum);

		try {   
		File newTextFile = new File("/home/cloudera/workspace/Logistic_Regression/src/main/resources/input/User_Input_1.csv");
		FileWriter fw = new FileWriter(newTextFile, true);
		fw.append("Month");
		fw.append(',');
		fw.append("DayofMonth");
		fw.append(',');
		fw.append("DayOfWeek");
		fw.append(',');
		fw.append("FlightNum");
		fw.append(',');
		fw.append("CRSDepTime");
		fw.append(',');
		fw.append("CRSArrTime");
		fw.append(',');
		fw.append("Origin");
		fw.append(',');
		fw.append("Dest");
        	fw.append("\n");
        	fw.append(Month);
		fw.append(',');
		fw.append(DayofMonth);
		fw.append(',');
		fw.append(DayOfWeek);
		fw.append(',');
		fw.append(FlightNum);
		fw.append(',');
		fw.append(CRSDepTime);
		fw.append(',');
		fw.append(CRSArrTime);
		fw.append(',');
		fw.append(Origin);
		fw.append(',');
		fw.append(Dest);
		fw.close();

	} catch (IOException iox) {
		//do stuff with exception
		iox.printStackTrace();
	}
		
	String filePath = "input/Book1.csv";
	//String filePath = "/usr/lib/hadoop-hdfs";
	
	File f = new File(filePath);
		
	if(f.exists())
	{
		System.out.println("file found");
		System.out.println(Resources.getResource(filePath).toString());
	}
		
	Iterator<DelayData> iFlight = new DelayDataParser(filePath).iterator() ;
	List<DelayData> Flight = Lists.newArrayList();

	double heldOutPercentage = 0.30;
	    
	while(iFlight.hasNext())
	{
	    Flight.add(iFlight.next());
	}
	    
	int cutoff = (int) (heldOutPercentage * Flight.size());
    	List<DelayData> test = Flight.subList(0, cutoff);
    	List<DelayData> train = Flight.subList(cutoff, Flight.size());
    	
    	int[] correct = new int[test.size() + 1];
    	double accuracy=0.0;
    	int y=0;
    	
    	OnlineLogisticRegression lr = new OnlineLogisticRegression(NUM_CATEGORIES, DelayData.FEATURES, new L1())
        .learningRate(1)
        .alpha(1)
        .lambda(0.000001)
        .stepOffset(10000)
        .decayExponent(0.2);
	    
	for (int run = 0; run < 20; run++) {
	   	Collections.shuffle(Flight);	

	   	for (int pass = 0; pass < 20; pass++) {
	   		for (DelayData observation : train) {
	   			lr.train(observation.getTarget(), observation.asVector());
	   		}
	   		if (pass % 5 == 0) {
	   			Auc eval = new Auc(0.5);
	   			for (DelayData testCall : test) {
	   				eval.add(testCall.getTarget(), lr.classifyScalar(testCall.asVector()));
	   				//eval.add(testCall.getTarget(), lr.classifyFull(testCall.asVector()));
	   			}
	   			//System.out.printf("%d, %.4f, %.4f\n", pass, lr.currentLearningRate(), eval.auc());
	   			//correct[y]++;
	   		}
	   	}
	}
	lr.close();
	    
	File algo = new File("/home/cloudera/workspace/Logistic_Regression/");
    	DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(algo, "algo.model")));
    	lr.write(outputStream);
    	outputStream.close();
	    
	//InputStream in=new FileInputStream("/home/cloudera/workspace/Logistic_Regression/algo.model");
	//CrossFoldLearner best=ModelSerializer.readBinary(in,CrossFoldLearner.class);
	//System.out.println("auc="+best.auc()+"\n" +"percentCorrect="+best.percentCorrect()+"\n"+"LogLikelihood="+best.getLogLikelihood());
	    
	//ModelSerializer.writeBinary("/home/cloudera/workspace/LR/alr.model", lr.getBest().getPayload().getLearner());//.getModels().get(0));//
	//InputStream in=new FileInputStream("/home/cloudera/workspace/LR/alr.model");
	//CrossFoldLearner best=ModelSerializer.readBinary(in,CrossFoldLearner.class);
	//System.out.println("auc="+best.auc()+"\n" +"percentCorrect="+best.percentCorrect()+"\n"+"LogLikelihood="+best.getLogLikelihood());
	    	
	int[] count = new int[3];        
	for (DelayData testCall : test) {    
		int s = lr.classifyFull(testCall.asVector()).maxValueIndex();
		//count[s]++;
		y += s == testCall.getTarget() ? 1 : 0;
	}
	correct[y]++;
	accuracy=(double)(y)/(double)(test.size());
	accuracy*=100.0;
	System.out.printf("Testing accuracy is %f\n\n", accuracy);
		
////////////////////////////////////////////////////////////////////////////////////////////		
		
	String NewFilePath = "input/User_Input_1.csv";
		
	File nf = new File(NewFilePath);
		
	if(nf.exists())
	{
		System.out.println("user file found");
		System.out.println(Resources.getResource(NewFilePath).toString());
	}
				
	Iterator<DelayData> iNewFlight = new DelayDataParser(NewFilePath).iterator() ;
	List<DelayData> NewFlight = Lists.newArrayList();
		
    	while(iNewFlight.hasNext())
    	{
    		NewFlight.add(iNewFlight.next());
    	}

    	Vector result = null;
    	List<DelayData> k = NewFlight.subList(0, 1);
    	//List<DelayData> k = NewFlight;
    	for (DelayData NewObservation : k) {
		result = lr.classifyFull(NewObservation.asVector());
		//System.out.println(result);
    	}

	System.out.println("------------- Predicting -------------");
	System.out.format("Probability of not Delay (0) = %.3f\n",result.get(0));
				//A.get(0));
	System.out.format("Probability of Delay (1)     = %.3f\n",result.get(1));
				//A.get(1));
	}
}
