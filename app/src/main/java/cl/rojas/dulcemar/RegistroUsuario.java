package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class RegistroUsuario extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mFireStore;

    String llave = "c7TXI0MJYZicK3VEwfCVY8xGie1byiXP";

    public SecretKeySpec CrearClave(String llave){
        try {
            byte[] cadena = llave.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            cadena = md.digest(cadena);
            cadena = Arrays.copyOf(cadena, 16);
            SecretKeySpec secretkeyspec = new SecretKeySpec(cadena, "AES");
            return secretkeyspec;
        }catch (Exception e){
            return null;
        }
    }

    public String EncriptarContraseña(String clave){
        try {
            SecretKeySpec secretKeySpec = CrearClave(llave);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

                byte[] cadena = clave.getBytes("UTF-8");
                byte[] encriptada = cipher.doFinal(cadena);
                String cadena_encriptada = android.util.Base64.encodeToString(encriptada, android.util.Base64.DEFAULT);
                return cadena_encriptada;
        }catch (Exception e){
            return null;
        }
    }



    // public String DesencriptarContraseña(){}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);

        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        EditText nombreIngresado = (EditText) findViewById(R.id.registroNombre);
        EditText correoIngresado = (EditText) findViewById(R.id.registroCorreo);
        EditText claveIngresada = (EditText) findViewById(R.id.registroClave);

        Button botonRegistrarse = (Button) findViewById(R.id.botonRegistro);
        Button botonVolverDeRegistro = (Button) findViewById(R.id.botonVolverRegistro);

        botonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreCapturadoRegistro = nombreIngresado.getText().toString().trim();
                String correoCapturadoRegistro = correoIngresado.getText().toString().trim();
                String claveCapturadaRegistro = claveIngresada.getText().toString().trim();

                if (nombreCapturadoRegistro.isEmpty() && correoCapturadoRegistro.isEmpty() && claveCapturadaRegistro.isEmpty()){
                    Toast.makeText(RegistroUsuario.this, "Complete los datos", Toast.LENGTH_SHORT).show();
                }else {
                    registerUser(nombreCapturadoRegistro, correoCapturadoRegistro, claveCapturadaRegistro);
                }
            }
        });

        botonVolverDeRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void registerUser(String nombreCapturadoRegistro, String correoCapturadoRegistro, String claveCapturadaRegistro){
        try {
            mAuth.createUserWithEmailAndPassword(correoCapturadoRegistro, claveCapturadaRegistro).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    String id = mAuth.getCurrentUser().getUid();
                    String userType = "Cliente";
                    String claveEncriptada = EncriptarContraseña(claveCapturadaRegistro);
                    Map<String, Object> map = new  HashMap<>();
                    map.put("id", id);
                    map.put("name", nombreCapturadoRegistro);
                    map.put("email", correoCapturadoRegistro);
                    map.put("password", claveEncriptada);
                    map.put("userType", userType);
                    mFireStore.collection("users").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            finish();
                            startActivity(new Intent(RegistroUsuario.this, MainActivity.class));
                            Toast.makeText(RegistroUsuario.this, "Usuario Registrado exitosamente", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistroUsuario.this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegistroUsuario.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(RegistroUsuario.this, "Error al encriptar la contraseña", Toast.LENGTH_SHORT).show();
        }
    }
}