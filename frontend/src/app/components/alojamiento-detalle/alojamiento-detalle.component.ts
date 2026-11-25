import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlojamientoDetalle, Cotizacion } from '../../models/modelos';
import { leerError, obtenerMensajeError, mostrarErrorEnCampo } from '../../models/validaciones';
import { ReservaService } from '../../services/reserva.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-alojamiento-detalle',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, ReactiveFormsModule],
  templateUrl: './alojamiento-detalle.component.html',
})
export class AlojamientoDetalleComponent implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private servicio = inject(ReservaService);
  auth = inject(AuthService); // sesión del usuario

  alojamiento: AlojamientoDetalle | null = null;
  cotizacion: Cotizacion | null = null;
  serviciosElegidos: number[] = [];
  cargando = true;
  cotizando = false;
  reservando = false;
  error = '';
  hoy = new Date().toLocaleDateString('en-CA'); // AAAA-MM-DD
  claveIdempotencia = crypto.randomUUID(); // evita duplicados

  // reglas del formulario
  form = this.fb.group(
    {
      llegada: ['', Validators.required],
      salida: ['', Validators.required],
      huespedes: [1, [Validators.required, Validators.min(1)]],
    },
    { validators: (g: AbstractControl) => this.validarFechas(g) }
  );

  errorDe = (campo: string) => obtenerMensajeError(this.form, campo);
  errorGrupo = (clave: string) => !!this.form.errors?.[clave];

  ngOnInit() {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.consultarAlojamiento(id).subscribe({
      next: (a) => {
        this.alojamiento = a;
        this.form.controls.huespedes.addValidators(Validators.max(a.capacidad));
        this.form.updateValueAndValidity();
        this.cargando = false;
      },
      error: (e) => {
        this.error = leerError(e).mensaje;
        this.cargando = false;
      },
    });
    this.form.valueChanges.subscribe(() => (this.cotizacion = null)); // cotizar de nuevo
  }

  private validarFechas(g: AbstractControl): ValidationErrors | null {
    const { llegada, salida } = g.value;
    const errores: ValidationErrors = {};
    if (llegada && llegada < this.hoy) errores['llegadaPasada'] = true;
    else if (llegada && salida && salida <= llegada) errores['salidaAnterior'] = true;
    else if (llegada && salida && this.alojamiento &&
      !this.alojamiento.disponibilidad.some((d) => llegada >= d.desde && salida <= d.hasta)) {
      errores['fueraDePeriodo'] = true;
    }
    return Object.keys(errores).length ? errores : null;
  }

  alternarServicio(id: number, marcado: boolean) {
    this.serviciosElegidos = marcado ? [...this.serviciosElegidos, id] : this.serviciosElegidos.filter((s) => s !== id);
    this.cotizacion = null;
  }

  /** texto de la política */
  obtenerTextoPolitica(): string {
    const pol = this.alojamiento?.politicaCancelacion;
    if (!pol) return 'Esta publicación no tiene política de cancelación definida.';
    const dias = pol.diasAnticipacion === 1 ? '1 día' : `${pol.diasAnticipacion} días`;
    return `Política ${pol.tipo.toLowerCase()}: devolución del ${pol.porcentajeDevolucion} % si cancelas con ${dias} o más de anticipación.`;
  }

  private armarSolicitud(conClave: boolean) {
    const v = this.form.getRawValue();
    return {
      publicacionId: this.alojamiento!.id,
      llegada: v.llegada ?? '',
      salida: v.salida ?? '',
      huespedes: v.huespedes ?? 1,
      serviciosIds: this.serviciosElegidos,
      claveIdempotencia: conClave ? this.claveIdempotencia : undefined,
    };
  }

  cotizar() {
    this.cotizando = true;
    this.error = '';
    this.servicio.cotizar(this.armarSolicitud(false)).subscribe({
      next: (c) => {
        this.cotizacion = c;
        this.cotizando = false;
      },
      error: (e) => this.fallar(e, 'cotizando'),
    });
  }

  reservar() {
    this.reservando = true;
    this.error = '';
    this.servicio.reservar(this.armarSolicitud(true)).subscribe({
      next: (r) => {
        // siguiente pantalla
        if (r.puedePagar) this.router.navigate(['/reservas', r.id, 'pagar']);
        else this.router.navigate(['/mis-reservas'], { queryParams: { aviso: 'reservada' } });
      },
      error: (e) => this.fallar(e, 'reservando'),
    });
  }

  private fallar(e: any, bandera: 'cotizando' | 'reservando') {
    this.error = mostrarErrorEnCampo(this.form, leerError(e)); // mensaje del servidor
    this[bandera] = false;
  }
}
