package pe.edu.ulima.biblioul.Clases;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import pe.edu.ulima.biblioul.Login.LoginActivity;


// http://www.androidhive.info/2012/08/android-session-management-using-shared-preferences/
public class Sesion {
    // Shared Preferences
    SharedPreferences pref;
    FirebaseAuth mAuth;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "MagnatechPref";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "nombre";
    public static final String KEY_ASISTENCIA = "Asistencia";
    public static final String KEY_TIPO = "tipo";
    public static final String KEY_ID = "id";
    public static final String KEY_ACT = "act";
    public static final String KEY_MONEDA = "moneda";


    // Email address (make variable public to access from outside)


    // Constructor
    public Sesion(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String tipo){

        // Storing name in pref
        //editor.putString(KEY_ASISTENCIA, "NINGUNA");
        editor.putString(KEY_TIPO, tipo);
        //editor.putLong(KEY_ID, id);

        // commit changes
        editor.commit();
    }

    public void putAsistencia(String asistencia){
        editor.putString(KEY_ASISTENCIA, asistencia);
        editor.commit();
    }

    public String getAsistencia(){
        return pref.getString(KEY_ASISTENCIA, "NINGUNA");
    }

    public void deleteAsistencia(){
        editor.remove(KEY_ASISTENCIA);
        editor.commit();
    }

    public void putAct(int flag){
        editor.putInt(KEY_ACT, flag);
        editor.commit();
    }

    public int getAct(){
        return pref.getInt(KEY_ACT, 0);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_TIPO, pref.getString(KEY_TIPO, null));

        // return user
        return user;
    }

    public String getTipo(){
        return pref.getString(KEY_TIPO, null);
    }

    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast.makeText(_context,"Por favor, inicie sesión",
                    Toast.LENGTH_SHORT).show();

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
        mAuth.getInstance().signOut();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        if (mAuth.getInstance().getCurrentUser() != null){
            return true;
        }else{
            return false;
        }

    }
}
