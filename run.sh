#!/bin/bash

# clear the screen
clear

# compile the files
javac -cp ".:lib/*" -sourcepath src -d bin src/*.java

# run
java -cp ".;lib/*;bin" App
