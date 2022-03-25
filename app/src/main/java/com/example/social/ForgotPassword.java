package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    public void reset(View v) {

        mAuth = FirebaseAuth.getInstance();


        EditText email = (EditText) findViewById(R.id.email);

        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required! ");
        }
        if (!TextUtils.isEmpty(email.getText().toString())) {
            String email_text = email.getText().toString();

            mAuth.getInstance().sendPasswordResetEmail(email_text)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "Email sent.");
                                Context context = getApplicationContext();
                                CharSequence text = "Reset Email Sent !";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                Intent i = new Intent(ForgotPassword.this, Login.class);
                                startActivity(i);
                            }
                            else{
                                Toast.makeText(ForgotPassword.this, "Error!"+task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }


    }

    public void back(View v) {
        Intent i = new Intent(ForgotPassword.this, Login.class);
        startActivity(i);

    }

}