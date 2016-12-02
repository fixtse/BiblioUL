package pe.edu.ulima.biblioul;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import pe.edu.ulima.biblioul.Clases.Sesion;
import pe.edu.ulima.biblioul.Fragments.fragMain;

public class MainActivity extends AppCompatActivity {

    private Sesion ses;
    private View headerView;
    private TextView txtUsuario, txtMonedas;
    private CircleImageView imgUsuario;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private FirebaseDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("BiblioUL");
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        nvDrawer = (NavigationView) findViewById(R.id.nav_view);

        setupDrawerContent(nvDrawer);


        headerView = nvDrawer.inflateHeaderView(R.layout.nav_header);

        drawerToggle = setupDrawerToggle();

        mDrawer.addDrawerListener(drawerToggle);

        txtUsuario = (TextView) headerView.findViewById(R.id.navNom);
        imgUsuario = (CircleImageView) headerView.findViewById(R.id.navImg);
        txtMonedas = (TextView)headerView.findViewById(R.id.txtCoin);

        ses = new Sesion(getApplicationContext());
        ses.checkLogin();

        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        txtUsuario.setText("" + users.getDisplayName());



        nvDrawer.inflateMenu(R.menu.menu_main);

        database = FirebaseDatabase.getInstance();

        setFoto(users);
        checkearVersion();

        //txtUsuario.setText("Usuario: " );
        //imgUsuario.setImageResource(R.drawable.diego);


        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(R.anim.slidein_left, R.anim.slideout_right);
        tx.replace(R.id.flaContenido, new fragMain());
        tx.commit();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass =null;
        switch(menuItem.getItemId()) {
            case R.id.m1:
                fragmentClass = fragMain.class;
                break;
            case R.id.m2:
                //fragmentClass = frag_tabactividad.class;
                fragmentClass = fragMain.class;
                break;
            case R.id.CSesion:
                //ses.logoutUser();
                dialogSesion();
                break;
            default:
                fragmentClass = fragMain.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (menuItem.getItemId()!=R.id.CSesion){
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.slidein_left, R.anim.slideout_right);
            ft.replace(R.id.flaContenido,fragment).commit();
            //menuItem.setChecked(true);
            // Set action bar title
            setTitle(menuItem.getTitle());

        }

        mDrawer.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ses.getAct()==1){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            setFoto(user);
            txtUsuario.setText("" + user.getDisplayName());
            ses.putAct(0);
        }
        DatabaseReference UserRef = database.getReference().child("usuarios").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        UserRef.child("Monedas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long monedas = dataSnapshot.getValue(Long.class);
                if(monedas !=null){
                    txtMonedas.setText(""+ monedas);
                }else{
                    txtMonedas.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setFoto(FirebaseUser user){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://biblioul-5f989.appspot.com");
        StorageReference imagesRef = storageRef.child("Fotos Usuarios");

        String usuario = user.getEmail();
        usuario = usuario.substring(0,usuario.indexOf("@"));
        /*imagesRef.child(usuario).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgUsuario.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });*/

        imagesRef.child(usuario).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final String urldw = uri.toString();
                Picasso.with(MainActivity.this)
                        .load(urldw)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgUsuario, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                //Try again online if cache failed
                                Picasso.with(MainActivity.this)
                                        .load(urldw)
                                        .error(R.drawable.person)
                                        .into(imgUsuario, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        });

                            }
                        });
            }
        });




    }

    private void dialogSesion(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ses.logoutUser();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea cerrar sesión?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void checkearVersion(){

        final Long versionCode = Long.valueOf(BuildConfig.VERSION_CODE);
        DatabaseReference VersionRef = database.getReference().child("version").child("0");
        VersionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long ver = (Long)dataSnapshot.getValue();
                if (versionCode < ver){
                    if(!((ver/10000) > (versionCode/10000))){
                        Snackbar.make(getCurrentFocus(),"Descarga la última versión", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                })
                                .show();
                    }else{
                        Toast.makeText(MainActivity.this, "Descarga la última versión", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
