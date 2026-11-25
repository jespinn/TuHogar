import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Reserva } from '../../models/modelos';
import { leerError, obtenerMensajeError, mostrarErrorEnCampo } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';

/** pago simulado */
@Component({
  selector: 'app-pago',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './pago.component.html',
})
export class PagoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(ReservaService);

  reserva: Reserva | null = null;
  cargando = true;
  pagando = false;
  error = '';

  // reglas del formulario
  form = this.fb.nonNullable.group({
    numeroTarjeta: ['', [Validators.required, Validators.pattern(/^\d{16}$/)]],
    titular: ['', Validators.required],
    codigoSeguridad: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
  });

  errorDe = (campo: string) => obtenerMensajeError(this.form, campo);

  ngOnInit() {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.consultarMisReservas().subscribe({
      next: (lista) => {
        this.reserva = lista.find((r) => r.id === id) ?? null;
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  pagar() {
    this.pagando = true;
    this.error = '';
    this.servicio.pagar(this.reserva!.id, this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/mis-reservas'], { queryParams: { aviso: 'pagada' } }),
      error: (e) => {
        this.error = mostrarErrorEnCampo(this.form, leerError(e)); // mensaje del banco
        this.pagando = false;
      },
    });
  }
}
