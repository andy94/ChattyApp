package idp.andrei.chatty;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import idp.andrei.chatty.utils.User;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        Intent intent = getIntent();
        final String otherUserUid = intent.getStringExtra("uid");
        final String chatName = intent.getStringExtra("name");
        final String active = intent.getStringExtra("active");
        final boolean isGroup = intent.getBooleanExtra("isGroup", false);

        toolbar.setTitle(chatName);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (isGroup) {

        } else { /* 1 v 1 */

            String cu = "";

            if(otherUserUid.compareTo(User.id) < 0){
                cu = otherUserUid + User.id;
            }
            else{
                cu =  User.id + otherUserUid;
            }

            final String chatUid = cu;

//            Toast.makeText(ChatActivity.this,  "", Toast.LENGTH_SHORT).show();


            DatabaseReference dbr = User.firebaseReference.child("users").child(User.id).child("chats");
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean isFirst = true;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        if (data.getKey().equalsIgnoreCase(chatUid)) {
                            isFirst = false;
                        }
                    }
//                    Toast.makeText(ChatActivity.this,  Boolean.toString(isFirst), Toast.LENGTH_SHORT).show();
                    if(isFirst){ /* first time */
                        User.firebaseReference.child("users").child(User.id).child("chats").child(chatUid).setValue(otherUserUid);
                        User.firebaseReference.child("users").child(otherUserUid).child("chats").child(chatUid).setValue(User.id);

                        DatabaseReference chatRef = User.firebaseReference.child("chats").child(chatUid);
                        chatRef.child("users").child(User.id).setValue(User.name);
                        chatRef.child("users").child(otherUserUid).setValue(chatName);
                    }
                    else { /* not first time */

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_participant) {


        } else if (id == R.id.send_file) {

        } else if (id == R.id.share_conversation) {

        }

        return super.onOptionsItemSelected(item);
    }

}
