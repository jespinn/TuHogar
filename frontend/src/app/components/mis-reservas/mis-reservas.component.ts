import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Reserva } from '../../models/modelos';
import { leerError, obtenerTextoEstado } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';

const AVISOS: Record<string, string> = {
  reservada: 'Reserva creada. Está pendiente de aprobación del anfitrión; cuando la apruebe podrá pagarla.',
  pagada: 'Pago aprobado. Su reserva quedó confirmada.',
};

@Component({
  selector: 'app-mis-reservas',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, ReactiveFormsModule],
  templateUrl: './mis-reservas.component.html',
})
export class MisReservasComponent implements OnInit {
  private servicio = inject(ReservaService);

  aviso = AVISOS[inject(ActivatedRoute).snapshot.queryParams['aviso']] ?? '';
  reservas: Reserva[] = [];
  cargando = true;
  cancelando: number | null = null; // cancelación abierta
  devolucion: number | null = null; // valor a devolver
  procesando = false;
  error = '';
  motivo = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] });
  obtenerTextoEstado = obtenerTextoEstado;

  ngOnInit() {
    this.servicio.consultarMisReservas().subscribe({
      next: (lista) => {
        this.reservas = lista;
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  abrirCancelacion(id: number) {
    this.cancelando = id;
    this.devolucion = null;
    this.motivo.reset('');
    // muestra la devolución
    this.servicio.consultarDevolucion(id).subscribe({
      next: (respuesta) => (this.devolucion = respuesta.valorDevolucion),
      error: (e) => (this.error = leerError(e).mensaje),
    });
  }

  // descarga el comprobante
  descargarComprobante(id: number) {
    this.error = '';
    this.servicio.obtenerComprobante(id).subscribe({
      next: (archivo) => this.servicio.descargarComprobante(id, archivo),
      error: () => (this.error = 'No se pudo descargar el comprobante'),
    });
  }

  confirmarCancelacion(id: number) {
    this.procesando = true;
    this.error = '';
    this.servicio.cancelar(id, this.motivo.value).subscribe({
      next: (actualizada) => {
        this.reservas = this.reservas.map((r) => (r.id === id ? actualizada : r));
        this.cancelando = null;
        this.procesando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.procesando = false;
      },
    });
  }
}
