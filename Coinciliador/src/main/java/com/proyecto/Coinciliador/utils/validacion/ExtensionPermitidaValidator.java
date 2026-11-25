package com.proyecto.Coinciliador.utils.validacion;

import com.proyecto.Coinciliador.helper.ExtensionHelper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/** Valida la extensión del nombre original con {@link ExtensionHelper}. Sin archivo, no es válido. */
public class ExtensionPermitidaValidator implements ConstraintValidator<ExtensionPermitida, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile archivo, ConstraintValidatorContext contexto) {
        return archivo != null && ExtensionHelper.esPermitida(archivo.getOriginalFilename());
    }
}
