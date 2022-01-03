import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class to retrieve all the files and folders that are eligible for backup, and
 * store them as an ArrayList in the RetrieveFileList object
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since June 2, 2021
 */
public class FileRetrieve {
    // appIO used for input/output operations
    private AppIO appIO;

    // Skipper object to check which directories are to be excluded
    private Skipper sk;

    // allFiles will be storing the complete list of files and directories scanned
    public ArrayList<File> allFiles;

    // basePath is used to keep track of the starting directory which user entered
    public String basePath;

    // used only for stat purposes
    private long fileCount;

    // used only for stat purposes
    private long directoryCount;

    /**
     * Default constructor to initialize object variables with default values
     */
    private FileRetrieve() {
        this.appIO = AppIO.getInstance();
        this.sk = Skipper.getInstance();
        this.allFiles = new ArrayList<>();
    }

    /**
     * One parameter constructor that retrieves all files and folders present in the
     * parameter basePath
     *
     * @param basePath The canonical representation of a directory in the file
     *                 system from where all files and folders are to be retrieved
     */
    public FileRetrieve(String basePath) {
        // call the no parameter constructor for initialization
        this();

        // storing basePath for future use by the class Delta
        this.basePath = basePath;

        this.appIO.printf("%n%n[%s] Scanning \"%s\"...", ColorText.text("INFO", Color.BRIGHT_BLUE),
                this.basePath);

        // the actual retrieval process
        this.retrieve(this.basePath);

        this.appIO.printf("%n[%s] %d files and %d directories scanned at \"%s\"",
                ColorText.text("PASS", Color.BRIGHT_GREEN), this.fileCount, this.directoryCount, this.basePath);

        // sort the allFiles, so that order in both source and destination is same
        // the File.listFiles() does not guarantee any order, therefore sorting is
        // required
        Collections.sort(allFiles);
    }

    /**
     * Method to loop through all the directories in the parameter filePath,
     * retrieve the child files and directories, and store them in ArrayList
     * allFiles
     *
     * @param filePath A directory in the file system from where the retrieval is
     *                 initiated
     */
    private void retrieve(String filePath) {
        File currentFile = new File(filePath);
        File[] fileList = currentFile.listFiles();

        // this might help in case of permission denials
        if (fileList == null) {
            this.appIO.printf("%n%n[%s] Could not find or enlist directory %s",
                    ColorText.text("FAIL", Color.BRIGHT_RED), filePath);
        }

        for (File f : fileList) {
            // windows default mandatory skip list
            if ((f.getName().contains("$RECYCLE.BIN")) || (f.getName().equals("System Volume Information"))) {
                continue;
            }

            if (f.isFile()) {
                allFiles.add(f);
                this.fileCount++;
            }

            else if (f.isDirectory()) {
                if (sk.isExcluded(this.appIO.fetchCanonical(f))) {
                    continue;
                }

                else {
                    allFiles.add(f);
                    this.directoryCount++;
                    retrieve(this.appIO.fetchCanonical(f));
                }
            }
        }
    }
}
