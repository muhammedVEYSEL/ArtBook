package com.veys.artbookkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.veys.artbookkotlin.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activityResaultLauncher : ActivityResultLauncher<Intent> // galeriye gitmek için kullanacağımız değişken
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var myDatabase : SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        myDatabase = this.openOrCreateDatabase("Art_Book", MODE_PRIVATE,null)
        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            binding.imageView.setImageResource(R.drawable.click)
            binding.ArteditText.setText("")
            binding.NameeditText.setText("")
            binding.YeareditText.setText("")
            binding.Savebutton.visibility = View.VISIBLE

        }else{
            binding.Savebutton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            var cursor = myDatabase.rawQuery("SELECT * FROM art_book WHERE id = ?",arrayOf(selectedId.toString()))

            var image = cursor.getColumnIndex("image")
            var name = cursor.getColumnIndex("art")
            var artist_name = cursor.getColumnIndex("artist")
            var year = cursor.getColumnIndex("year")

            while (cursor.moveToNext()){
                val byteArray = cursor.getBlob(image)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
                binding.ArteditText.setText(cursor.getString(name))
                binding.NameeditText.setText(cursor.getString(artist_name))
                binding.YeareditText.setText(cursor.getString(year))
            }

            cursor.close()
            binding.imageView.setOnClickListener(){

            }
        }



    }

    fun save(view:View){

        val artName = binding.ArteditText.text.toString()
        val artistName = binding.NameeditText.text.toString()
        val artYear = binding.YeareditText.text.toString()


        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            // aldığımız görseli veri tabanına kaydetmek için veriye dönüştürme işlemi yapmalıyız

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream)
            val byteArray = outputStream.toByteArray()// görselin byte hale çevrilmiş hali

            try {
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS art_book (id INTEGER PRIMARY KEY,image BLOP, art VARCHAR, artist VARCHAR, year VARCHAR) ")
                val sqlString = "INSERT INTO art_book (image, art, artist, year) VALUES (?,?,?,?)"
                val statement = myDatabase.compileStatement(sqlString)// uygulama içinde aldığımız değerleri sql içine kaydetmek için bir araç
                statement.bindBlob(1,byteArray)
                statement.bindString(2,artName)
                statement.bindString(3,artistName)
                statement.bindString(4,artYear)
                statement.execute()

            }catch (e:Exception){
                println(e.printStackTrace())
            }

            val intent = Intent(this@DetailsActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)// bundan önce hangi aktivite varsa kapat
            startActivity(intent)

        }

    }

    fun makeSmallerBitmap(image: Bitmap,maximumsize: Int): Bitmap{
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){// landscape

            width = maximumsize
            val scaleHight = width/bitmapRatio
            height = scaleHight.toInt()

        }else{//portrait
            height = maximumsize
            val scaleWidth = height*bitmapRatio
            width = scaleWidth.toInt()

        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun selectImage(view:View){
    //galeri izni için ilk baş manifest içine yazılır

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //ANDROİD 33 VE SONRASI İÇİN BU ŞEKİLDE BİR GÜNCELLEME YAPILMALIDIR

            // ContextCompat eski sürümler için bu uygulama çalıştırıldığı zaman çalışması için
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){//kullanıcı izin vermediğinde ona nedenini göstermek için
                    //rationel - uyarı gösterdirildikten sonra izin istenir

                    Snackbar.make(view,"Give permission to gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission!",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()

                }else{
                    //request permission (izin istemek) - uyarı gösterilmeden izin istenir
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{                          //bir aksiyon almak       ,   galeriye gidip
                val intenttoGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResaultLauncher.launch(intenttoGalery)
            }
        }else{
            // ContextCompat eski sürümler için bu uygulama çalıştırıldığı zaman çalışması için
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){//kullanıcı izin vermediğinde ona nedenini göstermek için
                    //rationel - uyarı gösterdirildikten sonra izin istenir

                    Snackbar.make(view,"Give permission to gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission!",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()

                }else{
                    //request permission (izin istemek) - uyarı gösterilmeden izin istenir
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{                          //bir aksiyon almak       ,   galeriye gidip
                val intenttoGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResaultLauncher.launch(intenttoGalery)
            }
        }

    }

    fun registerLauncher(){

        // GALERİYE GİTMEK VE RESİM SEÇMEK
        activityResaultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result -> // result galeriyr gidip bir şey seçmemizin sonucunu döndürür
            if(result.resultCode == RESULT_OK){
                val resultDataIntent = result.data
                if (resultDataIntent != null){
                    // galeriden bir resim seçilmiş
                    val imageIntentData = resultDataIntent.data
                    if (imageIntentData != null){
                        try {
                            // elimizde seçilmiş olan uri (görsel) ı bitmap e çevirme işlemi yapacağımız için hataya açık olur bu yüzden try catch içinde yapılır
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver,imageIntentData) // bu 0 1 leri görselleştirmek için ancak sdk 28 ve sonrası için çalışır
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                            else{// sdk 28 den küçükse
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageIntentData)
                                binding.imageView.setImageBitmap(selectedBitmap)

                            }
                        }catch (e:Exception){
                            println(e.printStackTrace())
                        }
                    }
                }
            }
        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
            // permission granted
                val intenttoGalery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResaultLauncher.launch(intenttoGalery)

            }else{
                //permissiion denied
                Toast.makeText(this@DetailsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }

        }





    }


}