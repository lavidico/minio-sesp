import {VitimaDto} from './vitima.dto';
import {SuspeitoDto} from './suspeito.dto';

export interface BoletimOcorrenciaDTO {
  id?: number;
  situacao?: string;
  dataHora: string;
  descricao: string;
  cep: string;
  rua: string;
  numero: string;
  complemento: string;
  idBairro: number;
  nomeAdministrativo: string;
  vitimas?: Array<VitimaDto>;
  suspeitos?: Array<SuspeitoDto>;
  compensacaoTarefa:boolean;
}
