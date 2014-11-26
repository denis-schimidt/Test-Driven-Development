package br.com.caelum.leilao.servico;

import java.util.Calendar;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.dao.RepositorioLeilao;
import br.com.caelum.leilao.infra.data.Relogio;
import br.com.caelum.leilao.infra.data.RelogioSistema;

public class GeradorDePagamento {

	private Avaliador avaliador;
	private RepositorioDePagamentos repositorioDePagamentos;
	private RepositorioLeilao repositorioLeilao;
	private Relogio relogio;

	public GeradorDePagamento(Avaliador avaliador, RepositorioLeilao repositorioLeilao, RepositorioDePagamentos repositorioDePagamentos, Relogio relogio) {
		this.avaliador = avaliador;
		this.repositorioDePagamentos = repositorioDePagamentos;
		this.repositorioLeilao = repositorioLeilao;
		this.relogio = relogio;
	}
	
	public GeradorDePagamento( Avaliador avaliador, RepositorioLeilao repositorioLeilao, RepositorioDePagamentos repositorioDePagamentos ){
		this(avaliador, repositorioLeilao, repositorioDePagamentos,  new RelogioSistema() );
	}
	
	public void gerar(){
		
		for( Leilao leilao : repositorioLeilao.encerrados() ) {
			avaliador.avalia(leilao);
			Pagamento pagamento = new Pagamento( avaliador.getMaiorLance(), proximoDiaUtil() );
			
			repositorioDePagamentos.salvar(pagamento);
		}
	}
	
	private Calendar proximoDiaUtil(){
		Calendar hoje = relogio.hoje();
		
		switch( hoje.get( Calendar.DAY_OF_WEEK ) ){
		
			case Calendar.SATURDAY:
				hoje.add( Calendar.DAY_OF_MONTH, 2);
				break;
				
			case Calendar.SUNDAY:
				hoje.add( Calendar.DAY_OF_MONTH, 1);
		}
		
		return hoje;
	}
}
