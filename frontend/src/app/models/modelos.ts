// tipos del frontend
import { environment } from '../../environments/environment';

// dirección de la api
export const API = environment.apiUrl;

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  tipoDocumento: string;
  numeroDocumento: string;
  estado: string;
  roles: string[];
  fechaRegistro: string;
}

export interface Sesion {
  token: string;
  usuario: Usuario;
}

export interface RegistroDatos {
  nombre: string;
  correo: string;
  contrasena: string;
  confirmacion: string;
  tipoDocumento: string;
  numeroDocumento: string;
  roles: string[];
  aceptaTratamientoDatos: boolean;
}

export interface PerfilDatos {
  nombre: string;
  tipoDocumento: string;
  numeroDocumento: string;
}

// formato de error
export interface ErrorApi {
  codigo: string;
  mensaje: string;
  campo: string | null;
}

// categoría y cantidad
export interface Indicadores {
  usuariosPorRol: Record<string, number>;
  usuariosPorEstado: Record<string, number>;
  publicacionesPorEstado: Record<string, number>;
  publicacionesPorCiudad: Record<string, number>;
  publicacionesPorTipo: Record<string, number>;
  reservasPorEstado: Record<string, number>;
  ingresosEstimados: number;
  reservasPorMes: Record<string, number>;
  reservasPorTipo: Record<string, number>;
  reservasPorCiudad: Record<string, number>;
  fechasMasFrecuentes: Record<string, number>;
  nochesFinDeSemana: Record<string, number>;
  ingresosPorReserva: { idReserva: number; alojamiento: string; ciudad: string; llegada: string; salida: string; total: number }[];
  alojamientosSinReservas: { id: number; titulo: string; ciudad: string; tipo: string }[];
  reservasRecientes: Record<string, number>; // periodos del panel
  aviso: string | null; // rango sin datos
}

// reservas

export interface AlojamientoResumen {
  id: number;
  titulo: string;
  ciudad: string;
  tipoAlojamiento: string;
  capacidad: number;
  precioNoche: number;
  imagenPrincipal: string | null;
}

export interface AlojamientoDetalle {
  id: number;
  titulo: string;
  descripcion: string;
  ciudad: string;
  tipoAlojamiento: string;
  capacidad: number;
  precioNoche: number;
  tarifaLimpieza: number;
  reglas: string | null;
  modoReserva: string;
  anfitrion: string;
  imagenes: string[];
  servicios: { id: number; nombre: string; costo: number }[];
  disponibilidad: { desde: string; hasta: string }[];
  politicaCancelacion: { tipo: string; diasAnticipacion: number; porcentajeDevolucion: number } | null;
}

export interface SolicitudReserva {
  publicacionId: number;
  llegada: string;
  salida: string;
  huespedes: number;
  serviciosIds: number[];
  claveIdempotencia?: string;
}

export interface Cotizacion {
  noches: number;
  tarifaBase: number;
  cargoLimpieza: number;
  serviciosAdicionales: number;
  cargoServicio: number;
  descuentos: number;
  total: number;
}

export interface Reserva {
  id: number;
  alojamientoId: number;
  alojamiento: string;
  ciudad: string;
  huesped: string;
  llegada: string;
  salida: string;
  noches: number;
  huespedes: number;
  tarifaBase: number;
  cargoLimpieza: number;
  serviciosAdicionales: number;
  cargoServicio: number;
  descuentos: number;
  total: number;
  estado: string;
  referenciaPago: string | null;
  valorDevolucion: number | null;
  // botones por estado
  puedePagar: boolean;
  puedeCancelar: boolean;
  puedeResponder: boolean;
}

export interface DatosPago {
  numeroTarjeta: string;
  titular: string;
  codigoSeguridad: string;
}

/** reserva para administrador */
export interface ReservaAdmin {
  id: number;
  alojamientoId: number;
  alojamiento: string;
  huesped: string;
  anfitrion: string;
  llegada: string;
  salida: string;
  total: number;
  estado: string;
}

/** filtros opcionales */
export interface FiltrosReservas {
  estado: string;
  desde: string;
  hasta: string;
  publicacion: string;
}
