import {Endereco} from './endereco.model';
import {Bairro} from './bairro.model';

export interface Pessoa {

  id?: number;
  nome: string;
  nomeMae: string;
  dataNascimento: string;
  telefones: string;
  email: string;
  cpf: string;
  bairro: Bairro;
  rua: string;
  numero: string;
  complemento: string;
  cep: string;
  endereco?: Endereco;

}
