import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to find the differences in the files and directories from two location
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 29, 2021
 */
public class Delta {
    private AppIO appIO;
    public Queue<File> sourceNewFiles;

    /**
     * Default constructor to initialize the object variables with default values
     */
    public Delta() {
        this.appIO = AppIO.getInstance();
        this.sourceNewFiles = new LinkedList<>();
    }

    /**
     * Method to compare the differences in the files/directories in the two
     * locations supplied as parameter, and store the actionable differences in the
     * object variable sourceNewFiles
     *
     * @param sourceFiles      an object of type FileRetrieve
     * @param destinationFiles an object of type FileRetrieve
     */
    public void calculate(FileRetrieve sourceFiles, FileRetrieve destinationFiles) {
        try {
            int i = 0, j = 0;
            FileDetails src = null, dst = null;
            this.appIO.printf("%n");

            // keep comparing until there exists files/directories in the source location
            while (i < sourceFiles.allFiles.size()) {

                // break the loop if there is nothing to compare with in the destination
                if (j == destinationFiles.allFiles.size()) {
                    break;
                }

                // fetch a file from sourceFiles
                src = new FileDetails(sourceFiles.allFiles.get(i), sourceFiles.basePath);

                // fetch a file from destinationFiles
                dst = new FileDetails(destinationFiles.allFiles.get(j), destinationFiles.basePath);

                // this.appIO.printf("%n%s compared with %s -> %d%n", src.fullPath,
                // dst.fullPath, src.compareTo(dst));

                // compare src and dst
                switch (src.compareTo(dst)) {
                    // file/directory missing from destination
                    case 100: {
                        this.sourceNewFiles.add(sourceFiles.allFiles.get(i));
                        this.appIO.printf("%n[%s] Item added to queue for creation - %s",
                                ColorText.text("INFO", Color.BRIGHT_BLUE), src.fullPath);
                        i++;
                    }
                        break;

                    // file/directory at destination is old copy
                    case 200: {
                        this.sourceNewFiles.add(sourceFiles.allFiles.get(i));
                        this.appIO.printf("%n[%s] Item added to queue for updation - %s",
                                ColorText.text("INFO", Color.BRIGHT_BLUE), src.fullPath);
                        i++;
                        j++;
                    }
                        break;

                    // file/directory synced
                    case 500: {
                        i++;
                        j++;
                    }
                        break;

                    // destination is newer copy (dont do anything)
                    case 800: {
                        i++;
                        j++;
                    }
                        break;

                    // extra file/directory present in the destination
                    case 900: {
                        j++;
                    }
                        break;
                }
            }

            // above loop will break when there's nothing to compare in destination
            while (i < sourceFiles.allFiles.size()) {
                this.sourceNewFiles.add(sourceFiles.allFiles.get(i));
                this.appIO.printf("%n[%s] Item added to queue for creation - %s",
                        ColorText.text("INFO", Color.BRIGHT_BLUE), sourceFiles.allFiles.get(i).getAbsolutePath());
                i++;
            }
        }

        catch (Exception e) {
            this.appIO.printf("%n%n[%s] An exception has occurred", ColorText.text("FAIL", Color.BRIGHT_RED));
            e.printStackTrace();
        }
    }

    /**
     * Method to print the statistics, how many items need to be backed up and their
     * cumulative size
     */
    public void printStats() {
        int queueSize = this.sourceNewFiles.size();
        this.appIO.printf("%n%n[%s] Total items in queue for backup = %d", ColorText.text("NOTE", Color.BRIGHT_MAGENTA),
                queueSize);

        long totalSize = 0;
        int count = 0;

        // determine total size of queue in bytes
        for (File aq : this.sourceNewFiles) {
            totalSize = totalSize + aq.length();
        }

        // convert size of queue in readable format
        double s = (double) totalSize;

        while (s / 1024 >= 1) {
            s = s / 1024;
            count++;
        }

        switch (count) {
            case 0:
                this.appIO.printf("%n[%s] Total size of the backup = %.2f %s%n",
                        ColorText.text("NOTE", Color.BRIGHT_MAGENTA), s, "B");
                break;
            case 1:
                this.appIO.printf("%n[%s] Total size of the backup = %.2f %s%n",
                        ColorText.text("NOTE", Color.BRIGHT_MAGENTA), s, "KB");
                break;
            case 2:
                this.appIO.printf("%n[%s] Total size of the backup = %.2f %s%n",
                        ColorText.text("NOTE", Color.BRIGHT_MAGENTA), s, "MB");
                break;
            case 3:
                this.appIO.printf("%n[%s] Total size of the backup = %.2f %s%n",
                        ColorText.text("NOTE", Color.BRIGHT_MAGENTA), s, "GB");
                break;
            case 4:
                this.appIO.printf("%n[%s] Total size of the backup = %.2f %s%n",
                        ColorText.text("NOTE", Color.BRIGHT_MAGENTA), s, "TB");
                break;
        }
    }

    /**
     * Check if there's any need for backup or everything is in sync
     *
     * @return <code>true</code> if backup is required, <code>false</code> otherwise
     */
    public boolean backupRequired() {
        if (this.sourceNewFiles.size() == 0) {
            return false;
        }

        else {
            return true;
        }
    }
}
