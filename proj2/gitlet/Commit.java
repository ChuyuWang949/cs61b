package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.serialize;
import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author cyw
 */
public class Commit implements Serializable  {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private Date timestamp;

    /** The message of this Commit. */
    private String message;
    private TreeMap<String, String> Filesnapshots;
    private String parent;
    private String secondparent;


    /* TODO: fill in the rest of this class. */
    // TODO: finish the logic of Filesnapshots

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parent = null;
        this.secondparent = null;
        this.Filesnapshots = new TreeMap<>();
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

    public TreeMap<String, String> getFilesnapshots() {
        return this.Filesnapshots;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setFilesnapshots(TreeMap<String, String> Filesnapshots) {
        this.Filesnapshots = Filesnapshots;
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        System.out.println("===");
        System.out.println("commit " + CommitsUtils.getCommitID(this));
        if (secondparent != null) {
            System.out.println("Merge: " + parent.substring(0, 7) + " " + secondparent.substring(0, 7));
        }
        System.out.println("Date: " + sdf.format(this.timestamp));
        System.out.println(this.message);
        System.out.println();
    }
}
