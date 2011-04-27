CS3240 Project

Team 5 - Chris Borja and Jeff Tougas

#################################################
#######	      Source Code Description	#########
#################################################

./src/Scanner.java		-- 	This is our lexer that converts a tiny program 
					into a newline-separated token list.
./src/Rule.java			-- 	This is a simple class representing a rule.  It
					also contains the logic for removing left 
					factoring and common prefix
./src/ParserGenerator.java	-- 	This is both the parser generator and parser 
					driver.  It calculates first and follow sets, 
					the parsing table, and optionally can parse an
					input file (containing appropriate tokens).

#################################################
########      Directory/File Layout	#########
#################################################

./README.txt				-- 	This file
./build.sh				--	Bash script that compiles the source code
./scanner.sh				--	Bash script that runs the lexer/scanner
./parser.sh				-- 	Bash script that runs the parser generator
./src/					-- 	Contains java source code
./tinyprograms/				--	Example programs in the tiny language.  Also
						contains token lists for the given tiny files
./grammars/				--	Example grammar files and generated parsetables
./output/				--	Complete output for generating a parsetable and
						also running the parser on a given tiny file

#################################################
########      Compilation Instructions	#########
#################################################

There is a bash script called "build.sh".  One can run it like so from the 
project base directory:

./build.sh

This will invoke javac on the various source files.  This is the equivalent of 
running:

cd src
javac Scanner.java
javac ParserGenerator.java


#################################################
########      Running Instructions	#########
#################################################

## Scanner
Run the bash script "scanner.sh":

./scanner.sh tinyprograms/complex.tm

This saves a file named "tinyprograms/complex.tm.tokens" with the list of 
tokens in it.

## ParserGenerator AND Parser together
Run the bash script "parser.sh":

./parser.sh grammars/tiny.txt tinyprograms/complex.tm.tokens 

## ParserGenerator by itself
Optionally you can only generate the parsing table for a given grammar by doing
the following:

./parser.sh grammars/tiny.txt

Both this command and the previous command (with token file included) will 
generate a parsing table csv at the file path "tiny.txt.parsetable.csv" which
can be opened with Microsoft Excel or something similar.
