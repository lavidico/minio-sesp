import {Bairro} from './bairro.model';

export interface Endereco {

  id: number;
  rua: string;
  numero: string;
  complemento: string;
  cep: string;
  bairro: Bairro;

}
