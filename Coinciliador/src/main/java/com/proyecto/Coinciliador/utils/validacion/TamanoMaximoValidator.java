package com.proyecto.Coinciliador.utils.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/** Valida que el archivo no supere 10 MB (10.485.760 bytes). Sin archivo, lo decide {@link ExtensionPermitida}. */
public class TamanoMaximoValidator implements ConstraintValidator<TamanoMaximo, MultipartFile> {

    static final long LIMITE_BYTES = 10L * 1024 * 1024;

    @Override
    public boolean isValid(MultipartFile archivo, ConstraintValidatorContext contexto) {
        return archivo == null || archivo.getSize() <= LIMITE_BYTES;
    }
}
