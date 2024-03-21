import {VitimaSave} from './vitima-save.dto';
import {SuspeitoSave} from './suspeito-save.dto';

export interface BoletimOcorrenciaSave {
  id?: number;
  situacao?: string;
  dataHora: string;
  descricao: string;
  cep: string;
  rua: string;
  numero: string;
  complemento: string;
  idBairro: number;
  idAdministrativo: number;
  vitimas?: Array<VitimaSave>;
  suspeitos?: Array<SuspeitoSave>;
  compensacaoTarefa?:boolean;
  evidencia?:string;
}
