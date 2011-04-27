#!/bin/bash
javac src/Scanner.java
javac -classpath .:src src/ParserGenerator.java
