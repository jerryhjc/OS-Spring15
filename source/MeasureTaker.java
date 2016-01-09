import java.util.ArrayList;

public class MeasureTaker implements Runnable
{
	//- Declare our variables.
	public ArrayList<Integer> line_counts = new ArrayList<Integer>();
	
	//- Thread operation.
	public void run()
	{
		//- Continually operate as long as the Main process grants it.
		while(PostOfficeSimulator.openForBusiness)
		{
			//- Sleep for 1 second before reading the global line count.
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){}
			
			//- Read the global line count.
			this.line_counts.add(PostOfficeSimulator.line_count);
		}
	}
}