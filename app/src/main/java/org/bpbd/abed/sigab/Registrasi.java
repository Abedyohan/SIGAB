package org.bpbd.abed.sigab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Registrasi extends AppCompatActivity {

    private EditText mNama ;
    private EditText mNIK ;
    private EditText mAlamat ;
    private EditText mNoHP ;
    private EditText mEmail ;
    private EditText mPassword ;
    private EditText mConfirmPassword ;
    private EditText mKodeJabatan;

    private ImageButton mFotoProfil;
    private Uri mUri = null;

    private Button mButtonBuatAkun;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mDatabase,mDatabaseJabatan;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    private static final int GALERY_REQUEST = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseJabatan = FirebaseDatabase.getInstance().getReference().child("Jabatan");
        mStorage = FirebaseStorage.getInstance().getReference();

        mProgressDialog = new ProgressDialog(this);

        mNama = (EditText) findViewById(R.id.editTextNamaLengkap);
        mNIK = (EditText) findViewById(R.id.editTextNIK);
        mAlamat = (EditText) findViewById(R.id.editTextAlamat);
        mNoHP = (EditText) findViewById(R.id.editTextNoHandphone);
        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mConfirmPassword = (EditText) findViewById(R.id.editTextConfirmPassword);
        mKodeJabatan = (EditText) findViewById(R.id.editTextKodeJabatan);

        mFotoProfil = (ImageButton) findViewById(R.id.imageButtonFoto);
        mFotoProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galeryintent = new Intent(Intent.ACTION_GET_CONTENT);
                galeryintent.setType("image/*");
                startActivityForResult(galeryintent, GALERY_REQUEST);

            }
        });

        mButtonBuatAkun = (Button) findViewById(R.id.buttonBuatAkun);

        mButtonBuatAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALERY_REQUEST && resultCode == RESULT_OK){

            mUri = data.getData();

            mFotoProfil.setImageURI(mUri);
        }
    }

    private void signup() {


        final String nama = mNama.getText().toString().trim();
        final String nik = mNIK.getText().toString().trim();
        final String alamat = mAlamat.getText().toString().trim();
        final String nohp = mNoHP.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmpassword = mConfirmPassword.getText().toString().trim();
        final String jabatan = mKodeJabatan.getText().toString().trim();

        if (TextUtils.isEmpty(nama) ||
                TextUtils.isEmpty(nik) ||
                TextUtils.isEmpty(alamat) ||
                TextUtils.isEmpty(nohp) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmpassword)){

            Toast.makeText(this,"Data belum lengkap", Toast.LENGTH_LONG).show();
        }else{
            mProgressDialog.setMessage("Mendaftarkan User");
            mProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        final String uid = mAuth.getCurrentUser().getUid();

                        StorageReference filepath = mStorage.child("user_propic").child(uid);
                        if(mUri != null){
                            filepath.putFile(mUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    @SuppressWarnings("VisibleForTests")  Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                    DatabaseReference newUser = mDatabase.child(uid);

                                    newUser.child("nik").setValue(nik);
                                    newUser.child("nama").setValue(nama);
                                    newUser.child("alamat").setValue(alamat);
                                    newUser.child("nohp").setValue(nohp);
                                    newUser.child("foto").setValue(downloadUrl.toString());
                                    newUser.child("jabatan").setValue(jabatan);

                                    mDatabaseJabatan.child(jabatan).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DatabaseReference newUser = mDatabase.child(uid);
                                            String value = dataSnapshot.getValue(String.class);
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                    mProgressDialog.dismiss();
                                }
                            });
                        }else{

                            DatabaseReference newUser = mDatabase.child(uid);

                            newUser.child("nik").setValue(nik);
                            newUser.child("nama").setValue(nama);
                            newUser.child("alamat").setValue(alamat);
                            newUser.child("nohp").setValue(nohp);
                            newUser.child("foto").setValue("");
                            newUser.child("jabatan").setValue(jabatan);
                        }
                    }else{
                        Toast.makeText(Registrasi.this,"Gagal dalam membuat akun", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }
                }
            });
        };



    }
}
