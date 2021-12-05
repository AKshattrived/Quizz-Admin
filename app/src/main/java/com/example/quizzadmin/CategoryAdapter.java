package com.example.quizzadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
/*
Category Adapter containing View Holder to set data in category_item.xml and activity_category.xml recyclerview
*/
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {

    private List<CategoryModel> categoryModelList;//A category model list to carry different category and data related to it.
    private DeleteListener deleteListener;

    public CategoryAdapter(List<CategoryModel> categoryModelList,DeleteListener deleteListener) {
        this.categoryModelList = categoryModelList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
        To inflate category_item layout and pass it to viewholder
        */
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        //setData call to set data
        holder.setData(categoryModelList.get(position).getUrl(),categoryModelList.get(position).getName(),categoryModelList.get(position).getKey(),position);
    }

    @Override
    public int getItemCount() {
        //number of items to be shown
        return categoryModelList.size();
    }

    /*
    Viewholder class of Recycler view
    * */
    class Viewholder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;//to access circle image view from category_item.xml
        private TextView title;//to access text view from category_item.xml
        private ImageButton delete;//to access delete button from category_item.xml

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.delete);
        }

        //It will set image at url and title of a category in recycler view and sets will be passed to setsActivity in click listener with the help of intent
        private void setData (String url, String title,final String key,final int position) {

            Glide.with(imageView.getContext()).load(url).into(imageView);//glide to load image from url to circle image view
            this.title.setText(title);

            //A click listener to invoke SetsActivity.java
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent setIntent = new Intent(itemView.getContext(),SetsActivity.class);
                    setIntent.putExtra("title",title);//to pass title of the category
                    setIntent.putExtra("position",position);//to pass number of sets of the category
                    setIntent.putExtra("key",key);//to pass key of the category
                    itemView.getContext().startActivity(setIntent);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteListener.onDelete(key,position);
                }
            });

        }
    }

    public interface DeleteListener{
        public void onDelete(String key,int position);
    }
}
