package main;

import static main.AnalisadorLexico.lexico;
import static main.AnalisadorSemantico.*;

public class AnalisadorSintatico {

	private static Integer nivel = 0;
	private static Token tokenAnteriorAtribuicao = null;
	private static Token tokenAnteriorExpressao = null;

	public static void analisadorSintatico() throws Exception {

		zeraVariaveis();
		nivel = 0;
		tokenAnteriorAtribuicao = null;
		tokenAnteriorExpressao = null;
		
		AnalisadorLexico.main(null);
		Token token = lexico();

		if (token.getSimbolo().equals("sprograma")) {
			token = lexico();
			if (token.getSimbolo().equals("sidentificador")) {
				insereTabelaSimbolos(token.getLexema(), null, nivel, null, "nomedeprograma");
				token = lexico();
				if (token.getSimbolo().equals("sponto_virgula")) {
					token = analisaBloco(token);
					if (token.getSimbolo().equals("sponto")) {
						throw new Exception("Arquivo compilado com sucesso!");
					} else {
						throw new Exception("Erro: Espera-se um ponto final ap�s a palavra 'fim'");
					}
				} else {
					throw new Exception("Erro na linha " + token.getLinha()
							+ " . Espera-se um ponto e virgula no final ap�s o identificador. \n Ultimo token lido: "
							+ token.getLexema());
				}
			} else {
				throw new Exception("Erro na linha " + token.getLinha()
						+ " . Espera-se identificador ap�s a palavra reservada 'programa'. \n Ultimo token lido: "
						+ token.getLexema());
			}
		} else {
			throw new Exception("Erro na linha " + token.getLinha()
					+ " . Espera-se que um programa come�e com a palavra 'programa'. \n Ultimo token lido: "
					+ token.getLexema());
		}

	}

	private static Token analisaBloco(Token token) throws Exception {
		token = lexico();
		token = analisaEtapaVariaveis(token);
		token = analisaSubrotinas(token);
		token = analisaComandos(token);

		// TODO : verificar nivel maior que zero
		desempilhaNivelTabela(nivel);
		nivel--;
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
						throw new Exception("Erro na linha " + token.getLinha()
								+ " . Espera-se um ponto e virgula ap�s o �ltimo identificador. \n Ultimo token lido: "
								+ token.getLexema());
					}
				} while (token.getSimbolo().equals("sidentificador"));
			} else {
				throw new Exception("Erro na linha " + token.getLinha()
						+ " . Espera-se um identificador ap�s a palavra 'var'. \n Ultimo token lido: "
						+ token.getLexema());
			}
		}
		return token;
	}

	private static Token analisaVariaveis(Token token) throws Exception {
		do {
			if (token.getSimbolo().equals("sidentificador")) {
				if (!pesquisaDuplicidadeVariavelTabela(token.getLexema(), nivel)) {
					insereTabelaSimbolos(token.getLexema(), null, nivel, null, "nomedevariavel");
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
				} else {
					throw new Exception("Erro na linha" + token.getLinha()
							+ ". J� existe uma variavel, fun��o ou procedimento com esse nome.");
				}
			} else {
				throw new Exception("Erro no m�todo analisaVariaveis(). Na linha " + token.getLinha()
						+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
			}
		} while (!token.getSimbolo().equals("sdoispontos"));
		return analisaTipo(lexico());
	}

	private static Token analisaTipo(Token token) throws Exception {
		if (!token.getSimbolo().equals("sinteiro") && !token.getSimbolo().equals("sbooleano")) {
			throw new Exception("Erro no m�todo analisaTipo(). Na linha " + token.getLinha()
					+ " � permitido apenas tipo inteiro ou booleano. \n Token lido: " + token.getLexema());
		}
		colocaTipoVariaveis(token.getSimbolo());
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
			tokenAnteriorAtribuicao = token;
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
			if (pesquisaDeclaracaoVariavelTabela(tokenAnteriorAtribuicao.getLexema())) {
				return analisaAtribuicao(token);
			} else {
				throw new Exception("Erro na linha " + token.getLinha() + ". A variavel "
						+ tokenAnteriorAtribuicao.getLexema() + " n�o foi declarada.");
			}
		} else {
			return analisaChamadaProcedimento(token);
		}
	}

	private static Token analisaAtribuicao(Token token) throws Exception {
		token = lexico();
		token = analisaExpressao(token);

		String tipoExpressao = analisaPosfixo();
		/*
		 * if
		 * (!tipoExpressao.equals(getSimbolo(getSimbolo(tokenAnteriorAtribuicao.
		 * getSimbolo()).getLexema()))) { throw new Exception("Erro na linha " +
		 * token.getLinha() +
		 * " . Incompatibilidade do tipo de retorno da expressao com o tipo da variavel "
		 * +tokenAnteriorAtribuicao.getLexema()); }
		 */

		tokenAnteriorExpressao = null;
		return token;
	}

	private static Token analisaLeia(Token token) throws Exception {
		token = lexico();
		if (token.getSimbolo().equals("sabre_parenteses")) {
			token = lexico();
			if (token.getSimbolo().equals("sidentificador")) {
				if (pesquisaDeclaracaoVariavelTabela(token.getLexema())) {
					token = lexico();
					if (token.getSimbolo().equals("sfecha_parenteses")) {
						token = lexico();
						return token;
					} else {
						throw new Exception("Erro no m�todo analisaLeia(). Na linha " + token.getLinha()
								+ " est� faltando um fecha parenteses ap�s identificador. \n Token lido: "
								+ token.getLexema());
					}
				} else {
					throw new Exception(
							"Erro na linha " + token.getLinha() + ". N�o foi encontrada a variavel para leitura.");
				}
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
				if (pesquisaDeclaracaoFuncaoVariavelTabela(token.getLexema())) {
					token = lexico();
					if (token.getSimbolo().equals("sfecha_parenteses")) {
						token = lexico();
						return token;
					} else {
						throw new Exception("Erro no m�todo analisaEscreva(). Na linha " + token.getLinha()
								+ " est� faltando um fecha parenteses ap�s identificador. \n Token lido: "
								+ token.getLexema());
					}
				} else {
					throw new Exception("Erro na linha " + token.getLinha()
							+ ". N�o foi encontrada a variavel ou funcao para escrita.");
				}
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

		verificaTipoBooleano(analisaPosfixo(), token);
		tokenAnteriorExpressao = null;

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

		verificaTipoBooleano(analisaPosfixo(), token);
		tokenAnteriorExpressao = null;

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
			// geracao de codigo
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
		if (token.getSimbolo().equals("sidentificador")) {
			if (!pesquisaDeclaracaoProcedimentoTabela(token.getLexema())) {
				nivel++;
				insereTabelaSimbolos(token.getLexema(), null, nivel, null, "nomedeprocedimento");

				token = lexico();
				if (token.getSimbolo().equals("sponto_virgula")) {
					return analisaBloco(token);
				} else {
					throw new Exception("Erro no m�todo analisaDeclaracaoProcedimento(). Na linha" + token.getLinha()
							+ " est� faltando um ponto e virgula. \n Token lido: " + token.getLexema());
				}
			} else {
				throw new Exception("Erro na linha " + token.getLinha() + ". J� existe um procedimento com esse nome");
			}
		} else {
			throw new Exception("Erro no m�todo analisaDeclaracaoProcedimento(). Na linha" + token.getLinha()
					+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
		}
	}

	private static Token analisaDeclaracaoFuncao(Token token) throws Exception {
		token = lexico();
		if (token.getSimbolo().equals("sidentificador")) {
			if (!pesquisaDeclaracaoFuncaoTabela(token.getLexema())) {
				nivel++;
				insereTabelaSimbolos(token.getLexema(), null, nivel, null, "nomedefuncao");

				token = lexico();
				if (token.getSimbolo().equals("sdoispontos")) {
					token = lexico();
					if ((token.getSimbolo().equals("sinteiro") || token.getSimbolo().equals("sbooleano"))) {
						if (token.getSimbolo().equals("sinteiro")) {
							colocaTipoRetornoFuncao("sinteiro");
						} else if (token.getSimbolo().equals("sbooleano")) {
							colocaTipoRetornoFuncao("sbooleano");
						}
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
			} else {
				throw new Exception("Erro na linha " + token.getLinha() + ". J� existe uma fun��o com esse nome");
			}
		} else {
			throw new Exception("Erro no m�todo analisaDeclaracaoFuncao(). Na linha" + token.getLinha()
					+ " est� faltando um identificador. \n Token lido: " + token.getLexema());
		}
		desempilhaNivelTabela(nivel);
		nivel--;
		return token;
	}

	private static Token analisaExpressao(Token token) throws Exception {
		token = analisaExpressaoSimples(token);
		if (token.getSimbolo().equals("smaior") || token.getSimbolo().equals("smaiorig")
				|| token.getSimbolo().equals("sig") || token.getSimbolo().equals("smenor")
				|| token.getSimbolo().equals("smenorig") || token.getSimbolo().equals("sdif")) {
			tokenAnteriorExpressao = token;
			token.setUnario(false);
			adicionaPilhaPosfixo(token);
			token = lexico();
			token = analisaExpressaoSimples(token);
		}
		return token;
	}

	private static Token analisaExpressaoSimples(Token token) throws Exception {
		if (token.getSimbolo().equals("smais") || token.getSimbolo().equals("smenos")) {
			token.setUnario(verificaUnario(tokenAnteriorExpressao));
			adicionaPilhaPosfixo(token);
			tokenAnteriorExpressao = token;
			token = lexico();
		}
		token = analisaTermo(token);
		while (token.getSimbolo().equals("smais") || token.getSimbolo().equals("smenos")
				|| token.getSimbolo().equals("sou")) {
			token.setUnario(verificaUnario(tokenAnteriorExpressao));
			adicionaPilhaPosfixo(token);
			tokenAnteriorExpressao = token;
			token = lexico();
			token = analisaTermo(token);
		}
		return token;
	}

	private static Token analisaTermo(Token token) throws Exception {
		token = analisaFator(token);
		while (token.getSimbolo().equals("smult") || token.getSimbolo().equals("sdiv")
				|| token.getSimbolo().equals("se")) {
			token.setUnario(false);
			adicionaPilhaPosfixo(token);
			tokenAnteriorExpressao = token;
			token = lexico();
			token = analisaFator(token);
		}
		return token;
	}

	private static Token analisaFator(Token token) throws Exception {
		if (token.getSimbolo().equals("sidentificador")) {
			if (pesquisa_tabela(token.getLexema())) {
				String tipo = getTipoFuncao(token.getLexema());
				if (tipo.equals("inteiro") || tipo.equals("boleano")) {
					token = analisaChamadaFuncao(tipo);
				}
			} else {
				adicionaFilaPosfixo(token);
				tokenAnteriorExpressao = token;
				token = lexico();
			}
			return token;
		} else if (token.getSimbolo().equals("snumero")) {
			adicionaFilaPosfixo(token);
			tokenAnteriorExpressao = token;
			return lexico();
		} else if (token.getSimbolo().equals("snao")) {
			token.setUnario(true);
			adicionaPilhaPosfixo(token);
			tokenAnteriorExpressao = token;
			token = lexico();
			return analisaFator(token);
		} else if (token.getSimbolo().equals("sabre_parenteses")) {
			token.setUnario(false);
			adicionaPilhaPosfixo(token);
			tokenAnteriorExpressao = token;
			token = lexico();
			token = analisaExpressao(token);
			if (token.getSimbolo().equals("sfecha_parenteses")) {
				desempilhaPilhaParenteses();
				tokenAnteriorExpressao = token;
				return lexico();
			} else {
				throw new Exception("Erro no m�todo analisaFator(). Na linha " + token.getLinha()
						+ " est� faltando um fecha parenteses, ap�s express�o. \n Token lido: " + token.getLexema());
			}
		} else if (token.getLexema().equals("verdadeiro") || token.getLexema().equals("falso")) {
			adicionaFilaPosfixo(token);
			tokenAnteriorExpressao = token;
			return lexico();
		} else {
			throw new Exception("Erro no m�todo analisaFator(). Na linha " + token.getLinha()
					+ " o fator � inv�lido. Apenas s�o permitidos identificadores, numeros, express�es, palavra 'verdadeiro', 'falso' ou 'nao' mais fator. \n Token lido: "
					+ token.getLexema());
		}
	}

	private static Token analisaChamadaProcedimento(Token token) throws Exception {
		if (!pesquisaDeclaracaoProcedimentoTabela(tokenAnteriorAtribuicao.getLexema())) {
			throw new Exception("Erro na linha " + tokenAnteriorAtribuicao.getLinha() + ". Procedimento "
					+ tokenAnteriorAtribuicao.getLexema() + " n�o declarado.");
		}
		return token;
	}

	private static Token analisaChamadaFuncao(String tipo) throws Exception {
		String tipoTokenAnterior = getTipoFuncao(tokenAnteriorAtribuicao.getLexema());
		if (tipo.equals(tipoTokenAnterior)) {
			return lexico();
		}
		throw new Exception(
				"Erro na linha " + tokenAnteriorAtribuicao.getLinha() + ". Tipos n�o compativeis, vari�vel de tipo "
						+ tipoTokenAnterior + " � diferente do tipo de retorno da fun��o (" + tipo + ").");
	}

}