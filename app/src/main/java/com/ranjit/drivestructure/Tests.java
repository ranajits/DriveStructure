package com.ranjit.drivestructure;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by ranjit on 10/10/17.
 */

public class Tests {


   /* public void checkAndcreateParentFolder(final String parentFolder, final String childFlder, final String fileToUpload) {


        isRunning = true;
        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE, parentFolder),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
                    boolean isFound = false;
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(parentFolder)) {
                            isFound = true;
                            Log.e(TAG, "Parent exists ");
                            checkAndCreateChildFolder(m.getDriveId(), childFlder, fileToUpload);
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.i(TAG, "Folder not exists, Creating Now");
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setViewed(false)
                                .setDescription("My apps Root Folder")
                                .setLastViewedByMeDate(new Date(System.currentTimeMillis()))
                                .setTitle(parentFolder).build();

                        //Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFolder(mGoogleApiClient, changeSet)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFolderResult result) {
                                        if (!result.getStatus().isSuccess()) {
                                            Log.i(TAG, "Error while trying to create the folder");
                                            return;
                                        }
                                        checkAndCreateChildFolder(result.getDriveFolder().getDriveId(), childFlder, fileToUpload);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Log.e(TAG, "onFailure");
            }
        });

        Log.e(TAG, "createParentFolder complete");
    }


    public void checkAndCreateChildFolder(final DriveId sFolderId, final String childFlder, final String fileToUpload) {


        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE, childFlder),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder under the root.");
                } else {
                    boolean isFound = false;
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(childFlder)) {
                            Log.e(TAG, "Child exists ");
                            isFound = true;
                            checkAndCreateFileInChildFolder(m.getDriveId(), fileToUpload);
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.e(TAG, "Child not exists, Creating Now");
                        DriveFolder folder = sFolderId.asDriveFolder();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(childFlder).build();
                        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFolderResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    Log.e(TAG, "Error while trying to create the folder");
                                    return;
                                }
                                checkAndCreateFileInChildFolder(result.getDriveFolder().getDriveId(), fileToUpload);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Log.e(TAG, "onFailure");
            }
        });

        Log.e(TAG, "checkAndCreateChildFolder complete");
    }


    public void checkAndCreateFileInChildFolder(final DriveId sFolderId, final String fileToUpload) {

        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE, new File(fileToUpload).getName()),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder under the root.");
                } else {
                    boolean isFound = false;
                    Log.e(TAG, "Checking File: " + fileToUpload);

                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(new File(fileToUpload).getName())) {
                            Log.e(TAG, "File exist in Child");
                            btnDelete.setText(m.getDriveId().encodeToString());
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.e(TAG, "File not exist in Child");
                        createFileInChildFolder(sFolderId, fileToUpload);
                    } else {
                        try {
                            isRunning = false;
                        } finally {

                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Log.e(TAG, "onFailure");
            }
        });
    }


    private void createFileInChildFolder(final DriveId driveId, String fileToUpload) {
        String fileType = getFileType(fileToUpload);
        if (fileType.equals(typeImg)) {
            MIME = MIME_PHOTO;
        } else if (fileType.equals(typeDoc)) {
            MIME = MIME_DOC;
        } else if (fileType.equals(typeVdo)) {
            MIME = MIME_VIDEO;
        } else {

        }
        saveFiletoDrive(fileToUpload, MIME, driveId);
    }


    *//**
     * Create a new file and save it to Drive.
     *//*
    private void saveFiletoDrive(final String fileToUpload, final String mime, final DriveId driveId) {
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        Log.i(TAG, "Connection successful, creating new contents..." + fileToUpload);
                        OutputStream outputStream = result.getDriveContents().getOutputStream();

                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(fileToUpload);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n;
                            while (-1 != (n = fis.read(buf)))
                                baos.write(buf, 0, n);
                            byte[] photoBytes = baos.toByteArray();
                            outputStream.write(photoBytes);

                            outputStream.close();
                            outputStream = null;
                            fis.close();
                            fis = null;

                        } catch (FileNotFoundException e) {
                            Log.w(TAG, "FileNotFoundException: " + e.getMessage());
                        } catch (IOException e1) {
                            Log.w(TAG, "Unable to write file contents." + e1.getMessage());
                        }
                        String title = new File(fileToUpload).getName();


                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()

                                .setMimeType(mime).setTitle(title).build();


                        DriveFolder folder = driveId.asDriveFolder();
                        folder.createFile(mGoogleApiClient,
                                metadataChangeSet,
                                result.getDriveContents()).setResultCallback(new ResultCallbacks<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onSuccess(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (driveFileResult.getStatus().isSuccess()) {
                                    Log.d(TAG, "Came in Backup completed!");
                                    btnDelete.setText(driveFileResult.getDriveFile().getDriveId().encodeToString());
                                } else {
                                    Log.d(TAG, "Someting Went Wrong! Backup not completed");
                                }
                                try {
                                    isRunning = false;
                                } finally {

                                }

                            }

                            @Override
                            public void onFailure(@NonNull Status status) {
                                isRunning = false;
                            }
                        });
                    }
                });
    }*/
}
