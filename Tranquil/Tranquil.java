package Tranquil;

import java.io.*;
import java.util.Scanner;

public class Tranquil
{
	public static void main(String[] args)
	{
		PrintWriter pw = new PrintWriter(System.out, true);
		PrintWriter pe = new PrintWriter(System.out, true);
		Scanner sc = new Scanner(System.in);
		
		String source;
		String destination;
		
		//Step 1 - Get the source and destination
		//----------------------------------------------------------------------
		pw.println("[INFO] Directory path to be backed up - ");
		source = sc.nextLine();
		
		pw.println("[INFO] Backup drive/location - ");
		destination = sc.nextLine();
		//----------------------------------------------------------------------

		//Step 2 - Read files and folders at both the locations
		//----------------------------------------------------------------------
		RetrieveFileList sourceList = new RetrieveFileList(source);
		if(!sourceList.successfulRetrieve)
		{
			return;
		}

		RetrieveFileList destinationList = new RetrieveFileList(destination);
		if(!destinationList.successfulRetrieve)
		{
			return;
		}
		//----------------------------------------------------------------------

		
		//Step 3 - Find the changes at the source, and add them to a queue
		//----------------------------------------------------------------------
		FindDelta fd = new FindDelta();
		fd.findDelta(sourceList, destinationList);
		//----------------------------------------------------------------------
		
		
		//Step 4 - copy items from the queue to destination
		//----------------------------------------------------------------------
		pw.println("\n\n");
		FileCopier fc = new FileCopier();

		if(fc.fileCopy(source, destination, fd.delta))
			System.out.println("\n[PASS] Backup is successful");
		//----------------------------------------------------------------------
	}
}


