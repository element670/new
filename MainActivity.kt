package com.example.myproject

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.util.TimeUnit
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.net.URI


private const val REQUEST_CAMERA = 1

class MainActivity : AppCompatActivity() {
    private val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    private val projection = arrayOf(
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.DISPLAY_NAME,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.SIZE
    )
    private var selection = "${MediaStore.Images.ImageColumns.SIZE} >= ?"
    private var selectionArgs = arrayOf(
        TimeUnit.MILLISECOND.toString()
    )

    private val sortOrder = "${MediaStore.Images.ImageColumns.DISPLAY_NAME} ASC"
    private val pictureList = mutableListOf<Picture>()

    private var imageButton: Button? = null
    private var imageView: ImageView? = null
    private var choosePicture: Button? = null

    private val storagePermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            } else {

            }
        }


    private var launchCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedImage = it.data?.extras?.get("data") as Bitmap
                imageView?.setImageBitmap(selectedImage)
                insertInGallery()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        choosePicture = findViewById(R.id.select_picture)
        imageView = findViewById(R.id.image_vw)
        imageButton = findViewById(R.id.take_picture_button)

        imageButton?.setOnClickListener {
//            if (checkPermission(Manifest.permission.CAMERA)) {
                launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
//            } else {
//                requestPermissions(
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    REQUEST_CAMERA
//                )
//            }

        }

    }


    private fun insertInGallery() {
        val query = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use {
            val id = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
            val name = it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
            val size = it.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)
            val date = it.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)

            while (it.moveToNext()) {
                val id = it.getLong(id)
                val name = it.getString(name)
                val size = it.getInt(size)
                val date = it.getInt(date)

                val contentUri: Uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                pictureList += Picture(contentUri as URI, name, size, date)
            }
        }

    }

    fun checkPermission(permission:String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


}

data class Picture(
    val uri: URI,
    val name: String,
    val time: Int,
    val size: Int
)