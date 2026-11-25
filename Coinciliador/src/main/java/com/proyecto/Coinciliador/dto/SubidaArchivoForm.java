package com.proyecto.Coinciliador.dto;

import com.proyecto.Coinciliador.utils.validacion.ExtensionPermitida;
import com.proyecto.Coinciliador.utils.validacion.TamanoMaximo;
import org.springframework.web.multipart.MultipartFile;

/** Formulario de subida de un archivo a un lugar (RF-3, RF-4, RF-5). */
public record SubidaArchivoForm(@ExtensionPermitida @TamanoMaximo MultipartFile archivo) {
}
