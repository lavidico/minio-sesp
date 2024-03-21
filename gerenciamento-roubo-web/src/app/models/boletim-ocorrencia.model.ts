import {Vitima} from './vitima.model';
import {Suspeito} from './suspeito.model';

export interface BoletimOcorrencia {
  id?: number;
  situacao: string;
  dataHora: string;
  idBairro: number;
  rua: string;
  numero: string;
  complemento: string;
  cep: string;
  descricao: string;
  idAdministrativo: number;
  vitimas: Array<Vitima>;
  suspeitos: Array<Suspeito>;
  evidencia: string;
}
