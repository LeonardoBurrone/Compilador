package main;

import java.util.ArrayList;
import java.util.List;
import static main.Operadores.*;

public class AnalisadorSemantico {

	private static final String NOME_DE_VARIAVEL = "nomedevariavel";
	private static final String NOME_DE_PROCEDIMENTO = "nomedeprocedimento";
	private static final String NOME_DE_FUNCAO = "nomedefuncao";

	private static List<Simbolo> tabelaSimbolos = new ArrayList<>();

	private static List<Token> pilhaPosfixo = new ArrayList<>();
	private static List<Token> filaPosfixo = new ArrayList<>();

	private static int getUltimaPosicaoLista() {
		return tabelaSimbolos.size() - 1;
	}

	public static List<Simbolo> getTabelaSimbolos() {
		return tabelaSimbolos;
	}

	public static void desempilhaPilhaParenteses() {
		int i = pilhaPosfixo.size() - 1;
		while (i >= 0) {
			if (!pilhaPosfixo.get(i).getLexema().equals("(")) {
				adicionaFilaPosfixo(pilhaPosfixo.remove(i));
			} else {
				pilhaPosfixo.remove(i);
				break;
			}
			i--;
		}
	}

	public static Simbolo getSimboloTopoTabela() {
		return tabelaSimbolos.get(getUltimaPosicaoLista());
	}

	public static void adicionaFilaPosfixo(Token token) {
		filaPosfixo.add(token);
	}

	public static void adicionaPilhaPosfixo(Token token, boolean isUnario) {
		int i = pilhaPosfixo.size() - 1;
		int predenciaParametro = isUnario ? getPrecedenciaOperadores("unario")
				: getPrecedenciaOperadores(token.getLexema());
		int predenciaPilha;
		if ("(".equals(token.getLexema())) {
			pilhaPosfixo.add(token);
		} else {
			while (i >= 0) {
				predenciaPilha = getPrecedenciaOperadores(pilhaPosfixo.get(i).getLexema());
				if (predenciaPilha >= predenciaParametro) {
					adicionaFilaPosfixo(pilhaPosfixo.remove(i));
				} else {
					break;
				}
				i--;
			}
			pilhaPosfixo.add(token);
		}
	}

	public static Simbolo getSimbolo(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& NOME_DE_VARIAVEL.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return tabelaSimbolos.get(i);
			}
			i--;
		}
		return null;
	}

	public static void insereTabelaSimbolos(String lexema, String tipo, Integer nivel, String rotulo,
			String tipoLexema) {
		Simbolo simbolo = new Simbolo();
		simbolo.setLexema(lexema);
		simbolo.setTipo(tipo);
		simbolo.setNivel(nivel);
		simbolo.setRotulo(rotulo);
		simbolo.setTipoLexema(tipoLexema);
		tabelaSimbolos.add(simbolo);
	}

	public static void colocaTipoVariaveis(String tipo) {
		String tipoVariavel = tipo.substring(1);
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema())
					|| NOME_DE_PROCEDIMENTO.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				break;
			}
			else if (tabelaSimbolos.get(i).getTipo() == null 
					&& NOME_DE_VARIAVEL.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				tabelaSimbolos.get(i).setTipo(tipoVariavel);
			}
			i--;
		}
	}

	public static boolean pesquisaDeclaracaoVariavelTabela(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& NOME_DE_VARIAVEL.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return true;
			}
			i--;
		}
		return false;
	}

	public static boolean pesquisaDuplicidadeVariavelTabela(String lexema, Integer nivel) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema) && nivel.equals(tabelaSimbolos.get(i).getNivel())) {
				return true;
			}
			i--;
		}
		return verificaVariavelEqualsNomeProcedimentoFuncao(lexema);
	}

	private static boolean verificaVariavelEqualsNomeProcedimentoFuncao(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& (NOME_DE_PROCEDIMENTO.equals(tabelaSimbolos.get(i).getTipoLexema())
							|| NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema()))) {
				return true;
			}
			i--;
		}
		return false;
	}

	public static boolean pesquisaDeclaracaoFuncaoVariavelTabela(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& NOME_DE_VARIAVEL.equals(tabelaSimbolos.get(i).getTipoLexema())
					|| NOME_DE_FUNCAO.equalsIgnoreCase(tabelaSimbolos.get(i).getTipoLexema())) {
				return true;
			}
			i--;
		}
		return false;
	}

	public static boolean pesquisaDeclaracaoProcedimentoTabela(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& NOME_DE_PROCEDIMENTO.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return true;
			}
			i--;
		}
		return false;
	}

	public static boolean pesquisaDeclaracaoFuncaoTabela(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema)
					&& NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return true;
			}
			i--;
		}
		return false;
	}

	public static void desempilhaNivelTabela(Integer nivel) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getNivel().equals(nivel)
					&& (NOME_DE_PROCEDIMENTO.equals(tabelaSimbolos.get(i).getTipoLexema())
							|| NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema()))) {
				tabelaSimbolos.remove(i);
			} else {
				break;
			}
			i--;
		}
	}

	public static void colocaTipoRetornoFuncao(String tipo) {
		String tipoVariavel = tipo.substring(1);
		tabelaSimbolos.get(getUltimaPosicaoLista()).setTipo(tipoVariavel);
	}

	public static boolean pesquisa_tabela(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema) 
					&& NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return true;
			}
			i--;
		}
		return false;
	}
	
	public static String getTipoFuncao(String lexema) {
		int i = getUltimaPosicaoLista();
		while(i >= 0) {
			if (tabelaSimbolos.get(i).getLexema().equals(lexema) 
					&& NOME_DE_FUNCAO.equals(tabelaSimbolos.get(i).getTipoLexema())) {
				return tabelaSimbolos.get(i).getTipo();
			}
			i--;
		}
		return null;
	}

	/*
	 * TODO: Retorna o tipo da expressao
	 */
	public static String analisaPosfixo() {
		if(!pilhaPosfixo.isEmpty()) {
			int i = pilhaPosfixo.size() - 1;
			while (i >= 0) {
				adicionaFilaPosfixo(pilhaPosfixo.remove(i));
				i--;
			}
		}
		
		for (Token token : filaPosfixo) {
			System.out.print(token.getLexema());
		}
		System.out.print("\n");
		
		filaPosfixo.removeAll(filaPosfixo);
		
		return null;
	}

	public static void verificaTipoBooleano(String tipo, Token token) throws Exception {
		/*if (!"booleano".equals(tipo)) {
			throw new Exception("Erro na linha " + token.getLinha()
					+ " . Espera-se que o tipo de retorno da expressao seja booleano.");
		}*/
	}

	public static boolean verificaUnario(Token tokenAnteriorExpressao) {
		if (tokenAnteriorExpressao == null) {
			return true;
		} else if ("(".equals(tokenAnteriorExpressao.getSimbolo())) {
			return true;
		} 
		return false;
	}

}
