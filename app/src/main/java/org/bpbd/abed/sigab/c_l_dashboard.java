package org.bpbd.abed.sigab;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class c_l_dashboard extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToogle;

    private RecyclerView mBlogList;
    private Toolbar mToolbar;

    private DatabaseReference mDatabase,mDatabaseNotifikasi,mDatabasePengguna,mDataBasePopNotifikasi;

    private FirebaseAuth mAuth;

    private Button mButtonMainMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_l_dashboard);

        mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
        mToogle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);

        mDrawerLayout.addDrawerListener(mToogle);
        mToogle.syncState();
        mButtonMainMenu = (Button) findViewById(R.id.buttonMainMenu);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pelaporan");
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabaseNotifikasi = FirebaseDatabase.getInstance().getReference().child("Pelaporan");
        mDatabasePengguna = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDataBasePopNotifikasi = FirebaseDatabase.getInstance().getReference().child("Notifikasi");

        mBlogList = (RecyclerView) findViewById(R.id.notifikasi_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mDataBasePopNotifikasi.orderByChild("status").equalTo("belum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(c_l_dashboard.this);
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.sigab_logo_baru);
                builder.setLargeIcon(bm);
                builder.setSmallIcon(R.drawable.sigab_logo_baru);
                builder.setContentTitle("Pemberitahuan Baru");
                builder.setContentText(String.valueOf(count)+" Laporan Bencana");
                Intent intent = new Intent(c_l_dashboard.this, c_l_dashboard.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(c_l_dashboard.this);
                stackBuilder.addParentStack(detail_laporan.class);
                stackBuilder.addNextIntent(intent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NM.notify(0,builder.build());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabaseNotifikasi.orderByChild("verifikasi_camat_lurah").equalTo("belum_belum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                TextView textViewJenisBencana = (TextView) findViewById(R.id.textViewJumlahNotifikasi);
                textViewJenisBencana.setText(String.valueOf(count));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabasePengguna.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map =  (Map<String, String>) dataSnapshot.getValue();

                String nama = map.get("nama");
                String foto = map.get("foto");

                TextView textNama = (TextView) findViewById(R.id.textViewNama);
                textNama.setText(nama);

                CircleImageView post_image = (CircleImageView) findViewById(R.id.circleImageView);
                Picasso.with(c_l_dashboard.this).load(foto).into(post_image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_menu);
            mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case (R.id.Dashboard):
                        Intent accountActivity = new Intent(c_l_dashboard.this, MainMenu.class);
                        startActivity(accountActivity);
                        break;
                    case (R.id.Beranda):
                        Intent beranda = new Intent(c_l_dashboard.this, MainMenu.class);
                        startActivity(beranda);
                        break;
                }
                return true;
            }
        });



        mButtonMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainmenu = new Intent(c_l_dashboard.this, MainMenu.class);
                startActivity(mainmenu);
            }
        });

    }

    // Main Menu clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToogle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStart() {
        super.onStart();

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseRecyclerAdapter<Notifikasi , NotifikasiViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notifikasi, NotifikasiViewHolder>(

                Notifikasi.class,
                R.layout.notifikasi_row,
                NotifikasiViewHolder.class,
                mDatabase.orderByChild("verifikasi_camat_lurah").equalTo("belum_belum")

        ) {
            @Override
            protected void populateViewHolder(NotifikasiViewHolder viewHolder, Notifikasi model, int position) {
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

        public void setImage(Context ctx ,String image){
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
