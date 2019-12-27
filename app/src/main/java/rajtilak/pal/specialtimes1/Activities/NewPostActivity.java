package rajtilak.pal.specialtimes1.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;

import rajtilak.pal.specialtimes1.Others.App;
import rajtilak.pal.specialtimes1.models.Posts;
import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class NewPostActivity extends AppCompatActivity {

    // vars
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "NewPostActivity";
    private Uri imageUri;
    private int privacyCode=0;

    // widgets
    CircularImageView postProfileImage;
    TextView profileName;
    EditText postDescription;
    ImageView postImage;
    Button chooseImage;
    ProgressBar retrieveUser;
    FloatingActionButton postBtn;
    Spinner privacySpinner;

    // Firebase Classes
    FirebaseAuth mAuth;
    DatabaseReference userRef,postRef;
    StorageReference profileRef;

    // Classes
    Users user=new Users();
    Posts post=new Posts();
    Context context;
    NotificationManagerCompat notificationManager;
    DecimalFormat df;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        context=getApplicationContext();
        postProfileImage=findViewById(R.id.post_profile_image);
        profileName=findViewById(R.id.post_profile_name);
        postDescription=findViewById(R.id.post_description);
        postImage=findViewById(R.id.post_image);
        chooseImage=findViewById(R.id.post_choose_image);
        postBtn=findViewById(R.id.post_btn);
        privacySpinner=findViewById(R.id.post_privacy_spinner);
        retrieveUser=findViewById(R.id.post_retrieve_user);
        retrieveUser.setVisibility(View.GONE);
        notificationManager = NotificationManagerCompat.from(this);
        df=new DecimalFormat("0.00");

        ArrayAdapter<String> privacyAdapter=new ArrayAdapter<>(NewPostActivity.this,android.R.layout.simple_list_item_1,new String[]{"Public","Private"});
        privacySpinner.setAdapter(privacyAdapter);
        privacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                privacyCode=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                privacyCode=0;
            }
        });




        Log.d(TAG, "onViewCreated: Initializing FirebaseAuth, StorageReferences and DatabaseRefernces ");
        mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference("users/"+mAuth.getCurrentUser().getUid());
        postRef= FirebaseDatabase.getInstance().getReference("posts");
        profileRef= FirebaseStorage.getInstance().getReference("post_images");
        Log.d(TAG, "onViewCreated: Done Initialization");


        retrieveUserInfo();
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: Opening File Chooser");
                openFileChooser();
            }
        });


        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user!=null && (!postDescription.getText().toString().equals("") || imageUri!=null)) {
                    retrieveUser.setVisibility(View.VISIBLE);
                    setPost();
                }
                else
                {
                    Log.d(TAG, "onClick: Null Post information or null user information");
                    Toast.makeText(context, "Please enter post information or wait till initialization is complete or setup your profile", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void postPost() {
        Log.d(TAG, "postPost: Posting into database");
        DatabaseReference postRef1=postRef.child(String.valueOf(System.currentTimeMillis()));
        postRef1.setValue(post);
        Log.d(TAG, "postPost: Done Posting");
    }

    private void setInformation(Users currentUser) {

        Picasso.get().load(currentUser.getProfileImageUrl()).placeholder(R.drawable.ic_post_image_black_24dp).into(postProfileImage);
        profileName.setText(currentUser.getName());

    }

    private void retrieveUserInfo()
    {
        retrieveUser.setVisibility(View.VISIBLE);


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    user=dataSnapshot.getValue(Users.class);
                    setInformation(user);
                    Log.d(TAG, "onDataChange: User Info Retrieved");
                }
                catch(Exception e){
                    Log.d(TAG, "onDataChange: Profile not setup yet");
                    Toast.makeText(context, "Did not find any profile information. Please setup profile before you post", Toast.LENGTH_SHORT).show();
                }
                retrieveUser.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void init(){
        Log.d(TAG, "init: Initializing all widgets after posting");
        postDescription.setText("");
        imageUri=null;
        Picasso.get().load(imageUri).placeholder(R.drawable.ic_post_image_black_24dp).into(postImage);
    }

    private void openFileChooser() {
        Log.d(TAG, "openFileChooser: Opening File Chooser");
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST  && resultCode== Activity.RESULT_OK && data != null && data.getData() != null)
        {
            imageUri=data.getData();
            Log.d(TAG, "onActivityResult: Received Image Uri: "+imageUri.toString());
        }
        if(imageUri!=null)
        {
            Picasso.get().load(imageUri).into(postImage);
            Log.d(TAG, "onActivityResult: Done loading Image Uri into picasso");
        }
        else
            Log.d(TAG, "onActivityResult: No Image Uri found");
    }
   /* private void setPost()
    {
        Log.d(TAG, "setPost: Uploading Image");
        post.setPostDate(System.currentTimeMillis());
        post.setUserId(mAuth.getCurrentUser().getUid());
        post.setPostDescription(postDescription.getText().toString());
        post.setPrivacy(privacyCode);
        final StorageReference fileReference;
        Intent activityIntent=new Intent(this,NewPostActivity.class);
        final PendingIntent contentIntent=PendingIntent.getActivity(this,0,activityIntent,0);

        if(imageUri!=null)
        {
            fileReference = profileRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            UploadTask uploadTask = fileReference.putFile(imageUri);
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
                                .setContentTitle("Uploading Post")
                                .setContentText("Upload Completed Successfully")
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setContentIntent(contentIntent)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setProgress(0, 0, false);

                        notificationManager.notify(2, notification.build());
                        post.setPostImageUrl(task.getResult().toString());
                        Log.d(TAG, "onComplete: Done uploading Image");
                        postPost();
                        retrieveUser.setVisibility(View.GONE);
                        init();
                        Toast.makeText(context, "Post Uploaded", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: Post Uploaded");
                    } else
                        Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                            .setContentTitle("Uploading Post")
                            .setContentText("Upload Failed")
                            .setVibrate(new long[]{1000,1000})
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(contentIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setProgress(0, 0, false);
                    notificationManager.notify(2, notification.build());
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: Post Upload Failed");
                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                            .setContentTitle("Uploading Post")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setContentText(df.format(taskSnapshot.getBytesTransferred() / (1024.0 * 1024.0)) + "MB/" + df.format(taskSnapshot.getTotalByteCount() / (1024.0 * 1024.0)) + "MB")
                            .setProgress(100, (int) progress, false);

                    notificationManager.notify(2, notification.build());

                }
            });
        }

    }
*/
    private void setPost()
    {
        Log.d(TAG, "setPost: Uploading Image");
        post.setPostDate(System.currentTimeMillis());
        post.setUserId(mAuth.getCurrentUser().getUid());
        post.setPostDescription(postDescription.getText().toString());
        post.setPrivacy(privacyCode);
        final StorageReference fileReference;
        Intent activityIntent=new Intent(this,NewPostActivity.class);
        final PendingIntent contentIntent=PendingIntent.getActivity(this,0,activityIntent,0);

        if(imageUri!=null)
        {
            fileReference = profileRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            byte[] compressed_image=compressImage();
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
                                .setContentTitle("Uploading Post")
                                .setContentText("Upload Completed Successfully")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(contentIntent)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setProgress(0, 0, false);

                        notificationManager.notify(2, notification.build());
                        post.setPostImageUrl(task.getResult().toString());
                        Log.d(TAG, "onComplete: Done uploading Image");
                        postPost();
                        retrieveUser.setVisibility(View.GONE);
                        init();
                        Toast.makeText(context, "Post Uploaded", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: Post Uploaded");
                    } else
                        Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                            .setContentTitle("Uploading Post")
                            .setContentText("Upload Failed")
                            .setVibrate(new long[]{1000,1000})
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(contentIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setProgress(0, 0, false);
                    notificationManager.notify(2, notification.build());
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: Post Upload Failed");
                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                            .setContentTitle("Uploading Post")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setContentText(df.format(taskSnapshot.getBytesTransferred() / (1024.0 * 1024.0)) + "MB/" + df.format(taskSnapshot.getTotalByteCount() / (1024.0 * 1024.0)) + "MB")
                            .setProgress(100, (int) progress, false);

                    notificationManager.notify(2, notification.build());

                }
            });
        }

    }


    public byte[] compressImage()
    {
        Log.d(TAG, "compressImage: Compressing Image...");
        Bitmap bitmap=((BitmapDrawable)postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] data=baos.toByteArray();
        Log.d(TAG, "compressImage: Image Compressed Successfully.");
        return data;
    }

    private String getFileExtension(Uri uri)
    {
        Log.d(TAG, "getFileExtension: Getting File Extension");
        ContentResolver cr=context.getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

}
