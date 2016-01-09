import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class PostOfficeSimulator
{
	//declare variables.
	public static int cust_id;
	public static int cust_service;
	public static int worker_id;

	public static void main(String args[])
	{
		// declare constant variables.
		final int NUMCUSTOMERS = 50;
		final int NUMMAXCAPACITY = 10;
		final int NUMPOSTALWORKERS = 3;
		int i = 0;
		
		// declare semaphores.
		Semaphore max_capacity = new Semaphore( NUMMAXCAPACITY, true ); //- The line for the store.
		Semaphore service_counter = new Semaphore( NUMPOSTALWORKERS, true );
		Semaphore cust_ready = new Semaphore( 0, true);

		//declare semaphore for each customer regarding services completed by the postal worker.
		ArrayList<Semaphore> service_finished = new ArrayList<Semaphore>();
		for(int s=0; s<NUMCUSTOMERS; s++){
			service_finished.add(s, new Semaphore(0,true));
		}
		Semaphore leave_service_counter = new Semaphore( 0 , true);
		
		// declare mutual exclusion semaphores for the critical sections.
		Semaphore mutex_inner = new Semaphore( 3 , true);
		Semaphore mutex_outer = new Semaphore( 1 , true);
		Semaphore mutex_greet = new Semaphore( 0 , true);
		
		// notify that simulation is beginning.
		System.out.println("\nSimulating Post Office with "+ NUMCUSTOMERS +" customers and "+ NUMPOSTALWORKERS +" postal workers\n");

		// declare postal worker objects and threads.
		PostalWorker pwThr[] = new PostalWorker[NUMPOSTALWORKERS];
		Thread postalWorkerThreads[] = new Thread[NUMPOSTALWORKERS];
		
		// declare customer objects and threads.
		Customer cThr[] = new Customer[NUMCUSTOMERS];
		Thread customerThreads[] = new Thread[NUMCUSTOMERS];
		
		// create Customer objects and initiate their threads.
		for( i = 0; i < NUMCUSTOMERS; ++i )
		{
			cThr[i] = new Customer(i,
								   max_capacity,
								   service_counter,
								   cust_ready,
								   service_finished,
								   leave_service_counter,
								   mutex_inner,
								   mutex_outer,
								   mutex_greet);
			customerThreads[i] = new Thread( cThr[i] ); 
			customerThreads[i].start(); 
		}

		// create PostalWorker objects and initiate their threads.
		for( i = 0; i < NUMPOSTALWORKERS; ++i )
		{
			pwThr[i] = new PostalWorker(i,
										max_capacity,
										service_counter,
										cust_ready,
										service_finished,
										leave_service_counter,
										mutex_inner,
										mutex_greet);
			postalWorkerThreads[i] = new Thread( pwThr[i] ); 
			postalWorkerThreads[i].start(); 
		}
		
		// join customer threads back into the main process.
		for( i = 0; i < NUMCUSTOMERS; ++i ) 
		{
			try
			{
				customerThreads[i].join(); 
				System.out.println("Joined customer " + i);
			}
			catch (InterruptedException e)
			{
			}
		}
		
		// Process Termination: terminate the process.
		System.exit(0);
		
	}
	
	
	// customer selection 
	public static String serviceDisplay(int u, int s)
	{
		if(u == 0){ 
			switch(s)
			{
				case 1: return "to buy stamps";
				
				case 2: return "to mail a letter";
				
				case 3: return "to mail a package";
			}	
		}
		else if (u == 1){ 
			switch(s)
			{
				case 1: return "finished buying stamps";
				
				case 2: return "finished mailing a letter";
				
				case 3: return "finished mailing a package";
				
			}
		}
		
		return "broken";
	}
	
	public static int serviceSleep(int s)
	{
		switch(s)
		{
			case 0: return 500;  //
			
			case 1: return 60000; // This is buying stamps. 60000
				
			case 2: return 90000; // This is mailing a letter. 90000
			
			case 3: return 120000; // This is mailing a package. 120000
			
		}
		
		return 10000; // This is the default sleep 
	}
}

