package idp.andrei.chatty;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import idp.andrei.chatty.utils.Chat;
import idp.andrei.chatty.utils.Friend;
import idp.andrei.chatty.utils.User;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class ChatActivity extends AppCompatActivity {

    private ProgressDialog dialog;

    private ChatActivity.MessageListAdapter adapter;
    private String chatID = "";
    private String u1uid = "";
    private String u2uid = "";
    private String u1name = "";
    private String u2name = "";
    private boolean group = false;

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
        if(group){
            Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
            startActivity(intent);
        }
        else{
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

        toolbar.setTitle(chatName);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialog  = ProgressDialog.show(ChatActivity.this, "", "Loading. Please wait", true);



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
                    if(isGroup){
                        DatabaseReference chatRef = User.firebaseReference.child("chats").push();
                        chatUid = chatRef.getKey();
                        chatRef.child("users").child(User.id).setValue(User.name);
                        chatRef.child("users").child(otherUserUid).setValue(userName);
                        chatRef.child("users").child(otherUserUid1).setValue(userName1);
                        chatRef.child("isGroup").setValue(isGroup);
                        chatRef.child("name").setValue("Group conversation");
                    }
                    else{

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


    }

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

            if(group){ // just add a participant

                DatabaseReference chatRef = User.firebaseReference.child("chats").child(chatID);
                chatRef.child("users").child(u2uid).setValue(u2name); //

                User.firebaseReference.child("users").child(u2uid).child("chats").child(chatID).setValue(u2uid);
            }else{ // create and go to group

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                String cu = "";

                intent.putExtra("chatUid", cu);
                intent.putExtra("uid", u1uid);
                intent.putExtra("userName", u1name);
                intent.putExtra("uid1", u2uid);
                intent.putExtra("userName1", u2name);
                intent.putExtra("name", "Group");

                intent.putExtra("isGroup", true);

                startActivity(intent);

            }




        } else if (id == R.id.send_file) {

        } else if (id == R.id.share_conversation) {

        }

        return super.onOptionsItemSelected(item);
    }

}
