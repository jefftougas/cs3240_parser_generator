import java.util.regex.*;
import java.io.*;
//import java.util.Scanner;

class Scanner{
	
	public static void main(String[] args)
	{
		String userInput = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		//get user input
		if (args.length > 0) {
			userInput = args[0];
		}
		else
		{
			System.out.println("Usage\n");
			System.out.println("Using scanner.sh:");
			System.out.println("./scanner.sh programfile");
			System.out.println("\nUsing java directly, from within src directory:");
			System.out.println("java Scanner programfile");
			return;
			
			/*
			System.out.println("Input?");
			try {
				userInput = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		
		//get input from file
		String fileInput = "";
		try{
		    FileInputStream fstream = new FileInputStream(userInput);
		    
		    //System.out.println(fstream.getFD());
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      fileInput = fileInput + " " + strLine;
		    }
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
		
		//parse string
		//System.out.println("Output:");
		//String outputString = parseLinebyChar(userInput);
		String outputString = parseLinebyChar(fileInput);
		//System.out.println(parseLine(userInput));
		//System.out.println(outputString);
		
		//save tokens to text file
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(userInput + ".tokens"));
			System.out.println("Writing to file: " + userInput + ".tokens");
			out.write(outputString);
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static String matchString(String s)
	{
		String temp = s.toLowerCase();
		if(temp.isEmpty()){
			return "";
		}
		
		if(Pattern.matches("^\\d*$",temp)){
			return "INTNUM ";
		}
		else if(temp.equals("begin")){
			return "BEGIN ";
		}
		else if(temp.equals("end")){
			return "END ";
		}
		else if(temp.equals("print")){
			return "PRINT ";
		}
		else if(temp.equals("read")){
			return "READ ";
		}
		else if(temp.equals("end")){
			return "END ";
		}
		else if(temp.equals(":=")){
			return "ASSIGN ";
		}
		else{
			return "ID ";
		}
	}
	
	public static String parseLinebyChar(String s)
	{
		String bufferString = "";
		String parsedString = "";
		char temp = ' ';
		String tempString;
		for(int i = 0; i < s.length(); i++)
		{
			temp = s.charAt(i);
			if(temp == '+')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "PLUS ";
				bufferString = "";
			}
			else if(temp == ';')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "SEMICOLON ";
				bufferString = "";
			}
			else if(temp == ',')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "COMMA ";
				bufferString = "";
			}
			else if(temp == '(')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "LEFTPAR ";
				bufferString = "";
			}
			else if(temp == ')')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "RIGHTPAR ";
				bufferString = "";
			}
			else if(temp == '-')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "MINUS ";
				bufferString = "";
			}
			else if(temp == '*')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "MULTIPLY  ";
				bufferString = "";
			}
			else if(temp == '%')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				parsedString = parsedString + "MODULO ";
				bufferString = "";
			}
			else if(temp == ' ')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				bufferString = "";
			}
			else if(temp == '\t')
			{
				tempString = matchString(bufferString);
				parsedString = parsedString + tempString;
				bufferString = "";
			}
			else
			{
				bufferString = bufferString + temp;
			}
		}
		tempString = matchString(bufferString);
		parsedString = parsedString + tempString;
		bufferString = "";
		
		
		
		return parsedString.trim().replaceAll("\\s+", "\n");
	}
}