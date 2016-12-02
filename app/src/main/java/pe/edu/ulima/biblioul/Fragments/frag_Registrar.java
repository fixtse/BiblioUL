package pe.edu.ulima.biblioul.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import pe.edu.ulima.biblioul.Clases.FireBasePushIdGenerator;
import pe.edu.ulima.biblioul.Clases.Sesion;
import pe.edu.ulima.biblioul.MainActivity;
import pe.edu.ulima.biblioul.R;
import pe.edu.ulima.biblioul.datePicker;


public class frag_Registrar extends Fragment {


    private FirebaseDatabase database;
    private Sesion ses;
    private CircleImageView cImg;
    private ProgressDialog dialog;
    static final int REQUEST_CAMERA = 1;
    static final int SELECT_FILE = 2;
    private FirebaseAuth mAuth;
    private EditText nom, ape, cod,telf,fecnac;
    private FirebaseUser user;
    private final int ID_CAMARA=1, ID_IMEI=2;


    public frag_Registrar() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Completar información");
        //setHasOptionsMenu(true); // Seteo que el fragment va a tener su propio menu de opciones
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_registrar, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //getActivity().setRequestedOrientation(
        //        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ses = new Sesion(getContext());

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        /*if ( Build.VERSION.SDK_INT >= 23 ){
                checkPermisosIMEI();
        }*/



        nom = (EditText)getView().findViewById(R.id.txtNom);
        ape = (EditText)getView().findViewById(R.id.txtApe);
        cod = (EditText)getView().findViewById(R.id.txtCod);
        telf = (EditText)getView().findViewById(R.id.txtTelf);
        fecnac = (EditText)getView().findViewById(R.id.fecnac);

        cImg = (CircleImageView)getView().findViewById(R.id.navImg);


        FloatingActionButton fav = (FloatingActionButton)getView().findViewById(R.id.agregarImg);
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d = new AlertDialog.Builder(getContext())
                        //.setTitle("Seleccionar Origen")
                        //.setNegativeButton("Cancel", null)
                        .setItems(new String[]{"Cámara", "Galeria"}, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dlg, int position)
                            {
                                if ( position == 0 )
                                {
                                    //if ( Build.VERSION.SDK_INT < 23 ){
                                        camara();
                                    /*}else{
                                        checkPermisosCamara();
                                    }*/
                                }
                                else if(position == 1){
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                                    startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
                                }

                            }
                        })
                        .create();
                d.show();
            }
        });


        fecnac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new datePicker();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        Button btnGuardar = (Button)getView().findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validar()){
                    dialog = new ProgressDialog(getContext());
                    dialog.setMessage("Guardando información de usuario");
                    dialog.setIndeterminate(true);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    crearUsuario(cod);
                }
            }
        });


        rotar();

        Button btnLimpiar = (Button)getView().findViewById(R.id.btnLimpiar);
        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cImg.setImageResource(R.drawable.person);
                fecnac.setText(null);
                cod.setText(null);
                nom.setText(null);
                ape.setText(null);
                telf.setText(null);
                nom.requestFocus();
                nom.setError(null);
                ape.setError(null);
                telf.setError(null);
                cod.setError(null);
                fecnac.setError(null);

            }
        });

        btnLimpiar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ses.logoutUser();
                Toast.makeText(getActivity(), "Se cerró la sesión de usuario", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    private void rotar(){
        ImageView rot = (ImageView)getView().findViewById(R.id.rotI);
        rot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotarImagen();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // inflater.inflate(R.menu.menu_main, menu);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data){
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                bm = scaleBitmapAndKeepRation(bm,300,300);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cImg.setImageBitmap(bm);

    }

    private void onCaptureImageResult(Intent data){
        Bitmap foto = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        foto = scaleBitmapAndKeepRation(foto,300,300);
        cImg.setImageBitmap(foto);



    }

    public void SubirArchivo(){

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://biblioul-5f989.appspot.com");
        StorageReference imagesRef = storageRef.child("Fotos Usuarios");

        // Get the data from an ImageView as bytes
        cImg.setDrawingCacheEnabled(true);
        cImg.buildDrawingCache();
        Bitmap bitmap = cImg.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        user = mAuth.getInstance().getCurrentUser();
        String usuario = user.getEmail();
        usuario = usuario.substring(0,usuario.indexOf("@"));
        UploadTask uploadTask = imagesRef.child(usuario).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nom.getText().toString().trim() +" "+ ape.getText().toString().trim())
                        .setPhotoUri(downloadUrl)
                        .build();

                user.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        guardar();
                    }
                });
            }
        });

    }

    public static Bitmap scaleBitmapAndKeepRation(Bitmap TargetBmp,int reqHeightInPixels,int reqWidthInPixels)
    {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, TargetBmp.getWidth(), TargetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
        Bitmap scaledBitmap = Bitmap.createBitmap(TargetBmp, 0, 0, TargetBmp.getWidth(), TargetBmp.getHeight(), m, true);
        return scaledBitmap;
    }

    private void rotarImagen(){
        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);  // La rotación debe ser decimal (float o double)
        cImg.setDrawingCacheEnabled(true);
        cImg.buildDrawingCache();
        Bitmap original = cImg.getDrawingCache();

        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        cImg.setImageBitmap(rotatedBitmap);
        cImg.setDrawingCacheEnabled(false);
    }

    private void guardar(){


        DatabaseReference Usuarioref = database.getReference().child("usuarios").child(user.getUid());
        Usuarioref.child("nombre").setValue(nom.getText().toString().trim());
        Usuarioref.child("apellido").setValue(ape.getText().toString().trim());
        Usuarioref.child("cod").setValue(Long.valueOf(cod.getText().toString()));
        FechaNacimiento(Usuarioref,fecnac.getText().toString());
        Usuarioref.child("telf").setValue(Long.valueOf(telf.getText().toString()));
        nom.setText(null);
        ape.setText(null);
        cod.setText(null);
        fecnac.setText(null);
        telf.setText(null);
        dialog.dismiss();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();

    }

    public boolean validar(EditText codigo){
        boolean valid = true;

        if(codigo.getText().toString().isEmpty()){
            codigo.setError("Ingresa un Usuario");
            valid = false;
        }else{
            codigo.setError(null);
        }
        return valid;
    }

    private void crearUsuario(final EditText codigo){

        final String codi = codigo.getText().toString().toLowerCase().trim();
        String fid =  FireBasePushIdGenerator.getFirebaseId();
        mAuth.getInstance().createUserWithEmailAndPassword(codi+"@aloe.ulima.edu.pe",fid)
                .addOnCompleteListener(getActivity(),new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //https://firebase.google.com/docs/auth/android/password-auth
                        user = mAuth.getInstance().getCurrentUser();
                        DatabaseReference Usuarioref = database.getReference().child("usuarios").child(user.getUid());
                        Usuarioref.child("user").setValue(codi);
                        //codigo.setText(null);
                        final String email=codi+"@aloe.ulima.edu.pe";
                        mAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Email de reseteo enviado a "+email, Toast.LENGTH_SHORT).show();

                                            SubirArchivo();
                                            //Snackbar.make(this,"Email de reseteo enviado a "+email, Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });


    }

    private void FechaNacimiento(DatabaseReference Usuarioref, String fechaString){
        int dia = Integer.parseInt(fechaString.substring(0, 2));
        int mes = Integer.parseInt(fechaString.substring(3, 5));
        int ano = Integer.parseInt(fechaString.substring(6, 10));

        Usuarioref.child("fecND").setValue(dia);
        Usuarioref.child("fecNM").setValue(mes);
        Usuarioref.child("fecNA").setValue(ano);

    }

    public boolean validar(){
        boolean valid = true;

        if(nom.getText().toString().isEmpty()){
            nom.setError("Ingresa tu nombre");
            valid = false;
        }else{
            nom.setError(null);
        }

        if(cod.getText().toString().length() != 8 ){
            cod.setError("Ingresa un código válido");
            valid = false;
        }else{
            cod.setError(null);
        }

        if(ape.getText().toString().isEmpty()){
            ape.setError("Ingresa tu Apellido Parterno");
            valid = false;
        }else{
            ape.setError(null);
        }

        if(fecnac.getText().toString().isEmpty()){
            fecnac.setError("Ingresa tu fecha de nacimiento");
            valid = false;
        }else{
            fecnac.setError(null);
        }

        if(telf.getText().toString().length() != 9 ){
            telf.setError("Ingresa tu número de celular");
            valid = false;
        }else{
            telf.setError(null);
        }

        return valid;
    }

    public void camara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /*public void checkPermisosCamara(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
                // No se encuentran dados los permisos
                // Preguntamos si mostramos una explicación de los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)){
                // Se debe de mostrar un mensaje al usuario explicandole de los permisos
                Toast.makeText(getContext(), "Permitir, para poder tomar fotos desde la aplicación" , Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        ID_CAMARA);
            }else{
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        ID_CAMARA);
            }
        }else{
            // Realizamos el servicio
           camara();
        }
    }

    public void checkPermisosIMEI(){

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            // No se encuentran dados los permisos
            // Preguntamos si mostramos una explicación de los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_PHONE_STATE)){
                // Se debe de mostrar un mensaje al usuario explicandole de los permisos
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                        ID_IMEI);
            }else{
               requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                        ID_IMEI);
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case ID_CAMARA:
                // Probamos que nos hayan dado los permisos
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED){
                    // Realizamos la tarea dado que ya me han dado los permisos
                        camara();
                    }
                }
                return;
            case ID_IMEI:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }else {
                    checkPermisosIMEI();
                }
                return;
        }
    }*/


}
