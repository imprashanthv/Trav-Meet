package ml.dstudios.travmeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {


    TextView dpName;
    EditText travelledPlaces;
    EditText interestedPlaces;
    EditText bio;
    Button submit;
    CircularImageView profileImage;
    FirebaseDatabase database;
    String user; // to store profile picture
    Bitmap bitmap;

    private static int RESULT_LOAD_IMAGE = 1;
    private static int REQUEST_CODE_FOR_IMAGE=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dpName = findViewById(R.id.displayname);
        submit = findViewById(R.id.submitButton);
        profileImage = findViewById(R.id.profileImage);

        if (FirebaseAuth.getInstance().getCurrentUser() != null ){
            String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            dpName.setText("Hello "+displayName);
            database = FirebaseDatabase.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser().getUid();
            followup();
        }
        else{
            Log.d("error", "problem occurred");
        }

    }

    public void followup(){

        travelledPlaces = findViewById(R.id.travelled);
        interestedPlaces = findViewById(R.id.toTravel);
        bio = findViewById(R.id.bio);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write a message to the database

                if (interestedPlaces.getText() == null || interestedPlaces.getText().toString().equals("")
                        || interestedPlaces.getText().toString().equals(" ") ){
                    Toast.makeText(getApplicationContext(),
                            "Please enter atleast one interested place", Toast.LENGTH_SHORT)
                            .show();
                }
                else {

                    DatabaseReference updateBio = database.getReference()
                            .child(user).child("bio");
                    updateBio.setValue(bio.getText().toString());

                    DatabaseReference updateTravelledPlaces = database.getReference()
                            .child(user).child("travelledPlaces");
                    updateTravelledPlaces.setValue(travelledPlaces.getText().toString());

                    DatabaseReference updateInterestedPlaces = database.getReference()
                            .child(user).child("interestedPlaces");
                    updateInterestedPlaces.setValue(interestedPlaces.getText().toString());

                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Choose one of the following methods");
                // Add the buttons
                                builder.setPositiveButton("Take a photo", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        if(intent.resolveActivity(getPackageManager()) != null ){
                                            startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
                                        }
                                    }
                                });

                                builder.setNeutralButton("Gallery", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(
                                                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                                    }


                                });



                // Create the AlertDialog
                                AlertDialog dialog = builder.create();
                                dialog.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            bitmap = (Bitmap) data.getExtras().get("data");
            profileImage.setImageBitmap(bitmap);
            handleUpload(bitmap);
        }

        if(requestCode == REQUEST_CODE_FOR_IMAGE){
            switch(resultCode){
                case RESULT_OK:
                   bitmap  = (Bitmap) data.getExtras().get("data");
                    profileImage.setImageBitmap(bitmap);
                    handleUpload(bitmap);
                    break;
                case RESULT_CANCELED:
                    break;
                default:
                    break;
            }
        }
    }

    private void handleUpload(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        final StorageReference storageReference =
                FirebaseStorage.getInstance().getReference()
                        .child("profileImages").child(user+".jpeg");

        storageReference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getDownloadUrl(storageReference);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error","onFailure", e.getCause());
            }
        });

    }

    private void getDownloadUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        setUserProfileUrl(uri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}