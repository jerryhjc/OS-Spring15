import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ArrayList;

public class Customer implements Runnable
{
	// Customer variables.
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
	
	private int post_worker_id;
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
		this.service_needed = generator.nextInt(3) + 1;  //only three service 
		
		//- Notify that the Customer has been created and is traveling to the store.
		System.out.println( "Customer " + num + " created" );
		if(num < 10)
		{
			System.out.println( "Customer " + num + " enters the post office." );
		}
		
	}
	
	//- Thread operation.
	public void run()
	{
		try{
			max_capacity.acquire(); // decrease line availability
		}catch (InterruptedException e){}
		
		try{
			Thread.sleep( ( PostOfficeSimulator.serviceSleep(0) * (num + 1) ) );
		}catch(InterruptedException e){}
		
		// System.out.println( "Customer " + num + " enters the store." );
		try{
			service_counter.acquire(); // take a place at the counter
		}catch (InterruptedException e){}

		// System.out.println( "Customer " + num + " approaches the counter " + PostOfficeSimulator.serviceDisplay(0, this.service_needed));
		
		try{
			mutex_outer.acquire();
		}catch (InterruptedException e){}	
		
		try{
			mutex_inner.acquire();
		}catch (InterruptedException e){}
		

		//- Set global customer information.
		PostOfficeSimulator.cust_id = this.num;
		PostOfficeSimulator.cust_service = this.service_needed;

		//- signal(cust_ready);
		//- This customer is now available to be served by a postal worker.
		cust_ready.release();
		//// signal(mutex_inner);
		mutex_inner.release();

		this.post_worker_id = PostOfficeSimulator.worker_id;

		//- wait(mutex_greet);
		//- Wait until greeted by postal worker before allowing another customer to globalize thier data.
		try{
			mutex_greet.acquire();
		}catch (InterruptedException e){}
		//// signal(mutex_outer);
		System.out.println("Customer " + num + " " + PostOfficeSimulator.serviceDisplay(1, this.service_needed));
		mutex_outer.release();
		// System.out.println("Customer " + num + " " + PostOfficeSimulator.serviceDisplay(1, this.service_needed));

		//- wait(service_finished);
		//- Wait until the assigned postal worker has completed the service desired.
		try{
			service_finished.get(this.num).acquire();
		}catch (InterruptedException e){}

		//- Notify postal worker that customer is leaving.
		leave_service_counter.release();


		//- signal(max_capacity);
		System.out.println( "Customer " + num + " leaves post office." );
		max_capacity.release();
		
	}
}