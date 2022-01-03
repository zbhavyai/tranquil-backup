import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Queue;
import java.util.regex.Matcher;

/**
 * Class to provide method that copy the queue of files from source to
 * destination
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 29, 2021
 */
public class FileCopier {
    private AppIO appIO;
    private boolean backupStatus;

    /**
     * Default constructor to initialize the object variables with default values
     */
    FileCopier() {
        this.appIO = AppIO.getInstance();
        this.backupStatus = true;
    }

    /**
     * Method to copy all the files in the delta from source to destination
     *
     * @param sourceRoot      the path to the root folder that needs to be backed up
     * @param destinationRoot the path to the root folder where backup needs to be
     *                        done
     * @param delta
     * @return
     */
    public boolean fileCopy(String sourceRoot, String destinationRoot, Queue<File> delta) {
        String temp;

        sourceRoot = Matcher.quoteReplacement(sourceRoot);
        destinationRoot = Matcher.quoteReplacement(destinationRoot);

        while (!delta.isEmpty()) {
            // get the path of file to be copied
            temp = this.appIO.fetchCanonical(delta.peek());
            Path sp = Paths.get(temp);

            // create the path of file where it is to be copied
            temp = temp.replaceFirst(sourceRoot, destinationRoot);
            Path dp = Paths.get(temp);

            this.appIO.printf("%n%n[%s] Copying \"%s\" to \"%s\"", ColorText.text("INFO", Color.BRIGHT_BLUE),
                    sp.toString(), dp.toString());

            // start the copying process
            try {
                Files.copy(sp, dp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                this.appIO.printf("%n[%s] Copied", ColorText.text("INFO", Color.BRIGHT_BLUE));
            }

            // in case the destination has same file, but with permissions issue
            // try deleting the file and then copy (destination permission will get removed)
            catch (AccessDeniedException e) {
                this.appIO.printf("%n[%s] Access denied while backing up of \"%s\" to \"%s\"",
                        ColorText.text("FAIL", Color.BRIGHT_RED), sp.toString(), dp.toString());

                // if its a file, remove it and try copy again
                // dont try this method if its a folder, as folder would have other files as
                // well
                if (sp.toFile().isFile()) {
                    this.appIO.printf("%n[%s] Trying to backup \"%s\" to \"%s\" again",
                            ColorText.text("INFO", Color.BRIGHT_BLUE), sp.toString(), dp.toString());

                    try {
                        dp.toFile().delete();
                        Files.copy(sp, dp, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                        this.appIO.printf("%n[%s] Copied", ColorText.text("INFO", Color.BRIGHT_BLUE));
                    }

                    // some other (possibly unknown) exception occurs, skip that file and move on
                    catch (IOException newE) {
                        this.appIO.printf("%n[%s] Could not backup \"%s\" to \"%s\"",
                                ColorText.text("FAIL", Color.BRIGHT_RED), sp.toString(), dp.toString());
                        this.appIO.printf("%n[%s] Moving ahead with next backup item",
                                ColorText.text("INFO", Color.BRIGHT_BLUE));
                        this.backupStatus = false;
                    }
                }
            }

            catch (IOException e) {
                this.appIO.printf("%n[%s] Exception during copying \"%s\" to \"%s\"",
                        ColorText.text("FAIL", Color.BRIGHT_RED), sp.toString(), dp.toString());
                e.printStackTrace();
                this.appIO.printf("%n[%s] Moving ahead with next backup item",
                        ColorText.text("INFO", Color.BRIGHT_BLUE));
                this.backupStatus = false;
            }

            delta.poll();
        }

        return this.backupStatus;
    }
}
