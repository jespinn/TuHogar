import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Usuario } from '../../models/modelos';
import { leerError, obtenerMensajeError, mostrarErrorEnCampo } from '../../models/validaciones';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './perfil.component.html',
})
export class PerfilComponent implements OnInit {
  private fb = inject(FormBuilder);
  private servicio = inject(UsuarioService);
  private auth = inject(AuthService);

  usuario: Usuario | null = null;
  cargando = true;
  guardando = false;
  error = '';
  exito = '';

  // correo no editable
  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/^[A-Za-zÁÉÍÓÚáéíóúÑñÜü .'-]*$/)]],
    tipoDocumento: ['CC', Validators.required],
    numeroDocumento: ['', [Validators.required, Validators.maxLength(20)]],
  });

  errorDe = (campo: string) => obtenerMensajeError(this.form, campo);

  ngOnInit() {
    this.servicio.verPerfil().subscribe({
      next: (u) => {
        this.mostrar(u);
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
  }

  guardar() {
    this.exito = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra errores
      return;
    }
    this.guardando = true;
    this.error = '';
    this.exito = '';
    this.servicio.actualizarPerfil(this.form.getRawValue()).subscribe({
      next: (u) => {
        this.mostrar(u);
        this.auth.actualizarUsuario(u); // actualiza el nombre
        this.exito = 'Perfil actualizado';
        this.guardando = false;
      },
      error: (e) => {
        this.error = mostrarErrorEnCampo(this.form, leerError(e));
        this.guardando = false;
      },
    });
  }

  esAdministrador = () => this.auth.tieneRol('ADMINISTRADOR');

  desactivarCuenta() {
    if (!confirm('¿Desea desactivar su cuenta? Podrá reactivarla al iniciar sesión.')) return;
    this.servicio.desactivarMiCuenta().subscribe({
      next: () => this.auth.salir(false, 'desactivada'),
      error: (e) => (this.error = leerError(e).mensaje),
    });
  }

  private mostrar(u: Usuario) {
    this.usuario = u;
    this.form.reset({ nombre: u.nombre, tipoDocumento: u.tipoDocumento, numeroDocumento: u.numeroDocumento });
  }
}
