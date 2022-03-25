package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupHyperlink();

    }
    private void setupHyperlink() {
        TextView linkTextView = (TextView) findViewById(R.id.forgot);
        //linkTextView.setTextColor(Color.BLUE);
        linkTextView.setTextColor(Color.BLUE);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        linkTextView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent i = new Intent(Login.this,ForgotPassword.class);
                startActivity(i);

            }
        });
    }

    public void login(View v){
        mAuth = FirebaseAuth.getInstance();

        EditText email = (EditText) findViewById(R.id.email);
        EditText password = (EditText) findViewById(R.id.password);

        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required! ");
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Password is required! ");
        }

        if (!TextUtils.isEmpty(email.getText().toString()) && !TextUtils.isEmpty(password.getText().toString())){
            String email_text = email.getText().toString();
            String password_text = password.getText().toString();

            mAuth.signInWithEmailAndPassword(email_text, password_text)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Context context = getApplicationContext();
                                CharSequence text = "Login Successfully!";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                Intent i = new Intent(Login.this, MainActivity.class);
                                startActivity(i);

                            } else {
                                Toast.makeText(Login.this, "Error! "+task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                Log.d("TAG", "createUserWithEmail:failure");
                            }
                        }
                    });


        }



    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void register_page (View v){
        Intent i = new Intent(Login.this, Register.class);
        startActivity(i);
    }
}