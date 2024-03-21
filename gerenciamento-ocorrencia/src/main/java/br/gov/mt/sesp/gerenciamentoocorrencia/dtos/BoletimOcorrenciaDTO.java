package br.gov.mt.sesp.gerenciamentoocorrencia.dtos;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

public class BoletimOcorrenciaDTO implements Serializable {

    private Long id;

    private String situacao;

    private String dataHora;

    private Long idBairro;

    private String bairro;

    private String cidade;

    private String rua;

    private String numero;

    private String complemento;

    private String cep;

    private String descricao;

    private Long idAdministrativo;

    private String nomeAdministrativo;

    private List<VitimaDTO> vitimas;

    private List<SuspeitoDTO> suspeitos;

    private boolean compensacaoTarefa;
    
    private String evidencia;

    public BoletimOcorrenciaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public Long getIdBairro() {
        return idBairro;
    }

    public void setIdBairro(Long idBairro) {
        this.idBairro = idBairro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getIdAdministrativo() {
        return idAdministrativo;
    }

    public void setIdAdministrativo(Long idAdministrativo) {
        this.idAdministrativo = idAdministrativo;
    }

    public String getNomeAdministrativo() {
        return nomeAdministrativo;
    }

    public void setNomeAdministrativo(String nomeAdministrativo) {
        this.nomeAdministrativo = nomeAdministrativo;
    }

    public List<VitimaDTO> getVitimas() {
        return vitimas;
    }

    public void setVitimas(List<VitimaDTO> vitimas) {
        this.vitimas = vitimas;
    }

    public List<SuspeitoDTO> getSuspeitos() {
        return suspeitos;
    }

    public void setSuspeitos(List<SuspeitoDTO> suspeitos) {
        this.suspeitos = suspeitos;
    }

    public boolean isCompensacaoTarefa() {
        return compensacaoTarefa;
    }

    public void setCompensacaoTarefa(boolean compensacaoTarefa) {
        this.compensacaoTarefa = compensacaoTarefa;
    }

    public String getEvidencia() {
		return evidencia;
	}

	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
