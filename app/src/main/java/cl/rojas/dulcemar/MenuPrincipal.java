package cl.rojas.dulcemar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MenuPrincipal extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Button PedidoCasa = (Button) findViewById(R.id.Domicilio);
        Button RetiroLocal = (Button) findViewById(R.id.RetiroL);
        ImageButton botonCuenta = findViewById(R.id.BotonCuenta);

        mAuth = FirebaseAuth.getInstance();

        botonCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarMenuCuenta(view);
            }
        });

        PedidoCasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuPrincipal.this,busqueda.class));
            }
        });
        RetiroLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuPrincipal.this,busqueda.class));
            }
        });
    }

    private void mostrarMenuCuenta(View view){
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_opciones_cuenta, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.CerrarSesion) {
                    cerrarSesion();
                    return true;
                }else{
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void cerrarSesion(){
        mAuth.signOut();
        finish();
        startActivity(new Intent(MenuPrincipal.this, MainActivity.class));
    }
}