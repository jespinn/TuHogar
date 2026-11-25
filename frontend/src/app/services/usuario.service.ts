import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API, PerfilDatos, Usuario } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);

  verPerfil() {
    return this.http.get<Usuario>(`${API}/usuarios/perfil`);
  }

  actualizarPerfil(datos: PerfilDatos) {
    return this.http.put<Usuario>(`${API}/usuarios/perfil`, datos);
  }

  // huésped o anfitrión
  desactivarMiCuenta() {
    return this.http.put<Usuario>(`${API}/usuarios/perfil/desactivacion`, {});
  }

  // solo administrador
  listarUsuarios() {
    return this.http.get<Usuario[]>(`${API}/usuarios`);
  }

  desactivarCuenta(id: number) {
    return this.http.put<Usuario>(`${API}/usuarios/${id}/desactivar`, {});
  }

  activarCuenta(id: number) {
    return this.http.put<Usuario>(`${API}/usuarios/${id}/activar`, {});
  }
}
