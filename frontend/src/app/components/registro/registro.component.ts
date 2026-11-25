import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { validarClavesIguales, leerError, obtenerMensajeError, mostrarErrorEnCampo } from '../../models/validaciones';
import { AuthService } from '../../services/auth.service';

// roles a elegir
function alMenosUnRol(grupo: AbstractControl): ValidationErrors | null {
  return grupo.get('huesped')?.value || grupo.get('anfitrion')?.value ? null : { sinRol: true };
}

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
})
export class RegistroComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  cargando = false;
  error = '';

  // reglas del formulario
  form = this.fb.nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/^[A-Za-zÁÉÍÓÚáéíóúÑñÜü .'-]*$/)]],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      tipoDocumento: ['CC', Validators.required],
      numeroDocumento: ['', [Validators.required, Validators.maxLength(20)]],
      contrasena: ['', [Validators.required, Validators.minLength(8)]],
      confirmacion: ['', Validators.required],
      huesped: [false],
      anfitrion: [false],
      acepta: [false, Validators.requiredTrue],
    },
    { validators: [validarClavesIguales, alMenosUnRol] }
  );

  errorDe = (campo: string) => obtenerMensajeError(this.form, campo);

  faltaElegirRol(): boolean {
    const f = this.form.controls;
    return !!this.form.errors?.['sinRol'] && (f.huesped.touched || f.anfitrion.touched);
  }

  enviar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra errores
      return;
    }
    const v = this.form.getRawValue();
    const roles = [v.huesped ? 'HUESPED' : '', v.anfitrion ? 'ANFITRION' : ''].filter((r) => r);
    this.cargando = true;
    this.error = '';
    this.auth
      .registrar({
        nombre: v.nombre,
        correo: v.correo,
        contrasena: v.contrasena,
        confirmacion: v.confirmacion,
        tipoDocumento: v.tipoDocumento,
        numeroDocumento: v.numeroDocumento,
        roles,
        aceptaTratamientoDatos: v.acepta,
      })
      .subscribe({
        next: () => this.router.navigate(['/login'], { queryParams: { aviso: 'registrado' } }),
        error: (e) => {
          this.error = mostrarErrorEnCampo(this.form, leerError(e)); // correo ya existe
          this.cargando = false;
        },
      });
  }
}
