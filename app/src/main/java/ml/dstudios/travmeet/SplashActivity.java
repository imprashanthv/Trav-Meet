package ml.dstudios.travmeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    TextView captionText;
    FirebaseDatabase database;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        captionText = findViewById(R.id.caption_text);
        captionText.animate().alpha(0f).scaleX(1.10f).scaleY(1.10f).alpha(1f).setDuration(1500);

        database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = database.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        final Handler handler = new Handler();

        if (FirebaseAuth.getInstance().getCurrentUser()==null){

            //not registered or signed out
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                   startActivity(intent);
               }
           }, 1700);

        }

        else{
            // already registered and signed in!
            Log.d("message bro ", "exists");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean childExists = snapshot.hasChild(user.getUid());
                    if(childExists){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            }
                        }, 1700);
                    }
                    else{
                        Log.d("message bro ", "Not exists");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        }, 1700);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}