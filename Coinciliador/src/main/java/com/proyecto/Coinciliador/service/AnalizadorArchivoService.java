package com.proyecto.Coinciliador.service;

import com.proyecto.Coinciliador.helper.ExtensionHelper;
import com.proyecto.Coinciliador.model.EstadoLugar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Elige el lector según la extensión: ".csv" va al lector de CSV y cualquier otra al de Excel.
 * La extensión ya fue validada antes (@ExtensionPermitida), por eso no se vuelve a controlar acá.
 */
@Service
@RequiredArgsConstructor
public class AnalizadorArchivoService implements IAnalizadorArchivoService {

    private static final String EXTENSION_CSV = "csv";

    private final ILectorCsvService lectorCsv;
    private final ILectorExcelService lectorExcel;

    @Override
    public EstadoLugar analizar(String nombre, byte[] bytes) {
        if (EXTENSION_CSV.equals(ExtensionHelper.extension(nombre))) {
            return lectorCsv.leer(nombre, bytes);
        }
        return lectorExcel.leer(nombre, bytes);
    }
}
