package br.com.caelum.leilao.servico;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioLeilao;
import br.com.caelum.leilao.infra.email.Carteiro;

@RunWith(MockitoJUnitRunner.class)
public class EncerradorDeLeilaoTest {
	private static Calendar seteDiasAtrasComHoraAtual;
	private static Calendar dataMesPassada;
	private static Calendar dataAnoPassado;
	private static Calendar ontemManha;
	private static Calendar seisDiasNoLimiteParaSeteDias;
	
	private final Leilao leilao7DiasAtras = new CriadorDeLeilao().para( "Carro Importado" ).naData( seteDiasAtrasComHoraAtual ).constroi();
	private final Leilao leilao1MesAtras = new CriadorDeLeilao().para( "Video game" ).naData( dataMesPassada ).constroi();
	private final Leilao leilao1AnoAtras = new CriadorDeLeilao().para( "Quadros" ).naData( dataAnoPassado ).constroi();
	
	private final Leilao leilaoOntem = new CriadorDeLeilao().para( "Carro Importado" ).naData( ontemManha ).constroi();
	private final Leilao leilaoSeteDiasMaisUmaHora = new CriadorDeLeilao().para( "Video game" ).naData( seisDiasNoLimiteParaSeteDias ).constroi();
	
	@Mock
	private RepositorioLeilao repositorioMock;
	
	@Mock
	private Carteiro carteiro;
	
	@BeforeClass
	public static void iniciarDatas(){
		seteDiasAtrasComHoraAtual = Calendar.getInstance();
		seteDiasAtrasComHoraAtual.add(Calendar.DAY_OF_MONTH, -7);
		
		dataMesPassada = Calendar.getInstance();
		dataMesPassada.add(Calendar.MONTH, -1);
		
		dataAnoPassado = Calendar.getInstance();
		dataAnoPassado.add(Calendar.YEAR, -1);
		
		ontemManha = Calendar.getInstance();
		ontemManha.add(Calendar.DAY_OF_MONTH, -1);
		ontemManha.set( Calendar.HOUR_OF_DAY, 10 );
		
		seisDiasNoLimiteParaSeteDias = Calendar.getInstance();
		seisDiasNoLimiteParaSeteDias.add(Calendar.DAY_OF_MONTH, -6);
		seisDiasNoLimiteParaSeteDias.add( Calendar.HOUR_OF_DAY, 23 );
		seisDiasNoLimiteParaSeteDias.add( Calendar.MINUTE, 59 );
		seisDiasNoLimiteParaSeteDias.add( Calendar.SECOND, 59 );
	}
	

	@Test
	public void encerrarLeilaoQueIniciouMaisDeUmaSemanaAtras(){
		
		when( repositorioMock.correntes() ).thenReturn( Arrays.asList( leilao7DiasAtras, leilao1MesAtras, leilao1AnoAtras) );
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao( repositorioMock, carteiro );
		encerradorDeLeilao.encerra();
		
		assertThat( encerradorDeLeilao.getTotalEncerrados() , equalTo( 3 ) );
		assertTrue( leilao7DiasAtras.isEncerrado() );
		assertTrue( leilao1MesAtras.isEncerrado() );
		assertTrue( leilao1AnoAtras.isEncerrado() );
		
		InOrder inOrder = inOrder( repositorioMock, carteiro );
		verificarSequenciaExecucao( inOrder, leilao7DiasAtras );
		verificarSequenciaExecucao( inOrder, leilao1MesAtras );
		verificarSequenciaExecucao( inOrder, leilao1AnoAtras );
	}


	private void verificarSequenciaExecucao(InOrder inOrder, Leilao leilao) {
		inOrder.verify( repositorioMock, times( 1 ) ).atualiza( leilao );
		inOrder.verify( carteiro, times( 1 ) ).envia( leilao );
	}
	
	@Test
	public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras(){		
		
		when( repositorioMock.correntes() ).thenReturn( Arrays.asList( leilaoOntem, leilaoSeteDiasMaisUmaHora) );
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao( repositorioMock, carteiro );
		encerradorDeLeilao.encerra();
		
		assertThat( encerradorDeLeilao.getTotalEncerrados() , equalTo( 0 ) );
		assertFalse( leilaoOntem.isEncerrado() );
		assertFalse( leilaoSeteDiasMaisUmaHora.isEncerrado() );
		
		verify( repositorioMock, never() ).atualiza( leilaoOntem );
		verify( repositorioMock, never() ).atualiza( leilaoSeteDiasMaisUmaHora );
	}
	
	@Test
	public void naoDeveExecutarAcaoNenhuma(){		
		when( repositorioMock.correntes() ).thenReturn( new ArrayList<Leilao>() );
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao( repositorioMock, carteiro );
		encerradorDeLeilao.encerra();
		
		assertThat( encerradorDeLeilao.getTotalEncerrados() , equalTo( 0 ) );
	}
	
	@Test
	public void deveContinuarMesmoQuandoRecebeExceptionEmUmEncerramento(){		
		when( repositorioMock.correntes() ).thenReturn( Arrays.asList( leilao7DiasAtras, leilao1MesAtras ) );
		doThrow( new RuntimeException() ).when( repositorioMock ).atualiza( leilao7DiasAtras );
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao( repositorioMock, carteiro );
		encerradorDeLeilao.encerra();

		InOrder inOrder = inOrder( carteiro, repositorioMock );
		
		inOrder.verify( carteiro, never() ).envia( leilao7DiasAtras );
		inOrder.verify( repositorioMock ).atualiza( leilao1MesAtras );
		inOrder.verify( carteiro ).envia( leilao1MesAtras );
		
		assertThat( encerradorDeLeilao.getTotalEncerrados() , equalTo( 1 ) );
	}
	
	@Test
	public void emCasoDeErroEmTodosNuncaInvocarOCarteiro(){		
		when( repositorioMock.correntes() ).thenReturn( Arrays.asList( leilao7DiasAtras, leilao1MesAtras ) );
		
		doThrow( new RuntimeException() ).when( repositorioMock ).atualiza( any( Leilao.class ) );
		
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao( repositorioMock, carteiro );
		encerradorDeLeilao.encerra();

		verify( carteiro, never() ).envia( any( Leilao.class ) );
		
		assertThat( encerradorDeLeilao.getTotalEncerrados() , equalTo( 0 ) );
	}
}
