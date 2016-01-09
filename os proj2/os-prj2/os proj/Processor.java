import java.io.*;
import java.util.Random;

public class Processor 
{
	static int PC = 0, SP = 1000, timer = 0; //registers
	static int AC, X, Y;
	static String IR = "0";   //instruction register
	static boolean intrrupts=true;

	public static void main(String[] args)
	{
		
		try
		{	
			//initialize memory and create pipe
			Process memory = Runtime.getRuntime().exec("java Memory " + args[0]);
			BufferedReader pipeIn = new BufferedReader(new InputStreamReader(memory.getInputStream()));
        	PrintWriter pipeOut = new PrintWriter(new OutputStreamWriter(memory.getOutputStream()));
            
            
           //execute instruction set
            while(true)
            {
    			//pipe next instruction to memory
            	pipeOut.println(PC);      
				pipeOut.flush();
				
				//read instruction to IR from memory through pipe 
				IR=pipeIn.readLine();
				
				if(!IR.isEmpty() && !IR.equals(""))
				{
					
					//execute different instructon
					switch(Integer.parseInt(IR))
					{
					case 1:  //load value
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 2:  //load address
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						pipeOut.println(pipeIn.readLine());   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 3:  //load value from address2 found in address1 to AC
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						pipeOut.println(pipeIn.readLine());   
						pipeOut.flush();
						pipeOut.println(pipeIn.readLine());   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 4: //load value from address+X to AC
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						pipeOut.println(Integer.parseInt(pipeIn.readLine())+X);   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 5: //load value from address+Y to AC
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						pipeOut.println(Integer.parseInt(pipeIn.readLine())+Y);   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 6:  //load value from Sp+X to AC
						pipeOut.println((SP+X));   
						pipeOut.flush();
						AC=Integer.parseInt(pipeIn.readLine());
						PC++;
						break;
					case 7:  //store value from AC to address
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						pipeOut.println(pipeIn.readLine() + " " + AC);   
						pipeOut.flush();
						PC++;
						break;
					case 8:  //random int from 1 to 100
						Random num = new Random();
						AC=num.nextInt(100) + 1; 		
						PC++;
						break;
					case 9:  //write AC to screen
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						if(Integer.parseInt(pipeIn.readLine()) == 1)
							System.out.print(AC);
						else
							System.out.print((char)AC);
						PC++;
						break;
					case 10:
						AC = AC+X;
						PC++;
						break;
					case 11:
						AC = AC+Y;
						PC++;
						break;
					case 12:
						AC = AC-X;
						PC++;
						break;
					case 13:
						AC = AC-Y;
						PC++;
						break;
					case 14:
						X = AC;
						PC++;
						break;
					case 15:
						AC = X;
						PC++;
						break;
					case 16:
						Y = AC;
						PC++;
						break;
					case 17:
						AC = Y;
						PC++;
						break;
					case 18:
						SP = AC;
						PC++;
						break;
					case 19:
						AC = SP;
						PC++;
						break;
					case 20:
						PC++;
						pipeOut.println(PC);   
						pipeOut.flush();
						PC=Integer.parseInt(pipeIn.readLine());
						break;
					case 21:  //jump to address if AC==0
						PC++;
						if(AC == 0)
						{
							pipeOut.println(PC);   
							pipeOut.flush();
							PC=Integer.parseInt(pipeIn.readLine());
						}
						else
						{
							PC++;
						}
						break;
					case 22:  //jump to address if AC!=0
						PC++;
						if(AC != 0)
						{
							pipeOut.println(PC);   
							pipeOut.flush();
							PC=Integer.parseInt(pipeIn.readLine());
						}
						else
						{
							PC++;
						}
						break;
					case 23:  //push return address
						PC++;
						if(SP > 500)    
						{
							SP--;
							pipeOut.println(SP + " " +(++PC));   
							pipeOut.flush();
							pipeOut.println(--PC);   
							pipeOut.flush();
							PC=Integer.parseInt(pipeIn.readLine());
						}
						else
						{
							System.out.println("User stack error.");
							memory.destroy();
							System.exit(1);
						}
						break;
					case 24:   //pop return address from stack and jump to address
						pipeOut.println(SP);   
						pipeOut.flush();
						PC=Integer.parseInt(pipeIn.readLine());
						SP++;
						break;
					case 25:
						X++;
						PC++;
						break;
					case 26:
						X--;
						PC++;
						break;
					case 27:  //push AC to stack
						if(SP > 500) 
						{
							SP--;
							pipeOut.println(SP + " " + AC);  
							pipeOut.flush();
							PC++;
						}
						else
						{
							System.out.println("User stack error.");
							memory.destroy();
							System.exit(1);
						}
						break;
					case 28: //load value from address of SP to AC 
						pipeOut.println(SP);   
						pipeOut.flush();
						AC = Integer.parseInt(pipeIn.readLine());
						SP++;
						PC++;
						break;
					case 29:  //disable interrupt to avoid nested execution
						if(intrrupts == true)  
						{
							pipeOut.println("1999 "+SP);
							pipeOut.println("1998 "+(++PC));
							pipeOut.println("1997 "+AC);
							pipeOut.println("1996 "+X);
							pipeOut.println("1995 "+Y);
							PC=1500;
							SP=1995;
							intrrupts=false;
						}
						else
						{
							PC++;
						}
						break;
					case 30:   //restore registers and switch to user mode
						intrrupts=true;
						if(SP<2000)
						{
							pipeOut.println(SP);   
							pipeOut.flush();
							Y=Integer.parseInt(pipeIn.readLine());
							SP++;
							pipeOut.println(SP);   
							pipeOut.flush();
							X=Integer.parseInt(pipeIn.readLine());
							SP++;
							pipeOut.println(SP);   
							pipeOut.flush();
							AC=Integer.parseInt(pipeIn.readLine());
							SP++;
							pipeOut.println(SP);   
							pipeOut.flush();
							PC=Integer.parseInt(pipeIn.readLine());
							SP++;
							pipeOut.println(SP);   
							pipeOut.flush();
							SP=Integer.parseInt(pipeIn.readLine());
						}
						else
						{
							System.out.println("System stack error. ");
							memory.destroy();
							System.exit(1);
						}
						break;
					case 50:  //end execution
						pipeOut.println("End"); 	
						System.exit(0);
						
					}
				}
				//timer start
				timer++;
				if(timer == Integer.parseInt(args[1]))
				{
					if(intrrupts==true) 
					{
						pipeOut.println("1999 "+ SP);
						pipeOut.println("1998 "+ PC);
						pipeOut.println("1997 "+ AC);
						pipeOut.println("1996 "+ X);
						pipeOut.println("1995 "+ Y);
						PC=1000;
						SP=1995;
						intrrupts=false;
					}
					timer=0;
				}
            }
		}
		catch(Exception e)
		{
			System.exit(1);
		}
	}

}
