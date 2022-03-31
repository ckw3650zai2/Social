package com.example.social;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreatePost extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    StorageReference storageReference;


    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //permission array
    String[] cameraPermission;
    String[] storagePermission;

    //image pick constants
    private static final int CAMERA_PICK_CODE = 300;
    private static final int STORAGE_PICK_CODE = 400;


    //View component
    EditText etTitle, etDescription;
    ImageView etImage;
    Button uploadBtn;
    Uri image_uri = null;

    //user information
    String name;
    String email;
    String currentUid;
    Uri profilepic = null;

    //Progress Bar
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");

        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permission arrays
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);

        actionBar.setSubtitle(email);

        //check user status
        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //get user info to be included in the post
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();



        //initialize component
        etTitle = findViewById(R.id.et_pTitle);
        etDescription = findViewById(R.id.et_pDesc);
        etImage = findViewById(R.id.et_pImage);
        uploadBtn = findViewById(R.id.et_uploadBtn);

        //retrieve image from gallery
        etImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //post button listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                if(TextUtils.isEmpty(title) ){
                    Toast.makeText(CreatePost.this, "Title Should not be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(CreatePost.this, "Description Should not be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(image_uri==null){
                    
                    uploadData(title,description,"noImage");
                }else{
                    uploadData(title,description,String.valueOf(image_uri));
                }

            }
        });
    }

    private void uploadData(String title, String description, String uri) {

        progressDialog.setMessage("Publishing post...");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filepath = "Posts/"+"post_"+timestamp;

        if(!uri.equals("noImage")){
            //posting with image
            StorageReference postRef = storageReference.child("Posts/").child(filepath);
            postRef.putFile(Uri.parse(uri)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HashMap<Object,String> postMap = new HashMap<>();
                                postMap.put("postId",timestamp);
                                postMap.put("user",currentUid);
                                //postMap.put("school",);
                                postMap.put("title",title);
                                postMap.put("description",description);
                                postMap.put("image",uri.toString());
                                postMap.put("postedTime",timestamp);


                                firestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePost.this,"Posted Successfully!",Toast.LENGTH_SHORT).show();
                                            //reset view
                                            etTitle.setText("");
                                            etDescription.setText("");
                                            etImage.setImageURI(null);
                                            image_uri=null;
                                            startActivity(new Intent(CreatePost.this,MainActivity.class));
                                            finish();
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePost.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                    }else{
                        Log.i("Info","I am here");
                        Toast.makeText(CreatePost.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }else{
            //posting without image

            HashMap<Object,String> postMap = new HashMap<>();
            postMap.put("image","noImage");
            postMap.put("user",currentUid);
//            postMap.put("email",email);
//            postMap.put("profilepic",profilepic.toString());
//            postMap.put("name",name);
            postMap.put("description",description);
            postMap.put("title",title);
            postMap.put("postedTime",timestamp);
            postMap.put("postId",timestamp);

            firestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {

                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(CreatePost.this,"Posted Successfully!",Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etDescription.setText("");
                        etImage.setImageURI(null);
                        image_uri=null;
                        startActivity(new Intent(CreatePost.this,MainActivity.class));
                        finish();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(CreatePost.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void showImagePickDialog() {

        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0) {
                    //camera choose
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                }
                if (i == 1) {
                    //storage choose
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromStorage();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void pickFromStorage() {
        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,STORAGE_PICK_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desc");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,CAMERA_PICK_CODE);

    }


    private boolean checkStoragePermission() {
        //check if permission is enabled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        //check if permission is enabled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result2;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);

    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            currentUid = user.getUid();
            name = user.getDisplayName();
            profilepic = user.getPhotoUrl();
        } else {

            startActivity(new Intent(this, MainActivity.class));

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_addPost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();

        if (id == R.id.action_addPost) {

        }
        return super.onOptionsItemSelected(item);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraApproved = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageApproved = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraApproved && storageApproved) {
                        //camera & storage permission are granted
                        pickFromCamera();
                    } else {
                        //camera or storage permission denied
                        Toast.makeText(this, "Camera and Storage Permission Required!", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageApproved = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageApproved) {
                        pickFromStorage();
                    } else {
                        Toast.makeText(this, "Storage Permission Required!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //called after picking image from camera or storage
        if(resultCode == RESULT_OK){

            if(requestCode == STORAGE_PICK_CODE){
                //picked from storage
                image_uri = data.getData();
                Log.i("info",image_uri.toString());
                //set to ImageView
                etImage.setImageURI(image_uri);
            }
            else if(requestCode == CAMERA_PICK_CODE){
                //picked from camera

                etImage.setImageURI(image_uri);
            }
        }

    }
}