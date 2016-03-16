package part4;

import java.util.Arrays;

public class Statistics 
{
    double[] data;
    int size = 0;   

    public Statistics(double[] data) 
    {
        for(double a : data)
        {
        	if(a != -1)
        		size++;
        }
        
        double[] tempdata = new double[size];
        int temp = 0;
        
        for(double a : data)
        {
        	if(a != -1)
        	{
        		tempdata[temp] = a;
        		temp++;
        	}
        }
        
        this.data = tempdata;
    }   

    double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            	sum += a;
        return sum/size;
    }

    double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
           		temp += (mean-a)*(mean-a);
        return temp/size;
    }

    double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double median() 
    {
       Arrays.sort(data);

       if (data.length % 2 == 0) 
       {
          return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
       } 
       else 
       {
          return data[data.length / 2];
       }
    }
}