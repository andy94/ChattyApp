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
import android.widget.Button;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import idp.andrei.chatty.utils.Friend;
import idp.andrei.chatty.utils.User;

public class FriendsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyCustomAdapter adapter;
    private ProgressDialog dialog;

    public class MyCustomAdapter extends BaseAdapter implements ListAdapter {

        private ArrayList<Friend> list = new ArrayList<>();
        private Context context;

        public MyCustomAdapter(ArrayList<Friend> list, Context context) {
            this.list = list;
            this.context = context;
        }

        class ViewHolder {
            TextView friendName;
            ImageView friendImage;
            Button chatButton;
            TextView isOnline;
        };

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
            ViewHolder holder = null;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.friends_layout, null);

                holder = new ViewHolder();
                holder.friendName = (TextView) view.findViewById(R.id.friend_name);
                holder.friendImage = (ImageView) view.findViewById(R.id.friendPicture);
                holder.chatButton = (Button) view.findViewById(R.id.firend_chat_button);
                holder.isOnline = (TextView) view.findViewById(R.id.isOnline);
                view.setTag(holder);
            }
            else {
                holder = (ViewHolder)view.getTag();
            }

            view.setBackgroundColor(Color.parseColor("#FFFFFF"));

            // Set name in TextView
            holder.friendName.setText(list.get(position).name);
            Picasso.with(context).load(list.get(position).urlImage).into(holder.friendImage);
            holder.isOnline.setText(list.get(position).active ? "online":"offline" );



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

            holder.chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    String cu = "";

                    if (list.get(position).id.compareTo(User.id) < 0) {
                        cu = list.get(position).id + User.id;
                    } else {
                        cu = User.id + list.get(position).id;
                    }
                    intent.putExtra("chatUid", cu);
                    intent.putExtra("uid", list.get(position).id);
                    intent.putExtra("uid1", list.get(position).id);
                    intent.putExtra("name", list.get(position).name);
                    intent.putExtra("userName", list.get(position).name);
                    intent.putExtra("userName1", list.get(position).name);
                    intent.putExtra("active", list.get(position).active);
                    intent.putExtra("isGroup", false);

                    startActivity(intent);
                }
            });

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
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

        navigationView.getMenu().getItem(1).setChecked(true);
        /* END Navigation *************************************************************************/

        dialog  = ProgressDialog.show(FriendsActivity.this, "", "Loading. Please wait", true);


        final ArrayList<Friend> friends = new ArrayList<>();

        final AtomicInteger count = new AtomicInteger();
        count.set(0);

        for(Map.Entry<String, String> e : User.friends.entrySet() ){
            final Map.Entry<String, String> entry = e;
            DatabaseReference friendRef = User.firebaseReference.child("users").child(entry.getKey());
            friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot != null) {
                        Friend f = new Friend();
                        f.name = entry.getValue();
                        f.id = entry.getKey();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            if (data.getKey().toString().equalsIgnoreCase("profilePictureUrl")) {
                                String url = data.getValue().toString();
                                f.urlImage = url;
                            }

                            if (data.getKey().toString().equalsIgnoreCase("lastLoginTime")) {
                                long t = (long) data.getValue();
                                f.lastLoginTime = t;
                            }

                            if (data.getKey().toString().equalsIgnoreCase("isOnline")){
                                boolean online = (boolean) data.getValue();
                                f.active = online;
                            }
                        }
                        friends.add(f);

                        count.incrementAndGet();

                    }




                    if (count.get() >= User.friends.size()){
                        adapter = new MyCustomAdapter(friends,getApplicationContext());
                        ListView listView = (ListView) findViewById(R.id.friends_listView);
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

        } else if (id == R.id.chats) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if(id == R.id.groups){
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
