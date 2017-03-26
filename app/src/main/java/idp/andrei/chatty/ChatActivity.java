package idp.andrei.chatty;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import idp.andrei.chatty.utils.Chat;
import idp.andrei.chatty.utils.Friend;
import idp.andrei.chatty.utils.User;

import static android.provider.UserDictionary.Words.APP_ID;
import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class ChatActivity extends AppCompatActivity {

    private ProgressDialog dialog;

    private ChatActivity.MessageListAdapter adapter;
    private String chatID = "";
    private String chatName = "";
    private String u1uid = "";
    private String u2uid = "";
    private String u1name = "";
    private String u2name = "";
    private boolean group = false;

    private AlertDialog.Builder builerViewMembers;

    public class MessageListAdapter extends BaseAdapter implements ListAdapter {

        private ArrayList<Chat> list = new ArrayList<>();
        private Context context;

        public MessageListAdapter(ArrayList<Chat> list, Context context) {
            this.list = list;
            this.context = context;
        }

        class ViewHolder {
            TextView author;
            TextView text;
            TextView date;
            TextView seen;
        }

        ;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int pos) {
            return list.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            View view = convertView;
            ChatActivity.MessageListAdapter.ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.message_layout, null);

                holder = new ChatActivity.MessageListAdapter.ViewHolder();
                holder.author = (TextView) view.findViewById(R.id.msg_friend_name);
                holder.text = (TextView) view.findViewById(R.id.msg_message);
                holder.date = (TextView) view.findViewById(R.id.msg_date);
                holder.seen = (TextView) view.findViewById(R.id.msg_seen);
                view.setTag(holder);
            } else {
                holder = (ChatActivity.MessageListAdapter.ViewHolder) view.getTag();
            }

            view.setBackgroundColor(Color.parseColor("#FFFFFF"));

            // Set name in TextView

            if (list.get(position).authorID.equals(User.id)) {
                holder.author.setText("Me");

            } else {
                holder.author.setText(list.get(position).authorName);
                holder.author.setTextColor(Color.parseColor("#FF4081"));

            }
            holder.text.setText(list.get(position).text);
            String timePassedString = DateUtils.getRelativeTimeSpanString(list.get(position).date, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();

            holder.date.setText(timePassedString);
            holder.seen.setText(list.get(position).seen ? "seen" : "");


//            final View currentView = view;
//            view.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v) {
//                    currentView.setBackgroundColor(Color.parseColor("#D3D3D3"));
//                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
//                    intent.putExtra("uid", list.get(position).uid);
//                    startActivity(intent);
//                }
//            });
            return view;
        }
    }

    @Override
    public void onBackPressed() {
        if (group) {
            Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        Intent intent = getIntent();
        final String cuid = intent.getStringExtra("chatUid");
        final String chatName = intent.getStringExtra("name");
        final String userName = intent.getStringExtra("userName");
        final String otherUserUid = intent.getStringExtra("uid");
        final String userName1 = intent.getStringExtra("userName1");
        final String otherUserUid1 = intent.getStringExtra("uid1");
        final boolean isGroup = intent.getBooleanExtra("isGroup", false);


        builerViewMembers = new AlertDialog.Builder(this);
        builerViewMembers.setTitle("Group members:");


        if (isGroup && cuid.equals("")) { // first time in this group
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Conversation Name");

            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textName = input.getText().toString();
                    User.firebaseReference.child("chats").child(chatID).child("name").setValue(textName);

                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setTitle(textName);
                    setSupportActionBar(toolbar);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }


        toolbar.setTitle(chatName);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialog = ProgressDialog.show(ChatActivity.this, "", "Loading. Please wait", true);


//      Toast.makeText(ChatActivity.this,  "", Toast.LENGTH_SHORT).show();


        DatabaseReference dbr = User.firebaseReference.child("users").child(User.id).child("chats");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String chatUid = cuid;
                boolean isFirst = true;
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getKey().equalsIgnoreCase(chatUid)) {
                        isFirst = false;
                    }
                }

//              Toast.makeText(ChatActivity.this,  Boolean.toString(isFirst), Toast.LENGTH_SHORT).show();
                if (isFirst) { /* first time */
                    if (isGroup) {
                        DatabaseReference chatRef = User.firebaseReference.child("chats").push();
                        chatUid = chatRef.getKey();
                        chatRef.child("users").child(User.id).setValue(User.name);
                        chatRef.child("users").child(otherUserUid).setValue(userName);
                        chatRef.child("users").child(otherUserUid1).setValue(userName1);
                        chatRef.child("isGroup").setValue(isGroup);
                        User.firebaseReference.child("users").child(otherUserUid1).child("chats").child(chatUid).setValue(User.id);


                    } else {

                        DatabaseReference chatRef = User.firebaseReference.child("chats").child(chatUid);
                        chatRef.child("users").child(User.id).setValue(User.name);
                        chatRef.child("users").child(otherUserUid).setValue(userName);
                        chatRef.child("isGroup").setValue(isGroup);
                    }


                    User.firebaseReference.child("users").child(User.id).child("chats").child(chatUid).setValue(otherUserUid);
                    User.firebaseReference.child("users").child(otherUserUid).child("chats").child(chatUid).setValue(User.id);

                }

                chatID = chatUid;
                group = isGroup;
                u1uid = otherUserUid;
                u2uid = otherUserUid1;
                u1name = userName;
                u2name = userName1;

                makeAdaptor(chatUid);

                final FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.fab);
                final EditText messageText = (EditText) findViewById(R.id.input);

                final String cid = chatUid;

                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String text = messageText.getText().toString();
                        messageText.setText("");

                        if (text.equals("")) {
                            return;

                        }

                        Map<String, Object> mesg = new HashMap<>();

                        mesg.put("text", text);
                        mesg.put("authorID", User.id);
                        mesg.put("authorName", User.name);
                        long now = System.currentTimeMillis();
                        mesg.put("date", now);


                        User.firebaseReference.child("chats").child(cid).child("mesg").push().setValue(mesg);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        msg_builer = new AlertDialog.Builder(this);
        msg_builer.setTitle("Select messages");


        shareDialog = new ShareDialog(this);
        CallbackManager callbackManager = CallbackManager.Factory.create();
        shareDialog.registerCallback(callbackManager, new
                FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "Not Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                    }
                });


    }

    private AlertDialog.Builder msg_builer;
    private ShareDialog shareDialog;

    private void makeAdaptor(String chatUid) {

        final ArrayList<Chat> chats = new ArrayList<>();

        final AtomicInteger count = new AtomicInteger();
        count.set(0);

        DatabaseReference friendRef = User.firebaseReference.child("chats").child(chatUid).child("mesg");
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long nr = 0;
                chats.clear();
                if (snapshot != null) {

                    nr = snapshot.getChildrenCount();

                    for (DataSnapshot mesg : snapshot.getChildren()) { /* For each message */
                        Chat c = new Chat();
                        for (DataSnapshot data : mesg.getChildren()) {
                            if (data.getKey().toString().equalsIgnoreCase("text")) {
                                c.text = data.getValue().toString();
                            }

                            if (data.getKey().toString().equalsIgnoreCase("authorID")) {
                                c.authorID = data.getValue().toString();
                            }

                            if (data.getKey().toString().equalsIgnoreCase("authorName")) {
                                c.authorName = data.getValue().toString();
                            }

                            if (data.getKey().toString().equalsIgnoreCase("date")) {
                                c.date = (long) data.getValue();
                            }
                        }
                        chats.add(c);
                        count.incrementAndGet();
                    }
                }


                if (count.get() >= nr) {
                    adapter = new ChatActivity.MessageListAdapter(chats, getApplicationContext());
                    ListView listView = (ListView) findViewById(R.id.chat_messages_listView);
                    listView.setSelection(listView.getCount() - 1);
                    listView.setAdapter(adapter);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Intent intent = getIntent();
        final boolean grp = intent.getBooleanExtra("isGroup", false);
        if (grp) {
            getMenuInflater().inflate(R.menu.main_group, menu);

        } else {
            getMenuInflater().inflate(R.menu.main, menu);

        }
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


            final CharSequence friends[] = new CharSequence[User.friends.size()];
            int index = 0;

            for (Map.Entry<String, String> friend : User.friends.entrySet()) {
                friends[index] = friend.getValue();
                index++;

            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose a friend");
            builder.setItems(friends, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String n = friends[which].toString();
                    String i = null;

                    for (Map.Entry<String, String> friend : User.friends.entrySet()) {
                        if (n.equals(friend.getValue())) {
                            i = friend.getKey();
                            break;
                        }

                    }


                    if (group) { // just add a participant

                        DatabaseReference chatRef = User.firebaseReference.child("chats").child(chatID);
                        chatRef.child("users").child(i).setValue(n); //

                        User.firebaseReference.child("users").child(i).child("chats").child(chatID).setValue(i);
                    } else { // create and go to group

                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        String cu = "";

                        intent.putExtra("chatUid", cu);
                        intent.putExtra("uid", u1uid);
                        intent.putExtra("userName", u1name);
                        intent.putExtra("uid1", i);
                        intent.putExtra("userName1", n);
                        intent.putExtra("name", "Group");

                        intent.putExtra("isGroup", true);

                        startActivity(intent);

                    }
                }
            });
            builder.show();


        } else if (id == R.id.send_file) {

        } else if (id == R.id.share_conversation) {

            LoginManager.getInstance()
                    .logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

            final ArrayList<Chat> chats = new ArrayList<>();

            DatabaseReference friendRef = User.firebaseReference.child("chats").child(chatID).child("mesg");
            friendRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot != null) {
                        for (DataSnapshot mesg : snapshot.getChildren()) { /* For each message */
                            Chat c = new Chat();
                            for (DataSnapshot data : mesg.getChildren()) {
                                if (data.getKey().toString().equalsIgnoreCase("text")) {
                                    c.text = data.getValue().toString();
                                }

                                if (data.getKey().toString().equalsIgnoreCase("authorID")) {
                                    c.authorID = data.getValue().toString();
                                }

                                if (data.getKey().toString().equalsIgnoreCase("authorName")) {
                                    c.authorName = data.getValue().toString();
                                }

                                if (data.getKey().toString().equalsIgnoreCase("date")) {
                                    c.date = (long) data.getValue();
                                }
                            }
                            chats.add(c);


                        }

                        final CharSequence chats_name[] = new CharSequence[chats.size() + 1];
                        chats_name[0] = "ALL MESSAGES";
                        int index = 1;

                        final Map<String, Chat> chats_map = new HashMap<String, Chat>();

                        for (Chat c : chats) {
                            chats_name[index] = c.text;
                            index++;
                            chats_map.put(c.text, c);
                        }

                        final ArrayList<Chat> final_C = new ArrayList<>();


                        final boolean[] checked = new boolean[chats.size() + 1];

                        msg_builer.setMultiChoiceItems(chats_name, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                String n = chats_name[which].toString();

                                if (n.equals("ALL MESSAGES")) {


                                    final_C.clear();
                                    if (isChecked) {
                                        for (Map.Entry<String, Chat> e : chats_map.entrySet()) {
                                            final_C.add(e.getValue());
                                        }
                                    }
                                    return;
                                }

                                if (isChecked && final_C.indexOf(chats_map.get(n))<=0) {
                                    final_C.add(chats_map.get(n));
                                } else {
                                    final_C.remove(chats_map.get(n));
                                }

                            }
                        });



                        msg_builer.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                GraphRequest request = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(), "me/feed", null, new GraphRequest.Callback() {
                                    @Override
                                    public void onCompleted(GraphResponse response) {
                                        Toast.makeText(getApplicationContext(), "Conversation shared!", Toast.LENGTH_LONG).show();
                                    }
                                });
                                Bundle parameters = new Bundle();

                                String message;
                                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

                                if(final_C.isEmpty()){
                                    Toast.makeText(getApplicationContext(), "No Message Selected", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if(group){
                                    message = toolbar.getTitle().toString() + " Group Conversation\n\n";
                                }
                                else{
                                    message = "Conversation with: " + toolbar.getTitle().toString() + "\n\n";
                                }

                                for(Chat c : final_C){
                                    message+=c.authorName+ ": " + c.text + "\n";
                                }




                                parameters.putString("message", message);
                                request.setParameters(parameters);
                                request.executeAsync();
                            }
                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {


                                    }
                                });

                        msg_builer.show();
                    }


                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });





        } else if (id == R.id.view_members) {

            final ArrayList<String> users = new ArrayList<>();
            DatabaseReference dbr = User.firebaseReference.child("chats").child(chatID).child("users");
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot usr : snapshot.getChildren()) {
                        users.add(usr.getValue().toString());
                    }

                    final CharSequence usersName[] = new CharSequence[users.size()];
                    int index = 0;

                    for (String u : users) {
                        usersName[index] = u;
                        index++;
                    }


                    builerViewMembers.setItems(usersName, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builerViewMembers.show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else if (id == R.id.leave_group) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure?");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    User.firebaseReference.child("users").child(User.id).child("chats").child(chatID).removeValue();
                    User.firebaseReference.child("chats").child(chatID).child("users").child(User.id).removeValue();

                    Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
                    startActivity(intent);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


        } else if (id == R.id.change_name) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Conversation Name");

            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textName = input.getText().toString();
                    User.firebaseReference.child("chats").child(chatID).child("name").setValue(textName);

                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setTitle(textName);
                    setSupportActionBar(toolbar);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }


        return super.onOptionsItemSelected(item);
    }

}
