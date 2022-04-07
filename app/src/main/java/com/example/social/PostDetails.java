package com.example.social;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social.adapter.CommentAdapter;
import com.example.social.adapter.PostAdapter;
import com.example.social.model.CommentModel;
import com.example.social.model.PostModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostDetails extends AppCompatActivity {

    //Retrive post and user details
    String currentuserid, uname,users,cuser,cprofilePicURL,cschool, profilePicURL,email,postId,school,pTitle,pDesc,pImage,pTime;


    //View Image

    ImageView profilePic,postImage;
    TextView username, postedTime, postTitle,postDesc,postLikes,postComment,postSchool;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;

    //add comment view
    EditText commentEdit;
    ImageButton sendBtn;
    ImageView commentPic;


    //init firebase
    FirebaseFirestore firestore;

    //Progress bar
    ProgressDialog progressDialog;

    //Recycle view
    RecyclerView recyclerView;

    List<CommentModel> commentModelList;
    CommentAdapter commentAdapter;

    //for commentid pass
    String CommentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        firestore = FirebaseFirestore.getInstance();

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //get id of post
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //default get view

        profilePic = findViewById(R.id.post_profilepictv);
        postImage = findViewById(R.id.post_imagetv);
        username = findViewById(R.id.post_usernametv);
        postSchool = findViewById(R.id.post_schooltv);
        postedTime = findViewById(R.id.post_timetv);
        postTitle = findViewById(R.id.post_titletv);
        postDesc = findViewById(R.id.post_desctv);
        postLikes = findViewById(R.id.post_likestv);
        postComment = findViewById(R.id.post_commentstv);
        likeBtn = findViewById(R.id.like_btntv);
        shareBtn = findViewById(R.id.share_btntv);
        profileLayout = findViewById(R.id.profileLayout);

        commentEdit = findViewById(R.id.comment_et);
        sendBtn = findViewById(R.id.sendBtn);
        commentPic = findViewById(R.id.commentPic);

        recyclerView = findViewById(R.id.rv_comments);

        checkUserStatus();
        loadPostInfo();
        checkLikeImage();
        loadCommentInfo();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postCommentInfo();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeInfo();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareInfo();
            }
        });


    }

    private void loadCommentInfo() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentModelList = new ArrayList<>();

        Query query = firestore.collection("Posts/"+postId+"/Comments").orderBy("timestamp",Query.Direction.ASCENDING);
        commentModelList.clear();
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange doc: value.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        CommentId = doc.getDocument().getId();
                        CommentModel comment = doc.getDocument().toObject(CommentModel.class);

                        commentModelList.add(comment);
                        //adapter
                        commentAdapter = new CommentAdapter(getApplicationContext(),commentModelList,postId,CommentId);
                        commentAdapter.notifyDataSetChanged();
                        //set adapter to recycleview
                        recyclerView.setAdapter(commentAdapter);
                    }
                }
            }
        });

    }


    private void shareText(String title, String desc) {

        String shareBody = title+"\n"+desc;

        //share intent
        Intent stIntent = new Intent(Intent.ACTION_SEND);
        stIntent.setType("text/plain");
        stIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Title ");
        stIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(stIntent,"Share via"));
    }

    private void shareImageText(String title, String desc, Bitmap bitmap) {
        String shareBody = title+"\n"+desc;

        Uri uri = saveImageToShare(bitmap);

        //share intent;

        Intent sitIntent = new Intent(Intent.ACTION_SEND);
        sitIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sitIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sitIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Title");
        sitIntent.setType("image/png");
        startActivity(Intent.createChooser(sitIntent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(this.getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdirs(); //create if new
            File file =  new File(imageFolder,"shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(this,"com.example.social.fileprovider",file);

        }catch(Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();

        }
        return uri;
    }

    private void shareInfo() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable)postImage.getDrawable();

        if(bitmapDrawable==null){
            //post without image

            shareText(pTitle,pDesc);

        }else{

            Bitmap bitmap = bitmapDrawable.getBitmap();
            shareImageText(pTitle,pDesc,bitmap);

        }
        Toast.makeText(PostDetails.this,"Shared.",Toast.LENGTH_SHORT).show();
    }

    private void likeInfo() {

        firestore.collection("Posts/"+postId+"/Likes").document(currentuserid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists()){
                    Map<String,Object> likesMap = new HashMap<>();
                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                    firestore.collection("Posts/"+postId+"/Likes").document(currentuserid).set(likesMap);
                }else{

                    firestore.collection("Posts/"+postId+"/Likes").document(currentuserid).delete();
                }
            }
        });
//
        checkLikeImage();
    }

    private void checkLikeImage(){
        firestore.collection("Posts/"+postId+"/Likes").document(currentuserid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error== null){
                    if(value.exists()){
                        //user liked this post
                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                        likeBtn.setText("Liked");
                    }else{
                        //user didnt like this psot
                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                        likeBtn.setText("Like");
                    }
                }
            }
        });
    }
    private void postCommentInfo() {

        String timestamp = String.valueOf(System.currentTimeMillis());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending comment...");

        //get data from comment edit text
        String comment = commentEdit.getText().toString().trim();

        //validate comment
        if(!comment.isEmpty()){

            Map<String,Object> commentMap = new HashMap<>();
            commentMap.put("cid",timestamp);
            commentMap.put("comment",comment);
            commentMap.put("timestamp", timestamp);
            commentMap.put("uname",currentuserid);
            commentMap.put("school",cschool);

            firestore.collection("Posts/"+postId+"/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PostDetails.this,"Comment sent!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        commentEdit.setText("");
                        updateCommentCount();
                    }else{
                        Toast.makeText(PostDetails.this,"Empty comment not allowed.",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }else{
            Toast.makeText(this,"Comment is empty.",Toast.LENGTH_SHORT).show();
            return;
        }


    }

    private void updateCommentCount() {
       Query query = firestore.collection("Posts/"+postId+"/Comments");

       query.addSnapshotListener(new EventListener<QuerySnapshot>() {
           @Override
           public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
               for(DocumentChange doc: value.getDocumentChanges()) {
                   if (doc.getType() == DocumentChange.Type.ADDED) {

                       if (!value.isEmpty()) {

                           int count = value.size();

                           if (count > 1) {
                               postComment.setText(String.valueOf(count) + " comments");
                           } else {
                               postComment.setText("");
                           }

                       } else {
                           postComment.setText("");
                       }


                   }
               }
           }
       });
    }


    private void loadPostInfo() {

        DocumentReference docRef = firestore.collection("Posts").document(postId);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if (snapshot != null && snapshot.exists()) {

                    Log.i("test", snapshot.getData().toString());
                    Log.i("test", snapshot.get("description").toString());
//                    PostModel post = (PostModel) snapshot.getData();

                    pTitle = snapshot.get("title").toString();
                    pDesc = snapshot.get("description").toString();
                    pImage = snapshot.get("image").toString();
                    uname = snapshot.get("user").toString();
                    pTime = snapshot.get("postedTime").toString();

                }

                    //retrieve user profile to the post
                    handleUserProfile(uname);

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTime));

                    String Time = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


                    postTitle.setText(pTitle);
                    postDesc.setText(pDesc);
                    postedTime.setText(Time);


                    //For image handle visibility

                    if(pImage.equals("noImage")){
                        //hide imageview
                        postImage.setVisibility(View.GONE);

                    }else{
                        try {
                            Picasso.get().load(pImage).into(postImage);

                        }catch (Exception e){

                        }
                    }

            }

        });




        Query query2 = firestore.collection("Posts/"+postId+"/Likes");
        query2.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    if(!value.isEmpty()){

                       String totalLike = String.valueOf(value.size()) ;

                        if(value.size()>1) {
                            postLikes.setText(totalLike+" Likes");
                        }else{
                            postLikes.setText(totalLike+" Like");
                        }

                    }else{
                       postLikes.setText("Be the First to Like");

                    }
                }else{

                }
            }
        });

        Query query = firestore.collection("Posts/"+postId+"/Comments");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){

                        if (!value.isEmpty()) {

                            int count = value.size();

                            if (count > 1) {
                                postComment.setText(String.valueOf(count) + " comments");
                            } else {
                                postComment.setText("");
                            }

                        } else {
                            postComment.setText("");
                        }


                    }

            }
        });


    }

    private void handleUserProfile(String userid) {

        firestore.collection("users").document(userid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    //get username and profile pic url
                    users = value.get("username").toString();
                    school = value.get("university").toString();
                    profilePicURL = value.get("imageUrl").toString();
                    username.setText(users);
                    postSchool.setText(school);

                    //set user comment profile pic
                    String up = "";
                    try {

                        Picasso.get().load(profilePicURL).placeholder(R.drawable.default_pic).resize(50,50).centerCrop().into(profilePic);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.default_pic).into(profilePic);
                    }

                }else{
                    // cannot get username and profile pic url
                }
            }
        });

        firestore.collection("users").document(currentuserid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    //get username and profile pic url
                    cuser = value.get("username").toString();
                    cschool = value.get("university").toString();
                    cprofilePicURL = value.get("imageUrl").toString();


                    //set user comment profile pic
                    String up = "";
                    try {
                        Picasso.get().load(cprofilePicURL).placeholder(R.drawable.default_pic).resize(50,50).centerCrop().into(commentPic);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.default_pic).into(commentPic);
                    }

                }else{
                    // cannot get username and profile pic url
                }
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            email = user.getEmail();
            currentuserid = user.getUid();
//            name = user.getDisplayName();
//            profilepic = user.getPhotoUrl();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}