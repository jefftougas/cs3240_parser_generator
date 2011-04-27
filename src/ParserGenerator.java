import java.util.regex.*;
import java.io.*;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;

public class ParserGenerator {
	static ArrayList<String> tokensList = new ArrayList<String>();
	static ArrayList<String> nonTerminalsList = new ArrayList<String>();
	static ArrayList<String> testLanguage = new ArrayList<String>();
	
	static ArrayList<Rule> rules = new ArrayList<Rule>();
	static String start;
	
	static Hashtable<String, ArrayList<String>> tokensFirstSet = new Hashtable<String, ArrayList<String>>();
	static Hashtable<String, ArrayList<String>> firstSet = new Hashtable<String, ArrayList<String>>();
	static Hashtable<String, ArrayList<String>> firstSetComplete = new Hashtable<String, ArrayList<String>>();
	static Hashtable<String, ArrayList<String>> followSet = new Hashtable<String, ArrayList<String>>();
	static Hashtable<String, Hashtable<String, String>> parsingTable = new Hashtable<String, Hashtable<String, String>>();
	
	static String HEADER = "----------------------"; 
	
	public static void main(String[] args) throws IOException {
		String tokens, nonTerminals;
		
		if (args.length == 0) {
			System.out.println("Usage\n");
			System.out.println("Using parser.sh:");
			System.out.println("./parser.sh grammarfilepath [tokenfilepath]\n");
			System.out.println("Using java directly, from within src directory:");
			System.out.println("java ParserGenerator grammarfilepath [tokenfilepath]\n");
			
			System.out.println("If the optional tokenfilepath is provided, then the tokens will be parsed with the given grammar. Otherwise ParserGenerator ends after generating the parsing table.");
			return;
		}
		
		String fileName = args[0];
		//String fileName = "testtable.txt";
		String languageFileName = null;
		if (args.length > 1)
			languageFileName = args[1];
		//String languageFileName = "output.txt";

		
		StringBuilder text = new StringBuilder();
	    
		String newline = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(new FileInputStream(fileName));
	
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println(HEADER + " READING GRAMMAR FILE " + HEADER);
	    System.out.println(HEADER + "----------------------" + HEADER);
	    
	    try {
	      	tokens = scanner.nextLine();
			tokens = tokens.replaceFirst("(?i:%Tokens )", "");
			Collections.addAll(tokensList, tokens.split("\\s"));
			System.out.println("First line (tokens): " + tokensList);
			
		 	nonTerminals = scanner.nextLine();
			nonTerminals = nonTerminals.replaceFirst("(?i:%Non-Terminals )", "");
			Collections.addAll(nonTerminalsList, nonTerminals.split("\\s"));
			System.out.println("Second line (non-terminals): " + nonTerminalsList);
			
			
			start = scanner.nextLine();
			start = start.replaceFirst("(?i:%Start )", "");
			System.out.println("Start variable: " + start);
			
			scanner.nextLine(); // Swallow the "%Rules" line
			
			int i = 0;
			
			while (scanner.hasNextLine()) {
				String nl = scanner.nextLine();
				
				Rule currentRule = new Rule(nl);
				
				List<Rule> modifiedRules = currentRule.eliminateLeftRecursion();
				
				for (Rule r : modifiedRules) {
					List<Rule> noncommonPrefixRules = r.eliminateCommonPrefix();
					
					for (Rule cpf : noncommonPrefixRules) {
						if (!nonTerminalsList.contains(cpf.symbol))
							nonTerminalsList.add(cpf.symbol);
					}
					
					rules.addAll(noncommonPrefixRules);
				}
				
				//rules.add(currentRule);
				
				
				//System.out.println("Original Rule " + i + ": " + currentRule);
				//if (modifiedRules.size() > 1) {
				//	System.out.println("Modified version of Rule " + i + ": ");
				//	for (Rule r : modifiedRules) {
				//		System.out.println("	" + r);
				//	}
				//}
				
				
				i++;
			}
			
			// Massage the rules by combining duplicate symbol definitions into one "rule" object (with multiple productions)
			for (int x = 0; x < rules.size(); x++) {
				Rule rule_x = rules.get(x);
				for (int y = x + 1; y < rules.size(); y++) {
					Rule rule_y = rules.get(y);
					if (rule_x.symbol.equals(rule_y.symbol)) {
						for (String p : rule_y.productions) {
							if (!rule_x.productions.contains(p)) {
								rule_x.productions.add(p);
							}
						}
						rules.remove(rule_y);
						y--;
					}
				}
			}
			
			for (Rule r: rules) {
				System.out.println("Rule: " + r);
			}
	    }
	    finally{
	      scanner.close();
	    }
	    
	    
	    System.out.println("\n");
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println(HEADER + "  READING TOKEN FILE  " + HEADER);
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println();
	    
	  //get language tokens from scanner output file
	    
	    if (languageFileName != null) {
		    scanner = new Scanner(new FileInputStream(languageFileName));
		    try {
		      	String temp;
		      	while(scanner.hasNextLine())
		      	{
		      		temp = scanner.nextLine();
		      		testLanguage.add(temp);
		      	}
		    }
		    finally{
		      scanner.close();
		    }
	    }
	   
	    System.out.println("Read tokens: " + testLanguage);
		
	    System.out.println("\n");
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println(HEADER + "   CALC FIRST SET     " + HEADER);
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println();
	    
	    firstSet();
	    
	    System.out.println("First set: " + firstSet);
	    
	    System.out.println("\n");
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println(HEADER + "   CALC FOLLOW SET    " + HEADER);
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println();
	    
	    followSet();
	    
	    System.out.println("Follow set: " + followSet);
	    
	    System.out.println("\n");
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println(HEADER + "   CALC PARSE TABLE   " + HEADER);
	    System.out.println(HEADER + "----------------------" + HEADER);
	    System.out.println();
	    
	    System.out.println("Note: This is in CSV (comma-separated value) format, and can be opened with Microsoft Excel or something similar.\n\n" + fileName + ".parsetable.csv\n");
	    
	    computeParsingTable();
		
	    StringBuilder sb = new StringBuilder();
	    
		sb.append("M[N][T]");
		for (String t : tokensList) {
			sb.append("," + t);
		}
		sb.append("," + "$\n");
		
		for (String n : nonTerminalsList) {
			sb.append(n);
			Hashtable<String, String> currentRow = parsingTable.get(n);
			for (String t: tokensList) {
				String currentRule = currentRow.get(t);
				sb.append("," + (currentRule == null ? "" : currentRule));
			}	
			String currentRule = currentRow.get("$");
			sb.append("," + (currentRule == null ? "" : currentRule) + "\n");
		}
		
		System.out.println(sb.toString());
		
		//save parse table to text file
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(fileName + ".parsetable.csv"));
			out.write(sb.toString());
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (languageFileName != null) {
			System.out.println("\n");
		    System.out.println(HEADER + "----------------------" + HEADER);
		    System.out.println(HEADER + "    PARSE LANGUAGE    " + HEADER);
		    System.out.println(HEADER + "----------------------" + HEADER);
		    System.out.println();
			
			
			parseLanguage();
		}
	}
	
	public static void leftRecursion() {
		
	}
	
	public static void commonPrefix() {
		
	}
	
	public static void parseLanguage()
	{
		Stack<String> parseStack = new Stack<String>();	//symbol stack
		Stack<String> tokenStack = new Stack<String>(); //stack of input tokens
		
		parseStack.push("$");
		parseStack.push(start);	//push start symbol onto stack
		
		tokenStack.push("$");
		
		for (int i = testLanguage.size() - 1; i >= 0; i--) {
			tokenStack.push(testLanguage.get(i));
		}
		
		//System.out.println("On all stacks, left is bottom of stack.");
		//System.out.println("Start input stack: " + tokenStack);
		//System.out.println("Start parse stack: " + parseStack);
		
		String partialSentence = "";	//partial sentence to print in case of error
		
		while(!parseStack.peek().equals("$")) {
			String topOfParseStack = parseStack.peek();
			String topOfInputStack = tokenStack.peek();
			
			//System.out.println("Current input stack: " + tokenStack);
			//System.out.println("Current parse stack: " + parseStack);
			
			if (tokensList.contains(topOfParseStack) && topOfParseStack.equals(topOfInputStack)) 
			{
				//System.out.println("Matched tokens!");
				
				parseStack.pop();
				tokenStack.pop();
			} 
			else if (	nonTerminalsList.contains(topOfParseStack) && 
						(tokensList.contains(topOfInputStack) || topOfInputStack.equals("$")) &&
						parsingTable.get(topOfParseStack).get(topOfInputStack) != null)
			{
				String production = parsingTable.get(topOfParseStack).get(topOfInputStack);
				
				//System.out.println("Generating for production: " + production);
				parseStack.pop();
								
				String[] productionElements = production.trim().split("\\s+");
				
				for (int i = productionElements.length - 1; i >= 0; i--) {
					if (!"EMPTY".equals(productionElements[i]))
							parseStack.push(productionElements[i]);
				}
			}
			else
			{
				System.out.println("There was an error on token " + topOfInputStack);
				System.out.println("Current input stack: " + tokenStack);
				System.out.println("Current parse stack: " + parseStack);
				break;
			}
		}
		
		if ("$".equals(parseStack.peek()) && "$".equals(tokenStack.peek())) {
			System.out.println("Successful parse!");
		} else {
			System.out.println("Unsuccessful parse!");
		}
		
		//System.out.println("End input stack: " + tokenStack);
		//System.out.println("End parse stack: " + parseStack);
	}

	
	public static void firstSet() {
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> emptySet = new ArrayList<String>();
		emptySet.add("EMPTY");
		
		for (String s : tokensList) 
		{
			ArrayList<String> tokenString = new ArrayList<String>();
			tokenString.add(s);
			tokensFirstSet.put(s, tokenString);
		}
		
		
		// Initialize all first sets with blank
		for(Rule r : rules)
		{
			if (!firstSet.containsKey(r.symbol)) {
				firstSet.put(r.symbol, new ArrayList<String>());
			}
		}
		//System.out.println("firstSet to start: " + firstSet);
		
		
		boolean areChanges = true;
		
		while (areChanges) {
			// First reset this boolean.  Will be marked to true if at any point changes are made to a first set
			areChanges = false;
			
			for (Rule r: rules) {
				//System.out.println("Considering rule: " + r);
				ArrayList<String> consideredSymbolSet = firstSet.get(r.symbol);
				
				
				for (String production : r.productions) {
					String[] productionItems = production.trim().split("\\s+");
					
					if (!firstSetComplete.containsKey(r.symbol + ":" + production))
						firstSetComplete.put(r.symbol + ":" + production, new ArrayList<String>());
					
					ArrayList<String> individualSet = firstSetComplete.get(r.symbol + ":" + production);
					
					int k = 0;
					boolean cont = true;
					
					while (k < productionItems.length && cont)
					{
						String item = productionItems[k];
						
						ArrayList<String> curTerm;
						
						if (firstSet.containsKey(item)) {
							//System.out.println("Getting curTerm for key item: " + item);
							curTerm = firstSet.get(item);
						} else {
							//System.out.println("FirstSet does not contain key item: " + item);
							curTerm = new ArrayList<String>();
							curTerm.add(item);
						}
						
						ArrayList<String> minusEmptySet = new ArrayList<String>();
						minusEmptySet.addAll(curTerm);
						
						ArrayList<String> individualMinusEmptySet = new ArrayList<String>();
						individualMinusEmptySet.addAll(curTerm);
						
						//System.out.println("	minusEmptySet: " + minusEmptySet);
						boolean emptyContained = minusEmptySet.contains("EMPTY");
						if (emptyContained) {
							minusEmptySet.remove("EMPTY");
							//individualMinusEmptySet.remove("EMPTY");
						} else {
							cont = false;
						}
						
						minusEmptySet.removeAll(consideredSymbolSet);
						
						areChanges = consideredSymbolSet.addAll(minusEmptySet) || areChanges;
						
						individualMinusEmptySet.removeAll(individualSet);
						
						individualSet.addAll(individualMinusEmptySet);
						
						k = k + 1;
					}
					
					if (cont) {
						//System.out.println("Continuing!  Adding empty character to: " + consideredSymbolSet);
						areChanges = (consideredSymbolSet.contains("EMPTY") ? false : consideredSymbolSet.add("EMPTY")) || areChanges;
					}
				}
			}
		}
	}
	
	public static void followSet() {
		ArrayList<String> emptySet = new ArrayList<String>();
		emptySet.add("EMPTY");
		
		
		for(String s : nonTerminalsList)
		{
			if(s.equals(start)) //follow(start-symbol) = $
			{
				ArrayList<String> tempSet = new ArrayList<String>();
				tempSet.add("$");
				followSet.put(s, tempSet);
			}
			else
			{
				followSet.put(s, new ArrayList<String>());
			}
		}
		
		//System.out.println("Starting follow set: " + followSet);
		
		boolean areChanges = true;
		while(areChanges)
		{
			areChanges = false;
			for(Rule r : rules)
			{
				//System.out.println("Current Rule: " + r);
				for(int i = 0; i < r.productions.size(); i++)
				{
					String s = r.productions.get(i);
					//System.out.println("\tCurrent Production: " + s);
					String[] productionItems = s.trim().split("\\s+");
					for(int q = 0; q < productionItems.length; q++)
					{
						if(nonTerminalsList.contains(productionItems[q]))
						{
							ArrayList<String> currentFollowSet = followSet.get(productionItems[q]);
							//System.out.println("\t\tCurrent Item: " + productionItems[q]);
							
							boolean isEmpty = false;
							
							if (q < productionItems.length - 1) {
								//System.out.println("\t\tItem following: " + productionItems[q + 1]);
								ArrayList<String> tempList = new ArrayList<String>();
								ArrayList<String> currentFirstSet = firstSet.get(productionItems[q + 1]);
								
								if (currentFirstSet == null)
									currentFirstSet = tokensFirstSet.get(productionItems[q + 1]);
								
								if (currentFirstSet != null) {
									tempList.addAll(currentFirstSet);
									if (tempList.contains("EMPTY")) {
										tempList.remove("EMPTY");
										//System.out.println("removing EMPTY");
										isEmpty = true;
									}
									
								}
								
								//System.out.println("\t\tTo be added to follow: " + tempList);
								
								for (String z : tempList) {
									if (!currentFollowSet.contains(z)) {
										currentFollowSet.add(z);
										areChanges = true;
									}
								}
							} else {
								isEmpty = true;
							}
							
							if (isEmpty) {
								ArrayList<String> firstFollowSet = followSet.get(r.symbol);
								
								//System.out.println("Got follow set for symbol[" + r.symbol + "]: " + firstFollowSet);
								
								for (String z : firstFollowSet) {
									if (!currentFollowSet.contains(z)) {
										currentFollowSet.add(z);
										areChanges = true;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static ArrayList<String> getFirstForAlpha(String alpha) {
		String[] productionItems = alpha.trim().split("\\s+");
		
		ArrayList<String> alphaFirstSet = new ArrayList<String>();
		
		for (String s : productionItems) {
			if (s.equals("EMPTY")) 
				continue;
			
			ArrayList<String> tempFirstSet;
			
			tempFirstSet = firstSet.get(s);
			
			if (tempFirstSet == null)
				tempFirstSet = tokensFirstSet.get(s);
			
			if (tempFirstSet != null) {
				alphaFirstSet.addAll(tempFirstSet);
				
				alphaFirstSet.remove("EMPTY");
				
				if (alphaFirstSet.size() > 0)
					return alphaFirstSet;
			}
		}
		
		alphaFirstSet.add("EMPTY");
		
		return alphaFirstSet;		
	}
	
	public static void computeParsingTable() {		
		for (Rule r: rules) {
			ArrayList<String> followSetTokens = followSet.get(r.symbol);
			Hashtable<String, String> currentRow = new Hashtable<String, String>();
			
			for (String production: r.productions) {
				
				
				//String[] productionItems = production.trim().split("\\s+");
				
				//System.out.println("Currently considering first set(" + r.symbol + ":" + production + "): " + firstSetComplete.get(r.symbol + ":" + production));
				ArrayList<String> firstSetTokens = firstSetComplete.get(r.symbol + ":" + production);
				//System.out.println("Currently considering firstSet for production (" + production + ")" + getFirstForAlpha(production));
				//ArrayList<String> firstSetTokens = getFirstForAlpha(production);
				
				for (String t : firstSetTokens) {
					//System.out.println("M[A,a] i.e. M[" + r.symbol + "," + t + "] = " + r.symbol + " : " + production);
					if (!currentRow.containsKey(t))
						currentRow.put(t, production);
				}
				
				if (firstSetTokens.contains("EMPTY")) {
					for (String f : followSetTokens) {
						currentRow.put(f, production);
					}
				}
				
				
			}
			parsingTable.put(r.symbol, currentRow);
		}
	}
}