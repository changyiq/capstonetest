package project.capstone6.acne_diagnosis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 10005;
    public static final int GOOGLE_SING_IN_CODE = REQUEST_CODE;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    // Google sign in
    private SignInButton signInButton;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;

    // Facebook sign in
    private LoginButton fbLoginButton;

    // text fields and button for sign up
    private EditText etRegUsername, etRegEmail, etRegPwd;
    private Button btnSignUp;
    // text view to turn to log in page
    private TextView turnToLogin;

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signInButton = findViewById(R.id.googleSignIn);
        fbLoginButton = findViewById(R.id.fbLogInButton);
        fbLoginButton.setReadPermissions("email", "public_profile");
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPwd = findViewById(R.id.etRegPwd);
        btnSignUp = findViewById(R.id.btnSignUp);
        turnToLogin = findViewById(R.id.turnToLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        // check user log in/out status, if the user is logged in, directly go to main activity
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // -----------------------------------Google Authentication---------------------------------
        // google sign in
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("520981538611-b1e1cdgu0mhcem9i4g96rrm7cch95618.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // check if the user is exit when use google sign in
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){
            startActivity(new Intent(this, TakeSelfie.class));
        }
        signInButton.setOnClickListener(view -> {
            Intent sign = googleSignInClient.getSignInIntent();
            startActivityForResult(sign, GOOGLE_SING_IN_CODE);
        });

        // -----------------------------------Facebook Authentication-------------------------------
        // Facebook sign in
        callbackManager = CallbackManager.Factory.create();

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // check log in credentials
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Error" + error);
                Toast.makeText(RegisterActivity.this, "Error!" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // check facebook authentication status
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if( currentUser != null){
                    startActivity(new Intent(getApplicationContext(), TakeSelfie.class));
                    finish();
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    firebaseAuth.signOut();
                }
            }
        };

        // -----------------------------------Email Sign up------------------------------------
        // register with email
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etRegEmail.getText().toString().trim();
                String password = etRegPwd.getText().toString().trim();

                // error msgs
                if (TextUtils.isEmpty(email)) {
                    etRegEmail.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    etRegPwd.setError("Password is required");
                }
                if (password.length() < 6) {
                    etRegPwd.setError("Password must be more than 6 characters.");
                }

                // register in firebase
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), TakeSelfie.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // -----------------------------------To Log in---------------------------------
        // turn to login page
        turnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    // check google sign in result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SING_IN_CODE){
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount signInAccount = signInTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(getApplicationContext(), "Your Google account is connected to our application", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), TakeSelfie.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    // check facebook valid credentials
    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Your Facebook account is connected to our application", Toast.LENGTH_SHORT).show();
                    //FirebaseUser user = firebaseAuth.getCurrentUser();
                    startActivity(new Intent(getApplicationContext(), TakeSelfie.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Authentication failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}