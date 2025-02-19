package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

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
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File STAGING_AREA = join(GITLET_DIR, "staging_area");
    public static final File BLOBS = join(GITLET_DIR, "blobs");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File REFS_HEAD = join(REFS, "heads");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

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
            String initialCommitID = CommitsUtils.saveCommit(initialCommit);
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
            Commit currentCommit = CommitsUtils.getCurrentCommit();
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
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            StagingArea index = readObject(STAGING_AREA, StagingArea.class);
            newCommit.setParent(CommitsUtils.getCurrentCommitID());
            newCommit.setMessage(message);
            newCommit.setTimestamp(new Date());

            // based on the current commit and the staging area generate the new commit
            newCommit.setFileSnapshots(CommitsUtils.mergeSnapshots(currentCommit, index));
            StageUtils.saveBlobs(index.getAddedFile()); // save the staged file to the blobs

            StageUtils.saveStage(); // clean and save the staging area

            CommitsUtils.setHEAD(CommitsUtils.saveCommit(newCommit)); // save the new commit and update the HEAD
        }
    }

    public static void rm(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("No reason to remove the untracked file.");
            System.exit(0);
        } else {
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            StagingArea index = readObject(STAGING_AREA, StagingArea.class);

            TreeMap<String, String> indexAdded = index.getAddedFile();
            TreeMap<String, String> currentTracked = currentCommit.getFileSnapshots();

            if (indexAdded.containsKey(fileName)) { // if the file is staged for addition
                index.removeFileFromAdded(fileName);
                if (currentTracked.containsKey(fileName)) {
                    index.addFileToRemoved(fileName);
                    restrictedDelete(file);
                }
            } else if (CommitsUtils.isUntracked(fileName)) {
                System.out.println("No reason to remove the file.");
            } else {
                index.addFileToRemoved(fileName);
                restrictedDelete(file);
            }
            writeObject(STAGING_AREA, index); // dont forget to save the staging area
        }
    }

    public static void log() {
        Commit currentCommit = CommitsUtils.getCurrentCommit();
        if (currentCommit.getSecondparent() != null) {

        } else {
            while (currentCommit != null) { // walk through the commit history
                currentCommit.printCommitInfo();
                if (currentCommit.getParent() == null) {
                    break;
                }
                currentCommit = CommitsUtils.getCommit(currentCommit.getParent());
             }
        }
    }

    public static void checkout(String[] strings) {
        if (strings.length == 1) {
            String branchName = strings[0];
            File head = join(REFS_HEAD, branchName);
            String currentBranch = readContentsAsString(Repository.HEAD);
            if (!head.exists()) {
                System.out.println("No such branch exists.");
            } else if (currentBranch == branchName) {
                System.out.println("No need to checkout the current branch.");
            } else {
                Commit givenCommit = CommitsUtils.getCommit(readContentsAsString(head));
                Commit currentCommit = CommitsUtils.getCurrentCommit();
                TreeMap<String, String> currentFilesnapshots = currentCommit.getFileSnapshots();
                TreeMap<String, String> givenFilesnapshots = givenCommit.getFileSnapshots();

                for (String filename : givenFilesnapshots.keySet()) {
                    File file = join(CWD, filename);
                    boolean isConsistent = CommitsUtils.isConsistent(filename, currentCommit, givenCommit);
                    if (CommitsUtils.isUntracked(filename)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                    if (currentFilesnapshots.containsKey(filename) && !isConsistent) {
                        writeContents(file, readContents(join(BLOBS, givenFilesnapshots.get(filename))));
                    } else if (!currentFilesnapshots.containsKey(filename)) {
                        restrictedDelete(file);
                    }
                }

                CommitsUtils.setHEAD(readContentsAsString(head));
                writeContents(Repository.HEAD, branchName);
                StageUtils.saveStage();
            }
        } else if (strings.length == 2) {
            String filename = strings[1];
            if (!CommitsUtils.getCurrentCommit().getFileSnapshots().containsKey(filename)) {
                System.out.println("File does not exist in the current commit.");
            } else {
                File file = join(CWD, filename);
                TreeMap<String, String> currentFilesnapshots = CommitsUtils.getCurrentCommit().getFileSnapshots();
                writeContents(file, readContents(join(BLOBS, currentFilesnapshots.get(filename))));
            }
        } else {
            if (strings[1] != "--") {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String commitID = strings[0];
            String fileName = strings[2];
            File commitFile = join(COMMITS, commitID);
            if (!commitFile.exists()) {
                System.out.println("No commit with that id exists.");
            } else {
                Commit commit = CommitsUtils.getCommit(commitID);
                if (commit.getFileSnapshots().containsKey(fileName)) {
                    File file = join(CWD, fileName);
                    writeContents(file, readContents(join(BLOBS, commit.getFileSnapshots().get(fileName))));
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
            Commit commit = CommitsUtils.getCommit(commitID);
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
            Commit commit = CommitsUtils.getCommit(commitID);
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
        Commit currentCommit = CommitsUtils.getCurrentCommit();
        TreeMap<String, String> currentTracked = currentCommit.getFileSnapshots();
        TreeMap<String, String> stagingAdded = StageUtils.getStage().getAddedFile();
        TreeSet<String> stagingRemoved = StageUtils.getStage().getRemovedFile();

        System.out.println("=== Branches ===");
        List<String> branchesList = plainFilenamesIn(REFS_HEAD);
        branchesList = branchesList.stream().sorted().collect(Collectors.toList());
        for (String branch : branchesList) {
            String head = readContentsAsString(Repository.HEAD);

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
            // if file is a directory
            if (file.isDirectory()) {
                continue;
            }
            String currentFileID = sha1(readContents(file));
            if (stagingAdded.containsKey(fileName)) {
                if (!stagingAdded.get(fileName).equals(currentFileID)) {
                    System.out.println(fileName + " (modified)");
                }
            } else {
                if (currentTracked.containsKey(fileName) && !currentTracked.get(fileName).equals(currentFileID)) {
                    System.out.println(fileName + " (modified)");
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
            writeContents(branchFile, CommitsUtils.getCurrentCommitID());
        }
    }

    public static void rmBranch(String branchname) {
        if (!join(REFS_HEAD, branchname).exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchname.equals(readContentsAsString(Repository.HEAD))) {
            System.out.println("Cannot remove the current branch.");
        } else {
            join(REFS_HEAD, branchname).delete();
        }
    }

    public static void reset(String commitID) {
        if (!join(COMMITS, commitID).exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit commit = CommitsUtils.getCommit(commitID);
            for (String fileName : plainFilenamesIn(CWD)) {
                if (!commit.getFileSnapshots().containsKey(fileName)) {
                    File file = join(CWD, fileName);
                    file.delete();
                } else {
                    String[] args = {commitID, "--", fileName};
                    checkout(args);
                }
            }
            StageUtils.saveStage();
            CommitsUtils.setHEAD(commitID);
        }
    }

    public static void merge(String givenbranch) {
        if (!join(REFS_HEAD, givenbranch).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (givenbranch.equals(readContentsAsString(Repository.HEAD))) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        } else if (!StageUtils.isemptyStage()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else {
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            Commit givenCommit = CommitsUtils.getCommit(readContentsAsString(join(REFS_HEAD, givenbranch)));

            if (CommitsUtils.isAncestor(currentCommit, givenCommit)) {
                String[] args = {givenbranch};
                checkout(args);
                System.out.println("Current branch fast-forwarded.");
                return;
            }

            if (CommitsUtils.isAncestor(givenCommit, currentCommit)) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return;
            }
            Commit splitPoint = CommitsUtils.getSplitPoint(currentCommit, givenCommit);
            TreeMap<String, String> commonSnapshots = splitPoint.getFileSnapshots();
            TreeMap<String, String> currentSnapshots = currentCommit.getFileSnapshots();
            TreeMap<String, String> givenSnapshots = givenCommit.getFileSnapshots();
            for (String path : plainFilenamesIn(CWD)) { // all file in working directory
                if (join(path).isDirectory()) {
                    continue;
                }
                if (!currentSnapshots.containsKey(path)) {
                    if (givenSnapshots.containsKey(path)) {
                        String fileID = sha1(readContents(join(CWD, path)));
                        if (!givenSnapshots.get(path).equals(fileID)) {
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            return;
                        }
                    }
                }
            }
            TreeSet<String> filesList = new TreeSet<>();
            filesList.addAll(currentSnapshots.keySet());
            filesList.addAll(givenSnapshots.keySet());
            filesList.addAll(commonSnapshots.keySet());

            for (String fileName : filesList) {
                Boolean splitgivenConsistent = CommitsUtils.isConsistent(fileName, splitPoint, givenCommit);
                Boolean splitcurrentConsistent = CommitsUtils.isConsistent(fileName, splitPoint, currentCommit);
                Boolean currentgivenConsistent = CommitsUtils.isConsistent(fileName, currentCommit, givenCommit);

                // case 1,2: 010 011
                if (!splitgivenConsistent && splitcurrentConsistent) {
                    if (CommitsUtils.isUntracked(fileName)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                    if (!givenSnapshots.containsKey(fileName)) {
                        rm(fileName);
                    } else {
                        String[] args = {givenbranch, "--", fileName};
                        checkout(args);
                        add(fileName);
                    }
                }

                if (!splitgivenConsistent && !splitcurrentConsistent && !currentgivenConsistent) {
                    if (CommitsUtils.isUntracked(fileName)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                    File file = join(CWD, fileName);
                    String contentv1 = currentSnapshots.containsKey(fileName) ? readContentsAsString(join(CWD, fileName)) : "";
                    String contentv2 = givenSnapshots.containsKey(fileName) ? readContentsAsString(join(CWD, fileName)) : "";
                    String content = StageUtils.merge(contentv1, contentv2);
                    writeContents(file, content);
                    add(fileName);
                    System.out.println("Encountered a merge conflict.");
                }

                commit("Merged " + givenbranch + " into " + readContentsAsString(Repository.HEAD) + ".");
                Commit mergeCommit = CommitsUtils.getCurrentCommit();
                mergeCommit.setSecondparent(CommitsUtils.getCommitID(givenCommit));
                CommitsUtils.setHEAD(CommitsUtils.saveCommit(mergeCommit));
            }
        }
    }
}


