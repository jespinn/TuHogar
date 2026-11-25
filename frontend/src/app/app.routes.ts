import { Routes } from '@angular/router';
import { adminGuard, authGuard, rolGuard } from './guards/auth.guard';
import { InicioComponent } from './components/inicio/inicio.component';
import { LoginComponent } from './components/login/login.component';
import { RegistroComponent } from './components/registro/registro.component';
import { RecuperarComponent } from './components/recuperar/recuperar.component';
import { PerfilComponent } from './components/perfil/perfil.component';
import { ReportesComponent } from './components/reportes/reportes.component';
import { AlojamientosComponent } from './components/alojamientos/alojamientos.component';
import { AlojamientoDetalleComponent } from './components/alojamiento-detalle/alojamiento-detalle.component';
import { PagoComponent } from './components/pago/pago.component';
import { MisReservasComponent } from './components/mis-reservas/mis-reservas.component';
import { ReservasAnfitrionComponent } from './components/reservas-anfitrion/reservas-anfitrion.component';
import { TodasLasReservasComponent } from './components/todas-las-reservas/todas-las-reservas.component';
import { UsuariosComponent } from './components/usuarios/usuarios.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: InicioComponent },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'recuperar', component: RecuperarComponent },
  { path: 'perfil', component: PerfilComponent, canActivate: [authGuard] },
  { path: 'reportes', component: ReportesComponent, canActivate: [adminGuard] },
  { path: 'todas-las-reservas', component: TodasLasReservasComponent, canActivate: [adminGuard] },
  { path: 'usuarios', component: UsuariosComponent, canActivate: [adminGuard] },
  // alojamientos públicos
  { path: 'alojamientos', component: AlojamientosComponent },
  { path: 'alojamientos/:id', component: AlojamientoDetalleComponent },
  // rutas del huésped
  { path: 'reservas/:id/pagar', component: PagoComponent, canActivate: [rolGuard('HUESPED')] },
  { path: 'mis-reservas', component: MisReservasComponent, canActivate: [rolGuard('HUESPED')] },
  // rutas del anfitrión
  { path: 'reservas-anfitrion', component: ReservasAnfitrionComponent, canActivate: [rolGuard('ANFITRION')] },
  { path: '**', redirectTo: '' },
];
