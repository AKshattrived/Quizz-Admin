 package com.example.quizzadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity {

    //Retrieves the instance from firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private Dialog loadingDialog,categoryDialog;//access loading and add category dialog
    //To access views in add category dialog
    private CircleImageView addImage;
    private EditText categoryName;
    private Button addBtn;

    private Uri image;//uri to store image from gallery

    private String downloadUrl;

    private RecyclerView recyclerView;//To access recycler view from activity_catagories.xml
    public static List<CategoryModel> list;////A category model list to carry different category and data related to it.
    private CategoryAdapter adapter;//Adapter declaration

    private String category_id;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //Setting toolbar as support action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        //Initializing loadingDialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);//setting loading.xml layout as loading dialog box
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));//set background rounded_corners
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);//can be later changed if we want loading dialog box to be cancelable

        setCategoryDialog();//A method to initialise category dialog and its components

        //Accessing recycler view and setting new vertical linear layout manager for it
        recyclerView = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        //Setting a category adapter and setting it to recycler view
        adapter = new CategoryAdapter(list, new CategoryAdapter.DeleteListener() {
            @Override
            public void onDelete(String key,int position) {

                new AlertDialog.Builder(CategoryActivity.this,R.styleable.Layout.length)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.show();
                                myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            for (String setIds : list.get(position).getSets()){
                                                myRef.child("SETS").child(setIds).removeValue();
                                            }

                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            loadingDialog.dismiss();
                                        }else {
                                            Toast.makeText(CategoryActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);//setting values of adapter in recycler view


        loadingDialog.show();
        //Query to retrieve data for categories from firebase
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Retrieve all the categories and store it in the list
                for (DataSnapshot snapshot1 : snapshot.getChildren()){

                    List<String> sets = new ArrayList<>();
                    for (DataSnapshot snapshot2 : snapshot1.child("sets").getChildren()){
                        sets.add(snapshot2.getKey());
                    }

                    list.add(new CategoryModel(snapshot1.child("name").getValue().toString(),sets,snapshot1.child("url").getValue().toString(),snapshot1.getKey()));
                }
                adapter.notifyDataSetChanged();//Refresh adapter with new retrieved data
                loadingDialog.dismiss();//dismiss loading dialog box when data retrieved
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();//dismiss loading dialog box when get error from database
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //to invoke category dialog when clicked on add btn in action bar
        if (item.getItemId() == R.id.add) {
            categoryDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCategoryDialog(){
        //Initializing category dialog
        categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.add_category_dialog);//setting add_category_dialog.xml as view of dialog
        categoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.category_bg));//set background category_bg
        categoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryDialog.setCancelable(true);

        //Initializing views in category dialog
        addImage= categoryDialog.findViewById(R.id.image);
        categoryName= categoryDialog.findViewById(R.id.categoryname);
        addBtn= categoryDialog.findViewById(R.id.add);

        //To select image from gallery
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,101);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryName.getText().toString().isEmpty() || categoryName.getText() == null){
                    categoryName.setError("Required");
                    return;
                }
                for(CategoryModel model : list){
                    if (categoryName.getText().toString().equals(model.getName())){
                        categoryName.setError("Category already exist!");
                        return;
                    }
                }
                if (image==null){
                    Toast.makeText(CategoryActivity.this, "Please select Image for new category", Toast.LENGTH_SHORT).show();
                    return;
                }
                categoryDialog.dismiss();
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==101){
            if (resultCode==RESULT_OK){
                image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    //to upload image to firebase storage
    private void uploadData(){

        loadingDialog.show();

        category_id = UUID.randomUUID().toString();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageReference = storageReference.child("categories").child(category_id);

        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadUrl = task.getResult().toString();
                            uploadCategoryName();

                        }else {
                            loadingDialog.dismiss();
                            Toast.makeText(CategoryActivity.this, "Something went wrong, Try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(CategoryActivity.this, "Something went wrong, Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //to upload category details to firebase realtime database
    private void uploadCategoryName(){

        Map<String,Object> map = new HashMap<>();
        map.put("name",categoryName.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().child("Categories").child(category_id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    list.add(new CategoryModel(categoryName.getText().toString(),new ArrayList<String>(),downloadUrl,category_id));
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CategoryActivity.this, "Something went wrong, Try again!", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }

    @Override
    protected void onStart() {
        adapter.notifyDataSetChanged();
        super.onStart();
    }
}