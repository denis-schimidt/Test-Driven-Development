package br.com.caelum.leilao.servico;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.dao.RepositorioLeilao;

@RunWith( MockitoJUnitRunner.class )
public class GeradorDePagamentoTest {
	
	@Mock
	private RepositorioLeilao repositorioLeilao;
	
	@Mock
	private RepositorioDePagamentos repositorioDePagamentos;
	
	private Avaliador avaliador;
	
	@Before
	public void setUp(){
		avaliador = new Avaliador();
	}
	
	@Test
	public void valorPagamentoGeradoIgualAoMaiorLanceDado(){
		Leilao leilao = new CriadorDeLeilao().para( "Apartamento" )
				.lance( new Usuario("Fulano"), 300000)
				.lance( new Usuario( "Ciclano"), 350000)
				.constroi();
		
		when( repositorioLeilao.encerrados() ).thenReturn( Arrays.asList( leilao ) );
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(avaliador, repositorioLeilao, repositorioDePagamentos);
		geradorDePagamento.gerar();
		
		ArgumentCaptor<Pagamento> argumentoRepositorioPagamentos = ArgumentCaptor.forClass( Pagamento.class );
		verify(repositorioDePagamentos).salvar( argumentoRepositorioPagamentos.capture() );
		
		assertThat( argumentoRepositorioPagamentos.getValue().getValor(), closeTo(350000, 0.000001 ) );
	}
}
