package rajtilak.pal.specialtimes1.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.cert.CollectionCertStoreParameters;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import rajtilak.pal.specialtimes1.Adapters.RecyclerAdapter;
import rajtilak.pal.specialtimes1.Activities.NewPostActivity;
import rajtilak.pal.specialtimes1.models.Posts;
import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class PostsFragment extends Fragment{

    // vars
    private static final String TAG = "PostsFragment";
    private Boolean isScrolling=false;
    private int currentItems,totalItems,scrolledItems;

    // widgets
    FloatingActionButton newPostBtn;
    ProgressBar retrievePosts;
    Context context;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    // Firebase Classes
    DatabaseReference userRef,postRef;
    FirebaseAuth mAuth;

    // Classes
    ArrayList<Posts> items,sortedItems;
    ArrayList<Users> users,sortedUsers;
    RecyclerAdapter recyclerAdapter;

    public PostsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context=container.getContext();
        Log.d(TAG, "onCreateView: Inflating Posts Fragment");
        items=new ArrayList<>();
        users=new ArrayList<>();
        sortedItems=new ArrayList<>();
        sortedUsers=new ArrayList<>();
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newPostBtn=view.findViewById(R.id.new_post_button);
        recyclerView=view.findViewById(R.id.recycler_post);
        retrievePosts=view.findViewById(R.id.retrieve_posts);
        retrievePosts.setVisibility(View.VISIBLE);

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(context, NewPostActivity.class);
                Log.d(TAG, "onClick: Opening New post Activity");
                startActivity(i);
            }
        });


        // Initializing user and post references of FirebaseDatabase
        userRef= FirebaseDatabase.getInstance().getReference("users");
        postRef=FirebaseDatabase.getInstance().getReference("posts");
        mAuth=FirebaseAuth.getInstance();


        // Setting up recyclerview
        layoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setItemViewCacheSize(100);


        Log.d(TAG, "onViewCreated: Initializing Adapter");
        recyclerAdapter=new RecyclerAdapter(items,users);
        Log.d(TAG, "onViewCreated: Adapter Initialized");
        recyclerView.setAdapter(recyclerAdapter);
        Log.d(TAG, "onViewCreated: RecyclerView Set");
        displayPosts(System.currentTimeMillis(),new PostsRetrieveCallback() {
            @Override
            public void retrievePost() {
                sortLists();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Log.d(TAG, "onScrollStateChanged: User is Scrolling");
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems=layoutManager.getChildCount();
                totalItems=layoutManager.getItemCount();
                scrolledItems=layoutManager.findFirstVisibleItemPosition();
                if(isScrolling && (currentItems+scrolledItems==totalItems))
                {
                    isScrolling=false;
                    Log.d(TAG, "onScrolled: Retrieving more posts");
                    displayPosts(items.get(items.size() - 1).getPostDate(), new PostsRetrieveCallback() {
                        @Override
                        public void retrievePost() {
                            sortLists();
                        }
                    });
                    Log.d(TAG, "onScrolled: More Posts retrieved");
                }
            }
        });

    }



    private void sortLists() {

        Log.d(TAG, "sortLists: Sorting Lists");
        sortedUsers=new ArrayList<>();
        sortedItems=new ArrayList<>(items);
        Log.d(TAG, "sortLists: Unsorted items: ");
        for(int i=0;i<sortedItems.size();i++)
            Log.d(TAG, "sortLists: "+sortedItems.get(i).getPostId());
        bubbleSort(sortedItems);

        for(int i=0;i<sortedItems.size();i++)
        {
            sortedUsers.add(users.get(items.indexOf(sortedItems.get(i))));
        }
        items.clear();
        users.clear();
        for(int i=0;i<sortedUsers.size();i++)
        {
            items.add(sortedItems.get(i));
            users.add(sortedUsers.get(i));
        }
        Log.d(TAG, "sortLists: Sorted items: ");
        for(int i=0;i<sortedItems.size();i++)
            Log.d(TAG, "sortLists: "+items.get(i).getPostId());

        Log.d(TAG, "sortLists: Notifying Data set changed");
        recyclerAdapter.notifyDataSetChanged();
        retrievePosts.setVisibility(View.GONE);

    }

    private void bubbleSort(ArrayList<Posts> sortedItems)
    {
        Posts temp;
        for (int i=0;i<sortedItems.size()-1;i++)
        {
            for(int j=0;j<sortedItems.size()-i-1;j++)
            {
                if(sortedItems.get(j).getPostDate()<sortedItems.get(j+1).getPostDate())
                {
                    temp=sortedItems.get(j+1);
                    sortedItems.remove(temp);
                    sortedItems.add(j,temp);
                }
            }
        }
    }



    private void getUsers(final Posts post, final UsersRetrieveCallback usersRetriveCallback)
    {
        userRef.child(post.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user1 =dataSnapshot.getValue(Users.class);
                if(mAuth.getCurrentUser().getUid().equals(dataSnapshot.getKey()) || post.getPrivacy()==0) {
                    usersRetriveCallback.retrieveUsers(user1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayPosts(long endTime,final PostsRetrieveCallback postsRetrieveTask)
    {
        Query query=postRef.orderByKey().endAt(endTime+"").limitToLast(20);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    final Posts post1=ds.getValue(Posts.class);
                    getUsers(post1, new UsersRetrieveCallback() {
                        @Override
                        public void retrieveUsers(Users user) {

                            if(!listContains(items,post1))
                            {
                                Log.d(TAG, "retrieveUsers: user: "+user.getName());
                                users.add(0,user);
                                items.add(0,post1);
                            }
                            postsRetrieveTask.retrievePost();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean listContains(ArrayList<Posts> item,Posts post1)
    {
        for(int i=0;i<item.size();i++)
        {
            if(item.get(i).getPostId().equals(post1.getPostId()))
                return true;

        }
        return false;

    }



    // Callback Interfaces
    public interface PostsRetrieveCallback
    {
        void retrievePost();
    }
    public interface UsersRetrieveCallback{
        void retrieveUsers(Users user);
    }


}



