package gitlet;

import java.io.File;

import static gitlet.Utils.join;

public class GitletConstants {
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File STAGING_AREA = join(GITLET_DIR, "staging_area");
    public static final File BLOBS = join(GITLET_DIR, "blobs");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File REFS_HEAD = join(REFS, "heads");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public static final String UNINITIALIZED_WARNING = "Not in an initialized Gitlet directory.";
    public static final String INCORRECT_OPERANDS_WARNING = "Incorrect operands.";
    public static final String ALREADY_EXISTS_WARNING =
            "A Gitlet version-control system already exists in the current directory.";
    public static final String MERGE_MODIFY_UNTRACKED_WARNING =
            "There is an untracked file in the way; delete it, or add and commit it first.";
}
