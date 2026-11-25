import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AlojamientoResumen } from '../../models/modelos';
import { leerError } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';

/** lista de alojamientos */
@Component({
  selector: 'app-alojamientos',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './alojamientos.component.html',
})
export class AlojamientosComponent implements OnInit {
  private servicio = inject(ReservaService);

  lista: AlojamientoResumen[] = [];
  cargando = true;
  error = '';

  ngOnInit() {
    this.servicio.buscarAlojamientos().subscribe({
      next: (datos) => {
        this.lista = datos;
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }
}
