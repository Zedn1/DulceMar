package cl.rojas.dulcemar;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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


    FirebaseFirestore bd;

    TextView descripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descripcion_productos);
        // Initialize Firestore
        bd = FirebaseFirestore.getInstance();
        // Find the TextView by its ID
        descripcion = findViewById(R.id.read); // Ensure this ID matches your XML layout
        // Get a reference to the Firestore document
        DocumentReference bdref = bd.collection("producto").document("K4IR8XEzg9s3xCJsjy44");
        // Retrieve the document
        Button Regret = (Button)findViewById(R.id.RMenu);
        bdref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Extract the text you want from the document
                        String text = document.getString("descripcion"); // Replace with your field name
                        descripcion.setText(text); // Set the text to the TextView
                    } else {
                        Toast.makeText(DescripcionProductos.this, "", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DescripcionProductos.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
