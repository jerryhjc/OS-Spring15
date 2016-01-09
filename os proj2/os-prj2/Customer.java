  class Customer extends Thread {

  /* we create the integer iD which is a unique ID number for every worker
     and a boolean notServed which is used in the Customer waiting loop */

  int iD;
  int bG;
  Semaphore sem;
  boolean notServed=true;

  /* Constructor for the Customer */

  public Customer(int i, int b, Semaphore s) {
    iD = i;
     bG = b;
     sem = s;
  }

  public void run() {   
    while (notServed) {  // as long as the worker is not cut 
      try {
      accessSpots.acquire();  //tries to get access to the rooms
      if (numberOfAvailRoom > 0) {  //if there are any free rooms
        System.out.println("Customer " + this.iD + " enter post office ");
          try {
        sleep(1000);
        } catch (InterruptedException ex){ }
          numberOfAvailRoom--;  //room is taken
        sem.release();  //notify the worker that there is a worker
          help.release();
        try {
          workers.acquire();  // now it's this customers turn but we have to wait if the worker is busy
        notServed = false;  // this worker will now leave after the procedure
          if (bG == 1)
            {
                System.out.println("Customer "+this.iD+" ask postal worker to buy stamp");
                buyStamp();
                help.release();
                workers.release();
                try {

                     sleep(1000);
                } catch (InterruptedException ex){ }


            }
            else if (bG == 2)
            {
                System.out.println("Customer "+this.iD+" ask postal worker to mail letter");
                mailLetter();
                help.release();
                workers.release();
                try {

                     sleep(1000);
                } catch (InterruptedException ex){ }


            }

        else if (bG == 3)
        {
            //scale.release();
            System.out.println("Customer "+this.iD+" ask postal worker to mail package");
            //scale.release();
            mailPackage();
            help.release();
            workers.release();
            //scale.release();
            try {

                     sleep(1000);
            } catch (InterruptedException ex){ }

            //bellHops.acquire();
        }
} catch (InterruptedException ex) {}
      }   
      else  {  // there are no free rooms
        //System.out.println("There are no available room. Customer " + this.iD + " has left the workershop.");
        accessSpots.release();  //release the lock on the rooms
        notServed=true; // .
      }
     }
      catch (InterruptedException ex) {}
    }
  }

  /* this method will simulate getting a hair-cut */

  public void buyStamp(){

  System.out.println("Customer " + this.iD+" finished buying stamp" );
    try {
    sleep(1000);
    } catch (InterruptedException ex) {}
     sem.release();
     leave();

  }
  public void mailLetter(){
  //i = a;

  System.out.println("Customer " + this.iD + " finished mailing letter ");
    try {
    sleep(1500);
    } catch (InterruptedException ex) {}
     sem.release();
     leave();

  }

  public void mailPackage(){
  //i = a;
    scale.release();

  System.out.println("Customer " + this.iD + " finished mailing package ");
    try {
    sleep(2000);
    } catch (InterruptedException ex) {}
     sem.release();
     leave();

  }
    public void leave()
    {
        System.out.println("Customer "+this.iD+" left the post office ");
    }

}


/* THE EMPLOYEE THREAD */


class Worker extends Thread {
 int iD;
 Semaphore sem;
 //int bG

  public Worker(int a, Semaphore s) 
  {
    iD = a;
    sem = s;
    //bG = b;
  }

  public void run() {
    while(true) {  // runs in an infinite loop
      try {
      customers.acquire(); // tries to acquire the next avail customer
        accessSpots.release(); // at this time he has been awaken -> want to modify the number of available rooms
      numberOfAvailRoom++; // one room gets free
        int a = numberOfAvailRoom;
        System.out.println("Postal Worker "+this.iD +" serving cutomer ");
      workers.release();  // the worker is ready
      accessSpots.release(); // we don't need the lock on the room anymore
        //help.acquire();
        //scale.acquire();
        this.Scale();
        //scaleRelease();
        //int a = gNum;
      //this.Finished();  //cutting...      
    } catch (InterruptedException ex) {}
    }
  }
     public void Scale(){
     try
     {
        scale.acquire();
     System.out.println("Scale in use by Postal Worker "+this.iD);
      this.scaleRelease();
     }catch (InterruptedException ex){}

    }

    public void scaleRelease(){
    //try
    //{
        //rel.acquire();
     System.out.println("Scale released by Postal Worker "+this.iD);
     //}catch (InterruptedException ex){}
   }   
    public void Finished(){
        System.out.println("Postal Worker "+this.iD+" finished serving customer ");
        //done.release();

  }
}