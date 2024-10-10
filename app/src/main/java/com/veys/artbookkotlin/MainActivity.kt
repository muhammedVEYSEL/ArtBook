package com.veys.artbookkotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.veys.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var artArray : ArrayList<Art>
    private lateinit var artAdapter : RecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

       artArray = ArrayList<Art>()

        artAdapter = RecycleAdapter(artArray)
        binding.recycleview.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.recycleview.adapter = artAdapter

        try {
            val myDatabase = this.openOrCreateDatabase("Art_Book", MODE_PRIVATE,null)

            val cursor = myDatabase.rawQuery("SELECT * FROM art_book",null)
            val nameIx = cursor.getColumnIndex("art")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(nameIx)
                val id = cursor.getInt(idIx)
                var myArt = Art(name,id)
                artArray.add(myArt)
            }

            artAdapter.notifyDataSetChanged() // art list güncellendiği için artadpter e haber gönderir
            cursor.close()

        }catch (e:Exception){
            println(e.printStackTrace())
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // menüyü activt main e bağlama işlemi yapılır

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // menü içinden bir item seçildiğinde ne yapılacağı yazılır

        if(item.itemId == R.id.Add_Art_item){
            val intent = Intent(this@MainActivity,DetailsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }
}