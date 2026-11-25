import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { validarClavesIguales, leerError, obtenerMensajeError, mostrarErrorEnCampo } from '../../models/validaciones';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-recuperar',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recuperar.component.html',
})
export class RecuperarComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  enviado = ''; // mensaje del servidor
  codigo = ''; // código simulado
  expira = '';
  cargando = false;
  error = '';

  pedido = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
  });

  cambio = this.fb.nonNullable.group(
    {
      codigo: ['', Validators.required],
      contrasena: ['', [Validators.required, Validators.minLength(8)]],
      confirmacion: ['', Validators.required],
    },
    { validators: validarClavesIguales }
  );

  errorPedido = (campo: string) => obtenerMensajeError(this.pedido, campo);
  errorCambio = (campo: string) => obtenerMensajeError(this.cambio, campo);

  pedirCodigo() {
    this.cargando = true;
    this.error = '';
    this.auth.pedirCodigo(this.pedido.controls.correo.value).subscribe({
      next: (r) => {
        this.enviado = r.mensaje;
        this.codigo = r.codigo ?? ''; // sin código si el correo no existe
        this.expira = r.expiraEn ? r.expiraEn.substring(11, 16) : '';
        this.cargando = false;
      },
      error: (e) => {
        this.error = mostrarErrorEnCampo(this.pedido, leerError(e));
        this.cargando = false;
      },
    });
  }

  cambiarContrasena() {
    if (this.cambio.invalid) {
      this.cambio.markAllAsTouched(); // muestra errores
      return;
    }
    this.cargando = true;
    this.error = '';
    this.auth
      .restablecer({ correo: this.pedido.controls.correo.value, ...this.cambio.getRawValue() })
      .subscribe({
        next: () => this.router.navigate(['/login'], { queryParams: { aviso: 'clave' } }),
        error: (e) => {
          this.error = mostrarErrorEnCampo(this.cambio, leerError(e)); // código no válido
          this.cargando = false;
        },
      });
  }
}
