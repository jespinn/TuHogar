package co.edu.unbosque.model.administracion;

import co.edu.unbosque.model.usuarios.Usuario;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** registro de exportación */
@Entity
@Table(name = "reporte")
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_administrador")
    private Usuario administrador;

    private String tipo;
    private String formato;
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    public Reporte() {
    }

    public Reporte(Usuario administrador, String tipo, String formato) {
        this.administrador = administrador;
        this.tipo = tipo;
        this.formato = formato;
    }

    /** crea el archivo */
    public byte[] exportar(List<String[]> filas) {
        if (formato.equals("XLSX")) {
            return crearExcel(filas);
        }
        return crearPdf(filas);
    }

    // excel por sección
    private byte[] crearExcel(List<String[]> filas) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = null;
            String seccionActual = "";
            int numeroFila = 0;
            for (String[] fila : filas) {
                if (!fila[0].equals(seccionActual)) {
                    seccionActual = fila[0];
                    // nombre de hoja
                    hoja = libro.createSheet(WorkbookUtil.createSafeSheetName(seccionActual));
                    Row titulos = hoja.createRow(0);
                    titulos.createCell(0).setCellValue("Categoría");
                    titulos.createCell(1).setCellValue("Total");
                    numeroFila = 1;
                }
                Row nueva = hoja.createRow(numeroFila);
                nueva.createCell(0).setCellValue(fila[1]);
                nueva.createCell(1).setCellValue(Double.parseDouble(fila[2]));
                numeroFila++;
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el Excel", e);
        }
    }

    // pdf por sección
    private byte[] crearPdf(List<String[]> filas) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document();
        PdfWriter.getInstance(documento, salida);
        documento.open();
        documento.add(new Paragraph("Reporte de " + tipo.toLowerCase() + " - TuHogar"));

        PdfPTable tabla = null;
        String seccionActual = "";
        for (String[] fila : filas) {
            if (!fila[0].equals(seccionActual)) {
                if (tabla != null) {
                    documento.add(tabla);
                }
                seccionActual = fila[0];
                documento.add(new Paragraph(" "));
                documento.add(new Paragraph(seccionActual));
                tabla = new PdfPTable(2);
                tabla.addCell("Categoría");
                tabla.addCell("Total");
            }
            tabla.addCell(fila[1]);
            tabla.addCell(fila[2]);
        }
        documento.add(tabla);
        documento.close();
        return salida.toByteArray();
    }

    public Long getId() { return id; }
    public Usuario getAdministrador() { return administrador; }
    public String getTipo() { return tipo; }
    public String getFormato() { return formato; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
}
