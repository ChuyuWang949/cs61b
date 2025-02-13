package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    private static HashMap<String, String> addedFile;
    private static HashSet<String> removedFile;

    public StagingArea() {
        addedFile = new HashMap<>();
        removedFile = new HashSet<>();
    }

    public HashMap<String, String> getAddedFile() {
        return addedFile;
    }

    public HashSet<String> getRemovedFile() {
        return removedFile;
    }

    public void addFileToAdded(String fileName, String fileID) {
        if (addedFile == null) {
            addedFile = new HashMap<>();
        }
        addedFile.put(fileName, fileID);
        if (removedFile == null) {
            removedFile = new HashSet<>();
        } else {
            removedFile.remove(fileName);
        }
    }

    public void removeFileFromAdded(String fileName) {
        addedFile.remove(fileName);
    }

    public void addFileToRemoved(String fileName) {
        if (removedFile == null) {
            removedFile = new HashSet<>();
        }
        removedFile.add(fileName);
        if (addedFile == null) {
            addedFile = new HashMap<>();
        } else {
            addedFile.remove(fileName);
        }
    }

    public void clean() {
        addedFile = null; // maybe risky pay attention!
        removedFile = null;
    }

    public void saveStage() {
        File stageFile = join(Repository.STAGING_AREA, "stage");
        writeObject(stageFile, new StagingArea());
    }
}
