package idp.andrei.chatty;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import idp.andrei.chatty.utils.Chat;
import idp.andrei.chatty.utils.User;

public class GroupsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyCustomAdapter adapter;

    private ProgressDialog dialog;

    public class MyCustomAdapter extends BaseAdapter implements ListAdapter {

        private ArrayList<Chat> list = new ArrayList<>();
        private Context context;

        public MyCustomAdapter(ArrayList<Chat> list, Context context) {
            this.list = list;
            this.context = context;
        }

        class ViewHolder {
            TextView friendName;
            ImageView friendImage;
            TextView isOnline;
            TextView lastMesg;
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
            MyCustomAdapter.ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.message_list_layout, null);

                holder = new MyCustomAdapter.ViewHolder();
                holder.friendName = (TextView) view.findViewById(R.id.friend_nameMSG);
                holder.friendImage = (ImageView) view.findViewById(R.id.friendPictureMSG);
                holder.isOnline = (TextView) view.findViewById(R.id.isOnlineMSG);
                holder.lastMesg = (TextView) view.findViewById(R.id.preview_mesgMSG);
                view.setTag(holder);
            } else {
                holder = (MyCustomAdapter.ViewHolder) view.getTag();
            }

            view.setBackgroundColor(Color.parseColor("#FFFFFF"));

            // Set name in TextView
            holder.friendName.setText(list.get(position).authorName);
            String txt = list.get(position).text;
            int leng = txt.length();
            leng = leng <=25 ? leng : 25;
            holder.lastMesg.setText(txt.substring(0,leng));
            holder.isOnline.setText(list.get(position).users);
            holder.friendImage.setBackgroundResource(R.mipmap.ic_launcher6);


            final View currentView = view;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentView.setBackgroundColor(Color.parseColor("#D3D3D3"));

                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    String cu = list.get(position).uid;
                    intent.putExtra("chatUid", cu);
                    intent.putExtra("uid", list.get(position).authorID);
                    intent.putExtra("uid1", list.get(position).authorID);
                    intent.putExtra("name", list.get(position).authorName);
                    intent.putExtra("userName", list.get(position).authorName);
                    intent.putExtra("userName1", list.get(position).authorName);
                    intent.putExtra("active", list.get(position).seen);
                    intent.putExtra("isGroup", true);

                    startActivity(intent);

                }
            });

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        /* Navigation *****************************************************************************/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View mHeaderView = navigationView.getHeaderView(0);
        TextView usernameHeader = (TextView) mHeaderView.findViewById(R.id.nav_user_name);
        usernameHeader.setText(User.name);

        ImageView userPic = (ImageView) mHeaderView.findViewById(R.id.headerImgView);
        userPic.setImageBitmap(User.image);

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(3).setChecked(true);
        /* END Navigation *************************************************************************/


        dialog  = ProgressDialog.show(GroupsActivity.this, "", "Loading. Please wait", true);


        final ArrayList<Chat> chats = new ArrayList<>();
        final ArrayList<Chat> finalChats = new ArrayList<>();

        final AtomicInteger count = new AtomicInteger();
        count.set(0);

        DatabaseReference chatRef = User.firebaseReference.child("users").child(User.id).child("chats");
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long nr = 0;
                if (snapshot != null) {

                    nr = snapshot.getChildrenCount();

                    for (DataSnapshot chat : snapshot.getChildren()) { /* For each chat */
                        Chat c = new Chat();

                        c.uid = chat.getKey();
                        chats.add(c);
                        count.incrementAndGet();
                    }
                }


                if (count.get() >= nr) {

                    count.set(0);
                    final AtomicInteger total = new AtomicInteger();
                    total.set(chats.size());

                    for (Chat c : chats) {

                        final String cid = c.uid;

                        DatabaseReference cr = User.firebaseReference.child("chats").child(cid);
                        cr.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snp) {
                                if (snp != null) {
                                    final Chat c = new Chat();

                                    for (DataSnapshot cht : snp.getChildren()) {
                                        if (cht.getKey().toString().equalsIgnoreCase("isGroup")) {
                                            c.group = (boolean) cht.getValue();
                                        }

                                        if (cht.getKey().toString().equalsIgnoreCase("users")) {
                                            int nrUsers = (int)cht.getChildrenCount();
                                            c.users = Integer.toString(nrUsers) + " members";
                                        }

                                        c.uid = cid;

                                        if (cht.getKey().toString().equalsIgnoreCase("mesg")) {
                                            DataSnapshot last = null;
                                            for (DataSnapshot msg : cht.getChildren()) {
                                                last = msg;
                                            }
                                            c.text = "";
                                            for (DataSnapshot dat : last.getChildren()) {
                                                if (dat.getKey().toString().equalsIgnoreCase("text")) {
                                                    c.text += dat.getValue().toString();
                                                }
                                                if (dat.getKey().toString().equalsIgnoreCase("authorName")) {
                                                    String nm = dat.getValue().toString();
                                                    if (nm.equals(User.name)){
                                                        nm = "Me";
                                                    }
                                                    if(c.text.equals("")){
                                                        c.text = nm + ": ";
                                                    }
                                                    else{
                                                        c.text = nm+ ": " + c.text;

                                                    }
                                                }
                                            }

                                        }

                                        if (cht.getKey().toString().equalsIgnoreCase("name")) {
                                            c.authorName = cht.getValue().toString();
                                        }

                                    }

                                    if(c.group) {
                                        finalChats.add(c);
                                        count.incrementAndGet();
                                    }
                                    else{
                                        total.decrementAndGet();
                                    }



                                    if (count.get() >= total.get()) {

                                        adapter = new MyCustomAdapter(finalChats, getApplicationContext());
                                        ListView listView = (ListView) findViewById(R.id.group_listView);
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

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }


                        });



                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.my_profile) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.friends) {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
        } else if (id == R.id.chats) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if(id == R.id.groups){

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
