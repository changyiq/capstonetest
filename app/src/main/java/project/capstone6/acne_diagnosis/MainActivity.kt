package project.capstone6.acne_diagnosis

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myNewsList = generateNewsList(4)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = MyRecyclerView(myNewsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

    }

    private fun generateNewsList(size: Int): List<NewsFeeds> {

        val list = ArrayList<NewsFeeds>()

        for (i in 0 until size) {
            val drawable = when (i % 3) {
                0 -> R.drawable.img1
                1 -> R.drawable.img2
                else -> R.drawable.img3
            }

            val newsFeed = when (i % 3) {
                0 -> getString(R.string.feed1)
                1 -> getString(R.string.feed2)
                else -> getString(R.string.feed3)
            }

            val item = NewsFeeds(drawable, newsFeed)

            list += item
        }

        return list
    }

//    fun redoAnalysis(view: View) {
//
//        val selfieIntent = Intent(this, TakeSelfie::class.java)
//        startActivity(selfieIntent)
//    }


}