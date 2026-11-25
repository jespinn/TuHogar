import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API, Indicadores } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private http = inject(HttpClient);

  // fechas opcionales (aaaa-mm-dd)
  private armarFiltro(desde: string, hasta: string) {
    const filtro: Record<string, string> = {};
    if (desde) filtro['desde'] = desde;
    if (hasta) filtro['hasta'] = hasta;
    return filtro;
  }

  consultarIndicadores(desde = '', hasta = '') {
    return this.http.get<Indicadores>(`${API}/reportes/indicadores`, { params: this.armarFiltro(desde, hasta) });
  }

  // descarga con token
  exportar(tipo: string, formato: string, desde = '', hasta = '') {
    const params = { tipo, formato, ...this.armarFiltro(desde, hasta) };
    return this.http.get(`${API}/reportes/exportar`, { params, responseType: 'blob' });
  }
}
