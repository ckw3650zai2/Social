package com.example.social;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        String id = mAuth.getCurrentUser().getUid();



        EditText username = (EditText) findViewById(R.id.username);

        EditText email = (EditText) findViewById(R.id.email);
        Spinner mySpinner = (Spinner) findViewById(R.id.uni_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.university, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);

        DocumentReference docRef = db.collection("users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String current_email = "";
                    String current_username = "";
                    String current_uni = "";
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> currentUser = new HashMap<>();
                        currentUser=document.getData();
                        for(Map.Entry map  :  currentUser.entrySet() ) {
                            System.out.println(map.getKey()+" "+map.getValue());
                            if(map.getKey().equals("email")){
                                current_email = map.getValue().toString();
                            }
                            if(map.getKey().equals("username")){
                                current_username = map.getValue().toString();
                            }
                            if(map.getKey().equals("university")){
                                current_uni = map.getValue().toString();
                            }
                        }

                        username.setText(current_username);
                        email.setText(current_email);

                        if (current_uni != null) {
                            int spinnerPosition = adapter.getPosition(current_uni);
                            mySpinner.setSelection(spinnerPosition);
                        }


                        Log.d("TAG", "DocumentSnapshot data: " + currentUser);
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });


    }

    public void onUpload (View v){

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView = findViewById(R.id.profileImage);
        if(requestCode ==2 && resultCode == RESULT_OK && data!=null){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

            StorageReference fileRef = reference.child("/Users").child(System.currentTimeMillis()+ "."+ getFileExtension(imageUri));

            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("TAG", uri.toString());
                            // store image to user
                            Map<String, Object> user_url = new HashMap<>();
                            mAuth = FirebaseAuth.getInstance();
                            String id = mAuth.getCurrentUser().getUid();
                            user_url.put("imageUrl",uri.toString());
                            db.collection("users").document(id)
                                    .update(user_url);

                            Toast.makeText(EditProfile.this, "Upload Successfully "+uri.toString(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, "Error! "+e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(cr.getType(mUri));
    }

    public void onEdit(View v){
        Spinner mySpinner = (Spinner) findViewById(R.id.uni_list);
        String selected_uni = mySpinner.getSelectedItem().toString();

        EditText username = (EditText) findViewById(R.id.username);
        EditText email = (EditText) findViewById(R.id.email);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required! ");
        }
        if (TextUtils.isEmpty(username.getText().toString())) {
            username.setError("Username is required! ");
        }

        if (!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(username.getText().toString()) && !TextUtils.isEmpty(selected_uni)) {
            Map<String, Object> user_edit = new HashMap<>();
            mAuth = FirebaseAuth.getInstance();
            String id = mAuth.getCurrentUser().getUid();

            if(email.getText().toString().equals(user.getEmail())){

                user_edit.put("username", username.getText().toString());
                user_edit.put("university", selected_uni);

                db.collection("users").document(id)
                        .update(user_edit);

                Context context = getApplicationContext();
                CharSequence text = "Profile Updated Successfully!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Intent i = new Intent(this,MainActivity.class);
                startActivity(i);

            }
            else{
                user.updateEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user_edit.put("username", username.getText().toString());
                                    user_edit.put("email", email.getText().toString());
                                    user_edit.put("university", selected_uni);
                                    db.collection("users").document(id)
                                            .update(user_edit);

                                    Context context = getApplicationContext();
                                    CharSequence text = "Profile Updated Successfully!";
                                    int duration = Toast.LENGTH_SHORT;

                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    Intent i = new Intent(EditProfile.this,MainActivity.class);
                                    startActivity(i);

                                    Log.d("TAG", "User email address updated.");
                                }
                                else{
                                    Toast.makeText(EditProfile.this, "Error! "+task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        }


    }
}