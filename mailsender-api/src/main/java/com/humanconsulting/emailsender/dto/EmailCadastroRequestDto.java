package com.humanconsulting.emailsender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCadastroRequestDto {
    public String nome;
    public String email;
    public String senha;
    public String cargo;
    public String area;
    public String nomeEmpresa;
}
