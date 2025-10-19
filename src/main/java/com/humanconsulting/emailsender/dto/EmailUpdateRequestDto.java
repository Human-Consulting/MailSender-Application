package com.humanconsulting.emailsender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailUpdateRequestDto {
    private String descricaoProjeto;
    private String descricaoSprint;
    private String descricaoTarefa;
    private String nomeResponsavelTarefa;
    private String emailResponsavelTarefa;
    private String nomeResponsavelProjeto;
    private String emailResponsavelProjeto;
    private String comentario;
    private boolean comImpedimento;
}