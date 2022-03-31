package com.example.social;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.example.social.adapter.PostAdapter;
import com.example.social.model.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeForumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeForumFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    List<PostModel> postModelList;
    PostAdapter postAdapter;
    Query query;
    FirebaseFirestore firestore;
    ListenerRegistration listenerRegistration;


    public HomeForumFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeForumFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeForumFragment newInstance(String param1, String param2) {
        HomeForumFragment fragment = new HomeForumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home_forum,container,false);

        //init fireauth
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        //recycleView
        recyclerView = view.findViewById(R.id.rv_posts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recycleview
        recyclerView.setLayoutManager(layoutManager);

        //initial post' list
        postModelList= new ArrayList<>();
        loadPosts();

        return view;
    }


    //Inflated options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);

        //searchview to search posts title and description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when press search
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user searching any letter
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }else{
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();

        if (id == R.id.action_addPost) {

        }
        return super.onOptionsItemSelected(item);
    }



    private void loadPosts() {

        query = firestore.collection("Posts").orderBy("postedTime",Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                postModelList.clear();
                for(DocumentChange doc: value.getDocumentChanges()){
                    if(doc.getType()== DocumentChange.Type.ADDED){
                        String postId = doc.getDocument().getId();
                        PostModel post = doc.getDocument().toObject(PostModel.class).withId(postId);
                        postModelList.add(post);

                        //adapter
                        postAdapter = new PostAdapter(getActivity(),postModelList);

                        //set adapter to recycleview
                        recyclerView.setAdapter(postAdapter);
                    }
                }
            }
        });

    }

    private void searchPosts(final String searchQuery){

        postModelList.clear();
        query = firestore.collection("Posts").orderBy("postedTime",Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange doc: value.getDocumentChanges()){
                    if(doc.getType()== DocumentChange.Type.ADDED){

                        String postId = doc.getDocument().getId();
                        PostModel post = doc.getDocument().toObject(PostModel.class).withId(postId);

                        if(post.getTitle().toLowerCase().contains(searchQuery)||post.getDescription().toLowerCase().contains((searchQuery))){
                            postModelList.add(post);

                        }

                        //adapter
                        postAdapter = new PostAdapter(getActivity(),postModelList);
                        postAdapter.notifyDataSetChanged();
                        //set adapter to recycleview
                        recyclerView.setAdapter(postAdapter);
                    }
                }
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//            email = user.getEmail();
//            currentUid = user.getUid();
//            name = user.getDisplayName();
//            profilepic = user.getPhotoUrl();
        } else {
//            startActivity(new Intent(this, MainActivity.class));

        }
    }



}