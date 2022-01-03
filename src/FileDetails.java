import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class to fetch the details (metadata) of the file or directory and compare
 * this metadata with other files/directories
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 29, 2021
 */
public class FileDetails implements Comparable<FileDetails> {
    private AppIO appIO;
    private String name;
    private long lastModified;
    private Calendar lastModifiedHuman;
    private long fileSize;
    private String humanSize;
    public boolean isDirectory;

    // basePath is used to keep track of the starting directory which user entered
    public String basePath;

    // the canonical path of the file or directory
    public String fullPath;

    // path relative from the basePath, to be used for comparison
    public String relativePath;

    /**
     * Constructor to initialize the object variables with values derived the
     * arguments passed
     *
     * @param f        The file or directory whose metadata is to be fetched
     * @param basePath The basePath of the root folder from where retrieval had
     *                 started
     */
    public FileDetails(File f, String basePath) {
        this.appIO = AppIO.getInstance();

        // the name of the file or directory, which humans mostly deal with. eg.
        // 'file.txt' or 'Documents'
        this.name = f.getName();
        this.basePath = basePath;
        this.fullPath = this.appIO.fetchCanonical(f);
        this.relativePath = this.fullPath.substring(this.basePath.length());
        this.lastModified = f.lastModified();
        this.lastModifiedHuman = Calendar.getInstance();
        this.lastModifiedHuman.setTimeInMillis(this.lastModified);
        this.fileSize = f.length();
        this.humanSize = this.humanReadableSize(this.fileSize);
        this.isDirectory = f.isDirectory();
    }

    /**
     * Method to convert the number of bytes into a human readable size
     *
     * @param fileSize The length of the file in bytes
     * @return a String representing the size in human readable format
     */
    private String humanReadableSize(long fileSize) {
        String humanReadableSize = "";

        int count = 0;
        double s = (double) this.fileSize;

        while (s / 1024 >= 1) {
            s = s / 1024;
            count++;
        }

        switch (count) {
            case 0:
                this.humanSize = String.format("%.2f %s", s, " B");
                break;
            case 1:
                this.humanSize = String.format("%.2f %s", s, "KB");
                break;
            case 2:
                this.humanSize = String.format("%.2f %s", s, "MB");
                break;
            case 3:
                this.humanSize = String.format("%.2f %s", s, "GB");
                break;
            case 4:
                this.humanSize = String.format("%.2f %s", s, "TB");
                break;
        }

        return humanReadableSize;
    }

    /**
     * Method to print the some of the details of the file f passed to the
     * constructor
     */
    public void printDetails() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        this.appIO.printf("%n%nName: %s", this.name);
        this.appIO.printf("%nLocation: %s", this.fullPath);
        this.appIO.printf("%nLast Modification: %s", formatter.format(this.lastModifiedHuman.getTime()));
        this.appIO.printf("%nSize: %s", this.humanSize);
    }

    /**
     * Method to determine if the comparison of file name should ignore case or not
     * Windows and Mac have case-insensitive file naming, Unix and SunOS are
     * case-sensitive
     *
     * @return <code>false</code> for *nix and solaris, <code>true</code> for
     *         windows and macOS
     */
    private boolean ignoreCase() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("win") > -1)
            return true;

        else if (os.indexOf("mac") > -1)
            return true;

        else if (os.indexOf("sunos") > -1)
            return false;

        else // (os.indexOf("nix") > -1 || OS.indexOf("nux") > -1 || OS.indexOf("aix") > -1)
            return false;
    }

    /**
     * Method to compare this FileDetails object with another one passed as an
     * argument
     *
     * Comparison is done not just on the file name, eg. filename.txt, but from the
     * basePath eg. 'sourceBasePath/folder1/folder2/filename.txt' vs
     * 'destinationBasePath/folder1/folder2/filename.txt'. That's why we extract the
     * relative path from the basePath - though sourceBasePath and
     * destinationBasePath themselves are not included in comparison
     *
     * @param fd The FileDetails object to be compared with this object
     *
     * @return Any of the following integers, with meanings as below
     *         <ul>
     *         <li>100 = names don't match, source name is lower, means
     *         file/directory missing from destination</li>
     *
     *         <li>200 = names match, source is newer, means file/directory at
     *         destination is old copy</li>
     *
     *         <li>500 = names match, time match, means file/directory at
     *         destination is same as source</li>
     *
     *         <li>800 = names match, destination is newer, means changes were
     *         directly made to destination</li>
     *
     *         <li>900 = names don't match, destination is lower, means some extra
     *         files/directories present in destination</li>
     *         </ul>
     */
    @Override
    public int compareTo(FileDetails fd) {
        // declare strings for comparison
        String compareSrc;
        String compareDst;

        // make both comparison strings lowercase for case-insensitive comparison
        if (this.ignoreCase()) {
            compareSrc = this.relativePath.toLowerCase();
            compareDst = fd.relativePath.toLowerCase();
        }

        else {
            compareSrc = this.relativePath;
            compareDst = fd.relativePath;
        }

        // if source file/directory is missing from destination
        if (compareSrc.compareTo(compareDst) < 0) {
            return 100;
        }

        // if extra file/directory is present in destination
        else if (compareSrc.compareTo(compareDst) > 0) {
            return 900;
        }

        // if names are same, check for timestamps
        else {
            // no timestamp checking for directories
            if (this.isDirectory && fd.isDirectory) {
                return 500;
            }

            // if timestamp is same
            else if (this.lastModified == fd.lastModified) {
                return 500;
            }

            // if source is newer
            else if (this.lastModified > fd.lastModified) {
                return 200;
            }

            // if destination is newer
            else {
                return 800;
            }
        }
    }
}
