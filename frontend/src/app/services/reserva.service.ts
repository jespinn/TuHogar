import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  API, AlojamientoDetalle, AlojamientoResumen, Cotizacion, DatosPago, FiltrosReservas, Reserva, ReservaAdmin,
  SolicitudReserva,
} from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private http = inject(HttpClient);
  private url = `${API}/reservas`;

  buscarAlojamientos() {
    return this.http.get<AlojamientoResumen[]>(`${this.url}/alojamientos`);
  }

  consultarAlojamiento(id: number) {
    return this.http.get<AlojamientoDetalle>(`${this.url}/alojamientos/${id}`);
  }

  cotizar(solicitud: SolicitudReserva) {
    return this.http.post<Cotizacion>(`${this.url}/cotizar`, solicitud);
  }

  reservar(solicitud: SolicitudReserva) {
    return this.http.post<Reserva>(this.url, solicitud);
  }

  consultarMisReservas() {
    return this.http.get<Reserva[]>(`${this.url}/mias`);
  }

  // solo filtros llenos
  consultarTodasLasReservas(filtros: FiltrosReservas) {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.desde) params = params.set('desde', filtros.desde);
    if (filtros.hasta) params = params.set('hasta', filtros.hasta);
    if (filtros.publicacion) params = params.set('publicacion', filtros.publicacion);
    return this.http.get<ReservaAdmin[]>(`${this.url}/todas`, { params });
  }

  consultarReservasDeMisPublicaciones() {
    return this.http.get<Reserva[]>(`${this.url}/de-mis-publicaciones`);
  }

  aprobar(id: number) {
    return this.http.put<Reserva>(`${this.url}/${id}/aprobar`, {});
  }

  rechazar(id: number, motivo: string) {
    return this.http.put<Reserva>(`${this.url}/${id}/rechazar`, { motivo });
  }

  pagar(id: number, datos: DatosPago) {
    return this.http.post<Reserva>(`${this.url}/${id}/pagar`, datos);
  }

  // valor a devolver
  consultarDevolucion(id: number) {
    return this.http.get<{ valorDevolucion: number }>(`${this.url}/${id}/devolucion`);
  }

  // comprobante en pdf
  obtenerComprobante(id: number) {
    return this.http.get(`${this.url}/${id}/comprobante`, { responseType: 'blob' });
  }

  // guarda el pdf
  descargarComprobante(id: number, archivo: Blob) {
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(archivo);
    enlace.download = 'comprobante-reserva-' + id + '.pdf';
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }

  cancelar(id: number, motivo: string) {
    return this.http.put<Reserva>(`${this.url}/${id}/cancelar`, { motivo });
  }
}
