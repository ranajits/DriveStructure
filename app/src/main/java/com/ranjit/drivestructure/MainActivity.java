package com.ranjit.drivestructure;

import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.util.Date;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static  final  String TAG="MainActiviy";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private static  final  String Folder_name = "MyFolder_1";
    private static  final  String Folder_name_child_1 = "Child_1";
    private static  final  String File_name_In_child_1 = "Child_1_File_1";

    private boolean createChild= false;
    private boolean createFileInChild= false;
    private DriveId mFolderDriveId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)

                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(getApplicationContext(), "Error while trying to create the folder", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(createChild)
                        createChildFolder(result.getDriveFolder().getDriveId());
                    if(createFileInChild)
                        createFileChild(result.getDriveFolder().getDriveId());
                }
            };


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public void createParent(View v){

        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE,Folder_name),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {
                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();

                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(getApplicationContext(), "Cannot create folder in the root.", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isFound = false;
                    createChild=true;
                    for(Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(Folder_name)) {
                            isFound = true;    createChild=true;
                            createChildFolder(m.getDriveId());
                            break;
                        }
                    }
                    if(!isFound) {

                        Toast.makeText(getApplicationContext(), "Folder not exists, Creating Now", Toast.LENGTH_SHORT).show();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setViewed(false)
                                .setDescription("My apps Root Folder")
                                .setLastViewedByMeDate(new Date(System.currentTimeMillis()))
                                .setTitle(Folder_name).build();
                        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
                                mGoogleApiClient, changeSet).setResultCallback(folderCreatedCallback);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });
    }




    public void createChildFolder(final DriveId sFolderId){

        createChild=false;

        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE,Folder_name_child_1),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();


        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {
                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();

                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(getApplicationContext(), "Cannot create folder under the root.", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isFound = false;
                    createFileInChild=true;
                    for(Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(Folder_name_child_1)) {
                            Toast.makeText(getApplicationContext(), "Child exists ", Toast.LENGTH_SHORT).show();
                            isFound = true;
                            createFileChild(m.getDriveId());
                            break;
                        }
                    }
                    if(!isFound) {
                        Toast.makeText(getApplicationContext(), "Child not exists, Creating Now", Toast.LENGTH_SHORT).show();
                        DriveFolder folder = sFolderId.asDriveFolder();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(Folder_name_child_1).build();
                        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(folderCreatedCallback);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void createFileChild(final DriveId sFolderId){
        createFileInChild=false;
        Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE,File_name_In_child_1),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallbacks<DriveApi.MetadataBufferResult>() {
            @Override
            public void onSuccess(@NonNull DriveApi.MetadataBufferResult result) {

                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(getApplicationContext(), "Cannot create folder under the root.", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isFound = false;
                    createFileInChild=true;
                    for(Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(File_name_In_child_1)) {
                            Toast.makeText(getApplicationContext(), "File exist in Child", Toast.LENGTH_SHORT).show();
                            isFound = true;
                            break;
                        }
                    }
                    if(!isFound) {
                        Toast.makeText(getApplicationContext(), "File not exist in Child", Toast.LENGTH_SHORT).show();
                        create_file_in_folder(sFolderId);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void create_file_in_folder(final DriveId driveId) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while trying to create new file contents");
                    return;
                }

             //   OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();


                //------ THIS IS AN EXAMPLE FOR FILE --------
                Toast.makeText(MainActivity.this, "Uploading to drive.", Toast.LENGTH_SHORT).show();
                /*final File theFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xtests/tehfile.txt");
                try {
                    FileInputStream fileInputStream = new FileInputStream(theFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e1) {
                    Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                }*/

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(File_name_In_child_1).setMimeType("text/plain").setStarred(false).build();
                DriveFolder folder = driveId.asDriveFolder();
                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (!driveFileResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Error while trying to create the file");
                                    return;
                                }
                                Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                            }
                        });
            }
        });
    }



}
