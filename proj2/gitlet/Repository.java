package gitlet;
import java.io.File;
import java.util.*;
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

    /* TODO: fill in the rest of this class. */
    public static boolean isinitialized() {
        return GITLET_DIR.exists();
    }


    public static void init() {
        if(GITLET_DIR.exists()) {
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
            } catch (Exception e) {
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
            TreeMap<String, String> CurrentVersion = currentCommit.getFilesnapshots();
            TreeMap<String, String> CurrentStaged = index.getAddedFile();

            if (CurrentVersion.containsKey(fileName) && CurrentVersion.get(fileName).equals(fileID)) { // if the file is in the current commit
                if (CurrentStaged.containsKey(fileName)) {                                             // and the file is not staged for removal
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
            newCommit.setFilesnapshots(CommitsUtils.mergeSnapshots(currentCommit, index));
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
            TreeMap<String, String> CurrentTracked = currentCommit.getFilesnapshots();

            if (indexAdded.containsKey(fileName)) { // if the file is staged for addition
                index.removeFileFromAdded(fileName); // remove the file from the staging area
                if (CurrentTracked.containsKey(fileName)) { // if the file is tracked in the current commit
                    index.addFileToRemoved(fileName); // add it to the staging area for removal waiting for commiting
                    restrictedDelete(file); // delete the file from the working directory
                }
            } else if (!CurrentTracked.containsKey(fileName)) { // if the file is neither staged nor tracked
                System.out.println("No reason to remove the file.");
            }
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
            String BranchName = strings[0];
            File head = join(REFS_HEAD, BranchName);
            String currentBranch = readContentsAsString(Repository.HEAD);
            if (!head.exists()) {
                System.out.println("No such branch exists.");
            } else if (currentBranch == BranchName) {
                System.out.println("No need to checkout the current branch.");
            } else {
                TreeMap<String, String> currentFilesnapshots = CommitsUtils.getCurrentCommit().getFilesnapshots();
                TreeMap<String, String> givenFilesnapshots = CommitsUtils.getCommit(readContentsAsString(head)).getFilesnapshots();
                for (String filename : givenFilesnapshots.keySet()) {
                    if (!currentFilesnapshots.containsKey(filename)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                } // check if there is any untracked file in the way before do anything else

                for (String filename : currentFilesnapshots.keySet()) {
                    if (!givenFilesnapshots.containsKey(filename)) {
                        File file = join(CWD, filename);
                        if (file.exists()) {
                            restrictedDelete(file);
                        }
                    } else {
                        if (!currentFilesnapshots.get(filename).equals(givenFilesnapshots.get(filename))) {
                            File file = join(CWD, filename);
                            writeContents(file, readContents(join(BLOBS, givenFilesnapshots.get(filename))));
                        }
                    }
                }
                CommitsUtils.setHEAD(readContentsAsString(head));
                writeContents(Repository.HEAD, BranchName);
                StageUtils.saveStage();
            }
        } else if (strings.length == 2) {
            String filename = strings[1];
            if (!CommitsUtils.getCurrentCommit().getFilesnapshots().containsKey(filename)) {
                System.out.println("File does not exist in the current commit.");
            } else {
                File file = join(CWD, filename);
                TreeMap<String, String> currentFilesnapshots = CommitsUtils.getCurrentCommit().getFilesnapshots();
                writeContents(file, readContents(join(BLOBS, currentFilesnapshots.get(filename))));
            }
        } else {
            String commitID = strings[0];
            String fileName = strings[2];
            File commitFile = join(COMMITS, commitID);
            if (!commitFile.exists()) {
                System.out.println("No commit with that id exists.");
            } else {
                Commit commit = CommitsUtils.getCommit(commitID);
                if (commit.getFilesnapshots().containsKey(fileName)) {
                    File file = join(CWD, fileName);
                    writeContents(file, readContents(join(BLOBS, commit.getFilesnapshots().get(fileName))));
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
            if (commit.getMessage().contains(message)) {
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
        TreeMap<String, String> CurrentTracked = currentCommit.getFilesnapshots();
        TreeMap<String, String> StagingAdded = StageUtils.getStage().getAddedFile();
        TreeSet<String> StagingRemoved = StageUtils.getStage().getRemovedFile();

        System.out.println("=== Branches ===");
        for (String branch : plainFilenamesIn(REFS_HEAD)) {
            String HEAD = readContentsAsString(Repository.HEAD);
            if (branch == HEAD) {
                System.out.println("*" + branch + "\n");
            } else {
                System.out.println(branch + "\n");
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName : StagingAdded.keySet()) {
            System.out.println(fileName + "\n");
        }
        System.out.println("=== Removed Files ===");
        for (String fileName : StagingRemoved) {
            System.out.println(fileName + "\n");
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> filesList = plainFilenamesIn(CWD);
        for (String fileName : filesList) {
            File file = join(CWD, fileName);
            // if file is a directory
            if (file.isDirectory()) {
                continue;
            }
            String currentFileID = sha1(readContents(file));
            if (StagingAdded.containsKey(fileName)) {
                if (!StagingAdded.get(fileName).equals(currentFileID)) {
                    System.out.println(fileName + " (modified)" + "\n");
                }
            } else {
                if (CurrentTracked.containsKey(fileName) && !CurrentTracked.get(fileName).equals(currentFileID)) {
                    System.out.println(fileName + " (modified)" + "\n");
                }
            }
        }
        for (String fileName : StagingAdded.keySet()) { // get keys in StagingAdded
            if (!filesList.contains(fileName)) {
                System.out.println(fileName + " (deleted)" + "\n");
            }
        }

        for (String fileName : CurrentTracked.keySet()) {
            if (!filesList.contains(fileName) && !StagingRemoved.contains(fileName)) {
                System.out.println(fileName + " (deleted)" + "\n");
            }
        }

        System.out.println("=== Untracked Files ===");
        for (String fileName : filesList) {
            if (!CurrentTracked.containsKey(fileName) && !StagingAdded.containsKey(fileName)) {
                System.out.println(fileName + "\n");
            }
        }
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
                if (!commit.getFilesnapshots().containsKey(fileName)) {
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

            TreeMap<String, String> commonSnapshots = CommitsUtils.getSplitPoint(currentCommit, givenCommit).getFilesnapshots();
            TreeMap<String, String> currentSnapshots = currentCommit.getFilesnapshots();
            TreeMap<String, String> givenSnapshots = givenCommit.getFilesnapshots();
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
                Boolean splitgivenConsistent = CommitsUtils.isConsistent(fileName, currentCommit, givenCommit);
                Boolean splitcurrentConsistent = CommitsUtils.isConsistent(fileName, givenCommit, currentCommit);
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


