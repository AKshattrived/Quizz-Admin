package com.example.quizzadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SetsActivity extends AppCompatActivity {

    private GridView gridView;//To access grid view from activity_sets.xml
    private Dialog loadingDialog;
    private GridAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private String categoryName;
    private DatabaseReference myRef;
    private List<String> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        //Initializing loadingDialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);//setting loading.xml layout as loading dialog box
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));//set background rounded_corners
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);//can be later changed if we want loading dialog box to be cancelable

        //Setting toolbar as support action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryName = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(categoryName);

        myRef = FirebaseDatabase.getInstance().getReference();

        //setting values in grid view with the help of grid adapter
        gridView = findViewById(R.id.gridview);

        sets = CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getSets();
        //To pass title and sets to adapter to pass in questions activity
        adapter = new GridAdapter(sets, getIntent().getStringExtra("title"), new GridAdapter.GridListener() {
            @Override
            public void addset() {

                loadingDialog.show();

                String id = UUID.randomUUID().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("Categories").child(getIntent().getStringExtra("key")).child("sets").child(id).setValue("SET ID").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            sets.add(id);
                            adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });



            }

            @Override
            public void onLongClick(String setId,int position) {
                new AlertDialog.Builder(SetsActivity.this, R.styleable.Layout.length)
                        .setTitle("Delete SET "+position)
                        .setMessage("Are you sure you want to delete this SET?\nAll the question in this set will be deleted")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                myRef.child("SETS").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef.child("Categories").child(CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getKey())
                                                    .child("sets").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        sets.remove(setId);
                                                        adapter.notifyDataSetChanged();
                                                    }else {
                                                        Toast.makeText(SetsActivity.this, "Something went wrong Try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingDialog.dismiss();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(SetsActivity.this, "Something went wrong Try again", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    }
                                });

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();            }
        });
        gridView.setAdapter(adapter);
    }

    //For enabled back function in support action bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            categoryAdapter.notifyDataSetChanged();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        categoryAdapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}