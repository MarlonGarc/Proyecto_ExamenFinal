package gt.edu.umg.dangermap.BaseDatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NOMBRE = "db_incidentes.db";

    // Tabla de reportes de incidentes
    public static final String TABLE_REPORTES = "reportes";

    // Columnas de la tabla de reportes
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIPO_INCIDENTE = "tipo_incidente";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_FECHA_HORA = "fecha_hora"; // Opcional para almacenar la fecha y hora
    public static final String COLUMN_IMAGEN_RUTA = "imagen_ruta"; // Columna para la ruta de la imagen

    public DbHelper(@Nullable Context context) {
        super(context, DB_NOMBRE, null, DB_VERSION);
    }

    @Override
    // Se crea la tabla la primera vez que se ejecuta la aplicación
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_REPORTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIPO_INCIDENTE + " TEXT NOT NULL, " +
                COLUMN_LATITUD + " REAL NOT NULL, " +
                COLUMN_LONGITUD + " REAL NOT NULL, " +
                COLUMN_FECHA_HORA + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + // Almacena la fecha y hora por defecto
                COLUMN_IMAGEN_RUTA + " TEXT" + // Columna para la ruta de la imagen
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTES);
        onCreate(db);
    }
}
