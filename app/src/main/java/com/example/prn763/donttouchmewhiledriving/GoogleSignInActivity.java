package com.example.prn763.donttouchmewhiledriving;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;


/**
 * Created by PRN763 on 2/13/2018.
 */

public class GoogleSignInActivity extends AppCompatActivity {
    private final static String TAG =  "GoogleSignInActivity";
    private SignInButton mGoogleSinginButton;
    private final static int RC_SIGN_IN = 2;
    private GoogleApiClient mGoogleSignInClient;
    private TextView mUserNameTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_signin_activity);
        mGoogleSinginButton = findViewById(R.id.signinButton);
        mUserNameTextView = findViewById(R.id.userNameTextView);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                      .requestEmail()
                                        .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                                  .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                                        @Override
                                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                            //to do
                                        }
                                    })
                                  .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                  .build();

        View.OnClickListener signinListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        mGoogleSinginButton.setOnClickListener(signinListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account != null){
            mUserNameTextView.setText(account.getDisplayName());
        }else{
            signIn();
        }


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                Log.d(TAG,"Email:"+account.getEmail());
            }else{
                //handle failure signin
            }
        }
    }
}
