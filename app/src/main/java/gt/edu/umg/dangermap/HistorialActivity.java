package gt.edu.umg.dangermap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gt.edu.umg.dangermap.BaseDatos.DbHelper;
import gt.edu.umg.dangermap.BaseDatos.entidades.Reporte;
import gt.edu.umg.dangermap.adaptadores.ReporteAdapter;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistorial;
    private ArrayList<Reporte> listaReportes;
    private ReporteAdapter reporteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Inicializar el RecyclerView
        recyclerViewHistorial = findViewById(R.id.recyclerViewHistorial);
        listaReportes = new ArrayList<>();

        // Configurar el RecyclerView
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        reporteAdapter = new ReporteAdapter(this, listaReportes);
        recyclerViewHistorial.setAdapter(reporteAdapter);

        // Cargar los reportes desde la base de datos
        cargarReportes();

        // Botón para regresar a la actividad anterior
        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> finish()); // Cerrar la actividad actual
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReportes();
        reporteAdapter.notifyDataSetChanged(); // Notificar que los datos han cambiado
    }

    private void eliminarReporte(int posicion) {
        SQLiteDatabase db = new DbHelper(this).getWritableDatabase();

        // Obtener el reporte que se va a eliminar
        Reporte reporte = listaReportes.get(posicion);
        String whereClause = "id = ?";
        String[] whereArgs = new String[] { String.valueOf(reporte.getId()) };

        // Eliminar el reporte de la base de datos
        int deletedRows = db.delete("reportes", whereClause, whereArgs);
        if (deletedRows > 0) {
            Log.d("HistorialActivity", "Reporte eliminado correctamente.");
            listaReportes.remove(posicion); // Eliminar el reporte de la lista
            reporteAdapter.notifyItemRemoved(posicion); // Notificar al adaptador que se ha eliminado un ítem
        } else {
            Log.d("HistorialActivity", "Error al eliminar el reporte.");
        }

        // Cerrar la base de datos
        db.close();
    }

    // Método para cargar los reportes desde la base de datos
    private void cargarReportes() {
        listaReportes.clear(); // Limpiar antes de cargar nuevos reportes
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, tipo_incidente, latitud, longitud, imagen_ruta FROM reportes ORDER BY fecha_hora DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0); // Asegúrate de obtener el ID
                String tipoIncidente = cursor.getString(1);
                double latitud = cursor.getDouble(2);
                double longitud = cursor.getDouble(3);
                String imagenRuta = cursor.getString(4);

                listaReportes.add(new Reporte(id, tipoIncidente, latitud, longitud, imagenRuta)); // Asegúrate de que tu constructor está correcto
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }
}