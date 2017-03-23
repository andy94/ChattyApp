package idp.andrei.chatty.utils;

import com.google.firebase.database.DatabaseReference;

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

    public static String profilePictureUrl;
    public static DatabaseReference firebaseReference;
    public static Map<String,String> friends;

    public static Map<String, Object> getMapForFirebase(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("email", email);
        map.put("name", name);
        map.put("lastLoginTime", lastLoginTime);
        map.put("profilePictureUrl", profilePictureUrl);
        map.put("friends", friends);


        return map;
    }

}
