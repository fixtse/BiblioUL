package pe.edu.ulima.biblioul.Login;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


import pe.edu.ulima.biblioul.Clases.Sesion;
import pe.edu.ulima.biblioul.MainActivity;
import pe.edu.ulima.biblioul.R;
import pe.edu.ulima.biblioul.RegistrarActivity;


public class LoginActivity extends AppCompatActivity implements LoginView {


    private EditText eteUsuario, etePassword;
    private ProgressDialog dialog;
    private String usuario;
    private Sesion ses;
    private LoginPresenter presenter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private int cont = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        ses = new Sesion(getApplicationContext());
        if(ses.isLoggedIn()){
            iniciar();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                eteUsuario = (EditText) findViewById(R.id.txtUsuario);
                etePassword = (EditText) findViewById(R.id.txtContra);
                if (user != null) {
                    iniciar();
                }

            }
        };

        Button but = (Button)findViewById(R.id.btnIng);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ProgressDialog(LoginActivity.this);
                //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMessage("Iniciando Sesión...");
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();


                usuario = eteUsuario.getText().toString().trim().toLowerCase()+"@aloe.ulima.edu.pe";
                String password = etePassword.getText().toString();


                setPresenter(new LoginPresenter(LoginActivity.this));

                if (presenter.validar(usuario, password)) {
                    login(usuario, password);
                } else {
                    onLoginFailed();
                }
            }
        });


        Button reg = (Button)findViewById(R.id.btnReg);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, RegistrarActivity.class);
                intent.putExtra("tipe", 1);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void setPresenter(LoginPresenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Falló la autenticación", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else{
                            iniciar();
                        }

                    }
                });
    }

    public void iniciar() {
        Intent intent;

        if(usuario != null) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
            if (dialog != null) {
                dialog.dismiss();
            }

        }

    }

    @Override
    public void errPass(String err) {
        etePassword.setError(err);
    }

    @Override
    public void errMail(String err) {
        eteUsuario.setError(err);
    }

    @Override
    public void onLoginFailed() {
        dialog.dismiss();

    }

    public void onContClicked(View view){
        final AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Restablecer contraseña")
                .setView(R.layout.input_dialog)
                .create();
        d.show();
        final EditText ContenidoTxt= (EditText) d.findViewById(R.id.email);
        Button saveBtn= (Button) d.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=ContenidoTxt.getText().toString().toLowerCase().trim()+"@aloe.ulima.edu.pe";
                mAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email de reseteo enviado a "+email, Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                d.hide();
            }
        });

    }

    /*public void VerificarIMEI(){

        final TelephonyManager mngr = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        //mAuth = FirebaseAuth.getInstance();
        if (mAuth != null){


        DatabaseReference imeiRef = database.getReference().child("usuarios").child(mAuth.getCurrentUser().getUid()).child("IMEI");
        imeiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    String IMEI = dataSnapshot.getValue(String.class);
                    if (IMEI != null){
                        if (IMEI.equals(mngr.getDeviceId())){
                            Intent intent;
                            intent = new Intent(getApplicationContext(), mainActivity.class);
                            startActivity(intent);
                            finish();
                            if(dialog != null){
                                dialog.dismiss();
                            }


                        }else{
                                if(dialog != null){
                                dialog.dismiss();
                                Snackbar.make(getCurrentFocus(), "Solo puedes iniciar sesión desde tu propio celular", Snackbar.LENGTH_SHORT)
                                        .show();
                            }



                        }
                    }else{
                        Intent intent;
                        intent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intent);
                        finish();
                        if(dialog != null){
                            dialog.dismiss();
                        }
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }

    }


    public void checkPermisosIMEI(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            // No se encuentran dados los permisos
            // Preguntamos si mostramos una explicación de los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)){
                // Se debe de mostrar un mensaje al usuario explicandole de los permisos
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},
                        ID_IMEI);
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},
                        ID_IMEI);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case ID_IMEI:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }else {
                    checkPermisosIMEI();
                }
                break;
        }
    }*/

}
