package com.proyecto.Coinciliador.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Cubre RF-18: qué formatos de Excel son fechas y cómo se muestran (D10).
class FechaExcelHelperTest {

    @ParameterizedTest
    @ValueSource(strings = {"m/d/yy", "dd/mm/yyyy;@", "d-mmm-yy", "mmm-yy", "d-mmm", "mmmm",
            "[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy", "d \"de\" mmmm \"de\" yyyy", "YYYY-MM-DD"})
    void unaFechaSinHoraSeMuestraComoDiaMesAnio(String formato) {
        assertThat(FechaExcelHelper.patron(formato)).contains("dd/MM/yy");
    }

    @ParameterizedTest
    @ValueSource(strings = {"m/d/yy h:mm", "dd/mm/yyyy hh:mm:ss", "yyyy-mm-dd h:mm AM/PM"})
    void unaFechaConHoraConservaLaHora(String formato) {
        assertThat(FechaExcelHelper.patron(formato)).contains("dd/MM/yy HH:mm");
    }

    @ParameterizedTest
    @ValueSource(strings = {"h:mm", "h:mm:ss AM/PM", "mm:ss", "[h]:mm:ss", "0.00", "General", "#,##0 \"días\""})
    void unaHoraSolaOUnNumeroNoSonUnaFecha(String formato) {
        assertThat(FechaExcelHelper.patron(formato)).isEmpty();
    }
}
