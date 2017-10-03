package org.bpbd.abed.sigab;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class detail_laporan extends AppCompatActivity {

    private DatabaseReference mDatabase,mDatabaseJabatan;

    private Button mButtonTerima , mButtonTolak ;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_laporan);


        final String s = getIntent().getStringExtra("EXTRA_SESSION_ID");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pelaporan");
        mDatabaseJabatan = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        final String uid = mAuth.getCurrentUser().getUid();


        mButtonTerima = (Button) findViewById(R.id.buttonTerima) ;
        mButtonTolak = (Button) findViewById(R.id.buttonTolak) ;


        mDatabase.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map =  (Map<String, String>) dataSnapshot.getValue();

                String jenis_bencana = map.get("jenis_bencana");
                String nama = map.get("nama");
                String foto = map.get("foto");
                String image = map.get("image");
                String korban_meninggal = map.get("korban_meninggal");
                String luka_berat = map.get("luka_berat");
                String luka_ringan = map.get("luka_ringan");
                String dampak_infrastruktur = map.get("dampak_infrastruktur");

                //nama dan fotoprofile
                TextView textNama = (TextView) findViewById(R.id.textViewNama);
                textNama.setText(nama);
                CircleImageView foto_profile = (CircleImageView) findViewById(R.id.circleImageView2);
                Picasso.with(detail_laporan.this).load(foto).into(foto_profile);

                //foto kejadian
                ImageView post_image = (ImageView) findViewById(R.id.imageView2);
                Picasso.with(detail_laporan.this).load(image).into(post_image);

                //jenis bencana
                TextView textJenisBencana = (TextView) findViewById(R.id.textViewJenisBencana);
                textJenisBencana.setText(jenis_bencana);

                //korban
                TextView textKorbanmeninggal = (TextView) findViewById(R.id.textViewMeninggal);
                textKorbanmeninggal.setText(korban_meninggal);
                TextView textKorbanLukaberat = (TextView) findViewById(R.id.textViewLukaBerat);
                textKorbanLukaberat.setText(luka_berat);
                TextView textKorbanLukaRingan = (TextView) findViewById(R.id.textViewLukaRingan);
                textKorbanLukaRingan.setText(luka_ringan);

                //dampak infrastruktur
                TextView textDampakInfraStruktur = (TextView) findViewById(R.id.textViewDampakInfrastrtuktur);
                textDampakInfraStruktur.setText(dampak_infrastruktur);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseJabatan.child(uid).child("jabatan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String jabatan = dataSnapshot.getValue().toString();
                if (jabatan == "1"){
                    mButtonTerima.setVisibility(View.GONE);
                    mButtonTolak.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mButtonTerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(s).child("uid_camat_verifikasi").setValue("256_sudah");
                mDatabase.child(s).child("verfikasi_camat").setValue("terima");
            }
        });
        mButtonTolak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(s).child("uid_camat_verifikasi").setValue("256_tidak_diterima");
                mDatabase.child(s).child("verifikasi_camat").setValue("tolak");
            }
        });

    }

}
