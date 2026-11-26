import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlojamientoResumen } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ReservaService } from '../../services/reserva.service';

/** portada: buscador y alojamientos disponibles */
@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, FormsModule],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css',
})
export class InicioComponent implements OnInit {
  auth = inject(AuthService);
  private reservas = inject(ReservaService);

  todos: AlojamientoResumen[] = [];
  ciudades: string[] = [];
  tipos: string[] = [];
  cargando = true;

  // filtros del buscador
  ciudad = '';
  tipo = '';
  huespedes = 1;

  // la lista es pública: la ve cualquier persona
  ngOnInit() {
    this.reservas.buscarAlojamientos().subscribe({
      next: (lista) => {
        this.todos = lista;
        this.ciudades = [...new Set(lista.map((a) => a.ciudad))].sort();
        this.tipos = [...new Set(lista.map((a) => a.tipoAlojamiento))].sort();
        this.cargando = false;
      },
      error: () => {
        this.todos = [];
        this.cargando = false;
      },
    });
  }

  // alojamientos que cumplen los filtros
  get resultados(): AlojamientoResumen[] {
    return this.todos.filter(
      (a) =>
        (!this.ciudad || a.ciudad === this.ciudad) &&
        (!this.tipo || a.tipoAlojamiento === this.tipo) &&
        a.capacidad >= (this.huespedes || 1),
    );
  }

  elegirCiudad(ciudad: string) {
    this.ciudad = this.ciudad === ciudad ? '' : ciudad;
  }

  // baja hasta los resultados
  verResultados() {
    document.getElementById('resultados')?.scrollIntoView({ behavior: 'smooth' });
  }

  limpiar() {
    this.ciudad = '';
    this.tipo = '';
    this.huespedes = 1;
  }

  mostrarSinFoto(alojamiento: AlojamientoResumen) {
    alojamiento.imagenPrincipal = null; // foto no carga
  }
}
