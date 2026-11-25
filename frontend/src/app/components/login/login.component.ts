import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { obtenerMensajeError, leerError } from '../../models/validaciones';
import { AuthService } from '../../services/auth.service';

const AVISOS: Record<string, string> = {
  registrado: 'Cuenta creada. Ya puede iniciar sesión.',
  desactivada: 'Su cuenta quedó inactiva. Se cerró la sesión.',
  clave: 'Contraseña actualizada. Ya puede iniciar sesión.',
  vencida: 'Su sesión venció. Inicie sesión de nuevo.',
};

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  aviso = AVISOS[inject(ActivatedRoute).snapshot.queryParams['aviso']] ?? '';
  cargando = false;
  error = '';
  cuentaInactiva = false;
  verClave = false; // mostrar contraseña

  form = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', Validators.required],
  });

  errorDe = (campo: string) => obtenerMensajeError(this.form, campo);

  entrar() {
    this.cargando = true;
    this.error = '';
    this.cuentaInactiva = false;
    this.auth.iniciarSesion(this.form.controls.correo.value, this.form.controls.contrasena.value).subscribe({
      next: () => this.router.navigate(['/perfil']),
      error: (e) => {
        const err = leerError(e);
        this.cuentaInactiva = err.codigo === 'CUENTA_INACTIVA';
        this.error = err.mensaje;
        this.cargando = false;
      },
    });
  }

  reactivar() {
    this.cargando = true;
    this.auth.reactivar(this.form.controls.correo.value, this.form.controls.contrasena.value).subscribe({
      next: () => this.router.navigate(['/perfil']),
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }
}
