package Tranquil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.nio.file.AccessDeniedException;
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

		while(!delta.isEmpty())
		{
			Path sp = Paths.get(delta.peek().getAbsolutePath());
			temp = delta.peek().getAbsolutePath().replaceFirst(source, destination);
			Path dp = Paths.get(temp);

			pw.printf("[INFO] Copying %s to %s\n", sp.toString(), dp.toString());

			try
			{
				Files.copy(sp, dp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				pw.printf("[PASS] Done\n\n");
			}

			catch(AccessDeniedException e)
			{
				pe.printf("[FAIL] Access denied while backing up of %s to %s\n", sp.toString(), dp.toString());

				//dont try again if its a folder, as folder would have other files as well
				//if its a file, remove it and try copy again
				if(sp.toFile().isFile())
				{
					pw.printf("[INFO] Trying to backup %s to %s again\n", sp.toString(), dp.toString());

					try
					{
						dp.toFile().delete();
						Files.copy(sp, dp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
						pw.printf("[PASS] Done\n\n");
					}

					catch(IOException newE)
					{
						pe.printf("[FAIL] Could not backup %s to %s\n", sp.toString(), dp.toString());
						pw.printf("[INFO] Moving ahead with next backup item\n");
					}
				}
			}

			catch(IOException e)
			{
				pe.printf("[FAIL] Exception during copying %s to %s\n", sp.toString(), dp.toString());
				e.printStackTrace();
				pw.printf("[INFO] Moving ahead with next backup item\n");
			}

			delta.poll();
		}

		return true;
	}
}
