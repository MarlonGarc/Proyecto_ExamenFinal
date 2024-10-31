package gt.edu.umg.dangermap;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gt.edu.umg.dangermap.BaseDatos.DbHelper;


public class ReportActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 103;

    private EditText txtTipoIncidente;
    private Button btnGuardarReporte, btnTomarFoto, btnRegresar;
    private Uri imageUri;
    private ImageView imgIncidente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        txtTipoIncidente = findViewById(R.id.txtTipoIncidente);
        btnGuardarReporte = findViewById(R.id.btnGuardarReporte);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnRegresar = findViewById(R.id.btnRegresar);
        imgIncidente = findViewById(R.id.imgIncidente);

        // Inicializa ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Solicitar permisos de almacenamiento al iniciar la aplicación
        checkStoragePermission();

        // Restablecer campos
        resetFields();

        btnTomarFoto.setOnClickListener(v -> checkCameraPermission());

        btnGuardarReporte.setOnClickListener(v -> {
            if (txtTipoIncidente.getText().toString().trim().isEmpty()) {
                Toast.makeText(ReportActivity.this, "Debes completar la descripción del incidente", Toast.LENGTH_SHORT).show();
            } else {
                checkLocationPermission(); // Aquí se verifican los permisos de ubicación
            }
        });

        btnRegresar.setOnClickListener(v -> finish());

        checkLocationPermission(); // Mover aquí para solicitar ubicación
    }

    // Método para restablecer campos
    private void resetFields() {
        txtTipoIncidente.setText("");
        imgIncidente.setImageURI(null);
        imageUri = null;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "gt.edu.umg.dangermap.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            imgIncidente.setImageURI(imageUri);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            obtenerUbicacion(); // Obtener la ubicación si ya se tiene permiso
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permiso para acceder a la cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion(); // Obtener la ubicación si se concede el permiso
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. Activa el permiso en la configuración.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitud = location.getLatitude();
                            double longitud = location.getLongitude();
                            guardarReporte(latitud, longitud); // Guardar el reporte con la ubicación
                        } else {
                            Toast.makeText(ReportActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void guardarReporte(double latitud, double longitud) {
        String tipoIncidente = txtTipoIncidente.getText().toString().trim();

        if (tipoIncidente.isEmpty()) {
            Toast.makeText(this, "Por favor, complete la descripción del incidente.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = new DbHelper(this).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tipo_incidente", tipoIncidente);
        values.put("latitud", latitud);
        values.put("longitud", longitud);
        values.put("imagen_ruta", imageUri != null ? imageUri.toString() : "");

        long newRowId = db.insert("reportes", null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Reporte guardado", Toast.LENGTH_SHORT).show();
            resetFields();

            // Enviar el broadcast al registrar un nuevo reporte
            Intent intent = new Intent("com.tuapp.REPORTE_AGREGADO");
            intent.putExtra("latitud", latitud);
            intent.putExtra("longitud", longitud);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}