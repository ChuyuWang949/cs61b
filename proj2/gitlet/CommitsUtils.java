package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.GitletConstants.*;
public class CommitsUtils {
    public static String saveCommit(Commit commit) {
        String commitID = getCommitID(commit);
        File commitFile = join(COMMITS, commitID);
        writeObject(commitFile, commit);
        return commitID;
    }

    public static Commit getCommit(String commitID) {
        if (!join(COMMITS, commitID).exists()) {
            return null;
        }
        File commitFile = join(COMMITS, commitID);
        return readObject(commitFile, Commit.class);
    }

    public static Commit getCommitabbreviate(String commitID) {
        for (String filename : plainFilenamesIn(COMMITS)) {
            if (filename.startsWith(commitID)) {
                return getCommit(filename);
            }
        }
        return null;
    }

    public static Commit getCurrentCommit() {
        return readObject(join(COMMITS, getCurrentCommitID()), Commit.class);
    }

    public static String getCurrentCommitID() {
        String head = readContentsAsString(HEAD);
        File pointer = join(REFS_HEAD, head);
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
        for (String fileName : index.getRemovedFile()) {
            mergedSnapshots.remove(fileName);
        }
        return mergedSnapshots;
    }

    public static void setHEAD(String commitID) {
        String head = readContentsAsString(HEAD);
        File pointer = join(REFS_HEAD, head);
        writeContents(pointer, commitID);
    }

    public static Commit getSplitPoint(Commit currentCommit, Commit givenCommit) {
        TreeSet<String> ancestors1 = commitAncestors(currentCommit);
        TreeSet<String> ancestors2 = commitAncestors(givenCommit);
        TreeSet<String> commonancestors = new TreeSet<>();

        for (String commitID : ancestors1) {
            if (ancestors2.contains(commitID)) {
                commonancestors.add(commitID);
            }
        }

        TreeMap<String, Integer> commonancestorsInDegree = inDegree(commonancestors);
        for (String commitID : commonancestorsInDegree.keySet()) {
            if (commonancestorsInDegree.get(commitID) == 0) {
                return getCommit(commitID);
            }
        }
        return null;
    }

    public static TreeMap<String, Integer> inDegree(TreeSet<String> commitIDs) {
        TreeMap<String, Integer> inDegree = new TreeMap<>();
        for (String commitID : commitIDs) {
            inDegree.put(commitID, 0);
        }
        for (String commitID : commitIDs) {
            Commit commit = getCommit(commitID);
            assert commit != null;
            if (commit.getParent() != null) {
                inDegree.put(commit.getParent(), inDegree.get(commit.getParent()) + 1);
            }
            if (commit.getSecondparent() != null) {
                inDegree.put(commit.getSecondparent(), inDegree.get(commit.getSecondparent()) + 1);
            }
        }
        return inDegree;
    }

    public static boolean isConsistent(String filename, Commit currentCommit, Commit givenCommit) {
        assert currentCommit != null && givenCommit != null && filename != null;
        TreeMap<String, String> currentSnapshots = currentCommit.getFileSnapshots();
        TreeMap<String, String> givenSnapshots = givenCommit.getFileSnapshots();
        if (currentSnapshots.containsKey(filename) && givenSnapshots.containsKey(filename)) {
            if (currentSnapshots.get(filename).equals(givenSnapshots.get(filename))) {
                return true;
            } else {
                return false;
            }
        } else if (!currentSnapshots.containsKey(filename) && !givenSnapshots.containsKey(filename)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAncestor(Commit fatherCommit, Commit childCommit) {
        String fatherID = getCommitID(fatherCommit);
        TreeSet<String> ancestors = commitAncestors(childCommit);
        return ancestors.contains(fatherID);
    }

    public static TreeSet<String> commitAncestors(Commit commit) {
        TreeSet<String> ancestors = new TreeSet<>();
        String parentID = commit.getParent();
        String secondparentID = commit.getSecondparent();
        if (parentID != null) {
            ancestors.add(parentID);
            ancestors.addAll(commitAncestors(getCommit(parentID)));
        }
        if (secondparentID != null) {
            ancestors.add(secondparentID);
            ancestors.addAll(commitAncestors(getCommit(secondparentID)));
        }
        return ancestors;
    }

    public static boolean isUntracked(String filename) {
        assert filename != null;
        List<String> files = plainFilenamesIn(CWD);
        Commit currentCommit = getCurrentCommit();
        assert currentCommit != null && files != null;
        return files.contains(filename) && !currentCommit.getFileSnapshots().containsKey(filename);
    }
}
