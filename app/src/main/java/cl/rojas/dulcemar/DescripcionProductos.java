package cl.rojas.dulcemar;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import static androidx.constraintlayout.widget.Constraints.TAG;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class DescripcionProductos extends AppCompatActivity {

    private TextView nombreTextView;
    private TextView precioTextView;
    private TextView descripcionTextView;
    private ImageView imagenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descripcion_productos);

        //nombreTextView = findViewById(R.id.);
        //precioTextView = findViewById(R.id.);
        //descripcionTextView = findViewById(R.id);
        //imagenImageView = findViewById(R.id);

        Intent intent = getIntent();
        String nombre = intent.getStringExtra("NombreDelProducto");
        double precio = intent.getDoubleExtra("PrecioDelProducto", 0.0);
        String descripcion = intent.getStringExtra("DescripcionDelProducto");
        String imagen = intent.getStringExtra("ImagenDelProducto");

        nombreTextView.setText(nombre);
        precioTextView.setText(String.valueOf(precio));
        descripcionTextView.setText(descripcion);

        int imagenId = getResources().getIdentifier(imagen, "drawable", getPackageName());
        imagenImageView.setImageResource(imagenId);
    }
}
