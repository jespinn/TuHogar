import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Reserva } from '../../models/modelos';
import { leerError, obtenerTextoEstado } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';

/** reservas del anfitrión */
@Component({
  selector: 'app-reservas-anfitrion',
  standalone: true,
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './reservas-anfitrion.component.html',
})
export class ReservasAnfitrionComponent implements OnInit {
  private servicio = inject(ReservaService);

  reservas: Reserva[] = [];
  cargando = true;
  rechazando: number | null = null; // rechazo abierto
  procesando = false;
  error = '';
  motivo = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(200)] });
  obtenerTextoEstado = obtenerTextoEstado;

  ngOnInit() {
    this.servicio.consultarReservasDeMisPublicaciones().subscribe({
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

  aprobar(id: number) {
    this.responder(id, this.servicio.aprobar(id));
  }

  abrirRechazo(id: number) {
    this.rechazando = id;
    this.motivo.reset('');
  }

  confirmarRechazo(id: number) {
    this.responder(id, this.servicio.rechazar(id, this.motivo.value));
  }

  // descarga el comprobante
  descargarComprobante(id: number) {
    this.error = '';
    this.servicio.obtenerComprobante(id).subscribe({
      next: (archivo) => this.servicio.descargarComprobante(id, archivo),
      error: () => (this.error = 'No se pudo descargar el comprobante'),
    });
  }

  // actualiza la lista
  private responder(id: number, peticion: ReturnType<ReservaService['aprobar']>) {
    this.procesando = true;
    this.error = '';
    peticion.subscribe({
      next: (actualizada) => {
        this.reservas = this.reservas.map((r) => (r.id === id ? actualizada : r));
        this.rechazando = null;
        this.procesando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.procesando = false;
      },
    });
  }
}
