## Tranquil Backup
A simple yet functional command line differential backup utility for backing up of data to your external hard disks


### Features
+ Only copy the files that are newer or not present at the backup location, thus saving time and manual efforts
+ Preserve the metadata of each of the files and folders during backup


### Usage
1. Compile the java file and run the compiled class
    ```
    javac Tranquil/Tranquil.java Tranquil/RetrieveFileList.java Tranquil/FileDetails.java Tranquil/FindDelta.java Tranquil/FileCopier.java
    java Tranquil.Tranquil
    ```

1. Input the location of source directory and destination directory as absolute paths

1. Wrapper scripts for Linux and Windows are also provided

    For linux,
    ```
    chmod +x Tranquil.sh
    ./Tranquil.sh
    ```

    For windows,
    ```
    Tranquil.cmd
    ```

