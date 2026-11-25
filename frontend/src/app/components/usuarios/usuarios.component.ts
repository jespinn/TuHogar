import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Usuario } from '../../models/modelos';
import { leerError } from '../../models/validaciones';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';

/** cuentas para el administrador */
@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './usuarios.component.html',
})
export class UsuariosComponent implements OnInit {
  private servicio = inject(UsuarioService);
  auth = inject(AuthService);

  usuarios: Usuario[] = [];
  cargando = true;
  procesando: number | null = null; // cuenta en proceso
  error = '';

  ngOnInit() {
    this.servicio.listarUsuarios().subscribe({
      next: (lista) => {
        this.usuarios = lista;
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  cambiarEstado(u: Usuario) {
    const desactivar = u.estado === 'ACTIVA';
    if (desactivar && !confirm(`¿Restringir la cuenta de ${u.nombre}? No podrá iniciar sesión.`)) return;
    this.procesando = u.id;
    this.error = '';
    const peticion = desactivar ? this.servicio.desactivarCuenta(u.id) : this.servicio.activarCuenta(u.id);
    peticion.subscribe({
      next: (actualizado) => {
        this.usuarios = this.usuarios.map((x) => (x.id === u.id ? actualizado : x));
        this.procesando = null;
      },
      error: (e) => {
        this.error = leerError(e).mensaje; // propia cuenta o último administrador
        this.procesando = null;
      },
    });
  }

  esPropia(u: Usuario) {
    return u.correo === this.auth.usuario()?.correo;
  }
}
