package com.ranjit.drivestructure;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
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
import java.util.ArrayList;
import java.util.Date;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import java.io.IOException;
import java.io.OutputStream;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActiviy";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private boolean isRunning = false;
    Button btnDelete;
    public static final String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String APP_MEDIA = "/" + "Quiky" + "/" + "Quiky Media/Jokes";
    public static final String APP_MEDIA_PATH = ROOT + APP_MEDIA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        btnDelete = (Button) findViewById(R.id.btnDelete);
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
        Log.i(TAG, "Connected");

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


    public void SelectFile(View v) {
        filesArrallist= new ArrayList<>();
        getfiles();
    }


    public void deleteFile(View v) {

        if (btnDelete.getText().toString().equals("Delete File")) {
        } else {
            deleteFileFromDrive(btnDelete.getText().toString());
        }

    }

    private void getfiles(){
        File file = new File(APP_MEDIA_PATH);
        final File[] files = file.listFiles();
        for (int x = 0; x < files.length; x++) {
            File f = files[x];

            if (f.isDirectory()) {
            } else if (f.getName().startsWith(".")) {
            } else {
                String fname = ImageFilePath.getPath(MainActivity.this, Uri.fromFile(f));
                Log.d(TAG, "working on Image  : " + f);
                filesArrallist.add(fname);
            }
        }
        uploadFiles();

    }

    ArrayList<String> filesArrallist;
    public void uploadFiles() {
        if(isRunning){
        }else {
            if(filesArrallist.size()>0){
            uploadStatus("Parent2", "Child2", filesArrallist.get(0));
            }else {
                Log.e(TAG, "All files uploaded");
            }
        }
    }


    private void deleteFileFromDrive(String driveIdStr) {

        DriveId fileId = DriveId.decodeFromString(driveIdStr);
        DriveFile sumFile = fileId.asDriveFile();
        sumFile.delete(mGoogleApiClient).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                if (!status.isSuccess()) {
                    Log.e(TAG, "Unable to delete app data.");
                    // return;
                }
                btnDelete.setText("Delete File");
                Log.e(TAG, "file deleted");
            }

            @Override
            public void onFailure(@NonNull Status status) {

            }
        });


    }


    public static final String typeImg = "Img";
    public static final String typeDoc = "Doc";
    public static final String typeNoPdf = "noPdf";
    public static final String typeVdo = "Vdo";

    public static String getFileType(String strFileName) {

        if (strFileName.endsWith(".pdf")
                || strFileName.endsWith(".PDF")
                || (strFileName.endsWith(".txt")
                || strFileName.endsWith(".doc")
                || strFileName.endsWith(".DOC")
                || strFileName.endsWith(".docx")
                || strFileName.endsWith(".DOCX")
                || strFileName.endsWith(".TXT"))) {
            return typeDoc;
        } else if (strFileName.endsWith(".mp4")
                || strFileName.endsWith(".mkv")
                || strFileName.endsWith(".webm")
                || strFileName.endsWith(".3gp")
                || strFileName.endsWith(".MP4")
                || strFileName.endsWith(".MKV")
                || strFileName.endsWith(".WEBM")
                || strFileName.endsWith(".mov")
                || strFileName.endsWith(".MOV")
                || strFileName.endsWith(".3GP")) {
            return typeVdo;
        } else if (strFileName.endsWith(".png")
                || strFileName.endsWith(".PNG")
                || strFileName.endsWith(".jpg")
                || strFileName.endsWith(".JPG")
                || strFileName.endsWith(".jpeg")
                || strFileName.endsWith(".JPEG")
                || strFileName.endsWith(".GIF")
                || strFileName.endsWith(".gif")) {
            return typeImg;
        } else {
            return "";
        }
    }


    private void uploadStatus(final String parentFolder, final String childFlder, final String fileToUpload) {
        isRunning =true;
        upDate(MainActivity.this, parentFolder,childFlder, fileToUpload, new UploadListener() {
            @Override
            public void success(String driveId) {

                Log.e(TAG, " onSuccess : "+  driveId);
                btnDelete.setText(driveId);
                if(filesArrallist.size()>0){
                    isRunning =false;
                    filesArrallist.remove(0);
                    uploadFiles();
                }
            }

            @Override
            public void error(Throwable error) {

                Log.e(TAG, error.toString());
                error.printStackTrace();
                if(filesArrallist.size()>0){
                    isRunning =false;
                    filesArrallist.remove(0);
                    uploadFiles();
                }
            }
        });
    }

    public void upDate(final Context ctx, final String parentFolder, final String childFlder, final String fileToUpload, final UploadListener uploadListener) {
        Log.e(TAG, "In upDate");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "Received response for " + fileToUpload);
                String receivedValue = intent.getStringExtra("DriveId");
                Log.e(TAG, "receivedValue = " + receivedValue);
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                if(receivedValue.equals("")) {
                    uploadListener.error(new RuntimeException("Error while Updating DriveId"+ fileToUpload));
                }else {
                uploadListener.success(receivedValue);
                }

            }
        };
        UploadService.invoke(ctx, parentFolder, childFlder, fileToUpload, receiver);
    }



	public interface UploadListener extends MYListener<String> {

        }

    public interface MYListener<Model> {

            void success(Model model);

            void error(Throwable error);
	}



	   /*for (int x = 0; x < files.length; x++) {
            File f = files[1];
            if (f.isDirectory()) {
            } else if (f.getName().startsWith(".")) {
            } else {
                //We are uploadinf only one file now
                if (singleFileUpload) {
                    String fname = ImageFilePath.getPath(MainActivity.this, Uri.fromFile(f));
                    Log.d(TAG, "working on Image  : " + f);
                    //checkAndcreateParentFolder("Parent2", "Child2", fname);
                    uploadStatus("Parent2", "Child2", fname);

                    singleFileUpload = false;
                }
            }
        }*/




}
