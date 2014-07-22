package org.yats.connectivity.nj4x;

import com.jfx.Broker;
import com.jfx.ErrUnknownSymbol;
import com.jfx.MT4;
import com.jfx.TickInfo;
import com.jfx.strategy.Strategy;
import com.jfx.strategy.StrategyRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.MarketDataMsg;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Connection {

    final Logger log = LoggerFactory.getLogger(Connection.class);

    private final String JFX_SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        new Connection().go();
    }

    public Connection() {
    }

    public void go() throws IOException {
        strategy = new Strategy() {
            @Override
            public synchronized void init(String symbol, int period,
                                          StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
                super.init(symbol, period, strategyRunner);
                setAutoRefreshRates(false);
            }
        };


        String configDir = System.getProperty("user.home")+"/secure";
        PropertiesReader jfxPropertyReader = PropertiesReader.createFromConfigFile(configDir + "/jfx.txt");
        System.setProperty("jfx_activation_key", jfxPropertyReader.get("password"));

        PropertiesReader oandaPropertyReader = PropertiesReader.createFromConfigFile(configDir + "/mt4oanda.txt");

        strategy.connect(JFX_SERVER_ADDRESS, 7788, new Broker("oanda-MT4"),
                oandaPropertyReader.get("username"), oandaPropertyReader.get("password"));

        strategy.addTerminal(Strategy.TerminalType.ORDERS_WORKER).connect();// as many as you need parallel order processors

        strategy.addTerminal(Strategy.TerminalType.TICK_WORKER)
                .addTickListener("EURUSD", new MyTickListener("EURUSD"))
                .addTickListener("GBPUSD", new MyTickListener("GBPUSD"))
//                .addTickListener("USDZAR", new MyTickListener("GBPUSD"))
//                .addTickListener("USDTRY", new MyTickListener("GBPUSD"))
                .addTickListener("USDCNY", new MyTickListener("USDCNY"))
                .addTickListener("USDJPY", new MyTickListener("USDJPY"))
                .addTickListener("USDCAD", new MyTickListener("USDCAD"))
                .addTickListener("USDINR", new MyTickListener("USDINR"))
                .connect();


        System.out.println("\n===");
        System.out.println("Initialisation done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.out.print(System.in.read());
        System.out.println("\nexiting...\n");

        executor.shutdown();
        strategy.disconnect();
        System.exit(0);
    }

    private class MyTickListener implements Strategy.TickListener {

        private String symbol;
        Future<Integer> orderJob;
        Future<Boolean> orderCloseJob;
        Sender<MarketDataMsg> sender;

        private MyTickListener(String _symbol) {
            this.symbol = _symbol;
            orderJob = null;
            orderCloseJob = null;
            Config c = Config.fromProperties(Config.createRealProperties());
            sender = new Sender<MarketDataMsg>(c.getExchangeMarketData(), c.getServerIP());
        }

        @Override
        public void onTick(final TickInfo tick, MT4 connection) {

            MarketDataMsg data = new MarketDataMsg();

            data.productId=symbol;
            data.bid= Decimal.fromDouble(tick.bid).toString();
            data.ask= Decimal.fromDouble(tick.ask).toString();
            data.bidSize="1";
            data.askSize="1";
            data.timestamp= Tool.getUTCTimestampString();
            sender.publish(data.getTopic(), data);

            log.debug(data.toString());

            System.out.println(data);
        }

    } // MyTickListener

    private Strategy strategy;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);


}
