import { ResultadoPrazo } from '../prazo';

/** O que a IA extrai de uma publicação. Espelha o record do backend. */
export interface Classificacao {
  tipoAto: string;
  prazoEmDias: number;
  tipoContagem: 'DIAS_UTEIS' | 'DIAS_CORRIDOS';
  providencia: string;
  tipoPecaSugerida: 'CONTESTACAO' | 'PETICAO_SIMPLES' | 'NENHUMA';
  urgencia: 'ALTA' | 'MEDIA' | 'BAIXA';
  /** 0 a 1. Abaixo de 0,7 a intimação vai para a fila de revisão. */
  confianca: number;
  fundamentacao: string;
}

export interface Processo {
  id: string;
  numero: string;
  cliente: string;
  parteContraria: string;
  vara: string;
  area: string;
  fase: string;
  resumo: string;
}

/** A linha da lista no "Seu dia". */
export interface LinhaIntimacao {
  id: string;
  orgao: string;
  dataPublicacao: string;
  numeroProcesso: string;
  cliente: string;
  situacao: 'NAO_LIDA' | 'PROCESSANDO' | 'PRAZO_NA_AGENDA' | 'RASCUNHO_PRONTO';
  /** Verdadeiro entre o disparo do fluxo de IA e a chegada do resultado. */
  processando: boolean;
  /** Preenchido quando o fluxo falhou — a tela mostra o motivo em vez de um erro genérico. */
  erroIa: string | null;
  temRascunho: boolean;
  /** O processo ainda não tem cliente definido: a IA fica bloqueada até o advogado dizer. */
  aguardaCliente: boolean;
  processoId: string | null;
  /** Link do PJe, quando a intimação veio do DJEN. Nulo nas semeadas. */
  link: string | null;
  origem: 'DEMO' | 'DJEN';
  tipoAto: string | null;
  providencia: string | null;
  urgencia: string | null;
  precisaRevisao: boolean;
  dataVencimento: string | null;
}

export interface Dashboard {
  /** Falso quando o fluxo de IA não está configurado — a tela avisa em vez de quebrar. */
  iaDisponivel: boolean;
  hoje: string;
  naoLidas: number;
  prazosNaAgenda: number;
  rascunhosProntos: number;
  aguardandoRevisao: number;
  /** Há casos de exemplo (origem DEMO) no acervo? Decide o rótulo do botão. */
  temExemplos: boolean;
  intimacoes: LinhaIntimacao[];
}

export interface DetalheIntimacao extends LinhaIntimacao {
  texto: string;
  processo: Processo;
  classificacao: Classificacao | null;
  rascunho: string | null;
  /** Nulo quando não há prazo. Vem do motor determinístico, com a memória de cálculo. */
  prazo: ResultadoPrazo | null;
}

/** Resposta do disparo: 202 e nada mais. O resultado chega depois, por polling. */
export interface RespostaProcessamento {
  situacao: string;
  aviso?: string;
}

/** Processo do DJEN em que o advogado ainda não disse de que lado está. */
export interface ProcessoPendente {
  id: string;
  numero: string;
  vara: string | null;
  tribunal: string | null;
  classe: string | null;
  /** As partes de cada polo, para o advogado escolher qual é o cliente. */
  partesPoloAtivo: string | null;
  partesPoloPassivo: string | null;
}
