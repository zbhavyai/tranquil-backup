package Tranquil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;

class RetrieveFileList
{
	ArrayList<File> allFiles;
	ArrayList<File> skipList;
	String basePath;
	long totalFileCount;
	long totalDirectoryCount;
	boolean successfulRetrieve;
	PrintWriter pw;
	PrintWriter pe;

	RetrieveFileList(String path)
	{
		pw = new PrintWriter(System.out, true);
		pe = new PrintWriter(System.err, true);

		this.allFiles = new ArrayList<>();

		//read the folder skip list
		this.skipList = new ArrayList<>();
		File skipFile = new File("skipList.txt");

		try
		{
			Scanner sc = new Scanner(skipFile);
			File skipName;

			while(sc.hasNext())
			{
				skipName = new File(sc.nextLine());

				if(skipName.exists() && skipName.isDirectory())
				{
					skipList.add(skipName.getAbsoluteFile());
				}
			}
		}

		catch(FileNotFoundException e)
		{
			pw.println("[INFO] No directories to be skipped");
		}


		File f = new File(path);
		this.basePath = f.getAbsolutePath();

		if(!f.exists())
		{
			pe.printf("[FAIL] Could not find path %s\n", f.getAbsolutePath());
			successfulRetrieve = false;
		}

		else
		{
			//adding this because directories like D:, G: are written as D:\, G:\,
			//while normal directories are written as D:\Normal, without \ at end
			if(f.isDirectory())
			{
				if(path.charAt(path.length()-1) != File.separatorChar)
					this.basePath = this.basePath + File.separator;
			}

			pw.printf("[INFO] Starting scanning of %s\n", f.getAbsolutePath());
			retrieve(path);
			successfulRetrieve = true;
			pw.printf("[PASS] %d objects scanned in %s\n\n", totalFileCount+totalDirectoryCount, f.getAbsolutePath());

			//sort the allFiles, so that order in both source and destination is same
			Collections.sort(allFiles);
		}
	}


	//retrieve the child files and directories, and store them in ArrayList
	private void retrieve(String filePath)
	{
		File currentFile = new File(filePath);
		File[] fileList = currentFile.listFiles();

		if(fileList == null)
		{
			pe.printf("[FAIL] Could not find or enlist directory %s\n", currentFile.getAbsolutePath());
			return;
		}

		for(File f : fileList)
		{
			//windows default skip list
			if((f.getName().contains("$RECYCLE.BIN")) || (f.getName().equals("System Volume Information")))
			{
				continue;
			}

			if(f.isFile())
			{
				allFiles.add(f);
				totalFileCount++;
			}

			else if(f.isDirectory())
			{
				boolean skipOrNot = false;

				for(int i=0; i<skipList.size(); i++)
				{
					if(skipList.get(i).getAbsolutePath().equals(f.getAbsolutePath()))
					{
						skipOrNot = true;
						break;
					}
				}


				if(skipOrNot == false)
				{
					allFiles.add(f);
					totalDirectoryCount++;
					retrieve(f.getAbsolutePath());
				}
			}
		}
	}
}

