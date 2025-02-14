package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

public class BlobsUtils {
    public static void saveBlobs(HashMap<String, String> index) {
        for (String fileName : index.keySet()) {
            String fileID = index.get(fileName);
            File blobFile = join(Repository.blobs, fileID);
            String content = readContentsAsString(join(Repository.CWD, fileName));
            writeContents(blobFile, content);
        }
    }
}
