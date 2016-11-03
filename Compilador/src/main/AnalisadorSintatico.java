package main;

import static main.AnalisadorLexical.lexico;
import static main.AnalisadorSemantico.*;

public class AnalisadorSintatico {

	public static void main(String[] args) {
		try {
			AnalisadorLexical.main(null);
			Token token = lexico();

			if (token.getSimbolo().equals("sprograma")) {
				token = lexico();
				if (token.getSimbolo().equals("sidentificador")) {
					// TODO: insereTabelaSimbolos(token.getLexema());
					token = lexico();
					if (token.getSimbolo().equals("sponto_virgula")) {
						token = analisaBloco(token);
						if (token.getSimbolo().equals("sponto")) {
							System.out.println("Arquivo lido com sucesso!");
						} else {
							throw new Exception("Erro no m�todo analisadorSintatico(). Na linha " + token.getLinha()
									+ " est� faltando um ponto final ap�s a palavra 'fim'. \n Token lido: "
									+ token.getLexema());
						}
					} else {
						throw new Exception("Erro no m�todo analisadorSintatico(). Na linha " + token.getLinha()
								+ " est� faltando um ponto e virgula no final. \n Token lido: " + token.getLexema());
					}
				} else {
					throw new Exception("Erro no m�todo analisadorSintatico(). Na linha " + token.getLinha()
							+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
				}
			} else {
				throw new Exception("Erro no m�todo analisadorSintatico(). Na linha " + token.getLinha()
						+ " est� faltando a palavra 'programa'. \n Token lido: " + token.getLexema());
			}
		} catch (Exception e) {
			if (e.getMessage().equals("Chegou ao fim do arquivo. N�o h� mais tokens.")) {
				System.err.println("Erro no m�todo analisadorSintatico(). Est� faltando a palavra 'fim'.");
			} else {
				System.err.println(e);
			}
		}

	}

	private static Token analisaBloco(Token token) throws Exception {
		token = lexico();
		token = analisaEtapaVariaveis(token);
		token = analisaSubrotinas(token);
		token = analisaComandos(token);
		return token;
	}

	private static Token analisaEtapaVariaveis(Token token) throws Exception {
		if (token.getSimbolo().equals("svar")) {
			token = lexico();
			if (token.getSimbolo().equals("sidentificador")) {
				do {
					token = analisaVariaveis(token);
					if (token.getSimbolo().equals("sponto_virgula")) {
						token = lexico();
					} else {
						throw new Exception("Erro no m�todo analisaEtapaVariaveis(). Na linha " + token.getLinha()
								+ " est� faltando um ponto e virgula ap�s o �ltimo identificador. \n Token lido: "
								+ token.getLexema());
					}
				} while (token.getSimbolo().equals("sidentificador"));
			} else {
				throw new Exception("Erro no m�todo analisaEtapaVariaveis(). Na linha " + token.getLinha()
						+ "est� faltando um identificador ap�s a palavra 'var'.  \n Token lido: " + token.getLexema());
			}
		}
		return token;
	}

	private static Token analisaVariaveis(Token token) throws Exception {
		do {
			if (token.getSimbolo().equals("sidentificador")) {
				//TODO: if(naoExisteVariavelNaSimboloTabela) { 
				// TODO: insereTabelaSimbolos(token.getLexema()); 
				token = lexico();
				if (token.getSimbolo().equals("svirgula") || token.getSimbolo().equals("sdoispontos")) {
					if (token.getSimbolo().equals("svirgula")) {
						token = lexico();
						if (token.getSimbolo().equals("sdoispontos")) {
							throw new Exception("Erro no m�todo analisaVariaveis(). Na linha " + token.getLinha()
									+ " n�o � permitido ter dois pontos ap�s uma v�rgula. \n Token lido: "
									+ token.getLexema());
						}
					}
				} else {
					throw new Exception("Erro no m�todo analisaVariaveis(). Na linha " + token.getLinha()
							+ " est� faltando uma virgula ou dois pontos. \n Token lido: " + token.getLexema());
				}
				// else erro j� existe na tab de simbolos
			} else {
				throw new Exception("Erro no m�todo analisaVariaveis(). Na linha " + token.getLinha()
						+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
			}
		} while (!token.getSimbolo().equals("sdoispontos"));
		return analisaTipo(lexico());
	}

	private static Token analisaTipo(Token token) throws Exception {
		if (token.getSimbolo().equals("sinteiro") && token.getSimbolo().equals("sbooleano")) {
			throw new Exception("Erro no m�todo analisaTipo(). Na linha " + token.getLinha()
					+ " � permitido apenas tipo inteiro ou booleano. \n Token lido: " + token.getLexema());
		}
		// TODO: colocaTipoVariaveis(token.getSimbolo())
		return lexico();
	}

	private static Token analisaComandos(Token token) throws Exception {
		if (token.getSimbolo().equals("sinicio")) {
			token = lexico();
			token = analisaComandoSimples(token);
			while (!token.getSimbolo().equals("sfim")) {
				if (token.getSimbolo().equals("sponto_virgula")) {
					token = lexico();
					if (!token.getSimbolo().equals("sfim")) {
						token = analisaComandoSimples(token);
					}
				} else {
					throw new Exception("Erro no m�todo analisaComandos(). Na linha " + token.getLinha()
							+ " est� faltando ponto e virgula. \n Token lido: " + token.getLexema());
				}
			}
			return lexico();
		} else {
			throw new Exception("Erro no m�todo analisaComandos(). Na linha " + token.getLinha()
					+ " est� faltando a palavra 'inicio'. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaComandoSimples(Token token) throws Exception {
		if (token.getSimbolo().equals("sidentificador")) {
			return analisaAtribuicaoChamadaProcedimento(token);
		} else if (token.getSimbolo().equals("sse")) {
			return analisaSe(token);
		} else if (token.getSimbolo().equals("senquanto")) {
			return analisaEnquanto(token);
		} else if (token.getSimbolo().equals("sleia")) {
			return analisaLeia(token);
		} else if (token.getSimbolo().equals("sescreva")) {
			return analisaEscreva(token);
		} else {
			return analisaComandos(token);
		}
	}

	private static Token analisaAtribuicaoChamadaProcedimento(Token token) throws Exception {
		token = lexico();
		if (token.getSimbolo().equals("satribuicao")) {
			return analisaAtribuicao(token);
		} else {
			return analisaChamadaProcedimento(token);
		}
	}

	private static Token analisaAtribuicao(Token token) throws Exception {
		token = lexico();
		token = analisaExpressao(token);
		return token;
	}

	private static Token analisaLeia(Token token) throws Exception {
		token = lexico();
		if (token.getSimbolo().equals("sabre_parenteses")) {
			token = lexico();
			if (token.getSimbolo().equals("sidentificador")) {
				//TODO: if (existeVariavelNaTabelaSimbolos(token.getLexam()))
				token = lexico();
				
				if (token.getSimbolo().equals("sfecha_parenteses")) {
					token = lexico();
					return token;
				} else {
					throw new Exception("Erro no m�todo analisaLeia(). Na linha " + token.getLinha()
							+ " est� faltando um fecha parenteses ap�s identificador. \n Token lido: "
							+ token.getLexema());
				}
				
				// erro 
				
			} else {
				throw new Exception("Erro no m�todo analisaLeia(). Na linha " + token.getLinha()
						+ " est� faltando um identificador ap�s abertura dos parenteses. \n Token lido: "
						+ token.getLexema());
			}
		} else {
			throw new Exception("Erro no m�todo analisaLeia(). Na linha " + token.getLinha()
					+ " est� faltando abre parenteses ap�s a palavra leia. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaEscreva(Token token) throws Exception {
		token = lexico();
		if (token.getSimbolo().equals("sabre_parenteses")) {
			token = lexico();
			if (token.getSimbolo().equals("sidentificador")) {
				//TODO: if (existeChamadaFuncaoNaTabelaSimbolos(token.getLexam()))
				token = lexico();
				if (token.getSimbolo().equals("sfecha_parenteses")) {
					token = lexico();
					return token;
				} else {
					throw new Exception("Erro no m�todo analisaEscreva(). Na linha " + token.getLinha()
							+ " est� faltando um fecha parenteses ap�s identificador. \n Token lido: "
							+ token.getLexema());
				}
				// erro
				
			} else {
				throw new Exception("Erro no m�todo analisaEscreva(). Na linha " + token.getLinha()
						+ " est� faltando um identificador ap�s abertura dos parenteses. \n Token lido: "
						+ token.getLexema());
			}
		} else {
			throw new Exception("Erro no m�todo analisaEscreva(). Na linha " + token.getLinha()
					+ " est� faltando abre parenteses ap�s a palavra escreve. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaEnquanto(Token token) throws Exception {
		token = lexico();
		token = analisaExpressao(token);
		if (token.getSimbolo().equals("sfaca")) {
			token = lexico();
			token = analisaComandoSimples(token);
			return token;
		} else {
			throw new Exception("Erro no m�todo analisaEnquanto(). Na linha " + token.getLinha()
					+ " est� faltando a palavra 'faca' ou condi��o invalida. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaSe(Token token) throws Exception {
		token = lexico();
		token = analisaExpressao(token);
		if (token.getSimbolo().equals("sentao")) {
			token = lexico();
			token = analisaComandoSimples(token);
			if (token.getSimbolo().equals("ssenao")) {
				token = lexico();
				token = analisaComandoSimples(token);
			}
			return token;
		} else {
			throw new Exception("Erro no m�todo analisaSe(). Na linha " + token.getLinha()
					+ " est� faltando a palavra 'entao'. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaSubrotinas(Token token) throws Exception {
		if (token.getSimbolo().equals("sprocedimento") || token.getSimbolo().equals("sfuncao")) {

		}
		while (token.getSimbolo().equals("sprocedimento") || token.getSimbolo().equals("sfuncao")) {
			if (token.getSimbolo().equals("sprocedimento")) {
				token = analisaDeclaracaoProcedimento(token);
			} else {
				token = analisaDeclaracaoFuncao(token);
			}
			if (token.getSimbolo().equals("sponto_virgula")) {
				token = lexico();
			} else {
				throw new Exception("Erro no m�todo analisaSubrotinas(). Na linha " + token.getLinha()
						+ " est� faltando um ponto e virgula ap�s declara��o de subrotinas. \n Token lido: "
						+ token.getLexema());
			}
		}
		return token;
	}

	private static Token analisaDeclaracaoProcedimento(Token token) throws Exception {
		token = lexico();
		// n�vel := �L� (marca ou novo galho) 
		if (token.getSimbolo().equals("sidentificador")) {
			// TODO: if naoExisteDeclaracaoProcedimentoNatabelaDeSimbolos(token.lexema)
			 // insereTabelaSimbolos(token.lexema,�procedimento�,n�vel)
			token = lexico();
			if (token.getSimbolo().equals("sponto_virgula")) {
				return analisaBloco(token);
			} else {
				throw new Exception("Erro no m�todo analisaDeclaracaoProcedimento(). Na linha" + token.getLinha()
						+ " est� faltando um ponto e virgula. \n Token lido: " + token.getLexema());
			}
			// erro: 
		} else {
			throw new Exception("Erro no m�todo analisaDeclaracaoProcedimento(). Na linha" + token.getLinha()
					+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
		}
		// desempilha
	}

	private static Token analisaDeclaracaoFuncao(Token token) throws Exception {
		token = lexico();
		// TODO: n�vel := �L� (marca ou novo galho)
		if (token.getSimbolo().equals("sidentificador")) {
			// if naoExisteDeclaracaoFuncaoNaTabelaSimbolos
			// insereTabela
			token = lexico();
			if (token.getSimbolo().equals("sdoispontos")) {
				token = lexico();
				if ((token.getSimbolo().equals("sinteiro") || token.getSimbolo().equals("sbooleano"))) {
					/*
					se (token.s�mbolo = Sinteger)
					 ent�o TABSIMB[pc].tipo:=
					 �fun��o inteiro�
					 sen�o TABSIMB[pc].tipo:=
					 �fun��o boolean� */
					token = lexico();
					if (token.getSimbolo().equals("sponto_virgula")) {
						return analisaBloco(token);
					}
				} else {
					throw new Exception("Erro no m�todo analisaDeclaracaoFuncao(). Na linha" + token.getLinha()
							+ " o tipo de retorno � inv�lido, � permitido apenas inteirou ou booleano. \n Token lido: "
							+ token.getLexema());
				}
			} else {
				throw new Exception("Erro no m�todo analisaDeclaracaoFuncao(). Na linha" + token.getLinha()
						+ " est� faltando dois pontos ap�s o identificador. \n Token lido: " + token.getLexema());
			}
			//erro
		} else {
			throw new Exception("Erro no m�todo analisaDeclaracaoFuncao(). Na linha" + token.getLinha()
					+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
		}
		// desempilha
		return token;
	}

	private static Token analisaExpressao(Token token) throws Exception {
		token = analisaExpressaoSimples(token);
		if (token.getSimbolo().equals("smaior") || token.getSimbolo().equals("smaiorig")
				|| token.getSimbolo().equals("sig") || token.getSimbolo().equals("smenor")
				|| token.getSimbolo().equals("smenorig") || token.getSimbolo().equals("sdif")) {
			token = lexico();
			token = analisaExpressaoSimples(token);
		}
		return token;
	}

	private static Token analisaExpressaoSimples(Token token) throws Exception {
		if (token.getSimbolo().equals("smais") || token.getSimbolo().equals("smenos")) {
			token = lexico();
		}
		token = analisaTermo(token);
		while (token.getSimbolo().equals("smais") || token.getSimbolo().equals("smenos")
				|| token.getSimbolo().equals("sou")) {
			token = lexico();
			token = analisaTermo(token);
		}
		return token;
	}

	private static Token analisaTermo(Token token) throws Exception {
		token = analisaFator(token);
		while (token.getSimbolo().equals("smult") || token.getSimbolo().equals("sdiv")
				|| token.getSimbolo().equals("se")) {
			token = lexico();
			token = analisaFator(token);
		}
		return token;
	}

	private static Token analisaFator(Token token) throws Exception {
		if (token.getSimbolo().equals("sidentificador")) {
			/*
			 * Se pesquisa_tabela(token.lexema,n�vel,ind)
 				Ent�o Se (TabSimb[ind].tipo = �fun��o inteiro�) ou
				 (TabSimb[ind].tipo = �fun��o booleano�)
				 Ent�o Analisa_chamada_fun��o
				 Sen�o L�xico(token)
				 Sen�o ERRO
			 */
			token = analisaChamadaFuncao(token);
			return token;
		} else if (token.getSimbolo().equals("snumero")) {
			return lexico();
		} else if (token.getSimbolo().equals("snao")) {
			token = lexico();
			return analisaFator(token);
		} else if (token.getSimbolo().equals("sabre_parenteses")) {
			token = lexico();
			token = analisaExpressao(token);
			if (token.getSimbolo().equals("sfecha_parenteses")) {
				return lexico();
			} else {
				throw new Exception("Erro no m�todo analisaFator(). Na linha " + token.getLinha()
						+ " est� faltando um fecha parenteses, ap�s express�o. \n Token lido: " + token.getLexema());
			}
		} else if (token.getLexema().equals("verdadeiro") || token.getLexema().equals("falso")) {
			return lexico();
		} else {
			throw new Exception("Erro no m�todo analisaFator(). Na linha " + token.getLinha()
					+ " o fator � inv�lido. Apenas s�o permitidos identificadores, numeros, express�es, palavra 'verdadeiro', 'falso' ou 'nao' mais fator. \n Token lido: "
					+ token.getLexema());
		}
	}

	private static Token analisaChamadaProcedimento(Token token) {
		return token;
	}

	private static Token analisaChamadaFuncao(Token token) throws Exception {
		return lexico();
	}

	/* Metodos para teste */
	private static void exibeToken(Token token) {
		System.out.println(
				"Linha: " + token.getLinha() + " Lexema: " + token.getLexema() + " Simbolo: " + token.getSimbolo());
	}

}