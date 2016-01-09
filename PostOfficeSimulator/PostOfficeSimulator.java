import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class PostOfficeSimulator
{
	//- Declare our global variables.
	public static int cust_id;
	public static int cust_service;
	public static int line_count;  //可以注释掉
	public static boolean openForBusiness = false;  //可以注释掉, measure 用的
	
	public static void main(String args[])
	{
		
		  ////////////////////////////////
		 //    ARGUMENT ACCEPTANCE     //
		////////////////////////////////
		
		// // Argument Protection: avoid any non-whole number strings.
		// try
		// {
		// 	//- Avoid blank arguments.
		// 	if(args[0].equals("") || args[1].equals("")){
		// 		System.out.println("Usage Error: missing arguments\nFormat: java PostOfficeSimulator [customers] [postal workers]\n\nExample: java PostOfficeSimulator 10 2");
		// 		System.exit(0);
		// 	}
		// 	//- Avoid negative and zero arguments.
		// 	if(Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[1]) < 1){
		// 		System.out.println("Usage Error: negative/zero arguments\nFormat: java PostOfficeSimulator [customers] [postal workers]\nExample: java PostOfficeSimulator 10 2");
		// 		System.exit(0);
		// 	}			
		// }
		// catch(NumberFormatException e)
		// {
		// 	//- Avoid arguments that are not numeric.
		// 	System.out.println("Usage Error: invalid arguments\nFormat: java PostOfficeSimulator [customers] [postal workers]\nExample: java PostOfficeSimulator 10 2");
		// 	System.exit(0);
		// }
		// catch(ArrayIndexOutOfBoundsException e)
		// {
		// 	//- Avoid arguments that are not existent.
		// 	System.out.println("Usage Error: missing arguments\nFormat: java PostOfficeSimulator [customers] [postal workers]\nExample: java PostOfficeSimulator 10 2");
		// 	System.exit(0);
		// }
		




		  ////////////////////////////////
		 //    VARIABLE DECLARATION    //
		////////////////////////////////
		
		// Variable Declaration: create our constant variables.
		// final int NUMCUSTOMERS = Integer.parseInt(args[0]);
		// final int NUMMAXCAPACITY = 10;
		// final int NUMPOSTALWORKERS = Integer.parseInt(args[1]);

		final int NUMCUSTOMERS = 11;  //should be 50 
		final int NUMMAXCAPACITY = 10;
		final int NUMPOSTALWORKERS = 3;
		int i=0;
		
		// Variable Declaration: create our semaphores.
		Semaphore max_capacity = new Semaphore( NUMMAXCAPACITY, true ); //- The line for the store.
		Semaphore service_counter = new Semaphore( NUMPOSTALWORKERS, true );
		Semaphore cust_ready = new Semaphore( 0, true);
		//- Seperate semaphore for each customer regarding services completed by the postal worker.
		ArrayList<Semaphore> service_finished = new ArrayList<Semaphore>();
		for(int s=0; s<NUMCUSTOMERS; s++){
			service_finished.add(s, new Semaphore(0,true));
		}
		Semaphore leave_service_counter = new Semaphore( 0 , true);
		
		// Variable Declaration: create our mutual exclusion semaphores for the critical sections.
		Semaphore mutex_inner = new Semaphore( 3 , true);
		Semaphore mutex_outer = new Semaphore( 1 , true);
		Semaphore mutex_greet = new Semaphore( 0 , true);
		
		
		
		
		
		
		  ////////////////////////////////
		 //      PROCESS CREATION      //
		////////////////////////////////
		
		// Process Creation: notify the system that the store creation is beginning.
		System.out.println("\n\nSimulating Post Office with "+ NUMCUSTOMERS +" customers and "+ NUMPOSTALWORKERS +" postal workers\n");
		PostOfficeSimulator.openForBusiness = true; //- This variable is used by MeasureTaker.
		
		// Process Creation: declare our postal worker objects and threads.
		PostalWorker[] pwThr = new PostalWorker[NUMPOSTALWORKERS];
		Thread[] postalWorkerThreads = new Thread[NUMPOSTALWORKERS];
		
		// Process Creation: declare our customer objects and threads.
		Customer[] cThr = new Customer[NUMCUSTOMERS];
		Thread[] customerThreads = new Thread[NUMCUSTOMERS];
		
		
		
		// Process Creation: create our Customer objects and initiate their threads.
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
			customerThreads[i] = new Thread( cThr[i] ); //- Save the Customer as a single thread.
			customerThreads[i].start(); //- Initiate the thread.
		}
		
		// Process Creation: create our PostalWorker objects and initiate their threads.
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
			postalWorkerThreads[i] = new Thread( pwThr[i] ); //- Save the PostalWorker as a single thread.
			postalWorkerThreads[i].start(); //- Initiate the thread.
		}
		
		
		
		  ////////////////////////////////
		 //    PROCESS TERMINATION     //
		////////////////////////////////		
		
		
		// Process Termination: join our customer threads back into the main process.
		for( i = 0; i < NUMCUSTOMERS; ++i ) 
		{
			try
			{
				customerThreads[i].join(); //- Wait for thread to die.
				System.out.println("Joined customer "+i+".");
			}
			catch (InterruptedException e)
			{
			}
		}
		
		// Process Termination: set our process as complete to stop the MeasureTaker.
		PostOfficeSimulator.openForBusiness = false;
		System.out.println("\nPost Office closed.\n\n");
		
		
	
		
	}
	
	
	/**
	 * Returns a String that displays the type of service a customer desires
	 * or a postal worker is completing. 
	 *
	 * @param  u    the type of user: 0 for customer, 1 for postal worker
	 * @param  s    the type of service desired by customer/postal worker
	 * @return      String that describes the service
	 */
	public static String serviceDisplay(int u, int s)
	{
		if(u == 0){ // customer
			switch(s)
			{
				case 1: return "to buy stamps";
				
				case 2: return "to mail a letter";
				
				case 3: return "to mail a package";
				
				// case 4: return "to buy a money order";
				
				// case 5: return "to pickup a package";
			}	
		}
		else{ // postal worker
			switch(s)
			{
				case 1: return "sells stamps to";
				
				case 2: return "sends a letter for";
				
				case 3: return "sends a package for";
				
				// case 4: return "sells a money order to";
				
				// case 5: return "gets a package for";
			}
		}
		
		return "broken";
	}
	
	
	/**
	 * Returns an integer of the time (in miliseconds) that a service should 
	 * should a thread to sleep for. 
	 *
	 * @param  s    the type of service
	 * @return      an integer that represents miliseconds
	 */
	public static int serviceSleep(int s)
	{
		switch(s)
		{
			case 0: return 500;  // This is the arrival rate of customers.
			
			case 1: return 60000; // This is buying stamps.
				
			case 2: return 90000; // This is mailing a letter.
			
			case 3: return 120000; // This is mailing a package.
			
			case 4: return 2500; // This is buying a money order. //commentt 
			
			case 5: return 3000; // This is picking up a package. //commentt
		}
		
		return 10000; // This is the default sleep which will portray an error in the system.
	}
}

