package com.proyecto.Coinciliador.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.proyecto.Coinciliador.enums.Lugar;
import com.proyecto.Coinciliador.enums.MensajeCarga;
import com.proyecto.Coinciliador.enums.TipoConciliacion;
import com.proyecto.Coinciliador.model.ArchivoCargado;
import com.proyecto.Coinciliador.model.EstadoLugar;
import com.proyecto.Coinciliador.model.Resumen;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

// Cubre RF-2 (una sola carga), RF-24 (reiniciar por tipo), RNF-1 (todo en memoria) y RNF-2 (compartida entre pestañas).
class CargaEnMemoriaRepositoryTest {

    private final CargaEnMemoriaRepository repositorio = new CargaEnMemoriaRepository();

    @Test
    void empiezaSinTipoActivoYConAmbosLugaresVacios() {
        assertThat(repositorio.tipoActivo()).isEmpty();
        assertThat(repositorio.obtener().propio()).isEqualTo(new EstadoLugar.Vacio());
        assertThat(repositorio.obtener().contraparte()).isEqualTo(new EstadoLugar.Vacio());
        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void guardarUnLugarNoModificaElOtro() {
        EstadoLugar rechazado = new EstadoLugar.Rechazado("malo.ods", MensajeCarga.M1);
        EstadoLugar propio = valido("propio.csv");
        repositorio.guardar(Lugar.CONTRAPARTE, rechazado);

        repositorio.guardar(Lugar.PROPIO, propio);

        assertThat(repositorio.obtener().contraparte()).isEqualTo(rechazado);
        assertThat(repositorio.obtener().propio()).isEqualTo(propio);
    }

    // Se guarda y se compara la misma instancia: ArchivoCargado lleva un byte[] y los arreglos se comparan por referencia.
    @Test
    void guardarEnContraparteTampocoModificaPropio() {
        EstadoLugar propio = valido("propio.csv");
        repositorio.guardar(Lugar.PROPIO, propio);

        repositorio.guardar(Lugar.CONTRAPARTE, new EstadoLugar.Rechazado("malo.ods", MensajeCarga.M1));

        assertThat(repositorio.obtener().propio()).isEqualTo(propio);
    }

    @Test
    void confirmarMarcaLaCarga() {
        repositorio.confirmar();

        assertThat(repositorio.obtener().confirmada()).isTrue();
    }

    @Test
    void cualquierGuardadoBorraLaConfirmacion() {
        repositorio.confirmar();

        repositorio.guardar(Lugar.CONTRAPARTE, new EstadoLugar.Rechazado("malo.ods", MensajeCarga.M1));

        assertThat(repositorio.obtener().confirmada()).isFalse();
    }

    @Test
    void dosLecturasDevuelvenLaMismaCarga() {
        repositorio.guardar(Lugar.PROPIO, valido("propio.csv"));

        assertThat(repositorio.obtener()).isSameAs(repositorio.obtener());
    }

    @Test
    void guardarNoCambiaElTipoActivo() {
        repositorio.reiniciar(TipoConciliacion.VENTAS);

        repositorio.guardar(Lugar.PROPIO, valido("propio.csv"));

        assertThat(repositorio.tipoActivo()).contains(TipoConciliacion.VENTAS);
    }

    @Test
    void reiniciarDejaAmbosLugaresVaciosSinConfirmacionYConEseTipo() {
        repositorio.guardar(Lugar.PROPIO, valido("propio.csv"));
        repositorio.guardar(Lugar.CONTRAPARTE, new EstadoLugar.Rechazado("malo.ods", MensajeCarga.M1));
        repositorio.confirmar();

        repositorio.reiniciar(TipoConciliacion.IVA);

        assertThat(repositorio.obtener().propio()).isEqualTo(new EstadoLugar.Vacio());
        assertThat(repositorio.obtener().contraparte()).isEqualTo(new EstadoLugar.Vacio());
        assertThat(repositorio.obtener().confirmada()).isFalse();
        assertThat(repositorio.tipoActivo()).isEqualTo(Optional.of(TipoConciliacion.IVA));
    }

    @Test
    void reiniciarConOtroTipoReemplazaElTipoActivo() {
        repositorio.reiniciar(TipoConciliacion.IVA);

        repositorio.reiniciar(TipoConciliacion.COMPRAS);

        assertThat(repositorio.tipoActivo()).contains(TipoConciliacion.COMPRAS);
    }

    private EstadoLugar.Valido valido(String nombre) {
        ArchivoCargado archivo = ArchivoCargado.csv(nombre, new byte[0], "huella", ';', StandardCharsets.UTF_8);
        return new EstadoLugar.Valido(archivo, new Resumen(nombre, null, 1, List.of()));
    }
}
