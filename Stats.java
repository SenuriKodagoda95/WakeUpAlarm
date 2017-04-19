package com.example.shakila.wakeupalarmsystem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Stats extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;

    private ListView listV;
    private String sessionName;
    private ArrayList<String> data = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        System.out.println("=======================================================================================");
        Intent intent = getIntent();
        sessionName = intent.getStringExtra("alarmSession");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        getDataFromDB();




    }

    public void getDataFromDB(){

        myRef.child(sessionName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    System.out.println("======="+dataSnapshot.getChildrenCount()+"========"+singleSnapshot.getKey()+" : "+singleSnapshot.getValue());
                    data.add(singleSnapshot.getKey()+" moved");
                    System.out.println("Rows : "+(int)dataSnapshot.getChildrenCount()+" added "+singleSnapshot.getKey());
                    updateListView();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void updateListView(){
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_list_view, data);
        ListView listView = (ListView) findViewById(R.id.mList);
        listView.setAdapter(adapter);
    }
}
