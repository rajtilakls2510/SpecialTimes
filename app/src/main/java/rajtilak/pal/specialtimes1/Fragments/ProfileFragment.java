package rajtilak.pal.specialtimes1.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;

import rajtilak.pal.specialtimes1.Activities.EditProfileActivity;
import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class ProfileFragment extends Fragment {

    // vars
    private static final String TAG = "ProfileFragment";

    // widgets
    CircularImageView profileImage;
    TextView profileName, profileCaption,profileDob,profileAge,profileGender,profileLive,profileSchool,profileRelation,profileEmail;
    ProgressBar initProg;

    // Firebase Classes
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    StorageReference storageRef;

     // Classes
    Context context;
    Users currentUser;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileImage = view.findViewById(R.id.ProfileImage);
        profileName = view.findViewById(R.id.ProfileName);
        profileCaption = view.findViewById(R.id.ProfileCaption);
        initProg=view.findViewById(R.id.init_progress);
        profileAge=view.findViewById(R.id.profile_age);
        profileDob=view.findViewById(R.id.profile_dob);
        profileGender=view.findViewById(R.id.profile_gender);
        profileLive=view.findViewById(R.id.profile_live);
        profileRelation=view.findViewById(R.id.profile_relation);
        profileSchool=view.findViewById(R.id.profile_school);
        profileEmail=view.findViewById(R.id.profile_email);

        currentUser=new Users();

        Log.d(TAG, "onViewCreated: Initializing FirebaseAuth, StorageReferences and DatabaseRefernces ");
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        userRef = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getCurrentUser().getUid());
        Log.d(TAG, "onViewCreated: user: "+mAuth.getCurrentUser().getUid());



    }

    private void setInformation() {

        Log.d(TAG, "setInformation: Setting Information");
        try {
            Picasso.get().load(currentUser.getProfileImageUrl()).placeholder(R.drawable.raj).into(profileImage);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.raj).into(profileImage);}

        try {
            profileCaption.setText(currentUser.getCaption());
        } catch (Exception e) {
            profileCaption.setText("No Caption");
        }
        try {
            profileName.setText(currentUser.getName());
        } catch (Exception e) {
            profileName.setText("No Name");
        }

        try {
            if(currentUser.getDob()!=0)
                profileAge.setText(String.valueOf(calculateAge(currentUser.getDob())));
            else
                profileAge.setText("N/A");
        } catch (Exception e) {
            profileAge.setText("N/A");
        }
        try {
            if(currentUser.getDob()!=0) {
                profileDob.setText(getDateString(currentUser.getDob()));
            }
            else
                profileDob.setText("Not Set Yet");
        } catch (Exception e) {
            profileDob.setText("Not Set Yet");
        }
        try {
            if(!currentUser.getGender().trim().equals("Male/Female") && !currentUser.getGender().equals("") && currentUser.getGender()!=null)
                 profileGender.setText(currentUser.getGender());
            else
                profileGender.setText("Not Set Yet");
        } catch (Exception e) {
                profileGender.setText("Not Set Yet");
        }
        try {
            if(!currentUser.getPlaceLiving().equals(""))
                profileLive.setText(currentUser.getPlaceLiving());
            else
                profileLive.setText("Not Set Yet");
        } catch (Exception e) {
            profileLive.setText("Not Set Yet");
        }
        try {
            if(!currentUser.getSchool().equals(""))
                profileSchool.setText(currentUser.getSchool());
            else
                profileSchool.setText("Not Set Yet");
        } catch (Exception e) {
            profileSchool.setText("Not Set Yet");
        }
        try {
            if(!currentUser.getRelationship().equals(""))
                profileRelation.setText(currentUser.getRelationship());
            else
                profileRelation.setText("Not Set Yet");
        } catch (Exception e) {
            profileRelation.setText("Not Set Yet");
        }
        profileEmail.setText(mAuth.getCurrentUser().getEmail());

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
                    currentUser=dataSnapshot.getValue(Users.class);
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
