package gt.edu.umg.dangermap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
//importaciones para acces fine location
import android.Manifest;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import gt.edu.umg.dangermap.BaseDatos.DbHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;

    // Receptor para recibir actualizaciones de reporte
    private BroadcastReceiver reporteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitud = intent.getDoubleExtra("latitud", 0);
            double longitud = intent.getDoubleExtra("longitud", 0);
            actualizarZona(latitud, longitud);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configuración inicial de los botones
        configurarBotones();

        // Verificar permisos de ubicación
        checkLocationPermission();
    }

    private void configurarBotones() {
        // Botón para hacer reporte
        Button btnReporte = findViewById(R.id.btnReporteIncidente);
        btnReporte.setOnClickListener(v -> {
            Log.d("MainActivity", "Botón reportar clickeado");
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent); // Llevar a la actividad de reporte
        });

        // Botón para ver historial de incidentes
        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Ejemplo: añadir un marcador en una ubicación fija
        LatLng ubicacionEjemplo = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(ubicacionEjemplo).title("Zona peligrosa"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionEjemplo));

        // Mostrar la ubicación actual del usuario
        mostrarUbicacionActual();
    }

    private void mostrarUbicacionActual() {
        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No se tienen permisos, solicitar permisos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Obtener la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitud = location.getLatitude();
                        double longitud = location.getLongitude();
                        actualizarZona(latitud, longitud); // Actualizar zona según la ubicación actual
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para actualizar la zona en el mapa en tiempo real
    public void actualizarZona(double latitud, double longitud) {
        // Contar incidentes en la zona
        int incidentCount = contarIncidentesPorZona(latitud, longitud);

        // Determinar el color del círculo y mensaje según el número de incidentes
        int fillColor;
        String mensaje;
        if (incidentCount >= 4) {
            fillColor = Color.argb(70, 255, 0, 0); // Rojo
            mensaje = "Zona peligrosa";
        } else if (incidentCount >= 2) {
            fillColor = Color.argb(70, 255, 255, 0); // Amarillo
            mensaje = "Zona de precaución";
        } else {
            fillColor = Color.argb(70, 0, 255, 0); // Verde
            mensaje = "Zona segura";
        }

        // Limpiar los círculos y marcadores anteriores (si es necesario)
        googleMap.clear(); // Limpia el mapa antes de dibujar nuevos elementos

        // Agregar el círculo al mapa
        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(latitud, longitud))
                .radius(100) // Radio en metros
                .strokeWidth(2)
                .strokeColor(Color.BLACK)
                .fillColor(fillColor));

        // Agregar marcador con mensaje
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitud, longitud))
                .title(mensaje));
        marker.showInfoWindow(); // Mostrar la ventana de información de inmediato

        // Mover la cámara a la ubicación actual
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud, longitud), 15));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Configuración de botones al reanudar la actividad
        configurarBotones();
        LocalBroadcastManager.getInstance(this).registerReceiver(reporteReceiver, new IntentFilter("ACTUALIZAR_ZONA"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reporteReceiver);
    }

    private int contarIncidentesPorZona(double latitud, double longitud) {
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        double range = 0.001; // Rango para definir proximidad (aproximadamente 100m)

        String query = "SELECT COUNT(*) FROM reportes WHERE latitud BETWEEN ? AND ? AND longitud BETWEEN ? AND ?";
        String[] args = {
                String.valueOf(latitud - range),
                String.valueOf(latitud + range),
                String.valueOf(longitud - range),
                String.valueOf(longitud + range)
        };

        Cursor cursor = db.rawQuery(query, args);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Si ya tiene permiso, mostrar ubicación actual
            mostrarUbicacionActual();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarUbicacionActual(); // Si se concede el permiso, mostrar la ubicación
            } else {
                // Mensaje mejorado si se niega el permiso
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Se necesita permiso de ubicación para mostrar tu posición.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado. Activa el permiso en la configuración.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}