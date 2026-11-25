import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup, ValidationErrors } from '@angular/forms';
import { ErrorApi } from './modelos';

/** error del campo */
export function obtenerMensajeError(form: FormGroup, campo: string): string {
  const control = form.get(campo);
  if (!control || !control.touched) return '';
  const e = control.errors ?? {};
  if (e['required']) return 'Este campo es obligatorio';
  if (e['email']) return 'El correo no es válido';
  if (e['minlength']) return `Debe tener al menos ${e['minlength'].requiredLength} caracteres`;
  if (e['maxlength']) return `No puede superar ${e['maxlength'].requiredLength} caracteres`;
  if (e['min']) return `El valor mínimo es ${e['min'].min}`;
  if (e['max']) return `El máximo es ${e['max'].max}`;
  if (e['pattern']) return 'El formato no es válido';
  if (e['servidor']) return e['servidor'];
  if (campo === 'confirmacion' && form.errors?.['noCoinciden']) return 'Las contraseñas no coinciden';
  return '';
}

/** contraseñas iguales */
export function validarClavesIguales(grupo: AbstractControl): ValidationErrors | null {
  const clave = grupo.get('contrasena')?.value;
  const confirmacion = grupo.get('confirmacion')?.value;
  return clave === confirmacion ? null : { noCoinciden: true };
}

/** convierte el error */
export function leerError(e: HttpErrorResponse): ErrorApi {
  if (e.error && e.error.mensaje) return e.error as ErrorApi;
  return { codigo: 'RED', mensaje: 'No se pudo conectar con el servidor. Intente de nuevo.', campo: null };
}

/** muestra el error */
export function mostrarErrorEnCampo(form: FormGroup, error: ErrorApi): string {
  const control = error.campo ? form.get(error.campo) : null;
  if (!control) return error.mensaje;
  control.setErrors({ servidor: error.mensaje });
  control.markAsTouched();
  return '';
}

/** texto del estado */
export function obtenerTextoEstado(estado: string): string {
  const texto = estado.replaceAll('_', ' ').toLowerCase().replace('aprobacion', 'aprobación').replace('revision', 'revisión');
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
