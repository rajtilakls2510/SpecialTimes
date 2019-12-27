package rajtilak.pal.specialtimes1.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rajtilak.pal.specialtimes1.Fragments.PostsFragment;
import rajtilak.pal.specialtimes1.Fragments.ProfileFragment;
import rajtilak.pal.specialtimes1.R;

public class MainActivity extends AppCompatActivity {

    // vars
    private static final String TAG = "MainActivity";
    
    // widgets
    BottomNavigationView bot;
    
    // Firebase Classes
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    // Fragments
    Fragment postsFragment,profileFragment,active;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bot=findViewById(R.id.bottom_nav_drawer);
        Log.d(TAG, "onCreate: Initializing Firebase Auth, GoogleSignInOptions and GoogleSignInClient");
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        postsFragment=new PostsFragment();
        profileFragment=new ProfileFragment();
        fm=getSupportFragmentManager();

        Log.d(TAG, "onCreate: Checking for saved state");

            Log.d(TAG, "onCreate: No State saved");
            Log.d(TAG, "onCreate: Setting Default Fragment: Profile Fragment");


            fm.beginTransaction().add(R.id.fragment_container,profileFragment,"2").hide(profileFragment).commit();
            fm.beginTransaction().add(R.id.fragment_container,postsFragment,"1").commit();
            active=postsFragment;

            getSupportActionBar().setTitle("SpecialTimes");
            getSupportActionBar().setSubtitle("Posts");
            Log.d(TAG, "onCreate: Default Fragment set");


        bot.setOnNavigationItemSelectedListener(navListener);
        
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch(menuItem.getItemId())
            {
                case R.id.itemPosts:
                    Log.d(TAG, "onNavigationItemSelected: Posts Fragment Selected");
                    getSupportActionBar().setSubtitle("Posts");
                    fm.beginTransaction().hide(active).show(postsFragment).commit();
                    active=postsFragment;
                    return true;

                case R.id.profile:
                    Log.d(TAG, "onNavigationItemSelected: Profile Fragment Selected");
                    getSupportActionBar().setSubtitle("Profile");
                    fm.beginTransaction().hide(active).show(profileFragment).commit();
                    active=profileFragment;
                    return true;

            }

            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.logoutItem:
                Log.d(TAG, "onOptionsItemSelected: Logging Out");
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
                Log.d(TAG, "onOptionsItemSelected: Logged Out");
                return true;

            case R.id.editProfileItem:
                Log.d(TAG, "onOptionsItemSelected: Directing to Edit Profile Activity");
                Intent i=new Intent(MainActivity.this,EditProfileActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(FirebaseUser user) {
        if(user==null)
        {
            Log.d(TAG, "updateUI: No logged in user found. Redirecting to LoginActivity");
            Intent i=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(i);
        }
    }
    @Override
    public void onBackPressed() {
        if(mAuth.getCurrentUser()!=null)
        {
            Log.d(TAG, "onBackPressed: Back Pressed");
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: Cancelling all notifications");
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}