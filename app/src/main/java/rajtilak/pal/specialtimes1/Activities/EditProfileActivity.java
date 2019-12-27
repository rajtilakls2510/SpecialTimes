package rajtilak.pal.specialtimes1.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import rajtilak.pal.specialtimes1.Others.App;
import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class EditProfileActivity extends AppCompatActivity {

    // vars
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri, profileImageUrl;
    private static final String TAG = "EditProfileActivity";
    private long dateOfBirth;
    private boolean invalidDateFlag = false;


    // widgets
    ImageView uploadImage, deleteGender;
    Button choiceBtn;
    ProgressBar uploadProg;
    EditText changeName, changeCaption, changeDob, changeLiving, changeSchool, changeRelation;
    AutoCompleteTextView changeGender;

    // Firebase Classes
    FirebaseAuth mAuth;
    DatabaseReference editRef;
    StorageReference storageRef;

    // Classes
    Users user;
    Calendar c;
    NotificationManagerCompat notificationManager;
    DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        uploadImage = findViewById(R.id.upload_image);
        choiceBtn = findViewById(R.id.choice_btn);
        changeName = findViewById(R.id.change_name);
        changeCaption = findViewById(R.id.change_caption);
        uploadProg = findViewById(R.id.upload_prog);
        changeDob = findViewById(R.id.change_dob);
        changeGender = findViewById(R.id.change_gender);
        deleteGender = findViewById(R.id.del_gender);
        changeLiving = findViewById(R.id.change_location);
        changeSchool = findViewById(R.id.change_school);
        changeRelation = findViewById(R.id.change_relation);

        user = new Users();
        notificationManager = NotificationManagerCompat.from(this);
        c = Calendar.getInstance();
        dateOfBirth = 0;
        df=new DecimalFormat("0.00");
        invalidDateFlag = false;
        Log.d(TAG, "onViewCreated: Initializing FirebaseAuth, StorageReferences and DatabaseRefernces ");
        mAuth = FirebaseAuth.getInstance();
        editRef = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        uploadProg.setVisibility(View.GONE);

        retrieveUserInfo();
        choiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };

        changeDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditProfileActivity.this, datePicker, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{"Male", "Female", "Others"});
        changeGender.setAdapter(arrayAdapter);
        changeGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                changeGender.showDropDown();
                changeGender.setText(String.valueOf(adapterView.getItemAtPosition(position)));
                deleteGender.setAlpha(0.8f);
            }
        });
        changeGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeGender.showDropDown();
            }
        });

        deleteGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeGender.setText(null);
                deleteGender.setAlpha(0.2f);
            }
        });
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        changeDob.setText(sdf.format(c.getTime()));

        dateOfBirth = c.getTimeInMillis();
        if (calculateAge(dateOfBirth) <= 0) {
            invalidDateFlag = true;
            Toast.makeText(this, "Invalid Date. Cannot Accept this date", Toast.LENGTH_SHORT).show();
        } else
            invalidDateFlag = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_save_edit:
                try {
                    if (!changeName.getText().toString().equals("") && !changeCaption.getText().toString().equals("") && (mImageUri != null || user.getProfileImageUrl() != null) && !changeGender.getText().toString().equals("") && !invalidDateFlag && dateOfBirth != 0) {
                        uploadProg.setVisibility(View.VISIBLE);
                        uploadFile();
                    } else {
                        Log.d(TAG, "onOptionsItemSelected: Empty Fields Found");
                        Toast.makeText(this, "Fields are Empty. Fill up Empty fields", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onOptionsItemSelected: Catch Empty Fields Found");
                    Toast.makeText(this, "Fields are Empty. Fill up Empty fields", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private Users setUser(String name, String caption, String proUrl, long date, String gender, String location, String school, String relation) {
        Users users1 = new Users();

        users1.setName(name);
        users1.setCaption(caption);
        users1.setProfileImageUrl(proUrl);
        users1.setDob(date);
        users1.setGender(gender);
        users1.setPlaceLiving(location);
        users1.setSchool(school);
        users1.setRelationship(relation);
        return users1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: Getting Image Uri");
            mImageUri = data.getData();
        }
        if (mImageUri != null) {
            Log.d(TAG, "onActivityResult: Setting selected image");
            Picasso.get().load(mImageUri).into(uploadImage);
        } else {
            Log.d(TAG, "onActivityResult: Setting Profile Image");
            Picasso.get().load(user.getProfileImageUrl()).into(uploadImage);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

    private void uploadFile() {
        final StorageReference fileReference;
        Intent activityIntent=new Intent(this,EditProfileActivity.class);
        final PendingIntent contentIntent=PendingIntent.getActivity(this,1,activityIntent,0);

        if (mImageUri != null) {
            Log.d(TAG, "uploadFile: Image Uri found: "+mImageUri);


            byte[] compressed_image=compressImage();

            fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
            Log.d(TAG, "uploadFile: Uploading Image");
            UploadTask uploadTask = fileReference.putBytes(compressed_image);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                        throw task.getException();
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                                .setContentTitle("Uploading Profile Information")
                                .setContentText("Upload Completed Successfully")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(contentIntent)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setProgress(0, 0, false);

                        Log.d(TAG, "onComplete: Image Upload Successful");
                        Log.d(TAG, "onComplete: Retrieving Image Url");
                        profileImageUrl = task.getResult();


                        Log.d(TAG, "uploadFile: Deleting Previous Profile Image");
                        StorageReference previousProfileImageRef=FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfileImageUrl());

                        previousProfileImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Previous Profile Image Deleted Successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Previous Profile Image Delete Unsucessful");
                            }
                        });


                        // Updating User info
                        user = setUser(changeName.getText().toString(), changeCaption.getText().toString(), profileImageUrl.toString(), dateOfBirth, changeGender.getText().toString(), changeLiving.getText().toString(), changeSchool.getText().toString(), changeRelation.getText().toString());
                        updateUserInfo(user);
                        notificationManager.notify(2, notification.build());

                    } else {
                        Log.d(TAG, "onComplete: Image Upload Failed");
                        Toast.makeText(EditProfileActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                            .setContentTitle("Uploading Profile Information")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            .setContentIntent(contentIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentText(df.format(taskSnapshot.getBytesTransferred() / (1024.0 * 1024.0)) + "MB/" + df.format(taskSnapshot.getTotalByteCount() / (1024.0 * 1024.0)) + "MB")
                            .setProgress(100, (int) progress, false);
                    notificationManager.notify(2, notification.build());

                }
            });

        } else if (mImageUri == null && user.getProfileImageUrl() != null) {
            Log.d(TAG, "uploadFile: Image Uri is null but Profile Image Url found");
            user = setUser(changeName.getText().toString(), changeCaption.getText().toString(), user.getProfileImageUrl(), dateOfBirth, changeGender.getText().toString(), changeLiving.getText().toString(), changeSchool.getText().toString(), changeRelation.getText().toString());
            updateUserInfo(user);
        } else {
            Log.d(TAG, "uploadFile: No Image File selected");
            Toast.makeText(getApplicationContext(), "No Image File Selected", Toast.LENGTH_SHORT).show();
        }

    }

    public byte[] compressImage()
    {
        Log.d(TAG, "compressImage: Compressing Image....");
        Bitmap bitmap=((BitmapDrawable)uploadImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] data=baos.toByteArray();
        Log.d(TAG, "compressImage: Image Compressed Successfully.");
        return data;
    }

    private void retrieveUserInfo() {
        uploadProg.setVisibility(View.VISIBLE);

        Log.d(TAG, "retrieveUserInfo: Retrieving User Info");
        editRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    user = dataSnapshot.getValue(Users.class);
                    setInformation(user);
                    Log.d(TAG, "onDataChange: User info retrieved successfully");
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: Retrieve unsuccessful");
                }
                uploadProg.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openFileChooser() {
        Log.d(TAG, "openFileChooser: Opening File chooser");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setInformation(Users currentUser) {

        Picasso.get().load(currentUser.getProfileImageUrl()).into(uploadImage);

        changeCaption.setText(currentUser.getCaption());
        changeName.setText(currentUser.getName());
        changeDob.setText(getDateString(currentUser.getDob()));
        dateOfBirth = currentUser.getDob();
        changeGender.setText(currentUser.getGender());
        changeLiving.setText(currentUser.getPlaceLiving());
        changeSchool.setText(currentUser.getSchool());
        changeRelation.setText(currentUser.getRelationship());
    }

    private void updateUserInfo(Users user1) {
        Log.d(TAG, "updateUserInfo: Updating User Info");
        editRef.setValue(user1);
        Log.d(TAG, "updateUserInfo: Update Complete");
        Intent i = new Intent(EditProfileActivity.this, MainActivity.class);
        uploadProg.setVisibility(View.GONE);
        Log.d(TAG, "updateUserInfo: Redirecting to MainActivity");
        startActivity(i);
    }

    private int calculateAge(long dateOfBirth) {
        int age ;
        Log.d(TAG, "calculateAge: Calculating Age");
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(dateOfBirth);
        Calendar today = Calendar.getInstance();
        age=today.get(Calendar.YEAR)-dob.get(Calendar.YEAR);
        if(today.get(Calendar.MONTH)==dob.get(Calendar.MONTH))
        {
            if(today.get(Calendar.DAY_OF_MONTH)<dob.get(Calendar.DAY_OF_MONTH))
                age--;
        }
        else if(today.get(Calendar.MONTH)<dob.get(Calendar.MONTH))
            age--;

        Log.d(TAG, "calculateAge: Age Calculated: " + age);
        return age;
    }

    private String getDateString(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
}
