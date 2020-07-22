package com.clocktower.lullaby.model.utilities;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import com.clocktower.lullaby.model.SongInfo;
import com.crashlytics.android.Crashlytics;

abstract public class RealPathUtil {

    private static final String TAG = "RealPathUtil";

    public static Object getMediaPath(Context context, Uri uri, long mediaType) {

        String metadata, meta_id;
        Uri extUri;
        if (mediaType == Constants.IMAGE){
            metadata = MediaStore.Images.Media.DATA;
            extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Images.Media._ID;
        } else if (mediaType == Constants.VIDEO) {
            metadata =  MediaStore.Video.Media.DATA;
            extUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Video.Media._ID;
        }else {
            metadata =  MediaStore.Audio.Media.DATA;
            extUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Audio.Media._ID;
        }

        String realPath = null;

        // SDK < API11
        //if(mediaType ==1 || Build.VERSION.SDK_INT < 19) {
        String[] proj = {metadata};
        CursorLoader cursorLoader = new CursorLoader(context,
                uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(metadata);
            cursor.moveToFirst();
            realPath = cursor.getString(column_index);
            if(mediaType == Constants._AUDIO){
                String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                return new SongInfo(songName, artist, realPath);
            }

            cursor.close();
        }
        //}
        // SDK > 19 (Android 4.4)

        if (TextUtils.isEmpty(realPath)){
            return getRealPathFromURI_API19(context, uri, mediaType);
        }
        return realPath;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("ObsoleteSdkInt")
    public static Object getMediaPathRetry(Context context, Uri uri, long mediaType) {

        String metadata, meta_id;
        Uri extUri;
        if (mediaType == Constants.IMAGE){
            metadata = MediaStore.Images.Media.DATA;
            extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Images.Media._ID;
        } else if (mediaType == Constants.VIDEO) {
            metadata =  MediaStore.Video.Media.DATA;
            extUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Video.Media._ID;
        }else {
            metadata =  MediaStore.Audio.Media.DATA;
            extUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            meta_id = MediaStore.Audio.Media._ID;
        }

        String realPath = null;

        try{
            // SDK > 19 (Android 4.4)
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = { metadata};
            // where id is equal to
            String sel = meta_id + "=?";
            if (mediaType == Constants._AUDIO)
                return RealPathUtil
                        .geSongInfo(context, extUri, sel, new String[]{ id });

            realPath =RealPathUtil
                    .getDataColumn(context, extUri, sel, new String[]{ id });
        }catch (Exception e){
            e.printStackTrace();
        }
        return realPath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String result = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static Object getRealPathFromURI_API19(final Context context, final Uri uri, long mediaType) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String uriString;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (RealPathUtil.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    uriString = Environment.getExternalStorageDirectory() + "/" + split[1];
                    if (mediaType ==Constants._AUDIO)
                        return new SongInfo("unknown", "unknown",
                                uriString);

                    return uriString;
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (RealPathUtil.isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads",
                        "content://downloads/public_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        if (mediaType ==Constants._AUDIO)
                            return RealPathUtil.geSongInfo(context, contentUri, null, null);
                        return RealPathUtil.getDataColumn(context, contentUri, null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "getRealPathFromURI_API19: wrong folder - " + contentUriPrefix);
                    }
                }
            }
            // MediaProvider
            else if (RealPathUtil.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                if (mediaType ==Constants._AUDIO)
                    return RealPathUtil.geSongInfo(context, contentUri, selection, selectionArgs);

                return RealPathUtil.getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if ( mediaType== Constants._AUDIO)
                return RealPathUtil.geSongInfo(context, uri, null, null);

            return RealPathUtil.getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            if (mediaType ==Constants._AUDIO)
                return new SongInfo("", "", uri.getPath());
            return uri.getPath();
        }

        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            CursorLoader cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs,null);
            cursor = cursorLoader.loadInBackground();
            if (cursor != null ) {
                final int index = cursor.getColumnIndexOrThrow(column);
                cursor.moveToFirst();
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static SongInfo geSongInfo(Context context, Uri uri, String selection,
                                      String[] selectionArgs){
        Cursor cursor = null;
        final String column = MediaStore.Audio.AudioColumns.DATA;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                String url = cursor.getString(index);
                String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
                Log.w("Songs", songName);
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                return  new SongInfo(songName, artist, url);
            }
        }catch (Exception e){
            Crashlytics.log(e.getLocalizedMessage());
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
