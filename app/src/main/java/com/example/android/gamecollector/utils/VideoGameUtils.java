package com.example.android.gamecollector.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.android.gamecollector.data.propertyBags.VideoGame;
import com.example.android.gamecollector.data.sqlite.CollectableContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * Created by shalom on 2018-01-12.
 */

public final class VideoGameUtils {
    private static final String LOG_TAG = VideoGameUtils.class.getSimpleName();

    /*Constant names of database nodes*/
    public static final String NODE_COLLECTABLES_OWNED = "collectables_owned";
    public static final String NODE_USERS = "users";
    public static final String NODE_VIDEO_GAMES = "video_games";

    public static void CreateNode(VideoGame videoGame) {
        /*Create a unique ID that names a node for an individual video game when it's added*/
        videoGame.setValueUniqueNodeId(UUID.randomUUID().toString());

        CheckRegionLock(videoGame);


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = GetDatabaseReference(videoGame);

        dbRef.child(videoGame.KEY_UNIQUE_ID).setValue(videoGame.getValueUniqueID());
        dbRef.child(videoGame.KEY_CONSOLE).setValue(videoGame.getValueConsole());
        dbRef.child(videoGame.KEY_TITLE).setValue(videoGame.getValueTitle());
        dbRef.child(videoGame.KEY_LICENSEE).setValue(videoGame.getValueLicensee());
        dbRef.child(videoGame.KEY_RELEASED).setValue(videoGame.getValueReleased());
        dbRef.child(videoGame.KEY_DATE_ADDED).setValue(videoGame.getValueDateAdded());
        dbRef.child(videoGame.KEY_REGION_LOCK).setValue(videoGame.getValueRegionLock());
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.GAME).setValue(videoGame.getValuesComponentsOwned().get(videoGame.GAME));
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.MANUAL).setValue(videoGame.getValuesComponentsOwned().get(videoGame.MANUAL));
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.BOX).setValue(videoGame.getValuesComponentsOwned().get(videoGame.BOX));
        dbRef.child(videoGame.KEY_NOTE).setValue(videoGame.getValueNote());
        dbRef.child(videoGame.KEY_UNIQUE_NODE_ID).setValue(videoGame.getValueUniqueNodeId());

//        int rowsUpdated = updateCopiesOwned();
//        Log.i(LOG_TAG, "Rows updated: " + rowsUpdated);
    }

    public static void UpdateNode(VideoGame videoGame) {

        Log.i(LOG_TAG, "regionLock: " + videoGame.getValueRegionLock()
                + ", game: " + videoGame.getValueGame()
                + ", manual: " + videoGame.getValueManual()
                + ", box: " + videoGame.getValueBox());

        CheckRegionLock(videoGame);

        DatabaseReference dbRef = GetDatabaseReference(videoGame);
        /*Update region lock*/
        dbRef.child(videoGame.KEY_REGION_LOCK).setValue(videoGame.getValueRegionLock());
        /*Update components owned*/
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.GAME).setValue(videoGame.getValueGame());
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.MANUAL).setValue(videoGame.getValueManual());
        dbRef.child(videoGame.KEY_COMPONENTS_OWNED).child(videoGame.BOX).setValue(videoGame.getValueBox());
        /*Update note*/
        dbRef.child(videoGame.KEY_NOTE).setValue(videoGame.getValueNote());

    }

    /**
     * Will be used later for informing SQLite database that video game was added or deleted from
     * user's collection
     * @param context
     * @param videoGame
     * @return How many rows were updates. Should only be one (test for this)
     */
    private static int UpdateCopiesOwned(Context context, VideoGame videoGame) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = firebaseDatabase.getReference();

        /*Updated number of copes owned*/
        int updatedCopies = videoGame.getValueCopiesOwned() + 1;
        /*Uri with appropriate rowId appended*/
        Uri uri = Uri.withAppendedPath(CollectableContract.VideoGamesEntry.CONTENT_URI, videoGame.getValueRowID());
        /*Key value pair used for updating database*/
        ContentValues sqliteUpdate = new ContentValues();
        sqliteUpdate.put(CollectableContract.VideoGamesEntry.COLUMN_COPIES_OWNED, String.valueOf(updatedCopies));

        String selection = videoGame.KEY_UNIQUE_ID + "=?";
        String[] selectionArgs = {videoGame.getValueUniqueID()};


        /*Update SQLite database*/
        int rowsUpdated = context.getContentResolver().update(uri, sqliteUpdate, selection, selectionArgs);

//        if (rowsUpdated != 1) thrown new Exception;

        return rowsUpdated;
    }

    /*Removes the video game's node from Firebase*/
    public static void DeleteNode(VideoGame videoGame) {
        GetDatabaseReference(videoGame).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    /**
     * Handles a null valueRegionLock by setting it's value to VideoGame.UNDEFINED_TRAIT
     * @param videoGame Video object
     */
    private static void CheckRegionLock(VideoGame videoGame) {
        if (videoGame.getValueRegionLock() == null) {
            videoGame.setValueRegionLock(VideoGame.UNDEFINED_TRAIT);
        }
    }
    /**
     * @param videoGame VideoGame object containing data
     * @return reference to a newly generated node
     */
    private static DatabaseReference GetDatabaseReference(VideoGame videoGame) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = firebaseDatabase.getReference()
                .child(NODE_COLLECTABLES_OWNED)
                .child(NODE_USERS)
                .child(GetUserID())
                .child(NODE_VIDEO_GAMES)
                .child(videoGame.getValueUniqueNodeId());

        return dbRef;
    }

    /**
     * Extracts and tests for existence of current user
     * @return UID from FirebaseAuth object
     */
    private static String GetUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        /*Holds user's ID*/
        String uid = null;
        /*Handles there being no user*/
        if (user != null)  {
            uid = user.getUid();
            return uid;
        } else {
            throw new UnsupportedOperationException("There is no current user");
        }
    }
}