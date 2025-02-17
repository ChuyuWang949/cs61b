package gitlet;

import com.sun.security.jgss.GSSUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import static gitlet.Utils.*;
// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
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
    public static final File blobs = join(GITLET_DIR, "blobs");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File refs = join(GITLET_DIR, "refs");
    public static final File refs_heads = join(refs, "heads");
    public static final File refs_tags = join(refs, "tags");
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
            blobs.mkdir();
            commits.mkdir();
            refs.mkdir();
            refs_heads.mkdir();
            refs_tags.mkdir();

            Commit initialCommit = new Commit();
            String initialCommitID = CommitsUtils.saveCommit(initialCommit);
            File head = join(refs_heads, "master");
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
    // How do the commit command interact with the staging area?
    // TODO：how to implement the check stage and remove stage?
    // TODO: how to implement the add stage?
    // TODO: rm的部分还需要额外考虑一下。
    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            String fileID = sha1(readContents(file));
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            StagingArea index = readObject(STAGING_AREA, StagingArea.class);
            TreeMap<String, String> CurrentVersion = currentCommit.getFileSnapshots();
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
            newCommit.setFileSnapshots(CommitsUtils.mergeSnapshots(currentCommit, index));
            BlobsUtils.saveBlobs(index.getAddedFile()); // save the staged file to the blobs // TODO：a file exist both in added and removed?

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
            TreeMap<String, String> CurrentTracked = currentCommit.getFileSnapshots();

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

    // TODO: how to deal with merge commit?
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
            File head = join(refs_heads, BranchName);
            String currentBranch = readContentsAsString(Repository.HEAD);
            if (!head.exists()) {
                System.out.println("No such branch exists.");
            } else if (currentBranch == BranchName) {
                System.out.println("No need to checkout the current branch.");
            } else {
                TreeMap<String, String> currentFileSnapshots = CommitsUtils.getCurrentCommit().getFileSnapshots();
                TreeMap<String, String> givenFileSnapshots = CommitsUtils.getCommit(readContentsAsString(head)).getFileSnapshots();
                for (String filename : givenFileSnapshots.keySet()) {
                    if (!currentFileSnapshots.containsKey(filename)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                } // check if there is any untracked file in the way before do anything else

                for (String filename : currentFileSnapshots.keySet()) {
                    if (!givenFileSnapshots.containsKey(filename)) {
                        File file = join(CWD, filename);
                        if (file.exists()) {
                            restrictedDelete(file);
                        }
                    } else {
                        if (!currentFileSnapshots.get(filename).equals(givenFileSnapshots.get(filename))) {
                            File file = join(CWD, filename);
                            writeContents(file, readContents(join(blobs, givenFileSnapshots.get(filename))));
                        }
                    }
                }
                CommitsUtils.setHEAD(readContentsAsString(head));
                writeContents(Repository.HEAD, BranchName);
                StageUtils.saveStage();
            }
        } else if (strings.length == 2) {
            String filename = strings[1];
            if (!CommitsUtils.getCurrentCommit().getFileSnapshots().containsKey(filename)) {
                System.out.println("File does not exist in the current commit.");
            } else {
                File file = join(CWD, filename);
                writeContents(file, readContents(join(blobs, CommitsUtils.getCurrentCommit().getFileSnapshots().get(filename))));
            }
        } else {
            String commitID = strings[0];
            String fileName = strings[2];
            File commitFile = join(commits, commitID);
            if (!commitFile.exists()) {
                System.out.println("No commit with that id exists.");
            } else {
                Commit commit = CommitsUtils.getCommit(commitID);
                if (commit.getFileSnapshots().containsKey(fileName)) {
                    File file = join(CWD, fileName);
                    writeContents(file, readContents(join(blobs, commit.getFileSnapshots().get(fileName))));
                } else {
                    System.out.println("File does not exist in that commit.");
                }
            }
        }
    }
}
