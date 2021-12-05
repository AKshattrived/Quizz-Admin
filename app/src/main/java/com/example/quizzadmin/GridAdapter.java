package com.example.quizzadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    public List<String> sets ;
    private String category;//to pass category in questions activity
    private GridListener listener;

    public GridAdapter(List<String> sets,String category,GridListener listener) {

        this.sets = sets;
        this.listener = listener;
        this.category = category;
    }

    @Override
    public int getCount() {
        return sets.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        /*
        To inflate set_item layout
        */
        if (convertView ==  null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item,parent,false);
        }else {
            view = convertView;
        }

        if (position==0){
            ((TextView)view.findViewById(R.id.textview)).setText("+");
        }else {
            ((TextView) view.findViewById(R.id.textview)).setText(String.valueOf(position));
        }

        //A click listener to invoke QuestionsActivity.java
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position==0){
                    listener.addset();
                }else {
                    Intent questionIntent = new Intent(parent.getContext(),QuestionsActivity.class);
                    questionIntent.putExtra("category",category);//to pass title of the category
                    questionIntent.putExtra("setId",sets.get(position-1));//to pass set
                    parent.getContext().startActivity(questionIntent);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position!=0) {
                    listener.onLongClick(sets.get(position-1),position);
                }
                return false;
            }
        });

        return view;
    }

    public interface GridListener{

        public void addset();

        void onLongClick(String setId,int position);

    }
}
