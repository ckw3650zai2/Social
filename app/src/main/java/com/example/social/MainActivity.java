package com.example.social;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //comment
        mAuth = FirebaseAuth.getInstance();
        String id = mAuth.getCurrentUser().getUid();

        TextView tx = (TextView) findViewById(R.id.show);

        tx.setText(id);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.menu,menu);
        menu.add("Logout")
                .setIntent(new Intent(this, Login.class));
        menu.add("Exit")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //finish();
                        finishAffinity();

                        return false;

                    }
                });
        return true;
    }

    public void timetable (View v){

    }

    public void notes (View v){

    }

    public void edit (View v){

    }
}