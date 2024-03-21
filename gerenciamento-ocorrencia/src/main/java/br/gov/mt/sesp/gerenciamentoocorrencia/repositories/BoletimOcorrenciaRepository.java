package br.gov.mt.sesp.gerenciamentoocorrencia.repositories;

import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoletimOcorrenciaRepository extends JpaRepository<BoletimOcorrencia, Long> {

    @Query("SELECT DISTINCT b FROM BoletimOcorrencia b "
            + " LEFT JOIN b.vitimas vt "
            + " LEFT JOIN b.suspeitos sp "
            + " WHERE (b.situacao = :situacao OR :situacao IS NULL) "
            + " AND (vt.id = :idVitima OR :idVitima IS NULL) "
            + " AND (sp.id = :idSuspeito OR :idSuspeito IS NULL) "
            + " AND (vt.idPessoa = :idPessoaVitima OR :idPessoaVitima IS NULL) "
    )
    Page<BoletimOcorrencia> findAll(Pageable pageable,
                                    @Param("situacao") SituacaoBoletimOcorrencia situacao,
                                    @Param("idVitima") Long idVitima,
                                    @Param("idSuspeito") Long idSuspeito,
                                    @Param("idPessoaVitima") Long idPessoaVitima);
}
