export type TipoContagem = 'DIAS_UTEIS' | 'DIAS_CORRIDOS';
export type Justica = 'ESTADUAL' | 'TRABALHISTA' | 'FEDERAL';

export interface CalculoRequest {
  dataIntimacao: string;
  prazoEmDias: number;
  tipoContagem: TipoContagem;
  justica: Justica;
  considerarRecesso: boolean;
}

export interface PassoContagem {
  data: string;
  contado: boolean;
  motivo: string;
  numero: number;
}

export interface ResultadoPrazo {
  dataIntimacao: string;
  dataInicioContagem: string;
  dataVencimento: string;
  prazoEmDias: number;
  tipoContagem: TipoContagem;
  justica: Justica;
  diasCorridosTotais: number;
  passos: PassoContagem[];
  fundamentacao: string[];
  avisos: string[];
}
