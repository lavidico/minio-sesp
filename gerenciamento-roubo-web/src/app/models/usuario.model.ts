export class Usuario {
  private _nome: string;
  private _permissao: string;
  private _dadosToken: any;


  get nome(): string {
    return this._nome;
  }

  set nome(value: string) {
    this._nome = value;
  }

  get permissao(): string {
    return this._permissao;
  }

  set permissao(value: string) {
    this._permissao = value;
  }

  get dadosToken(): any {
    return this._dadosToken;
  }

  set dadosToken(value: any) {
    this._dadosToken = value;
  }
}



