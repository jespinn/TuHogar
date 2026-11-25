import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AlojamientoResumen } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ReservaService } from '../../services/reserva.service';

/** portada */
@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css',
})
export class InicioComponent implements OnInit {
  auth = inject(AuthService);
  private reservas = inject(ReservaService);

  destacados: AlojamientoResumen[] = [];

  // carga destacados
  ngOnInit() {
    if (!this.auth.tieneRol('HUESPED')) return;
    this.reservas.buscarAlojamientos().subscribe({
      next: (lista) => (this.destacados = lista.slice(0, 3)),
      error: () => (this.destacados = []),
    });
  }

  mostrarSinFoto(alojamiento: AlojamientoResumen) {
    alojamiento.imagenPrincipal = null; // foto no carga
  }
}
