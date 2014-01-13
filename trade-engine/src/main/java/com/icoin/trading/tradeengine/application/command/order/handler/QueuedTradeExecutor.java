package com.icoin.trading.tradeengine.application.command.order.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.icoin.trading.tradeengine.application.command.order.ExecuteBuyOrderCommand;
import com.icoin.trading.tradeengine.application.command.order.ExecuteSellOrderCommand;
import com.icoin.trading.tradeengine.domain.model.order.Order;
import com.icoin.trading.tradeengine.domain.model.order.OrderBookId;
import com.icoin.trading.tradeengine.domain.model.order.OrderId;
import com.icoin.trading.tradeengine.domain.model.order.OrderRepository;
import com.icoin.trading.tradeengine.query.order.OrderBookEntry;
import com.icoin.trading.tradeengine.query.order.repositories.OrderBookQueryRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.homhon.util.Collections.isEmpty;

/**
 * Created with IntelliJ IDEA.
 * User: liougehooa
 * Date: 13-12-3
 * Time: AM7:47
 * To change this template use File | Settings | File Templates.
 */
@Component
public class QueuedTradeExecutor implements TradeExecutor {
    private static Logger logger = LoggerFactory.getLogger(QueuedTradeExecutor.class);
    private Map<OrderBookId, BlockingQueue<Order>> orderBookPool;
    private CommandGateway commandGateway;
    private OrderRepository orderRepository;
    private ExecutorService executor;
    private OrderBookQueryRepository orderBookRepository;
    private AtomicBoolean halted;

    //todo, load orderbook id from the repo
    //todo cannot reloaded runtime
//    QueuedTradeExecutor(Map<OrderBookId, BlockingQueue<AbstractOrder>> orderBookPool) {
//        this.orderBookPool = ImmutableMap.copyOf(orderBookPool);
//    }

    @Autowired
    public QueuedTradeExecutor(OrderBookQueryRepository orderBookRepository,
                               CommandGateway commandGateway,
                               OrderRepository orderRepository) {
        this.orderBookRepository = orderBookRepository;
        this.commandGateway = commandGateway;
        this.orderRepository = orderRepository;
        initialize();
        start();
        resolveNotExecutedOrders();
    }

    public void reinitialize() {
        logger.info("Shutting down executor.");
        stop();
        logger.info("Stopped executor.");
        if(executor !=null){
            executor.shutdown();
            logger.info("Shut down executor already.");
        }

        logger.info("reinitializing orderbook queues.");
        initialize();
        logger.info("reinitialized orderbook queues.");
        logger.info("starting executors...");
        start();
        logger.info("resolving unfinished orders ...");
        resolveNotExecutedOrders();
        logger.info("resolved unfinished orders ...");
    }

    private void stop() {
        halted.set(true);
    }

    protected void initialize() {
        initOrderBookPool(orderBookRepository);
//        executor = Executors.newFixedThreadPool(orderBookPool.size());
    }

    //OrderBookListener.handleTradeExecuted, add lastTradedTime
    private void resolveNotExecutedOrders() {
        for (OrderBookId orderBookId : orderBookPool.keySet()) {
            Order highestBuyOrder = orderRepository.findHighestPricePendingBuyOrder(orderBookId);

            if (highestBuyOrder == null) {
                continue;
            }

            Order lowestSellOrder = orderRepository.findLowestPricePendingSellOrder(orderBookId);
            if (lowestSellOrder == null) {
                continue;
            }

            //some orders not executed
            if (highestBuyOrder.getItemPrice().isLessThan(lowestSellOrder.getItemPrice())) {
                continue;
            }

            OrderBookEntry orderBook = orderBookRepository.findOne(orderBookId.toString());
            Date lastTradedTime = orderBook.getLastTradedTime();

            List<Order> orders = orderRepository.findPlacedPendingOrdersAfter(lastTradedTime, orderBookId, 100);

            while (!isEmpty(orders)) {
                for (Order order : orders) {
                    execute(order);
                }

                orders = orderRepository.findPlacedPendingOrdersAfter(lastTradedTime, orderBookId, 100);
            }
        }
    }

    private void initOrderBookPool(OrderBookQueryRepository orderBookRepository) {
        final Iterable<OrderBookEntry> orderBookEntries = orderBookRepository.findAll();
        final HashMap<OrderBookId, BlockingQueue<Order>> map = Maps.newHashMap();

        for (OrderBookEntry orderBook : orderBookEntries) {
            map.put(new OrderBookId(orderBook.getPrimaryKey()),
                    new LinkedBlockingDeque<Order>());
            logger.warn("initialized order book trading pool with {}", orderBook);
        }
        this.orderBookPool = ImmutableMap.copyOf(map);
    }

    @Override
    public void execute(Order order) {
        final OrderBookId orderBookId = order.getOrderBookId();

        if (!orderBookPool.containsKey(orderBookId)) {
            logger.warn("order book id is {}, not in the pool {}", orderBookId, orderBookPool.keySet());
            return;
        }

        final BlockingQueue<Order> orderQueue = orderBookPool.get(orderBookId);

        try {
            orderQueue.put(order);
        } catch (InterruptedException e) {
            logger.warn("Interrupted Queue for orderbookId {} when En-queuing", orderBookId);
        }
    }

    class TradeExecutor implements Runnable {
        private final BlockingQueue<Order> queue;
        private final OrderBookId orderBookId;
        private final AtomicBoolean stop;

        TradeExecutor(OrderBookId orderBookId, AtomicBoolean stop, BlockingQueue<Order> q) {
            this.orderBookId = orderBookId;
            this.stop = stop;
            this.queue = q;
        }

        public void run() {
            try {
                while (!stop.get()) {
                    final Order take = queue.take();
                    consume(take);
                }
            } catch (InterruptedException ex) {
                logger.warn("Interruppted Queue for orderbookId {} when De-queuing", orderBookId);
            }
        }

        void consume(Order order) {
            logger.info("Excuting order {}:{}", order.getOrderType(), order);
            switch (order.getOrderType()) {
                case BUY:
                    final ExecuteBuyOrderCommand executeBuyOrderCommand =
                            new ExecuteBuyOrderCommand(new OrderId(order.getPrimaryKey()),
                                    order.getPortfolioId(),
                                    order.getOrderBookId(),
                                    order.getTransactionId(),
                                    order.getItemRemaining(),
                                    order.getItemPrice(),
                                    order.getPlaceDate());
                    commandGateway.send(executeBuyOrderCommand);
                    break;
                case SELL:
                    final ExecuteSellOrderCommand executeSellOrderCommand =
                            new ExecuteSellOrderCommand(new OrderId(order.getPrimaryKey()),
                                    order.getPortfolioId(),
                                    order.getOrderBookId(),
                                    order.getTransactionId(),
                                    order.getItemRemaining(),
                                    order.getItemPrice(),
                                    order.getPlaceDate());
                    commandGateway.send(executeSellOrderCommand);
                    break;
                default:
                    throw new UnsupportedOperationException("order type not supported for executing, type:" + order.getOrderType());
            }
        }
    }

    class Setup implements Runnable {
        final ExecutorService executor;

        private Setup() {
            int size = orderBookPool.size();
            if (size <= 0) {
                size = 1;
            }
            executor = Executors.newFixedThreadPool(size);
        }

        @Override
        public void run() {
//             ExecutorService executor = Executors.newFixedThreadPool(orderBookPool.size());
            for (OrderBookId orderBookId : orderBookPool.keySet()) {
                final BlockingQueue<Order> queue = orderBookPool.get(orderBookId);

                executor.execute(
                        new TradeExecutor(orderBookId, halted, queue)
                );
            }
        }
    }

    public void start() {
        final Runnable setup = new Setup();
        halted = new AtomicBoolean(false);
        new Thread(setup).start();
    }
}
