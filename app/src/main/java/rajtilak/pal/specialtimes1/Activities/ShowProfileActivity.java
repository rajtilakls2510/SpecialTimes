package rajtilak.pal.specialtimes1.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class ShowProfileActivity extends AppCompatActivity {

    // vars
    private static final String TAG = "ShowProfileActivity";
    String UserId;

    // widgets
    CircularImageView profileImage;
    TextView profileName, profileCaption,profileDob,profileAge,profileGender,profileLive,profileSchool,profileRelation;
    ProgressBar initProg;

    //Firebase Classes
    DatabaseReference userRef;
    StorageReference storageRef;

    // Other Classes
    Users showUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);
        profileImage = findViewById(R.id.ProfileImage);
        profileName = findViewById(R.id.ProfileName);
        profileCaption = findViewById(R.id.ProfileCaption);
        initProg=findViewById(R.id.init_progress);
        profileAge=findViewById(R.id.profile_age);
        profileDob=findViewById(R.id.profile_dob);
        profileGender=findViewById(R.id.profile_gender);
        profileLive=findViewById(R.id.profile_live);
        profileRelation=findViewById(R.id.profile_relation);
        profileSchool=findViewById(R.id.profile_school);
        UserId= getIntent().getStringExtra("UserId");

        showUser=new Users();

        Log.d(TAG, "onCreate: Initializing FirebaseAuth, StorageReferences and DatabaseRefernces");
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        userRef = FirebaseDatabase.getInstance().getReference("users/" + UserId);


    }


    private void setInformation() {

        Log.d(TAG, "setInformation: Setting Information");
        try {
            Picasso.get().load(showUser.getProfileImageUrl()).placeholder(R.drawable.raj).into(profileImage);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.raj).into(profileImage);}

        try {
            profileCaption.setText(showUser.getCaption());
        } catch (Exception e) {
            profileCaption.setText("No Caption");
        }
        try {
            profileName.setText(showUser.getName());
        } catch (Exception e) {
            profileName.setText("No Name");
        }

        try {
            if(showUser.getDob()!=0)
                profileAge.setText(String.valueOf(calculateAge(showUser.getDob())));
            else
                profileAge.setText("N/A");
        } catch (Exception e) {
            profileAge.setText("N/A");
        }
        try {
            if(showUser.getDob()!=0) {
                profileDob.setText(getDateString(showUser.getDob()));
            }
            else
                profileDob.setText("Not Set Yet");
        } catch (Exception e) {
            profileDob.setText("Not Set Yet");
        }
        try {
            if(!showUser.getGender().trim().equals("Male/Female") && !showUser.getGender().equals("") && showUser.getGender()!=null)
                profileGender.setText(showUser.getGender());
            else
                profileGender.setText("Not Set Yet");
        } catch (Exception e) {
            profileGender.setText("Not Set Yet");
        }
        try {
            if(!showUser.getPlaceLiving().equals(""))
                profileLive.setText(showUser.getPlaceLiving());
            else
                profileLive.setText("Not Set Yet");
        } catch (Exception e) {
            profileLive.setText("Not Set Yet");
        }
        try {
            if(!showUser.getSchool().equals(""))
                profileSchool.setText(showUser.getSchool());
            else
                profileSchool.setText("Not Set Yet");
        } catch (Exception e) {
            profileSchool.setText("Not Set Yet");
        }
        try {
            if(!showUser.getRelationship().equals(""))
                profileRelation.setText(showUser.getRelationship());
            else
                profileRelation.setText("Not Set Yet");
        } catch (Exception e) {
            profileRelation.setText("Not Set Yet");
        }


    }


    @Override
    public void onResume() {
        initProg.setVisibility(View.VISIBLE);

        Log.d(TAG, "onResume: Retrieving User Information");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try
                {
                    showUser=dataSnapshot.getValue(Users.class);
                    Log.d(TAG, "onDataChange: User Info Retrieved");
                    setInformation();
                }
                catch(Exception e){
                    Log.d(TAG, "onDataChange: No User Info found");
                    setInformation();
                }
                initProg.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
        super.onResume();
    }


    private int calculateAge(long dateOfBirth)
    {
        int age;
        Log.d(TAG, "calculateAge: Calculating Age");
        Calendar dob=Calendar.getInstance();
        dob.setTimeInMillis(dateOfBirth);
        Calendar today=Calendar.getInstance();
        age=today.get(Calendar.YEAR)-dob.get(Calendar.YEAR);
        if(today.get(Calendar.MONTH)==dob.get(Calendar.MONTH))
        {
            if(today.get(Calendar.DAY_OF_MONTH)<dob.get(Calendar.DAY_OF_MONTH))
                age--;
        }
        else if(today.get(Calendar.MONTH)<dob.get(Calendar.MONTH))
            age--;
        Log.d(TAG, "calculateAge: Age Calculated: "+age);
        return age;
    }

    private String getDateString(long date)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
}
