package com.hacktoberapp.voiceconnectz.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.hacktoberapp.voiceconnectz.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Login extends AppCompatActivity {

    //private String TAG = this.getLocalClassName() ;
    private SignInButton signInButton;
    private GoogleSignInClient googleSignInClient;
    private GoogleApi mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    String web_client_id = getResources().getString(R.string.web_client_id);
    String web_client_secret = getResources().getString(R.string.web_client_secret);

    String django_client_id = getResources().getString(R.string.django_client_id);
    String django_client_secret = getResources().getString(R.string.django_client_secret);

    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences("TOKENS", Context.MODE_PRIVATE);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_client_id)
                .requestServerAuthCode(web_client_id)
                .requestEmail()
                .build();

        googleSignInClient= GoogleSignIn.getClient(this,gso);



        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();

            }
        });







    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(this.getLocalClassName(), "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


    private void  updateUI(GoogleSignInAccount account){
        Toast.makeText(this,"SUCCESS",Toast.LENGTH_SHORT).show();
        Log.w(this.getLocalClassName(), "updateUI:::SUCCESS"  +"AUTH CODE"+account.getServerAuthCode()+ " \nID TOKEN OBJ : "+account+" \nID TOKEN : "+account.getIdToken()+" \nEMAIL : "+account.getEmail()+" \nNAME : "+account.getDisplayName());

        getAccessToken(account.getServerAuthCode());




    }



    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void getAccessToken(String authCode){

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("client_id",this.web_client_id)
                .add("client_secret",this.web_client_secret)
                .add("redirect_uri","")
                .add("code",authCode)
                .build();
        final Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("onFailure", e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);

                    // Save it in SP

                    SharedPreferences.Editor editor = sharedPreferences.edit();


                    editor.putString("google_access_token",jsonObject.getString("access_token"));
//                    editor.putString("google_refresh_token",jsonObject.getString("refresh_token"));
                    editor.apply();


                    // get the DRF token too....


                    this.getDRFToken(jsonObject.getString("access_token"));









                    Log.i("onResponse", message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void getDRFToken(String access_token) {

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody1 = new FormEncodingBuilder()
                        .add("grant_type", "convert_token")
                        .add("client_id",django_client_id)
                        .add("client_secret",django_client_secret)
                        .add("backend","google-oauth2")
                        .add("token",access_token)
                        .build();
                final Request request = new Request.Builder()
                        .url("http://192.168.2.106:8000/auth/convert-token")
                        .post(requestBody1)
                        .build();



                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e("onFailure2", e.toString());
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            final String message = jsonObject.toString(5);


                            SharedPreferences.Editor editor = sharedPreferences.edit();


                            editor.putString("drf_access_token",jsonObject.getString("access_token"));
                            editor.putString("drf_refresh_token",jsonObject.getString("refresh_token"));

                            editor.apply();





                            Log.i("onResponse----->DRF", message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });








            }
        });









    }








}