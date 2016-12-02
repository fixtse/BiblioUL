package pe.edu.ulima.biblioul.Login;

/**
 * Created by fixt on 06/08/16.
 */
public interface LoginView {

    public void login(String email, String password);
    public void errPass(String err);
    public void errMail(String err);
    public void onLoginFailed();
}
