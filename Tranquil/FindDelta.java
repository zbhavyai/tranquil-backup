package Tranquil;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class FindDelta
{
	Queue<File> delta;
	PrintWriter pw;
	PrintWriter pe;
//	String osType;

	FindDelta()
	{
		pw = new PrintWriter(System.out, true);
		pe = new PrintWriter(System.err, true);
		delta = new LinkedList<>();
	}

	void findDelta(RetrieveFileList sourceFiles, RetrieveFileList destinationFiles)
	{
		try
		{
			int i=0, j=0;

			while(i<sourceFiles.allFiles.size())
			{
				if(j == destinationFiles.allFiles.size())
				{
					break;
				}

				FileDetails src = new FileDetails(sourceFiles.allFiles.get(i), sourceFiles.basePath);
				FileDetails dst = new FileDetails(destinationFiles.allFiles.get(j), destinationFiles.basePath);

				/**
				 * Return Code List
				 * 100 = names don't match, source name is lower -> meaning missing from destination
				 * 200 = names match, source is newer -> destination is old copy
				 * 500 = names match, time match
				 * 800 = names match, destination is newer -> changes were directly made to destination
				 * 900 = names don't match, destination is lower -> some extra folders in destination
				 */

//				pw.printf("\n%s compared with %s -> %d\n", src.fullPath, dst.fullPath, src.compareTo(dst));
				switch(src.compareTo(dst))
				{
					case 100:
						delta.add(sourceFiles.allFiles.get(i));
						pw.printf("[INFO] Item added to queue for creation - %s\n", src.fullPath);
						i++;
						break;

					case 200:
						delta.add(sourceFiles.allFiles.get(i));
						pw.printf("[INFO] Item added to queue for updation - %s \n", src.fullPath);
						i++; j++;
						break;

					case 500: i++; j++; break;
					case 800: i++; j++; break;
					case 900: j++; break;
				}
			}

			//loop will break when there's nothing to compare in destination
			while(i<sourceFiles.allFiles.size())
			{
				delta.add(sourceFiles.allFiles.get(i));
				pw.printf("[INFO] Item added to queue for creation - %s\n", sourceFiles.allFiles.get(i).getAbsolutePath());
				i++;
			}
		}

		catch(Exception e)
		{
			pw.println("[FAIL] An exception has occurred");
			e.printStackTrace();
		}
	}

	void analyzeQueue()
	{
		int queueSize=delta.size();
		pw.printf("\n\n[INFO] Total items in queue for backup = %d\n", queueSize);

		long totalSize=0;
		int count=0;


		//determine total size of queue in bytes
		for(File aq : this.delta)
		{
			totalSize = totalSize + aq.length();
		}

		//convert size of queue in readable format
		double s = (double)totalSize;

		while(s/1024 >= 1)
		{
			s = s/1024;
			count++;
		}

		switch(count)
		{
			case 0: pw.printf("[INFO] Total data to be backed up = %.2f %s\n",s, "B"); break;
			case 1: pw.printf("[INFO] Total data to be backed up = %.2f %s\n",s, "KB"); break;
			case 2: pw.printf("[INFO] Total data to be backed up = %.2f %s\n",s, "MB"); break;
			case 3: pw.printf("[INFO] Total data to be backed up = %.2f %s\n",s, "GB"); break;
			case 4: pw.printf("[INFO] Total data to be backed up = %.2f %s\n",s, "TB"); break;
		}
	}
}
