package br.com.caelum.leilao.servico;

import java.util.Calendar;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.dao.RepositorioLeilao;

public class GeradorDePagamento {

	private Avaliador avaliador;
	private RepositorioDePagamentos repositorioDePagamentos;
	private RepositorioLeilao repositorioLeilao;

	public GeradorDePagamento( Avaliador avaliador, RepositorioLeilao repositorioLeilao, RepositorioDePagamentos repositorioDePagamentos ){
		this.avaliador = avaliador;
		this.repositorioLeilao = repositorioLeilao;
		this.repositorioDePagamentos = repositorioDePagamentos;
	}
	
	public void gerar(){
		
		for( Leilao leilao : repositorioLeilao.encerrados() ) {
			avaliador.avalia(leilao);
			Pagamento pagamento = new Pagamento( avaliador.getMaiorLance(), Calendar.getInstance() );
			
			repositorioDePagamentos.salvar(pagamento);
		}
	}
}
