package org.bergefall.se.server.strategy.beans;

import java.io.IOException;

import org.bergefall.base.strategy.IntraStrategyBeanMsg;
import org.bergefall.base.strategy.Status;
import org.bergefall.base.strategy.StrategyToken;
import org.bergefall.base.strategy.basicbeans.StoreTradeToDb;
import org.bergefall.common.config.MetaTraderConfig;
import org.bergefall.common.log.RotatingFileHandler;
import org.bergefall.common.log.system.SystemLoggerIf;
import org.bergefall.protocol.metatrader.MetaTraderProtos.MetaTraderMessage.Type;
import org.bergefall.protocol.metatrader.MetaTraderProtos.Trade;

public class TradeHandlingBean extends StoreTradeToDb {

	/**
	 * 
	 */
	private static final long serialVersionUID = 736085641456361407L;
	private static final char cSeparator = ',';
	
	RotatingFileHandler fileHander;

	@Override
	public Status execute(StrategyToken token, IntraStrategyBeanMsg intraMsg) {
		super.execute(token, intraMsg);
		if (Type.Trade == msg.getMsgType()) {
			handleTrade(msg.getTrade());
		}
		return status;
	}
	
	protected void handleTrade(Trade trade) {
		try {
			fileHander.write(formatLogEntry(trade));
		} catch (IOException e) {
			status.setCode(Status.ERROR);
			status.setMsg(SystemLoggerIf.getStacktrace(e));
		}
		
	}

	@Override
	public void initBean(MetaTraderConfig config) {
		fileHander = new RotatingFileHandler("./", false, 60_000, "Trades-");
		storeToDB = false;
	}
	
	protected String formatLogEntry(Trade trade) {
		StringBuilder buf = new StringBuilder(256);
		buf.append(trade.getInstrument().getName());
		buf.append(cSeparator);
		buf.append(trade.getDate());
		buf.append(cSeparator);
		buf.append(trade.getIsEntry());
		buf.append(cSeparator);
		buf.append(trade.getPrice());
		buf.append(cSeparator);
		buf.append(trade.getQty());
		buf.append(cSeparator);
		buf.append(trade.getNetProfit());
		buf.append(cSeparator);
		buf.append(trade.getGrossProfit());
		buf.append(cSeparator);
		buf.append(trade.getCommission());
		return buf.toString();
	}
	@Override
	public void shutdownHook() {
		if (null != fileHander) {
			fileHander.close();
		}
	}
}