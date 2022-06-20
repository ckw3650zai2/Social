package com.example.social.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.PostDetails;
import com.example.social.R;
import com.example.social.model.PostModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Myholder>{

    Context context;
    List<PostModel> postModelList;
    String currentUid, username,school,profilePicURL;
    int count=0;


    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;


    boolean mProcessLike = false;


    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;

    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout singlepost.xml

        View view = LayoutInflater.from(context).inflate(R.layout.singlepost,parent,false);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int i) {
        String postId = postModelList.get(i).PostID;
        String user = postModelList.get(i).getUser();
        String title = postModelList.get(i).getTitle();
        String desc = postModelList.get(i).getDescription();
        String image = postModelList.get(i).getImage();
        String postedTime = postModelList.get(i).getPostedTime();
        String likes = postModelList.get(i).getPostLike();
        String comment = postModelList.get(i).getPostComment();



        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(postedTime));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //retrieve user profile to the post
        firestore.collection("users").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){

                    //get username and profile pic url
                    username = value.get("username").toString();
                    school = value.get("university").toString();
                    profilePicURL = value.get("imageUrl").toString();
                    holder.post_username.setText(username);
                    holder.post_school.setText(school);
                    //set user profile pic
                    try {

                        Picasso.get().load(profilePicURL).placeholder(R.drawable.default_pic).resize(50,50).centerCrop().into(holder.post_profilepic);

                    }catch (Exception e){

                    }
                }else{
                    // cannot get username and profile pic url
                }
            }
        });

        //set data
        holder.post_title.setText(title);
        holder.post_desc.setText(desc);


        holder.post_time.setText(pTime);
        holder.post_likes.setText(likes+ " Likes");
        holder.post_comments.setText(comment+ " comments");




        //set post image
        if(image.equals("noImage")){
            //hide imageview
            holder.post_image.setVisibility(View.GONE);

        }else{
            try {
                Picasso.get().load(image).resize(1200,600).centerCrop().into(holder.post_image);

            }catch (Exception e){

            }
        }

        //like button
        currentUid = mAuth.getCurrentUser().getUid();

        //handle button click
        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firestore.collection("Posts/"+postId+"/Likes").document(currentUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("Posts/"+postId+"/Likes").document(currentUid).set(likesMap);
                        }else{

                            firestore.collection("Posts/"+postId+"/Likes").document(currentUid).delete();
                        }
                    }
                });
//                Toast.makeText(context,"Liked.",Toast.LENGTH_SHORT).show();
            }
        });

        firestore.collection("Posts/"+postId+"/Likes").document(currentUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error== null){

                    if(value.exists()){

                        //user liked this post
                        holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                        holder.like_btn.setText("Liked");
                    }else{
                        //user didnt like this psot
                        holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                        holder.like_btn.setText("Like");


                    }
                }
            }
        });

        firestore.collection("Posts/"+postId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    if(!value.isEmpty()){

                       count = value.size();

                       if(count>1) {
                           holder.post_likes.setText(String.valueOf(count)+" Likes");
                       }else{
                           holder.post_likes.setText(String.valueOf(count)+" Like");
                       }

                    }else{
                        holder.post_likes.setText("Be the First to Like");

                    }
                }else{

                }
            }
        });

        firestore.collection("Posts/"+postId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    if(!value.isEmpty()){

                      int count2= value.size();

                        if(count2>1) {
                            holder.post_comments.setText(String.valueOf(count2)+" comments");
                        }else{
                            holder.post_comments.setText(String.valueOf(count2)+" comment");
                        }

                    }else{
                        holder.post_comments.setText(" ");

                    }
                }else{

                }
            }
        });


        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetails.class);
                intent.putExtra("postId",postId);
                context.startActivity(intent);
            }
        });

        holder.share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.post_image.getDrawable();

                if(bitmapDrawable==null){
                    //post without image

                    shareText(title,desc);

                }else{

                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageText(title,desc,bitmap);

                }
                Toast.makeText(context,"Shared.",Toast.LENGTH_SHORT).show();


            }
        });
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
        context.startActivity(Intent.createChooser(sitIntent,"Share via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdirs(); //create if new
            File file =  new File(imageFolder,"shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.example.social.fileprovider",file);

        }catch(Exception e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();

        }
        return uri;
    }

    private void shareText(String title, String desc) {

        String shareBody = title+"\n"+desc;

        //share intent
        Intent stIntent = new Intent(Intent.ACTION_SEND);
        stIntent.setType("text/plain");
        stIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Title ");
        stIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(stIntent,"Share via"));
    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }
//view holder

    class Myholder extends RecyclerView.ViewHolder{

        //View from singlepost.xml

        ImageView post_profilepic, post_image;
        TextView post_username, post_school,post_time,post_title,post_desc,post_likes,post_comments;
        Button like_btn,comment_btn,share_btn;

        public Myholder(@NonNull View itemView){
            super(itemView);

            post_profilepic = itemView.findViewById(R.id.post_profilepic);
            post_image= itemView.findViewById(R.id.post_image);
            post_username = itemView.findViewById(R.id.post_username);
            post_school = itemView.findViewById(R.id.post_school);
            post_time = itemView.findViewById(R.id.post_time);
            post_title = itemView.findViewById(R.id.post_title);
            post_desc = itemView.findViewById(R.id.post_desc);
            post_likes = itemView.findViewById(R.id.post_likes);
            post_comments = itemView.findViewById(R.id.post_comments);
            like_btn = itemView.findViewById(R.id.like_btn);
            comment_btn = itemView.findViewById(R.id.comment_btn);
            share_btn = itemView.findViewById(R.id.share_btn);

        }
    }
}
