package br.com.caelum.leilao.infra.data;

import java.util.Calendar;

public class RelogioSistema implements Relogio {

	@Override
	public Calendar hoje() {
		return Calendar.getInstance();
	}
}
