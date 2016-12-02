package pe.edu.ulima.biblioul.Login;

import android.util.Patterns;

public class LoginPresenter {

    private LoginView loginView;

    public LoginPresenter(LoginView loginView) {
        this.loginView = loginView;
    }

    public boolean validar(String email, String password){
        boolean valid = true;


        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            loginView.errMail("Ingresa un email valido");
            valid = false;
        }else{
            loginView.errMail(null);
        }

        if(password.isEmpty() || password.length() < 4 || password.length() > 10){
            loginView.errPass("Ingrese un valor alfanumerico entre 4 y 10 carácteres");
            valid = false;
        }else{
            loginView.errPass(null);
        }

        return valid;
    }


}
