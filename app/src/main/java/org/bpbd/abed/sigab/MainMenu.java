package org.bpbd.abed.sigab;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MainMenu extends AppCompatActivity {

    private RecyclerView mBlogList;

    private DatabaseReference mDatabase,mDatabaseNotifikasi,mDatabasePengguna,mDataBasePopNotifikasi;

    private FirebaseAuth mAuth;

    private Button mButtonLaporanBaru;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pelaporan");
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabaseNotifikasi = FirebaseDatabase.getInstance().getReference().child("Pelaporan");

        mButtonLaporanBaru = (Button) findViewById(R.id.buttonLaporanBaru);


        mBlogList = (RecyclerView) findViewById(R.id.notifikasi_list_semua);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mButtonLaporanBaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent laporanbaru = new Intent(MainMenu.this,BuatLaporan.class);
                startActivity(laporanbaru);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseRecyclerAdapter<MainMenuRow , NotifikasiViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MainMenuRow, NotifikasiViewHolder>(

                MainMenuRow.class,
                R.layout.notifikasi_row,
                NotifikasiViewHolder.class,
                mDatabase

        ) {
            @Override
            protected void populateViewHolder(NotifikasiViewHolder viewHolder, MainMenuRow model, int position) {
                viewHolder.setJenisBencana(model.getJenis_bencana());
                viewHolder.setNama(model.getNama());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setFoto_Profile(getApplicationContext(), model.getFoto());
                viewHolder.setButtonDetail(model.getKey());
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }



    public static class NotifikasiViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public NotifikasiViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }
        //Menaruh semua elemen dalam database ke dalam tampilan
        public void setJenisBencana(String JenisBencana){
            TextView textViewJenisBencana = (TextView) mView.findViewById(R.id.textViewJenisBencana);
            textViewJenisBencana.setText(JenisBencana);
        }
        public void setNama(String Nama){
            TextView textNama = (TextView) mView.findViewById(R.id.nama_text);
            textNama.setText(Nama);
        }

        public void setImage(Context ctx , String image){
            ImageView post_image = (ImageView) mView.findViewById(R.id.image_notifikasi_jenis_bencana);
            Picasso.with(ctx).load(image).into(post_image);
        }

        public void setFoto_Profile(Context ctx ,String foto){
            ImageView post_image = (ImageView) mView.findViewById(R.id.image_user);
            Picasso.with(ctx).load(foto).into(post_image);
        }

        public void setButtonDetail(final String key){
            Button detail = (Button) mView.findViewById(R.id.buttonDetail);
            final Context ctx = itemView.getContext();
            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, detail_laporan.class);
                    intent.putExtra("EXTRA_SESSION_ID", key);
                    ctx.startActivity(intent);
                }
            });

        }
    }
}
