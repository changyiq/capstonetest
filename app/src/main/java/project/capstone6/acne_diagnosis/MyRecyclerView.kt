package project.capstone6.acne_diagnosis

/*
   Author: Yiqian Chang
   Student ID: 991554674
   Date: 2021.09.18
   This project it to create a grocery list using recycler view and card view.
   This is the recycler view, in this class, the layout resources are accessed using findViewById,
   and set the view to current item, then set to make a toast message when click the item.
 */
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent as Intent1

class MyRecyclerView(private val newsList: List<NewsFeeds>) :
    RecyclerView.Adapter<MyRecyclerView.MyViewHolder>() {


    class MyViewHolder(newView: View) : RecyclerView.ViewHolder(newView) {

        val imageView: ImageView = itemView.findViewById(R.id.img1)
        val newsView: TextView = itemView.findViewById(R.id.newsView)
        //val redoButton: Button = itemView.findViewById(R.id.redoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val infoView = LayoutInflater.from(parent.context).inflate(
            R.layout.news_feeds,
            parent, false
        )
        return MyViewHolder(infoView)
    }

    override fun getItemCount() = newsList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = newsList[position]

        holder.imageView.setImageResource(currentItem.imageResource)
        holder.newsView.text = currentItem.newsFeed

//        holder.redoButton.setOnClickListener {
//
//        }
    }


}

