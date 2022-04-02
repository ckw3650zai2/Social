package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Spinner spinnerList = findViewById(R.id.uni_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.university, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerList.setAdapter(adapter);

    }

    public void register(View v) {
        mAuth = FirebaseAuth.getInstance();


        Spinner mySpinner = (Spinner) findViewById(R.id.uni_list);
        String selected_uni = mySpinner.getSelectedItem().toString();

        EditText username = (EditText) findViewById(R.id.username);
        EditText email = (EditText) findViewById(R.id.email);
        EditText password = (EditText) findViewById(R.id.password_register);
        EditText confirm_password = (EditText) findViewById(R.id.confirm_password);

        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required! ");
        }
        if (TextUtils.isEmpty(username.getText().toString())) {
            username.setError("Username is required! ");
        }
        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Password is required! ");
        }
        if (TextUtils.isEmpty(confirm_password.getText().toString())) {
            confirm_password.setError("Confirm Password is required! ");
        }

        if (!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(username.getText().toString()) &&
                !TextUtils.isEmpty(password.getText().toString()) &&
                !TextUtils.isEmpty(confirm_password.getText().toString()) &&
                !TextUtils.isEmpty(selected_uni)) {

            if (confirm_password.getText().toString().equals(password.getText().toString())) {
                String email_text = email.getText().toString();
                String password_text = password.getText().toString();
                String username_text = username.getText().toString();


                mAuth.createUserWithEmailAndPassword(email_text, password_text)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "createUserWithEmail:success");
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username_text);
                                    user.put("email", email_text);
                                    user.put("university", selected_uni);
                                    user.put("imageName", "");
                                    user.put("imageUrl", "");
                                    String uid = task.getResult().getUser().getUid();
                                    Log.d("uid", uid);

                                    db.collection("users").document(uid)
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("TAG", "DocumentSnapshot successfully written!");
                                                    Context context = getApplicationContext();
                                                    CharSequence text = "Registerd Successfully!";
                                                    int duration = Toast.LENGTH_SHORT;

                                                    Toast toast = Toast.makeText(context, text, duration);
                                                    toast.show();
                                                    Intent i = new Intent(Register.this, Login.class);
                                                    startActivity(i);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("TAG", "Error writing document", e);
                                                    Toast.makeText(Register.this, "Error! "+ e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });





                                } else {
                                    Toast.makeText(Register.this, "Error!"+task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", "createUserWithEmail:failure");

                                }
                            }
                        });

            } else {

                Context context = getApplicationContext();
                CharSequence text = "Password does not match confirm password!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }

        }


//        Intent i = new Intent(Register.this, Login.class);
//        startActivity(i);


    }

    public void back(View v) {
        Intent i = new Intent(Register.this, Login.class);
        startActivity(i);
    }
}