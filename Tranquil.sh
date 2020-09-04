#!/bin/bash


#compile the files at the same time
javac Tranquil/Tranquil.java Tranquil/RetrieveFileList.java Tranquil/FileDetails.java Tranquil/FindDelta.java Tranquil/FileCopier.java

#run the package.class
java Tranquil.Tranquil

