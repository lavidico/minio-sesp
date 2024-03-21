import {Cidade} from './cidade.model';

export interface Bairro {
  id: number;
  nome: string;
  cidade: Cidade;
}
