package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

import static gitlet.Utils.*;

public class CommitsUtils {
    public static String saveCommit(Commit commit) {
        String commitID = getCommitID(commit);
        File commitFile = join(Repository.commits, commitID);
        writeObject(commitFile, commit);
        return commitID;
    }

    public static Commit getCommit(String commitID) {
        if (!join(Repository.commits, commitID).exists()) {
            return null;
        }
        File commitFile = join(Repository.commits, commitID);
        return readObject(commitFile, Commit.class);
    }

    public static Commit getCurrentCommit() {
        return readObject(join(Repository.commits, getCurrentCommitID()), Commit.class);
    }

    public static String getCurrentCommitID() {
        String head = readContentsAsString(Repository.HEAD);
        File pointer = join(Repository.refs_heads, head);
        return readContentsAsString(pointer);
    }

    public static String getCommitID(Commit commit) {
        return sha1((Object) serialize(commit));
    }

    public static TreeMap<String, String> mergeSnapshots(Commit currentCommit, StagingArea index) {
        TreeMap<String, String> mergedSnapshots = new TreeMap<>();
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
        File pointer = join(Repository.refs_heads, head);
        writeContents(pointer, commitID);
    }
}
