import {PessoaService} from 'src/app/services';
import {Component, OnInit, TemplateRef} from '@angular/core';
import {BoletimOcorrencia, Suspeito} from 'src/app/models';
import {AuthService, BoletimOcorrenciaService, permissoes} from '../../../services';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import {BairroService} from '../../../services/api-registro-geral/bairro.service';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-listar-boletim-ocorrencia',
  templateUrl: './listar-boletim.component.html',
  styleUrls: ['./listar-boletim.component.css']
})

/**
 * Classe responsável pelo controle do template de listagem de BOs
 */
export class ListarBoletimComponent implements OnInit {

  paginaAtual = 1;
  paginas: Array<number>;
  _isCidadao = false;
  _isViatura = false;
  _evidenciaValida = false;
  _titulo;
  _boletins: any[];
  modalRef: BsModalRef;
  boletimFiltradoPorId: any[];
  boletimID: any;
  totalRegistros: number;

  constructor(private modalService: BsModalService,
              private boletimService: BoletimOcorrenciaService,
              private pessoaService: PessoaService,
              private bairroService: BairroService,
              private authService: AuthService) {
  }

  /**
   * Método executado quando o componente é inicializado.
   * Aqui a lista de Boletins é trazida do backend
   */
  async ngOnInit(): Promise<void> {
    await this.getBoletins();
  }

  /**
   * Método responsável por exibir os detalhes de um boletim de ocorrência selecionado
   * @param id id do objeto a ser exibido
   * @param template modal a ser aberto com os detalhes do boletim.
   */
  public exibeDetalheBoletim(id: string, template: TemplateRef<any>): void {

    this.boletimFiltradoPorId = this._boletins.filter((boletim) => {
      return boletim.id === id;
    });

    this.boletimID = this.boletimFiltradoPorId[0];
    
    this.boletimService.buscar(this.boletimFiltradoPorId[0].id)
      .pipe(take(1)).toPromise()
      .then(x => {
        this.boletimID = x;
      });
    
    /*.subscribe({
      next(x) {
        this.boletimID = x;
      },
      error(e) {
        console.log(e);
      },
      complete() {
        console.log("completou");
      }
    });
*/
    this.modalRef = this.modalService.show(template);
  }

  protected async getBoletins(): Promise<void> {
    const permissao = this.authService.permissaoUsuario;
    this._isViatura = permissoes.get('VT') === permissao;

    if (permissoes.get('CD') === permissao) {
      this._isCidadao = true;

      const email = this.authService.decodeToken().email;

      const listaPaginada = await this.boletimService.listarPorVitima(email, this.paginaAtual);
      this._titulo = listaPaginada.totalElements === 0 ? 'Você não possui ocorrências cadastradas' : 'Meus Boletins de Ocorrência';
      this._boletins = listaPaginada.content;
      this.paginas = new Array(listaPaginada.totalpages);
      this.totalRegistros = listaPaginada.totalElements;

    } else {
      this.boletimService.listar(this.paginaAtual).subscribe(
        listaPaginada => {
          this._boletins = listaPaginada.content;
          this.paginas = new Array(listaPaginada.totalPages);
          this.totalRegistros = listaPaginada.totalElements;
        },
        (error) => {
          console.log(error.error.message);
        }
      );
    }
  }

  get isCidadao(): boolean {
    return this._isCidadao;
  }

  get isViatura(): boolean {
    return this._isViatura;
  }

  get titulo(): string {
    return this._titulo;
  }

  public async pageChanged(event) {
    this.paginaAtual = event;
    await this.trocaPagina(this.paginaAtual);
  }

  protected async trocaPagina(i) {
    this.paginaAtual = i;
    await this.getBoletins();
  }

  protected async paraDadosBasicos(idPessoa: number, caracteristicas: string, lista: DadosBasicosPessoa[]): Promise<void> {
    if (idPessoa) {
      const pessoaConsulta = await this.pessoaService.buscar(idPessoa);
      lista.push({nome: pessoaConsulta.nome, caracteristicas: caracteristicas});
    } else {
      lista.push({caracteristicas: caracteristicas});
    }
  }

}

interface DadosBasicosPessoa {
  nome?: string;
  caracteristicas: string;
}
