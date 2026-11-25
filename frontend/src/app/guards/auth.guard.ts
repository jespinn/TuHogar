import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** requiere sesión */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.tieneSesionVigente()) return true;
  auth.salir(auth.token !== null); // sesión vencida
  return false;
};

/** solo administrador */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.tieneSesionVigente()) {
    auth.salir(auth.token !== null);
    return false;
  }
  return auth.tieneRol('ADMINISTRADOR') ? true : router.createUrlTree(['/perfil']);
};

/** requiere el rol */
export function rolGuard(rol: string): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    if (!auth.tieneSesionVigente()) {
      auth.salir(auth.token !== null);
      return false;
    }
    return auth.tieneRol(rol) ? true : inject(Router).createUrlTree(['/perfil']);
  };
}
