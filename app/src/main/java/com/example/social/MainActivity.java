package com.example.social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    Button HomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //comment
        mAuth = FirebaseAuth.getInstance();
        String id = mAuth.getCurrentUser().getUid();

        TextView tx = (TextView) findViewById(R.id.show);

        tx.setText(id);


        // start initial screen Forum
        HomeForumFragment homeForumFragment = new HomeForumFragment();
        FragmentTransaction forumFT = getSupportFragmentManager().beginTransaction();
        forumFT.add(R.id.mainLayout,homeForumFragment,"");
        forumFT.commit();

        HomeButton = findViewById(R.id.home_button);

        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeForumFragment homeForumFragment = new HomeForumFragment();
                FragmentTransaction forumFT = getSupportFragmentManager().beginTransaction();
                forumFT.replace(R.id.mainLayout,homeForumFragment,"");
                forumFT.commit();
            }
        });

        //fragment manager


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main,menu);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_addPost:{
                Intent intent = new Intent(this,CreatePost.class);
                startActivity(intent);
                
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void timetable (View v){

    }

    public void notes (View v){

    }

    public void edit (View v){

    }
}