package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioLeilao;
import br.com.caelum.leilao.infra.email.Carteiro;

public class EncerradorDeLeilao {

    private int total = 0;
    private final RepositorioLeilao dao;
    private final Carteiro carteiro;

    public EncerradorDeLeilao(RepositorioLeilao dao, Carteiro carteiro) {
        this.dao = dao;
        this.carteiro = carteiro;
    }

    public void encerra() {
        List<Leilao> todosLeiloesCorrentes = dao.correntes();

        for (Leilao leilao : todosLeiloesCorrentes) {
        	
        	try{
	            if (comecouSemanaPassada(leilao)) {
	                leilao.encerra();
	                dao.atualiza(leilao);
	                carteiro.envia(leilao);
	                total++;
	            }
	            
        	}catch( Exception e ){
        		//Executa alguma ação
        		System.out.println( e );
        	}
        }
    }

    private boolean comecouSemanaPassada(Leilao leilao) {
        return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
    }

    private int diasEntre(Calendar inicio, Calendar fim) {
        Calendar data = (Calendar) inicio.clone();
        int diasNoIntervalo = 0;
        while (data.before(fim)) {
            data.add(Calendar.DAY_OF_MONTH, 1);
            diasNoIntervalo++;
        }
        return diasNoIntervalo;
    }

    public int getTotalEncerrados() {
        return total;
    }
}