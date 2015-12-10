package lang.com.applocker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import lang.com.applocker.utils.AppLockerData;

/**
 * Created by android on 12/3/15.
 */
public class AppLockerProvider extends ContentProvider {

    private static final String TAG = "AppLockerProvider";

    public static final int ADD = 1;
    public static final int DELETE = 2;

    private AppLockerData appLockerData;

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        matcher.addURI("lang.com.applocker.applock", "ADD", ADD);
        matcher.addURI("lang.com.applocker.applock", "DELETE", DELETE);
    }

    @Override
    public boolean onCreate() {
        appLockerData = new AppLockerData(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int result = matcher.match(uri);
        if (result == ADD) {
            String packname = values.getAsString("packname");
            appLockerData.add(packname);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = matcher.match(uri);
        if (result == DELETE) {
            appLockerData.delete(selectionArgs[0]);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
