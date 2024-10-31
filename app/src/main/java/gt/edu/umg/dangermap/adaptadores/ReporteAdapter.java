package gt.edu.umg.dangermap.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import gt.edu.umg.dangermap.BaseDatos.DbHelper;
import gt.edu.umg.dangermap.BaseDatos.entidades.Reporte;
import gt.edu.umg.dangermap.R;
import gt.edu.umg.dangermap.ReportActivity;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private Context context;
    private ArrayList<Reporte> reportes;
    private DbHelper dbHelper;

    public ReporteAdapter(Context context, ArrayList<Reporte> reportes) {
        this.context = context;
        this.reportes = reportes;
        this.dbHelper = new DbHelper(context); // Inicializar DbHelper
    }

    public static class ReporteViewHolder extends RecyclerView.ViewHolder {
        public TextView txtIncidente;
        public ImageView imgIncidente;
        public Button btnEliminar, btnActualizar;

        public ReporteViewHolder(View itemView) {
            super(itemView);
            txtIncidente = itemView.findViewById(R.id.txtIncidente);
            imgIncidente = itemView.findViewById(R.id.imgIncidente);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnActualizar = itemView.findViewById(R.id.btnActualizar);
        }
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        Reporte reporte = reportes.get(position);
        holder.txtIncidente.setText("Incidente: " + reporte.getTipoIncidente() +
                "\nLatitud: " + reporte.getLatitud() +
                "\nLongitud: " + reporte.getLongitud());

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(reporte.getImagenRuta())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imgIncidente);

        // Configurar el botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            eliminarReporte(reporte);
            reportes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, reportes.size());
        });

        // Configurar el botón Actualizar
        holder.btnActualizar.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("reporteId", reporte.getId());
            context.startActivity(intent);
        });
    }

    private void eliminarReporte(Reporte reporte) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("reportes", "id = ?", new String[]{String.valueOf(reporte.getId())});
        db.close();
        Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return reportes.size();
    }
}