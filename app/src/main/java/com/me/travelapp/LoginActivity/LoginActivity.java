package com.me.travelapp.LoginActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.me.travelapp.MainActivity;
import com.me.travelapp.POJO.UserData;
import com.me.travelapp.R;
import com.me.travelapp.javafiles.SplashActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText username;
    private EditText password;
    private Button signIN;
    private ProgressBar progressBar;
    private TextView forgotTextView;
    private TextView signUpTextView;
    private RelativeLayout homeLayout;
    private boolean checked=false;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window= getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }

        homeLayout=findViewById(R.id.sign_out_and_disconnect);
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);

        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        signIN=findViewById(R.id.login_btn);
        signIN.setEnabled(false);
        progressBar=findViewById(R.id.login_progress);
        forgotTextView=findViewById(R.id.forgot_btn);
        signUpTextView=findViewById(R.id.signUpText);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btnStateChange();
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btnStateChange();
            }
        });

//        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.INVISIBLE);

        String bold="Get help signing in.";
        String normal="Forgot your login details? ";
        SpannableString str=new SpannableString(normal+bold);
        str.setSpan(new StyleSpan(Typeface.BOLD),normal.length(),normal.length()+bold.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forgotTextView.setText(str);
        forgotTextView.setTextColor(Color.DKGRAY);
        forgotTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),ForgotPassword.class);
                startActivity(intent);
            }
        });

        bold="Sign Up.";
        normal="Don't have an account? ";
        str=new SpannableString(normal+bold);
        str.setSpan(new StyleSpan(Typeface.BOLD),normal.length(),normal.length()+bold.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpTextView.setText(str);
        signUpTextView.setTextColor(Color.DKGRAY);
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth=FirebaseAuth.getInstance();
        Log.w(TAG, "onCreate: "+mAuth.getCurrentUser() );
//        boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
//        Log.w(TAG, "onCreate: "+loggedIn );

        if(mAuth.getCurrentUser()==null)
        {
            updateUI(mAuth.getCurrentUser());
        }
        else if(mAuth.getCurrentUser().isEmailVerified())
            updateUI(mAuth.getCurrentUser());
        else
        {
            if(!checked)
                Snackbar.make(homeLayout,"Verify email!!!",Snackbar.LENGTH_SHORT).show();
        }
    }


    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(user.getDisplayName());
//            mDetailTextView.setText(user.getPhotoUrl().toString());
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            findViewById(R.id.forgot_btn).setVisibility(View.GONE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);
            findViewById(R.id.forgot_btn).setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: "+requestCode);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
//                updateUI(account);
            } catch (ApiException e) {
                Log.d(TAG, "onActivityResult: ApiException  "+e);
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserData.addUser(user.getUid(),user.getDisplayName(),"",user.getEmail(),"google");
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI((FirebaseUser) null);

                        }
                    }
                });
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }


    void btnStateChange()
    {
        signIN.setText("Log In");
        if(!password.getText().toString().equals("")&&!username.getText().toString().equals(""))  //username.getText().toString()!=null&&password.getText().toString()!=null&&
        {
            buttonOn();
        }
        else
        {
            buttonOff();
        }
    }

    void buttonOn()
    {
        signIN.setEnabled(true);
        signIN.setBackgroundResource(R.drawable.login_button_on_bg);
        signIN.setAlpha(1);
        signIN.setTextColor(Color.WHITE);
    }

    void buttonOff()
    {
        signIN.setEnabled(false);
        signIN.setBackgroundResource(R.drawable.login_button_bg);
        signIN.setAlpha(0.7f);
        signIN.setTextColor(getResources().getColor(R.color.colorPrimary));

    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.disconnect_button) {
            revokeAccess();
        } else if (i == R.id.login_btn) {
            signIN.setText("");
            signIN.setBackgroundResource(R.drawable.login_button_bg);
            signIN.setAlpha(0.7f);
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(username.getText().toString().trim(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user.isEmailVerified())
                                {
                                    updateUI(user);
                                }
                                else
                                {
                                    Snackbar.make(homeLayout,"Verify email and try again!!!",Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    password.setError("Invalid Password");
                                    password.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    username.setError("Invalid");
                                    username.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
//                                    Toast.makeText(LoginActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Snackbar.make(homeLayout,"Authentication failed",Snackbar.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());

                                }
                            }
                            signIN.setText("Log In");
                            progressBar.setVisibility(View.INVISIBLE);
                            buttonOn();
                        }
                    });

        }
    }


}
