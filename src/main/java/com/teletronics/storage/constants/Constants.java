package com.teletronics.storage.constants;

import java.util.Set;

public class Constants {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;

    public static final String URL_PREFIX = "http://localhost:9000";

    public static final String FILE_UPLOAD_ERROR = "Error with uploading file to storage";
    public static final String FILE_EXISTS_ERROR = "File already exists";
    public static final String EMPTY_FILE_ERROR = "File can not be empty";
    public static final String FILE_EXISTS_CHECK_ERROR = "Error with checking file existing";

    public static final String TAG_EXISTS_ERROR = "Tag already exists";
    public static final String TAG_IS_EMPTY_ERROR = "Tag is empty";
    public static final String TAG_IS_NOT_ALLOWED_ERROR = "One or more tags do not exist in the system: ";

    public static final String USER_IS_NOT_FILE_OWNER_ERROR = "User is not owner of this file";
    public static final String EMPTY_FILE_NAME_ERROR = "Filename cannot be empty";
    public static final String FILE_NOT_FOUND_ERROR = "File not found";
    public static final String FILE_RENAME_ERROR = "File rename failed";
    public static final String FILE_DELETE_ERROR = "File deletion failed";
    public static final String INVALID_SORT_FIELD_ERROR = "Invalid sort field: ";
    public static final String INVALID_SORT_ORDER_ERROR = "Invalid sort order: ";

    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_NOT_FOUND = "not found";

    public static final Set<String> ALLOWED_SORTFIELDS = Set.of("uploaddate", "filename", "filesize");
    public static final Set<String> ALLOWED_SORT_ORDERS = Set.of("asc", "desc");
}
