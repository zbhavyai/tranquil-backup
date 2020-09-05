package Tranquil;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class FileDetails implements Comparable<FileDetails>
{
	String name;
	String path;
	String basePath;
	String fullPath;
	boolean isDirectory;
	long lastModified;
	Calendar lastModifiedHuman;
	boolean hidden;
	long fileSize;
	String humanSize;

	FileDetails(File f, String path)
	{
		this.name = f.getName();
		this.path = f.getPath();
		this.fullPath = f.getAbsolutePath();
		this.isDirectory = f.isDirectory();
		this.basePath = path;
		this.lastModified = f.lastModified();
		this.lastModifiedHuman = Calendar.getInstance();
		this.lastModifiedHuman.setTimeInMillis(this.lastModified);
		this.hidden = f.isHidden();
		this.fileSize = f.length();

		int count=0;
		double s = (double)this.fileSize;

		while(s/1024 >= 1)
		{
			s = s/1024;
			count++;
		}

		switch(count)
		{
			case 0: this.humanSize = String.format("%.2f %s",s, " B"); break;
			case 1: this.humanSize = String.format("%.2f %s",s, "KB"); break;
			case 2: this.humanSize = String.format("%.2f %s",s, "MB"); break;
			case 3: this.humanSize = String.format("%.2f %s",s, "GB"); break;
			case 4: this.humanSize = String.format("%.2f %s",s, "TB"); break;
		}

//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(this.lastModified);
//		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		System.out.println(String.format(formatter.format(calendar.getTime())));
//		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		System.out.println(String.format(formatter.format(this.lastModified.getTime())));
	}

	void printDetails()
	{
		PrintWriter pw = new PrintWriter(System.out, true);
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		pw.println("[INFO] Name: " + this.name);
		pw.println("[INFO] Last Modification: " + String.format(formatter.format(this.lastModifiedHuman.getTime())));
		pw.println("[INFO] Size: " + this.humanSize);
		pw.println("[INFO] Hidden: " + this.hidden);
		pw.println("------------------------------------------------------------");
	}


	/**
	 * Return Code List
	 * 100 = names don't match, source name is lower -> meaning missing from destination
	 * 200 = names match, source is newer -> destination is old copy
	 * 500 = names match, time match
	 * 800 = names match, destination is newer -> changes were directly made to destination
	 * 900 = names don't match, destination is lower -> some extra folders in destination
	 */
	@Override
	public int compareTo(FileDetails o)
	{
		String compareSrc = this.fullPath.substring(this.basePath.length());
		String compareDst = o.fullPath.substring(o.basePath.length());

		if(compareSrc.compareToIgnoreCase(compareDst) < 0)
		{
			return 100;
		}

		else if(compareSrc.compareToIgnoreCase(compareDst) > 0)
		{
			return 900;
		}

		else	//(compareSrc.equals(compareDst))
		{
			if((this.lastModified == o.lastModified) || (this.isDirectory))
			{
				return 500;
			}

			else if(this.lastModified > o.lastModified)
			{
				return 200;
			}

			else
			{
				return 800;
			}
		}
	}
}
