package com.bakerapps.stepcounter;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomeActivity extends AppCompatActivity {

    EditText inputUsername;
    private DatabaseReference myDatabase;
    private SharedPreferences.Editor editor;

    //gets reference to database, gets the username input, gets a reference to SharedPreferences for our app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("StepCounter_Preferences", MODE_PRIVATE);
        editor = prefs.edit();

        myDatabase = FirebaseDatabase.getInstance().getReference();
        inputUsername = (EditText) findViewById(R.id.inputUsername);
    }

    //checks user input to see if it already exists in database, and creates the user in the database if it doesn't
    public void submitOnClick(View view){
        myDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String trimmedUsername = inputUsername.getText().toString().trim();
                try{
                    if(dataSnapshot.hasChild(trimmedUsername.toLowerCase())){
                        //error
                        Toast errorToast = Toast.makeText(getApplicationContext(), "Username already exists, please try another", Toast.LENGTH_SHORT);
                        errorToast.show();
                    } else{

                        myDatabase.child("users").child(trimmedUsername.toLowerCase()).child("steps").setValue(0);
                        myDatabase.child("users").child(trimmedUsername.toLowerCase()).child("nickname").setValue(trimmedUsername);
                        Toast successToast = Toast.makeText(getApplicationContext(), "Registration completed", Toast.LENGTH_SHORT);
                        successToast.show();
                        editor.putString("userName", trimmedUsername);
                        editor.apply();
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                } catch (DatabaseException ex){
                    Toast failureToast = Toast.makeText(getApplicationContext(), "Illegal characters in username!", Toast.LENGTH_SHORT);
                    failureToast.show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
