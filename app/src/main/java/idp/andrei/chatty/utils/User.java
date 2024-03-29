package idp.andrei.chatty.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrei on 3/19/2017.
 */

public class User {
    public static String id;
    public static String name;
    public static String email;
    public static double lastLoginTime;

    public static boolean isOnline;

    public static Bitmap image;

    public static String profilePictureUrl;
    public static DatabaseReference firebaseReference;
    public static StorageReference firebaseStorageReference;
    public static FirebaseStorage firebaseStorage;
    public static Map<String,String> friends;

    public static Map<String, Object> getMapForFirebase(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("email", email);
        map.put("name", name);
        map.put("lastLoginTime", lastLoginTime);
        map.put("profilePictureUrl", profilePictureUrl);
        map.put("friends", friends);
        map.put("isOnline", isOnline);


        return map;
    }

}
