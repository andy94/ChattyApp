package idp.andrei.chatty;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import idp.andrei.chatty.utils.User;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.


        User.name = "First Last";


        User.firebaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
//                    User.firebaseReference = FirebaseDatabase.getInstance().getReference();
//                    DatabaseReference dbr = User.firebaseReference.child("mesg1");
//                    dbr.setValue("URAA!!Success!!!");

                    if (AccessToken.getCurrentAccessToken() == null) {
                        return;
                    }

                    LoginButton login_button = (LoginButton) findViewById(R.id.email_sign_in_button);
                    login_button.setVisibility(View.INVISIBLE);

                    TextView hello = (TextView) findViewById(R.id.editText);
                    hello.setVisibility(View.INVISIBLE);

                    GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {

                                    String email = object.optString("email");
                                    String id = object.optString("id");
                                    String name = object.optString("name");
                                    String pictureUrl = "";
                                    double lastLoginTime = 0;


                                    try {
                                        JSONObject picurljson = object.getJSONObject("picture");
                                        JSONObject picurljsondata = picurljson.getJSONObject("data");
                                        String url = picurljsondata.optString("url");
                                        if (!url.isEmpty()) {
                                            url = url.replace("\"", "");
                                            url = url.replace("\\", "");
                                            pictureUrl = url;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    lastLoginTime = System.currentTimeMillis();

                                    Map<String, String> friends = new HashMap<String, String>();
                                    try {
                                        JSONObject friendsJson = object.getJSONObject("friends");
                                        JSONArray friendsData = friendsJson.getJSONArray("data");

                                        for (int i = 0, size = friendsData.length(); i < size; i++) {
                                            JSONObject objectInArray = friendsData.getJSONObject(i);
                                            friends.put(objectInArray.optString("id"), objectInArray.optString("name"));
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


//                                    ImageView profilePicture = (ImageView) findViewById(R.id.profileImageView);
//                                    profilePicture.setImageBitmap(profilePictureView.getDrawingCache());

//                                    greeting.setVisibility(View.VISIBLE);
//                                    greeting.setText(getString(R.string.hello_user, profile.getFirstName()));


                                    User.id = id;
                                    User.name = name;
                                    User.email = email;
                                    User.lastLoginTime = lastLoginTime;
                                    User.profilePictureUrl = pictureUrl;
                                    User.firebaseReference = FirebaseDatabase.getInstance().getReference();
                                    User.friends = friends;
                                    User.isOnline = true;

                                    /* User photo */
                                    Target target = new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                            User.image = bitmap.copy(bitmap.getConfig(), true);
                                            ImageView userPic = (ImageView) findViewById(R.id.profileImageView);
                                            userPic.setImageBitmap(User.image);

                                            TextView hello = (TextView) findViewById(R.id.editText);
                                            hello.setVisibility(View.VISIBLE);
                                            hello.setText("Welcome, "+ User.name + "!");

                                            User.firebaseReference.child("users").child(User.id).updateChildren(User.getMapForFirebase());


                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new Handler().postDelayed(new Runnable(){
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    }, 4000);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {

                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    };

                                    Picasso.with(getApplicationContext()).load(pictureUrl).into(target);


                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,picture.width(300).height(300),friends");
                    request.setParameters(parameters);
                    request.executeAsync();


                } else {

                }
            }
        };


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.email_sign_in_button);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FBK", "facebook:onSuccess:" + loginResult);
                LoginButton login_button = (LoginButton) findViewById(R.id.email_sign_in_button);
                login_button.setVisibility(View.INVISIBLE);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FBK", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FBK", "facebook:onError", error);
                Toast.makeText(LoginActivity.this, "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                // ...
            }
        });


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FBK", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("FBK", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
//                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

