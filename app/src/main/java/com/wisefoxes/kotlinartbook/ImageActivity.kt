package com.wisefoxes.kotlinartbook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import kotlinx.android.synthetic.main.activity_image.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import androidx.core.content.ContextCompat.checkSelfPermission as checkSelfPermission1


@Suppress("DEPRECATION")
class ImageActivity : AppCompatActivity() {

    var selectedBitmap : Bitmap? = null
    var selectedPicture: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val intent = intent

        val info = intent.getStringExtra("info")

        if (info.equals("new")){

            txtGameName.setText("")
            txtGamePlatform.setText("")
            txtGameType.setText("")
            button.visibility = View.VISIBLE

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.defaultimage)
            imageView.setImageBitmap(selectedImageBackground)

        }else {
            button.visibility = View.INVISIBLE

            val selectedId = intent.getIntExtra("id", 1)

            val database = this.openOrCreateDatabase("Games", Context.MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM games WHERE id = ?", arrayOf(selectedId.toString()))

            val gameNameIndex = cursor.getColumnIndex("gameName")
            val gameTypeIndex = cursor.getColumnIndex("gameType")
            val gamePlatformIndex = cursor.getColumnIndex("gamePlatform")
            val gameImageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                //get text
                txtGameName.setText(cursor.getString(gameNameIndex))
                txtGameType.setText(cursor.getString(gameTypeIndex))
                txtGamePlatform.setText(cursor.getString(gamePlatformIndex))

                //Get Image
                val byteArray = cursor.getBlob(gameImageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }
    }

    fun save(view: View){

        val gameName = txtGameName.text.toString()
        val gameType = txtGameType.text.toString()
        val gamePlatform = txtGamePlatform.text.toString()

        if (selectedBitmap != null){

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                val database = this.openOrCreateDatabase("Games", Context.MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS games (id INTEGER PRIMARY KEY, gameName VARCHAR, gameType VARCHAR, gamePlatform VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO games (gameName, gameType, gamePlatform, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, gameName)
                statement.bindString(2, gameType)
                statement.bindString(3, gamePlatform)
                statement.bindBlob(4, byteArray)

                statement.execute()

            }catch (e: Exception){
                e.printStackTrace()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            //finish()
        }

    }

    fun makeSmallerBitmap(image: Bitmap, maximumSize: Int) : Bitmap{

        var width = image.width
        var heigh = image.height

        val bitmapRatio: Double = width.toDouble() / heigh.toDouble()
        if (bitmapRatio > 1){
            width = maximumSize
            val scaleHeight = width / bitmapRatio
            heigh = scaleHeight.toInt()
        }else{
            heigh = maximumSize
            val scaleWidth = heigh * bitmapRatio
            width = scaleWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, heigh, true)
    }

    fun selectImage(view: View){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        }else {
            val intentToGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentToGalery, 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intentToGalery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentToGalery, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selectedPicture = data.data

            try {

                if (selectedPicture != null){
                    if (Build.VERSION.SDK_INT >= 20){
                        val source = ImageDecoder.createSource(this.contentResolver, selectedPicture!!)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(selectedBitmap)
                    }else{
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPicture)
                        imageView.setImageBitmap(selectedBitmap)
                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }




//    @RequiresApi(Build.VERSION_CODES.M)
//    fun selectImage(view: View){
//        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            //Request permission
//            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
//
//        }else{
//            //Pick the picture from the external content
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, 1)
//        }
//
//    }
//
//    override fun onRequestPermissionsResult( requestCode: Int,permissions: Array<out String>, grantResults: IntArray) {
//
//        if (requestCode == 2){
//
//            if (grantResults.size > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                //Pick the picture from the external content
//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                startActivityForResult(intent, 1)
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){
//            //convert image into data
//            var image = data.data
//
//            try {
//                val selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, image)
//                imageView.setImageBitmap(selectedImage)
//            }catch(e:Exception){
//                e.printStackTrace()
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    fun save_image(view: View){
//
//        val artName = txtGameName.text.toString()
//
//        val outputStream = ByteArrayOutputStream()
//        //Compress image to png format
//        selectedBitmap?.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
//        val byteArray = outputStream.toByteArray()
//
//        try {
//            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
//            //Create table to store name and the picture
//            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)")
//            //Insert data to database
//            val sqlString = "INSERT INTO arts (name, image) VALUES (? , ?)"
//            val statement = database.compileStatement(sqlString)
//
//            statement.bindString(1, artName)
//            statement.bindBlob(2, byteArray)
//            statement.execute()
//
//
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)
//
//
//    }
}
