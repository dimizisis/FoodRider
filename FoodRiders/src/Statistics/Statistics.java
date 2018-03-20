package Statistics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;

public class Statistics {
	
	private int n;
	
	private double mean;
	
	private ImageIcon barplot;
	
	private ArrayList<Integer> freq; // Temp, �� ������� �� ������ ���� �����
	
	private ArrayList<Integer> interv; // Temp, �� ������� �� ������ ���� �����
	
	private int[] frequency;
	
	private int[] interval;
	
	public Statistics() throws IOException {
		
		this.interv = new ArrayList<>(Arrays.asList(24,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23));
		
		this.freq = new ArrayList<>(Arrays.asList(4,5,10,30,10,20,23,25,27,21,3,45,78,23,12,45,23,12,34,23,1,2,34));
			
		// ��������� ��� ArrayList �� ������, ��� �� �������� �� ��������� �� �������� ���� R
		this.frequency = convertListToArray(freq);
		
		// ��������� ��� ArrayList �� ������, ��� �� �������� �� ��������� �� �������� ���� R
		this.interval = convertListToArray(interv);
		
		// ����������� ��� n (�������� ���� ��� ����������)
		this.n = calcN();
		
		// ����������� ��� ����� (����� ���� ����������� ��� ����)
		this.mean = calcMean();
		
		// ���������� ��� barplot
		this.barplot = drawPlot();
		
	}
	
	
	/* Public Methods */
	
	public int getN() {
		return n;
	}
	
	public double getMean() {
		return mean;
	}

	public ImageIcon getBarplot() {
		return barplot;
	}
	
	/* Private Methods */
	
private int[] convertListToArray(ArrayList<Integer> list) {
		
		/* � ������� ���� ���������� ArrayList ����� int �� ������ ����� int */
		
		int[] array = new int[list.size()];
		
		for(int i=0;i<list.size();i++)
			array[i] = list.get(i);
		
		return array;
		
	}
	
	private int calcN() {
		
		/* 
		 * � ������� ���� ���������� �� n, �� ����� �� ���������� ����� ������� �� 
		 * �� �������� ��� ���� ��� ����������.
		 */
		
		int n=0;
		
		for(int i=0;i<freq.size();i++)
			n += freq.get(i);
		
		return n;
		
	}

	private double calcMean() {
		
		/* � ������� ���������� ��� ���� ��� ����������� ��� ����. ( sum(frequency*centralValues)/n ) */
		
		// ���� RCaller ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCaller caller = RCaller.create();
				
		// ��� RCode ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCode code = RCode.create();
	
		// � ���� ��� �� ���������� (� �����)
		double mean;
		
		// ����������� ��� ��������� �����
		double[] centralValues = calcCentralValues(interval);
		
		// � ������� frequency �� ������������� �� freq ���� R
		code.addIntArray("freq", frequency);
		
		// � ������� interval �� ������������� �� interv ���� R
		code.addIntArray("interv", interval);	
		
		// � ������� centralValues �� ������������� �� values ���� R
		code.addDoubleArray("values", centralValues);
				
		// ������� �� n ���� R, �� ����� ���� ����������� ��� ��� calcMean()
		code.addInt("n", n);

		// ����������� ��� ����� ���� R		
		code.addRCode("mean <- sum(values*freq)/n");
				
		// ������� ��� ������������ ��� ������ R ��� �� ������
		caller.setRCode(code);

		// �������� ��� ������
		caller.runAndReturnResult("mean");
		
		/* 
		 * � parser ���������� �� ������������ ����� �� ������. ����, ����, ������� ��� �� ������ ����
		 * ��� �������� (mean) ���������� ���� ��� 1� ���� ��� ������ ��� ������������� ��� ���
		 * ��������� ��� mean.
		 */
		mean = caller.getParser().getAsDoubleArray("mean")[0];
		
		return mean;            

	}
	
	private ImageIcon drawPlot() throws IOException {
		
		// ���� RCaller ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCaller caller = RCaller.create();
		
		// ��� RCode ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCode code = RCode.create();
		
		// �� �������� ��� �� ����������� �� ���� �������� ����������� ���� ���������� (���������)
		String[] names = getNames(interval);
		
		// � ������� frequency �� ������������� �� freq ���� R
		code.addIntArray("freq", frequency);
		
		// � ������� interval �� ������������� �� interv ���� R
		code.addIntArray("interv", interval);	
		
		// � ������� names �� ������������� �� names.arg ���� R
		code.addStringArray("names", names);
		
		// To ����������� ������������ �� File
		File plotFile = code.startPlot();
		
		// �������� ��� ������� ��� �������������� ���� R
		code.addRCode("barplot(freq, main=\"\", horiz = T, names=names, las=1, col=\"brown\", cex.names=0.5, ylim = c(0, 24), xlim = c(0, 100))");
		
		// ������� � �������� ��� ������� barplot
		code.endPlot();
		
		// ������ ��� R ������ ���� caller
		caller.setRCode(code);
		
		// ������� ��� script
		caller.runOnly();
		 code.showPlot(plotFile);
		// ��������� ��� ������ ��� �������������� �� imageicon �����������
		ImageIcon plot = code.getPlot(plotFile);

		return plot;
		
	}
	
	private double[] calcCentralValues(int[] interval) {
		
		// ���� RCaller ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCaller caller = RCaller.create();
				
		// ��� RCode ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCode code = RCode.create();
		
		// ������� �� ��� ��������� ����� ��� ����� ����������� (��� ��� ���������� �����, �����������)
		double[] values = new double[n];
								
		// � ������� interval �� ������������� �� interv ���� R
		code.addIntArray("interv", interval);
						
		// � ������� interval �� ������������� �� interv ���� R
		code.addDoubleArray("values", values);
		
		// ����������� ��������� �����
		code.addRCode("k <- 1\r\n" + 
								"  while(k < length(interv)){\r\n" + 
								"    values[k] <- (interv[k] + interv[k+1])/2\r\n" + 
								"    k <- k + 1\r\n" + 
								"  }");
		
		// ������� ��� ������ ���� caller
		caller.setRCode(code);
		
		// �������� ��� ������
		caller.runAndReturnResult("values");
		
		// ��������� ��� ��������� ����� ��� �������� values
		values = caller.getParser().getAsDoubleArray("values");
		
		return values;		
		
	}
	
	private String[] getNames(int[] interval) {
		
		// ���� RCaller ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCaller caller = RCaller.create();
						
		// ��� RCode ��� ���� ������, ��� �� ��� ��� ��������� �������� �� �� threads
		RCode code = RCode.create();
		
		String[] names = new String[interval.length-1];
		
		// � ������� interval �� ������������� �� interv ���� R
		code.addIntArray("interval", interval);
		
		// � ������� interval �� ������������� �� interv ���� R
		code.addStringArray("names", names);
		
		code.addRCode("k=1\r\n" + 
				"				  while(k<length(interval)){\r\n" + 
				"				    names[k] = paste(as.character(interval[k]), \":00 - \", (as.character(interval[k+1])), \":00\")\r\n" + 
				"				    k=k+1\r\n" + 
				"				  }");
		
		// ������� ��� ������ ���� caller
		caller.setRCode(code);
		
		// �������� ��� ������
		caller.runAndReturnResult("names");
		
		names = caller.getParser().getAsStringArray("names");
		
		return names;
	
	}

}