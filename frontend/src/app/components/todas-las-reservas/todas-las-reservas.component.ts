import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FiltrosReservas, ReservaAdmin } from '../../models/modelos';
import { leerError, obtenerTextoEstado } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';

// estados del filtro
const ESTADOS = [
  'PENDIENTE_APROBACION', 'PENDIENTE_PAGO', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA', 'COMPLETADA', 'EN_REVISION_MANUAL',
];

@Component({
  selector: 'app-todas-las-reservas',
  standalone: true,
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './todas-las-reservas.component.html',
})
export class TodasLasReservasComponent implements OnInit {
  private servicio = inject(ReservaService);
  private fb = inject(FormBuilder);

  filtros = this.fb.nonNullable.group({ estado: '', desde: '', hasta: '', publicacion: '' });
  reservas: ReservaAdmin[] = [];
  alojamientos: { id: number; titulo: string }[] = []; // opciones de alojamiento
  estados = ESTADOS;
  obtenerTextoEstado = obtenerTextoEstado;
  cargando = true;
  error = '';

  ngOnInit() {
    this.buscar(true);
  }

  // arma el filtro
  buscar(primeraVez = false) {
    const valores: FiltrosReservas = this.filtros.getRawValue();
    if (valores.desde && valores.hasta && valores.desde > valores.hasta) {
      this.error = 'La fecha "desde" no puede ser posterior a la fecha "hasta"';
      return;
    }
    this.cargando = true;
    this.error = '';
    this.servicio.consultarTodasLasReservas(valores).subscribe({
      next: (lista) => {
        this.reservas = lista;
        if (primeraVez) this.armarAlojamientos(lista);
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  limpiar() {
    this.filtros.reset();
    this.buscar();
  }

  private armarAlojamientos(lista: ReservaAdmin[]) {
    for (const r of lista) {
      let yaEsta = false;
      for (const a of this.alojamientos) {
        if (a.id === r.alojamientoId) yaEsta = true;
      }
      if (!yaEsta) this.alojamientos.push({ id: r.alojamientoId, titulo: r.alojamiento });
    }
  }
}
