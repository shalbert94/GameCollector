package com.example.android.gamecollector.collected.videoGames;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.example.android.gamecollector.R;
import com.example.android.gamecollector.collectable.videoGames.CollectableActivity;
import com.example.android.gamecollector.collectable.videoGames.VideoGame;
import com.example.android.gamecollector.data.firebase.CollectedArrayAdapter;
import com.example.android.gamecollector.data.sqlite.CollectableContract.VideoGamesEntry;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by shalom on 2017-10-05.
 * Displays user's personal collection
 * Also, it's the main activity/
 */

public class CollectionActivity extends AppCompatActivity{
    public static final String LOG_TAG = CollectionActivity.class.getSimpleName();
    /*Contains a list of objects with video game data that will be adapted to a ListView*/
    private ArrayList<VideoGame> videoGames = new ArrayList<>();
    /*Handles videoGames list*/
    private CollectedArrayAdapter adapter;
    /*Works with adapter to display data from videoGames*/
    private ListView listView;
    /*Instantiation of Realtime Database objects*/
    private FirebaseDatabase database;
    private DatabaseReference collectionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        /*Set toolbar as activity's ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_collection_toolbar);
        setSupportActionBar(toolbar);
        /*Set toolbar's title*/
        toolbar.setTitle(R.string.activity_collection_toolbar_title);

        createDatabaseListeners();

        floatingActionButtonListner();
    }

    /*Handles tapping the FloatingActionButton by starting CollectableActivity*/
    private void floatingActionButtonListner() {
        final FloatingActionButton floatingActionButton = (FloatingActionButton)
                findViewById(R.id.activity_collection_floating_action_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeActivity = new Intent(getApplicationContext(),
                        CollectableActivity.class);
                startActivity(changeActivity);
            }
        });
    }

    /*Initializes a ChildEventListener and ValueEventListener, and adapts videoGames to adapter,
    * and sets adapter as listView's adapter*/
    private void createDatabaseListeners() {
        database = FirebaseDatabase.getInstance();
        collectionRef = database.getReference().child("collectables_owned").child("video_games");

        /*Used to receive events about changes in the child locations of a given DatabaseReference ref.
        Attach the listener to a location using addChildEventListener(ChildEventListener) and the
        appropriate method will be triggered when changes occur.*/
        collectionRef.addChildEventListener(new ChildEventListener() {
            /*Creates VideoGame objects and adds them to the videoGames ArrayList.
            * Runs after onCreate finishes.*/
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String,Boolean> componentsOwned = new HashMap<>();

                /*Data points from each video_games node*/
                String uniqueId = dataSnapshot.child(VideoGamesEntry.COLUMN_UNIQUE_ID).getValue(String.class);
                String console = dataSnapshot.child(VideoGamesEntry.COLUMN_CONSOLE).getValue(String.class);
                String title = dataSnapshot.child(VideoGamesEntry.COLUMN_TITLE).getValue(String.class);
                String licensee = dataSnapshot.child(VideoGamesEntry.COLUMN_LICENSEE).getValue(String.class);
                String released = dataSnapshot.child(VideoGamesEntry.COLUMN_RELEASED).getValue(String.class);
                String dateAdded = dataSnapshot.child(VideoGame.DATE_ADDED).getValue(String.class);
                String regionLock = dataSnapshot.child(VideoGame.REGION_LOCK).getValue(String.class);
                componentsOwned = buildComponentsMap(dataSnapshot);
                String note = dataSnapshot.child(VideoGame.NOTE).getValue(String.class);

                videoGames.add(new VideoGame(uniqueId, console, title, licensee, released, dateAdded,
                        regionLock, componentsOwned, note));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*Used to receive events about data changes at a location given a DatabaseReference.*/
        collectionRef.addValueEventListener(new ValueEventListener() {
            /*Gives videoGames as argument to adapter, and sets adapter as listView's adapter.
            * Runs after ChildEventListener finishes.*/
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listView = (ListView) findViewById(R.id.activity_collection_listview);
                adapter = new CollectedArrayAdapter(getApplicationContext(), videoGames);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Builds HashMap of componenets owned from Firebase DataSnapshot
     * @param dataSnapshot DataSnapshot object provided by onChildAdded()
     * @return HashMap populated with values representing whether component is owned
     */
    private HashMap<String,Boolean> buildComponentsMap(DataSnapshot dataSnapshot) {
        /*Placeholder HashMap*/
        HashMap<String,Boolean> componentsOwned = new HashMap<>();

        /*Extract values from dataSnapshot*/
        boolean game = dataSnapshot.child(VideoGame.COMPONENTS_OWNED).child(VideoGame.GAME).getValue(Boolean.class);
        boolean manual = dataSnapshot.child(VideoGame.COMPONENTS_OWNED).child(VideoGame.MANUAL).getValue(Boolean.class);
        boolean box = dataSnapshot.child(VideoGame.COMPONENTS_OWNED).child(VideoGame.BOX).getValue(Boolean.class);

        /*Add values to HashMap*/
        componentsOwned.put(VideoGame.GAME, game);
        componentsOwned.put(VideoGame.MANUAL, manual);
        componentsOwned.put(VideoGame.BOX, box);

        return componentsOwned;
    }
}