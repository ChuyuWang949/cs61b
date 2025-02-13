package gitlet;

import java.io.File;
import java.util.Arrays;

import static gitlet.Utils.*;

public class StageUtils {
    public static boolean emptyStage() {
        StagingArea index = readObject(Repository.STAGING_AREA, StagingArea.class);
        return index.getAddedFile().isEmpty() && index.getRemovedFile().isEmpty();
    }
}
