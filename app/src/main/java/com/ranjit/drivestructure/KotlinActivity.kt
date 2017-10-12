package com.ranjit.drivestructure

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.content.IntentSender
import android.widget.Toast


class KotlinActivity : AppCompatActivity() ,GoogleApiClient.OnConnectionFailedListener {

    val filesArrallist = ArrayList<String>()
    var isRunning = false

    val ROOT = Environment.getExternalStorageDirectory().absolutePath
    val APP_MEDIA = "/" + "Quiky" + "/" + "Quiky Media/Jokes"
    val APP_MEDIA_PATH = ROOT + APP_MEDIA
    val  mGoogleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(onConnected)
                .addOnConnectionFailedListener(this)
                .build()
    }
    private val  onConnected= onConnnectedListener()


    private inner class onConnnectedListener: GoogleApiClient.ConnectionCallbacks{
        override fun onConnected(result: Bundle?) {
          println("KotlinActivity onConnnected")

        }
        override fun onConnectionSuspended(p0: Int) {
        }
    }

    private var mResolvingError: Boolean = false

    override fun onConnectionFailed(result: ConnectionResult) {
        println("Connection Failed with code ${result.errorCode}")
        if (mResolvingError) {
            return
        }
        if (result.hasResolution()) {
            try {
                mResolvingError = true
                result.startResolutionForResult(this, 202)
            } catch (e: IntentSender.SendIntentException) {
                // todo: signal error to user
            }
        } else {
            mResolvingError = true
        }
    }


    override fun onResume() {
        super.onResume()
        if(!mGoogleApiClient.isConnected){
            mGoogleApiClient.connect()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mGoogleApiClient.connect()

        addFilesToArrayList()

        btnUpload.setOnClickListener{
          println("KotlinActivity clicked")
            if(mGoogleApiClient.isConnected){
                uploadFile()
            }else{
              println("KotlinActivity not connected")
            }
        }

        btnDelete.setOnClickListener {
            println("KotlinActivity Button Clicked")
            Toast.makeText(this, "onclick demo", Toast.LENGTH_SHORT).show()
        }

    }

    fun addFilesToArrayList(){
        val folder= File(APP_MEDIA_PATH)
        val files= folder.listFiles()
        for (x in files){
            if(x.isDirectory){

            }else if (x.name.startsWith(".")){

            }else{
                val fileItem= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ImageFilePath.getPath(this, Uri.fromFile(x))
                } else {
                    x.absolutePath
                }
                filesArrallist.add(fileItem)
            }
        }
    }


    fun uploadFile(){
        if(filesArrallist.size>0){
            val lastItem = filesArrallist[0]
            println("KotlinActivity uploadFile "+ lastItem)
            uploadStatus("Parent2", "Child2", filesArrallist[0])
        }
    }

    fun uploadStatus(a: String , b: String,  c: String){

        isRunning= true

        chekStatus(a,b,c , object : uploadListener{
            override fun onSuccess(driveId: String) {
                println("KotlinActivity uploadFile onSuccess "+ driveId)
                btnDelete.setText(  driveId)
                if (filesArrallist.size > 0) {
                    isRunning = false
                    filesArrallist.removeAt(0)
                    uploadFile()
                }

            }

            override fun onFail(errorMsg: String) {
                btnDelete.setText("")
                if (filesArrallist.size > 0) {
                    isRunning = false
                    filesArrallist.removeAt(0)
                    uploadFile()
                }
            }

        })
    }


    fun  chekStatus(a:String, b: String,c:String, result: uploadListener){

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
            println("KotlinActivity  response for " + c)
                val receivedValue = intent.getStringExtra("DriveId")
            println("KotlinActivity  receivedValue = " + receivedValue)
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
                if (receivedValue == "") {
                    result.onFail("Error while Updating DriveId" + c)
                } else {
                    result.onSuccess(receivedValue)
                }

            }
        }
        KotlinService.getValuesFromActivity(applicationContext, a,b,c , receiver)

    }


    interface uploadListener{
        fun  onSuccess(driveId: String)
        fun  onFail(errorMsg: String)
    }


}
