import java.io.*;

public class Memory 
{

	static int[] mem=new int[2000]; //memory array 
	static BufferedReader input_file;
	static String pipe;

	public static void main(String[] args) 
	{
		//memory initial 
		try 
		{
			input_file = new BufferedReader(new FileReader(args[0])); //read input file 
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("fail to read file");
			System.exit(1);
		}
		
		try
		{
			String line;
			int index = 0;
			while((line = input_file.readLine()) != null)   //read input file line by line 
			{
				try
				{
					if(line.contains(" "))
						line = line.substring(0, line.indexOf(" ")); //remove comment

					if(!line.isEmpty())
						mem[index++] = Integer.parseInt(line); //write lines from input file to memory
				}
				catch(NumberFormatException e)
				{
					index = Integer.parseInt(line.substring(1)); //handle addresses start with '.'
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("fail to write memory");
			System.exit(1);
		}
		
		BufferedReader pipeRead=new BufferedReader(new InputStreamReader(System.in)); //pipe input
		while(true)
		{
			//read from pipe
			try
			{
				pipe=pipeRead.readLine();
			} 		
			catch (IOException e) 
			{
				System.out.println("fail to read from pipe");
				System.exit(1);
			}
			
			if(pipe.equals("End")) //end execution once reach end
				System.exit(0);

			//write to pipe	
			if(!pipe.isEmpty())
			{
				try
				{
					//write to pipe
					System.out.println(mem[Integer.parseInt(pipe)]);				
				}
				catch(NumberFormatException e)
				{   //write to memory
					mem[Integer.parseInt(pipe.split(" ")[0])]=Integer.parseInt(pipe.split(" ")[1]); 
				}
			}
		}
		
	}
}
