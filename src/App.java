import org.fusesource.jansi.AnsiConsole;

/**
 * The Tranquil app
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 29, 2021
 */
public class App {
    private AppIO appIO;
    private String sourcePath;
    private String destinationPath;

    /**
     * Constructor to initialize the object variables with default values
     */
    public App() {
        this.appIO = AppIO.getInstance();
        AnsiConsole.systemInstall();
    }

    /**
     * Start the backup process
     */
    private void startBackup() {
        // Step 1 - Get the directories
        // --------------------------------------------------------------------------------
        this.sourcePath = this.appIO.getDirectory("Enter the directory path to be backed up");
        this.destinationPath = this.appIO.getDirectory("Enter the backup drive or location");
        // --------------------------------------------------------------------------------

        // Step 2 - Read files and folders at both the locations
        // --------------------------------------------------------------------------------
        FileRetrieve sourceList = new FileRetrieve(sourcePath);
        FileRetrieve destinationList = new FileRetrieve(destinationPath);
        // --------------------------------------------------------------------------------

        // Step 3 - Find the changes at the source, and add them to a queue
        // --------------------------------------------------------------------------------
        Delta delta = new Delta();
        delta.calculate(sourceList, destinationList);
        delta.printStats();
        // --------------------------------------------------------------------------------

        // Step 4 - ask confirmation and then copy items from the queue to destination
        // --------------------------------------------------------------------------------
        if (delta.backupRequired() == false) {
            this.appIO.printf("%n%n[%s] Backup not required. \"%s\" and \"%s\" are in sync.",
                    ColorText.text("NOTE", Color.BRIGHT_MAGENTA), sourcePath, destinationPath);

            return;
        }
        // --------------------------------------------------------------------------------

        // Step 5 - ask confirmation and then copy items from the queue to destination
        // --------------------------------------------------------------------------------
        this.appIO.printf("%n%n[%s] Start the backup now? ", ColorText.text("QUES", Color.BRIGHT_YELLOW));
        char ch = 's';

        while (!(ch == 'n' || ch == 'N')) {
            try {
                ch = this.appIO.readLine().charAt(0);

                if ((ch == 'y') || (ch == 'Y')) {
                    this.appIO.printf("%n%n[%s] Starting with backup process...%n%n",
                            ColorText.text("INFO", Color.BRIGHT_BLUE));
                    FileCopier fc = new FileCopier();

                    if (fc.fileCopy(sourcePath, destinationPath, delta.sourceNewFiles)) {
                        this.appIO.printf("%n%n[%s] Backup is successful%n%n",
                                ColorText.text("DONE", Color.BRIGHT_GREEN));
                        ch = 'n';
                    }
                }

                else {
                    this.appIO.printf("%n%n[%s] Backup aborted!%n%n", ColorText.text("NOTE", Color.BRIGHT_MAGENTA));
                    ch = 'n';
                }

            }

            catch (StringIndexOutOfBoundsException e) {
                this.appIO.printf("%n%n[%s] Please enter a some response. Start the backup now? ",
                        ColorText.text("INFO", Color.BRIGHT_BLUE));
            }
        }
        // --------------------------------------------------------------------------------
    }

    /**
     * Tranquil menu
     */
    public void appMenu() {
        // priming input ch
        int choice = 9;

        while (choice != 0) {
            this.appIO.clearConsole();

            this.appIO.printf("%n%nTranquil Backup Manager");
            this.appIO.printf("%n-----------------------");
            this.appIO.printf("%n%n[1] Start backing up");
            this.appIO.printf("%n%n[2] Configure directories to be excluded from backing up");
            this.appIO.printf("%n%n[0] Exit");

            try {
                this.appIO.printf("%n%n%n[%s] Please enter your choice: ", ColorText.text("QUES", Color.BRIGHT_YELLOW));
                choice = Integer.parseInt(this.appIO.readLine());

                switch (choice) {
                    // backup
                    case 1: {
                        this.startBackup();

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // skip menu
                    case 2: {
                        Skipper sk = Skipper.getInstance();
                        sk.configMenu();
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

    public static void main(String[] args) {
        App main = new App();
        main.appMenu();
    }
}
