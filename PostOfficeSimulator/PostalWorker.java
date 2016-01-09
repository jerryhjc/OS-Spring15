import java.util.concurrent.Semaphore; // get semaphore powers
import java.util.ArrayList;

public class PostalWorker implements Runnable
{
	//- Declare our PostalWorker variables.
	private int num;
	private Semaphore max_capacity;
	private Semaphore service_counter;
	private Semaphore cust_ready;
	private ArrayList<Semaphore> service_finished;
	private Semaphore leave_service_counter;
	private Semaphore mutex_inner;
	private Semaphore mutex_greet;
	
	//- These variables store the read global customer data of assigned customer.
	private int customer_id;
	private int customer_service;
	
	//- Constructor.
	PostalWorker( int num, 
				  Semaphore max_capacity, 
				  Semaphore service_counter, 
				  Semaphore cust_ready, 
				  ArrayList<Semaphore> service_finished, 
				  Semaphore leave_service_counter,
				  Semaphore mutex_inner,
				  Semaphore mutex_greet)
	{
		//- Save our variables from Main.
		this.num = num;
		this.max_capacity = max_capacity;
		this.service_counter = service_counter;
		this.cust_ready = cust_ready;
		this.service_finished = service_finished;
		this.leave_service_counter = leave_service_counter;
		this.mutex_inner = mutex_inner;
		this.mutex_greet = mutex_greet;
		
		//- Notify that the PostalWorker has been created.
		// System.out.println( "Postal worker " + num + " created." );
			// System.out.println( "Postal worker " + num + " serving customer "+ this.customer_id );
		
	}
	
	//- Thread operation.
	public void run()
	{
		//- Loop the postal worker until the system exits, always accepting and helping customers.
		while(true)
		{
			
			//////////////////////////////////////////////////////////
			//- wait(cust_ready);
			//- Wait until a customer is ready to be served.

			// System.out.println( "Postal worker " + num + " prepares for customer." );
			try{
				cust_ready.acquire();
			}catch (InterruptedException e){}
			
			
			
			//////////////////////////////////////////////////////////
			//- wait(mutex_inner);
			//- Postal worker cannot read the customer information until the Customer object has safely written it.
			try{
				mutex_inner.acquire();
			}catch (InterruptedException e){}
			
		System.out.println( "Postal worker " + num + " created." );
			
			//- Fetch the assigned global customer information.
			this.customer_id = PostOfficeSimulator.cust_id;
			this.customer_service = PostOfficeSimulator.cust_service;
			System.out.println( "Postal worker " + num + " serving customer "+ this.customer_id );


			//////////////////////////////////////////////////////////
			//- signal(mutex_greet);
			//- Let the customer know that the postal worker has successfully read the customer data.
			mutex_greet.release();
				
			//////////////////////////////////////////////////////////
			//- signal(mutex_inner);
			//- Allow other customers to write to the global customer data.			
			mutex_inner.release();
			
			
			//////////////////////////////////////////////////////////
			//- perform_service();
			//- Sleep for the proper service duration, then notify of its completion.
			try {
				Thread.sleep(PostOfficeSimulator.serviceSleep(this.customer_service));
			}catch(InterruptedException e){}
			System.out.println( "Customer " + this.customer_id + " asks postal worker  "+ num + PostOfficeSimulator.serviceDisplay(0, this.customer_service) );
			
			
			//////////////////////////////////////////////////////////
			//- signal(service_finished);
			//- Notify the customer that the service has been fully rendered.
			service_finished.get(this.customer_id).release();
			
			//////////////////////////////////////////////////////////
			//- wait(leave_service_counter);
			//- Wait for the customer to leave the counter.
			try{
				leave_service_counter.acquire(); // decrease line availability
			}catch (InterruptedException e){}
			
			//////////////////////////////////////////////////////////
			//- signal(service_counter);
			//- Allow another customer to approach the counter since it is now vacant.
			service_counter.release();
		}
		
	}
}