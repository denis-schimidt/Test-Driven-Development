package br.com.caelum.leilao.servico;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;

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
import br.com.caelum.leilao.infra.data.Relogio;

@RunWith( MockitoJUnitRunner.class )
public class GeradorDePagamentoTest {
	
	@Mock
	private RepositorioLeilao repositorioLeilao;
	
	@Mock
	private RepositorioDePagamentos repositorioDePagamentos;
	
	@Mock
	private Relogio relogio;
	
	private Avaliador avaliador;
	
	@Before
	public void setUp(){
		avaliador = new Avaliador();
	}
	
	@Test
	public void valorPagamentoGeradoIgualAoMaiorLanceDado(){
		Leilao leilao = criarLeilao();
		
		when( repositorioLeilao.encerrados() ).thenReturn( Arrays.asList( leilao ) );
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(avaliador, repositorioLeilao, repositorioDePagamentos);
		geradorDePagamento.gerar();
		
		ArgumentCaptor<Pagamento> argumentoRepositorioPagamentos = ArgumentCaptor.forClass( Pagamento.class );
		verify(repositorioDePagamentos).salvar( argumentoRepositorioPagamentos.capture() );
		
		assertThat( argumentoRepositorioPagamentos.getValue().getValor(), closeTo(350000, 0.000001 ) );
	}

	private Leilao criarLeilao() {
		Leilao leilao = new CriadorDeLeilao().para( "Apartamento" )
				.lance( new Usuario("Fulano"), 300000)
				.lance( new Usuario( "Ciclano"), 350000)
				.constroi();
		return leilao;
	}
	
	@Test
	public void gerarPagamentoNoSabadoQueCairaNaSegunda(){
		Leilao leilao = criarLeilao();
		
		when( repositorioLeilao.encerrados() ).thenReturn( Arrays.asList( leilao ) );
		
		Calendar sabado = criarData( 29, 11, 2014);
		Calendar segunda = criarData( 1, 12, 2014);
		
		when( relogio.hoje() ).thenReturn( sabado );
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(avaliador, repositorioLeilao, repositorioDePagamentos, relogio);
		geradorDePagamento.gerar();
		
		ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass( Pagamento.class );
		verify( repositorioDePagamentos ).salvar( argumentCaptor.capture() );
		
		Pagamento pagamento = argumentCaptor.getValue();
		
		assertThat( pagamento.getData().get( Calendar.DAY_OF_WEEK), equalTo( Calendar.MONDAY ) );
		assertThat( pagamento.getData(), equalTo( segunda ) );
		
	}
	
	@Test
	public void gerarPagamentoNoDomingoQueCairaNaSegunda(){
		Leilao leilao = criarLeilao();
		
		when( repositorioLeilao.encerrados() ).thenReturn( Arrays.asList( leilao ) );
		
		Calendar domingo = criarData( 30, 11, 2014);
		Calendar segunda = criarData( 1, 12, 2014);
		
		when( relogio.hoje() ).thenReturn( domingo );
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(avaliador, repositorioLeilao, repositorioDePagamentos, relogio);
		geradorDePagamento.gerar();
		
		ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass( Pagamento.class );
		verify( repositorioDePagamentos ).salvar( argumentCaptor.capture() );
		
		Pagamento pagamento = argumentCaptor.getValue();
		
		assertThat( pagamento.getData().get( Calendar.DAY_OF_WEEK), equalTo( Calendar.MONDAY ) );
		assertThat( pagamento.getData(), equalTo( segunda ) );
		
	}
	
	private Calendar criarData( int dia, int mes, int ano ){
		Calendar data = Calendar.getInstance();
		data.setLenient( false );
		
		data.set(ano, --mes, dia);
		data.clear( Calendar.HOUR_OF_DAY);
		data.clear( Calendar.MINUTE);
		data.clear( Calendar.SECOND);
		data.clear( Calendar.MILLISECOND);
		data.clear( Calendar.HOUR);
		
		System.out.printf( "Data Criada: %1$td/%1$tm/%1$tY\n", data );
		
		return data;
	}
}
