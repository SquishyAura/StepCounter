package com.bakerapps.stepcounter;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    private TextView txtMyScore;
    private ListView listLeaderboard;
    private DatabaseReference myDatabase;
    private int userScore;
    private String userName;
    private ArrayList<String> scores;
    private ProgressBar loadingBar;

    //gets reference to database, SharedPreferences, ListView, ProgressBar, and TextView
    //Checks the database for the score of the user and updates txtMyScore
    //gets top 10 scores of all users in the database and shows them in the ListView with an ArrayAdapter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        SharedPreferences prefs = getSharedPreferences("StepCounter_Preferences", MODE_PRIVATE);

        myDatabase = FirebaseDatabase.getInstance().getReference();

        txtMyScore = (TextView) findViewById(R.id.txtMyScore);
        listLeaderboard = (ListView) findViewById(R.id.listLeaderboard);
        loadingBar = (ProgressBar) findViewById(R.id.progressBar);

        userName = prefs.getString("userName", "Anonymous");
        scores = new ArrayList<String>();




        myDatabase.child("users").child(userName.toLowerCase()).addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userScore = ((Long) dataSnapshot.child("steps").getValue()).intValue();
                txtMyScore.setText(userName + ": " + userScore);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        Query leaderboardRef = myDatabase.child("users").orderByChild("steps").limitToLast(10);

        leaderboardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scores.clear();
                for(DataSnapshot user : dataSnapshot.getChildren()){
                    scores.add(user.child("nickname").getValue() + ": " + user.child("steps").getValue());
                }
                Collections.reverse(scores);
                ArrayAdapter leaderboardAdapter = new ArrayAdapter(LeaderboardActivity.this, android.R.layout.simple_list_item_1, scores);
                listLeaderboard.setAdapter(leaderboardAdapter);
                loadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void goBack(View view){
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
