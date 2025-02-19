package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class StagingArea implements Serializable {
    private TreeMap<String, String> addedFile;
    private TreeSet<String> removedFile;

    public StagingArea() {
        addedFile = new TreeMap<>();
        removedFile = new TreeSet<>();
    }

    public TreeMap<String, String> getAddedFile() {
        return addedFile;
    }

    public TreeSet<String> getRemovedFile() {
        return removedFile;
    }

    public void addFileToAdded(String fileName, String fileID) {
        if (addedFile == null) {
            addedFile = new TreeMap<>();
        }
        addedFile.put(fileName, fileID);
        if (removedFile == null) {
            removedFile = new TreeSet<>();
        } else {
            removedFile.remove(fileName);
        }
    }

    public void removeFileFromAdded(String fileName) {
        addedFile.remove(fileName);
    }

    public void addFileToRemoved(String fileName) {
        if (removedFile == null) {
            removedFile = new TreeSet<>();
        }
        removedFile.add(fileName);
        if (addedFile == null) {
            addedFile = new TreeMap<>();
        } else {
            addedFile.remove(fileName);
        }
    }
}
