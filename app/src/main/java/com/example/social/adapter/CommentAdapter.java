package com.example.social.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.PostDetails;
import com.example.social.R;
import com.example.social.model.CommentModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder>{
    Context context;
    String postId,commentId;
    List<CommentModel> commentModelList;
    String username,profilePicURL,School;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public CommentAdapter(Context context, List<CommentModel> commentModelList,String postId,String commentId) {
        this.context = context;
        this.commentModelList = commentModelList;
        this.postId = postId;
        this.commentId=commentId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind here
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        View view = LayoutInflater.from(context).inflate(R.layout.singlecomment,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {


        //get current uid for delete usage
        String tempuid = mAuth.getCurrentUser().getUid().toString();

        //get comment data
        String cid = commentModelList.get(position).getCid();
        String uname = commentModelList.get(position).getUname();
        String comment = commentModelList.get(position).getComment();
        String timestamp = commentModelList.get(position).getTimestamp();
        String school = commentModelList.get(position).getSchool();

        //retrieve user profile to the post
        firestore.collection("users").document(uname).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){

                    //get username and profile pic url
                    username = value.get("username").toString();
                    profilePicURL = value.get("imageUrl").toString();
                    School = value.get("university").toString();
                    holder.cdName.setText(username);
                    holder.cdSchool.setText(School);
                    //set user profile pic in comment
                    try {
                        String up = "";
                        Picasso.get().load(profilePicURL).placeholder(R.drawable.default_pic).resize(50,50).centerCrop().into(holder.cdPic);

                    }catch (Exception e){
                        //no user profile pic
                    }


                }else{
                    // cannot get username and profile pic url
                }
            }
        });

        //Convery timestamp
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String cTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.cdComment.setText(comment);
        holder.cdTime.setText(cTime);

        if(uname.equals(tempuid)){
            holder.cdDelete.setVisibility(View.VISIBLE);
        }else{
            holder.cdDelete.setVisibility(View.GONE);
        }

        holder.cdDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle("Delete");
                builder.setMessage("Confirm to delete this comment ?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //delete comment
                        firestore.collection("Posts/"+postId+"/Comments").document(commentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyItemRemoved(position);
                                commentModelList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, commentModelList.size());
                                Toast.makeText(context,"Comment Deleted",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //do nothing
                        dialogInterface.dismiss();
                    }
                });

                //show dialog
                builder.create().show();
            }
        });



    }


    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView cdPic;
        TextView cdName,cdComment,cdTime,cdSchool;
        ImageButton cdDelete;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            cdPic = itemView.findViewById(R.id.cdPic);
            cdName = itemView.findViewById(R.id.cdName);
            cdComment = itemView.findViewById(R.id.cdComment);
            cdTime = itemView.findViewById(R.id.cdTime);
            cdSchool = itemView.findViewById(R.id.cdSchool);
            cdDelete = itemView.findViewById(R.id.cdDelete);

        }
    }
}
