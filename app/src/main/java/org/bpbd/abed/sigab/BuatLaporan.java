package org.bpbd.abed.sigab;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BuatLaporan extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_ACCES_FINE_LOCATION = 10 ;
    private static final int MY_PERMISION_REQUEST_FINE_LOCATION = 12 ;
    private static final int GALERY_REQUEST = 1 ;

    private GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private boolean permissiongranted = false;

    private ImageButton mSelectImage;
    private Uri mImageUri = null;

    private Spinner mPostJenisBencana;
    private EditText mPostWaktuKejadian;
    private EditText mPostLat;
    private EditText mPostLang;
    private EditText mPostKorbanMeninggal;
    private EditText mPostLukaBerat;
    private EditText mPostLukaRingan;
    private EditText mPOstDampakInfrastruktur;

    private Button mButtonnSubmit;

    //firebase
    private StorageReference mStorage;
    private DatabaseReference mDatabase,mDatabasejabatan,mDatabaseNotifikasi;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            Toast.makeText(this, "Sempurna !!!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_buat_laporan);
            initmap();

            // location_manager();
        } else {
            // Google Maps Not Supported
        }


        Spinner spinner_jenis_bencana = (Spinner) findViewById(R.id.jenis_bencana);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.jenis_bencana, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_jenis_bencana.setAdapter(adapter);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pelaporan");
        mDatabasejabatan = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseNotifikasi = FirebaseDatabase.getInstance().getReference().child("Notifikasi");
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        mSelectImage = (ImageButton) findViewById(R.id.imageButtonSelect);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galeryintent = new Intent(Intent.ACTION_GET_CONTENT);
                galeryintent.setType("image/*");
                startActivityForResult(galeryintent, GALERY_REQUEST);

            }
        });

        mPostJenisBencana = (Spinner) findViewById(R.id.jenis_bencana);
        mPostWaktuKejadian = (EditText) findViewById(R.id.editText_waktu_kejadian);
        mPostKorbanMeninggal = (EditText) findViewById(R.id.editTextKorbanMeninggal);
        mPostLukaBerat = (EditText) findViewById(R.id.editTextLukaBerat);
        mPostLukaRingan = (EditText) findViewById(R.id.editTextLukaRingan);
        mPOstDampakInfrastruktur = (EditText) findViewById(R.id.editTextDampakInfrastruktur);

        mPostLat = (EditText) findViewById(R.id.editTextLat);
        mPostLang = (EditText) findViewById(R.id.editTextLng);

        mButtonnSubmit = (Button) findViewById(R.id.buttonSubmit);
        mProgressDialog = new ProgressDialog(this);

        mButtonnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALERY_REQUEST && resultCode == RESULT_OK){

           mImageUri = data.getData();

            mSelectImage.setImageURI(mImageUri);

        }
    }

    private void startPosting() {
        mProgressDialog.setMessage("Posting ....");
        mProgressDialog.show();

        final String title_jenis_bencana = mPostJenisBencana.getSelectedItem().toString().trim();
        final String title_waktu_kejadian = mPostWaktuKejadian.getText().toString().trim();
        final String title_korban_meninggal = mPostKorbanMeninggal.getText().toString().trim();
        final String title_luka_berat = mPostLukaBerat.getText().toString().trim();
        final String title_luka_ringan = mPostLukaRingan.getText().toString().trim();
        final String title_dampak_infrastruktur = mPOstDampakInfrastruktur.getText().toString().trim();
        final String title_lat = mPostLat.getText().toString().trim();
        final String title_lng = mPostLang.getText().toString().trim();


        if (!TextUtils.isEmpty(title_waktu_kejadian) &&
                !TextUtils.isEmpty(title_korban_meninggal) &&
                !TextUtils.isEmpty(title_luka_berat) &&
                !TextUtils.isEmpty(title_luka_ringan) &&
                !TextUtils.isEmpty(title_dampak_infrastruktur)){

            StorageReference filepath = mStorage.child("pelaporan_image").child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")  Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final String key = mDatabase.push().getKey();
                    DatabaseReference newLaporan = mDatabase.child(key);

                    newLaporan.child("key").setValue(key);
                    newLaporan.child("jenis_bencana").setValue(title_jenis_bencana);
                    newLaporan.child("waktu_kejadian").setValue(title_waktu_kejadian);
                    newLaporan.child("korban_meninggal").setValue(title_korban_meninggal);
                    newLaporan.child("luka_berat").setValue(title_luka_berat);
                    newLaporan.child("luka_ringan").setValue(title_luka_ringan);
                    newLaporan.child("dampak_infrastruktur").setValue(title_dampak_infrastruktur);
                    newLaporan.child("lat").setValue(title_lat);
                    newLaporan.child("lng").setValue(title_lng);


                    final String uid = mAuth.getCurrentUser().getUid();
                    mDatabasejabatan.child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String,String> map =  (Map<String, String>) dataSnapshot.getValue();

                            String foto = map.get("foto");
                            String nama = map.get("nama");

                            DatabaseReference newlaporan = mDatabase.child(key);
                            newlaporan.child("foto").setValue(foto);
                            newlaporan.child("nama").setValue(nama);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    newLaporan.child("uid").setValue(uid);
                    newLaporan.child("verifikasi_camat_lurah").setValue("belum_belum");
                    newLaporan.child("verifikasi_lurah").setValue("belum");
                    newLaporan.child("verifikasi_camat").setValue("belum");

                    newLaporan.child("image").setValue(downloadUrl.toString());

                    String key_notifikasi = mDatabaseNotifikasi.push().getKey();

                    mDatabaseNotifikasi.child(key_notifikasi).child("title").setValue(title_jenis_bencana);
                    mDatabaseNotifikasi.child(key_notifikasi).child("status").setValue("belum");

                    mProgressDialog.dismiss();

                    Intent laporan = new Intent(BuatLaporan.this,c_l_dashboard.class);
                    startActivity(laporan);
                }
            });

        }else{
            Toast.makeText(this,"Tolong Lengkapi Data Diatas", Toast.LENGTH_LONG).show();
        }


    }


    public void initmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog_api = api.getErrorDialog(this, isAvailable, 0);
            dialog_api.show();
        } else {
            Toast.makeText(this, "Tidak bisa menyambung ke PLAY SERVICES", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        //LatLng sydney = new LatLng(-34 , 151);
        //mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Sydney"));
        // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
         //goToLocationZoom(39.008224,-76.8984527 , 15);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Jika Aplikasi Tidak Mendapatkan Permision
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCES_FINE_LOCATION);
                }
//                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
//                    Toast.makeText(this," Membutuhkan Persetujuan Pengguna Untuk Mengakses Lokasi Anda", Toast.LENGTH_SHORT).show();
//                }
//
//              ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCES_FINE_LOCATION);

            }else{
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
               ///mGoogleMap.setMyLocationEnabled(true);

               // mGoogleApiClient.connect();
                return;
            }


//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
            //mGoogleMap.setMyLocationEnabled(true);

            mGoogleApiClient.connect();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
       switch (requestCode){
           case REQUEST_ACCES_FINE_LOCATION :
               if (grantResults.length > 0
                       && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   initmap();
               }else{
                   if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                       new AlertDialog.Builder(this)
                               .setTitle("Meminta izin untuk mengakses lokasi")
                               .setMessage("Kamu membutuhkan izin untuk membaca lokasi").show();
                   }else{
                       new AlertDialog.Builder(this)
                               .setTitle("Meminta izin untuk mengakses lokasi ditolak")
                               .setMessage("Kamu menolak untuk memberikan izin mengakses lokasi").show();
                   }
               }

               break;
       }
    }

    private void goToLocation(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mGoogleMap.moveCamera(update);
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.animateCamera(update);


    }

    Marker marker;

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editTextGoogleMaps);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location , 1);
        Address address = list.get(0);
        String locality = address.getLocality()+ ",";
        locality += address.getSubLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        String lat_2 = String.valueOf(lat);
        String lng_2 = String.valueOf(lng);
        goToLocationZoom(lat, lng, 20);

        setMarker(locality, lat, lng);


        EditText edittextLat = (EditText) findViewById(R.id.editTextLat);
        edittextLat.setText(lat_2,TextView.BufferType.EDITABLE);
        EditText edittextLng = (EditText) findViewById(R.id.editTextLng);
        edittextLng.setText(lng_2,TextView.BufferType.EDITABLE);

    }

    private void setMarker(String locality, double lat, double lng) {
        if (marker != null){
            marker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                                    .title(locality)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    .position(new LatLng(lat ,lng))
                                    .snippet("Iam Here");
        marker =  mGoogleMap.addMarker(options);
    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            Toast.makeText(this, "Tidak Bisa Mendapatkan Lokasi ", Toast.LENGTH_LONG).show();
        }else{
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,15);
            mGoogleMap.animateCamera(update);
        }
    }






}
