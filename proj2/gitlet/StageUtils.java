package gitlet;

import java.io.File;
import java.util.TreeMap;
import static gitlet.GitletConstants.*;
import static gitlet.Utils.*;

public class StageUtils {
    public static boolean isemptyStage() {
        StagingArea index = readObject(STAGING_AREA, StagingArea.class);
        return index.getAddedFile().isEmpty() && index.getRemovedFile().isEmpty();
    }

    public static void saveStage() {
        writeObject(STAGING_AREA, new StagingArea());
    }

    public static StagingArea getStage() {
        return readObject(STAGING_AREA, StagingArea.class);
    }

    public static void saveBlobs(TreeMap<String, String> index) {
        for (String fileName : index.keySet()) {
            String fileID = index.get(fileName);
            File blobFile = join(BLOBS, fileID);
            String content = readContentsAsString(join(CWD, fileName));
            writeContents(blobFile, content);
        }
    }

    public static String merge(String contentv1, String contentv2) {
        return "<<<<<<< HEAD\n" + contentv1 + "=======\n" + contentv2 + ">>>>>>>\n";
    }
}
