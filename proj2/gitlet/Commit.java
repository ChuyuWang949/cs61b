package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Collection;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

import static gitlet.Utils.serialize;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable  {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private Date timestamp;

    /** The message of this Commit. */
    private String message;
    private HashMap<String, String> FileSnapshots;
    private String parent;
    private String secondparent;


    /* TODO: fill in the rest of this class. */
    // TODO: finish the logic of FileSnapshots
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.timestamp = new Date();
    }

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parent = null;
        this.secondparent = null;
    }

    public String getParent() {
        return this.parent;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getSecondparent() {
        return this.secondparent;
    }

    public HashMap<String, String> getFileSnapshots() {
        return this.FileSnapshots;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setFileSnapshots(HashMap<String, String> FileSnapshots) {
        this.FileSnapshots = FileSnapshots;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setSecondparent(String secondparent) {
        this.secondparent = secondparent;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void printCommitInfo() {
    }
}
