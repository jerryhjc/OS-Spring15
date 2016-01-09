import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

/*
 * Theater Simulation 
 * Code written by Rohan Pandare(rsp130330) as a part of CS 5348.001 Operating Systems project #2
 * Boa--->Box Office Agent
 * Csw--->Concession Stand Worker
 * Tt--->Ticket Taker
 * Number of customers is initialized to int noOfCutomers=90. It can be changed before compilation.
 * 				finished[class_object]
 * 						   /      \
 * 						  /        \
 *                       /          \
 *        Semaphore served        boolean serverMsg    
 */

public class MainClass 
{
	static BufferedReader readMovie=null,readFile=null; //for reading input movies.txt file
	static ArrayList<MainClass> movieList;  //contains movies list and number of seats
	static MainClass[] finished;  //semaphore array for customers
	String movieName;
	int noOfSeats;
	boolean serverMsg; //available or not
	Random r=new Random(); //generates random numbers
	/*
	 * Change noOfCustomers in the following line
	 */
	static int noOfCutomers=300,noOfBoa=2; //this states number of customers and box office agents. It can be changed before compilation of the code.
	/*
	 * Change noOfCustomers in the above line
	 */
	static Queue<String> queueBoa=new LinkedList<String>();
	static Queue<Integer> queueTt=new LinkedList<Integer>();
	static Queue<String> queueCsw=new LinkedList<String>();
	Semaphore served; //array of semaphores. One for each customer. Used by agents and workers to tell customers when they have finished serving respective customer
	static Semaphore mutexQueueBoa=new Semaphore(1); //mutual exclusion for box office agent queue
	static Semaphore mutexQueueTt=new Semaphore(1);  //mutual exclusion for ticket taker queue
	static Semaphore mutexQueueCsw=new Semaphore(1);  //mutual exclusion for concession stand queue
	static Semaphore mutexMovieArray=new Semaphore(1); //mutual exclusion for array storing movies and available seats. Initial value is 1
	static Semaphore custReadyBoa=new Semaphore(0);  //required by box office agent thread to see if any customer is there in queue
	static Semaphore custReadyTt=new Semaphore(0);   //required by ticket taker thread to see if any customer is there in queue
	static Semaphore custReadyCsw=new Semaphore(0);  //required by concession stand worker thread to see if any customer is there in queue
	
	public MainClass(){}
	
	public MainClass(String movieName,int noOfSeats) //movies array List
	{
		this.movieName=movieName;
		this.noOfSeats=noOfSeats;
	}
	
	public MainClass(Semaphore served,Boolean serverMsg) //customer  semaphore
	{
		this.served=served;
		this.serverMsg=serverMsg; //movie availability(true or false)
	}
	
	class Customer extends Thread //customer thread
	{
		private int myId,myMovieId;
		private String myOrder;
		public Customer(int myId) 
		{
			this.myId=myId;
		}
		public void run()
		{
			try
			{
				selectMovie();
				mutexQueueBoa.acquire();
				enqueueBoa(); //add customer to the box office agent queue
				custReadyBoa.release(); //tell Box office agent that customer is avialable
				mutexQueueBoa.release();
				finished[myId].served.acquire();
				if(isMovieTicketAvailable()==true) //check if movie available
				{
					mutexQueueTt.acquire();
					enqueueTt();  //join queue to see ticket taker
					custReadyTt.release(); //tell ticket taker that customer is avialable
					mutexQueueTt.release();
					finished[myId].served.acquire(); //wait till ticket taken
					if(visitConcessionStand()==true) 	//decide if going to concession stand
					{
						selectOrder();  //choose what to order
						mutexQueueCsw.acquire();
						enqueueCsw();  // join the queue to see concession stand worker
						custReadyCsw.release(); //tell concession stand worker that customer is avialable
						mutexQueueCsw.release();
						finished[myId].served.acquire(); //wait for the order
					}
					enterTheatre();
				}
			} 
			catch (InterruptedException e) {System.out.println("ERROR: Customer thread having id: "+myId);
			}
		}
		private void enqueueCsw()
		{
			queueCsw.add(myId+"\t"+myOrder);
			System.out.println("Customer "+myId+" in line to buy "+myOrder);
		}
		private void enqueueBoa() 
		{
			queueBoa.add(myId+" "+myMovieId); 
			
		}
		private void enqueueTt()
		{
			queueTt.add(myId);
			System.out.println("Customer "+myId+" in line to see ticket taker");
		}
		private boolean isMovieTicketAvailable() 
		{
			return finished[myId].serverMsg;
		}
		private void enterTheatre()
		{
			System.out.println("Customer "+myId+" enters theatre to watch "+movieList.get(myMovieId).movieName);
		}
		private void selectOrder()
		{
			int myOrderId=r.nextInt(3);
			if(myOrderId==0)
				myOrder="Popcorn";
			else if(myOrderId==1)
				myOrder="Soda";
			else if(myOrderId==2)
				myOrder="Popcorn and Soda";
		}
		private boolean visitConcessionStand() 
		{
			int decide=r.nextInt(2);
			if(decide==0)
				return true;
			else
				return false;
		}
		private void selectMovie() 
		{
			myMovieId=r.nextInt(movieList.size()); //generate random movieID
			System.out.println("Customer "+myId+" buying ticket to "+movieList.get(myMovieId).movieName);
		}
	}
	
	class BoxOfficeAgent extends Thread
	{
		private int myBoaId;
		private String fetchCustomer;
		private int customerMovieId,customerId;
		private boolean availableOrNot;
		public BoxOfficeAgent(int myBoaId) 
		{
			this.myBoaId=myBoaId;
			System.out.println("Box Office Agent "+myBoaId+" created");
		}
		
		public void run()
		{
			while(true)
			{
				try
				{
					custReadyBoa.acquire(); //check if someone is in queue
					mutexQueueBoa.acquire(); 
					dequeueBoa();  //get first customer in  queue
					mutexQueueBoa.release();
					mutexMovieArray.acquire();
					checkMovieAvailability(); //check for movie availability
					mutexMovieArray.release();					
					ProcessBoa();
					finished[customerId].served.release(); //tell customer that it has been processed	
				}
				catch(InterruptedException e){System.out.println("ERROR: BOA thread having id: "+myBoaId);}
			}
		}

		private void ProcessBoa() 
		{
			try {sleep(1500);}
			catch (InterruptedException e) {}
			if(availableOrNot==true)
				System.out.println("Box office agent "+myBoaId+" sold ticket for "+movieList.get(customerMovieId).movieName+" to customer "+customerId);
			finished[customerId].serverMsg=availableOrNot; 
		}
		private void checkMovieAvailability() 
		{
			if(movieList.get(customerMovieId).noOfSeats>0)
			{
				movieList.get(customerMovieId).noOfSeats--;
				availableOrNot=true;
			}
			else
				availableOrNot=false;
		}
		private void dequeueBoa() 
		{
			fetchCustomer=queueBoa.remove(); 
			customerId=Integer.parseInt(fetchCustomer.split(" ")[0]);
			customerMovieId=Integer.parseInt(fetchCustomer.split(" ")[1]);
			System.out.println("Box office agent "+myBoaId+" serving customer "+customerId);
		}
	}
	
	class TicketTaker extends Thread
	{
		private int customerId;
		public TicketTaker() 
		{
			System.out.println("Ticket Taker created");
		}
		public void run()
		{
			while(true)
			{
				try
				{
					custReadyTt.acquire(); //check if queue is not empty
					mutexQueueTt.acquire();
					dequeueTt(); //attend next customer in queue
					mutexQueueTt.release();
					ProcessTt();
	        		finished[customerId].served.release(); //tell customer that it has been processed
				}
				catch(InterruptedException e){System.out.println("ERROR: Ticket Taker serving Customer Id: "+customerId);}
			}
		
		}
		private void ProcessTt() 
		{
			try {sleep(250);}
			catch (InterruptedException e) {}
    		System.out.println("Ticket taken from customer "+customerId);
		}
		private void dequeueTt() 
		{
			customerId=queueTt.remove();			
		}
	}
	
	class ConcessionStandWorker extends Thread
	{
		private int customerId;
		private String customerOrder,fetchCustomer;
		public ConcessionStandWorker() 
		{
			System.out.println("Concession Stand Worker created");
		}
		public void run()
		{
			while(true)
			{
				try
				{
					custReadyCsw.acquire(); //check if queue is not empty
					mutexQueueCsw.acquire();
					dequeueCsw();  //attend next customer in queue
					mutexQueueCsw.release();
					ProcessCsw();
            		finished[customerId].served.release(); //tell customer that it has been processed
				}
				catch(InterruptedException e){System.out.println("ERROR: Concession Stand Worker serving Customer Id: "+customerId);}
			}
		}
		private void ProcessCsw() 
		{
			customerId=Integer.parseInt(fetchCustomer.split("\t")[0]); //ask for ID
			customerOrder=fetchCustomer.split("\t")[1];   //take order
			System.out.println("Order for "+customerOrder+" taken from customer "+customerId);
    		try {sleep(3000);}
    		catch (InterruptedException e) {}
    		System.out.println(customerOrder+" given to customer "+customerId);
		}
		private void dequeueCsw()
		{
			fetchCustomer=queueCsw.remove(); 
		}
		
	}
	
	public static void main(String[] args) 
	{
		//MAKING MOVIE & NO. OF SEATS ARRAYLIST FROM INPUT MOVIE.TXT FILE
		
		movieList=new ArrayList<MainClass>();
		try{readFile = new BufferedReader(new FileReader(args[0]));}
		catch (FileNotFoundException e) {System.out.println("ERROR: Movies File not found");System.exit(1);}
		try
		{
			String line=null;
			while((line= readFile.readLine()) != null)
			{
				try
				{
					movieList.add(new MainClass(line.split("\t")[0],Integer.parseInt(line.split("\t")[1])));
				}
				catch(NumberFormatException e){System.out.println("ERROR: Movies file is not formatted correctly");}
			}
		} 
		catch (IOException e) {System.out.println("Error reading file");System.exit(1);}
		
		// CREATING BOX OFFICE AGENT THREADS
		
		MainClass mc=new MainClass();
		for(int i=0;i<noOfBoa;i++)
    	{
    		BoxOfficeAgent boa=mc.new BoxOfficeAgent(i);
    		boa.start();
    	}
		
		// CREATING TICKET TAKER THREAD
		
		TicketTaker tt=mc.new TicketTaker();
		tt.start();
		
		//CREATING CONCESSION STAND WORKER THREAD
		
		ConcessionStandWorker csw=mc.new ConcessionStandWorker();
		csw.start();
		
		//CREATING CUSTOMER THREADS
		
		Thread[] custThread=new Thread[noOfCutomers];
		finished=new MainClass[noOfCutomers];
		for(int i=0;i<noOfCutomers;i++)
		{                                                                                                            
			custThread[i]=mc.new Customer(i);
			finished[i]=new MainClass(new Semaphore(0),false);
			custThread[i].start();
		}
		
		//JOINING ALL CUSTOMER THREADS
		
		for(int i=0;i<noOfCutomers;i++)
    	{
    		try {
				custThread[i].join();
				System.out.println("Joined customer "+i);
			} catch (InterruptedException e) {}
    	}

		//EXIT PROGRAM WHEN ALL CUSTOMER THREADS JOIN
		
    	System.exit(0);
	}

}
