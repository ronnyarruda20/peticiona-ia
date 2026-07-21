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
  situacao: 'NAO_LIDA' | 'PRAZO_NA_AGENDA' | 'RASCUNHO_PRONTO';
  temRascunho: boolean;
  tipoAto: string | null;
  providencia: string | null;
  urgencia: string | null;
  precisaRevisao: boolean;
  dataVencimento: string | null;
}

export interface Dashboard {
  /** Falso quando ANTHROPIC_API_KEY não está configurada — a tela avisa em vez de quebrar. */
  iaDisponivel: boolean;
  hoje: string;
  naoLidas: number;
  prazosNaAgenda: number;
  rascunhosProntos: number;
  aguardandoRevisao: number;
  intimacoes: LinhaIntimacao[];
}

export interface DetalheIntimacao extends LinhaIntimacao {
  texto: string;
  processo: Processo;
  classificacao: Classificacao | null;
  rascunho: string | null;
}

export interface RespostaClassificacao {
  classificacao: Classificacao;
  /** Ausente quando a publicação não abre prazo. Vem do motor determinístico. */
  prazo?: ResultadoPrazo;
  situacao: string;
}

export interface RespostaRascunho {
  rascunho: string;
  situacao: string;
}
