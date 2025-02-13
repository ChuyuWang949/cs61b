package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

public class CommitsUtils {
    public static String saveCommit(Commit commit) {
        String commitID = sha1(serialize(commit));
        File commitFile = join(Repository.commits, commitID);
        writeObject(commitFile, commit);
        return commitID;
    }

    public static Commit getCommit(String commitID) {
        File commitFile = join(Repository.commits, commitID);
        return readObject(commitFile, Commit.class);
    }

    public static Commit getCurrentCommit() {
        String head = readContentsAsString(Repository.HEAD);
        File pointer = join(Repository.CWD, head);
        String commitID = readContentsAsString(pointer);
        return readObject(join(Repository.commits, commitID), Commit.class);
    }

    public static String getCurrentCommitID() {
        String head = readContentsAsString(Repository.HEAD);
        File pointer = join(Repository.CWD, head);
        return readContentsAsString(pointer);
    }

    public static HashMap<String, String> mergeSnapshots(Commit currentCommit, StagingArea index) {
        HashMap<String, String> mergedSnapshots = new HashMap<>();
        if (currentCommit != null) {
            mergedSnapshots.putAll(currentCommit.getFileSnapshots());
        }
        mergedSnapshots.putAll(index.getAddedFile());
        for(String fileName : index.getRemovedFile()) {
            mergedSnapshots.remove(fileName);
        }
        return mergedSnapshots;
    }

    public static void setHEAD(String commitID) {
        String head = readContentsAsString(Repository.HEAD);
        File pointer = join(Repository.CWD, head);
        writeContents(pointer, commitID);
    }
}
