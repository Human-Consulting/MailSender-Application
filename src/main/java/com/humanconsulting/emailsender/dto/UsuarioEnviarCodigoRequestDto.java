package com.humanconsulting.emailsender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioEnviarCodigoRequestDto {
    public String email;
    public String codigo;
}
