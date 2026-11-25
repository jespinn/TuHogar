import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API, RegistroDatos, Sesion, Usuario } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // usuario en sesión
  usuario = signal<Usuario | null>(this.leerUsuarioGuardado());

  get token(): string | null {
    return localStorage.getItem('token');
  }

  /** token vigente */
  tieneSesionVigente(): boolean {
    const token = this.token;
    if (!token) return false;
    try {
      const datos = JSON.parse(atob(token.split('.')[1]));
      return datos.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  tieneRol(rol: string): boolean {
    return this.usuario()?.roles.includes(rol) ?? false;
  }

  iniciarSesion(correo: string, contrasena: string) {
    return this.http.post<Sesion>(`${API}/usuarios/login`, { correo, contrasena }).pipe(tap((s) => this.guardar(s)));
  }

  registrar(datos: RegistroDatos) {
    return this.http.post<Usuario>(`${API}/usuarios/registro`, datos);
  }

  pedirCodigo(correo: string) {
    return this.http.post<{ mensaje: string; codigo?: string; expiraEn?: string }>(`${API}/usuarios/recuperar`, { correo });
  }

  // reactiva e inicia sesión
  reactivar(correo: string, contrasena: string) {
    return this.http.post<Sesion>(`${API}/usuarios/reactivar`, { correo, contrasena }).pipe(tap((s) => this.guardar(s)));
  }

  restablecer(datos: { correo: string; codigo: string; contrasena: string; confirmacion: string }) {
    return this.http.post<{ mensaje: string }>(`${API}/usuarios/restablecer`, datos);
  }

  // cierra la sesión
  salir(vencida = false, aviso = '') {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    this.usuario.set(null);
    this.router.navigate(['/login'], { queryParams: vencida ? { aviso: 'vencida' } : aviso ? { aviso } : {} });
  }

  actualizarUsuario(usuario: Usuario) {
    localStorage.setItem('usuario', JSON.stringify(usuario));
    this.usuario.set(usuario);
  }

  private guardar(sesion: Sesion) {
    localStorage.setItem('token', sesion.token);
    this.actualizarUsuario(sesion.usuario);
  }

  private leerUsuarioGuardado(): Usuario | null {
    const texto = localStorage.getItem('usuario');
    return texto ? JSON.parse(texto) : null;
  }
}
