import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class Rule {
	public String symbol;
	public ArrayList<String> productions;
	
	public Rule() {
		
	}
	
	public Rule(String ruleAsString) {
		String[] leftAndRight = ruleAsString.trim().split(":");
		
		symbol = leftAndRight[0].trim();
		
		String[] roughProductions = leftAndRight[1].split("\\|");
		
		productions = new ArrayList<String>();
		
		for (int i = 0; i < roughProductions.length; i++) {
			productions.add(roughProductions[i].trim());
		}
	}
	
	public List<Rule> eliminateLeftRecursion() {		
		List<Rule> rules = new ArrayList<Rule>();
		
		Rule first = new Rule();
		first.symbol = this.symbol;
		first.productions = new ArrayList<String>();
		
		Rule tail = new Rule();
		tail.symbol = this.symbol.replaceFirst(">", "-tail>");
		tail.productions = new ArrayList<String>();
		
		for (String p : productions) {
			String[] tokens = p.split("\\s+");
			if (symbol.equals(tokens[0])) {
				String production_tail = p.replaceFirst(tokens[0] + "\\s+", "");
				tail.productions.add(production_tail + " " + tail.symbol);
			} else {
				first.productions.add((!p.equals("EMPTY") ? p + " " : "") + tail.symbol);
			}
		}
		
		tail.productions.add("EMPTY");
		
		//System.out.println("First: " + first);
		//System.out.println("Tail: " + tail);
		
		if (first.productions.size() > 0 && tail.productions.size() > 1) {
			rules.add(first);
			rules.add(tail);
		} else {
			rules.add(this);
		}
		
		return rules;
	}
	
	public List<Rule> eliminateCommonPrefix() {		
		List<Rule> rules = new ArrayList<Rule>();
		
		Rule first = new Rule();
		first.symbol = this.symbol;
		first.productions = new ArrayList<String>();
		
		HashMap<String, Rule> commonPrefixes = new HashMap<String, Rule>();
		
		for (String current : productions) {
			Boolean noDuplicates = true;
			
			String[] currentTokens = current.split("\\s+");
			if (commonPrefixes.containsKey(currentTokens[0])) continue;
						
			for (String match : productions) {
				if (current != match) {
					String[] matchTokens = match.split("\\s+");
					
					if (matchTokens[0].equals(currentTokens[0])) {
						noDuplicates = false;
						Rule tail;
						
						if (!commonPrefixes.containsKey(currentTokens[0])) {
							tail = new Rule();
							tail.symbol = this.symbol.replaceFirst(">", "-tail-" + currentTokens[0] + ">");
							tail.productions = new ArrayList<String>();
							
							String production_tail = current.replaceFirst(currentTokens[0] + "\\s+", "");
							if (production_tail.equals(currentTokens[0])) {
								production_tail = "EMPTY";
							}
							tail.productions.add(production_tail);
							commonPrefixes.put(currentTokens[0], tail);
							
							first.productions.add(currentTokens[0] + " " + tail.symbol);
							
						} else {
							tail = commonPrefixes.get(currentTokens[0]);
						}
												
						//System.out.println(matchTokens[0] + " == " + currentTokens[0]);
						String production_tail = match.replaceFirst(currentTokens[0] + "\\s+", "");
						
						if (production_tail.equals(currentTokens[0])) {
							production_tail = "EMPTY";
						}
						
						tail.productions.add(production_tail);
					}
				}
				
			}
			
			if (noDuplicates) {
				first.productions.add(current);
			}
		}
		
		rules.add(first);
		rules.addAll(commonPrefixes.values());
		
		return rules;
	}
	
	@Override public String toString() {
		return "Symbol: " + symbol + ", productions: (" + productions + ")";
	}
}
