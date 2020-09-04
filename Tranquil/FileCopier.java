package Tranquil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Queue;
import java.util.regex.Matcher;

public class FileCopier
{
	File ff;
	PrintWriter pw;
	PrintWriter pe;
	
	FileCopier()
	{
		pw = new PrintWriter(System.out, true);
		pe = new PrintWriter(System.err, true);
	}
	
	boolean fileCopy(String source, String destination, Queue<File> delta)
	{
		//adding this because directories like D:, G: are written as D:\, G:\, 
		//while normal directories are written as D:\Normal, without \ at end
		//----------------------------------------------------------------------
		ff = new File(source);
		if(ff.isDirectory())
		{
			if(source.charAt(source.length()-1) != File.separatorChar)
				source = source + File.separator;
		}

		ff = new File(destination);
		if(ff.isDirectory())
		{
			if(destination.charAt(destination.length()-1) != File.separatorChar)
				destination = destination + File.separator;
		}
		//----------------------------------------------------------------------

		String temp;
		source = Matcher.quoteReplacement(source);
		destination = Matcher.quoteReplacement(destination);
		
		try
		{	
			while(!delta.isEmpty())
			{
				Path sp = Paths.get(delta.peek().getAbsolutePath());
				temp = delta.peek().getAbsolutePath().replaceFirst(source, destination);
				Path dp = Paths.get(temp);
				
				pw.printf("[INFO] Copying %s to %s\n", sp.toString(), dp.toString());
				Files.copy(sp, dp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				pw.printf("[PASS] Done\n\n");


				delta.poll();
			}
			
			return true;
		}
		
		catch(Exception e)
		{
			pe.printf("[FAIL] Exception during copying file from %s to %s\n", source, destination);
			e.printStackTrace();
			return false;
		}
	}
}
