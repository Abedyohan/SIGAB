package org.bpbd.abed.sigab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class a_login extends AppCompatActivity implements View.OnClickListener {


    private EditText mEmail , mPassword;
    private Button buttonRegister , mButtonLogin;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_login);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);


        mEmail = (EditText) findViewById(R.id.editTextUsername);
        mPassword = (EditText) findViewById(R.id.editTextPassword);

        buttonRegister = (Button) findViewById(R.id.buttonDaftarBaru);
        mButtonLogin = (Button) findViewById(R.id.buttonLogin);

        //Cek apakah user sudah login atau belum
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(a_login.this, c_l_dashboard.class));
                }

            }
        };


        buttonRegister.setOnClickListener(this);

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               signin();

            }
        });

    }

    private void signin(){

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(a_login.this, "Mohon Lengkapi Data Email dan Password", Toast.LENGTH_LONG).show();
        }else{
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(!task.isSuccessful()){
                        Toast.makeText(a_login.this," Login gagal",Toast.LENGTH_LONG).show();
                    }else{
                        startActivity(new Intent(a_login.this, c_l_dashboard.class));
                    }

                }
            });
        }



    }


    private void registerUser(){
        String username = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)){
            //username kosong
            Toast.makeText(this, "Kolom username tidak boleh kosong", Toast.LENGTH_SHORT).show();
            //memberhentikan fungsi untuk eksekusi lebih lanjut
            return;
        }

        if (TextUtils.isEmpty(password)){
            //password kosong
            Toast.makeText(this, "Kolom password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            //memberhentikan fungsi untuk eksekusi lebih lanjut
            return;
        }
        progressDialog.setMessage("Mendaftarkan user...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(username,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Berhasih mendaftar ke dalam sistem
                            //Selanjutnya akan masuk ke halaman utama
                            Toast.makeText(a_login.this , "Berhasil melakukan Login", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(a_login.this, c_l_dashboard.class));
                        }else{
                            Toast.makeText(a_login.this , "Username atau Password salah", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if(v == buttonRegister){
//            registerUser();
          startActivity(new Intent(a_login.this, Registrasi.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }
}
