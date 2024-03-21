import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {DatePipe, Location} from '@angular/common';
import {Bairro, BoletimOcorrencia, Cidade, Pessoa} from 'src/app/models';
import {BoletimOcorrenciaSave, ProcuradoSave, SuspeitoSave, VitimaSave} from 'src/app/models/dtos';
import {BoletimOcorrenciaService, PessoaService, ProcuradoService, SuspeitoService, VitimaService} from 'src/app/services';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import * as moment from 'moment';
import {CidadeService} from 'src/app/services/api-registro-geral/cidade.service';
import {take} from 'rxjs/operators';


@Component({
  selector: 'app-cadastrar-boletim-ocorrencia',
  templateUrl: './cadastrar-boletim.component.html',
  styleUrls: ['./cadastrar-boletim.component.css']
})
/**
 * Classe responsável pelo controle do template de cadastro de BOs
 */
export class CadastrarBoletimComponent implements OnInit {

  form: FormGroup;
  modalRef: BsModalRef;

  @ViewChild('content') content: any;
  @ViewChild('nenhumResultadoObtido') nenhumResultadoObtido: TemplateRef<any>;

  // variaveis para controle de paginação
  consulta: { parametro: string, valor: string };
  paginaAtual = 1;
  paginas: Array<number>;
  totalRegistros: number;


  // flags vitima
  vitimaIdentificada: boolean;
  buscaVitima = false;
  linhaTextAreaVitimas = false;
  linhaTabelaVitimas = false;
  vitimaIncluida = false;
  selecionarOpcaoID = true;

  // flags suspeito
  suspeitoIdentificado: boolean;
  buscaSuspeito = false;
  linhaTextAreaSuspeitos = false;
  linhaTabelaSuspeitos = false;

  parametrosConsulta = ['nome', 'nome-mae', 'cpf', 'data-nascimento'];

  listaPessoaConsulta: Pessoa[] = [];

  _listaCidadeConsulta: Cidade[] = [];
  _listaBairroConsulta: Bairro[] = [];

  private _cidadeSelecionada: Cidade = undefined;
  private _bairroSelecionado: Bairro;

  compensacaoTarefa: boolean = false;

  vitimaSelecionada: Pessoa;
  suspeitoSelecionado: Pessoa;
  selecionarPessoa: (pessoa: Pessoa) => void;

  // representação da tabela de suspeitos
  listaVitimas: { pessoa: Pessoa, caracteristicas: string, identificada: string }[] = [];

  // representação da tabela de suspeitos
  listaSuspeitos: { pessoa: Pessoa, caracteristicas: string, identificada: string }[] = [];
  selecionarOpcaoIDSuspeito = true;
  suspeitoIncluido = false;

  constructor(private formBuilder: FormBuilder,
              private pessoaService: PessoaService,
              private modalService: BsModalService,
              private boletimService: BoletimOcorrenciaService,
              private vitimaService: VitimaService,
              private suspeitoService: SuspeitoService,
              private procuradoService: ProcuradoService,
              private cidadeServico: CidadeService,
              private location: Location
  ) {
  }

  async ngOnInit(): Promise<void> {

    moment.locale('pt-br');

    this.form = this.formBuilder
      .group({
        // FORMULARIO BO
        dataBo: this.formBuilder.control('', [Validators.required]),
        rua: this.formBuilder.control(
          '',
          [
            Validators.required,
            Validators.minLength(3),
            Validators.maxLength(30),
          ]
        ),
        complemento: this.formBuilder.control(
          '',
          [
            Validators.required,
            Validators.minLength(3),
            Validators.maxLength(25),
          ]
        ),
        numero: this.formBuilder.control(
          '',
          [
            Validators.required,
            Validators.maxLength(20),
          ]
        ),
        bairro: this.formBuilder.control(
          '',
          [Validators.required]
        ),
        cidade: this.formBuilder.control(
          '',
          [Validators.required]
        ),
        cep: this.formBuilder.control(
          '',
          [
            Validators.required,
            Validators.minLength(5),
            Validators.maxLength(20),
          ]
        ),
        descricao: this.formBuilder.control(
          '',
          [
            Validators.required,
            Validators.minLength(10),
            Validators.maxLength(255),
          ]
        ),
        compensacao: this.formBuilder.control(
          '',
          []
        ),
        evidencia: this.formBuilder.control(
          '',
          []
        ),

        // --------------------------------------------------------------------//

        // INPUT BUSCA VITIMA
        inputBuscaVitima: this.formBuilder.control('', []),
        criterioBuscaVitima: this.formBuilder.control('', []),

        // INPUTS COM VALORES TRAZIDOS DA BUSCA DA VITIMA
        nomeBuscaVitima: this.formBuilder.control({value: '', disabled: true}, []),
        cpfBuscaVitima: this.formBuilder.control({value: '', disabled: true}, []),
        nomeMaeBuscaVitima: this.formBuilder.control({value: '', disabled: true}, []),
        emailBuscaVitima: this.formBuilder.control({value: '', disabled: true}, []),
        telefoneBuscaVitima: this.formBuilder.control({value: '', disabled: true}, []),
        textAreaBuscaVitima: this.formBuilder.control('', []),


        // --------------------------------------------------------------------//

        // INPUT BUSCA SUSPEITO //
        inputBuscaSuspeito: this.formBuilder.control('', []),
        criterioBuscaSuspeito: this.formBuilder.control('', []),

        // INPUTS COM VALORES TRAZIDOS DA BUSCA DO SUSPEITO
        nomeBuscaSuspeito: this.formBuilder.control({value: '', disabled: true}, []),
        cpfBuscaSuspeito: this.formBuilder.control({value: '', disabled: true}, []),
        nomeMaeBuscaSuspeito: this.formBuilder.control({value: '', disabled: true}, []),
        emailBuscaSuspeito: this.formBuilder.control({value: '', disabled: true}, []),
        telefoneBuscaSuspeito: this.formBuilder.control({value: '', disabled: true}, []),
        textAreaBuscaSuspeito: this.formBuilder.control('', []),

      });

    // Seta o valor do datepicker para o dia atual
    const pipe = new DatePipe('pt-BR');
    const formato = 'y-MM-dd'; // YYYY-MM-DD
    const data = pipe.transform(new Date(), formato);
    this.form.controls.dataBo.setValue(data);

    this._listaCidadeConsulta = await this.cidadeServico.listar();
  }

  onFileChange(event) {

    const file:File = event.target.files[0];
    const reader = new FileReader();

    reader.onloadend = () => {
      const base64img = reader.result as string;
      this.form.value.evidencia = base64img;
    };

    if (file) {
        reader.readAsDataURL(file);
    }
  }

  /**
   * Método para retorno do objeto campo do formulário a partir da inserção de seu nome
   *
   * @param field nome do campo no formulário de cadastro
   * @return AbstractControl campo do formulário correspondente ao título inserido
   */
  getField(field: string): any {
    return this.form.get(field);
  }

  /**
   * Método para verificação de erros em um campo do formulário
   *
   * @param field nome do campo no formulário de cadastro
   * @param erro nome do erro para verificar a presença
   * @return ValidationErrors mapa contendo os erros encontrados no campo
   */
  getFieldErros(field: string, erro?: string): ValidationErrors {
    return erro ? this.form.get(field).errors[erro] : this.form.get(field).errors;
  }

  /**
   * Método especializado para passar os parâmetros para a busca de vítima
   *
   * @param template modal a ser aberto com o resultado da busca
   * @return vazio
   */
  realizaBuscaVitima(template: TemplateRef<any>): void {
    const indiceCriterio = this.form.value.criterioBuscaVitima;
    const parametro = this.parametrosConsulta[indiceCriterio];
    const conteudoBusca = this.form.value.inputBuscaVitima;

    // Método utilizado no template
    this.selecionarPessoa = this.selecionarVitima;

    this.realizaBusca(template, parametro, conteudoBusca);
  }

  /**
   * Método especializado para passar os parâmetros para a busca de suspeito
   *
   * @param template modal a ser aberto com o resultado da busca
   * @return vazio
   */
  realizaBuscaSuspeito(template: TemplateRef<any>): void {
    const indiceCriterio = this.form.value.criterioBuscaSuspeito;
    const parametro = this.parametrosConsulta[indiceCriterio];
    const conteudoBusca = this.form.value.inputBuscaSuspeito;


    // Método utilizado no template
    this.selecionarPessoa = this.selecionarSuspeito;

    this.realizaBusca(template, parametro, conteudoBusca);
  }

  /**
   *  Método generalizado para realizar a busca de vítimas ou suspeitos
   * @param template referência ao template que deve ser aberto
   * @param parametro parâmetro a ser utilizado como critério na busca
   * @param conteudoBusca conteúdo digitado para ser pesquisado
   *
   * @return Promise<void>
   */
  async realizaBusca(template: TemplateRef<any>, parametro: string, conteudoBusca: any): Promise<void> {
    this.consulta = {parametro: parametro, valor: conteudoBusca};
    const consultaPaginada = await this.pessoaService.buscaPaginada(parametro, conteudoBusca, this.paginaAtual);

    if (consultaPaginada.totalElements === 0) {
      this.abrirModal(this.nenhumResultadoObtido);
      return;
    }
    this.parseConsultaPaginadaPessoa(consultaPaginada);
    this.abrirModal(template);

    // if (this.listaPessoaConsulta.length > 0) {
    // } else {
    // }
  }

  async pageOnChange(template: TemplateRef<any>, $event: any): Promise<void> {
    try {
      this.paginaAtual = $event;
      const consultaPaginada = await this.pessoaService
        .buscaPaginada(this.consulta.parametro, this.consulta.valor, this.paginaAtual);
      this.parseConsultaPaginadaPessoa(consultaPaginada);
    } catch (e) {
      this.abrirModal(this.nenhumResultadoObtido);
    }
  }

  parseConsultaPaginadaPessoa(consultaPaginada: any): void {
    this.listaPessoaConsulta = consultaPaginada.content;
    this.paginas = consultaPaginada.totalPage;
    this.totalRegistros = consultaPaginada.totalElements;
  }

  /**
   *  Método para exibição do modal com o resultado da pesquisa
   *
   * @param template template a ser aberto
   *
   * @return vazio
   */
  abrirModal(template: TemplateRef<any>): void {
    this.modalRef = this.modalService.show(template);
  }

  /**
   *  Método responsável por selecionar uma vítima do resultado da pesquisa e
   *  carregar seus dados no formulário de inserção de vítimas
   *
   * @param vitima objeto contendo os dados da vítima selecionada
   *
   * @return vazio
   */
  selecionarVitima(vitima: Pessoa): void {
    this.vitimaSelecionada = vitima;

    this.buscaVitima = true;

    this.form.controls.nomeBuscaVitima.setValue(vitima.nome);
    this.form.controls.cpfBuscaVitima.setValue(vitima.cpf);
    this.form.controls.nomeMaeBuscaVitima.setValue(vitima.nomeMae);
    this.form.controls.emailBuscaVitima.setValue(vitima.email);
    this.form.controls.telefoneBuscaVitima.setValue(vitima.telefones);


    this.modalRef.hide();
  }

  /**
   * Método responsável por adicionar uma nova vítima à lista de  vítimas
   * a serem cadastradas no boletim
   *
   * @return vazio
   */
  adicionarNaListaDeVitimas(): void {

    const caracteristicasVitima: string = this.form.value.textAreaBuscaVitima;

    this.buscaVitima = false;
    this.linhaTextAreaVitimas = false;
    this.selecionarOpcaoID = false;
    this.vitimaIncluida = true;


    const novaVitima = this.adicionarNaTabela(
      this.vitimaSelecionada,
      caracteristicasVitima,
      this.vitimaIdentificada,
    );

    this.listaVitimas.push(novaVitima);
    if (this.listaVitimas.length > 0) {
      this.linhaTabelaVitimas = true;
    }
    this.vitimaIdentificada = null;
  }

  /**
   *  Método responsável por resetar os campos para a inserção de uma nova vitima na lista
   *  @return vazio
   */
  incluirNovaVitima(): void {
    // reseta o form da vitima
    this.selecionarOpcaoID = true;
    this.vitimaIncluida = false;
    this.buscaVitima = false;
    this.vitimaIdentificada = null;
    this.vitimaSelecionada = null;
    this.linhaTextAreaVitimas = false;

    this.form.controls.inputBuscaVitima.setValue('');
    this.form.controls.nomeBuscaVitima.setValue('');
    this.form.controls.cpfBuscaVitima.setValue('');
    this.form.controls.nomeMaeBuscaVitima.setValue('');
    this.form.controls.emailBuscaVitima.setValue('');
    this.form.controls.telefoneBuscaVitima.setValue('');
    this.form.controls.textAreaBuscaVitima.setValue('');
  }

  // SUSPEITO
  /**
   *  Método responsável por selecionar umm supeito do resultado da pesquisa e
   *  carregar seus dados no formulário de inserção de suspeitos
   *
   * @param vitima objeto contendo os dados do suspeito selecionado
   *
   * @return vazio
   */
  selecionarSuspeito(suspeito: Pessoa): void {
    this.suspeitoSelecionado = suspeito;

    this.buscaSuspeito = true;

    this.form.controls.nomeBuscaSuspeito.setValue(suspeito.nome);
    this.form.controls.cpfBuscaSuspeito.setValue(suspeito.cpf);
    this.form.controls.nomeMaeBuscaSuspeito.setValue(suspeito.nomeMae);
    this.form.controls.emailBuscaSuspeito.setValue(suspeito.email);
    this.form.controls.telefoneBuscaSuspeito.setValue(suspeito.telefones);

    this.modalRef.hide();
  }


  /**
   *  Método responsável por resetar os campos para a inserção de um novo suspeito na lista
   *  @return vazio
   */
  incluirNovoSuspeito(): void {
    // reseta o form do suspeito
    this.buscaSuspeito = false;
    this.selecionarOpcaoIDSuspeito = true;
    this.suspeitoIncluido = false;

    this.suspeitoIdentificado = null;
    this.suspeitoSelecionado = null;
    this.linhaTextAreaSuspeitos = false;
    this.form.value.inputBuscaSuspeito = '';


    this.form.controls.nomeBuscaSuspeito.setValue('');
    this.form.controls.cpfBuscaSuspeito.setValue('');
    this.form.controls.nomeMaeBuscaSuspeito.setValue('');
    this.form.controls.emailBuscaSuspeito.setValue('');
    this.form.controls.telefoneBuscaSuspeito.setValue('');
    this.form.controls.textAreaBuscaSuspeito.setValue('');
  }

  /**
   * Método responsável por adicionar um novo suspeito à lista de supeitos
   * a serem cadastrados no boletim
   *
   * @return vazio
   */
  adicionarNaListaSuspeito(): void {

    this.buscaSuspeito = false;
    this.linhaTextAreaSuspeitos = false;
    this.selecionarOpcaoIDSuspeito = false;
    this.suspeitoIncluido = true;

    const caracteristicas: string = this.form.value.textAreaBuscaSuspeito;

    const novoSuspeito = this.adicionarNaTabela(
      this.suspeitoSelecionado,
      caracteristicas,
      this.suspeitoIdentificado,
    );

    this.listaSuspeitos.push(novoSuspeito);
    if (this.listaSuspeitos.length > 0) {
      this.linhaTabelaSuspeitos = true;
    }
    this.suspeitoIdentificado = null;
  }


  /**
   * Método responsável por montar um objeto simplificado para inserção
   * na tabela de exibição de vítimas ou suspeitos
   *
   * @param pessoaSelecionada objeto contendo os dados da vítima ou do suspeito
   * @param caracteristicas variável que contém as características inseridas do textarea do formulário
   * @param identificado variável que indica se a vítima ou suspeito é ou não identificado
   *
   * @return vazio
   */

  adicionarNaTabela(pessoaSelecionada: Pessoa,
                    caracteristicas: string,
                    identificado: boolean): any {

    const pessoa = identificado ? pessoaSelecionada : {
      nome: 'Não identificado',
      nomeMae: '-',
      dataNascimento: '-',
      telefones: '-',
      email: '-',
      cpf: '-',
    };

    let dados: any;

    if (identificado) {

      dados = {
        pessoa,
        caracteristicas,
        identificada: 'Sim'
      };


    } else {
      dados = {
        pessoa,
        caracteristicas,
        identificada: 'Não'
      };
    }

    return dados;
  }

  /**
   * Método responsável por remover uma pessoa da lista de vítimas ou suspeitos montada durante o preenchimento do
   * formulário de cadastro de um novo boletim de ocorrência
   *
   * @param object Objeto genérico contendo uma vítima ou suspeito
   * @param lista  objeto contendo a lista de vítimas ou suspeitos montada
   * @param identificador string que define se a lista é de pessoas ou vítimas
   *
   * @return vazio
   */
  removerPessoa(object: any,
                lista: { pessoa: Pessoa, caracteristicas: string, identificada: string }[],
                identificador: string): void {

    const idx = lista.indexOf(object);
    lista.splice(idx, 1);

    if (lista.length === 0) {
      if (identificador === 'vitima') {

        this.incluirNovaVitima();
        this.linhaTabelaVitimas = false;

      } else {
        this.incluirNovoSuspeito();
        this.linhaTabelaSuspeitos = false;
      }
    }
  }

  /**
   *  Método responsável por montar o objeto Boletim de ocorrência e
   *  realizar o seu registro utilizando a classe {@link BoletimOcorrenciaService}
   *
   *  @return vazio
   */
  enviarDados(): void {

    if (this.form.valid) {
      const vitimas: VitimaSave[] = [];

      const suspeitos: SuspeitoSave[] = [];

      for (const item of this.listaVitimas) {

        const vitima: VitimaSave = {caracteristicas: item.caracteristicas};

        if (item.identificada === 'Sim') {
          vitima.idPessoa = item.pessoa.id;
        }

        vitimas.push(vitima);
      }

      for (const item of this.listaSuspeitos) {

        const suspeito: SuspeitoSave = {caracteristicas: item.caracteristicas};

        if (item.identificada === 'Sim') {
          suspeito.idPessoa = item.pessoa.id;
        }

        suspeitos.push(suspeito);
      }

      const dataHoraFormatada = (moment(this.form.value.dataBo).format('L')) + ' ' + moment().format('LTS');
      const boletim: BoletimOcorrenciaSave = {
        dataHora: dataHoraFormatada, // 01/08/2020 05:45:00
        descricao: this.form.value.descricao,
        cep: this.form.value.cep,
        rua: this.form.value.rua,
        numero: this.form.value.numero,
        complemento: this.form.value.complemento,
        idBairro: this.form.value.bairro,
        idAdministrativo: 1,     // TODO: alterar para administrativo que realizou criou o boletim (usuário logado)
        vitimas,
        suspeitos,
        compensacaoTarefa: this.form.value.compensacao,
        evidencia: this.form.value.evidencia
      };

      console.log(boletim);

      this.boletimService.criar(boletim).pipe(take(1)).subscribe((boCriado: BoletimOcorrencia) => {
        this.location.back();
      });
    }

  }


  get listaCidadeConsulta(): Cidade[] {
    return this._listaCidadeConsulta;
  }

  get listaBairroConsulta(): Bairro[] {
    return this._listaBairroConsulta;
  }

  get cidadeSelecionada(): Cidade {
    return this._cidadeSelecionada;
  }

  get bairroSelecionado(): Bairro {
    return this._bairroSelecionado;
  }

  set cidadeSelecionada(value: Cidade) {
    this._cidadeSelecionada = value;
  }

  set bairroSelecionado(value: Bairro) {
    this._bairroSelecionado = value;
  }

  selectBairroOnChange($event: any) {
    const id = $event.target.value;
    this.bairroSelecionado = this.listaBairroConsulta.filter(bairro => bairro.id === id)[0];
  }

  async selectCidadeOnChange($event: any): Promise<void> {
    const id = $event.target.value;
    this.cidadeSelecionada = this.listaCidadeConsulta.filter(cidade => cidade.id === id)[0];
    this.form.controls.bairro.setValue('');
    this._listaBairroConsulta = await this.cidadeServico.listarTodosBairros(id);
  }
}
