package com.oleg.mahjongclubbooster.constant;

import androidx.annotation.IntDef;

public interface PathType {
    /**
     * Access general paths through the File interface
     */
    int FILE = 0;
    /**
     * Accessing special paths through the Document API
     */
    int DOCUMENT = 1;
    /**
     * For Android 13 and above, the directories under data and obb are displayed directly by
     * package name (because data and obb cannot be authorized directly, only subdirectories
     * can be authorized)
     */
    int PACKAGE_NAME = 2;

    @IntDef({ FILE, DOCUMENT, PACKAGE_NAME })
    @interface PathType1 {
    }
}
