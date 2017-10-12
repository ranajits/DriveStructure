package com.ranjit.drivestructure

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.ResultCallbacks
import com.google.android.gms.common.api.Status
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.ranjit.drivestructure.MainActivity.typeImg
import java.io.*
import java.util.*

/**
 * Created by ranjit on 11/10/17.
 */

class KotlinService() : IntentService("") {



    fun KotlinService(str :String){

    }


    val onConnectFail= object : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(p0: ConnectionResult) {
        }
    }
    val mGAC: GoogleApiClient by lazy {
        GoogleApiClient.Builder(applicationContext)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(onConnect)
                .addOnConnectionFailedListener(onConnectFail)

                /*addConnectionCallbacks(object :GoogleApiClient.ConnectionCallbacks{
                    override fun onConnected(p0: Bundle?) {
                        checkAndcreateParentFolder(a, b, c)
                    }

                    override fun onConnectionSuspended(p0: Int) {
                    }


                })
                .addOnConnectionFailedListener(object: GoogleApiClient.OnConnectionFailedListener{
                    override fun onConnectionFailed(p0: ConnectionResult) {
                    }

                })*/
                .build()
    }
    val onConnect=object :GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(p0: Bundle?) {
            println("KotlinService Service connected.. ");
            checkAndcreateParentFolder(a, b, c)
        }

        override fun onConnectionSuspended(p0: Int) {
        }
    }

    val  mdf: GoogleApiClient by lazy {

        GoogleApiClient.Builder(this).build()
    }
    var a=""
    var b=""
    var c=""

    fun setValues(aa: String, bb:String, cc: String){
        a=aa
        b=bb
        c=cc
    }

    override fun onHandleIntent(intent: Intent){

        println("KotlinService Service started.. ");

        val a= intent.getStringExtra("Parent")
        val b= intent.getStringExtra("Child")
        val c= intent.getStringExtra("file")

        setValues(a,b,c)
        mGAC.connect()
        if(mGAC.isConnected){
            println("KotlinService Connected ");

        }else{
            println("KotlinService not Connected ");
        }

    }


    companion object {


        fun getValuesFromActivity(context: Context, a: String, b: String, c: String, receiver: BroadcastReceiver) {

            val kService = Intent(context, KotlinService::class.java)
            kService.putExtra("Parent", a)
            kService.putExtra("Child", b)
            kService.putExtra("file", c)

            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, IntentFilter(c))
            context.startService(kService)

        }
    }

    private fun setError(filter: String) {

        val localIntent = Intent(filter)
        localIntent.putExtra("DriveId", "")
        // Broadcast intent to receivers in this app only.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    private fun settSuccess(filter: String, driveID: String) {

        val localIntent = Intent(filter)
        localIntent.putExtra("DriveId", driveID)
        // Broadcast intent to receivers in this app only.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

    }


    fun checkAndcreateParentFolder (a: String, b: String, c: String){

        val query=Query.Builder().addFilter(Filters.or(Filters.eq(SearchableField.TITLE, a), (Filters.eq(SearchableField.TRASHED, false)))).build()

        Drive.DriveApi.query(mGAC, query).setResultCallback (object: ResultCallbacks<DriveApi.MetadataBufferResult>() {
            override fun onSuccess(result: DriveApi.MetadataBufferResult) {
                if (!result.status.isSuccess) {
                    setError(c)
                }

                var isFound = false;
                for (m in result.metadataBuffer) {
                    if (m.title == a) {
                        isFound = true
                        println("KotlinService Parent exists ");
                        checkAndCreateChildFolder(m.driveId, b, c)
                        break
                    }
                }

                if (!isFound) {
                    val cSet = MetadataChangeSet
                            .Builder()
                            .setViewed(false)
                            .setDescription("My apps Root Folder")
                            .setLastViewedByMeDate(Date(System.currentTimeMillis()))
                            .setTitle(a)
                            .build()

                    Drive.DriveApi.getRootFolder(mGAC)
                            .createFolder(mGAC, cSet)
                            .setResultCallback(object : ResultCallback<DriveFolder.DriveFolderResult> {
                                override fun onResult(result: DriveFolder.DriveFolderResult) {
                                    if (!result.status.isSuccess) {
                                        setError(c)
                                        return
                                    }
                                    checkAndCreateChildFolder(result.driveFolder.driveId, b, c)
                                }
                            })
                }
            }

            override fun onFailure(p0: Status) {
                setError(c)
            }
        })
    }


    fun checkAndCreateChildFolder(sFolderId: DriveId, childFlder: String, fileToUpload: String) {


        val query = Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE, childFlder),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build()

        Drive.DriveApi.query(mGAC, query).setResultCallback(object : ResultCallbacks<DriveApi.MetadataBufferResult>() {
            override fun onSuccess(result: DriveApi.MetadataBufferResult) {

                if (!result.status.isSuccess) {
                    println( "Cannot create folder under the root.")
                    setError(fileToUpload)
                } else {
                    var isFound = false
                    for (m in result.metadataBuffer) {
                        if (m.title == childFlder) {
                            println( "Child exists ")
                            isFound = true
                            checkAndCreateFileInChildFolder(m.driveId, fileToUpload)
                            break
                        }
                    }
                    if (!isFound) {
                        println( "Child not exists, Creating Now")
                        val folder = sFolderId.asDriveFolder()
                        val changeSet = MetadataChangeSet.Builder()
                                .setTitle(childFlder).build()
                        folder.createFolder(mGAC, changeSet).setResultCallback(ResultCallback { result ->
                            if (!result.status.isSuccess) {
                                println( "Error while trying to create the folder")
                                return@ResultCallback
                            }
                            checkAndCreateFileInChildFolder(result.driveFolder.driveId, fileToUpload)
                        })
                    }
                }
            }

            override fun onFailure(status: com.google.android.gms.common.api.Status) {
                println( "onFailure")
                setError(fileToUpload)
            }
        })
        println( "checkAndCreateChildFolder complete")
    }


    fun checkAndCreateFileInChildFolder(sFolderId: DriveId, fileToUpload: String) {

        val query = Query.Builder()
                .addFilter(Filters.or(Filters.eq(
                        SearchableField.TITLE, File(fileToUpload).name),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build()

        Drive.DriveApi.query(mGAC, query).setResultCallback(object : ResultCallbacks<DriveApi.MetadataBufferResult>() {
            override fun onSuccess(result: DriveApi.MetadataBufferResult) {

                if (!result.status.isSuccess) {
                   println( "Cannot create folder under the root.")
                    setError(fileToUpload)
                } else {
                    var isFound = false
                    println("KotlinService Checking File: " + fileToUpload)

                    for (m in result.metadataBuffer) {
                        if (m.title == File(fileToUpload).name) {
                            println("KotlinService File exist in Child")
                            //btnDelete.setText(m.getDriveId().encodeToString());

                            settSuccess(fileToUpload, m.driveId.encodeToString())
                            isFound = true
                            break
                        }
                    }
                    if (!isFound) {
                        println( "File not exist in Child")
                        createFileInChildFolder(sFolderId, fileToUpload)
                    }
                }
            }

            override fun onFailure(status: com.google.android.gms.common.api.Status) {
                println( "onFailure")
                setError(fileToUpload)
            }
        })
    }


    val typeImg = "Img"
    val typeDoc = "Doc"
    val typeVdo = "Vdo"
    private val MIME_PHOTO = "image/jpeg"
    private val MIME_VIDEO = "video/mp4"
    private val MIME_DOC = "application/pdf"
    private var MIME = ""
    private fun createFileInChildFolder(driveId: DriveId, fileToUpload: String) {
        val fileType = MainActivity.getFileType(fileToUpload)
        if (fileType == typeImg) {
            MIME = MIME_PHOTO
        } else if (fileType == typeDoc) {
            MIME = MIME_DOC
        } else if (fileType == typeVdo) {
            MIME = MIME_VIDEO
        } else {

        }
        saveFiletoDrive(fileToUpload, MIME, driveId)
    }

    private fun saveFiletoDrive(fileToUpload: String, mime: String, driveId: DriveId) {
        Drive.DriveApi.newDriveContents(mGAC).setResultCallback(
                ResultCallback { result ->
                    if (!result.status.isSuccess) {
                        println( "Failed to create new contents.")
                        setError(fileToUpload)
                        return@ResultCallback
                    }
                    println("KotlinService Connection successful, creating new contents..." + fileToUpload)
                    var outputStream: OutputStream? = result.driveContents.outputStream

                    var fis: FileInputStream?
                    try {
                        fis = FileInputStream(fileToUpload)
                        val baos = ByteArrayOutputStream()
                        val buf = ByteArray(1024)
                        var v= 0


                        do {
                            v= fis.read(buf)
                            if ( v<0 )
                                break
                            baos.write(buf, 0,v)
                        } while (true)

                        val photoBytes = baos.toByteArray()
                        outputStream!!.write(photoBytes)

                        outputStream.close()
                        outputStream = null
                        fis.close()
                        fis = null

                    } catch (e: FileNotFoundException) {
                        println( "FileNotFoundException: " + e.message)
                    } catch (e1: IOException) {
                        println("KotlinService Unable to write file contents." + e1.message)
                    }

                    val title = File(fileToUpload).name

                    val metadataChangeSet = MetadataChangeSet.Builder()
                            .setMimeType(mime).setTitle(title).build()
                    val folder = driveId.asDriveFolder()
                    folder.createFile(mGAC,
                            metadataChangeSet,
                            result.driveContents).setResultCallback(object : ResultCallbacks<DriveFolder.DriveFileResult>() {
                        override fun onSuccess(driveFileResult: DriveFolder.DriveFileResult) {
                            if (driveFileResult.status.isSuccess) {
                                println("KotlinService Came in Backup completed!")
                                settSuccess(fileToUpload, driveFileResult.driveFile.driveId.encodeToString())

                            } else {
                                println("KotlinService Someting Went Wrong! Backup not completed")
                                setError(fileToUpload)
                            }
                        }

                        override fun onFailure(status: Status) {
                            setError(fileToUpload)
                        }
                    })
                })
    }


}