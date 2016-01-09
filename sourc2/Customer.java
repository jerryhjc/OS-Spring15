import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ArrayList;

public class Customer implements Runnable
{
	//- Declare our Customer variables.
	private int num; //- customer id.
	private int service_needed; //- service desired.
	private Semaphore max_capacity;
	private Semaphore service_counter;
	private Semaphore cust_ready;
	private ArrayList<Semaphore> service_finished;
	private Semaphore leave_service_counter;
	private Semaphore mutex_inner;
	private Semaphore mutex_outer;
	private Semaphore mutex_greet;	
	
	//- Constructor.
	Customer( int num, 
				  Semaphore max_capacity, 
				  Semaphore service_counter, 
				  Semaphore cust_ready, 
				  ArrayList<Semaphore> service_finished, 
				  Semaphore leave_service_counter,
				  Semaphore mutex_inner,
				  Semaphore mutex_outer,
				  Semaphore mutex_greet )
	{
		//- Save our variables from Main.
		this.num = num;
		this.max_capacity = max_capacity;
		this.service_counter = service_counter;
		this.cust_ready = cust_ready;
		this.service_finished = service_finished;
		this.leave_service_counter = leave_service_counter;
		this.mutex_inner = mutex_inner;
		this.mutex_outer = mutex_outer;
		this.mutex_greet = mutex_greet;
		
		//- Randomly generate our customer's service desire.
		Random generator = new Random();
		this.service_needed = generator.nextInt(5) + 1;
		
		//- Notify that the Customer has been created and is traveling to the store.
		System.out.println( "Customer " + num + " created." );
		
	}
	
	//- Thread operation.
	public void run()
	{
		
		//////////////////////////////////////////////////////////
		//- wait(max_capacity);
		//- Wait until customer can enter the line.
		try{
			max_capacity.acquire(); // decrease line availability
		}catch (InterruptedException e){}
		
		//- Sleep .5 seconds per customer before entering the store.
		//- Use the customer id as a multiple of the sleep time.
		try{
			Thread.sleep( ( PostOfficeSimulator.serviceSleep(0) * (num + 1) ) );
		}catch(InterruptedException e){}
		
		//- Increase current global line count for MeasureTaker.
		PostOfficeSimulator.line_count++;
		
		
		//////////////////////////////////////////////////////////
		//- enter_post_office();
		System.out.println( "Customer " + num + " enters the store." );
		
		
		//////////////////////////////////////////////////////////
		//- wait(service_counter);
		//- Wait until a spot at the postal worker's counter has opened.
		try{
			service_counter.acquire(); // take a place at the counter
		}catch (InterruptedException e){}
		
		
		//////////////////////////////////////////////////////////
		//- approach_service_counter();
		//- A spot at the counter has opened, now the customer can approach.
		System.out.println( "Customer " + num + " approaches the counter " + PostOfficeSimulator.serviceDisplay(0, this.service_needed) + "." );
		
		
		
		
		
		//////////////////////////////////////////////////////////
		//// wait(mutex_outer);
		//// MUTEX OUTER: Limit other customers from writing to our global customer data
		////    START     until a postal worker has read this customer data. This represents
		////              assigning a postal worker to a particular customer.
		try{
			mutex_outer.acquire();
		}catch (InterruptedException e){}	
		
				//////////////////////////////////////////////////////////
				//// wait(mutex_inner);
				//// MUTEX INNER: Ensure that the postal worker assigned cannot gain access
				////    START     to the global customer data until the customer has written
				////              to it. 
				try{
					mutex_inner.acquire();
				}catch (InterruptedException e){}
				
				
						//- Set global customer information.
						PostOfficeSimulator.cust_id = this.num;
						PostOfficeSimulator.cust_service = this.service_needed;
		
						//////////////////////////////////////////////////////////
						//- signal(cust_ready);
						//- This customer is now available to be served by a postal worker.
						cust_ready.release();
					
				
				//////////////////////////////////////////////////////////
				//// signal(mutex_inner);
				//// MUTEX INNER: Release the postal worker to read the global customer information
				////     END      written by the customer now that it is complete.
				mutex_inner.release();
			
				//////////////////////////////////////////////////////////
				//- wait(mutex_greet);
				//- Wait until greeted by postal worker before allowing another customer to globalize thier data.
				try{
					mutex_greet.acquire();
				}catch (InterruptedException e){}
			
		//////////////////////////////////////////////////////////
		//// signal(mutex_outer);
		//// MUTEX OUTER: Other customers may now access the global customer data since this
		////     END      customer has safely written to it, and their assigner postal worker
		////              has safely read from it.
		mutex_outer.release();
		
		
		
		
		
		//////////////////////////////////////////////////////////
		//- wait(service_finished);
		//- Wait until the assigned postal worker has completed the service desired.
		try{
			service_finished.get(this.num).acquire();
		}catch (InterruptedException e){}

		//////////////////////////////////////////////////////////		
		//- leave_service_counter();		
		//- signal(leave_service_counter);
		//- The customer has received the service from the postal worker and can leave a counter space.
		System.out.println( "Customer " + num + " leaves the counter." );
		//- Notify postal worker that customer is leaving.
		leave_service_counter.release();
		
		//////////////////////////////////////////////////////////		
		//- exit_post_office();
		//- Decrease our global line count.
		PostOfficeSimulator.line_count--;
		
		//////////////////////////////////////////////////////////		
		//- signal(max_capacity);
		//- Leave the store's line and make room for another created customer.
		System.out.println( "Customer " + num + " leaves the store." );
		max_capacity.release();
		
	}
}