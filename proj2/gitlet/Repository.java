package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;
import static gitlet.GitletConstants.*;
import static gitlet.CommitsUtils.*;
/** Represents a gitlet repository.
 *  does at a high level.
 *  @author cyw
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */

    public static boolean isinitialized() {
        return GITLET_DIR.exists();
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            GITLET_DIR.mkdir();
            BLOBS.mkdir();
            COMMITS.mkdir();
            REFS.mkdir();
            REFS_HEAD.mkdir();

            Commit initialCommit = new Commit();
            String initialCommitID = saveCommit(initialCommit);
            File head = join(REFS_HEAD, "master");
            writeContents(head, initialCommitID);
            writeContents(HEAD, "master");
            try {
                STAGING_AREA.createNewFile();
                writeObject(STAGING_AREA, new StagingArea());
            } catch (IOException e) {
                System.out.println("Error");
            }
        }
    }

    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            String fileID = sha1(readContents(file));
            Commit currentCommit = getCurrentCommit();
            StagingArea index = readObject(STAGING_AREA, StagingArea.class);
            TreeMap<String, String> currentVersion = currentCommit.getFileSnapshots();
            TreeMap<String, String> currentAdded = index.getAddedFile();
            TreeSet<String> currentRemoved = index.getRemovedFile();
            if (currentRemoved.contains(fileName)) {
                currentRemoved.remove(fileName);
            }

            if (currentVersion.containsKey(fileName) && currentVersion.get(fileName).equals(fileID)) {
                if (currentAdded.containsKey(fileName)) {
                    index.removeFileFromAdded(fileName);
                }
            } else {
                index.addFileToAdded(fileName, fileID);
            }
            writeObject(STAGING_AREA, index);
        }
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (StageUtils.isemptyStage()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
        } else {
            Commit newCommit = new Commit();
            Commit currentCommit = getCurrentCommit();
            StagingArea index = readObject(STAGING_AREA, StagingArea.class);
            newCommit.setParent(getCurrentCommitID());
            newCommit.setMessage(message);
            newCommit.setTimestamp(new Date());

            // based on the current commit and the staging area generate the new commit
            newCommit.setFileSnapshots(mergeSnapshots(currentCommit, index));
            StageUtils.saveBlobs(index.getAddedFile()); // save the staged file to the blobs

            StageUtils.saveStage(); // clean and save the staging area
            setHEAD(saveCommit(newCommit));
        }
    }

    public static void rm(String fileName) {
        File file = join(CWD, fileName);
        Commit currentCommit = getCurrentCommit();
        StagingArea index = readObject(STAGING_AREA, StagingArea.class);

        TreeMap<String, String> indexAdded = index.getAddedFile();
        TreeMap<String, String> currentTracked = currentCommit.getFileSnapshots();

        if (indexAdded.containsKey(fileName)) {
            index.removeFileFromAdded(fileName);
            if (currentTracked.containsKey(fileName)) {
                index.addFileToRemoved(fileName);
                restrictedDelete(file);
            }
        } else if (isUntracked(fileName)) {
            System.out.println("No reason to remove the file.");
        } else {
            index.addFileToRemoved(fileName);
            restrictedDelete(file);
        }
        writeObject(STAGING_AREA, index);
    }

    public static void log() {
        Commit currentCommit = getCurrentCommit();

        while (currentCommit != null) {
            currentCommit.printCommitInfo();
            if (currentCommit.getParent() == null) {
                break;
            }
            currentCommit = getCommit(currentCommit.getParent());
         }

    }

    public static void checkout(String[] strings) {
        if (strings.length == 1) {
            String branchName = strings[0];
            File head = join(REFS_HEAD, branchName);
            String currentBranch = readContentsAsString(HEAD);
            if (!head.exists()) {
                System.out.println("No such branch exists.");
            } else if (currentBranch.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
            } else {
                Commit givenCommit = getCommit(readContentsAsString(head));
                Commit currentCommit = getCurrentCommit();
                TreeMap<String, String> currentFilesnapshots = currentCommit.getFileSnapshots();
                TreeMap<String, String> givenFilesnapshots = givenCommit.getFileSnapshots();

                TreeSet<String> fileLists = new TreeSet<>();
                fileLists.addAll(givenFilesnapshots.keySet());
                fileLists.addAll(currentFilesnapshots.keySet());

                for (String filename : fileLists) {
                    File file = join(CWD, filename);
                    boolean isConsistent = isConsistent(filename, currentCommit, givenCommit);
                    boolean currentHas = currentFilesnapshots.containsKey(filename);
                    boolean givenHas = givenFilesnapshots.containsKey(filename);
                    if (isUntracked(filename)) {
                        System.out.println(MERGE_MODIFY_UNTRACKED_WARNING);
                    }
                    if (!isConsistent) {
                        if (currentHas && givenHas) {
                            writeContents(file, readContents(join(BLOBS, givenFilesnapshots.get(filename))));
                        } else if (currentHas) {
                            restrictedDelete(file);
                        } else {
                            writeContents(file, readContents(join(BLOBS, givenFilesnapshots.get(filename))));
                        }
                    }
                }
                writeContents(HEAD, branchName);
                setHEAD(readContentsAsString(head));
                StageUtils.saveStage();
            }
        } else if (strings.length == 2) {
            String filename = strings[1];
            Commit currentCommit = getCurrentCommit();
            if (!currentCommit.getFileSnapshots().containsKey(filename)) {
                System.out.println("File does not exist in the current commit.");
            } else {
                File file = join(CWD, filename);
                TreeMap<String, String> currentFilesnapshots = currentCommit.getFileSnapshots();
                writeContents(file, readContents(join(BLOBS, currentFilesnapshots.get(filename))));
            }
        } else {
            if (!strings[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String commitID = strings[0];
            String fileName = strings[2];
            Commit commit = commitID.length() < 40 ? getCommitabbreviate(commitID) : getCommit(commitID);
            if (commit == null) {
                System.out.println("No commit with that id exists.");
            } else {
                TreeMap<String, String> commitFilesnapshots = commit.getFileSnapshots();
                if (commitFilesnapshots.containsKey(fileName)) {
                    File file = join(CWD, fileName);
                    writeContents(file, readContents(join(BLOBS, commitFilesnapshots.get(fileName))));
                } else {
                    System.out.println("File does not exist in that commit.");
                }
            }
        }
    }

    public static void globalLog() {
        List<String> commitsList = plainFilenamesIn(COMMITS);
        if (commitsList == null || commitsList.isEmpty()) {
            return;
        }
        for (String commitID : commitsList) {
            Commit commit = getCommit(commitID);
            commit.printCommitInfo();
        }
    }

    public static void find(String message) {
        List<String> commitsList = plainFilenamesIn(COMMITS);
        if (commitsList == null || commitsList.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        }
        int flag = 0;
        for (String commitID : commitsList) {
            Commit commit = getCommit(commitID);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitID);
                flag += 1;
            }
        }
        if (flag == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        Commit currentCommit = getCurrentCommit();
        TreeMap<String, String> currentTracked = currentCommit.getFileSnapshots();
        TreeMap<String, String> stagingAdded = StageUtils.getStage().getAddedFile();
        TreeSet<String> stagingRemoved = StageUtils.getStage().getRemovedFile();

        System.out.println("=== Branches ===");
        List<String> branchesList = plainFilenamesIn(REFS_HEAD);
        branchesList = branchesList.stream().sorted().collect(Collectors.toList());
        for (String branch : branchesList) {
            String head = readContentsAsString(HEAD);

            if (branch.equals(head)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName : stagingAdded.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String fileName : stagingRemoved) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> filesList = plainFilenamesIn(CWD);
        for (String fileName : filesList) {
            File file = join(CWD, fileName);
            String currentFileID = sha1(readContents(file));
            boolean currentHas = currentTracked.containsKey(fileName);
            boolean stagedHas = stagingAdded.containsKey(fileName);

            // if file is a directory
            if (file.isDirectory()) {
                continue;
            }

            if (stagedHas) {
                boolean isSamestaged = stagingAdded.get(fileName).equals(currentFileID);
                if (!isSamestaged) {
                    System.out.println(fileName + " (modified)");
                }
            } else {
                if (currentHas) {
                    boolean isSamecurrent = currentTracked.get(fileName).equals(currentFileID);
                    if (!isSamecurrent) {
                        System.out.println(fileName + " (modified)");
                    }
                }
            }
        }
        for (String fileName : stagingAdded.keySet()) { // get keys in StagingAdded
            if (!filesList.contains(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }

        for (String fileName : currentTracked.keySet()) {
            if (!filesList.contains(fileName) && !stagingRemoved.contains(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String fileName : filesList) {
            if (!currentTracked.containsKey(fileName) && !stagingAdded.containsKey(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void branch(String branchname) {
        if (join(REFS_HEAD, branchname).exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            File branchFile = join(REFS_HEAD, branchname);
            writeContents(branchFile, getCurrentCommitID());
        }
    }

    public static void rmBranch(String branchname) {
        if (!join(REFS_HEAD, branchname).exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchname.equals(readContentsAsString(HEAD))) {
            System.out.println("Cannot remove the current branch.");
        } else {
            join(REFS_HEAD, branchname).delete();
        }
    }

    public static void reset(String commitID) {
        if (!join(COMMITS, commitID).exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit givencommit = getCommit(commitID);
            Commit currentCommit = getCurrentCommit();
            TreeMap<String, String> givenSnapshots = givencommit.getFileSnapshots();
            TreeMap<String, String> currentSnapshots = currentCommit.getFileSnapshots();

            TreeSet<String> currentFiles = new TreeSet<>();
            currentFiles.addAll(currentSnapshots.keySet());
            currentFiles.addAll(givenSnapshots.keySet());

            for (String fileName : currentFiles) {
                if (!givenSnapshots.containsKey(fileName)) {
                    restrictedDelete(join(CWD, fileName));
                } else {
                    if (isUntracked(fileName)) {
                        System.out.println(MERGE_MODIFY_UNTRACKED_WARNING);
                        return;
                    }
                    String[] args = {commitID, "--", fileName};
                    checkout(args);
                }
            }
            StageUtils.saveStage();
            setHEAD(commitID);
        }
    }

    public static void merge(String givenbranch) {
        if (!join(REFS_HEAD, givenbranch).exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (givenbranch.equals(readContentsAsString(HEAD))) {
            System.out.println("Cannot merge a branch with itself.");
        } else if (!StageUtils.isemptyStage()) {
            System.out.println("You have uncommitted changes.");
        } else {
            Commit currentCommit = getCurrentCommit();
            Commit givenCommit = getCommit(readContentsAsString(join(REFS_HEAD, givenbranch)));
            if (isAncestor(currentCommit, givenCommit)) {
                String[] args = {givenbranch};
                checkout(args);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            if (isAncestor(givenCommit, currentCommit)) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return;
            }
            Commit splitPoint = getSplitPoint(currentCommit, givenCommit);
            TreeMap<String, String> commonSnapshots = splitPoint.getFileSnapshots();
            TreeMap<String, String> currentSnapshots = currentCommit.getFileSnapshots();
            TreeMap<String, String> givenSnapshots = givenCommit.getFileSnapshots();

            TreeSet<String> filesList = new TreeSet<>(currentSnapshots.keySet());
            filesList.addAll(givenSnapshots.keySet());
            filesList.addAll(commonSnapshots.keySet());
            for (String fileName : filesList) {
                Boolean splitgivenConsistent = isConsistent(fileName, splitPoint, givenCommit);
                Boolean splitcurrentConsistent = isConsistent(fileName, splitPoint, currentCommit);
                Boolean currentgivenConsistent = isConsistent(fileName, currentCommit, givenCommit);
                if (!splitgivenConsistent && splitcurrentConsistent) {
                    if (isUntracked(fileName)) {
                        System.out.println(MERGE_MODIFY_UNTRACKED_WARNING);
                        return;
                    }
                }
                if (!splitgivenConsistent && !splitcurrentConsistent && !currentgivenConsistent) {
                    if (isUntracked(fileName)) {
                        System.out.println(MERGE_MODIFY_UNTRACKED_WARNING);
                        return;
                    }
                }
            }
            int flag = 0;
            for (String fileName : filesList) {
                Boolean splitgivenConsistent = isConsistent(fileName, splitPoint, givenCommit);
                Boolean splitcurrentConsistent = isConsistent(fileName, splitPoint, currentCommit);
                Boolean currentgivenConsistent = isConsistent(fileName, currentCommit, givenCommit);
                if (!splitgivenConsistent && splitcurrentConsistent) {
                    if (!givenSnapshots.containsKey(fileName)) {
                        rm(fileName);
                    } else {
                        String branchHead = readContentsAsString(join(REFS_HEAD, givenbranch));
                        String[] args = {branchHead, "--", fileName};
                        checkout(args);
                        add(fileName);
                    }
                }
                if (!splitgivenConsistent && !splitcurrentConsistent && !currentgivenConsistent) {
                    File file = join(CWD, fileName);
                    String contentv1 = currentSnapshots.containsKey(fileName) ?
                            readContentsAsString(join(BLOBS, currentSnapshots.get(fileName))) : "";
                    String contentv2 = givenSnapshots.containsKey(fileName) ?
                            readContentsAsString(join(BLOBS, givenSnapshots.get(fileName))) : "";
                    String content = StageUtils.merge(contentv1, contentv2);
                    writeContents(file, content);
                    add(fileName);
                    flag = 1;
                }
            }
            commit("Merged " + givenbranch + " into " + readContentsAsString(HEAD) + ".");
            Commit mergeCommit = getCurrentCommit();
            mergeCommit.setSecondparent(getCommitID(givenCommit));
            setHEAD(saveCommit(mergeCommit));
            if (flag == 1) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }
}
