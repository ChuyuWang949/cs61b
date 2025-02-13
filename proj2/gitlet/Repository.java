package gitlet;

import com.sun.security.jgss.GSSUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

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
            writeContents(HEAD, "ref: refs/heads/master");


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
            StagingArea index = readObject(Repository.STAGING_AREA, StagingArea.class);
            HashMap<String, String> CurrentVersion = currentCommit.getFileSnapshots();
            HashMap<String, String> CurrentStaged = index.getAddedFile();

            if (CurrentVersion.containsKey(fileName) && CurrentVersion.get(fileName).equals(fileID)) { // if the file is in the current commit
                if (CurrentStaged.containsKey(fileName)) {                                             // and the file is not staged for removal
                    index.removeFileFromAdded(fileName);
                }
            } else {
                index.addFileToAdded(fileName, fileID);
            }
            writeObject(Repository.STAGING_AREA, index);
        }
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (!StageUtils.emptyStage()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
        } else {
            Commit newCommit = new Commit();
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            StagingArea index = readObject(Repository.STAGING_AREA, StagingArea.class);
            newCommit.setParent(CommitsUtils.getCurrentCommitID());
            newCommit.setMessage(message);
            newCommit.setTimestamp(new Date());

            // based on the current commit and the staging area generate the new commit
            newCommit.setFileSnapshots(CommitsUtils.mergeSnapshots(currentCommit, index));
            BlobsUtils.saveBlobs(index.getAddedFile()); // save the staged file to the blobs

            index.clean();
            index.saveStage(); // clean and save the staging area

            CommitsUtils.setHEAD(CommitsUtils.saveCommit(newCommit)); // save the new commit
        }
    }

    public static void rm(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("No reason to remove the untracked file.");
            System.exit(0);
        } else {
            Commit currentCommit = CommitsUtils.getCurrentCommit();
            StagingArea index = readObject(Repository.STAGING_AREA, StagingArea.class);

            HashMap<String, String> indexAdded = index.getAddedFile();
            HashMap<String, String> CurrentTracked = currentCommit.getFileSnapshots();

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
            while (currentCommit != null) {
                currentCommit.printCommitInfo();
                currentCommit = CommitsUtils.getCommit(currentCommit.getParent());
             }
        }
    }
}
