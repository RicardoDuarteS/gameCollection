package com.wisefoxes.kotlinartbook

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameNameList = ArrayList<String>()
        val gameIdList = ArrayList<Int>()

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, gameNameList)
        listView.adapter = arrayAdapter

        //Access database
        try {

            val database = this.openOrCreateDatabase("Games", Context.MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM games", null)
            val nameIndex = cursor.getColumnIndex("gameName")
            val idIndex = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){

                gameNameList.add(cursor.getString(nameIndex))
                gameIdList.add(cursor.getInt(idIndex))
                println(gameNameList)
            }

            arrayAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e: Exception){
            e.printStackTrace()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->

            val intent = Intent(this, ImageActivity::class.java)
            //send message to the next view that the item clicked it's sending and old picture not adding a new one
            intent.putExtra("info", "old")
            intent.putExtra("id", gameIdList[position])
            startActivity(intent)
        }
    }

    //Show menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Connect menu to this class
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_art, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //Function to create activities to selected menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_art){
            val intent = Intent(applicationContext, ImageActivity::class.java)
            //send message to next view that the user wants to add a new image
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
