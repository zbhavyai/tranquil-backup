import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Excludes the directory from the backup process. The input should be a
 * directory present in the file system at the time of calling this function.
 *
 * If the program is run specifically on the directory which is supposed to be
 * excluded, then, such request to exclude is meaningless for that particular
 * backup process
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 30, 2021
 */
public class Skipper {
    private AppIO appIO;
    private File skipFile;
    private HashSet<String> skipList;

    /**
     * The instance variable containing the one and only object of Skipper
     */
    private static Skipper instanceVar = null;

    /**
     * Skipper follows Singleton design pattern
     *
     * @return the instance of this Skipper
     */
    public static Skipper getInstance() {
        if (instanceVar == null)
            instanceVar = new Skipper();

        return instanceVar;
    }

    /**
     * Private constructor to restrict instantiating by foreign functions
     */
    private Skipper() {
        this.appIO = AppIO.getInstance();

        // set the location of the skipFile
        String defaultPath = "skipList";
        this.skipFile = new File(defaultPath);

        // HashSet to store only unique directories to be skipped
        this.skipList = new HashSet<String>();

        // if the skipFile can be read, read it to store the skipList in the memory
        if (this.skipFile.isFile() && this.skipFile.canRead()) {
            this.read();
        }

        // if for permissions the skipFile cannot be read
        else if ((this.skipFile.isFile() == true) && (this.skipFile.canRead() == false)) {
            this.appIO.printf("%n%n[%s] \"%s\" file cannot be read", ColorText.text("FAIL", Color.BRIGHT_RED),
                    this.skipFile.getAbsolutePath());
        }

        // if the skipFile does not exist, create it
        else {
            try {
                this.skipFile.createNewFile();
            }

            catch (IOException | SecurityException e) {
                this.appIO.printf("%n%n[%s] \"%s\" file could not be created", ColorText.text("FAIL", Color.BRIGHT_RED),
                        this.skipFile.getAbsolutePath());
            }
        }
    }

    /**
     * Reads all the directories to be skipped from the file on the disk to the
     * memory
     */
    private boolean read() {
        boolean flag = false;

        try (DataInputStream din = new DataInputStream(new BufferedInputStream(new FileInputStream(this.skipFile)))) {
            // clean the HashSet<String> skipList
            this.skipList.clear();

            while (true) {
                try {
                    // add the Strings from disk to memory (from skipFile to skipList)
                    this.skipList.add(din.readUTF());
                }

                catch (EOFException e) {
                    // break to stop reading. This is the standard way of reading from binary files
                    break;
                }
            }
            flag = true;
        }

        catch (FileNotFoundException e) {
            this.appIO.printf("%n%n[%s] FileNotFoundException reported. %s.", ColorText.text("FAIL", Color.BRIGHT_RED),
                    e.getMessage());
        }

        catch (IOException e) {
            this.appIO.printf("%n%n[%s] IOException reported. %s.", ColorText.text("FAIL", Color.BRIGHT_RED),
                    e.getMessage());
        }

        return flag;
    }

    /**
     * Writes all the directories to be skipped from the memory to the file on the
     * disk
     */
    private boolean write() {
        boolean flag = false;

        try (DataOutputStream dout = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(this.skipFile)))) {
            Iterator<String> i = this.skipList.iterator();

            while (i.hasNext()) {
                dout.writeUTF(i.next());
            }

            dout.flush();
            flag = true;
        }

        catch (FileNotFoundException e) {
            this.appIO.printf("%n%n[%s] FileNotFoundException reported. %s.", ColorText.text("FAIL", Color.BRIGHT_RED),
                    e.getMessage());
        }

        catch (IOException e) {
            this.appIO.printf("%n%n[%s] IOException reported. %s.", ColorText.text("FAIL", Color.BRIGHT_RED),
                    e.getMessage());
        }

        return flag;
    }

    /**
     * Add the directory to be skipped in the memory (HashSet<String> skipList) and
     * then in the File skipFile
     *
     * @param directorySkip the String representing the path to the directory to be
     *                      skipped
     * @return {@code true} if directorySkip added successfully, {@code false}
     *         otherwise
     */
    private boolean add(String directorySkip) {
        File f = new File(directorySkip);

        if (f.isDirectory()) {
            try {
                this.skipList.add(f.getCanonicalPath());
            }

            catch (IOException | SecurityException e) {
                return false;
            }
        }

        else {
            return false;
        }

        return this.write();
    }

    /**
     * Remove the directory from the skipList, so it can part of next backup process
     *
     * @param str the String representing the path to the directory that is
     *            currently present in the skipList
     */
    private boolean remove(String str) {
        File f = new File(str);

        Iterator<String> i = this.skipList.iterator();

        while (i.hasNext()) {
            try {
                if (i.next().equals(f.getCanonicalPath())) {
                    i.remove();
                    break;
                }
            }

            catch (IOException | SecurityException e) {
                return false;
            }
        }

        return this.write();
    }

    /**
     * Prints all directories to be excluded from the backup process
     *
     * While printing the elements the skipList, the file skipFile needs not to be
     * accessed
     *
     * This method can be used for manual debugging
     */
    private void print() {
        Iterator<String> i = this.skipList.iterator();

        while (i.hasNext()) {
            this.appIO.printf("%n- \"%s\"", i.next());
        }
    }

    /**
     * Check if a directory is to be excluded from the backup process
     */
    public boolean isExcluded(String str) {
        File test = new File(str);

        try {
            if (this.skipList.contains(test.getCanonicalPath()))
                return true;

            else
                return false;
        }

        catch (IOException | SecurityException e) {
            return false;
        }
    }

    /**
     * Remove all the skip directories that no longer exist in the system
     */
    private boolean clearInvalid() {
        File f;
        Iterator<String> i = this.skipList.iterator();

        while (i.hasNext()) {
            f = new File(i.next());

            if (f.isDirectory())
                continue;

            else
                i.remove();
        }

        return this.write();
    }

    /**
     * Skipper's own menu to add/remove/etc directories from the skipList and
     * skipFile
     */
    public void configMenu() {
        // priming input ch
        int choice = 9;

        while (choice != 0) {
            this.appIO.clearConsole();

            this.appIO.printf("%n%nMenu for configuration of exclusion of directories");
            this.appIO.printf("%n--------------------------------------------------");
            this.appIO.printf("%n%n[1] Add a directory to be excluded from the back-up");
            this.appIO.printf("%n%n[2] Remove a directory from the exclusion list");
            this.appIO.printf("%n%n[3] Print the exclusion list");
            this.appIO.printf("%n%n[4] Auto clear invalid entries from the exclusion list");
            this.appIO.printf("%n%n[5] Reload the skip configuration");
            this.appIO.printf("%n%n[6] Save the skip configuration");
            this.appIO.printf("%n%n[0] Return to the previous the menu");

            try {
                this.appIO.printf("%n%n%n[%s] Please enter your choice: ", ColorText.text("QUES", Color.BRIGHT_YELLOW));
                choice = Integer.parseInt(this.appIO.readLine());

                switch (choice) {
                    // add
                    case 1: {
                        String temp = this.appIO
                                .getDirectory("Enter the absolute path of the directory to be excluded");

                        if (this.add(temp)) {
                            this.appIO.printf("%n%n[%s] \"%s\" added successfully",
                                    ColorText.text("DONE", Color.BRIGHT_GREEN), temp);
                        }

                        else {
                            this.appIO.printf("%n%n[%s] Unable to add \"%s\". Please try again later.",
                                    ColorText.text("FAIL", Color.BRIGHT_RED), temp);
                        }

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // remove
                    case 2: {
                        String temp = this.appIO
                                .getDirectory("Enter the directory to be removed from the exclusion list");

                        if (this.remove(temp)) {
                            this.appIO.printf("%n%n[%s] \"%s\" removed successfully",
                                    ColorText.text("DONE", Color.BRIGHT_GREEN), temp);
                        }

                        else {
                            this.appIO.printf("%n%n[%s] Unable to remove \"%s\". Please try again later.",
                                    ColorText.text("FAIL", Color.BRIGHT_RED), temp);
                        }

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // print
                    case 3: {
                        this.appIO.printf("%n%nBelow directories are being excluded from the backup process -");
                        this.print();

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // clear invalid
                    case 4: {
                        this.appIO.printf("%n%n[%s] Clearing invalid entries",
                                ColorText.text("INFO", Color.BRIGHT_BLUE));

                        if (this.clearInvalid())
                            this.appIO.printf("%n%n[%s] Invalid entries cleared",
                                    ColorText.text("DONE", Color.BRIGHT_GREEN));

                        else
                            this.appIO.printf("%n%n[%s] Unable to clear invalid entries. Please try again later.",
                                    ColorText.text("FAIL", Color.BRIGHT_RED));

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // reload
                    case 5: {
                        this.appIO.printf("%n%n[%s] Reloading skip configuration",
                                ColorText.text("INFO", Color.BRIGHT_BLUE));

                        if (this.read())
                            this.appIO.printf("%n%n[%s] Skip configuration reloaded",
                                    ColorText.text("DONE", Color.BRIGHT_GREEN));

                        else
                            this.appIO.printf("%n%n[%s] Unable to read skip configuration. Please try again later.",
                                    ColorText.text("FAIL", Color.BRIGHT_RED));

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // save
                    case 6: {
                        this.appIO.printf("%n%n[%s] Saving the skip configuration",
                                ColorText.text("INFO", Color.BRIGHT_BLUE));

                        if (this.write())
                            this.appIO.printf("%n%n[%s] Skip configuration saved",
                                    ColorText.text("DONE", Color.BRIGHT_GREEN));
                        else
                            this.appIO.printf("%n%n[%s] Unable to save the skip configuration. Please try again later.",
                                    ColorText.text("FAIL", Color.BRIGHT_RED));

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // exit by changing choice
                    case 0: {
                        choice = 0;
                    }
                        break;

                    default: {
                        throw new NumberFormatException();
                    }
                }
            }

            catch (NumberFormatException e) {
                this.appIO.printf("%n%n[%s] Please enter a valid choice", ColorText.text("FAIL", Color.BRIGHT_RED));
                this.appIO.readLine("%n%n%nPress enter to return to the menu ");
            }

            catch (java.util.NoSuchElementException e) {
                this.appIO.printf("%n%n[%s] Input stream has been closed. Bye.",
                        ColorText.text("FAIL", Color.BRIGHT_RED));
                Runtime.getRuntime().exit(0);
            }
        }
    }
}
