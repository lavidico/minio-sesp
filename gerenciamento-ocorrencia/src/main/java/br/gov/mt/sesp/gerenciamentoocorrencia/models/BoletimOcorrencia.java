package br.gov.mt.sesp.gerenciamentoocorrencia.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "boletim_ocorrencia")
public class BoletimOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao")
    private SituacaoBoletimOcorrencia situacao;

    @Column(name = "datahora")
    private LocalDateTime dataHora;

    @Column(name = "idbairro")
    private Long idBairro;

    @Column(name = "rua")
    private String rua;

    @Column(name = "numero")
    private String numero;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "cep")
    private String cep;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "idadministrativo")
    private Long idAdministrativo;
    
    @Column(name = "evidencia")
    private String evidencia;

    @OneToMany(mappedBy = "boletimOcorrencia",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER)
    private Set<Vitima> vitimas;

    @OneToMany(mappedBy = "boletimOcorrencia",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER)
    private Set<Suspeito> suspeitos;

    public BoletimOcorrencia() {
        this.situacao = SituacaoBoletimOcorrencia.REGISTRADO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SituacaoBoletimOcorrencia getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoBoletimOcorrencia situacao) {
        this.situacao = situacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getIdBairro() {
        return idBairro;
    }

    public void setIdBairro(Long idBairro) {
        this.idBairro = idBairro;
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

    public String getEvidencia() {
		return evidencia;
	}

	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}

	public Set<Vitima> getVitimas() {
        if (vitimas == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(vitimas);
    }

    public void incluirVitima(Vitima vitima) {
        if (vitimas == null) {
            vitimas = new HashSet<>();
        }

        vitima.setBoletimOcorrencia(this);

        vitimas.add(vitima);
    }

    public Set<Suspeito> getSuspeitos() {
        if (suspeitos == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(suspeitos);
    }

    public void incluirSuspeito(Suspeito suspeito) {
        if (suspeitos == null) {
            suspeitos = new HashSet<>();
        }

        suspeito.setBoletimOcorrencia(this);

        suspeitos.add(suspeito);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoletimOcorrencia that = (BoletimOcorrencia) o;
        return Objects.equals(id, that.id)
                && situacao == that.situacao
                && Objects.equals(dataHora, that.dataHora)
                && Objects.equals(idBairro, that.idBairro)
                && Objects.equals(rua, that.rua)
                && Objects.equals(complemento, that.complemento)
                && Objects.equals(cep, that.cep)
                && Objects.equals(descricao, that.descricao)
                && Objects.equals(idAdministrativo, that.idAdministrativo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, situacao, dataHora, idBairro, rua, complemento, cep, descricao, idAdministrativo);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public enum SituacaoBoletimOcorrencia {
        PENDENTE,
        REGISTRADO,
        FINALIZADO,
        CANCELADO,
        COMPENSACAO_TAREFA;
    }
}


