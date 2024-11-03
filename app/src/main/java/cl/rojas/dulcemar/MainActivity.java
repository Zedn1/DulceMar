package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText correoIngresado = (EditText) findViewById(R.id.iniciarSesionCorreo);
        EditText claveIngresada = (EditText) findViewById(R.id.iniciarSesionClave);
        Button botonIniciarSesion = (Button) findViewById(R.id.botonIngresoSesion);
        Button botonVolverInicio = (Button) findViewById(R.id.botonIrRegistrarse);

        mAuth = FirebaseAuth.getInstance();


        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correoCapturado = correoIngresado.getText().toString();
                String claveCapturada = claveIngresada.getText().toString();

                if (correoCapturado.isEmpty() && claveCapturada.isEmpty()){
                    Toast.makeText(MainActivity.this, "Ingrese sus datos", Toast.LENGTH_SHORT).show();
                }else {
                    loginUser(correoCapturado, claveCapturada);
                }
            }
        });

        botonVolverInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistroUsuario.class));
            }
        });
    }

    private void loginUser(String correoCapturado, String claveCapturada){
        mAuth.signInWithEmailAndPassword(correoCapturado, claveCapturada).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    finish();
                    startActivity(new Intent(MainActivity.this, MenuPrincipal.class));
                    Toast.makeText(MainActivity.this, "Bienvenido a DulceMar", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Error, credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }   
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show();
            }
        });
    }
}