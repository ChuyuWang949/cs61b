package gitlet;

import java.io.File;
import java.util.Arrays;

import static gitlet.Utils.*;

public class StageUtils {
    public static boolean isemptyStage() {
        StagingArea index = readObject(Repository.STAGING_AREA, StagingArea.class);
        return index.getAddedFile().isEmpty() && index.getRemovedFile().isEmpty();
    }

    public static void saveStage() {
        writeObject(Repository.STAGING_AREA, new StagingArea());
    }
}
