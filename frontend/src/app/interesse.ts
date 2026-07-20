export interface InteresseRequest {
  nome: string;
  email: string;
  interesse: string;
  mensagem?: string;
}

export interface InteresseResponse {
  sucesso: boolean;
  mensagem: string;
}
