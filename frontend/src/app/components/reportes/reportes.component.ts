import { CurrencyPipe, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit } from '@angular/core';
import { Indicadores } from '../../models/modelos';
import { leerError } from '../../models/validaciones';
import { ReporteService } from '../../services/reporte.service';

/** barra del gráfico */
interface Barra {
  nombre: string;
  valor: number;
  ancho: number;
}

interface Grafico {
  titulo: string;
  barras: Barra[];
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CurrencyPipe, NgTemplateOutlet, FormsModule],
  templateUrl: './reportes.component.html',
})
export class ReportesComponent implements OnInit {
  private servicio = inject(ReporteService);

  datos: Indicadores | null = null;
  graficosReservas: Grafico[] = [];
  graficosGenerales: Grafico[] = [];
  totalUsuarios = 0;
  publicacionesActivas = 0;
  totalReservas = 0;
  cargando = true;
  exportando = false;
  tipoExportar = 'RESERVAS';
  formatoExportar = 'PDF'; // RF-25: PDF o Excel
  tipos = [
    { valor: 'USUARIOS', nombre: 'Usuarios' },
    { valor: 'PUBLICACIONES', nombre: 'Publicaciones' },
    { valor: 'RESERVAS', nombre: 'Reservas' },
    { valor: 'INGRESOS', nombre: 'Ingresos estimados' },
    { valor: 'DEMANDA', nombre: 'Demanda por ciudad' },
  ];
  error = '';

  // HU-32 CA2: rango de fechas
  desde = '';
  hasta = '';

  ngOnInit() {
    this.cargar();
  }

  limpiarFiltro() {
    this.desde = '';
    this.hasta = '';
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    this.error = '';
    this.servicio.consultarIndicadores(this.desde, this.hasta).subscribe({
      next: (d) => {
        this.datos = d;
        this.totalUsuarios = this.sumar(d.usuariosPorEstado);
        this.publicacionesActivas = d.publicacionesPorEstado['ACTIVA'] ?? 0;
        this.totalReservas = this.sumar(d.reservasPorEstado);
        this.graficosReservas = [
          this.crearGrafico('Reservas por estado', d.reservasPorEstado),
          this.crearGrafico('Reservas por mes (mes de llegada)', d.reservasPorMes),
          this.crearGrafico('Ciudades con mayor demanda', d.reservasPorCiudad),
          this.crearGrafico('Tipo de alojamiento reservado', d.reservasPorTipo),
          this.crearGrafico('Fechas más frecuentes (noches, top 5)', d.fechasMasFrecuentes),
          this.crearGrafico('Noches: fin de semana y entre semana', d.nochesFinDeSemana),
        ];
        this.graficosGenerales = [
          this.crearGrafico('Usuarios por rol', d.usuariosPorRol),
          this.crearGrafico('Usuarios por estado', d.usuariosPorEstado),
          this.crearGrafico('Publicaciones por estado', d.publicacionesPorEstado),
          this.crearGrafico('Publicaciones por ciudad', d.publicacionesPorCiudad),
          this.crearGrafico('Publicaciones por tipo de alojamiento', d.publicacionesPorTipo),
        ];
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  // mapa a barras
  private crearGrafico(titulo: string, mapa: Record<string, number>): Grafico {
    const valores = Object.values(mapa);
    const mayor = valores.length ? Math.max(...valores) : 0;
    const barras = Object.entries(mapa).map(([nombre, valor]) => ({
      nombre,
      valor,
      ancho: mayor > 0 ? Math.round((valor * 100) / mayor) : 0,
    }));
    return { titulo, barras };
  }

  private sumar(mapa: Record<string, number>) {
    return Object.values(mapa).reduce((total, valor) => total + valor, 0);
  }

  exportar() {
    this.exportando = true;
    this.error = '';
    this.servicio.exportar(this.tipoExportar, this.formatoExportar, this.desde, this.hasta).subscribe({
      next: (archivo) => {
        const enlace = document.createElement('a');
        enlace.href = URL.createObjectURL(archivo);
        enlace.download = 'reporte-' + this.tipoExportar.toLowerCase() + '.' + this.formatoExportar.toLowerCase();
        enlace.click();
        URL.revokeObjectURL(enlace.href);
        this.exportando = false;
      },
      error: (e) => {
        this.exportando = false;
        // lee el error
        if (e.error instanceof Blob) {
          e.error.text().then((texto: string) => {
            try {
              this.error = JSON.parse(texto).mensaje;
            } catch {
              this.error = 'No se pudo exportar el reporte';
            }
          });
        } else {
          this.error = 'No se pudo exportar el reporte';
        }
      },
    });
  }
}
