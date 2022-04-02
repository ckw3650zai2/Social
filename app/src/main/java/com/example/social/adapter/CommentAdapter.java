package com.example.social.adapter;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.R;
import com.example.social.model.CommentModel;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder>{
    Context context;
    List<CommentModel> commentModelList;

    public CommentAdapter(Context context, List<CommentModel> commentModelList) {
        this.context = context;
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind here
        View view = LayoutInflater.from(context).inflate(R.layout.singlecomment,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            //get comment data

        String cid = commentModelList.get(position).getCid();
        String uname = commentModelList.get(position).getUname();
        String comment = commentModelList.get(position).getComment();
        String timestamp = commentModelList.get(position).getTimestamp();
        String school = commentModelList.get(position).getSchool();

        //Convery timestamp
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String cTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.cdComment.setText(comment);
        holder.cdName.setText(uname);
        holder.cdTime.setText(cTime);

        //set user profile pic in comment
        try {
            String up = "";
            Picasso.get().load(up).placeholder(R.drawable.default_pic).into(holder.cdPic);

        }catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView cdPic;
        TextView cdName,cdComment,cdTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            cdPic = itemView.findViewById(R.id.cdPic);
            cdName = itemView.findViewById(R.id.cdName);
            cdComment = itemView.findViewById(R.id.cdComment);
            cdTime = itemView.findViewById(R.id.cdTime);
        }
    }
}
