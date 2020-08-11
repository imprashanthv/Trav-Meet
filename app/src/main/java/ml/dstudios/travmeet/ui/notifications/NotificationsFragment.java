package ml.dstudios.travmeet.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import ml.dstudios.travmeet.LoginActivity;
import ml.dstudios.travmeet.R;
import ml.dstudios.travmeet.SplashActivity;

public class NotificationsFragment extends Fragment {

    ImageView imageView;
    Button deleteAccountBtn;
    Button editDetailsBtn;
    Button signOutBtn;
    String photoURL;

    EditText interestedPlaces;
    EditText travelledPlaces;
    EditText bio;

    FirebaseUser user;


    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);


        //linking
        imageView = root.findViewById(R.id.imageView);
        interestedPlaces = root.findViewById(R.id.interestedPlacesInProfile);
        travelledPlaces = root.findViewById(R.id.travelledPlacesInProfile);
        bio = root.findViewById(R.id.bioInProfile);

        user = FirebaseAuth.getInstance().getCurrentUser();


        //loading image to thumbnail
        try {
            photoURL = user.getPhotoUrl().toString();
            Glide.with(this).load(photoURL).thumbnail().into(imageView);
        }catch(NullPointerException ne){
            Toast.makeText(getContext(), "unable to fetch profile photo", Toast.LENGTH_SHORT)
                    .show();
        }

        //set bio, interested places and travelled places
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbref = database.getReference();
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String interests = (String) snapshot.child(user.getUid())
                        .child("interestedPlaces").getValue();

                String travelled =  (String) snapshot.child(user.getUid())
                        .child("travelledPlaces").getValue();

                String about =  (String) snapshot.child(user.getUid())
                        .child("bio").getValue();


                travelledPlaces.setText(travelled);
                interestedPlaces.setText(interests);
                bio.setText(about);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






        //delete the account, if asked.
        deleteAccountBtn = root.findViewById(R.id.deleteAccount);
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleDelete();
            }
        });


        //edit the things.
        editDetailsBtn = root.findViewById(R.id.editDetails);
        editDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEdit();
            }
        });


        //sign out. if needed.
        signOutBtn = root.findViewById(R.id.signOut);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });





        return root;

    }

    private void handleEdit(){
        editDetailsBtn.setText("Submit");

    }

    private void handleDelete(){
        Log.d("I'm here","I'm here");
        //TODO Delete user records - images and database before proceeding.
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Account deleted successfully",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                    Log.d("success","deleted");
                }

                else{
                    Toast.makeText(getActivity(),
                            "Failed to Authenticate, login again to delete!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    Log.d("nope","not deleted");
                }
            }
        });
    }



}


