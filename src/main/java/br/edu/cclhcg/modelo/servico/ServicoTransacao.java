package br.edu.cclhcg.modelo.servico;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import br.edu.cclhcg.modelo.entidade.Cartao;
import br.edu.cclhcg.modelo.entidade.Transacao;

/**
 * Classe para operar percistencia
 * 
 * @author LucasCardoso
 *
 */
@Stateless
public class ServicoTransacao {
	
	@PersistenceContext
	private EntityManager em;
	
	/** Cadastrar recebe por parametro os valores do bean preenchidos para transa��o **/
	public Transacao cadastrar(String numero, String cvv, String tipo_transacao, Date data, Double valor_transacao, String credor) throws Exception {
		
		/** Instanciando uma nova Transa��o e realizando a busca pelo cart�o de credito recebido. **/
		Transacao transacao = new Transacao();
		Cartao cartaoExistente = buscaCartao(numero, cvv);
		
		/**  Caso n�o encontre o cart�o na consulta pelo N�mero e CVV **/
		if(cartaoExistente == null ) {
			throw new Exception("N�mero do cart�o ou CVV incorretos!");
		}
		
		/** Setando na m�o os valores soltos no objeto transa��o **/
		transacao.setCartao(cartaoExistente);
		transacao.setTipo_transacao(tipo_transacao);
		transacao.setValor_transacao(valor_transacao);
		transacao.setData(data);
		transacao.setCredor(credor);
		transacao.setData(dtCriacao());
		
		Boolean permissao = verificarSaldo(cartaoExistente, valor_transacao);
		
		
		/** If para validar se tem saldo para debitar e qual o tipo da transa��o **/
		if (tipo_transacao.equals("debito") && permissao == true) {
			Double novoSaldo = cartaoExistente.getLimite() - valor_transacao;
			cartaoExistente.setLimite(novoSaldo);
		} else if (tipo_transacao.equals("credito")) {
			Double novoSaldo = valor_transacao + cartaoExistente.getLimite();
			cartaoExistente.setLimite(novoSaldo);
		} else {
			throw new Exception("Limite menor que o valor!");
		}
		
		em.persist(transacao);
		return transacao;
	}
	
	/**  **/
	public List<Transacao> listar() {
		return em.createNamedQuery("Transacao.ListarTodos", Transacao.class).getResultList();
	}
	
	/** Busca cart�o no banco validando **/
	public Cartao buscaCartao(String numero, String cvv) {
		
		Query query = em.createQuery("FROM Cartao ct WHERE ct.numero = :p1 AND ct.cvv = :p2");
		query.setParameter("p1", numero);
		query.setParameter("p2", cvv);
		
		try {
			return (Cartao) query.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	/** Verifica se o saldo do cart�o � positivo para a transa��o de debito **/
	public Boolean verificarSaldo(Cartao cartao, Double valor) {
		Double saldo = cartao.getLimite();	
		
		if (saldo >= valor) {
			return true;
		}
		
		return false;
	}
	
	/** Gera a data e hora atual **/
	public Date dtCriacao() {
		Date date = new Date();
		
//		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//		return formatter.format(date);
		
		return date;
	}
	
	/**  **/
	public List<Transacao> buscarTransacoes(String numCartao)  {
		TypedQuery<Transacao> query = em.createQuery("SELECT t FROM Transacao t WHERE t.cartao.numero =:p1", Transacao.class).setParameter("p1", numCartao);
		
		return query.getResultList();
	}
}
